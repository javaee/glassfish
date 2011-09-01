/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.glassfish.hk2.AsyncPostConstruct;

/**
 * Helper class that will manage all {@link AsyncPostConstruct} services
 * and Futures for completion.
 *
 * <p/>
 * Once a service or Future is found to be completed, it is dropped.
 *
 * @author Jeff Trent
 */
public class AsyncWaiter {

    private Collection<AsyncPostConstruct> watches;
    
    private AsyncPostConstruct workingOn;
    
    
    /**
     * Clear the collection of watches, regardless of state.
     */
    public synchronized void clear() {
        watches = null;
        workingOn = null;
    }
    
    /**
     * Watches an inhabitant if the service implements {@link AsyncPostConstruct}.
     */
    public synchronized void watchIfNecessary(Inhabitant<?> i) {
        Object service = i.get();
        if (AsyncPostConstruct.class.isInstance(service)) {
            if (!AsyncPostConstruct.class.cast(service).isDone()) {
                if (null == watches) {
                    watches = new ArrayList<AsyncPostConstruct>();
                }
                watches.add(new AsyncInhabitant(i));
            }
        }
    }

    /**
     * Watches a Future for completion.
     */
    public synchronized void watchIfNecessary(Future<?> f) {
        if (!f.isDone()) {
            if (null == watches) {
                watches = new ArrayList<AsyncPostConstruct>();
            }
            watches.add(new AsyncFuture(f));
        }
    }
    
    /**
     * Waits for all watches to be done. This might be a blocking operation.
     */
    public synchronized void waitForDone() throws ExecutionException, TimeoutException, InterruptedException {
        if (null != watches) {
            Iterator<AsyncPostConstruct> iter = watches.iterator();
            while (iter.hasNext()) {
                workingOn = iter.next();
                workingOn.waitForDone();
                iter.remove();
            }
        }
        
        workingOn = null;
    }
    
    /**
     * Wait's for all inhabitants being watched to be done, giving each up
     * to timeout/unit's to be done before giving up throwing a {@link TimeoutException}.
     */
    public synchronized void waitForDone(long timeout, TimeUnit unit) throws ExecutionException, TimeoutException, InterruptedException {
        if (null != watches) {
            Iterator<AsyncPostConstruct> iter = watches.iterator();
            while (iter.hasNext()) {
                workingOn = iter.next();
                if (workingOn.waitForDone(timeout, unit)) {
                    iter.remove();
                } else {
                    throw new TimeoutException("timeout waiting for " + workingOn);
                }
            }
        }
        
        workingOn = null;
    }
    
    public synchronized int getWatches() {
        return (null == watches) ? 0 : watches.size();
    }
    
    /**
     * A non-blocking call that returns true when we are done waiting.
     */
    public synchronized boolean isDone() {
        if (null == watches) {
            return true;
        }
        
        Iterator<AsyncPostConstruct> iter = watches.iterator();
        while (iter.hasNext()) {
            workingOn = iter.next();
            if (workingOn.isDone()) {
                iter.remove();
            }
        }

        workingOn = null;
        
        return watches.isEmpty();
    }
    
    /**
     * Returns the last Inhabitant that was working on, provided that we are not in a "done" state.
     */
    public synchronized Inhabitant<?> getLastInhabitantWorkingOn() {
        return AsyncInhabitant.class.isInstance(workingOn) ? AsyncInhabitant.class.cast(workingOn).inhabitant : null;
    }
    
    
    private static class AsyncFuture implements AsyncPostConstruct {
        private final Future<?> future;
        
        private AsyncFuture(Future<?> future) {
            this.future = future;
        }
        
        @Override
        public void postConstruct() {
        }

        @Override
        public boolean isDone() {
            return future.isDone();
        }

        @Override
        public void waitForDone() throws ExecutionException, InterruptedException{
            future.get();
        }

        @Override
        public boolean waitForDone(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException {
            try {
                future.get(timeout, unit);
                return true;
            } catch (TimeoutException e) {
                return false;
            }
        }
    }

    
    private static class AsyncInhabitant implements AsyncPostConstruct {
        private final Inhabitant<?> inhabitant;
        private final AsyncPostConstruct service;

        private AsyncInhabitant(Inhabitant<?> inhabitant) {
            this.inhabitant = inhabitant;
            this.service = (AsyncPostConstruct) inhabitant.get();
        }
        
        @Override
        public void postConstruct() {
        }

        @Override
        public boolean isDone() {
            return service.isDone();
        }

        @Override
        public void waitForDone() throws ExecutionException, TimeoutException, InterruptedException {
            service.waitForDone();
        }

        @Override
        public boolean waitForDone(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException {
            return service.waitForDone(timeout, unit);
        }
    }

}
