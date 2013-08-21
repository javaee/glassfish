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
import org.glassfish.hk2.runlevel.ChangeableRunLevelFuture;
import org.glassfish.hk2.runlevel.ErrorInformation;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.runlevel.RunLevelListener;
import org.glassfish.hk2.runlevel.Sorter;
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
public class CurrentTaskFuture implements ChangeableRunLevelFuture {
    private final AsyncRunLevelContext parent;
    private final Executor executor;
    private final ServiceLocator locator;
    private int proposedLevel;
    private final boolean useThreads;
    private final List<ServiceHandle<RunLevelListener>> allListenerHandles;
    private final List<ServiceHandle<Sorter>> allSorterHandles;
    private final int maxThreads;
    
    private UpAllTheWay upAllTheWay;
    private DownAllTheWay downAllTheWay;
    
    private boolean done = false;
    private boolean cancelled = false;
    private boolean inCallback = false;
    
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
        this.maxThreads = maxThreads;
        
        int currentLevel = parent.getCurrentLevel();
        
        allListenerHandles = locator.getAllServiceHandles(RunLevelListener.class);
        allSorterHandles = locator.getAllServiceHandles(Sorter.class);
        
        if (currentLevel == proposedLevel) {
            done = true;
        }
        else if (currentLevel < proposedLevel) {
            upAllTheWay = new UpAllTheWay(proposedLevel, this, allListenerHandles, allSorterHandles, maxThreads, useThreads);
        }
        else {
            downAllTheWay = new DownAllTheWay(proposedLevel, this, allListenerHandles);
        }
    }
    
    /* package */ void go() {
        UpAllTheWay localUpAllTheWay;
        DownAllTheWay localDownAllTheWay;
        
        synchronized (this) {
            localUpAllTheWay = upAllTheWay;
            localDownAllTheWay = downAllTheWay;
        }
        
        if (localUpAllTheWay != null) {
            localUpAllTheWay.go();
        }
        else if (localDownAllTheWay != null) {
            if (useThreads) {
                executor.execute(localDownAllTheWay);
            }
            else {
                localDownAllTheWay.run();
            }
        }
        else {
            parent.jobDone();
        }
    }
    
    @Override
    public boolean isUp() {
        synchronized (this) {
            if (upAllTheWay != null) return true;
            return false;
        }
    }
    
    @Override
    public boolean isDown() {
        synchronized (this) {
            if (downAllTheWay != null) return true;
            return false;
        }
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
    
    @Override
    public int getProposedLevel() {
        synchronized (this) {
            return proposedLevel;
        }
    }
    
    @Override
    public int changeProposedLevel(int proposedLevel) {
        int oldProposedVal;
        boolean needGo = false;
        synchronized (this) {
            if (done) throw new IllegalStateException("Cannot change the proposed level of a future that is already complete");
            if (!inCallback) throw new IllegalStateException(
                    "changeProposedLevel must only be called from inside a RunLevelListener callback method");
            
            oldProposedVal = this.proposedLevel;
            int currentLevel = parent.getCurrentLevel();
            this.proposedLevel = proposedLevel;
            
            if (upAllTheWay != null) {
                if (currentLevel <= proposedLevel) {
                    upAllTheWay.setGoingTo(proposedLevel, false);
                }
                else {
                    // Changing directions to down
                    upAllTheWay.setGoingTo(currentLevel, true); // This will make upAllTheWay stop
                    upAllTheWay = null;
                    
                    downAllTheWay = new DownAllTheWay(proposedLevel, this, allListenerHandles);
                    needGo = true;
                }
            }
            else if (downAllTheWay != null) {
                if (currentLevel >= proposedLevel) {
                    downAllTheWay.setGoingTo(proposedLevel, false);
                }
                else {
                    // Changing directions to up
                    downAllTheWay.setGoingTo(currentLevel, true);  // This will make downAllTheWay stop
                    downAllTheWay = null;
                    
                    upAllTheWay = new UpAllTheWay(proposedLevel, this, allListenerHandles, allSorterHandles, maxThreads, useThreads);
                    needGo = true;
                }
            }
            else {
                if (proposedLevel > currentLevel) {
                    upAllTheWay = new UpAllTheWay(proposedLevel, this, allListenerHandles, allSorterHandles, maxThreads, useThreads);
                    needGo = true;
                }
                else if (proposedLevel < currentLevel) {
                    downAllTheWay = new DownAllTheWay(proposedLevel, this, allListenerHandles);
                    needGo = true;
                }
            }
        }
        
        if (needGo) {
            go();
        }
        
        return oldProposedVal;
    }
    
    private void setInCallback(boolean inCallback) {
        synchronized (this) {
            this.inCallback = inCallback;
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
        AllTheWay allTheWay = null;
        synchronized (this) {
            if (upAllTheWay != null) {
                allTheWay = upAllTheWay;
            }
            else if (downAllTheWay != null) {
                allTheWay = downAllTheWay;
            }
        }
        
        if (allTheWay == null) return null;
        
        Boolean result = null;
        for (;;) {
            try {
                result = allTheWay.waitForResult(timeout, unit);
                if (result == null) {
                    synchronized (this) {
                        if (upAllTheWay != null) {
                            allTheWay = upAllTheWay;
                        }
                        else if (downAllTheWay != null) {
                            allTheWay = downAllTheWay;
                        }
                    }
                    
                    continue;
                }
                
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
    }
    
    private void invokeOnProgress(ChangeableRunLevelFuture job, int level,
            List<ServiceHandle<RunLevelListener>> listeners) {
        setInCallback(true);
        try {
            for (ServiceHandle<RunLevelListener> listener : listeners) {
                try {
                    listener.getService().onProgress(job, level);
                }
                catch (Throwable th) {
                    // TODO:  Need a log message here
               }
            }
        }
        finally {
            setInCallback(false);
        }
    }
    
    private static void invokeOnCancelled(CurrentTaskFuture job, int levelAchieved,
            List<ServiceHandle<RunLevelListener>> listeners) {
        for (ServiceHandle<RunLevelListener> listener : listeners) {
            try {
                listener.getService().onCancelled(new CurrentTaskFutureWrapper(job), levelAchieved);
            }
            catch (Throwable th) {
                // TODO:  Need a log message here
            }
        }
    }
    
    private static ErrorInformation invokeOnError(CurrentTaskFuture job, Throwable th,
            ErrorInformation.ErrorAction action,
            List<ServiceHandle<RunLevelListener>> listeners) {
        ErrorInformationImpl errorInfo = new ErrorInformationImpl(th, action);
        
        for (ServiceHandle<RunLevelListener> listener : listeners) {
            try {
                listener.getService().onError(new CurrentTaskFutureWrapper(job),
                        errorInfo);
            }
            catch (Throwable th2) {
                 // TODO:  Need a log message here
            }
        }
        
        return errorInfo;
    }
    
    private interface AllTheWay {
        /**
         * The method to call on the internal job
         * 
         * @param timeout The amount of time to wait for a result
         * @param unit The unit of the above time value
         * @return True if the job finished, False if the timeout is up prior to the job
         * finishing, and null if the job was repurposed and the caller may now need to
         * listen on a different job
         * @throws InterruptedException On a thread getting jacked
         * @throws MultiException Other exceptions
         */
        public Boolean waitForResult(long timeout, TimeUnit unit) throws InterruptedException, MultiException;
        
    }
            
    
    private class UpAllTheWay implements AllTheWay {
        private final Object lock = new Object();
        
        private int goingTo;
        private final int maxThreads;
        private final boolean useThreads;
        private final CurrentTaskFuture future;
        private final List<ServiceHandle<RunLevelListener>> listeners;
        private final List<ServiceHandle<Sorter>> sorters;
        
        private int workingOn;
        private UpOneJob currentJob;
        private boolean cancelled = false;
        private boolean done = false;
        private boolean repurposed = false;
        private MultiException exception = null;
        
        private UpAllTheWay(int goingTo, CurrentTaskFuture future,
                List<ServiceHandle<RunLevelListener>> listeners,
                List<ServiceHandle<Sorter>> sorters,
                int maxThreads,
                boolean useThreads) {
            this.goingTo = goingTo;
            this.future = future;
            this.listeners = listeners;
            this.maxThreads = maxThreads;
            this.useThreads = useThreads;
            this.sorters = sorters;
            
            workingOn = parent.getCurrentLevel();
        }
        
        private void cancel() {
            synchronized (lock) {
                cancelled = true;
                currentJob.cancel();
            }
        }
        
        @Override
        public Boolean waitForResult(long timeout, TimeUnit unit) throws InterruptedException, MultiException {
            long totalWaitTimeMillis = TimeUnit.MILLISECONDS.convert(timeout, unit);
            
            synchronized (lock) {
                while (totalWaitTimeMillis > 0L && !done && !repurposed) {
                    long startTime = System.currentTimeMillis();
                    
                    lock.wait(totalWaitTimeMillis);
                    
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    totalWaitTimeMillis -= elapsedTime;
                }
                
                if (repurposed) return null;
                
                if (done && (exception != null)) {
                    throw exception;
                }
                
                return done;
            }
        }
        
        private void setGoingTo(int goingTo, boolean repurposed) {
            synchronized (lock) {
                this.goingTo = goingTo;
                if (repurposed) {
                    this.repurposed = true;
                }
            }
        }
        
        private void go() {
            if (useThreads) {
                synchronized (lock) {
                    workingOn++;
                    if (workingOn > goingTo) {
                        if (!repurposed) {
                            parent.jobDone();
                    
                            done = true;
                        }
                        
                        lock.notifyAll();
                        return;
                    }
            
                    currentJob = new UpOneJob(workingOn, this, future, listeners, sorters, maxThreads);
            
                    executor.execute(currentJob);
                    return;
                }
            }
                
            workingOn++;
            while (workingOn <= goingTo) {
                synchronized (lock) {
                    if (done) break;
                    
                    currentJob = new UpOneJob(workingOn, this, future, listeners, sorters, 0);
                }
                
                currentJob.run();
                
                workingOn++;
            }
             
            synchronized (lock) {
                if (done) return;
                
                if (!repurposed) {
                    parent.jobDone();
                
                    done = true;
                }
                
                lock.notifyAll();
            }
        }
        
        private void currentJobComplete(MultiException exception) {
            if (exception != null) {
                ErrorInformation info =
                        invokeOnError(future, exception, ErrorInformation.ErrorAction.GO_TO_NEXT_LOWER_LEVEL_AND_STOP, listeners);
                
                if (!ErrorInformation.ErrorAction.IGNORE.equals(info.getAction())) {
                    DownAllTheWay downer = new DownAllTheWay(workingOn - 1, null, null);
                
                    downer.run();
                
                    synchronized (lock) {                    
                        done = true;
                        this.exception = exception;
                        lock.notifyAll();
                    
                        parent.jobDone();
                    }
                
                    return;
                }
            }
            
            DownAllTheWay downer = null;
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
        private final CurrentTaskFuture currentTaskFuture;
        private final List<ServiceHandle<RunLevelListener>> listeners;
        private final List<ServiceHandle<Sorter>> sorters;
        private final UpAllTheWay master;
        private final int maxThreads;
        private int numJobs;
        private int completedJobs;
        private MultiException exception;
        private boolean cancelled = false;
        private int numJobsRunning = 0;
        
        private UpOneJob(int paramUpToThisLevel,
                UpAllTheWay master,
                CurrentTaskFuture currentTaskFuture,
                List<ServiceHandle<RunLevelListener>> listeners,
                List<ServiceHandle<Sorter>> sorters,
                int maxThreads) {
            this.upToThisLevel = paramUpToThisLevel;
            this.master = master;
            this.maxThreads = maxThreads;
            this.currentTaskFuture = currentTaskFuture;
            this.listeners = listeners;
            this.sorters = sorters;
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
        
        private List<ServiceHandle<?>> applySorters(List<ServiceHandle<?>> jobs) {
            List<ServiceHandle<?>> retVal = jobs;
            
            for (ServiceHandle<Sorter> sorterHandle : sorters) {
                Sorter sorter = sorterHandle.getService();
                if (sorter == null) continue;
                
                List<ServiceHandle<?>> sortedList = sorter.sort(retVal);
                if (sortedList == null) continue;
                
                retVal = sortedList;
            }
            
            return retVal;
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
            
            jobs = applySorters(jobs);
            
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
                ErrorInformation info = invokeOnError(currentTaskFuture, th,
                        ErrorInformation.ErrorAction.GO_TO_NEXT_LOWER_LEVEL_AND_STOP, listeners);
                
                if (ErrorInformation.ErrorAction.IGNORE.equals(info.getAction())) return;
                
                if (exception == null) {
                    exception = new MultiException();
                }
                
                exception.addError(th);
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
    
    /**
     * Goes down all the way to the proposed level
     * 
     * @author jwells
     *
     */
    private class DownAllTheWay implements Runnable, AllTheWay {
        private volatile int goingTo;
        private CurrentTaskFuture future;
        private final List<ServiceHandle<RunLevelListener>> listeners;
        
        private int workingOn;
        
        private boolean cancelled = false;
        private boolean done = false;
        private boolean repurposed = false;
        
        // private MultiException serviceDownErrors = null;
        
        public DownAllTheWay(int goingTo,
                CurrentTaskFuture future,
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
        
        private void setGoingTo(int goingTo, boolean repurposed) {
            synchronized (this) {
                this.goingTo = goingTo;
                if (repurposed) {
                    this.repurposed = true;
                }
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
                
                ErrorInformation errorInfo = null;
                for (ActiveDescriptor<?> removeMe : toRemove) {
                    try {
                        locator.getServiceHandle(removeMe).destroy();
                    }
                    catch (Throwable th) {
                        if (future != null) {
                            errorInfo = invokeOnError(future, th, ErrorInformation.ErrorAction.IGNORE, listeners);
                        }
                    }
                    
                }
                
                if (errorInfo != null && ErrorInformation.ErrorAction.GO_TO_NEXT_LOWER_LEVEL_AND_STOP.equals(errorInfo.getAction())) {
                    goingTo = workingOn;
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
                if (!repurposed) {
                    parent.jobDone();
                
                    done = true;
                }
                
                this.notifyAll();
            }
            
        }
        
        @Override
        public Boolean waitForResult(long timeout, TimeUnit unit) throws InterruptedException, MultiException {
            long totalWaitTimeMillis = TimeUnit.MILLISECONDS.convert(timeout, unit);
            
            synchronized (this) {
                while (totalWaitTimeMillis > 0L && !done && !repurposed) {
                    long startTime = System.currentTimeMillis();
                    
                    this.wait(totalWaitTimeMillis);
                    
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    totalWaitTimeMillis -= elapsedTime;
                }
                
                if (repurposed) return null;
                
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
            fService.setServiceData(!block);
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
    public String toString() {
        return "RunLevelFuture(proposedLevel=" + proposedLevel + "," +
          System.identityHashCode(this) + ")";
    }

    
}
