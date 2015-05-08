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

import java.lang.annotation.Annotation;
import java.util.Set;

import org.jvnet.hk2.annotations.Contract;

/**
 * @author jwells
 *
 */
@Contract
public interface OperationManager {
    /**
     * Creates an OperationHandle.  The returned
     * handle will not be associated with any threads.
     * The scope parameter is normally created with
     * {@link org.glassfish.hk2.api.AnnotationLiteral}
     * 
     * @param scope The scope annotation for this operation type
     * @return A non-null OperationHandle that can
     * be used to associate threads with the Operation
     */
    public <T extends Annotation> OperationHandle<T> createOperation(T scope);
    
    /**
     * Creates an OperationHandle that will be associated
     * with the thread calling this method.
     * The scope parameter is normally created with
     * {@link org.glassfish.hk2.api.AnnotationLiteral}
     * 
     * @param scope The scope annotation for this operation type
     * @return A non-null OperationHandle that can
     * be used to associate threads with the Operation
     * @throws IllegalStateException  if the current thread is
     * associated with a different Operation of the same type
     */
    public <T extends  Annotation> OperationHandle<T> createAndStartOperation(T scope);
    
    /**
     * Gets a set of all Operations that are in state
     * {@link OperationState#ACTIVE} or {@link OperationState#SUSPENDED}.
     * Operations that are in the {@link OperationState#CLOSED} state
     * are no longer tracked by the Manager.
     * The scope parameter is normally created with
     * {@link org.glassfish.hk2.api.AnnotationLiteral}
     * 
     * @param scope The scope annotation for this operation type
     * @return A non-null but possibly empty set of OperationHandles
     * that have not been closed
     */
    public <T extends Annotation> Set<OperationHandle<T>> getCurrentOperations(T scope);
    
    /**
     * Gets the current operation of scope type on the current thread.
     * The scope parameter is normally created with 
     * {@link org.glassfish.hk2.api.AnnotationLiteral}
     * 
     * @param scope The scope annotation for this operation type
     * @return The current operation of the given type on this thread.
     * May be null if there is no active operation on this thread of
     * this type
     */
    public <T extends Annotation> OperationHandle<T> getCurrentOperation(T scope);
    
    /**
     * This method will suspend all currently open operations on all threads and
     * then close them.  This will also remove all entities associated with
     * this operation type, including the OperationHandle associated with
     * this scope from the HK2 locator service registry.  Therefore this
     * mechanism of shutting down the operations should be used with care,
     * and would normally only be used when the Operation type can never
     * be used again.
     * <p>
     * The scope parameter is normally created with
     * {@link org.glassfish.hk2.api.AnnotationLiteral}
     * 
     * @param scope The scope annotation for this operation type
     */
    public void shutdownAllOperations(Annotation scope);
}
