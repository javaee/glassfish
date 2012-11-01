/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.jvnet.hk2.internal;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.inject.Singleton;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.ErrorService;
import org.glassfish.hk2.api.ErrorType;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.Proxiable;
import org.glassfish.hk2.api.ProxyCtl;
import org.glassfish.hk2.api.Self;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.Unproxiable;
import org.glassfish.hk2.api.Unqualified;
import org.glassfish.hk2.api.UseProxy;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.NamedImpl;
import org.glassfish.hk2.utilities.reflection.Logger;
import org.glassfish.hk2.utilities.reflection.Pretty;
import org.glassfish.hk2.utilities.reflection.ParameterizedTypeImpl;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * This class contains a set of static utilities useful
 * for implementing HK2
 *
 * @author jwells
 *
 */
public class Utilities {
    /**
     * This utility will return the proper implementation class, taking into account that the
     * descriptor may be a factory
     *
     * @param descriptor The descriptor (reified and not null) that will be used to find the
     * implementation
     *
     * @return The real implementation class
     */
    public static Class<?> getFactoryAwareImplementationClass(ActiveDescriptor<?> descriptor) {
        if (descriptor.getDescriptorType().equals(DescriptorType.CLASS)) {
            return descriptor.getImplementationClass();
        }

        return getFactoryProductionClass(descriptor.getImplementationClass());
    }

    /**
     * Checks that the incoming lookup type is not improper in some way
     *
     * @param checkMe  class to check
     */
    public static void checkLookupType(Class<?> checkMe) {
        if (!checkMe.isAnnotation()) return;

        // If it is in annotation we need to ensure it is either a
        // scope or a qualifier
        if (checkMe.isAnnotationPresent(Scope.class)) return;
        if (checkMe.isAnnotationPresent(Qualifier.class)) return;

        throw new IllegalArgumentException("Lookup type " + checkMe + " must be a scope or annotation");
    }

    /**
     * This is used to check on the annotation set.  It must be done under protection because the annotations may
     * attempt to discover if they are equal using getDeclaredMembers permission
     *
     * @param candidateAnnotations The candidate annotations
     * @param requiredAnnotations The required annotations
     * @return true if the candidate set contains the entire required set
     */
    /* package */
    static boolean annotationContainsAll(final Set<Annotation> candidateAnnotations, final Set<Annotation> requiredAnnotations) {
        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            @Override
            public Boolean run() {
                return candidateAnnotations.containsAll(requiredAnnotations);
            }

        });

    }

    private static Field[] getDeclaredFields(final Class<?> clazz) {
        return AccessController.doPrivileged(new PrivilegedAction<Field[]>() {

            @Override
            public Field[] run() {
                return clazz.getDeclaredFields();
            }

        });

    }

    /**
     * Get the declared methods of the given class.
     *
     * @param clazz  the class
     */
    private static Method[] getDeclaredMethods(final Class<?> clazz) {
        return AccessController.doPrivileged(new PrivilegedAction<Method[]>() {

            @Override
            public Method[] run() {
                return clazz.getDeclaredMethods();
            }

        });

    }

    /**
     * Sets this accessible object to be accessible using the permissions of
     * the hk2-locator bundle (which will need the required grant)
     *
     * @param ao The object to change
     */
    /* package */
    static void setAccessible(final AccessibleObject ao) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                ao.setAccessible(true);
                return null;
            }

        });

    }

    /**
     * Converts the type to its java form, or returns the original
     *
     * @param type The type to convert
     * @return The translated type or the type itself
     */
    public static Class<?> translatePrimitiveType(Class<?> type) {
        Class<?> translation = Constants.PRIMITIVE_MAP.get(type);
        if (translation == null) return type;
        return translation;
    }

    /**
     * Calls the list of error services for the list of errors
     *
     * @param results    the results
     * @param callThese  the services to call
     */
    public static void handleErrors(NarrowResults results, LinkedList<ErrorService> callThese) {
        Collector collector = new Collector();
        for (ErrorResults errorResult : results.getErrors()) {
            for (ErrorService eService : callThese) {
                try {
                    eService.onFailure(new ErrorInformationImpl(
                            ErrorType.FAILURE_TO_REIFY,
                            errorResult.getDescriptor(),
                            errorResult.getInjectee(),
                            errorResult.getMe()));
                } catch (MultiException me) {
                    for (Throwable th : me.getErrors()) {
                        collector.addThrowable(th);
                    }
                } catch (Throwable th) {
                    collector.addThrowable(th);
                }
            }
        }

        collector.throwIfErrors();
    }

    /**
     * Loads the class using the loader from the given descriptor or the
     * classloader of the utilities class otherwise
     *
     * @param loadMe The fully qualified class name
     * @param fromMe The descriptor to use for the loader
     * @param collector The error collector to fill in if this returns null
     * @return null on failure to load (the failure will be added to the collector)
     */
    public static Class<?> loadClass(String loadMe, Descriptor fromMe, Collector collector) {
        HK2Loader loader = fromMe.getLoader();
        if (loader == null) {
            ClassLoader cl = Utilities.class.getClassLoader();
            if (cl == null) {
                cl = ClassLoader.getSystemClassLoader();
            }

            try {
                return cl.loadClass(loadMe);
            } catch (Throwable th) {
                collector.addThrowable(th);
                return null;
            }
        }

        try {
            return loader.loadClass(loadMe);
        } catch (Throwable th) {
            if (th instanceof MultiException) {
                MultiException me = (MultiException) th;

                for (Throwable th2 : me.getErrors()) {
                    collector.addThrowable(th2);
                }
            } else {
                collector.addThrowable(th);
            }

            return null;
        }

    }

    /**
     * Load the given class for the given injectee.
     *
     * @param implementation  the impl class name string
     * @param injectee        the injectee
     *
     * @return The class represented by this implementation and injectee
     */
    public static Class<?> loadClass(String implementation, Injectee injectee) {
        ClassLoader loader;
        if (injectee != null) {
            AnnotatedElement parent = injectee.getParent();

            if (parent instanceof Constructor) {
                loader = ((Constructor<?>) parent).getDeclaringClass().getClassLoader();
            } else if (parent instanceof Method) {
                loader = ((Method) parent).getDeclaringClass().getClassLoader();
            } else if (parent instanceof Field) {
                loader = ((Field) parent).getDeclaringClass().getClassLoader();
            } else {
                loader = injectee.getClass().getClassLoader();
            }
        } else {
            loader = Utilities.class.getClassLoader();
        }

        try {
            return loader.loadClass(implementation);
        } catch (Throwable th) {
            throw new MultiException(th);
        }
    }

    /**
     * Will return the class of the injection resolver annotation type, or null if
     * no injection resolver annotation can be found
     *
     * @param desc The reified descriptor to find the injection resolution on
     * @return The annotation type for this injection resolver
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Annotation> getInjectionResolverType(ActiveDescriptor<?> desc) {
        for (Type advertisedType : desc.getContractTypes()) {
            Class<?> rawClass = ReflectionHelper.getRawClass(advertisedType);

            if (!InjectionResolver.class.equals(rawClass)) continue;

            // Found the InjectionResolver
            if (!(advertisedType instanceof ParameterizedType)) {
                return null;
            }

            Type firstType = getFirstTypeArgument(advertisedType);
            if (!(firstType instanceof Class)) {
                return null;
            }

            Class<?> retVal = (Class<?>) firstType;

            if (!Annotation.class.isAssignableFrom(retVal)) {
                return null;
            }

            return (Class<? extends Annotation>) retVal;
        }

        return null;
    }

    /**
     * This method assumes that this class has already been checked...
     *
     * @param factoryClass The non-null factory class
     * @return the CLASS version of what the factory produces
     */
    private static Class<?> getFactoryProductionClass(Class<?> factoryClass) {
        for (Type type : factoryClass.getGenericInterfaces()) {
            Class<?> rawClass = ReflectionHelper.getRawClass(type);
            if (rawClass == null) continue;

            if (!Factory.class.equals(rawClass)) continue;

            Type firstType = getFirstTypeArgument(type);

            return ReflectionHelper.getRawClass(firstType);
        }

        throw new MultiException(new IllegalArgumentException(factoryClass.getName() + " is not a factory"));
    }

    /**
     * Checks to be sure the Factory class is ok
     *
     * @param factoryClass  the class to check
     * @param collector     the exception collector
     */
    public static void checkFactoryType(Class<?> factoryClass, Collector collector) {
        for (Type type : factoryClass.getGenericInterfaces()) {
            Class<?> rawClass = ReflectionHelper.getRawClass(type);
            if (rawClass == null) continue;

            if (!Factory.class.equals(rawClass)) continue;

            Type firstType = getFirstTypeArgument(type);

// TODO : remove this block after review...
// This is required for Jersey and should work now with changes to ReflectionHelper.getTypeClosure()
//
//            if (firstType instanceof TypeVariable) {
//                collector.addThrowable(new IllegalArgumentException("The class " +
//                    Pretty.clazz(factoryClass) + " has a TypeVariable as its type"));
//            }

            if (firstType instanceof WildcardType) {
                // This should not be possible
                collector.addThrowable(new IllegalArgumentException("The class " +
                        Pretty.clazz(factoryClass) + " has a Wildcard as its type"));
            }
        }

    }

    private static Set<Type> getAutoAdvertisedTypes(Type t) {
        LinkedHashSet<Type> retVal = new LinkedHashSet<Type>();
        retVal.add(t);

        Class<?> rawClass = ReflectionHelper.getRawClass(t);
        if (rawClass == null) return retVal;

        Type genericSuperclass = rawClass.getGenericSuperclass();
        while (genericSuperclass != null) {
            Class<?> rawSuperclass = ReflectionHelper.getRawClass(genericSuperclass);
            if (rawSuperclass == null) break;

            if (rawSuperclass.isAnnotationPresent(Contract.class)) {
                retVal.add(genericSuperclass);
            }

            genericSuperclass = rawSuperclass.getGenericSuperclass();
        }

        while (rawClass != null) {
            for (Type iface : rawClass.getGenericInterfaces()) {
                Class<?> ifaceClass = ReflectionHelper.getRawClass(iface);
                if (ifaceClass.isAnnotationPresent(Contract.class)) {
                    retVal.add(iface);
                }
            }

            rawClass = rawClass.getSuperclass();
        }

        return retVal;
    }

    /**
     * Creates a reified automatically generated descriptor
     *
     * @param clazz The class to create the desciptor for
     * @param locator The service locator for whom we are creating this
     * @return A reified active descriptor
     *
     * @throws MultiException if there was an error in the class
     * @throws IllegalArgumentException If the class is null
     */
    public static <T> ActiveDescriptor<T> createAutoDescriptor(Class<T> clazz, ServiceLocatorImpl locator)
            throws MultiException, IllegalArgumentException {
        if (clazz == null) throw new IllegalArgumentException();

        Collector collector = new Collector();

        Creator<T> creator;
        Set<Annotation> qualifiers;
        Set<Type> contracts;
        Class<? extends Annotation> scope;
        String name;
        Boolean proxy = null;

        // Qualifiers naming dance
        qualifiers = ReflectionHelper.getQualifierAnnotations(clazz);
        name = getNameFromAllQualifiers(qualifiers, clazz);
        qualifiers = getAllQualifiers(clazz, name, collector);  // Fixes the @Named qualifier if it has no value

        contracts = getAutoAdvertisedTypes(clazz);
        ScopeInfo scopeInfo = getScopeInfo(clazz, null, collector);
        scope = scopeInfo.getAnnoType();

        creator = new ClazzCreator<T>(locator, clazz, null, collector);

        collector.throwIfErrors();

        Map<String, List<String>> metadata = new HashMap<String, List<String>>();
        if (scopeInfo.getScope() != null) {
            BuilderHelper.getMetadataValues(scopeInfo.getScope(), metadata);
        }

        for (Annotation qualifier : qualifiers) {
            BuilderHelper.getMetadataValues(qualifier, metadata);
        }
        
        UseProxy useProxy = clazz.getAnnotation(UseProxy.class);
        if (useProxy != null) {
            proxy = new Boolean(useProxy.value());
        }
        
        DescriptorVisibility visibility = DescriptorVisibility.NORMAL;
        Visibility vi = clazz.getAnnotation(Visibility.class);
        if (vi != null) {
            visibility = vi.value();
        }

        return new AutoActiveDescriptor<T>(
                clazz,
                creator,
                contracts,
                scope,
                name,
                qualifiers,
                visibility,
                0,
                proxy,
                metadata);
    }

    /**
     * Pre Destroys the given object
     *
     * @param preMe pre destroys the thing
     */
    public static void justPreDestroy(Object preMe) {
        if (preMe == null) throw new IllegalArgumentException();

        Class<?> baseClass = preMe.getClass();

        Collector collector = new Collector();
        Method preDestroy = findPreDestroy(baseClass, collector);

        collector.throwIfErrors();

        if (preDestroy == null) return;

        setAccessible(preDestroy);

        try {
            ReflectionHelper.invoke(preMe, preDestroy, new Object[0]);
        } catch (Throwable e) {
            throw new MultiException(e);
        }
    }

    /**
     * Post constructs the given object
     *
     * @param postMe post constructs the thing
     */
    public static void justPostConstruct(Object postMe) {
        if (postMe == null) throw new IllegalArgumentException();

        Class<?> baseClass = postMe.getClass();

        Collector collector = new Collector();
        Method postConstruct = findPostConstruct(baseClass, collector);

        collector.throwIfErrors();

        if (postConstruct == null) return;

        setAccessible(postConstruct);

        try {
            ReflectionHelper.invoke(postMe, postConstruct, new Object[0]);
        } catch (Throwable e) {
            throw new MultiException(e);
        }
    }

    /**
     * Just creates the thing, doesn't try to do anything else
     * @param injectMe The object to inject into
     * @param locator The locator to find the injection points with
     */
    public static void justInject(Object injectMe, ServiceLocatorImpl locator) {
        if (injectMe == null) throw new IllegalArgumentException();

        Class<?> baseClass = injectMe.getClass();

        Collector collector = new Collector();

        Set<Field> fields = findInitializerFields(baseClass, locator, collector);
        Set<Method> methods = findInitializerMethods(baseClass, locator, collector);

        collector.throwIfErrors();

        for (Field field : fields) {
            InjectionResolver<?> resolver = getInjectionResolver(locator, field);

            List<Injectee> injecteeFields = Utilities.getFieldInjectees(field);

            validateSelfInjectees(null, injecteeFields, collector);
            collector.throwIfErrors();

            Injectee injectee = injecteeFields.get(0);

            Object fieldValue = resolver.resolve(injectee, null);

            setAccessible(field);

            try {
                field.set(injectMe, fieldValue);
            } catch (IllegalAccessException e) {
                throw new MultiException(e);
            }
        }

        for (Method method : methods) {
            List<Injectee> injectees = Utilities.getMethodInjectees(method);

            validateSelfInjectees(null, injectees, collector);
            collector.throwIfErrors();

            Object args[] = new Object[injectees.size()];

            for (Injectee injectee : injectees) {
                InjectionResolver<?> resolver = getInjectionResolver(locator, injectee);
                args[injectee.getPosition()] = resolver.resolve(injectee, null);
            }

            setAccessible(method);

            try {
                ReflectionHelper.invoke(injectMe, method, args);
            } catch (Throwable e) {
                throw new MultiException(e);
            }
        }

    }

    /**
     * Just creates the thing, doesn't try to do anything else
     * @param createMe The thing to create
     * @param locator The locator to find the injection points with
     * @return The constructed thing, no further injection is performed
     */
    @SuppressWarnings("unchecked")
    public static <T> T justCreate(Class<T> createMe, ServiceLocatorImpl locator) {
        if (createMe == null) throw new IllegalArgumentException();

        Collector collector = new Collector();

        Constructor<?> c = findProducerConstructor(createMe, locator, collector);

        collector.throwIfErrors();

        List<Injectee> injectees = getConstructorInjectees(c);

        validateSelfInjectees(null, injectees, collector);
        collector.throwIfErrors();

        Object args[] = new Object[injectees.size()];

        for (Injectee injectee : injectees) {
            InjectionResolver<?> resolver = getInjectionResolver(locator, injectee);
            args[injectee.getPosition()] = resolver.resolve(injectee, null);
        }

        setAccessible(c);
        try {
            return (T) makeMe(c, args);
        } catch (Throwable th) {
            throw new MultiException(th);
        }

    }

    /**
     * Returns all the interfaces the proxy must implement
     * @param contracts All of the advertised contracts
     * @return The array of contracts to add to the proxy
     */
    public static Class<?>[] getInterfacesForProxy(Set<Type> contracts) {
        LinkedList<Class<?>> retVal = new LinkedList<Class<?>>();
        retVal.add(ProxyCtl.class);    // Every proxy implements this interface

        for (Type type : contracts) {
            Class<?> rawClass = ReflectionHelper.getRawClass(type);
            if (rawClass == null) continue;
            if (!rawClass.isInterface()) continue;

            retVal.add(rawClass);
        }

        return retVal.toArray(new Class<?>[retVal.size()]);
    }

    /**
     * Returns true if this scope is proxiable
     *
     * @param scope The scope annotation to test
     * @return true if this must be proxied
     */
    public static boolean isProxiableScope(Class<? extends Annotation> scope) {
        if (scope.isAnnotationPresent(Proxiable.class)) return true;
        return false;
    }
    
    /**
     * Returns true if this scope is unproxiable
     *
     * @param scope The scope annotation to test
     * @return true if this must be proxied
     */
    public static boolean isUnproxiableScope(Class<? extends Annotation> scope) {
        if (scope.isAnnotationPresent(Unproxiable.class)) return true;
        return false;
    }
    
    /**
     * This method determines whether or not the descriptor should be proxied.
     * The given descriptor must be reified and valid.
     * 
     * @param desc A non-null, reified ActiveDescriptor
     * @return true if this descriptor must be proxied, false otherwise
     */
    public static boolean isProxiable(ActiveDescriptor<?> desc) {
        Boolean directed = desc.isProxiable();
        if (directed != null) {
            return directed.booleanValue();
        }
        
        return isProxiableScope(desc.getScopeAnnotation());
    }

    /**
     * Returns the first thing found in the set
     *
     * @param set The set from which to get the first element
     * @return the first thing found in the set
     */
    public static <T> T getFirstThingInList(List<T> set) {
        for (T t : set) {
            return t;
        }

        return null;
    }

    /**
     * Get all fields on this class and all subclasses
     *
     * @param clazz The class to inspect
     * @return A set of all the fields on this class
     */
    private static Set<Field> getAllFields(Class<?> clazz, Collector collector) {
        HashSet<Field> retVal = new HashSet<Field>();

        HashSet<MemberKey> keys = new HashSet<MemberKey>();

        getAllFieldKeys(clazz, keys, collector);

        for (MemberKey key : keys) {
            retVal.add((Field) key.getBackingMember());
        }

        return retVal;
    }

    private static void getAllFieldKeys(Class<?> clazz, Set<MemberKey> currentFields, Collector collector) {
        if (clazz == null) return;

        // Do superclasses first, so that inherited methods are
        // overriden in the set
        getAllFieldKeys(clazz.getSuperclass(), currentFields, collector);

        try {
            for (Field field : getDeclaredFields(clazz)) {
                currentFields.add(new MemberKey(field));
            }
        } catch (Throwable th) {
            collector.addThrowable(new IllegalStateException("Error while getting fields of class " + clazz.getName(), th));
        }

    }

    /**
     * Returns a constant ActiveDescriptor for the basic ServiceLocator
     *
     * @param locator The service locator to get the ActiveDescriptor for
     * @return An active descriptor specifically for the ServiceLocator
     */
    public static ActiveDescriptor<ServiceLocator> getLocatorDescriptor(ServiceLocator locator) {
        HashSet<Type> contracts = new HashSet<Type>();
        contracts.add(ServiceLocator.class);

        Set<Annotation> qualifiers = Collections.emptySet();

        ActiveDescriptor<ServiceLocator> retVal =
                new ConstantActiveDescriptor<ServiceLocator>(
                        locator,
                        contracts,
                        PerLookup.class,
                        null,
                        qualifiers,
                        DescriptorVisibility.NORMAL,
                        0,
                        null,
                        locator.getLocatorId(),
                        null);

        return retVal;
    }

    /**
     * Creates a Three Thirty constant active descriptor
     *
     * @param locator The service locator to get the ActiveDescriptor for
     * @return An active descriptor specifically for the ServiceLocator
     */
    public static ActiveDescriptor<InjectionResolver<Inject>> getThreeThirtyDescriptor(
            ServiceLocatorImpl locator) {
        ThreeThirtyResolver threeThirtyResolver = new ThreeThirtyResolver(locator);

        HashSet<Type> contracts = new HashSet<Type>();

        Type actuals[] = new Type[1];
        actuals[0] = Inject.class;

        contracts.add(new ParameterizedTypeImpl(InjectionResolver.class, actuals));

        Set<Annotation> qualifiers = new HashSet<Annotation>();
        qualifiers.add(new NamedImpl(InjectionResolver.SYSTEM_RESOLVER_NAME));

        ActiveDescriptor<InjectionResolver<Inject>> retVal =
                new ConstantActiveDescriptor<InjectionResolver<Inject>>(
                        threeThirtyResolver,
                        contracts,
                        Singleton.class,
                        InjectionResolver.SYSTEM_RESOLVER_NAME,
                        qualifiers,
                        DescriptorVisibility.NORMAL,
                        0,
                        null,
                        locator.getLocatorId(),
                        null);

        return retVal;
    }

    /**
     * Validates the constructors of the annotated type and returns the
     * producer for the annotatedType (if there is no valid producer
     * constructor then this method returns null)
     *
     * @param annotatedType The type to find the producer constructor
     * @param locator The service locator to use when analyzing constructors
     * @param collector The error collector
     * @return The producer constructor or null if the type has no valid
     * producer constructor
     */
    public static Constructor<?> findProducerConstructor(Class<?> annotatedType, ServiceLocatorImpl locator, Collector collector) {
        Constructor<?> zeroArgConstructor = null;
        Constructor<?> aConstructorWithInjectAnnotation = null;

        Set<Constructor<?>> allConstructors = getAllConstructors(annotatedType);
        for (Constructor<?> constructor : allConstructors) {

            Type rawParameters[] = constructor.getGenericParameterTypes();
            if (rawParameters.length <= 0) {
                zeroArgConstructor = constructor;
            }

            if (hasInjectAnnotation(locator, constructor, true)) {
                if (aConstructorWithInjectAnnotation != null) {
                    collector.addThrowable(new IllegalArgumentException("There is more than one constructor on class " +
                            Pretty.clazz(annotatedType)));
                    return null;
                }

                aConstructorWithInjectAnnotation = constructor;
            }

            if (!isProperConstructor(constructor)) {
                collector.addThrowable(new IllegalArgumentException("The constructor for " +
                        Pretty.clazz(annotatedType) + " may not have an annotation as a parameter"));
                return null;

            }

        }

        if (aConstructorWithInjectAnnotation != null) {
            return aConstructorWithInjectAnnotation;
        }

        if (zeroArgConstructor == null) {
            collector.addThrowable(new IllegalArgumentException("The class " + Pretty.clazz(annotatedType) +
                    " has no constructor marked @Inject and no zero argument constructor"));
            return null;
        }

        return zeroArgConstructor;
    }

    private static boolean isProperConstructor(Constructor<?> c) {
        for (Class<?> pClazz : c.getParameterTypes()) {
            if (pClazz.isAnnotation()) return false;
        }

        return true;
    }

    /**
     * Gets the first type argument if this is a parameterized
     * type, otherwise it returns Object.class
     *
     * @param type The type to find the first type argument on
     * @return If this is a class, Object.class. If this is a parameterized
     * type, the type of the first actual argument
     */
    public static Type getFirstTypeArgument(Type type) {
        if (type instanceof Class) {
            return Object.class;
        }

        if (!(type instanceof ParameterizedType)) return Object.class;

        ParameterizedType pt = (ParameterizedType) type;
        Type arguments[] = pt.getActualTypeArguments();
        if (arguments.length <= 0) return Object.class;

        return arguments[0];
    }

    /**
     * Gets all the constructors for a given class
     *
     * @param clazz The class to find the constructors of
     * @return A set of Constructors for the given class
     */
    private static Set<Constructor<?>> getAllConstructors(Class<?> clazz) {
        HashSet<Constructor<?>> retVal = new HashSet<Constructor<?>>();

        HashSet<MemberKey> keys = new HashSet<MemberKey>();

        getAllConstructorKeys(clazz, keys);

        for (MemberKey key : keys) {
            retVal.add((Constructor<?>) key.getBackingMember());
        }

        return retVal;
    }

    private static void getAllConstructorKeys(Class<?> clazz, Set<MemberKey> currentConstructors) {
        if (clazz == null) return;

        // Constructors for the superclass do not equal constructors for this class

        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            currentConstructors.add(new MemberKey(constructor));
        }

    }

    /**
     * Gets all methods, public, private etc on this class and on all
     * subclasses
     *
     * @param clazz The class to check out
     * @return A set of all methods on this class
     */
    private static Set<Method> getAllMethods(Class<?> clazz) {
        HashSet<Method> retVal = new HashSet<Method>();

        HashSet<MemberKey> keys = new HashSet<MemberKey>();

        getAllMethodKeys(clazz, keys);

        for (MemberKey key : keys) {
            retVal.add((Method) key.getBackingMember());
        }

        return retVal;
    }

    private static void getAllMethodKeys(Class<?> clazz, Set<MemberKey> currentMethods) {
        if (clazz == null) return;

        // Do superclasses first, so that inherited methods are
        // overriden in the set
        getAllMethodKeys(clazz.getSuperclass(), currentMethods);

        for (Method method : getDeclaredMethods(clazz)) {
            currentMethods.add(new MemberKey(method));
        }
    }

    /**
     * Get all the initializer methods of the annotatedType.  If there are definitional
     * errors they will be put into the errorCollector (so as to get all the errors
     * at one shot)
     *
     * @param annotatedType The type to find the errors in
     * @param locator The locator to use when analyzing methods
     * @param errorCollector The collector to add errors to
     * @return A possibly empty but never null set of initializer methods
     */
    public static Set<Method> findInitializerMethods(
            Class<?> annotatedType,
            ServiceLocatorImpl locator,
            Collector errorCollector) {
        HashSet<Method> retVal = new HashSet<Method>();

        for (Method method : getAllMethods(annotatedType)) {
            if (!hasInjectAnnotation(locator, method, true)) {
                // Not an initializer method
                continue;
            }

            if (!isProperMethod(method)) {
                errorCollector.addThrowable(new IllegalArgumentException(
                        "An initializer method " + Pretty.method(method) +
                                " is static, abstract or has a parameter that is an annotation"));
                continue;
            }

            retVal.add(method);
        }

        return retVal;
    }

    /**
     * Will find all the initialize fields in the class
     *
     * @param annotatedType The class to search for fields
     * @param locator The locator to use when analyzing the class
     * @param errorCollector The error collector
     * @return A non-null but possibly empty set of initializer fields
     */
    public static Set<Field> findInitializerFields(Class<?> annotatedType,
                                                   ServiceLocatorImpl locator,
                                                   Collector errorCollector) {
        HashSet<Field> retVal = new HashSet<Field>();

        for (Field field : getAllFields(annotatedType, errorCollector)) {
            if (!hasInjectAnnotation(locator, field, false)) {
                // Not an initializer field
                continue;
            }

            if (!isProperField(field)) {
                errorCollector.addThrowable(new IllegalArgumentException("The field " +
                        Pretty.field(field) + " may not be static, final or have an Annotation type"));
                continue;
            }

            retVal.add(field);
        }

        return retVal;
    }

    /**
     * Checks whether an annotated element has any annotation that was used for the injection
     *
     * @param locator The service locator to use (as it will get all
     * the annotations that were added on as well as the normal Inject)
     * @param annotated  the annotated element
     * @param checkParams  check the params if true
     * @return True if element contains at least one inject annotation
     */
    private static boolean hasInjectAnnotation(ServiceLocatorImpl locator, AnnotatedElement annotated, boolean checkParams) {
        for (Annotation anno : annotated.getAnnotations()) {
            if (locator.isInjectAnnotation(anno)) {
                return true;
            }
        }

        if (!checkParams) return false;

        boolean isConstructor;
        Annotation allAnnotations[][];
        if (annotated instanceof Method) {
            Method m = (Method) annotated;

            isConstructor = false;
            allAnnotations = m.getParameterAnnotations();
        } else if (annotated instanceof Constructor) {
            Constructor<?> c = (Constructor<?>) annotated;

            isConstructor = true;
            allAnnotations = c.getParameterAnnotations();
        } else {
            return false;
        }

        for (Annotation allParamAnnotations[] : allAnnotations) {
            for (Annotation paramAnno : allParamAnnotations) {
                if (locator.isInjectAnnotation(paramAnno, isConstructor)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Gets the annotation that was used for the injection
     *
     * @param locator The service locator to use (as it will get all
     * the annotations that were added on as well as the normal Inject)
     * @param annotated the annotated annotated
     * @param checkParams  check the params if true
     * @param position index of constructor or method parameter which which will be checked
     *                 for inject annotations. The {@code position} parameter is only used when
     *                 {@code annotated} is method or constructor otherwise the value will be ignored.
     * @return The annotation that is the inject annotation, or null
     * if no inject annotation was found
     */
    private static Annotation getInjectAnnotation(ServiceLocatorImpl locator, AnnotatedElement annotated,
                                                  boolean checkParams, int position) {

        if (checkParams) {

            boolean isConstructor = false;
            boolean hasParams = false;
            Annotation allAnnotations[][] = null;
            if (annotated instanceof Method) {
                Method m = (Method) annotated;

                isConstructor = false;
                allAnnotations = m.getParameterAnnotations();
                hasParams = true;
            } else if (annotated instanceof Constructor) {
                Constructor<?> c = (Constructor<?>) annotated;

                isConstructor = true;
                allAnnotations = c.getParameterAnnotations();
                hasParams = true;
            }

            if (hasParams) {
                for (Annotation paramAnno : allAnnotations[position]) {
                    if (locator.isInjectAnnotation(paramAnno, isConstructor)) {
                        return paramAnno;
                    }
                }
            }
        }

        for (Annotation anno : annotated.getAnnotations()) {
            if (locator.isInjectAnnotation(anno)) {
                return anno;
            }
        }

        return null;
    }

    private static boolean isProperMethod(Method member) {
        if (ReflectionHelper.isStatic(member)) return false;
        if (isAbstract(member)) return false;
        for (Class<?> paramClazz : member.getParameterTypes()) {
            if (paramClazz.isAnnotation()) {
                return false;
            }
        }

        return true;
    }

    private static boolean isProperField(Field field) {
        if (ReflectionHelper.isStatic(field)) return false;
        if (isFinal(field)) return false;
        Class<?> type = field.getType();
        return !type.isAnnotation();
    }

    /**
     * Returns true if the underlying member is private
     *
     * @param member The non-null member to test
     * @return true if the member is private
     */
    public static boolean isPrivate(Member member) {
        int modifiers = member.getModifiers();

        return ((modifiers & Modifier.PRIVATE) != 0);
    }

    /**
     * Returns true if the underlying member is abstract
     *
     * @param member The non-null member to test
     * @return true if the member is abstract
     */
    public static boolean isAbstract(Member member) {
        int modifiers = member.getModifiers();

        return ((modifiers & Modifier.ABSTRACT) != 0);
    }

    /**
     * Returns true if the underlying member is abstract
     *
     * @param member The non-null member to test
     * @return true if the member is abstract
     */
    public static boolean isFinal(Member member) {
        int modifiers = member.getModifiers();

        return ((modifiers & Modifier.FINAL) != 0);
    }

    @SuppressWarnings("unchecked")
    private static ScopeInfo getScopeInfo(
            AnnotatedElement annotatedGuy,
            Descriptor defaultScope,
            Collector collector) {
        AnnotatedElement topLevelElement = annotatedGuy;

        Annotation winnerScope = null;
        while (annotatedGuy != null) {
            Annotation current = internalGetScopeAnnotationType(
                    annotatedGuy,
                    collector);
            if (current != null) {
                if (annotatedGuy.equals(topLevelElement)) {
                    // We found a winner, no matter the inherited state
                    winnerScope = current;
                    break;
                } else {
                    if (current.annotationType().isAnnotationPresent(Inherited.class)) {
                        winnerScope = current;
                        break;
                    }

                    // This non-inherited annotation wipes out all scopes above it
                    break;
                }
            }

            if (annotatedGuy instanceof Class) {
                annotatedGuy = ((Class<?>) annotatedGuy).getSuperclass();
            } else {
                Method theMethod = (Method) annotatedGuy;
                Class<?> methodClass = theMethod.getDeclaringClass();

                annotatedGuy = null;
                Class<?> methodSuperclass = methodClass.getSuperclass();
                while (methodSuperclass != null) {
                    if (Factory.class.isAssignableFrom(methodSuperclass)) {
                        annotatedGuy = getFactoryProvideMethod(methodSuperclass);
                        break;
                    }

                    methodSuperclass = methodSuperclass.getSuperclass();
                }
            }
        }

        if (winnerScope != null) {
            return new ScopeInfo(winnerScope, winnerScope.annotationType());
        }


        if (topLevelElement.isAnnotationPresent(Service.class)) {
            return new ScopeInfo(null, Singleton.class);
        }

        if (defaultScope != null && defaultScope.getScope() != null) {
            Class<? extends Annotation> descScope = (Class<? extends Annotation>)
                    loadClass(defaultScope.getScope(), defaultScope, collector);
            if (descScope != null) {
                return new ScopeInfo(null, descScope);
            }
        }

        return new ScopeInfo(null, PerLookup.class);

    }

    /**
     * Returns the scope of this thing
     *
     * @param fromThis The annotated class or producer method
     * @return The scope of this class or producer method.  If no scope is
     * found will return the dependent scope
     */
    public static Class<? extends Annotation> getScopeAnnotationType(
            Class<?> fromThis,
            Descriptor defaultScope) {
        Collector collector = new Collector();

        ScopeInfo si = getScopeInfo(fromThis, defaultScope, collector);

        collector.throwIfErrors();

        return si.getAnnoType();
    }

    /**
     * Returns the scope of this thing
     *
     * @param annotatedGuy The annotated class or producer method
     * @param collector The error collector
     * @return The scope of this class or producer method.  If no scope is
     * found will return the dependent scope
     */
    public static Class<? extends Annotation> getScopeAnnotationType(
            AnnotatedElement annotatedGuy,
            Descriptor defaultScope,
            Collector collector) {
        ScopeInfo si = getScopeInfo(annotatedGuy, defaultScope, collector);
        return si.getAnnoType();
    }

    /**
     * This returns the scope annotation on this class *itself*, and no other
     * classes (like, not subclasses).
     */
    private static Annotation internalGetScopeAnnotationType(
            AnnotatedElement annotatedGuy,
            Collector collector) {
        boolean epicFail = false;
        Annotation retVal = null;
        for (Annotation annotation : annotatedGuy.getDeclaredAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Scope.class)) {
                if (retVal != null) {
                    collector.addThrowable(new IllegalArgumentException("The type " + annotatedGuy +
                            " may not have more than one scope.  It has at least " +
                            Pretty.clazz(retVal.annotationType()) +
                            " and " + Pretty.clazz(annotation.annotationType())));
                    epicFail = true;
                    continue;
                }

                retVal = annotation;
            }
        }

        if (epicFail) return null;

        return retVal;
    }

    /**
     * Returns an injection resolver for the injectee
     *
     * @param locator The locator to use when finding the resolver
     * @param injectee Injectee from which the annotation should be extracted
     * @return Injection resolver used to resolve the injection for the injectee
     * @throws IllegalStateException If we could not find a valid resolver
     */
    public static InjectionResolver<?> getInjectionResolver(
            ServiceLocatorImpl locator, Injectee injectee) throws IllegalStateException {
        return getInjectionResolver(locator, injectee.getParent(), injectee.getPosition());

    }

    private static InjectionResolver<?> getInjectionResolver(
            ServiceLocatorImpl locator, AnnotatedElement annotatedGuy, int position) throws IllegalStateException {
        boolean methodOrConstructor = annotatedGuy instanceof Method || annotatedGuy instanceof Constructor<?>;
        Annotation injectAnnotation = getInjectAnnotation(locator, annotatedGuy, methodOrConstructor, position);

        Class<? extends Annotation> injectType = (injectAnnotation == null) ?
                Inject.class : injectAnnotation.annotationType();

        InjectionResolver<?> retVal = locator.getInjectionResolver(injectType);
        if (retVal == null) {
            // Not possible to get here, we only are here if we already found a resolver
            throw new IllegalStateException("There is no installed injection resolver for " +
                    Pretty.clazz(injectType) + " for type " + annotatedGuy);
        }

        return retVal;
    }

    /**
     * Returns an injection resolver for this AnnotatedElement. The method cannot be used for constructors
     * or methods.
     *
     * @param locator The locator to use when finding the resolver
     * @param annotatedGuy The annotated class or producer method
     * @return The scope of this class or producer method.  If no scope is
     * found will return the dependent scope
     * @throws IllegalStateException If we could not find a valid resolver
     */
    public static InjectionResolver<?> getInjectionResolver(
            ServiceLocatorImpl locator, AnnotatedElement annotatedGuy) throws IllegalStateException {
        if (annotatedGuy instanceof Method || annotatedGuy instanceof Constructor<?>) {
            throw new IllegalArgumentException("Annotated element '" + annotatedGuy + "' cannot be Method neither Constructor.");
        }
        return getInjectionResolver(locator, annotatedGuy, -1);
    }

    private final static String PROVIDE_METHOD = "provide";

    /**
     * This method will retrieve the provide method from a Factory
     *
     * @param clazz This class must implement factory
     * @return The provide method from this class
     */
    public static Method getFactoryProvideMethod(Class<?> clazz) {
        try {
            return clazz.getMethod(PROVIDE_METHOD);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Gets the name from the &46;Named qualifier in this set of qualifiers
     *
     * @param qualifiers The set of qualifiers that may or may not have Named in it
     * @param parent The parent element for which we are searching
     * @return null if no Named was found, or the appropriate name otherwise
     */
    public static String getNameFromAllQualifiers(Set<Annotation> qualifiers, AnnotatedElement parent) {
        for (Annotation qualifier : qualifiers) {
            if (!Named.class.equals(qualifier.annotationType())) continue;

            Named named = (Named) qualifier;
            if ((named.value() == null) || named.value().equals("")) {
                if (parent instanceof Class) {
                    return Pretty.clazz((Class<?>) parent);
                }

                throw new MultiException(new IllegalStateException("@Named must have a value for " + parent));
            }

            return named.value();
        }

        return null;
    }

    /**
     * Returns the default name if one can be found.  Will only work on
     * classes and methods
     *
     * @param parent The parent annotated element
     * @param collector For errors
     * @return null if there is no default name (no Named)
     */
    public static String getDefaultNameFromMethod(Method parent, Collector collector) {
        Named named = parent.getAnnotation(Named.class);
        if (named == null) {
            return null;
        }

        if (named.value() == null || named.value().equals("")) {
            collector.addThrowable(new IllegalArgumentException(
                    "@Named on the provide method of a factory must have an explicit value"));
        }

        return named.value();
    }

    /**
     * Returns the full set of qualifier annotations on this class
     *
     * @param annotatedGuy The element we are searching for qualifiers
     * @param name The name this element must have
     * @param collector The error collector
     * @return A non-null but possibly empty set of qualifiers
     */
    public static Set<Annotation> getAllQualifiers(
            AnnotatedElement annotatedGuy,
            String name,
            Collector collector) {

        Named namedQualifier = null;
        Set<Annotation> retVal = ReflectionHelper.getQualifierAnnotations(annotatedGuy);
        for (Annotation anno : retVal) {
            if (anno instanceof Named) {
                namedQualifier = (Named) anno;
                break;
            }
        }

        if (name == null) {
            if (namedQualifier != null) {
                collector.addThrowable(new IllegalArgumentException("No name was in the descriptor, but this element(" +
                        annotatedGuy + " has a Named annotation with value: " + namedQualifier.value()));

                retVal.remove(namedQualifier);
            }

            return retVal;
        }

        if (namedQualifier == null || namedQualifier.value().equals("")) {
            if (namedQualifier != null) {
                retVal.remove(namedQualifier);
            }

            namedQualifier = new NamedImpl(name);

            retVal.add(namedQualifier);
        }

        if (!name.equals(namedQualifier.value())) {
            collector.addThrowable(new IllegalArgumentException("The class had an @Named qualifier that was inconsistent." +
                    "  The expected name is " + name +
                    " but the annotation has name " + namedQualifier.value()));
        }

        return retVal;
    }


    private static Set<Annotation> getAllQualifiers(
            Annotation memberAnnotations[]) {

        HashSet<Annotation> retVal = new HashSet<Annotation>();
        for (Annotation annotation : memberAnnotations) {
            if (ReflectionHelper.isAnnotationAQualifier(annotation)) {
                retVal.add(annotation);
            }
        }

        return retVal;
    }

    private static boolean isOptional(
            Annotation memberAnnotations[]) {

        for (Annotation annotation : memberAnnotations) {
            if (annotation.annotationType().equals(Optional.class)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSelf(
            Annotation memberAnnotations[]) {

        for (Annotation annotation : memberAnnotations) {
            if (annotation.annotationType().equals(Self.class)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns all the injectees for a constructor
     * @param c The constructor to analyze
     * @return the list (in order) of parameters to the constructor
     */
    public static List<Injectee> getConstructorInjectees(Constructor<?> c) {
        Type genericTypeParams[] = c.getGenericParameterTypes();
        Annotation paramAnnotations[][] = c.getParameterAnnotations();
        Unqualified unqualified = c.getAnnotation(Unqualified.class);

        List<Injectee> retVal = new LinkedList<Injectee>();

        for (int lcv = 0; lcv < genericTypeParams.length; lcv++) {
            retVal.add(new InjecteeImpl(genericTypeParams[lcv],
                    getAllQualifiers(paramAnnotations[lcv]),
                    lcv,
                    c,
                    isOptional(paramAnnotations[lcv]),
                    isSelf(paramAnnotations[lcv]),
                    unqualified));
        }

        return retVal;
    }

    /**
     * Returns all the injectees for a constructor
     * @param c The constructor to analyze
     * @return the list (in order) of parameters to the constructor
     */
    public static List<Injectee> getMethodInjectees(Method c) {
        Type genericTypeParams[] = c.getGenericParameterTypes();
        Annotation paramAnnotations[][] = c.getParameterAnnotations();
        Unqualified unqualified = c.getAnnotation(Unqualified.class);

        List<Injectee> retVal = new LinkedList<Injectee>();

        for (int lcv = 0; lcv < genericTypeParams.length; lcv++) {
            retVal.add(new InjecteeImpl(genericTypeParams[lcv],
                    getAllQualifiers(paramAnnotations[lcv]),
                    lcv,
                    c,
                    isOptional(paramAnnotations[lcv]),
                    isSelf(paramAnnotations[lcv]),
                    unqualified));
        }

        return retVal;
    }

    /**
     * Returns the injectees for a field
     * @param f The field to analyze
     * @return the list (in order) of parameters to the constructor
     */
    public static List<Injectee> getFieldInjectees(Field f) {
        List<Injectee> retVal = new LinkedList<Injectee>();
        Unqualified unqualified = f.getAnnotation(Unqualified.class);

        retVal.add(new InjecteeImpl(f.getGenericType(),
                ReflectionHelper.getQualifierAnnotations(f),
                -1,
                f,
                isOptional(f.getAnnotations()),
                isSelf(f.getAnnotations()),
                unqualified));

        return retVal;
    }

    /**
     * This method validates a list of injectees to ensure that any self injectees have
     * the proper set of requirements.  It adds IllegalArgumentExceptions to the collector
     * if it finds errors
     *
     * @param givenDescriptor The descriptor associated with this injectee, or null if there are none
     * @param injectees The list of injectees to check.  Only self injectees are validates
     * @param collector The collector to add any errors to
     */
    public static void validateSelfInjectees(ActiveDescriptor<?> givenDescriptor,
                                             List<Injectee> injectees,
                                             Collector collector) {

        for (Injectee injectee : injectees) {
            if (!injectee.isSelf()) continue;

            Class<?> requiredRawClass = ReflectionHelper.getRawClass(injectee.getRequiredType());
            if (requiredRawClass == null || !(ActiveDescriptor.class.equals(requiredRawClass))) {
                collector.addThrowable(new IllegalArgumentException("Injection point " + injectee +
                        " does not have the required type of ActiveDescriptor"));
            }

            if (injectee.isOptional()) {
                collector.addThrowable(new IllegalArgumentException("Injection point " + injectee +
                        " is marked both @Optional and @Self"));
            }

            if (!injectee.getRequiredQualifiers().isEmpty()) {
                collector.addThrowable(new IllegalArgumentException("Injection point " + injectee +
                        " is marked @Self but has other qualifiers"));
            }

            if (givenDescriptor == null) {
                collector.addThrowable(new IllegalArgumentException("A class with injection point " + injectee +
                        " is being created or injected via the non-managed ServiceLocator API"));
            }
        }

    }

    private final static String CONVENTION_POST_CONSTRUCT = "postConstruct";
    private final static String CONVENTION_PRE_DESTROY = "preDestroy";

    /**
     * Finds the post construct method on this class
     * @param clazz The class to search for the post construct
     * @param collector An error collector
     * @return The post construct method or null
     */
    public static Method findPostConstruct(Class<?> clazz, Collector collector) {
        if (org.glassfish.hk2.api.PostConstruct.class.isAssignableFrom(clazz)) {
            // A little performance optimization
            return null;
        }

        for (Method method : getAllMethods(clazz)) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                if (method.getParameterTypes().length != 0) {
                    collector.addThrowable(new IllegalArgumentException("The method " + Pretty.method(method) +
                            " annotated with @PostConstruct must not have any arguments"));
                    return null;
                }

                return method;
            }

            if (method.getParameterTypes().length != 0) continue;
            if (!method.getName().equals(CONVENTION_POST_CONSTRUCT)) continue;

            return method;
        }

        return null;
    }

    /**
     * Finds the pre destroy method on this class
     * @param clazz The class to search for the pre destroy method
     * @param collector An error collector
     * @return The pre destroy method or null
     */
    public static Method findPreDestroy(Class<?> clazz, Collector collector) {
        if (org.glassfish.hk2.api.PreDestroy.class.isAssignableFrom(clazz)) {
            // A little performance optimization
            return null;
        }

        for (Method method : getAllMethods(clazz)) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                if (method.getParameterTypes().length != 0) {
                    collector.addThrowable(new IllegalArgumentException("The method " + Pretty.method(method) +
                            " annotated with @PreDestroy must not have any arguments"));
                    return null;
                }

                return method;
            }

            if (method.getParameterTypes().length != 0) continue;
            if (!method.getName().equals(CONVENTION_PRE_DESTROY)) continue;

            return method;
        }

        return null;
    }

    /**
     * This version of invoke is CCL neutral (it will return with the
     * same CCL as what it went in with)
     *
     * @param c the constructor to call
     * @param args The arguments to invoke (may not be null)
     * @return The return from the invocation
     * @throws Throwable The unwrapped throwable thrown by the method
     */
    public static Object makeMe(Constructor<?> c, Object args[])
            throws Throwable {
        ClassLoader currentCCL = Thread.currentThread().getContextClassLoader();

        try {
            return c.newInstance(args);
        } catch (InvocationTargetException ite) {
            Throwable targetException = ite.getTargetException();
            Logger.getLogger().debug(c.getDeclaringClass().getName(), c.getName(), targetException);
            throw targetException;
        } catch (Throwable th) {
            Logger.getLogger().debug(c.getDeclaringClass().getName(), c.getName(), th);
            throw th;
        } finally {
            ReflectionHelper.setContextClassLoader(Thread.currentThread(), currentCCL);
        }
    }

    /**
     * This method returns a set of qualifiers from an array of qualifiers.
     *
     * TODO  It can also do some sanity checking here (i.e., multiple
     * qualifiers of the same type, that sort of thing)
     *
     * @param qualifiers The qualifiers to convert.  May not be null, but
     * may be zero length
     * @param name The name this set of qualifiers must have
     * @return The set containing all the qualifiers
     */
    public static Set<Annotation> fixAndCheckQualifiers(Annotation qualifiers[], String name) {
        Set<Annotation> retVal = new HashSet<Annotation>();

        Set<String> dupChecker = new HashSet<String>();
        Named named = null;
        for (Annotation qualifier : qualifiers) {
            String annotationType = qualifier.annotationType().getName();
            if (dupChecker.contains(annotationType)) {
                throw new IllegalArgumentException(annotationType + " appears more than once in the qualifier list");
            }
            dupChecker.add(annotationType);

            retVal.add(qualifier);
            if (qualifier instanceof Named) {
                named = (Named) qualifier;

                if (named.value().equals("")) {
                    throw new IllegalArgumentException("The @Named qualifier must have a value");
                }

                if (name != null && !name.equals(named.value())) {
                    throw new IllegalArgumentException("The name passed to the method (" +
                            name + ") does not match the value of the @Named qualifier (" + named.value() + ")");
                }
            }
        }

        if (named == null && name != null) {
            retVal.add(new NamedImpl(name));
        }

        return retVal;
    }

    /**
     * Casts this thing to the given type
     * @param o The thing to cast
     * @return A casted version of o
     */
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object o) {
        return (T) o;
    }
    
    private static <T> T secureCreate(final Class<?> superclass,
            final Class<?>[] interfaces,
            final Callback callback) {
        
        /* construct the classloader where the generated proxy will be created --
         * this classloader must have visibility into the cglib classloader as well as
         * the superclass' classloader
         */
        final ClassLoader delegatingLoader = (ClassLoader) AccessController
                .doPrivileged(new PrivilegedAction<Object>() {

                    @Override
                    public Object run() {
                        // create a delegating classloader that attempts to
                        // load from the superclass' classloader first,
                        // then hk2-locator's classloader second.
                        return new DelegatingClassLoader<T>(
                                Enhancer.class.getClassLoader(), superclass.getClassLoader());
                    }
                });

        return AccessController.doPrivileged(new PrivilegedAction<T>() {

            @SuppressWarnings("unchecked")
            @Override
            public T run() {
                EnhancerWithClassLoader<T> e = new EnhancerWithClassLoader<T>(delegatingLoader);
                
                e.setSuperclass(superclass);
                e.setInterfaces(interfaces);
                e.setCallback(callback);
                
                return (T) e.create();
            }
            
        });
        
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T createService(ActiveDescriptor<T> root,
            Injectee injectee,
            ServiceLocatorImpl locator,
            ServiceHandle<T> handle) {
        if (root == null) throw new IllegalArgumentException();
        
        T service = null;
        
        if (!root.isReified()) {
            root = (ActiveDescriptor<T>) locator.reifyDescriptor(root, injectee);
        }
    
        if (Utilities.isProxiable(root)) {
            final Class<?> proxyClass = Utilities.getFactoryAwareImplementationClass(root);
          
            T proxy;
            try {
                proxy = (T) secureCreate(proxyClass,
                    Utilities.getInterfacesForProxy(root.getContractTypes()),
                    new MethodInterceptorImpl(locator, root, handle));
            }
            catch (Throwable th) {
                Exception addMe = new IllegalArgumentException("While attempting to create a Proxy for " + proxyClass.getName() +
                        " in proxiable scope " + root.getScope() + " an error occured while creating the proxy");
                
                if (th instanceof MultiException) {
                    MultiException me = (MultiException) th;
                    
                    me.addError(addMe);
                    
                    throw me;
                }
                
                MultiException me = new MultiException(th);
                me.addError(addMe);
                throw me;
            }
            
            return proxy;
        }
    
        Context<?> context;
        try {
            context = locator.resolveContext(root.getScopeAnnotation());
        }
        catch (Throwable th) {
            throw new MultiException(th);
        }
        
        service = context.findOrCreate(root, handle);
        if (service == null && !context.supportsNullCreation()) {
            throw new MultiException(new IllegalStateException("Context " +
                context + " findOrCreate returned a null for descriptor " + root +
                " and handle " + handle));
        }
    
        return service;
    }

    private static class MemberKey {
        private final Member backingMember;

        private MemberKey(Member method) {
            backingMember = method;
        }

        private Member getBackingMember() {
            return backingMember;
        }

        public int hashCode() {
            int startCode = 0;
            if (backingMember instanceof Method) {
                startCode = 1;
            } else if (backingMember instanceof Constructor) {
                startCode = 2;
            }

            startCode ^= backingMember.getName().hashCode();

            Class<?> parameters[];
            if (backingMember instanceof Method) {
                parameters = ((Method) backingMember).getParameterTypes();
            } else if (backingMember instanceof Constructor) {
                parameters = ((Constructor<?>) backingMember).getParameterTypes();
            } else {
                parameters = new Class<?>[0];
            }

            for (Class<?> param : parameters) {
                startCode ^= param.hashCode();
            }

            return startCode;
        }

        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof MemberKey)) return false;

            MemberKey omk = (MemberKey) o;

            Member oMember = omk.backingMember;

            if (oMember.equals(backingMember)) {
                // If they are the same, they are the same!
                return true;
            }

            if ((backingMember instanceof Field) || (oMember instanceof Field)) {
                // Fields do not inherit
                return false;
            }

            if ((backingMember instanceof Method) && !(oMember instanceof Method)) {
                return false;
            }
            if ((backingMember instanceof Constructor) && !(oMember instanceof Constructor)) {
                return false;
            }

            if (!oMember.getName().equals(backingMember.getName())) {
                return false;
            }

            if (isPrivate(backingMember) || isPrivate(oMember)) {
                // If either are private, they are not the same
                return false;
            }

            Class<?> oParams[];
            Class<?> bParams[];
            if (backingMember instanceof Method) {
                oParams = ((Method) oMember).getParameterTypes();
                bParams = ((Method) backingMember).getParameterTypes();
            } else if (backingMember instanceof Constructor) {
                oParams = ((Constructor<?>) oMember).getParameterTypes();
                bParams = ((Constructor<?>) backingMember).getParameterTypes();
            } else {
                oParams = new Class<?>[0];
                bParams = new Class<?>[0];
            }

            if (oParams.length != bParams.length) return false;
            for (int i = 0; i < oParams.length; i++) {
                if (oParams[i] != bParams[i]) return false;
            }

            return true;
        }
    }

    private static class ScopeInfo {
        private final Annotation scope;
        private final Class<? extends Annotation> annoType;

        private ScopeInfo(Annotation scope, Class<? extends Annotation> annoType) {
            this.scope = scope;
            this.annoType = annoType;
        }

        private Annotation getScope() {
            return scope;
        }

        private Class<? extends Annotation> getAnnoType() {
            return annoType;
        }
    }
}
