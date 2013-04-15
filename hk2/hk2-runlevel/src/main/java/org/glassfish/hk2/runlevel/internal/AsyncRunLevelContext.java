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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
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
@Service
public class AsyncRunLevelContext implements Context<RunLevel> {
    private static final Logger logger = Logger.getLogger(AsyncRunLevelContext.class.getName());
    
    private static final ThreadFactory THREAD_FACTORY = new RunLevelThreadFactory();
    
    private int currentLevel = RunLevel.RUNLEVEL_VAL_INITIAL;
    private CurrentTaskFuture currentTask = null;
    
    /**
     * The backing maps for this context.
     */
    private final Map<ActiveDescriptor<?>, Object> backingMap =
            new HashMap<ActiveDescriptor<?>, Object>();
    
    /**
     * The set of services currently being created
     */
    private final HashSet<ActiveDescriptor<?>> creatingDescriptors = new HashSet<ActiveDescriptor<?>>();
    
    private final LinkedList<ActiveDescriptor<?>> orderedCreationList = new LinkedList<ActiveDescriptor<?>>();
    
    private final Executor executor;
    private final ServiceLocator locator;
    private int maxThreads = Integer.MAX_VALUE;
    private RunLevelController.ThreadingPolicy policy = RunLevelController.ThreadingPolicy.FULLY_THREADED;
    
    @Inject
    private AsyncRunLevelContext(ServiceLocator locator) {
        this.locator = locator;
        
        executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(true),
                THREAD_FACTORY);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return RunLevel.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> U findOrCreate(ActiveDescriptor<U> activeDescriptor,
            ServiceHandle<?> root) {
        U retVal = null;
        
        int localCurrentLevel;
        synchronized (this) {
            retVal = (U) backingMap.get(activeDescriptor);
            if (retVal != null) return retVal;
            
            while (creatingDescriptors.contains(activeDescriptor)) {
                try {
                    this.wait();
                }
                catch (InterruptedException ie) {
                    throw new MultiException(ie);
                }
            }
            
            retVal = (U) backingMap.get(activeDescriptor);
            if (retVal != null) return retVal;
            
            creatingDescriptors.add(activeDescriptor);
            
            localCurrentLevel = currentLevel;
            if (currentTask != null && currentTask.isUp()) {
                localCurrentLevel++;
                
                if (localCurrentLevel > currentTask.getProposedLevel()) {
                    localCurrentLevel = currentTask.getProposedLevel();
                }
            }
        }
        
        try {
            int mode = Utilities.getRunLevelMode(activeDescriptor);

            if (mode == RunLevel.RUNLEVEL_MODE_VALIDATING) {
                validate(activeDescriptor, localCurrentLevel);
            }

            retVal = activeDescriptor.create(root);
            
            return retVal;
        }
        finally {
            synchronized (this) {
                if (retVal != null) {
                    backingMap.put(activeDescriptor, retVal);
                    orderedCreationList.addFirst(activeDescriptor);
                }
                
                creatingDescriptors.remove(activeDescriptor);
                this.notifyAll();
            }
        }
    }

    @Override
    public boolean containsKey(ActiveDescriptor<?> descriptor) {
        synchronized (this) {
            return backingMap.containsKey(descriptor);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void destroyOne(ActiveDescriptor<?> descriptor) {
        Object retVal = null;
        synchronized (this) {
            retVal = backingMap.remove(descriptor);
            if (retVal == null) return;
        }
            
        ((ActiveDescriptor<Object>) descriptor).dispose(retVal);
    }

    @Override
    public boolean supportsNullCreation() {
        // Run-Level services may not be null
        return false;
    }

    @Override
    public boolean isActive() {
        // This context is always active
        return true;
    }

    @Override
    public void shutdown() {
        // Do nothing, shutdown is controlled by the current level
        
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
    
    /* package */ synchronized void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }
    
    /* package */ synchronized void setPolicy(RunLevelController.ThreadingPolicy policy) {
        this.policy = policy;
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
    
    public RunLevelFuture proceedTo(int level) throws CurrentlyRunningException {
        CurrentTaskFuture localTask;
        synchronized (this) {
            boolean fullyThreaded = policy.equals(RunLevelController.ThreadingPolicy.FULLY_THREADED);
            
            if (currentTask != null) {
                throw new CurrentlyRunningException(currentTask);
            }
            
            currentTask = new CurrentTaskFuture(this,
                    executor,
                    locator,
                    level,
                    maxThreads,
                    fullyThreaded);
            
            localTask = currentTask;
        }
        
        // Do outside the lock so that when not fully threaded we do not hold the
        // AsyncRunLevelContext lock.  Otherwise this can lead to deadlock
        localTask.go();
            
        return localTask;
    }
    
    /* package */ synchronized void jobDone() {
        currentTask = null;
    }
    
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
