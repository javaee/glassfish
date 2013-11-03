/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.DynamicConfigurationListener;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.Immediate;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.utilities.ImmediateErrorHandler;

/**
 * The implementation of the immediate context.  This should NOT be added
 * automatically, and hence is not annotated with {@link org.jvnet.hk2.annotations.Service}
 * 
 * @author jwells
 */
@Singleton @Visibility(DescriptorVisibility.LOCAL)
public class ImmediateContext implements Context<Immediate>, DynamicConfigurationListener, Runnable {
    private static final ThreadFactory THREAD_FACTORY = new ImmediateThreadFactory();
    
    private static final Executor DEFAULT_EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(true),
            THREAD_FACTORY);
    
    private final HashMap<ActiveDescriptor<?>, Object> currentImmediateServices = new HashMap<ActiveDescriptor<?>, Object>();
    private final HashSet<ActiveDescriptor<?>> creating = new HashSet<ActiveDescriptor<?>>();
    
    private final Object queueLock = new Object();
    private boolean threadAvailable;
    private boolean outstandingJob;
    private boolean waitingForWork;
    
    @Inject
    private ServiceLocator locator;

    @Override
    public Class<? extends Annotation> getScope() {
        return Immediate.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor,
            ServiceHandle<?> root) {
        U retVal;
        
        synchronized (this) {
            retVal = (U) currentImmediateServices.get(activeDescriptor);
            if (retVal != null) return retVal;
            
            while (creating.contains(activeDescriptor)) {
                try {
                    this.wait();
                }
                catch (InterruptedException ie) {
                    throw new MultiException(ie);
                }
                
            }
            
            retVal = (U) currentImmediateServices.get(activeDescriptor);
            if (retVal != null) return retVal;
            
            creating.add(activeDescriptor);
        }
        
        try {
            retVal = (U) activeDescriptor.create(root);
        }
        finally {
            synchronized (this) {
                if (retVal != null) {
                    currentImmediateServices.put(activeDescriptor, retVal);
                }
                
                creating.remove(activeDescriptor);
                this.notifyAll();
            }
            
        }
        
        return retVal;
    }

    @Override
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        synchronized (this) {
            return currentImmediateServices.containsKey(descriptor);
        }
    }

    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        // Do nothing, Immediate services are not destroyed this way
        
    }

    @Override
    public boolean supportsNullCreation() {
        return false;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void shutdown() {
        // Do nothing
    }
    
    private boolean hasWork() {
        synchronized (queueLock) {
            // The point of hasWork is to avoid creating a thread
            // if there is no work for the thread to do.  So if
            // there is already a thread it doesn't hurt to just
            // have the thread check to see if there is work
            if (threadAvailable) return true;
        }
        
        List<ActiveDescriptor<?>> inScopeAndInThisLocator = getImmediateServices();
        
        HashSet<ActiveDescriptor<?>> oldSet;
        HashSet<ActiveDescriptor<?>> newFullSet = new HashSet<ActiveDescriptor<?>>(inScopeAndInThisLocator);
        
        synchronized(this) {
            oldSet = new HashSet<ActiveDescriptor<?>>(currentImmediateServices.keySet());
            oldSet.addAll(creating);
        }
        
        for (ActiveDescriptor<?> ad : inScopeAndInThisLocator) {
            if (!oldSet.contains(ad)) {
                // New guy to add
                return true;
            }
        }
                
        oldSet.removeAll(newFullSet);
        
        return !oldSet.isEmpty();
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
        final long locatorId = locator.getLocatorId();
        
        List<ActiveDescriptor<?>> inScopeAndInThisLocator = locator.getDescriptors(new Filter() {

            @Override
            public boolean matches(Descriptor d) {
                if (d.getScope() == null) return false;
                if (locatorId != d.getLocatorId()) return false;
                
                return d.getScope().equals(Immediate.class.getName());
            }
            
        });
        
        return inScopeAndInThisLocator;
    }
    
    @SuppressWarnings("unchecked")
    private void doWork() {
        List<ActiveDescriptor<?>> inScopeAndInThisLocator = getImmediateServices();
        
        List<ImmediateErrorHandler> errorHandlers = locator.getAllServices(ImmediateErrorHandler.class);
        
        HashSet<ActiveDescriptor<?>> oldSet;
        HashSet<ActiveDescriptor<?>> newFullSet = new HashSet<ActiveDescriptor<?>>(inScopeAndInThisLocator);
        HashSet<ActiveDescriptor<?>> addMe = new HashSet<ActiveDescriptor<?>>();
        
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
            
            oldSet = new HashSet<ActiveDescriptor<?>>(currentImmediateServices.keySet());
            
            for (ActiveDescriptor<?> ad : inScopeAndInThisLocator) {
                if (!oldSet.contains(ad)) {
                  addMe.add(ad);
                }
            }
                    
            oldSet.removeAll(newFullSet);
            
            for (ActiveDescriptor<?> gone : oldSet) {
                Object instance = currentImmediateServices.remove(gone);
                
                try {
                    ((ActiveDescriptor<Object>) gone).dispose(instance);
                }
                catch (Throwable th) {
                    for (ImmediateErrorHandler ieh : errorHandlers) {
                        try {
                            ieh.preDestroyFailed(gone, th);
                        }
                        catch (Throwable th2) {
                            // ignore
                        }
                    }
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

}
