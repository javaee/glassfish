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
package org.glassfish.hk2.extras.operation;

import java.util.List;

import org.jvnet.hk2.annotations.Contract;

/**
 * This handle is used to associate or dis-associate threads with
 * Operations.  It can also be used to close this Operation.
 * Every OperationHandle will be added as an HK2 service in
 * the Operations scope, and hence can be injected into other
 * HK2 services
 * 
 * @author jwells
 *
 */
@Contract
public interface OperationHandle {
    /**
     * Returns a unique identifier for this operation
     * 
     * @return A non-null unique identifier for this
     * operation
     */
    public OperationIdentifier getIdentifier();
    
    /**
     * Gets the current state of this operation
     * 
     * @return
     */
    public OperationState getState();
    
    /**
     * Gets a list of threads upon which this Operation is active
     * 
     * @return
     */
    public List<Long> getActiveThreads();
    
    /**
     * Suspends this operation on the given thread id.  If this Operation
     * is not associated with the given threadId this method does nothing
     * 
     * @param threadId The thread on which to suspend this operation
     */
    public void suspend(long threadId);
    
    /**
     * Suspends this operation on the current thread.    If this Operation
     * is not associated with the current threadId this method does nothing
     */
    public void suspend();
    
    /**
     * Resumes this operation on the given thread id.  If this Operation
     * is already associated with the given threadId this method does
     * nothing
     * 
     * @param threadId The thread on which to resume this operation
     * @throws IllegalStateException if the Operation is closed or
     * if the given thread is associated with a different Operation
     * of the same type
     */
    public void resume(long threadId) throws IllegalStateException;
    
    /**
     * Resumes this operation on the current thread.  If this Operation
     * is already associated with the current thread this method does
     * nothing
     * 
     * @throws IllegalStateException if the Operation is closed or
     * if the current thread is associated with a different Operation
     * of the same type
     */
    public void resume() throws IllegalStateException;
    
    /**
     * suspends this Operation on all threads where it is associated
     * and closes the operation.  All resume calls on this handle after
     * this is called will throw IllegalStateException.  If this handle
     * is already closed this method does nothing
     */
    public void closeOperation();
    
    /**
     * Gets arbitrary Operation data to be associated
     * with this Operation
     * 
     * @return Arbitrary (possibly null) data that
     * is associated with this Operation
     */
    public Object getOperationData();
    
    /**
     * Sets arbitrary Operation data to be associated
     * with this Operation
     * 
     * @param data (possibly null) data that
     * is associated with this Operation
     */
    public void setOperationData(Object data);

}
