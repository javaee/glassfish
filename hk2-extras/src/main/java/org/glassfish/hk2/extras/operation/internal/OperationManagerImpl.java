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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extras.operation.OperationHandle;
import org.glassfish.hk2.extras.operation.OperationManager;

/**
 * @author jwells
 *
 */
@Singleton
public class OperationManagerImpl implements OperationManager {
    private final HashMap<Class<? extends Annotation>, SingleOperationManager<?>> children =
            new HashMap<Class<? extends Annotation>, SingleOperationManager<?>>();
    
    @Inject
    private ServiceLocator locator;

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationManager#createOperation()
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Annotation> OperationHandle<T> createOperation(T scope) {
        SingleOperationManager<T> manager;
        synchronized (this) {
            manager = (SingleOperationManager<T>) children.get(scope.annotationType());
        
            if (manager == null) {
                manager = new SingleOperationManager<T>(this, scope, locator);
                children.put(scope.annotationType(), manager);
            }
        }
        
        return manager.createOperation();
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationManager#createAndStartOperation()
     */
    @Override
    public <T extends Annotation> OperationHandle<T> createAndStartOperation(T scope) {
        OperationHandle<T> retVal = createOperation(scope);
        retVal.resume();
        
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationManager#getCurrentOperations()
     */
    @Override
    public <T extends Annotation> Set<OperationHandle<T>> getCurrentOperations(T scope) {
        throw new AssertionError("getCurrentOperations not yet implemented");
    }
    
    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationManager#getCurrentOperation(java.lang.annotation.Annotation)
     */
    @Override
    public <T extends Annotation> OperationHandle<T> getCurrentOperation(T scope) {
        throw new AssertionError("getCurrentOperation not yet implemented");
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.extras.operation.OperationManager#shutdownAllOperations()
     */
    @Override
    public void shutdownAllOperations(Annotation scope) {
        throw new AssertionError("shutdownAllOperations not yet implemented");
        
    }

    

}
