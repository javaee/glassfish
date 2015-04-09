/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.extras.operation.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extras.operation.OperationHandle;
import org.glassfish.hk2.extras.operation.OperationIdentifier;
import org.glassfish.hk2.extras.operation.OperationState;

/**
 * @author jwells
 *
 */
public class OperationHandleImpl implements OperationHandle {
    private final SingleOperationManager parent;
    private final OperationIdentifier identifier;
    private final Object operationLock;
    private OperationState state;
    private final HashSet<Long> activeThreads = new HashSet<Long>();
    
    // Not controlled by operationLock
    private Object userData;
    
    /* package */ OperationHandleImpl(
            SingleOperationManager parent,
            OperationIdentifier identifier,
            Object operationLock,
            ServiceLocator locator) {
        this.parent = parent;
        this.identifier = identifier;
        this.operationLock = operationLock;
        this.state = OperationState.SUSPENDED;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationHandle#getIdentifier()
     */
    @Override
    public OperationIdentifier getIdentifier() {
        return identifier;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationHandle#getState()
     */
    @Override
    public OperationState getState() {
        synchronized (operationLock) {
            return state;
        }
    }
    
    private void checkState() {
        synchronized (operationLock) {
            if (OperationState.CLOSED.equals(state)) {
                throw new IllegalStateException(this + " is closed");
            }
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationHandle#getActiveThreads()
     */
    @Override
    public Set<Long> getActiveThreads() {
        synchronized (operationLock) {
            checkState();
            
            return Collections.unmodifiableSet(activeThreads);
        }
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationHandle#suspend(long)
     */
    @Override
    public void suspend(long threadId) {
        synchronized (operationLock) {
            if (OperationState.CLOSED.equals(state)) return;
            
            parent.disassociateThread(threadId);
            
            if (activeThreads.remove(threadId)) {
                if (activeThreads.isEmpty()) {
                    state = OperationState.SUSPENDED;
                }
            }
        }
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationHandle#suspend()
     */
    @Override
    public void suspend() {
        suspend(Thread.currentThread().getId());
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationHandle#resume(long)
     */
    @Override
    public void resume(long threadId) throws IllegalStateException {
        synchronized (operationLock) {
            checkState();
            
            if (activeThreads.contains(threadId)) return;
            
            // Check parent
            OperationHandleImpl existing = parent.getCurrentOperationOnThisThread(threadId);
            if (existing != null) {
                throw new IllegalStateException("The operation " + existing + " is active on " + threadId);
            }
            
            if (activeThreads.isEmpty()) {
                state = OperationState.ACTIVE;
            }
            activeThreads.add(threadId);
            
            parent.associateWithThread(threadId, this);
        }
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationHandle#resume()
     */
    @Override
    public void resume() throws IllegalStateException {
        resume(Thread.currentThread().getId());
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationHandle#closeOperation()
     */
    @Override
    public void closeOperation() {
        synchronized (operationLock) {
            for (long threadId : activeThreads) {
                parent.disassociateThread(threadId);
            }
            
            activeThreads.clear();
            state = OperationState.CLOSED;
            parent.closeOperation(this);
        }
        
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationHandle#getOperationData()
     */
    @Override
    public synchronized Object getOperationData() {
        return userData;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationHandle#setOperationData(java.lang.Object)
     */
    @Override
    public synchronized void setOperationData(Object data) {
        userData = data;
    }
    
    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof OperationHandleImpl)) return false;
        
        return identifier.equals(((OperationHandleImpl) o).identifier);
    }
    
    @Override
    public String toString() {
        return "OperationHandleImpl(" + identifier + "," + System.identityHashCode(this) + ")";
    }

}
