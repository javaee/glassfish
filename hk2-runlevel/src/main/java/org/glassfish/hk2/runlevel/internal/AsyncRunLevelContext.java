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
package org.glassfish.hk2.runlevel.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.DescriptorVisibility;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.Visibility;
import org.glassfish.hk2.runlevel.CurrentlyRunningException;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.glassfish.hk2.runlevel.utilities.Utilities;
import org.jvnet.hk2.annotations.Service;

/**
 * @author jwells
 *
 */
@Service @Visibility(DescriptorVisibility.LOCAL)
public class AsyncRunLevelContext {
    private static final Logger logger = Logger.getLogger(AsyncRunLevelContext.class.getName());
    
    private static final Timer timer = new Timer(true);
    
    private static final ThreadFactory THREAD_FACTORY = new RunLevelThreadFactory();
    
    private int currentLevel = RunLevel.RUNLEVEL_VAL_INITIAL;
    private CurrentTaskFutureWrapper currentTask = null;
    
    private static final Executor DEFAULT_EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(true),
            THREAD_FACTORY);
    
    /**
     * The backing maps for this context.
     */
    private final Map<ActiveDescriptor<?>, Object> backingMap =
            new HashMap<ActiveDescriptor<?>, Object>();
    
    /*
     * The within level errors thrown.  This prevents double
     * starting a service that failed within a level
     */
    private final Map<ActiveDescriptor<?>, RuntimeException> levelErrorMap =
            new HashMap<ActiveDescriptor<?>, RuntimeException>();
    
    private boolean wasCancelled = false;
    
    /**
     * The set of services currently being created
     */
    private final HashMap<ActiveDescriptor<?>, Long> creatingDescriptors = new HashMap<ActiveDescriptor<?>, Long>();
    
    /**
     * The set of descriptors that have been hard cancelled
     */
    private final HashSet<ActiveDescriptor<?>> hardCancelledDescriptors = new HashSet<ActiveDescriptor<?>>();
    
    private final LinkedList<ActiveDescriptor<?>> orderedCreationList = new LinkedList<ActiveDescriptor<?>>();
    
    private Executor executor = DEFAULT_EXECUTOR;
    private final ServiceLocator locator;
    private int maxThreads = Integer.MAX_VALUE;
    private RunLevelController.ThreadingPolicy policy = RunLevelController.ThreadingPolicy.FULLY_THREADED;
    private long cancelTimeout = 5 * 1000;
    
    /**
     * Constructor for the guy who does the work
     * 
     * @param locator The locator to use
     */
    @Inject
    private AsyncRunLevelContext(ServiceLocator locator) {
        this.locator = locator;
    }

    /**
     * This is from the {@link Context} API, called by the wrapper
     * 
     * @param activeDescriptor the descriptor to create
     * @param root The root descriptor
     * @return The object created
     */
    @SuppressWarnings("unchecked")
    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor,
            ServiceHandle<?> root) {
        boolean throwWouldBlock;
        if (root == null) {
            throwWouldBlock = false;
        }
        else {
            Object rootBlockObject = root.getServiceData();
            if (rootBlockObject == null) {
                throwWouldBlock = false;
            }
            else {
                throwWouldBlock = ((Boolean) rootBlockObject).booleanValue();
            }
        }
        
        U retVal = null;
        
        int localCurrentLevel;
        synchronized (this) {
            retVal = (U) backingMap.get(activeDescriptor);
            if (retVal != null) return retVal;
            
            RuntimeException previousException = levelErrorMap.get(activeDescriptor);
            if (previousException != null) {
                throw previousException;
            }
            
            if (hardCancelledDescriptors.contains(activeDescriptor)) {
                throw new MultiException(new WasCancelledException(activeDescriptor), false);
            }
            
            while (creatingDescriptors.containsKey(activeDescriptor)) {
                long holdingLock = creatingDescriptors.get(activeDescriptor);
                if (holdingLock == Thread.currentThread().getId()) {
                    throw new MultiException(new IllegalStateException(
                            "Circular dependency involving " + activeDescriptor.getImplementation() +
                            " was found.  Full descriptor is " + activeDescriptor));
                }
                
                if (throwWouldBlock) {
                    throw new MultiException(new WouldBlockException(activeDescriptor), false);
                }
                
                try {
                    this.wait();
                }
                catch (InterruptedException ie) {
                    throw new MultiException(ie);
                }
            }
            
            retVal = (U) backingMap.get(activeDescriptor);
            if (retVal != null) return retVal;
            
            previousException = levelErrorMap.get(activeDescriptor);
            if (previousException != null) {
                throw previousException;
            }
            
            if (hardCancelledDescriptors.contains(activeDescriptor)) {
                throw new MultiException(new WasCancelledException(activeDescriptor), false);
            }
            
            creatingDescriptors.put(activeDescriptor, Thread.currentThread().getId());
            
            localCurrentLevel = currentLevel;
            if (currentTask != null && currentTask.isUp()) {
                localCurrentLevel++;
                
                if (localCurrentLevel > currentTask.getProposedLevel()) {
                    localCurrentLevel = currentTask.getProposedLevel();
                }
            }
        }
        
        RuntimeException error = null;
        try {
            int mode = Utilities.getRunLevelMode(activeDescriptor);

            if (mode == RunLevel.RUNLEVEL_MODE_VALIDATING) {
                validate(activeDescriptor, localCurrentLevel);
            }

            retVal = activeDescriptor.create(root);
            
            return retVal;
        }
        catch (RuntimeException th) {
            if (th instanceof MultiException) {
                if (!CurrentTaskFuture.isWouldBlock((MultiException) th)) {
                    error = th;
                }
                
                // We want WouldBlock rethrown
            }
            else {
                error = th;
            }
            
            throw th;
        }
        finally {
            synchronized (this) {
                boolean hardCancelled = hardCancelledDescriptors.remove(activeDescriptor);
                
                if (retVal != null) {
                    if (!hardCancelled) {
                        backingMap.put(activeDescriptor, retVal);
                        orderedCreationList.addFirst(activeDescriptor);
                    }
                    
                    if (wasCancelled || hardCancelled) {
                        // Even though this service actually ran to completion, we
                        // are going to pretend it failed.  Putting it in the lists
                        // above will ensure it gets properly shutdown
                        
                        MultiException cancelledException = new MultiException(new WasCancelledException(activeDescriptor), false);
                        
                        if (!hardCancelled) {
                            levelErrorMap.put(activeDescriptor, cancelledException);
                        }
                        
                        creatingDescriptors.remove(activeDescriptor);
                        this.notifyAll();
                        
                        throw cancelledException;
                    }
                }
                else if (error != null) {
                    if (!hardCancelled) {
                        levelErrorMap.put(activeDescriptor, error);
                    }
                }
                
                creatingDescriptors.remove(activeDescriptor);
                this.notifyAll();
            }
        }
    }

    /**
     * The {@link Context} API for discovering if a descriptor has been created
     * 
     * @param descriptor The descriptor to find
     * @return true if already created, false otherwise
     */
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        synchronized (this) {
            return backingMap.containsKey(descriptor);
        }
    }
    
    /**
     * No need to lock this, it is called with the lock already held
     * 
     * @param descriptor the non-null descriptor to hard cancel
     */
    /* package */ void hardCancelOne(ActiveDescriptor<?> descriptor) {
        if (creatingDescriptors.containsKey(descriptor)) {
            // This guy has been hard-cancelled, mark it down
            hardCancelledDescriptors.add(descriptor);
        }
    }
    
    /**
     * The {@link Context} API.  Removes a descriptor from the set
     * 
     * @param descriptor The descriptor to destroy
     */
    @SuppressWarnings("unchecked")
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        Object retVal = null;
        synchronized (this) {
            retVal = backingMap.remove(descriptor);
            if (retVal == null) return;
        }
            
        ((ActiveDescriptor<Object>) descriptor).dispose(retVal);
    }
    
    /**
     * Verifies that the run level value of the {@link RunLevel} annotated
     * service described by the given descriptor is valid for activation.
     * Valid means that the run level value is less than or equal to the
     * current or planned run level of the given {@link org.glassfish.hk2.runlevel.RunLevelController}.
     *
     * @param descriptor  the descriptor of the service being activated
     * @param service     the run level service
     *
     * @throws RunLevelException if the validation fails
     */
    private void validate(ActiveDescriptor<?> descriptor, int currentLevel) throws IllegalStateException {

        Integer runLevel = Utilities.getRunLevelValue(descriptor);

        if (runLevel > currentLevel) {
            throw new IllegalStateException("Service " + descriptor.getImplementation() +
                    " was started at level " + currentLevel + " but it has a run level of " +
                    runLevel + ".  The full descriptor is " + descriptor);
        }
    }

    
    /* package */ synchronized int getCurrentLevel() {
        return currentLevel;
    }
    
    /* package */ synchronized void levelCancelled() {
        wasCancelled = true;
    }
    
    /* package */ synchronized void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }
    
    /* package */ synchronized void setPolicy(RunLevelController.ThreadingPolicy policy) {
        this.policy = policy;
    }
    
    /* package */ synchronized void setExecutor(Executor executor) {
        if (executor == null) {
            this.executor = DEFAULT_EXECUTOR;
        }
        else {
            this.executor = executor;
        }
    }
    
    /* package */ synchronized Executor getExecutor() {
        return executor;
    }
    
    /* package */ synchronized RunLevelController.ThreadingPolicy getPolicy() {
        return policy;
    }
    
    /* package */ List<ActiveDescriptor<?>> getOrderedListOfServicesAtLevel(int level) {
        synchronized (this) {
            LinkedList<ActiveDescriptor<?>> retVal = new LinkedList<ActiveDescriptor<?>>();
            
            while (!orderedCreationList.isEmpty()) {
                ActiveDescriptor<?> zero = orderedCreationList.get(0);
                
                int zeroLevel = Utilities.getRunLevelValue(zero);
                
                if (zeroLevel < level) return retVal;
                
                retVal.add(orderedCreationList.remove(0));
            }
            
            return retVal;
        }
        
    }
    
    /**
     * This method is called to change the proceedTo level of the system
     * @param level The level to change to
     * @return A non-null RunLevelFuture that is the handle to give the
     * caller
     * @throws CurrentlyRunningException If there is already a running job
     */
    public RunLevelFuture proceedTo(int level) throws CurrentlyRunningException {
        CurrentTaskFutureWrapper localTask;
        synchronized (this) {
            boolean fullyThreaded = policy.equals(RunLevelController.ThreadingPolicy.FULLY_THREADED);
            
            if (currentTask != null) {
                throw new CurrentlyRunningException(currentTask);
            }
            
            currentTask = new CurrentTaskFutureWrapper(new CurrentTaskFuture(this,
                    executor,
                    locator,
                    level,
                    maxThreads,
                    fullyThreaded,
                    cancelTimeout,
                    timer));
            
            localTask = currentTask;
        }
        
        // Do outside the lock so that when not fully threaded we do not hold the
        // AsyncRunLevelContext lock.  Otherwise this can lead to deadlock
        localTask.getDelegate().go();
            
        return localTask;
    }
    
    /* package */ synchronized void jobDone() {
        currentTask = null;
    }
    
    /**
     * Gets the current task
     * 
     * @return The current task, may be null if there is no current task
     */
    public synchronized RunLevelFuture getCurrentFuture() {
        return currentTask;
    }
    
    /* package */ synchronized void setMaximumThreads(int maximum) {
        if (maximum < 1) {
            maxThreads = 1;
        }
        else {
            maxThreads = maximum;
        }
    }
    
    /* package */ synchronized int getMaximumThreads() {
        return maxThreads;
    }
    
    /* package */ synchronized void clearErrors() {
        levelErrorMap.clear();
        wasCancelled = false;
    }
    
    /* package */ synchronized void setCancelTimeout(long cancelTimeout) {
        this.cancelTimeout = cancelTimeout;
    }
    
    /* package */ synchronized long getCancelTimeout() {
        return cancelTimeout;
    }
    
    private static class RunLevelControllerThread extends Thread {
        private RunLevelControllerThread(Runnable r) {
            super(r);
            setDaemon(true);
            setName(getClass().getSimpleName() + "-"
                    + System.currentTimeMillis());
        }
    }
    
    private static class RunLevelThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread activeThread = new RunLevelControllerThread(runnable);
                
            logger.log(Level.FINE, "new thread: {0}", activeThread);
                
            return activeThread;
        }
    }
}
