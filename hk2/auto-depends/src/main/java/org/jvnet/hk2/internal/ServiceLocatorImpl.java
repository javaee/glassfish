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
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorFilter;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;

/**
 * @author jwells
 *
 */
public class ServiceLocatorImpl implements ServiceLocator {
    private final static HK2Loader SYSTEM_LOADER = new SystemLoader();
    private final static DescriptorComparator COMPARATOR = new DescriptorComparator();
    private final static ServiceHandleComparator HANDLE_COMPARATOR = new ServiceHandleComparator();
    
    private final Object lock = new Object();
    private final String name;
    
    
    private final LinkedList<ActiveDescriptor<?>> allDescriptors = new LinkedList<ActiveDescriptor<?>>();
    private final HashMap<String, LinkedList<ActiveDescriptor<?>>> descriptorsByAdvertisedContract =
            new HashMap<String, LinkedList<ActiveDescriptor<?>>>();
    private final HashMap<String, HK2Loader> allLoaders = new HashMap<String, HK2Loader>();
    private final HashMap<Class<? extends Annotation>, InjectionResolver> allResolvers =
            new HashMap<Class<? extends Annotation>, InjectionResolver>();
    private final HashMap<Class<? extends Annotation>, LinkedList<Context>> allContexts =
            new HashMap<Class<? extends Annotation>, LinkedList<Context>>();
    
    /* package */ ServiceLocatorImpl(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getDescriptors(org.glassfish.hk2.api.Filter)
     */
    @Override
    public SortedSet<ActiveDescriptor<?>> getDescriptors(Filter<Descriptor> filter) {
        if (filter == null) throw new IllegalArgumentException("filter is null");
        
        synchronized (lock) {
            List<ActiveDescriptor<?>> sortMeOut;
            if (filter instanceof DescriptorFilter) {
                DescriptorFilter df = (DescriptorFilter) filter;
                
                String implementationName = df.getImplementation();
                if (implementationName != null) {
                    sortMeOut = descriptorsByAdvertisedContract.get(implementationName);
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
            
            TreeSet<ActiveDescriptor<?>> sorter = new TreeSet<ActiveDescriptor<?>>(COMPARATOR);
            
            for (ActiveDescriptor<?> candidate : sortMeOut) {
                if (filter.matches(candidate)) {
                    sorter.add(candidate);
                }
            }
            
            return sorter;
        }
    }
    
    public ActiveDescriptor<?> getBestDescriptor(Filter<Descriptor> filter) {
        if (filter == null) throw new IllegalArgumentException("filter is null");
        
        SortedSet<ActiveDescriptor<?>> sorted = getDescriptors(filter);
        
        return Utilities.getFirstThingInSet(sorted);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#reifyDescriptor(org.glassfish.hk2.api.Descriptor)
     */
    @Override
    public ActiveDescriptor<?> reifyDescriptor(Descriptor descriptor)
            throws MultiException {
        Class<?> implClass = loadClass(descriptor.getImplementation());
        
        if (!(descriptor instanceof ActiveDescriptor)) {
            SystemDescriptor<?> sd = new SystemDescriptor<Object>(descriptor);
            
            MultiException collector = new MultiException();
            sd.reify(implClass, this, collector);
            
            if (collector.hasErrors()) throw collector;
            
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
            sd = new SystemDescriptor<Object>(descriptor);
        }
        
        MultiException collector = new MultiException();
        sd.reify(implClass, this, collector);
        
        if (collector.hasErrors()) throw collector;
        
        return sd;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getInjecteeDescriptor(org.glassfish.hk2.api.Injectee)
     */
    @Override
    public ActiveDescriptor<?> getInjecteeDescriptor(Injectee injectee)
            throws MultiException {
        Type requiredType = injectee.getRequiredType();
        Set<Annotation> qualifiersAsSet = injectee.getRequiredQualifiers();
        
        Annotation qualifiers[] = qualifiersAsSet.toArray(new Annotation[qualifiersAsSet.size()]);
        
        ServiceHandle<?> handle = getServiceHandle(requiredType, qualifiers);
        if (handle == null) return null;
        
        return handle.getActiveDescriptor();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getServiceHandle(org.glassfish.hk2.api.ActiveDescriptor)
     */
    @Override
    public <T> ServiceHandle<T> getServiceHandle(
            ActiveDescriptor<T> activeDescriptor) throws MultiException {
        throw new AssertionError("not implemented yet");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getService(org.glassfish.hk2.api.ActiveDescriptor, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public <T> T getService(ActiveDescriptor<T> activeDescriptor,
            ServiceHandle<?> root) throws MultiException {
        Context context = resolveContext(activeDescriptor.getScopeAnnotation());
        if (context == null) return null;
        
        T service = context.findOrCreate(activeDescriptor, root);
        
        // TODO:  This is where we would proxy
        
        return service;
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
    @Override
    public <T> SortedSet<T> getAllServices(Type contractOrImpl, Annotation... qualifiers)
            throws MultiException {
        throw new AssertionError("not implemented yet");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getService(java.lang.reflect.Type, java.lang.String)
     */
    @Override
    public <T> T getService(Type contractOrImpl, String name, Annotation... qualifiers)
            throws MultiException {
        throw new AssertionError("not implemented yet");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServices(org.glassfish.hk2.api.Filter)
     */
    @Override
    public <T> SortedSet<T> getAllServices(Filter<Descriptor> searchCriteria)
            throws MultiException {
        throw new AssertionError("not implemented yet");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getName()
     */
    @Override
    public String getName() {
        return name;
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
    public Object create(Class<?> createMe) {
        throw new AssertionError("not implemented yet");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#inject(java.lang.Object)
     */
    @Override
    public void inject(Object injectMe) {
        throw new AssertionError("not implemented yet");

    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#postConstruct(java.lang.Object)
     */
    @Override
    public void postConstruct(Object postConstructMe) {
        throw new AssertionError("not implemented yet");

    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#preDestroy(java.lang.Object)
     */
    @Override
    public void preDestroy(Object preDestroyMe) {
        throw new AssertionError("not implemented yet");

    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getServiceHandle(java.lang.reflect.Type, java.lang.annotation.Annotation[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> ServiceHandle<T> getServiceHandle(Type contractOrImpl,
            Annotation... qualifiers) throws MultiException {
        Class<?> rawClass = Utilities.getRawClass(contractOrImpl);
        if (rawClass == null) return null;  // Can't be a TypeVariable or Wildcard
        
        DescriptorFilter filter = BuilderHelper.link(rawClass).build();
        SortedSet<ActiveDescriptor<?>> candidates = getDescriptors(filter);
        candidates = narrow(candidates, contractOrImpl, false, qualifiers);
        
        ActiveDescriptor<?> topDog = Utilities.getFirstThingInSet(candidates);
        if (topDog == null) return null;
        
        return new ServiceHandleImpl<T>(this, (ActiveDescriptor<T>) topDog);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServiceHandles(java.lang.reflect.Type, java.lang.annotation.Annotation[])
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public SortedSet<ServiceHandle<?>> getAllServiceHandles(
            Type contractOrImpl, Annotation... qualifiers)
            throws MultiException {
        Class<?> rawClass = Utilities.getRawClass(contractOrImpl);
        if (rawClass == null) return null;  // Can't be a TypeVariable or Wildcard
        
        DescriptorFilter filter = BuilderHelper.link(rawClass).build();
        SortedSet<ActiveDescriptor<?>> candidates = getDescriptors(filter);
        candidates = narrow(candidates, contractOrImpl, false, qualifiers);
        
        SortedSet<ServiceHandle<?>> retVal = new TreeSet<ServiceHandle<?>>(HANDLE_COMPARATOR);
        for (ActiveDescriptor<?> candidate : candidates) {
            retVal.add(new ServiceHandleImpl(this, candidate));
        }
        
        return retVal;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getServiceHandle(org.glassfish.hk2.api.Filter)
     */
    @Override
    public <T> ServiceHandle<T> getServiceHandle(Filter<Descriptor> filter)
            throws MultiException {
        throw new AssertionError("not implemented");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getServiceHandle(java.lang.reflect.Type, java.lang.String, java.lang.annotation.Annotation[])
     */
    @Override
    public <T> ServiceHandle<T> getServiceHandle(Type contractOrImpl,
            String name, Annotation... qualifiers) throws MultiException {
        throw new AssertionError("not implemented yet");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServiceHandles(org.glassfish.hk2.api.Filter)
     */
    @Override
    public <T> SortedSet<ServiceHandle<T>> getAllServiceHandles(
            Filter<Descriptor> searchCriteria) throws MultiException {
        throw new AssertionError("not implemented yet");
    }
    
    private void addConfigurationInternal(DynamicConfigurationImpl dci) {
        for (SystemDescriptor<?> sd : dci.getAllDescriptors()) {
            allDescriptors.add(sd);
            
            for (String advertisedContract : sd.getAdvertisedContracts()) {
                LinkedList<ActiveDescriptor<?>> byImpl = descriptorsByAdvertisedContract.get(advertisedContract);
                if (byImpl == null) {
                    byImpl = new LinkedList<ActiveDescriptor<?>>();
                    descriptorsByAdvertisedContract.put(advertisedContract, byImpl);
                }
                
                byImpl.add(sd);
                
            }
        }
        
        for (Map.Entry<String, HK2Loader> entry : dci.getAllLoaders().entrySet()) {
            allLoaders.put(entry.getKey(), entry.getValue());
        }
        
        for (Map.Entry<Class<? extends Annotation>, InjectionResolver> entry : dci.getAllResolvers().entrySet()) {
            allResolvers.put(entry.getKey(), entry.getValue());
        }
        
        for (Context context : dci.getAllContexts()) {
            Class<? extends Annotation> scope = context.getScope();
            if (scope == null) continue;
            
            LinkedList<Context> contexts = allContexts.get(scope);
            if (contexts == null) {
                contexts = new LinkedList<Context>();
                allContexts.put(scope, contexts);
            }
            
            contexts.add(context);
        }
        
    }
    
    /* package */ void addConfiguration(DynamicConfigurationImpl dci) {
        synchronized (lock) {
            addConfigurationInternal(dci);
        }
        
        
    }
    
    /* package */ boolean isInjectAnnotation(Annotation annotation) {
        synchronized (lock) {
            return allResolvers.containsKey(annotation.annotationType());
        }
    }
    
    /* package */ InjectionResolver getInjectionResolver(Class<? extends Annotation> annoType) {
        synchronized (lock) {
            return allResolvers.get(annoType);
        }
    }
    
    /* package */ Context resolveContext(Class<? extends Annotation> scope) throws IllegalStateException {
        synchronized (lock) {
            LinkedList<Context> matches = allContexts.get(scope);
            
            Context retVal = null;
            for (Context match : matches) {
                if (match.isActive()) {
                    if (retVal != null) {
                        throw new IllegalStateException("There are multiple active contexts for scope " +
                           Pretty.clazz(scope));
                    }
                    
                    retVal = match;
                }
            }
            
            if (retVal == null) {
                throw new IllegalStateException("There is no active context for scope " +
                        Pretty.clazz(scope));
            }
            
            return retVal;
        }
    }
    
    private Class<?> loadClass(String className) {
        for (HK2Loader loader : allLoaders.values()) {
            Class<?> found = loader.loadClass(className);
            if (found != null) return found;
        }
        
        return SYSTEM_LOADER.loadClass(className);
    }

    private SortedSet<ActiveDescriptor<?>> narrow(SortedSet<ActiveDescriptor<?>> candidates,
            Type requiredType, boolean getAll, Annotation... qualifiers) {
        SortedSet<ActiveDescriptor<?>> retVal = new TreeSet<ActiveDescriptor<?>>(COMPARATOR);
        
        for (ActiveDescriptor<?> candidate : candidates) {
            // We will not reify them all, we will only reify until we match
            if (!candidate.isReified()) {
                candidate = reifyDescriptor(candidate);
            }
            
            // Now match it
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
            
            // Now match the qualifiers
            Set<Annotation> candidateAnnotations = candidate.getQualifierAnnotations();
            Set<Annotation> requiredAnnotations = Utilities.getQualifiers(qualifiers);
            
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

}
