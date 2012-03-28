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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.Operation;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ValidationService;
import org.glassfish.hk2.utilities.BuilderHelper;

/**
 * @author jwells
 *
 */
public class ServiceLocatorImpl implements ServiceLocator {
    private final static Object sLock = new Object();
    private static long currentId = 0L;
    
    private final static DescriptorComparator DESCRIPTOR_COMPARATOR = new DescriptorComparator();
    private final static ServiceHandleComparator HANDLE_COMPARATOR = new ServiceHandleComparator();
    
    private final Object lock = new Object();
    private final String locatorName;
    private final long id;
    private final ServiceLocator parent;
    
    private final LinkedList<SystemDescriptor<?>> allDescriptors = new LinkedList<SystemDescriptor<?>>();
    private final HashMap<String, LinkedList<SystemDescriptor<?>>> descriptorsByAdvertisedContract =
            new HashMap<String, LinkedList<SystemDescriptor<?>>>();
    private final HashMap<String, LinkedList<SystemDescriptor<?>>> descriptorsByName =
            new HashMap<String, LinkedList<SystemDescriptor<?>>>();
    private final HashMap<Class<? extends Annotation>, InjectionResolver<?>> allResolvers =
            new HashMap<Class<? extends Annotation>, InjectionResolver<?>>();
    private final Context<Singleton> singletonContext = new SingletonContext();
    private final Context<PerLookup> perLookupContext = new PerLookupContext();
    private final LinkedHashSet<ValidationService> allValidators = new LinkedHashSet<ValidationService>();
    
    /* package */ ServiceLocatorImpl(String name, ServiceLocator parent) {
        locatorName = name;
        this.parent = parent;
        synchronized (sLock) {
            id = currentId++;
        }
    }
    
    /**
     * Must be called under lock
     * 
     * @param descriptor The descriptor to validate
     * @param onBehalfOf The fella who is being validated (or null)
     * @return true if every validator returned true
     */
    private boolean validate(SystemDescriptor<?> descriptor, Injectee onBehalfOf) {
        for (ValidationService vs : allValidators) {
            if (!descriptor.isValidating(vs)) continue;
            
            if (!vs.getValidator().validate(Operation.LOOKUP, descriptor, onBehalfOf)) {
                return false;
            }
        }
        
        return true;
    }
    
    private SortedSet<ActiveDescriptor<?>> getDescriptors(Filter filter, Injectee onBehalfOf) {
        if (filter == null) throw new IllegalArgumentException("filter is null");
        
        synchronized (lock) {
            List<SystemDescriptor<?>> sortMeOut;
            if (filter instanceof IndexedFilter) {
                IndexedFilter df = (IndexedFilter) filter;
                
                if (df.getName() != null) {
                    List<SystemDescriptor<?>> scopedByName;
                    
                    String name = df.getName();
                    
                    scopedByName = descriptorsByName.get(name);
                    if (scopedByName == null) {
                        scopedByName = Collections.emptyList();
                    }
                    
                    if (df.getAdvertisedContract() != null) {
                        sortMeOut = new LinkedList<SystemDescriptor<?>>();
                        
                        for (SystemDescriptor<?> candidate : scopedByName) {
                            if (candidate.getAdvertisedContracts().contains(df.getAdvertisedContract())) {
                                sortMeOut.add(candidate);
                            }
                        }
                    }
                    else {
                        sortMeOut = scopedByName;
                    }
                }
                else if (df.getAdvertisedContract() != null) {
                    String advertisedContract = df.getAdvertisedContract();
                    
                    sortMeOut = descriptorsByAdvertisedContract.get(advertisedContract);
                    if (sortMeOut == null) {
                        sortMeOut = Collections.emptyList();
                        
                    }
                }
                else {
                    sortMeOut = allDescriptors;
                }
            }
            else {
                sortMeOut = allDescriptors;
            }
            
            TreeSet<ActiveDescriptor<?>> sorter = new TreeSet<ActiveDescriptor<?>>(DESCRIPTOR_COMPARATOR);
            
            for (SystemDescriptor<?> candidate : sortMeOut) {
                if (!validate(candidate, onBehalfOf)) continue;
                
                if (filter.matches(candidate)) {
                    sorter.add(candidate);
                }
            }
            
            if (parent != null) {
                sorter.addAll(parent.getDescriptors(filter));
            }
            
            return sorter;
        }
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getDescriptors(org.glassfish.hk2.api.Filter)
     */
    @Override
    public SortedSet<ActiveDescriptor<?>> getDescriptors(Filter filter) {
        return getDescriptors(filter, null);
    }
    
    public ActiveDescriptor<?> getBestDescriptor(Filter filter) {
        if (filter == null) throw new IllegalArgumentException("filter is null");
        
        SortedSet<ActiveDescriptor<?>> sorted = getDescriptors(filter);
        
        return Utilities.getFirstThingInSet(sorted);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#reifyDescriptor(org.glassfish.hk2.api.Descriptor)
     */
    @Override
    public ActiveDescriptor<?> reifyDescriptor(Descriptor descriptor, Injectee injectee)
            throws MultiException {
        Class<?> implClass = loadClass(descriptor, injectee);
        
        if (!(descriptor instanceof ActiveDescriptor)) {
            SystemDescriptor<?> sd = new SystemDescriptor<Object>(descriptor, new Long(id));
            
            Collector collector = new Collector();
            sd.reify(implClass, this, collector);
            
            collector.throwIfErrors();
            
            return sd;
        }
        
        // Descriptor is an active descriptor
        ActiveDescriptor<?> active = (ActiveDescriptor<?>) descriptor;
        if (active.isReified()) return active;
        
        SystemDescriptor<?> sd;
        if (active instanceof SystemDescriptor) {
            sd = (SystemDescriptor<?>) active;
        }
        else {
            sd = new SystemDescriptor<Object>(descriptor, new Long(id));
        }
        
        Collector collector = new Collector();
        sd.reify(implClass, this, collector);
        
        collector.throwIfErrors();
        
        return sd;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#reifyDescriptor(org.glassfish.hk2.api.Descriptor)
     */
    @Override
    public ActiveDescriptor<?> reifyDescriptor(Descriptor descriptor)
            throws MultiException {
        return reifyDescriptor(descriptor, null);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getInjecteeDescriptor(org.glassfish.hk2.api.Injectee)
     */
    @Override
    public ActiveDescriptor<?> getInjecteeDescriptor(Injectee injectee)
            throws MultiException {
        Type requiredType = injectee.getRequiredType();
        Class<?> rawType = Utilities.getRawClass(requiredType);
        if (rawType == null) return null;
        
        if (Provider.class.equals(rawType) || IterableProvider.class.equals(rawType) ) {
            IterableProviderImpl<?> value = new IterableProviderImpl<Object>(this,
                    Utilities.getFirstTypeArgument(requiredType),
                    injectee.getRequiredQualifiers());
            
            return new ConstantActiveDescriptor<Object>(value, id);
        }
        
        Set<Annotation> qualifiersAsSet = injectee.getRequiredQualifiers();
        String name = Utilities.getNameFromAllQualifiers(qualifiersAsSet, injectee.getParent());
        
        Annotation qualifiers[] = qualifiersAsSet.toArray(new Annotation[qualifiersAsSet.size()]);
        
        ServiceHandle<?> handle = internalGetServiceHandle(injectee, requiredType, name, qualifiers);
        if (handle == null) return null;
        
        return handle.getActiveDescriptor();
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getServiceHandle(org.glassfish.hk2.api.ActiveDescriptor)
     */
    @Override
    public <T> ServiceHandle<T> getServiceHandle(
            ActiveDescriptor<T> activeDescriptor,
            Injectee injectee) throws MultiException {
        return new ServiceHandleImpl<T>(this, activeDescriptor, injectee);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getServiceHandle(org.glassfish.hk2.api.ActiveDescriptor)
     */
    @Override
    public <T> ServiceHandle<T> getServiceHandle(
            ActiveDescriptor<T> activeDescriptor) throws MultiException {
        return getServiceHandle(activeDescriptor, null);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getService(org.glassfish.hk2.api.ActiveDescriptor, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public <T> T getService(ActiveDescriptor<T> activeDescriptor,
            ServiceHandle<?> root) throws MultiException {
        ServiceHandle<T> subHandle = getServiceHandle(activeDescriptor);
        
        if (root != null && PerLookup.class.equals(activeDescriptor.getScopeAnnotation())) {
            ((ServiceHandleImpl<?>) root).addSubHandle((ServiceHandleImpl<T>) subHandle);
        }
        
        return subHandle.getService();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getService(java.lang.reflect.Type)
     */
    @Override
    public <T> T getService(Type contractOrImpl, Annotation... qualifiers) throws MultiException {
        ServiceHandle<T> serviceHandle = getServiceHandle(contractOrImpl, qualifiers);
        if (serviceHandle == null) return null;
        
        return serviceHandle.getService();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServices(java.lang.reflect.Type)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getAllServices(Type contractOrImpl, Annotation... qualifiers)
            throws MultiException {
        SortedSet<ServiceHandle<?>> services = getAllServiceHandles(contractOrImpl, qualifiers);
        
        List<T> retVal = new LinkedList<T>();
        for (ServiceHandle<?> service : services) {
            retVal.add((T) service.getService());
        }
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getService(java.lang.reflect.Type, java.lang.String)
     */
    @Override
    public <T> T getService(Type contractOrImpl, String name, Annotation... qualifiers)
            throws MultiException {
        ServiceHandle<T> handle = getServiceHandle(contractOrImpl, name, qualifiers);
        if (handle == null) return null;
        return handle.getService();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServices(org.glassfish.hk2.api.Filter)
     */
    @Override
    public List<?> getAllServices(Filter searchCriteria)
            throws MultiException {
        SortedSet<ServiceHandle<?>> handleSet = getAllServiceHandles(searchCriteria);
        
        List<Object> retVal = new LinkedList<Object>();
        for (ServiceHandle<?> handle : handleSet) {
            retVal.add(handle.getService());
        }
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getName()
     */
    @Override
    public String getName() {
        return locatorName;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#shutdown()
     */
    @Override
    public void shutdown() {
        throw new AssertionError("not implemented yet");

    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#create(java.lang.Class)
     */
    @Override
    public <T> T create(Class<T> createMe) {
        return Utilities.justCreate(createMe, this);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#inject(java.lang.Object)
     */
    @Override
    public void inject(Object injectMe) {
        Utilities.justInject(injectMe, this);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#postConstruct(java.lang.Object)
     */
    @Override
    public void postConstruct(Object postConstructMe) {
        Utilities.justPostConstruct(postConstructMe);

    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#preDestroy(java.lang.Object)
     */
    @Override
    public void preDestroy(Object preDestroyMe) {
        Utilities.justPreDestroy(preDestroyMe);

    }
    
    @SuppressWarnings("unchecked")
    private <T> ServiceHandle<T> internalGetServiceHandle(Injectee onBehalfOf, Type contractOrImpl,
            String name,
            Annotation... qualifiers) throws MultiException {
        Class<?> rawClass = Utilities.getRawClass(contractOrImpl);
        if (rawClass == null) return null;  // Can't be a TypeVariable or Wildcard
        
        Filter filter = BuilderHelper.createNameAndContractFilter(rawClass.getName(), name);
        SortedSet<ActiveDescriptor<?>> candidates = getDescriptors(filter, onBehalfOf);
        candidates = narrow(candidates, contractOrImpl, null, false, onBehalfOf, qualifiers);
        
        ActiveDescriptor<?> topDog = Utilities.getFirstThingInSet(candidates);
        if (topDog == null) return null;
        
        return getServiceHandle((ActiveDescriptor<T>) topDog);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getServiceHandle(java.lang.reflect.Type, java.lang.annotation.Annotation[])
     */
    @Override
    public <T> ServiceHandle<T> getServiceHandle(Type contractOrImpl,
            Annotation... qualifiers) throws MultiException {
        return internalGetServiceHandle(null, contractOrImpl, null, qualifiers);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServiceHandles(java.lang.reflect.Type, java.lang.annotation.Annotation[])
     */
    @Override
    public SortedSet<ServiceHandle<?>> getAllServiceHandles(
            Type contractOrImpl, Annotation... qualifiers)
            throws MultiException {
        Class<?> rawClass = Utilities.getRawClass(contractOrImpl);
        if (rawClass == null) return null;  // Can't be a TypeVariable or Wildcard
        
        Filter filter = BuilderHelper.createContractFilter(rawClass.getName());
        SortedSet<ActiveDescriptor<?>> candidates = getDescriptors(filter);
        candidates = narrow(candidates, contractOrImpl, null, true, null, qualifiers);
        
        SortedSet<ServiceHandle<?>> retVal = new TreeSet<ServiceHandle<?>>(HANDLE_COMPARATOR);
        for (ActiveDescriptor<?> candidate : candidates) {
            retVal.add(getServiceHandle(candidate));
        }
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getServiceHandle(java.lang.reflect.Type, java.lang.String, java.lang.annotation.Annotation[])
     */
    @Override
    public <T> ServiceHandle<T> getServiceHandle(Type contractOrImpl,
            String name, Annotation... qualifiers) throws MultiException {
        return internalGetServiceHandle(null, contractOrImpl, name, qualifiers);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServiceHandles(org.glassfish.hk2.api.Filter)
     */
    @Override
    public SortedSet<ServiceHandle<?>> getAllServiceHandles(
            Filter searchCriteria) throws MultiException {
        SortedSet<ActiveDescriptor<?>> candidates = getDescriptors(searchCriteria);
        candidates = narrow(candidates, null, null, true, null);
        
        SortedSet<ServiceHandle<?>> retVal = new TreeSet<ServiceHandle<?>>(HANDLE_COMPARATOR);
        for (ActiveDescriptor<?> candidate : candidates) {
            retVal.add(getServiceHandle(candidate));
        }
        
        return retVal;
    }
    
    private void checkConfiguration(DynamicConfigurationImpl dci) {
        for (SystemDescriptor<?> sd : dci.getAllDescriptors()) {
            if (sd.getAdvertisedContracts().contains(ValidationService.class.getName()) ||
                sd.getAdvertisedContracts().contains(InjectionResolver.class.getName())) {
                // These guys get reified right away because they are needed by the system
                reifyDescriptor(sd);
            }
            
            if (sd.getAdvertisedContracts().contains(InjectionResolver.class.getName())) {
                if (Utilities.getInjectionResolverType(sd) == null) {
                    throw new MultiException(new IllegalArgumentException(
                            "An implementation of InjectionResolver must be a parameterized type and the actual type" +
                            " must be an annotation"));
                }
            }
            
            for (ValidationService vs : allValidators) {
                if (!vs.getValidator().validate(Operation.BIND, sd, null)) {
                    throw new MultiException(new IllegalArgumentException("Descriptor " + sd + " did not pass the BIND validation"));
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void addConfigurationInternal(DynamicConfigurationImpl dci) {
        for (SystemDescriptor<?> sd : dci.getAllDescriptors()) {
            allDescriptors.add(sd);
            
            for (String advertisedContract : sd.getAdvertisedContracts()) {
                LinkedList<SystemDescriptor<?>> byImpl = descriptorsByAdvertisedContract.get(advertisedContract);
                if (byImpl == null) {
                    byImpl = new LinkedList<SystemDescriptor<?>>();
                    descriptorsByAdvertisedContract.put(advertisedContract, byImpl);
                }
                
                byImpl.add(sd);
            }
            
            if (sd.getName() != null) {
                String name = sd.getName();
                LinkedList<SystemDescriptor<?>> byName = descriptorsByName.get(name);
                if (byName == null) {
                    byName = new LinkedList<SystemDescriptor<?>>();
                    
                    descriptorsByName.put(name, byName);
                }
                
                byName.add(sd);
            }
            
            if (sd.getAdvertisedContracts().contains(ValidationService.class.getName())) {
                ServiceHandle<ValidationService> handle = getServiceHandle((ActiveDescriptor<ValidationService>) sd);
                ValidationService vs = handle.getService();
                allValidators.add(vs);
            }
        }
    }
    
    private void reupInjectionResolvers() {
        HashMap<Class<? extends Annotation>, InjectionResolver<?>> newResolvers =
                new HashMap<Class<? extends Annotation>, InjectionResolver<?>>();
        
        Filter injectionResolverFilter = BuilderHelper.createContractFilter(
                InjectionResolver.class.getName());
        
        SortedSet<ActiveDescriptor<?>> resolverDescriptors = getDescriptors(injectionResolverFilter);
        
        for (ActiveDescriptor<?> resolverDescriptor : resolverDescriptors) {
            Class<? extends Annotation> iResolve = Utilities.getInjectionResolverType(resolverDescriptor);
            
            if (iResolve != null && !newResolvers.containsKey(iResolve)) {
                InjectionResolver<?> resolver = (InjectionResolver<?>)
                        getServiceHandle(resolverDescriptor).getService();
                
                newResolvers.put(iResolve, resolver);
            }
        }
        
        allResolvers.clear();
        allResolvers.putAll(newResolvers);
    }
    
    /* package */ void addConfiguration(DynamicConfigurationImpl dci) {
        synchronized (lock) {
            checkConfiguration(dci);  // Does as much preliminary checking as possible
            
            addConfigurationInternal(dci);
            
            reupInjectionResolvers();
        }
    }
    
    /* package */ boolean isInjectAnnotation(Annotation annotation) {
        synchronized (lock) {
            return allResolvers.containsKey(annotation.annotationType());
        }
    }
    
    /* package */ InjectionResolver<?> getInjectionResolver(Class<? extends Annotation> annoType) {
        synchronized (lock) {
            return allResolvers.get(annoType);
        }
    }
    
    /* package */ Context<?> resolveContext(Class<? extends Annotation> scope) throws IllegalStateException {
        if (scope.equals(Singleton.class)) return singletonContext;
        if (scope.equals(PerLookup.class)) return perLookupContext;
        
        Type actuals[] = new Type[1];
        actuals[0] = scope;
        ParameterizedType findContext = new ParameterizedTypeImpl(Context.class, actuals);
        
        SortedSet<ServiceHandle<Context<?>>> contextHandles = Utilities.<SortedSet<ServiceHandle<Context<?>>>>cast(
                getAllServiceHandles(findContext));
        
        try {
            Context<?> retVal = null;
            for (ServiceHandle<Context<?>> contextHandle : contextHandles) {
                Context<?> context = contextHandle.getService();
                
                if (!context.isActive()) continue;
                
                if (retVal != null) {
                    throw new IllegalStateException("There is more than one active context for " + scope.getName());
                }
                
                retVal = context;
            }
            
            return retVal;
        }
        finally {
            for (ServiceHandle<Context<?>> contextHandle : contextHandles) {
                contextHandle.destroy();
            }
        }
    }
    
    private Class<?> loadClass(Descriptor descriptor, Injectee injectee) {
        HK2Loader loader = descriptor.getLoader();
        if (loader == null) {
            return Utilities.loadClass(descriptor.getImplementation(), injectee);
        }
        
        return loader.loadClass(descriptor.getImplementation());
    }

    private SortedSet<ActiveDescriptor<?>> narrow(SortedSet<ActiveDescriptor<?>> candidates,
            Type requiredType, String name, boolean getAll, Injectee injectee, Annotation... qualifiers) {
        SortedSet<ActiveDescriptor<?>> retVal = new TreeSet<ActiveDescriptor<?>>(DESCRIPTOR_COMPARATOR);
        
        Set<Annotation> requiredAnnotations = Utilities.fixAndCheckQualifiers(qualifiers, name);
        
        for (ActiveDescriptor<?> candidate : candidates) {
            // We will not reify them all, we will only reify until we match
            if (!candidate.isReified()) {
                candidate = reifyDescriptor(candidate, injectee);
            }
            
            // Now match it
            if (requiredType != null) {
                boolean safe = false;
                for (Type candidateType : candidate.getContractTypes()) {
                    if (TypeChecker.isTypeSafe(requiredType, candidateType)) {
                        safe = true;
                        break;
                    }
                }
            
                if (!safe) {
                    // Sorry, not type safe
                    continue;
                }
            }
            
            // Now match the qualifiers
            Set<Annotation> candidateAnnotations = candidate.getQualifierAnnotations();
            
            if (!candidateAnnotations.containsAll(requiredAnnotations)) {
                // The qualifiers do not match
                continue;
            }
            
            // If we are here, then this one matches
            retVal.add(candidate);
            
            if (!getAll) {
                // We found one, we don't want to reify any more than we have to
                break;
            }
        }
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getLocatorId()
     */
    @Override
    public long getLocatorId() {
        return id;
    }
    
    public String toString() {
        return "ServiceLocatorImpl(" + locatorName + "," + id + "," + System.identityHashCode(this) + ")";
    }

}
