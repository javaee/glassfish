/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.jvnet.hk2.component;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Extract;
import org.jvnet.hk2.annotations.Configurable;

import java.lang.reflect.*;
import java.lang.annotation.Annotation;
import java.util.*;

import com.sun.hk2.component.ScopeInstance;

/**
 * ComponentManager is responsible for instantiating components, as well as injecting
 * and extracting resources from components. Components are defined with an interface they
 * implement. Usually a component will implement the {@link Service} interface so the
 * corresponding META-INF/services entries are created automatically.
 *
 * The component manager is a thread safe, it keeps no context between invocations.
 *
 * @author Jerome Dochez
 * @author Kohsuke Kawaguchi
 */
@Service(scope=Singleton.class)
public class ComponentManager extends InjectionManager<Inject> {

    /**
     * Used to load services implementation from an underlying module system
     */
    final ServiceLookup serviceLookup;

    /**
     * Components of the singleton scope.
     */
    /*package*/ final ScopeInstance singletonScope;

    /**
     * Cached providers
     */
    final private Map<ResourceLocator, Class> cachedProviders = new Hashtable();

    /**
     * Creates a new component manager capable of instantiating components, injecting
     * and extracting resources from these components. Resources are procured and stored
     * in the resource manager instance passed as a parameter. The resourceManager
     * instance cannot be null.
     *
     * @param serviceLookup Lookup facility to retrieve services implementations
     */
    public ComponentManager(ServiceLookup serviceLookup) {
        this.serviceLookup = serviceLookup;
        this.singletonScope = new ScopeInstance();
        // make the component manager itself available
        singletonScope.store.add(new ResourceLocator<ComponentManager>(ComponentManager.class),this);
    }

    public ComponentManager(ServiceLookup serviceLookup, ResourceManager resourceManager) {
        this.serviceLookup = serviceLookup;
        this.singletonScope = new ScopeInstance("singleton", resourceManager);

        // make the component manager itself available
        singletonScope.store.add(new ResourceLocator<ComponentManager>(ComponentManager.class),this);
        
    }

    /**
     * Obtains a reference to the component inside the manager.
     *
     * <p>
     * This is the "new Foo()" equivalent in the IoC world.
     *
     * <p> 
     * Depending on the {@link Scope} of the component, a new instance
     * might be created, or an existing instance might be returned.
     *
     * @return
     *      non-null.
     * @throws ComponentException
     *      If failed to obtain a requested instance.
     *      In practice, failure only happens when we try to create a
     *      new instance of the component.
     */
    public <T> T getComponent(Class<T> clazz) throws ComponentException {
        Service svc = clazz.getAnnotation(Service.class);
        if(svc==null)

            throw new ComponentException(clazz+" is not a service");
        try {
            ScopeInstance si = getScope(svc);
            if(si==null)    // prototype scope?
                return instantiate(clazz,svc,null);

            // TODO: ResourceLocator needs to get contract class, not service class.
            ResourceLocator<T> loc = new ResourceLocator<T>(svc, clazz);

            T c = si.store.lookup(loc);
            if(c!=null) return c; // component already exists

            // so far the execution is concurrent, so to avoid creating
            // the same component twice, enter a lock
            final Object lock = instanciationLock[(loc.hashCode()&0x7FFFFFFF)%instanciationLock.length];
            synchronized(lock) {
                c = si.store.lookup(loc);
                if(c!=null)
                    return c; // glad we locked!

                // really create a new instance now
                c = instantiate(clazz,svc,si);
                si.store.add(loc,c);
            }

            return c;
        } catch (ComponentException e) {
            throw new ComponentException("Failed to obtain "+clazz+" component",e);
        }
    }

    /**
     * Add an already instantiated component to this manager. The component has
     * been instantiated by external code, however dependency injection, PostConstruct
     * invocation and dependency extraction will be performed on this instance before
     * it is store in the relevant scope's resource manager.
     *
     * @param name name of the component, could be default name
     * @param component component instance
     * @throws ComponentException if the passed object is not an HK2 component or
     * injection/extraction failed.
     */
    public <T> void addComponent(String name, T component)
        throws ComponentException {

        Service svc = component.getClass().getAnnotation(Service.class);
        if (svc==null) {
            throw new ComponentException(component.getClass() + " is not annotated with @Service");
        }
        // perform injection
        inject(component);

        // postContruct call if any
        if(component instanceof PostConstruct)
            ((PostConstruct)component).postConstruct();

        ScopeInstance si = getScope(svc);
        if(si!=null)
            // extraction amounts to no-op if this is prototype scope. so skip that.
            extract(component, si);

        // return component for future use.
        si.store.add(new ResourceLocator(name, component.getClass()), component);
    }

    private static final Object[] instanciationLock = new Object[32];
    static {
        for( int i=0; i<instanciationLock.length; i++ )
            instanciationLock[i] = new Object();
    }

    /**
     * Determines the {@link ScopeInstance} that stores the component.
     *
     * @return
     *      null for prototype scope. (Note that in {@link Scope#current()}
     *      null return value is an error.)
     */
    private ScopeInstance getScope(Service svc) throws ComponentException {
        Class<? extends Scope> s = svc.scope();
        // for performance reason and to avoid infinite recursion,
        // recognize these two fundamental built-in scopes and process them differently.
        if(s==Singleton.class)
            return singletonScope;
        if(s==PerLookup.class)
            return null;

        // for all the other scopes, including user-defined ones.
        Scope scope = getComponent(s);
        ScopeInstance si = scope.current();
        if(si==null) // scope is an extension point, so beware for broken implementations
            throw new ComponentException(scope+" returned null from the current() method");
        return si;
    }

    /**
     * Instantiate a new component instance and performs injection.
     *
     * @param impl
     *      Implementation class to be instanciated.
     * @param svc
     *      Service annotation on the class.
     * @param si
     *      the instanciated component will be put into this scope.
     *      this is used to store all {@link Extract extracted} resources.
     * @return a new instance of a service implementing the contract interface
     */
    private <T> T instantiate(Class<T> impl, Service svc, ScopeInstance si) throws ComponentException {

        T component;
        try {
            if(svc.factory()!=Factory.class) {
                // create a component via a factory
                Factory factory = getComponent(svc.factory());
                Object o = factory.getObject();
                try {
                    component = impl.cast(o);
                } catch (ClassCastException e) {
                    throw new ComponentException(svc.factory()+" returned "+o+" but expected "+impl, e);
                }
            } else {
                component = impl.newInstance();
            }
        } catch (InstantiationException e) {
            throw new ComponentException("Exception while instantiating component of type " + impl, e);
        } catch (IllegalAccessException e) {
            throw new ComponentException("Exception while instantiating component of type " + impl, e);
        }

        // perform injection
        inject(component);

        // postContruct call if any
        if(component instanceof PostConstruct)
            ((PostConstruct)component).postConstruct();

        if(si!=null)
            // extraction amounts to no-op if this is prototype scope. so skip that.
            extract(component, si);

        // return component for future use.
        return component;
    }

    /**
     * Initializes the component by performing injection.
     *
     * @param component component instance to inject
     * @throws ComponentException
     *      if injection failed for some reason. 
     */
    private  void inject(Object component) throws ComponentException {
        assert component!=null;

        super.inject(component, Inject.class);

/*        // TODO: faster implementation needed.

        Class currentClass = component.getClass();
        while (!currentClass.equals(Object.class)) {
            // get the list of the instances variable
            for (Field field : currentClass.getDeclaredFields()) {
                Inject inject = field.getAnnotation(Inject.class);
                if (inject == null)     continue;

                Class<?> fieldType = field.getType();
                try {
                    Object value = getValue(inject, fieldType);
                    if (value != null) {
                        field.setAccessible(true);
                        field.set(component, value);
                    } else {
                        if(!inject.optional())
                            throw new ComponentException("Failed to find component to be injected to "+field.toGenericString());
                    }
                } catch (IllegalAccessException e) {
                    throw new ComponentException("Injection failed on " + field.toGenericString(), e);
                }
            }
            for (Method method : currentClass.getDeclaredMethods()) {
                Inject inject = method.getAnnotation(Inject.class);
                if (inject == null)     continue;

                if (method.getReturnType() != void.class) {
                    throw new ComponentException("Injection failed on " + method.toGenericString()
                            + " : setter method is not declared with a void return type");
                }

                Class<?>[] paramTypes = method.getParameterTypes();

                if (paramTypes.length > 1) {
                    throw new ComponentException("injection failed on " + method.toGenericString() + " : setter method takes more than 1 parameter");
                }
                if (paramTypes.length == 0) {
                    throw new ComponentException("injection failed on " + method.toGenericString() + " : setter method does not take a parameter");
                }

                try {
                    Object value = getValue(inject, paramTypes[0]);
                    if (value != null) {
                        method.setAccessible(true);
                        method.invoke(component, value);
                    } else {
                        if (!inject.optional())
                            throw new ComponentException("Failed to find component to be injected to " + method.toGenericString());
                    }
                } catch (IllegalAccessException e) {
                    throw new ComponentException("Injection failed on " + method.toGenericString(), e);
                } catch (InvocationTargetException e) {
                    throw new ComponentException("Injection failed on " + method.toGenericString(), e);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        */
    }

    /**
     * Extracts resources identified with {@link Extract} annotations.
     *
     * TODO: it's not clear what type under which such components should be stored.
     *
     * @param component the component we should extract resources from
     * @throws ComponentException if the resource extract fail 
     */
    private void extract(Object component, ScopeInstance si) throws ComponentException {

        Class<?> currentClass = component.getClass();
        while (!currentClass.equals(Object.class)) {
            for (Field field : currentClass.getDeclaredFields()) {
                Extract extract = field.getAnnotation(Extract.class);
                if (extract == null)    continue;

                try {
                    field.setAccessible(true);
                    Object value = field.get(component);
                    Class<?> type = field.getType();

//                    if (LOGGER.isLoggable(Level.FINER)) {
//                        LOGGER.log(Level.FINER, "Extracting resource " + value + " returned from " + field);
//                    }
                    if (value!=null) {
                        extractValue(value, si, type, extract);
                    } else {
//                        if (LOGGER.isLoggable(Level.FINE)) {
//                            LOGGER.log(Level.FINE, "Resource returned from " + field + " is null");
//                        }
                    }
                } catch (IllegalArgumentException ex) {
                    throw new ComponentException("Extraction failed on " + field, ex);

                } catch (IllegalAccessException ex) {
                    throw new ComponentException("Extraction failed on " + field, ex);
                }
            }
            for (Method method : currentClass.getDeclaredMethods()) {
                Extract extract = method.getAnnotation(Extract.class);
                if (extract == null)    continue;

                Class<?> type = method.getReturnType();
                if (type == null) {
                    throw new ComponentException("Extraction failed : " + method + " has a void return type");
                }
                if (method.getParameterTypes().length > 0) {
                    throw new ComponentException("Extraction failed : " + method + " takes parameters, it should not");
                }
                try {
                    method.setAccessible(true);
                    Object value = method.invoke(component);
//                    if (LOGGER.isLoggable(Level.FINER)) {
//                        LOGGER.log(Level.FINER, "Extracting resource " + value + " returned from " + method);
//                    }
                    if (value!=null) {
                        extractValue(value, si, type, extract);
                    } else {
//                        if (LOGGER.isLoggable(Level.FINE)) {
//                            LOGGER.log(Level.FINE, "Resource returned from " + method + " is null");
//                        }
                    }
                } catch (IllegalArgumentException ex) {
                    throw new ComponentException("Extraction failed on " + method.toGenericString(), ex);
                } catch (InvocationTargetException ex) {
                    throw new ComponentException("Extraction failed on " + method.toGenericString(), ex);
                } catch (IllegalAccessException ex) {
                    throw new ComponentException("Extraction failed on " + method.toGenericString(), ex);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

    }

    @SuppressWarnings("unchecked")
    private void extractValue(Object value, ScopeInstance si, Class type, Extract extract) {
        if(value instanceof Iterable) {
            for (Object o : Iterable.class.cast(value)) {
                si.store.add(null, o);
            }
        } else
        if (type.isArray()) {
            Object[] values = (Object[]) value;
            for (Object o : values) {
                si.store.add(null, o);
            }
        } else {
            si.store.add(new ResourceLocator(extract, type), value);
        }
    }


    /**
     * Obtains all the components that implement the given contract.
     *
     * <p>
     * If necessary, the components are created.
     *
     * @param contract type of service to load/intitialized
     * @return
     *      can be empty but never null.
     */
    public <T> Collection<T> getComponents(Class<T> contract)  {

        Set<T> providers = new HashSet<T>();
        for (Class<? extends T> providerClass : serviceLookup.getProvidersClass(contract)) {

            try {
                Service service = providerClass.getAnnotation(Service.class);
                assert service!=null;

                Configurable configurable = providerClass.getAnnotation(Configurable.class);
                if (configurable!=null) {
                    // get them from the scope.
                    ScopeInstance scope = getScope(service);
                    providers.addAll(scope.store.lookupAll(providerClass));
                } else {
                    providers.add(getComponent(providerClass));
                }
            } catch(ComponentException e) {
                System.out.println("Cannot instantiate service " + providerClass + e);
            }
        }
        return providers;
        
    }


    /**
     * Loads a component that implements the given contract.
     *
     * <p>
     * If this method is used with a non-null name, then a service
     * that implements the given contract AND have the specified name
     * will be returned.
     *
     * <p>
     * If this method is used with null name, then one service
     * that implements the given contract will be returned, regardless of its name.
     * If there are multiple service classes, which one gets returned is unspecified.
     *
     * @return
     *      null if no such servce exists. 
     */
    public <T> T getComponent(ResourceLocator<T> locator) throws ComponentException {
        // TODO: more efficient implementation needed, but for now to get things moving


        Class<? extends T> cachedProvider = cachedProviders.get(locator);
        if (cachedProvider!=null) {
            return getComponent(cachedProvider);
        }
        for (Class<? extends T>  providerClass : serviceLookup.getProvidersClass(locator.getType())) {
            // let's cache this for future usage
            ResourceLocator cacheID = new ResourceLocator(providerClass.getAnnotation(Service.class),
                locator.getType());
            cachedProviders.put(cacheID, providerClass);

            if(locator.getName()!=null
            && !providerClass.getAnnotation(Service.class).name().equals(locator.getName()))
                continue;   // name doesn't match

            return getComponent(providerClass);
        }

        return null; // not found 
    }

    /**
     * Destroys the component instance explicitly.
     * <p>
     * Normally, a component is destroyed when the scope it's in is destroyed,
     * but you can call this method to force the early destruction.
     * <p>
     * Note that as soon as someone else tries to {@link #getComponent(Class)}
     * a component will be reinstanciated.
     */
    public void releaseComponent(Object component) throws ComponentException {
        Service svc = component.getClass().getAnnotation(Service.class);
        if(svc==null)
            throw new ComponentException("Unable to release "+component+"; no @Service annotation");
        ScopeInstance si = getScope(svc);
        if(si==null)
            // if si==null, then this component is prototype scope, so no there's store.
            // as per the prototype scope contract, there's no @PreDestroy
            return;

        // call the preDestroy if defined on the service
        if(component instanceof PreDestroy)
            ((PreDestroy)component).preDestroy();
        
        si.store.remove(component);
    }

    public final void releaseComponents(Object... components) throws ComponentException {
        for (Object c : components)
            releaseComponent(c);
    }

    public final void releaseComponents(Iterable<?> components) throws ComponentException {
        for (Object c : components)
            releaseComponent(c);
    }

    @Override
    protected boolean isOptional(Inject annotation) {
        return annotation.optional();
    }

    /**
     * Obtains the value to inject, based on the type and {@link Inject} annotation.
     */
    @SuppressWarnings("unchecked")
    protected Object getValue(AnnotatedElement target, Class type) throws ComponentException {


        Inject inject = target.getAnnotation(Inject.class);
        if (type.isArray()) {
            Class<?> ct = type.getComponentType();

            Contract ctr = ct.getAnnotation(Contract.class);
            if(ctr!=null) {
                Collection instances = getComponents(ct);
                return instances.toArray((Object[])Array.newInstance(ct, instances.size()));
            }
        } else {
            Annotation svc = type.getAnnotation(Service.class);
            if(svc!=null)
                // component injection
                return getComponent(type);

            Annotation ctr = type.getAnnotation(Contract.class);
            if(ctr!=null)
                // service lookup injection
                return getComponent(new ResourceLocator(inject, type));
        }
        throw new ComponentException(type+" cannot be injected: it's neither a contract nor a service");
    }

}
