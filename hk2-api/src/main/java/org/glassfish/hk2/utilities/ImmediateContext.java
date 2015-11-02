/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.utilities;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.Immediate;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.internal.HandleAndService;
import org.glassfish.hk2.internal.ImmediateLocalLocatorFilter;

/**
 * This is the {@link Context} implementation for the {@link Immediate}
 * scope
 * 
 * @author jwells
 *
 */
@Singleton @Visibility(DescriptorVisibility.LOCAL)
public class ImmediateContext implements Context<Immediate>{
    private final HashMap<ActiveDescriptor<?>, HandleAndService> currentImmediateServices = new HashMap<ActiveDescriptor<?>, HandleAndService>();
    private final HashMap<ActiveDescriptor<?>, Long> creating = new HashMap<ActiveDescriptor<?>, Long>();
    
    private final ServiceLocator locator;
    private final Filter validationFilter;
    
    @Inject
    private ImmediateContext(ServiceLocator locator) {
        this.locator = locator;
        validationFilter = new ImmediateLocalLocatorFilter(locator.getLocatorId());
    }
    
    @Override
    public Class<? extends Annotation> getScope() {
        return Immediate.class;
    }

    /**
     * @param activeDescriptor The descriptor to create
     * @param root The root handle
     * @return The service
     */
    @Override
    @SuppressWarnings("unchecked")
    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor,
            ServiceHandle<?> root) {
        U retVal = null;
        
        synchronized (this) {
            HandleAndService has = currentImmediateServices.get(activeDescriptor);
            if (has != null) {
                return (U) has.getService();
            }
            
            while (creating.containsKey(activeDescriptor)) {
                long alreadyCreatingThread = creating.get(activeDescriptor);
                if (alreadyCreatingThread == Thread.currentThread().getId()) {
                    throw new MultiException(new IllegalStateException(
                            "A circular dependency involving Immediate service " + activeDescriptor.getImplementation() +
                            " was found.  Full descriptor is " + activeDescriptor));
                }
                
                try {
                    this.wait();
                }
                catch (InterruptedException ie) {
                    throw new MultiException(ie);
                }
                
            }
            
            has = currentImmediateServices.get(activeDescriptor);
            if (has != null) {
                return (U) has.getService();
            }
            
            creating.put(activeDescriptor, Thread.currentThread().getId());
        }
        
        try {
            retVal = activeDescriptor.create(root);
        }
        finally {
            synchronized (this) {
                ServiceHandle<?> discoveredRoot = null;
                if (root != null) {
                    if (root.getActiveDescriptor().equals(activeDescriptor)) {
                        discoveredRoot = root;
                    }
                }
                
                if (retVal != null) {
                    currentImmediateServices.put(activeDescriptor, new HandleAndService(discoveredRoot, retVal));
                }
                
                creating.remove(activeDescriptor);
                this.notifyAll();
            }
            
        }
        
        return retVal;
    }

    /**
     * @param descriptor The descriptor to find
     * @return true if this service has been created
     */
    @Override
    public synchronized boolean containsKey(ActiveDescriptor<?> descriptor) {
        return currentImmediateServices.containsKey(descriptor);
    }

    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        destroyOne(descriptor, null);
    }
    
    /**
     * Destroys a single descriptor
     * 
     * @param descriptor The descriptor to destroy
     * @param errorHandlers The handlers for exceptions (if null will get from service locator)
     */
    @SuppressWarnings("unchecked")
    private void destroyOne(ActiveDescriptor<?> descriptor, List<ImmediateErrorHandler> errorHandlers) {
        if (errorHandlers == null) {
            errorHandlers = locator.getAllServices(ImmediateErrorHandler.class);
        }
        
        synchronized (this) {
            HandleAndService has = currentImmediateServices.remove(descriptor);
            Object instance = has.getService();
        
            try {
                ((ActiveDescriptor<Object>) descriptor).dispose(instance);
            }
            catch (Throwable th) {
                for (ImmediateErrorHandler ieh : errorHandlers) {
                    try {
                        ieh.preDestroyFailed(descriptor, th);
                    }
                    catch (Throwable th2) {
                        // ignore
                    }
                }
            }
            
        }
        
    }

    @Override
    public boolean supportsNullCreation() {
        return false;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    /**
     * For when the server shuts down
     */
    @Override
    public void shutdown() {
        List<ImmediateErrorHandler> errorHandlers = locator.getAllServices(ImmediateErrorHandler.class);
        
        synchronized (this) {
            for (Map.Entry<ActiveDescriptor<?>, HandleAndService> entry :
                new HashSet<Map.Entry<ActiveDescriptor<?>, HandleAndService>>(currentImmediateServices.entrySet())) {
                HandleAndService has = entry.getValue();
                
                ServiceHandle<?> handle = has.getHandle();
                
                if (handle != null) {
                    handle.destroy();
                }
                else {
                    destroyOne(entry.getKey(), errorHandlers);
                }
                
            }
            
            
        }
    }
    
    private List<ActiveDescriptor<?>> getImmediateServices() {
        List<ActiveDescriptor<?>> inScopeAndInThisLocator;
        try {
            inScopeAndInThisLocator = locator.getDescriptors(validationFilter);
        }
        catch (IllegalStateException ise) {
            // locator has been shutdown
            inScopeAndInThisLocator = Collections.emptyList();
        }
        
        return inScopeAndInThisLocator;
    }
    
    public Filter getValidationFilter() {
        return validationFilter;
    }
    
    public void doWork() {
        List<ActiveDescriptor<?>> inScopeAndInThisLocator = getImmediateServices();
        
        List<ImmediateErrorHandler> errorHandlers;
        try {
            errorHandlers = locator.getAllServices(ImmediateErrorHandler.class);
        }
        catch (IllegalStateException ise) {
            // Locator has been shut down
            return;
        }
        
        LinkedHashSet<ActiveDescriptor<?>> oldSet;
        LinkedHashSet<ActiveDescriptor<?>> newFullSet = new LinkedHashSet<ActiveDescriptor<?>>(inScopeAndInThisLocator);
        LinkedHashSet<ActiveDescriptor<?>> addMe = new LinkedHashSet<ActiveDescriptor<?>>();
        
        synchronized (this) {
            // First thing to do is wait until all the things in-flight have gone
            while (creating.size() > 0) {
                try {
                    this.wait();
                }
                catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }
            }
            
            oldSet = new LinkedHashSet<ActiveDescriptor<?>>(currentImmediateServices.keySet());
            
            for (ActiveDescriptor<?> ad : inScopeAndInThisLocator) {
                if (!oldSet.contains(ad)) {
                  addMe.add(ad);
                }
            }
                    
            oldSet.removeAll(newFullSet);
            
            for (ActiveDescriptor<?> gone : oldSet) {
                HandleAndService has = currentImmediateServices.get(gone);
                ServiceHandle<?> handle = has.getHandle();
                
                if (handle != null) {
                    handle.destroy();
                }
                else {
                    destroyOne(gone, errorHandlers);
                }
            }
        }
        
        for (ActiveDescriptor<?> ad : addMe) {
            // Create demand
            try {
                locator.getServiceHandle(ad).getService();
            }
            catch (Throwable th) {
                for (ImmediateErrorHandler ieh : errorHandlers) {
                    try {
                        ieh.postConstructFailed(ad, th);
                    }
                    catch (Throwable th2) {
                        // ignore
                    }
                }
                
            }
            
        }
    }

}
