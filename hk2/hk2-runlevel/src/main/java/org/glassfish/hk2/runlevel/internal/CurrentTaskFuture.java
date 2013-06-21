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

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.glassfish.hk2.runlevel.RunLevelListener;
import org.glassfish.hk2.runlevel.utilities.Utilities;

/**
 * This is the implementation of RunLevelFuture.  There should
 * only be one of these active in the system at any time.  Of
 * course users are given a handle to this object, so they can
 * hold onto references to it for as long as they'd like.
 * 
 * @author jwells
 *
 */
public class CurrentTaskFuture implements RunLevelFuture {
    private final AsyncRunLevelContext parent;
    private final Executor executor;
    private final ServiceLocator locator;
    private final int proposedLevel;
    private final boolean useThreads;
    
    private final UpAllTheWay upAllTheWay;
    private final DownAllTheWay downAllTheWay;
    
    private boolean done = false;
    private boolean cancelled = false;
    
    /* package */ CurrentTaskFuture(AsyncRunLevelContext parent,
            Executor executor,
            ServiceLocator locator,
            int proposedLevel,
            int maxThreads,
            boolean useThreads) {
        this.parent = parent;
        this.executor = (useThreads) ? executor : null;
        this.locator = locator;
        this.proposedLevel = proposedLevel;
        this.useThreads = useThreads;
        
        int currentLevel = parent.getCurrentLevel();
        
        List<ServiceHandle<RunLevelListener>> allListenerHandles =
                locator.getAllServiceHandles(RunLevelListener.class);
        
        if (currentLevel == proposedLevel) {
            upAllTheWay = null;
            downAllTheWay = null;
            
            done = true;
            
            parent.jobDone();
        }
        else if (currentLevel < proposedLevel) {
            upAllTheWay = new UpAllTheWay(proposedLevel, this, allListenerHandles, maxThreads, useThreads);
            downAllTheWay = null;
        }
        else {
            downAllTheWay = new DownAllTheWay(proposedLevel, this, allListenerHandles);
            upAllTheWay = null;
        }
    }
    
    /* package */ void go() {
        if (upAllTheWay != null) {
            upAllTheWay.go();
        }
        else if (downAllTheWay != null) {
            if (useThreads) {
                executor.execute(downAllTheWay);
            }
            else {
                downAllTheWay.run();
            }
        }
    }
    
    public boolean isUp() {
        if (upAllTheWay != null) return true;
        return false;
    }
    
    public boolean isDown() {
        if (downAllTheWay != null) return true;
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Future#cancel(boolean)
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized (this) {
            if (done) return false;
            if (cancelled) return false;
            
            cancelled = true;
            
            if (upAllTheWay != null) {
                upAllTheWay.cancel();
            }
            else if (downAllTheWay != null) {
                downAllTheWay.cancel();
            }
            
            return true;
        }
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Future#isCancelled()
     */
    @Override
    public boolean isCancelled() {
        synchronized (this) {
            return cancelled;
        }
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Future#isDone()
     */
    @Override
    public boolean isDone() {
        synchronized (this) {
            return done;
        }
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Future#get()
     */
    @Override
    public Object get() throws InterruptedException, ExecutionException {
        try {
            return get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException te) {
            throw new AssertionError(te);
        }
    }

    /* (non-Javadoc)
     * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        if (upAllTheWay != null) {
            try {
                boolean result = upAllTheWay.waitForResult(timeout, unit);
                if (!result) {
                    throw new TimeoutException();
                }
                
                synchronized (this) {
                    done = true;
                }
                
                return null;
            }
            catch (MultiException me) {
                synchronized (this) {
                    done = true;
                }
                
                throw new ExecutionException(me);
            }
        }
        else if (downAllTheWay != null) {
            try {
                boolean result = downAllTheWay.waitForResult(timeout, unit);
                if (!result) {
                    throw new TimeoutException();
                }
                
                synchronized (this) {
                    done = true;
                }
                
                return null;
            }
            catch (MultiException me) {
                synchronized (this) {
                    done = true;
                }
                
                throw new ExecutionException(me);
            }
        }
        
        return null;
    }
    
    private static void invokeOnProgress(RunLevelFuture job, int level,
            List<ServiceHandle<RunLevelListener>> listeners) {
        for (ServiceHandle<RunLevelListener> listener : listeners) {
            try {
                listener.getService().onProgress(job, level);
            }
            catch (Throwable th) {
                // TODO:  Need a log message here
            }
        }
    }
    
    private static void invokeOnCancelled(RunLevelFuture job, int levelAchieved,
            List<ServiceHandle<RunLevelListener>> listeners) {
        for (ServiceHandle<RunLevelListener> listener : listeners) {
            try {
                listener.getService().onCancelled(job, levelAchieved);
            }
            catch (Throwable th) {
                // TODO:  Need a log message here
            }
        }
    }
    
    private static void invokeOnError(RunLevelFuture job, Throwable th,
            List<ServiceHandle<RunLevelListener>> listeners) {
        for (ServiceHandle<RunLevelListener> listener : listeners) {
            try {
                listener.getService().onError(job, th);
            }
            catch (Throwable th2) {
                // TODO:  Need a log message here
            }
        }
    }
            
    
    private class UpAllTheWay {
        private final Object lock = new Object();
        
        private final int goingTo;
        private final int maxThreads;
        private final boolean useThreads;
        private final RunLevelFuture future;
        private final List<ServiceHandle<RunLevelListener>> listeners;
        
        private int workingOn;
        private UpOneJob currentJob;
        private boolean cancelled = false;
        private boolean done = false;
        private MultiException exception = null;
        
        private UpAllTheWay(int goingTo, RunLevelFuture future,
                List<ServiceHandle<RunLevelListener>> listeners,
                int maxThreads,
                boolean useThreads) {
            this.goingTo = goingTo;
            this.future = future;
            this.listeners = listeners;
            this.maxThreads = maxThreads;
            this.useThreads = useThreads;
            
            workingOn = parent.getCurrentLevel();
        }
        
        private void cancel() {
            synchronized (lock) {
                cancelled = true;
                currentJob.cancel();
            }
        }
        
        private boolean waitForResult(long timeout, TimeUnit unit) throws InterruptedException, MultiException {
            long totalWaitTimeMillis = TimeUnit.MILLISECONDS.convert(timeout, unit);
            
            synchronized (lock) {
                while (totalWaitTimeMillis > 0L && !done) {
                    long startTime = System.currentTimeMillis();
                    
                    lock.wait(totalWaitTimeMillis);
                    
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    totalWaitTimeMillis -= elapsedTime;
                }
                
                if (done && (exception != null)) {
                    throw exception;
                }
                
                return done;
            }
        }
        
        private void go() {
            if (useThreads) {
                synchronized (lock) {
                    workingOn++;
                    if (workingOn > goingTo) {
                        parent.jobDone();
                    
                        done = true;
                        lock.notifyAll();
                        return;
                    }
            
                    currentJob = new UpOneJob(workingOn, this, maxThreads);
            
                    executor.execute(currentJob);
                    return;
                }
            }
                
            workingOn++;
            while (workingOn <= goingTo) {
                synchronized (lock) {
                    if (done) break;
                    
                    currentJob = new UpOneJob(workingOn, this, 0);
                }
                
                currentJob.run();
                
                workingOn++;
            }
             
            synchronized (lock) {
                if (done) return;
                
                parent.jobDone();
                
                done = true;
                lock.notifyAll();
            }
        }
        
        private void currentJobComplete(MultiException exception) {
            DownAllTheWay downer = null;
            if (exception != null) {
                downer = new DownAllTheWay(workingOn - 1, null, null);
                
                downer.run();
                
                invokeOnError(future, exception, listeners);
                
                synchronized (lock) {                    
                    done = true;
                    this.exception = exception;
                    lock.notifyAll();
                    
                    parent.jobDone();
                }
                
                return;
            }
            
            synchronized (lock) {
                if (cancelled) {
                    downer = new DownAllTheWay(workingOn - 1, null, null);
                }
            }
            
            if (downer != null) {
                downer.run();
                
                invokeOnCancelled(future, workingOn - 1, listeners);
                
                synchronized (lock) {
                    done = true;
                    lock.notifyAll();
                        
                    parent.jobDone();
                        
                    return;
                }
            }
            
            parent.setCurrentLevel(workingOn);
            invokeOnProgress(future, workingOn, listeners);
                
            if (useThreads) {
                go();
            }
        }
        
    }
    
    private class UpOneJob implements Runnable {
        private final Object lock = new Object();
        private final int upToThisLevel;
        private final UpAllTheWay master;
        private final int maxThreads;
        private int numJobs;
        private int completedJobs;
        private MultiException exception = null;
        private boolean cancelled = false;
        private int numJobsRunning = 0;
        
        private UpOneJob(int paramUpToThisLevel, UpAllTheWay master, int maxThreads) {
            this.upToThisLevel = paramUpToThisLevel;
            this.master = master;
            this.maxThreads = maxThreads;
        }
        
        private void cancel() {
            synchronized (lock) {
                cancelled = true;
            }
        }
        
        private void jobRunning() {
            numJobsRunning++;
        }
        
        private void jobFinished() {
            numJobsRunning--;
        }
        
        private int getJobsRunning() {
            return numJobsRunning;
        }

        @Override
        public void run() {
            Object jobsLock = new Object();
            List<ServiceHandle<?>> jobs = locator.getAllServiceHandles(new IndexedFilter() {

                @Override
                public boolean matches(Descriptor d) {
                    return (upToThisLevel == Utilities.getRunLevelValue(d));
                }

                @Override
                public String getAdvertisedContract() {
                    return RunLevel.class.getName();
                }

                @Override
                public String getName() {
                    return null;
                }
                
            });
            
            numJobs = jobs.size();
            if (numJobs <= 0) {
                jobComplete();
                return;
            }
            
            int runnersToCreate = ((numJobs < maxThreads) ? numJobs : maxThreads) - 1;
            if (!useThreads) runnersToCreate = 0;
            
            for (int lcv = 0; lcv < runnersToCreate; lcv++) {
                QueueRunner runner = new QueueRunner(jobsLock, jobs, this, lock, maxThreads);
                
                executor.execute(runner);
            }
            
            QueueRunner myRunner = new QueueRunner(jobsLock, jobs, this, lock, maxThreads);
            myRunner.run();
        }
        
        private void fail(Throwable th) {
            synchronized (lock) {
                if (exception == null) {
                    exception = new MultiException(th);
                }
                else {
                    exception.addError(th);
                }
            }
        }
        
        private void jobComplete() {
            boolean complete = false;
            synchronized (lock) {
                completedJobs++;
                if (completedJobs >= numJobs) {
                    complete = true;
                }
            }
            
            if (complete) {
                master.currentJobComplete(exception);
            }
        }
        
    }
    
    public class DownAllTheWay implements Runnable {
        private final int goingTo;
        private final RunLevelFuture future;
        private final List<ServiceHandle<RunLevelListener>> listeners;
        
        private int workingOn;
        
        private boolean cancelled = false;
        private boolean done = false;
        
        private MultiException serviceDownErrors = null;
        
        public DownAllTheWay(int goingTo,
                RunLevelFuture future,
                List<ServiceHandle<RunLevelListener>> listeners) {
            this.goingTo = goingTo;
            this.future = future;
            this.listeners = listeners;
            
            if (future == null) {
                // This is an error or cancelled case, so we are pretending
                // we have gotten higher than we have
                workingOn = parent.getCurrentLevel() + 1;
            }
            else {
                workingOn = parent.getCurrentLevel();
            }
        }
        
        private void cancel() {
            synchronized (this) {
                cancelled = true;
            }
        }

        @Override
        public void run() {
            while (workingOn > goingTo) {
                boolean runOnCancelled;
                synchronized (this) {
                    runOnCancelled = cancelled && (future != null);
                }
                
                if (runOnCancelled) {
                    // Run outside of lock
                    invokeOnCancelled(future, workingOn, listeners);
                }
                
                synchronized (this) {
                    if (cancelled) {
                        done = true;
                        this.notifyAll();
                        
                        return;
                    }
                }
                
                int proceedingTo = workingOn - 1;
                
                // This happens FIRST.  Here the definition of
                // the current level is that level which is guaranteed
                // to have ALL of its known services started.  Once
                // we destroy the first one of them (or are about to)
                // then we are officially at the next level
                parent.setCurrentLevel(proceedingTo);
                
                // But we don't call the proceedTo until all those services are gone
                List<ActiveDescriptor<?>> toRemove = parent.getOrderedListOfServicesAtLevel(workingOn);
                
                for (ActiveDescriptor<?> removeMe : toRemove) {
                    try {
                        locator.getServiceHandle(removeMe).destroy();
                    }
                    catch (Throwable th) {
                        if (future != null) {
                        	if (serviceDownErrors == null) serviceDownErrors = new MultiException();
                        	
                        	serviceDownErrors.addError(th);
                            
                        }
                    }
                    
                }
                
                if (serviceDownErrors != null) {
                	invokeOnError(future, serviceDownErrors, listeners);
                }
                
                workingOn--;
                
                if (future != null) {
                    invokeOnProgress(future, proceedingTo, listeners);
                }
            }
            
            if (future == null) {
                // This is done as part of a cancel or error, do no
                // notifying this is special
                return;
            }
            
            synchronized (this) {
                parent.jobDone();
                
                done = true;
                this.notifyAll();
            }
            
        }
        
        private boolean waitForResult(long timeout, TimeUnit unit) throws InterruptedException, MultiException {
            long totalWaitTimeMillis = TimeUnit.MILLISECONDS.convert(timeout, unit);
            
            synchronized (this) {
                while (totalWaitTimeMillis > 0L && !done) {
                    long startTime = System.currentTimeMillis();
                    
                    this.wait(totalWaitTimeMillis);
                    
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    totalWaitTimeMillis -= elapsedTime;
                }
                
                return done;
            }
        }
        
    }
    
    private static class QueueRunner implements Runnable {
        private final Object queueLock;
        private final List<ServiceHandle<?>> queue;
        private final UpOneJob parent;
        private final Object parentLock;
        private final int maxThreads;
        private ServiceHandle<?> wouldHaveBlocked;
        private HashSet<ActiveDescriptor<?>> alreadyTried = new HashSet<ActiveDescriptor<?>>();
        
        private QueueRunner(Object queueLock,
                List<ServiceHandle<?>> queue,
                UpOneJob parent,
                Object parentLock,
                int maxThreads) {
            this.queueLock = queueLock;
            this.queue = queue;
            this.parent = parent;
            this.parentLock = parentLock;
            this.maxThreads = maxThreads;
        }

        @Override
        public void run() {
            boolean didUp = false;
            for (;;) {
                ServiceHandle<?> job;
                boolean block;
                synchronized(queueLock) {
                    if (didUp) parent.jobFinished();
                    
                    if (wouldHaveBlocked != null) {
                        alreadyTried.add(wouldHaveBlocked.getActiveDescriptor());
                        
                        queue.add(queue.size(), wouldHaveBlocked);
                        wouldHaveBlocked = null;
                    }
                    
                    if (queue.isEmpty()) return;
                    
                    if (maxThreads <= 0) {
                        block = true;
                    }
                    else {
                        int currentlyEmptyThreads = maxThreads - parent.getJobsRunning();
                        block = queue.size() <= currentlyEmptyThreads;
                    }
                    
                    if (block) {
                        job = queue.remove(0);
                    }
                    else {
                        job = null;
                        for (int lcv = 0; lcv < queue.size(); lcv++) {
                            ActiveDescriptor<?> candidate = queue.get(lcv).getActiveDescriptor();
                            if (!alreadyTried.contains(candidate)) {
                                job = queue.remove(lcv);
                                break;
                            }
                        }
                        if (job == null) {
                            // Every job in the queue is one I've tried already
                            job = queue.remove(0);
                            
                            block = true;
                        }
                    }
                    
                    parent.jobRunning();
                    didUp = true;
                }
                
                oneJob(job, block);
            }
            
        }
        
        private void oneJob(ServiceHandle<?> fService, boolean block) {
            fService.setServiceData(new Boolean(!block));
            boolean completed = true;
            try {
                boolean ok;
                synchronized (parentLock) {
                    ok = (!parent.cancelled && (parent.exception == null));
                }
                    
                if (ok) {
                    fService.getService();
                }
            }
            catch (MultiException me) {
                if (!block && isWouldBlock(me)) {
                    // In this case completed is FALSE, as the job has NOT completed
                    wouldHaveBlocked = fService;
                    completed = false;
                }
                else {
                    parent.fail(me);
                }
            }
            catch (Throwable th) {
                parent.fail(th);
            }
            finally {
                fService.setServiceData(null);
                if (completed) {
                    parent.jobComplete();
                }
            }
        }
    }
    
    private final static boolean isWouldBlock(MultiException me) {
        for (Throwable th : me.getErrors()) {
            if (th instanceof WouldBlockException) return true;
        }
        
        return false;
    }

    @Override
    public int getProposedLevel() {
        return proposedLevel;
    }
    
    @Override
    public int changeProposedLevel(int proposedLevel) {
        throw new AssertionError("changeProposedLevel not yet implemented");
    }
    
    public String toString() {
        return "RunLevelFuture(proposedLevel=" + proposedLevel + "," +
          System.identityHashCode(this) + ")";
    }

    
}
