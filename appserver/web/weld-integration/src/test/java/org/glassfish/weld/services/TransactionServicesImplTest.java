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
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
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

package org.glassfish.weld.services;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.Test;

import static junit.framework.Assert.*;
import static org.easymock.EasyMock.*;

import static javax.transaction.Status.*;


import org.easymock.EasyMockSupport;

public class TransactionServicesImplTest {
  @Test
  public void testisTransactionActive() throws Exception {
    EasyMockSupport mockSupport = new EasyMockSupport();
    ServiceLocator serviceLocator = mockSupport.createMock( ServiceLocator.class );
    JavaEETransactionManager transactionManager = mockSupport.createMock( JavaEETransactionManager.class );

    doTestIsTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_ACTIVE );

    doTestIsTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_MARKED_ROLLBACK );

    doTestIsTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_PREPARED );

    doTestIsTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_UNKNOWN );

    doTestIsTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_PREPARING );

    doTestIsTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_COMMITTING );

    doTestIsTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_ROLLING_BACK );

    doTestIsNotTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_COMMITTED );

    doTestIsNotTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_ROLLEDBACK );

    doTestIsNotTransactionActive( mockSupport,
                                          serviceLocator,
                                          transactionManager,
                                          STATUS_NO_TRANSACTION );
  }

  private void doTestIsTransactionActive( EasyMockSupport mockSupport,
                                          ServiceLocator serviceLocator,
                                          JavaEETransactionManager transactionManager,
                                          int expectedStatus ) throws Exception {

    expect( serviceLocator.getService(JavaEETransactionManager.class) ).andReturn( transactionManager );
    expect( transactionManager.getStatus() ).andReturn( expectedStatus );
    mockSupport.replayAll();

    TransactionServicesImpl transactionServices = new TransactionServicesImpl( serviceLocator );
    assertTrue( transactionServices.isTransactionActive() );

    mockSupport.verifyAll();
    mockSupport.resetAll();
  }

  private void doTestIsNotTransactionActive( EasyMockSupport mockSupport,
                                          ServiceLocator serviceLocator,
                                          JavaEETransactionManager transactionManager,
                                          int expectedStatus ) throws Exception {

    expect( serviceLocator.getService(JavaEETransactionManager.class) ).andReturn( transactionManager );
    expect( transactionManager.getStatus() ).andReturn( expectedStatus );
    mockSupport.replayAll();

    TransactionServicesImpl transactionServices = new TransactionServicesImpl( serviceLocator );
    assertFalse( transactionServices.isTransactionActive() );

    mockSupport.verifyAll();
    mockSupport.resetAll();
  }
}
