/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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

import java.util.LinkedList;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.FactoryDescriptors;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.utilities.FactoryDescriptorsImpl;
import org.glassfish.hk2.utilities.reflection.Pretty;

/**
 * The system implementation of the DynamicConfiguration service
 * 
 * @author jwells
 */
public class DynamicConfigurationImpl implements DynamicConfiguration {
    private final ServiceLocatorImpl locator;
    private final LinkedList<SystemDescriptor<?>> allDescriptors = new LinkedList<SystemDescriptor<?>>();
    private final LinkedList<Filter> allUnbindFilters = new LinkedList<Filter>();
    private final LinkedList<Filter> allIdempotentFilters = new LinkedList<Filter>();
    
    private final Object lock = new Object();
    private boolean committed = false;

    /**
     * Created by the generator, and hence must be public
     * 
     * @param locator The locator for which this will be the configuration service
     */
    public DynamicConfigurationImpl(ServiceLocatorImpl locator) {
        this.locator = locator;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Configuration#bind(org.glassfish.hk2.api.Descriptor)
     */
    @Override
    public <T> ActiveDescriptor<T> bind(Descriptor key) {
        return bind(key, true);
    }
    
    @Override
    public <T> ActiveDescriptor<T> bind(Descriptor key, boolean requiresDeepCopy) {
        checkState();
        checkDescriptor(key);

        SystemDescriptor<T> sd = new SystemDescriptor<T>(key,
                requiresDeepCopy,
                locator,
                locator.getNextServiceId());

        allDescriptors.add(sd);

        return sd;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Configuration#bind(org.glassfish.hk2.api.FactoryDescriptors)
     */
    @Override
    public FactoryDescriptors bind(FactoryDescriptors factoryDescriptors) {
        return bind(factoryDescriptors, true);
    }
    
    @Override
    public FactoryDescriptors bind(FactoryDescriptors factoryDescriptors, boolean requiresDeepCopy) {
        if (factoryDescriptors == null) throw new IllegalArgumentException("factoryDescriptors is null");
        
        // Now a bunch of validations
        Descriptor asService = factoryDescriptors.getFactoryAsAService();
        Descriptor asFactory = factoryDescriptors.getFactoryAsAFactory();
        
        checkDescriptor(asService);
        checkDescriptor(asFactory);
        
        String implClassService = asService.getImplementation();
        String implClassFactory = asFactory.getImplementation();
        
        if (!implClassService.equals(implClassFactory)) {
            throw new IllegalArgumentException("The implementation classes must match (" +
                implClassService + "/" + implClassFactory + ")");
        }
        
        if (!asService.getDescriptorType().equals(DescriptorType.CLASS)) {
            throw new IllegalArgumentException("The getFactoryAsService descriptor must be of type CLASS");
        }
        if (!asFactory.getDescriptorType().equals(DescriptorType.PROVIDE_METHOD)) {
            throw new IllegalArgumentException("The getFactoryAsFactory descriptor must be of type PROVIDE_METHOD");
        }
        
        final SystemDescriptor<?> boundAsService = new SystemDescriptor<Object>(asService,
                requiresDeepCopy,
                locator,
                locator.getNextServiceId());

        // Link the factory descriptor to the service descriptor for the factory
        final SystemDescriptor<?> boundAsFactory = new SystemDescriptor<Object>(asFactory,
                requiresDeepCopy,
                locator,
                locator.getNextServiceId());
        
        if (asService instanceof ActiveDescriptor) {
            boundAsFactory.setFactoryIds(boundAsService.getLocatorId(),
                boundAsService.getServiceId());
        }

        // Bind the factory first, so normally people get the factory, not the service
        allDescriptors.add(boundAsFactory);
        allDescriptors.add(boundAsService);

        return new FactoryDescriptorsImpl(boundAsService, boundAsFactory);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Configuration#addActiveDescriptor(org.glassfish.hk2.api.ActiveDescriptor)
     */
    @Override
    public <T> ActiveDescriptor<T> addActiveDescriptor(ActiveDescriptor<T> activeDescriptor)
            throws IllegalArgumentException {
        return addActiveDescriptor(activeDescriptor, true);
    }
    
    @Override
    public <T> ActiveDescriptor<T> addActiveDescriptor(ActiveDescriptor<T> activeDescriptor, boolean requiresDeepCopy)
            throws IllegalArgumentException {
        checkState();
        checkDescriptor(activeDescriptor);
        
        if (!activeDescriptor.isReified()) {
            throw new IllegalArgumentException();
        }
        
        checkReifiedDescriptor(activeDescriptor);
        
        SystemDescriptor<T> retVal = new SystemDescriptor<T>(activeDescriptor,
                requiresDeepCopy,
                locator,
                locator.getNextServiceId());
        
        allDescriptors.add(retVal);
        
        return retVal;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Configuration#addActiveDescriptor(java.lang.Class)
     */
    @Override
    public <T> ActiveDescriptor<T> addActiveDescriptor(Class<T> rawClass)
            throws IllegalArgumentException {
        AutoActiveDescriptor<T> ad = Utilities.createAutoDescriptor(rawClass, locator);
        
        checkReifiedDescriptor(ad);
        
        ActiveDescriptor<T> retVal = addActiveDescriptor(ad, false);
        
        ad.resetSelfDescriptor(retVal);
        
        return retVal;
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.DynamicConfiguration#addActiveFactoryDescriptor(java.lang.Class)
     */
    @Override
    public <T> FactoryDescriptors addActiveFactoryDescriptor(
            Class<? extends Factory<T>> rawFactoryClass) throws MultiException,
            IllegalArgumentException {
        Collector collector = new Collector();
        Utilities.checkFactoryType(rawFactoryClass, collector);
        collector.throwIfErrors();
        
        final ActiveDescriptor<?> factoryDescriptor = addActiveDescriptor(rawFactoryClass);
        ActiveDescriptor<?> userMethodDescriptor = Utilities.createAutoFactoryDescriptor(rawFactoryClass, factoryDescriptor, locator);
        final ActiveDescriptor<?> methodDescriptor = addActiveDescriptor(userMethodDescriptor);
        
        return new FactoryDescriptorsImpl(factoryDescriptor, methodDescriptor);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Configuration#addUnbindFilter(org.glassfish.hk2.api.Filter)
     */
    @Override
    public void addUnbindFilter(Filter unbindFilter)
            throws IllegalArgumentException {
        if (unbindFilter == null) throw new IllegalArgumentException();
        
        allUnbindFilters.add(unbindFilter);
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.DynamicConfiguration#addIdempotentFilter(org.glassfish.hk2.api.Filter[])
     */
    @Override
    public void addIdempotentFilter(Filter... idempotentFilter)
            throws IllegalArgumentException {
        if (idempotentFilter == null) throw new IllegalArgumentException();
        for (Filter iFilter : idempotentFilter) {
            if (iFilter == null) throw new IllegalArgumentException();
        }
        
        for (Filter iFilter : idempotentFilter) {
            allIdempotentFilters.add(iFilter);
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.DynamicConfiguration#commit()
     */
    @Override
    public void commit() throws MultiException {
        synchronized (lock) {
            checkState();
            
            committed = true;
        }
        
        locator.addConfiguration(this);
    }
    
    private void checkState() {
        synchronized (lock) {
            if (committed) throw new IllegalStateException();
        }
    }
    
    private static void checkDescriptor(Descriptor d) {
        if (d == null) throw new IllegalArgumentException();
        if (d.getImplementation() == null) throw new IllegalArgumentException();
        if (d.getAdvertisedContracts() == null) throw new IllegalArgumentException();
        if (d.getDescriptorType() == null) throw new IllegalArgumentException();
        if (d.getDescriptorVisibility() == null) throw new IllegalArgumentException();
        if (d.getMetadata() == null) throw new IllegalArgumentException();
        if (d.getQualifiers() == null) throw new IllegalArgumentException();
    }
    
    private static void checkReifiedDescriptor(ActiveDescriptor<?> d) {
        if (d.isProxiable() == null) return;
        if (!d.isProxiable()) return;
        
        // Now check to see if the scope is unproxiable
        if (Utilities.isUnproxiableScope(d.getScopeAnnotation())) throw new IllegalArgumentException();
    }

    /**
     * @return the allDescriptors
     */
    /* package */ LinkedList<SystemDescriptor<?>> getAllDescriptors() {
        return allDescriptors;
    }
    
    /* package */ LinkedList<Filter> getUnbindFilters() {
        return allUnbindFilters;
    }
    
    /* package */ LinkedList<Filter> getIdempotentFilters() {
        return allIdempotentFilters;
    }
    
    public String toString() {
        return "DynamicConfigurationImpl(" + locator + "," +
            Pretty.collection(allDescriptors) + "," +
            Pretty.collection(allUnbindFilters) + "," +
            System.identityHashCode(this) + ")";
    }

    

    
}
