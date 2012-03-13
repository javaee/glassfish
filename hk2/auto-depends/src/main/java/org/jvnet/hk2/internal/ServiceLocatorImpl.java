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

/**
 * @author jwells
 *
 */
public class ServiceLocatorImpl implements ServiceLocator {
    private final Object lock = new Object();
    private final String name;
    private final DescriptorComparator comparator = new DescriptorComparator();
    
    private final LinkedList<Descriptor> allDescriptors = new LinkedList<Descriptor>();
    private final HashMap<String, LinkedList<Descriptor>> descriptorsByImplementation =
            new HashMap<String, LinkedList<Descriptor>>();
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
    public SortedSet<Descriptor> getDescriptors(Filter<Descriptor> filter) {
        if (filter == null) throw new IllegalArgumentException("filter is null");
        
        synchronized (lock) {
            List<Descriptor> sortMeOut;
            if (filter instanceof DescriptorFilter) {
                DescriptorFilter df = (DescriptorFilter) filter;
                
                String implementationName = df.getImplementation();
                if (implementationName != null) {
                    sortMeOut = descriptorsByImplementation.get(implementationName);
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
            
            TreeSet<Descriptor> sorter = new TreeSet<Descriptor>(comparator);
            
            for (Descriptor candidate : sortMeOut) {
                if (filter.matches(candidate)) {
                    sorter.add(candidate);
                }
            }
            
            return sorter;
        }
    }
    
    public Descriptor getBestDescriptor(Filter<Descriptor> filter) {
        if (filter == null) throw new IllegalArgumentException("filter is null");
        
        SortedSet<Descriptor> sorted = getDescriptors(filter);
        
        for (Descriptor returnMe : sorted) {
            return returnMe;
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#reifyDescriptor(org.glassfish.hk2.api.Descriptor)
     */
    @Override
    public ActiveDescriptor<?> reifyDescriptor(Descriptor descriptor)
            throws MultiException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getInjecteeDescriptor(org.glassfish.hk2.api.Injectee)
     */
    @Override
    public ActiveDescriptor<?> getInjecteeDescriptor(Injectee injectee)
            throws MultiException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getServiceHandle(org.glassfish.hk2.api.ActiveDescriptor)
     */
    @Override
    public <T> ServiceHandle<T> getServiceHandle(
            ActiveDescriptor<T> activeDescriptor) throws MultiException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getService(org.glassfish.hk2.api.ActiveDescriptor, org.glassfish.hk2.api.ServiceHandle)
     */
    @Override
    public <T> T getService(ActiveDescriptor<T> activeDescriptor,
            ServiceHandle<?> root) throws MultiException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getService(java.lang.reflect.Type)
     */
    @Override
    public <T> T getService(Type contractOrImpl) throws MultiException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServices(java.lang.reflect.Type)
     */
    @Override
    public <T> SortedSet<T> getAllServices(Type contractOrImpl)
            throws MultiException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getService(java.lang.reflect.Type, java.lang.String)
     */
    @Override
    public <T> T getService(Type contractOrImpl, String name)
            throws MultiException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#getAllServices(org.glassfish.hk2.api.Filter)
     */
    @Override
    public <T> SortedSet<T> getAllServices(Filter<Descriptor> searchCriteria)
            throws MultiException {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#create(java.lang.Class)
     */
    @Override
    public Object create(Class<?> createMe) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#inject(java.lang.Object)
     */
    @Override
    public void inject(Object injectMe) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#postConstruct(java.lang.Object)
     */
    @Override
    public void postConstruct(Object postConstructMe) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ServiceLocator#preDestroy(java.lang.Object)
     */
    @Override
    public void preDestroy(Object preDestroyMe) {
        // TODO Auto-generated method stub

    }
    
    private void addConfigurationInternal(DynamicConfigurationImpl dci) {
        for (SystemDescriptor sd : dci.getAllDescriptors()) {
            allDescriptors.add(sd);
            
            String implementationName = sd.getImplementation();
            if (implementationName != null) {
                LinkedList<Descriptor> byImpl = descriptorsByImplementation.get(implementationName);
                if (byImpl == null) {
                    byImpl = new LinkedList<Descriptor>();
                    descriptorsByImplementation.put(implementationName, byImpl);
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

}
