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
package org.glassfish.hk2.tests.operation.basic;

import java.lang.annotation.Annotation;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extras.ExtrasUtilities;
import org.glassfish.hk2.extras.operation.OperationHandle;
import org.glassfish.hk2.extras.operation.OperationManager;
import org.glassfish.hk2.tests.extras.internal.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class OperationsTest {
    private final static Annotation BASIC_OPERATION_ANNOTATION = new BasicOperationScopeImpl();
    
    private final static String ALICE_NM = "Alice";
    private final static byte[] ALICE_PW = { 1, 2 };
    private final static String BOB_NM = "Bob";
    private final static byte[] BOB_PW = { 3, 4 };
    
    private final static OperationUser ALICE = new OperationUser() {

        @Override
        public String getName() {
            return ALICE_NM;
        }

        @Override
        public byte[] getPassword() {
            return ALICE_PW;
        }
        
    };
    
    private final static OperationUser BOB = new OperationUser() {

        @Override
        public String getName() {
            return BOB_NM;
        }

        @Override
        public byte[] getPassword() {
            return BOB_PW;
        }
        
    };
    
    private static ServiceLocator createLocator(Class<?>... clazzes) {
        ServiceLocator locator = Utilities.getUniqueLocator(clazzes);
        ExtrasUtilities.enableOperations(locator);
        
        return locator;
    }
    
    /**
     * Tests that operations can be properly swapped on a single thread
     */
    @Test // @org.junit.Ignore
    public void testChangeOperationOnSameThread() {
        ServiceLocator locator = createLocator(BasicOperationScopeContext.class,
                OperationUserFactory.class, SingletonThatUsesOperationService.class);
        
        OperationManager operationManager = locator.getService(OperationManager.class);
        
        OperationHandle aliceOperation = operationManager.createOperation(BASIC_OPERATION_ANNOTATION);
        aliceOperation.setOperationData(ALICE);
        
        OperationHandle bobOperation = operationManager.createOperation(BASIC_OPERATION_ANNOTATION);
        bobOperation.setOperationData(BOB);
        
        SingletonThatUsesOperationService singleton = locator.getService(SingletonThatUsesOperationService.class);
        
        // Start ALICE operation
        aliceOperation.resume();
        
        Assert.assertEquals(ALICE_NM, singleton.getCurrentUser().getName());
        
        // suspend ALICE and start BOB
        aliceOperation.suspend();
        bobOperation.resume();
        
        Assert.assertEquals(BOB_NM, singleton.getCurrentUser().getName());
        
        // Clean up
        aliceOperation.closeOperation();
        bobOperation.closeOperation();
    }

}
