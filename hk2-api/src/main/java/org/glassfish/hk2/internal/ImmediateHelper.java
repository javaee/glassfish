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
package org.glassfish.hk2.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.DynamicConfigurationListener;
import org.glassfish.hk2.api.ErrorInformation;
import org.glassfish.hk2.api.ErrorService;
import org.glassfish.hk2.api.ErrorType;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.Immediate;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.Operation;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ValidationInformation;
import org.glassfish.hk2.api.ValidationService;
import org.glassfish.hk2.api.Validator;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.utilities.ImmediateErrorHandler;

/**
 * The implementation of the immediate context.  This should NOT be added
 * automatically, and hence is not annotated with {@link org.jvnet.hk2.annotations.Service}
 * <p>
 * This implementation uses a lot of facilities of hk2, so lets explain each one.
 * <p>
 * The thing that makes Immediate services immediate is that they are started as soon as
 * they are noticed.  This is done by implementing the DynamicConfigurationListener, as it
 * will get notified when a configuration has changed.  However, since the creation of user
 * services can take an arbitrarily long time it is better to do that work on a separate
 * thread, which is why we implement Runnable.  The run method is the method that will pull
 * from the queue of work and instantiate (and destroy) the immediate services.
 * <p>
 * However, there is also a desire to be highly efficient.  This means we should not be
 * creating this thread if there is no work for the thread to do.  For this to work
 * we need to know which configuration changes have added or removed an Immediate service.
 * To know this we have implemented the Validation service and Validator.  The validation
 * service records the thread id of a configuration operation that is adding or removing
 * services.  This thread id goes into a map.  But we have to be sure that the map does not
 * grow without bound. To do this we clear the map both when the configuration service has
 * succeeded (in the DynamicConfigurationListener) and when the configuration service has failed
 * (in the ErrorService).  This is why we have implemented the ErrorService.
 * 
 * @author jwells
 */
@Singleton @Visibility(DescriptorVisibility.LOCAL)
public class ImmediateHelper implements DynamicConfigurationListener, Runnable,
    ValidationService, ErrorService, Validator {
    private static final ThreadFactory THREAD_FACTORY = new ImmediateThreadFactory();
    
    private static final Executor DEFAULT_EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(true),
            THREAD_FACTORY);
    
    private final Filter validationFilter;
    private final ServiceLocator locator;
    
    private final HashMap<ActiveDescriptor<?>, HandleAndService> currentImmediateServices = new HashMap<ActiveDescriptor<?>, HandleAndService>();
    private final HashMap<ActiveDescriptor<?>, Long> creating = new HashMap<ActiveDescriptor<?>, Long>();
    private final HashSet<Long> tidsWithWork = new HashSet<Long>();
    
    private final Object queueLock = new Object();
    private boolean threadAvailable;
    private boolean outstandingJob;
    private boolean waitingForWork;
    private boolean firstTime = true;
    
    @Inject
    private ImmediateHelper(ServiceLocator serviceLocator) {
        this.locator = serviceLocator;
        this.validationFilter = new ImmediateLocalLocatorFilter(serviceLocator.getLocatorId());
    }

    /**
     * Delegated to from the Context
     * @param activeDescriptor The descriptor to create
     * @param root The root handle
     * @return The service
     */
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
     * Delegated to from the Context
     * @param descriptor The descriptor to find
     * @return true if this service has been created
     */
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        synchronized (this) {
            return currentImmediateServices.containsKey(descriptor);
        }
    }
    
    private boolean hasWork() {
        long tid = Thread.currentThread().getId();
        
        boolean wasFirst;
        synchronized (this) {
            wasFirst = firstTime;
            firstTime = false;
            
            boolean retVal = tidsWithWork.contains(tid);
            tidsWithWork.remove(tid);
        
            if (retVal || !wasFirst) return retVal;
        }
        
        // OK, we added no Immediate services BUT this is the
        // first DynamicConfigurationService callback, which means
        // that there may already be immediate services in the
        // service locator
        
        List<ActiveDescriptor<?>> immediates = getImmediateServices();
        return !immediates.isEmpty();
    }
    
    @Override
    public void configurationChanged() {
        if (!hasWork()) {
            return;
        }
        
        synchronized (queueLock) {
            outstandingJob = true;
            
            if (!threadAvailable) {
                threadAvailable = true;
                
                DEFAULT_EXECUTOR.execute(this);
            }
            else if (waitingForWork) {
                queueLock.notify();
            }
        }
    }
    
    @Override
    public Filter getLookupFilter() {
        return validationFilter;
    }

    @Override
    public Validator getValidator() {
        return this;
    }

    @Override
    public void onFailure(ErrorInformation errorInformation)
            throws MultiException {
        if (!(ErrorType.DYNAMIC_CONFIGURATION_FAILURE.equals(errorInformation.getErrorType()))) {
            // Only interested in dynamic configuration failures
            long tid = Thread.currentThread().getId();
            
            synchronized (this) {
                tidsWithWork.remove(tid);
            }
            
            return;
        }
        
    }

    @Override
    public boolean validate(ValidationInformation info) {
        if (info.getOperation().equals(Operation.BIND) ||
                info.getOperation().equals(Operation.UNBIND)) {
            long tid = Thread.currentThread().getId();
            
            synchronized (this) {
                tidsWithWork.add(tid);
            }
        }
        
        return true;
    }
    
    /**
     * This thread will wait twenty seconds for new work to come in, and then
     * kill itself
     */
    @Override
    public void run() {
        for(;;) {
            synchronized (queueLock) {
                long decayTime = 20 * 1000L;
                
                while (!outstandingJob && (decayTime > 0L)) {
                    waitingForWork = true;
                    long currentTime = System.currentTimeMillis();
                    try {
                        queueLock.wait(decayTime);
                    }
                    catch (InterruptedException ie) {
                        threadAvailable = false;
                        waitingForWork = false;
                        return;
                    }
                    
                    long elapsedTime = System.currentTimeMillis() - currentTime;
                    decayTime -= elapsedTime;
                }
                waitingForWork = false;
                
                if (!outstandingJob) {
                    threadAvailable = false;
                    return;
                }
                
                outstandingJob = false;
            }
            
            doWork();
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
    
    /**
     * Destroys a single descriptor
     * 
     * @param descriptor The descriptor to destroy
     * @param errorHandlers The handlers for exceptions (if null will get from service locator)
     */
    @SuppressWarnings("unchecked")
    public void destroyOne(ActiveDescriptor<?> descriptor, List<ImmediateErrorHandler> errorHandlers) {
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
    
    /**
     * For when the server shuts down
     */
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
    
    private void doWork() {
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
    
    private static class ImmediateThread extends Thread {
        private ImmediateThread(Runnable r) {
            super(r);
            setDaemon(true);
            setName(getClass().getSimpleName() + "-"
                    + System.currentTimeMillis());
        }
    }
    
    private static class ImmediateThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread activeThread = new ImmediateThread(runnable);
                
            return activeThread;
        }
    }

    private static class ImmediateLocalLocatorFilter implements Filter {
        private final long locatorId;
        
        private ImmediateLocalLocatorFilter(long locatorId) {
            this.locatorId = locatorId;
        }

        @Override
        public boolean matches(Descriptor d) {
            String scope = d.getScope();
            if (scope == null) return false;
            if (d.getLocatorId() != locatorId) return false;
            
            return Immediate.class.getName().equals(scope);
        }
    }
    
    private static class HandleAndService {
        private final ServiceHandle<?> handle;
        private final Object service;
        
        private HandleAndService(ServiceHandle<?> handle, Object service) {
            this.handle = handle;
            this.service = service;
        }
        
        private ServiceHandle<?> getHandle() {
            return handle;
        }
        
        private Object getService() {
            return service;
        }
        
    }

}
