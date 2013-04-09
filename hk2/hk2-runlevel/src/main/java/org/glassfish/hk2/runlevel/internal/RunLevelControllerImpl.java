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

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.runlevel.CurrentlyRunningException;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

/**
 * This is the implementation of the RunLevelController
 * 
 * @author jwells
 */
@Service @ContractsProvided(RunLevelController.class)
public class RunLevelControllerImpl implements RunLevelController {
    @Inject
    private AsyncRunLevelContext context;
    
    
    @Override
    public void proceedTo(int runLevel) {
        RunLevelFuture future = context.proceedTo(runLevel);
        if (future == null) return;  // Happens if USE_NO_THREADS is true
        
        try {
            future.get();
        }
        catch (InterruptedException e) {
            throw new MultiException(e);
        }
        catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            
            throw new MultiException(cause);
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.runlevel.RunLevelController#proceedTo(int)
     */
    @Override
    public RunLevelFuture proceedToAsync(int runLevel)
            throws CurrentlyRunningException, IllegalStateException {
        if (context.getPolicy().equals(ThreadingPolicy.USE_NO_THREADS)) {
            throw new IllegalStateException("Cannot use proceedToAsync if the threading policy is USE_NO_THREADS");
        }
        
        return context.proceedTo(runLevel);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.runlevel.RunLevelController#getCurrentProceeding()
     */
    @Override
    public RunLevelFuture getCurrentProceeding() {
        return context.getCurrentFuture();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.runlevel.RunLevelController#cancel()
     */
    @Override
    public void cancel() {
        RunLevelFuture rlf = getCurrentProceeding();
        if (rlf == null) return;
        
        rlf.cancel(false);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.runlevel.RunLevelController#getCurrentRunLevel()
     */
    @Override
    public int getCurrentRunLevel() {
        return context.getCurrentLevel();
    }

    @Override
    public void setMaximumUseableThreads(int maximumThreads) {
        if (maximumThreads < 1) {
            throw new IllegalArgumentException("maximumThreads must be at least 1, but it is " +
                maximumThreads);
        }
        
        context.setMaximumThreads(maximumThreads);
    }

    @Override
    public int getMaximumUseableThreads() {
        return context.getMaximumThreads();
    }

    @Override
    public void setThreadingPolicy(ThreadingPolicy policy) {
        if (policy == null) throw new IllegalArgumentException();
        context.setPolicy(policy);
        
    }

    @Override
    public ThreadingPolicy getThreadingPolicy() {
        return context.getPolicy();
    }

}
