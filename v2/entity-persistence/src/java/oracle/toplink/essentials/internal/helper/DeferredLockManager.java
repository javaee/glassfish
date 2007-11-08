/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package oracle.toplink.essentials.internal.helper;

import java.util.*;

/**
 * INTERNAL:
 * <p>
 * <b>Purpose</b>: Be used for deadlock avoidance through allowing detection and resolution.
 *
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Keep track of all deferred locks of each thread.
 * <li> Keep track of all active locks of each thread..
 * <li> Maintain the depth of the each thread.
 * </ul>
 */
public class DeferredLockManager {
    protected Vector deferredLocks;
    protected Vector activeLocks;
    protected int threadDepth;
    protected boolean isThreadComplete;
    
    public static boolean SHOULD_USE_DEFERRED_LOCKS = true;

    /**
     * DeferredLockManager constructor comment.
     */
    public DeferredLockManager() {
        super();
        this.deferredLocks = new Vector(1);
        this.activeLocks = new Vector(1);
        this.threadDepth = 0;
        this.isThreadComplete = false;
    }

    /**
     * add a concurrency manager as active locks to the DLM
     */
    public void addActiveLock(Object manager) {
        getActiveLocks().addElement(manager);
    }

    /**
     * add a concurrency manager as deferred locks to the DLM
     */
    public void addDeferredLock(Object manager) {
        getDeferredLocks().addElement(manager);
    }

    /**
     * decrement the depth of the thread
     */
    public void decrementDepth() {
        threadDepth--;
    }

    /**
     * Return a set of the active locks from the DLM
     */
    public Vector getActiveLocks() {
        return activeLocks;
    }

    /**
     * Return a set of the deferred locks
     */
    public Vector getDeferredLocks() {
        return deferredLocks;
    }

    /**
     * Return the depth of the thread associated with the DLM, being used to release the lock
     */
    public int getThreadDepth() {
        return threadDepth;
    }

    /**
     * Return if there are any deferred locks.
     */
    public boolean hasDeferredLock() {
        return !getDeferredLocks().isEmpty();
    }

    /**
     * increment the depth of the thread
     */
    public void incrementDepth() {
        threadDepth++;
    }

    /**
     * Return if the thread is complete
     */
    public boolean isThreadComplete() {
        return isThreadComplete;
    }

    /**
     * Release the active lock on the DLM
     */
    public void releaseActiveLocksOnThread() {
        Vector activeLocks = getActiveLocks();
        if (!activeLocks.isEmpty()) {
            for (Enumeration activeLocksEnum = activeLocks.elements();
                     activeLocksEnum.hasMoreElements();) {
                ConcurrencyManager manager = (ConcurrencyManager)activeLocksEnum.nextElement();
                manager.release();
            }
        }
        setIsThreadComplete(true);
    }

    /**
     * set a set of the active locks to the DLM
     */
    public void setActiveLocks(Vector activeLocks) {
        this.activeLocks = activeLocks;
    }

    /**
     * set a set of the deferred locks to the DLM
     */
    public void setDeferredLocks(Vector deferredLocks) {
        this.deferredLocks = deferredLocks;
    }

    /**
     * set if the thread is complete in the given DLM
     */
    public void setIsThreadComplete(boolean isThreadComplete) {
        this.isThreadComplete = isThreadComplete;
    }
}
