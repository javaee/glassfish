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

package org.glassfish.hk2.runlevel;

import java.util.concurrent.Executor;

import org.jvnet.hk2.annotations.Contract;

/**
 * A RunLevelContoller controls the current state of
 * services registered in the {@link RunLevel} scope.  All
 * services annotated with a {@link RunLevel} equal to
 * or less than the current level of the system will be
 * started.  All services annotated with a {@link RunLevel}
 * higher than the current level of the system will not
 * be started.  This service can be used to change the
 * current level of the system.
 * <p>
 * Whether or not separate threads are used by the RunLevelController
 * is a policy set by the caller.  By default the RunLevelController
 * will use as many threads as there are services to be started at
 * a particular level.  So if your system has possibly hundreds of
 * services at some level, you will probably want to set your maximum
 * number of threads to some reasonable number.  You can also change
 * your threading policy to USE_NO_THREADS, in which case the
 * RunLevelController will not spawn any threads at all, but will
 * instead use the thread of the caller to perform all work.  In this
 * mode the Async API will throw an exception.
 * <p>
 * The RunLevelController starts at level -2.  The reasoning behind this
 * is to allow two "immediate" levels.  The first thing a system might do
 * is proceed to level 0 (running all services at level -1 and 0).
 * Thereafter the system may go up and down in levels, never going below
 * zero.  Note this is only a convention, and individual systems can choose
 * other meanings for the levels -1 and 0.
 *
 * @author jtrent, tbeerbower, jwells
 */
@Contract
public interface RunLevelController {
    
    /**
     * Causes this RunLevelController to move to the specified run level for
     * all {@link RunLevel} instances, orchestrating the appropriate
     * lifecycle events.
     * <p>
     * If the run level specified is the same as the current run level then
     * the RunLevelController may return immediately
     *
     * @param runLevel  the run level to move to
     * @return The future that can be used to wait for this object
     * @throws CurrentlyRunningException if there is currently a job running
     * this exception will be thrown with the currently running job
     * @throws IllegalStateException if this method is called when the
     *   USE_NO_THREADS policy is in effect
     */
    public RunLevelFuture proceedToAsync(int runLevel)
            throws CurrentlyRunningException, IllegalStateException;
    
    /**
     * This method will move to the given run level synchronously as per
     * {@link RunLevelController#proceedToAsync(int)}.
     * 
     * @param runLevel The level that should be proceeded to
     * @throws CurrentlyRunningException
     */
    public void proceedTo(int runLevel) throws CurrentlyRunningException;
    
    /**
     * This method will return the current proceedTo that the RunLevelController
     * is working on, or it will return null if the controller is not currently
     * moving up or down
     * 
     * @return the current job the run level controller is working on or null if
     * the system is not currently in flight
     */
    public RunLevelFuture getCurrentProceeding();

    /**
     * If there is a current procedure in process this method will get it
     * and cancel it
     */
    public void cancel();

    /**
     * The current run level state.  This represents the last run level
     * successfully achieved by the underlying RunLevelController responsible
     * for this scope.
     *
     * @return the current run level, or null if no run level has been
     *         been achieved
     */
    public int getCurrentRunLevel();
    
    /**
     * This sets the maximum number of threads that the system
     * can create for creation and/or destruction of threads.
     * This number must be one or greater
     * 
     * @param maximumThreads The maximum number of threads that
     * can be used by the system for creation or destruction of
     * services
     */
    public void setMaximumUseableThreads(int maximumThreads);
    
    /**
     * Returns the current number of maximum useable threads
     * 
     * @return the current number of maximum useable threads
     */
    public int getMaximumUseableThreads();
    
    /**
     * Sets the threading policy that will be used by
     * this controller.  The values can be:<OL>
     * <LI>FULLY_THREADED: Use maximumUseableThreads to complete any task</LI>
     * <LI>USE_NO_THREADS: Never create a thread, use the callers thread always</LI>
     * </OL>
     * 
     * @param policy The policy that should be used by this controller
     */
    public void setThreadingPolicy(ThreadingPolicy policy);
    
    /**
     * Returns the threading policy currently being used by
     * this controller
     * 
     * @return The threading policy currently in use with this controller
     */
    public ThreadingPolicy getThreadingPolicy();
    
    /**
     * Sets the executor to use for the next job.  This
     * value is ignored if the thread policy is
     * USE_NO_THREADS
     * 
     * @param executor The executor to use for the
     * next job.  If null a default executor will
     * be used
     */
    public void setExecutor(Executor executor);
    
    /**
     * Gets the executor that will be used by the system
     * when executing tasks.  This value is not used
     * by the system if the thread policy is USE_NO_THREADS
     * 
     * @return The currently installed executor.  Will
     * not return null (the default executor implementation
     * will be returned if the user has not supplied an
     * executor)
     */
    public Executor getExecutor();
    
    /**
     * These are the policies for how the RunLevelController
     * will use threads
     * 
     * @author jwells
     *
     */
    public enum ThreadingPolicy {
        /**
         * The RunLevelController will use as many threads
         * as it needs (but controlled by the MaximumUseableThreads
         * value)
         */
        FULLY_THREADED,
        
        /**
         * The RunLevelController will use no threads at all.
         * The MaximumUsealbeThreads value will be ignored
         */
        USE_NO_THREADS
    }
}
