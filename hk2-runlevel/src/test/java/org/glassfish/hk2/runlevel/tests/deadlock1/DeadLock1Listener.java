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
package org.glassfish.hk2.runlevel.tests.deadlock1;

import org.glassfish.hk2.runlevel.ChangeableRunLevelFuture;
import org.glassfish.hk2.runlevel.ErrorInformation;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.glassfish.hk2.runlevel.RunLevelListener;
import org.jvnet.hk2.annotations.Service;

/**
 * @author jwells
 *
 */
@Service
public class DeadLock1Listener implements RunLevelListener {
    private boolean go = false;

    /* (non-Javadoc)
     * @see org.glassfish.hk2.runlevel.RunLevelListener#onCancelled(org.glassfish.hk2.runlevel.RunLevelFuture, int)
     */
    @Override
    public void onCancelled(RunLevelFuture controller, int levelAchieved) {

    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.runlevel.RunLevelListener#onError(org.glassfish.hk2.runlevel.RunLevelFuture, java.lang.Throwable)
     */
    @Override
    public void onError(RunLevelFuture currentJob, ErrorInformation info) {

    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.runlevel.RunLevelListener#onProgress(org.glassfish.hk2.runlevel.RunLevelFuture, int)
     */
    @Override
    public void onProgress(ChangeableRunLevelFuture currentJob, int levelAchieved) {
        if (levelAchieved != 1) return;
        
        OtherThreadCanceller otc = new OtherThreadCanceller(currentJob, this);
        
        Thread myThread = new Thread(otc);
        myThread.start();
        
        synchronized(this) {
            while (!go) {
                try {
                    this.wait();
                }
                catch (InterruptedException e) {
                    throw new AssertionError(e);
                }
            }
        }
    }
    
    public class OtherThreadCanceller implements Runnable {
        private final RunLevelFuture job;
        private final Object lock;
        
        private OtherThreadCanceller(RunLevelFuture job, Object lock) {
            this.job = job;
            this.lock = lock;
            
        }

        @Override
        public void run() {
            // If locks are held in onProgress this will block forever
            job.cancel(false);
            
            synchronized (lock) {
                go = true;
                lock.notifyAll();
            }
            
            
        }
        
    }

}
