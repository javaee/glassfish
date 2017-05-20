/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at LICENSE.txt.
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

package com.sun.s1asdev.ejb.mdb.msgbean;

import javax.ejb.MessageDrivenContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

/**
 * Tests matrix of operations callable from the different
 * methods in a message-driven bean.  
 */
public class OperationTest {

    private static final boolean debug = false;

    // OPERATION TYPES
    public static final int ENV_ACCESS = 0;
    public static final int USER_TX_ACCESS = 1;
    public static final int GET_CALLER_PRINCIPAL = 2;
    public static final int GET_ROLLBACK_ONLY = 3;
    public static final int SET_ROLLBACK_ONLY = 4;
    public static final int IS_CALLER_IN_ROLE = 5;
    public static final int GET_EJB_HOME = 6;
    public static final int USER_TX_BEGIN = 7;
    public static final int USER_TX_COMMIT = 8; 
    public static final int USER_TX_ROLLBACK = 9;
    public static final int NUM_OPERATIONS = 10;
    
    // OPERATION SETS (Table 9, Section 13.5.1 EJB 2.0)
    private static boolean[][] OPERATION_SETS = 
    {
        { false, false, false, false, false,  false,  false, false, false, false }, 
        { true,  false, false, false, false,  false,  false, false, false, false },
        { true,  true,  false, false, false,  false,  false, true,  true,  true  },
        { true,  false, false, true,  true,   false,  false, false, false, false },
        { true,  true,  false, false, false,  false,  false, true,  true,  true  } 
    };

    // Transaction types
    public static final int CMT = 0;
    public static final int BMT = 1;

    // Method types
    public static final int CTOR = 0;
    public static final int SET_CONTEXT = 1;
    public static final int EJB_CREATE  = 2;
    public static final int EJB_REMOVE = 3;
    public static final int ON_MESSAGE = 4;
   
    // 
    private static final int OPERATION_SET_MATRIX[][] =
    {
        { 0, 1, 1, 1, 3 },
        { 0, 1, 2, 2, 4 }
    };

    // Might not be available from some methods;
    private MessageDrivenContext mdc = null;

    public OperationTest() {
    }

    public void doTest(int txType, int methodType, MessageDrivenContext mdc) {
        boolean[] operationSet = OPERATION_SETS[ OPERATION_SET_MATRIX[txType][methodType] ];
        this.mdc = mdc;

        boolean[] testResults = new boolean[NUM_OPERATIONS];

        for(int operation = 0; operation < NUM_OPERATIONS; operation++) {            
            boolean result = false;
            try {
                switch(operation) {
                    case ENV_ACCESS : 
                        result = doEnvAccess(); break;
                    case USER_TX_ACCESS :
                        result = doUserTxAccess(); break;
                    case GET_CALLER_PRINCIPAL :
                        result = doGetCallerPrincipal(); break;
                    case GET_ROLLBACK_ONLY :
                        result = doGetRollbackOnly(); break;
                    case SET_ROLLBACK_ONLY :
                        result = doSetRollbackOnly(); break;
                    case IS_CALLER_IN_ROLE :
                        result = doIsCallerInRole(); break;
                    case GET_EJB_HOME :
                        result = doGetEjbHome(); break;
                    case USER_TX_BEGIN :
                        result = doUserTxBegin(); break;
                    case USER_TX_COMMIT :
                        result = doUserTxCommit(); break;
                    case USER_TX_ROLLBACK :
                        result = doUserTxRollback(); break;
                    default :
                        throw new RuntimeException("Unknown operation " + operation);
                }
            }
            catch(Throwable t) {
                if( debug ) {
                    t.printStackTrace();
                }
                result = false;
            }

            if( result == operationSet[operation] ) {
                testResults[operation] = true;
            }
            else {
                testResults[operation] = false;
                    System.out.println("tx : " + txType + " , mt : " + methodType + " , op : " + operation);
                    System.out.println("Expected result : " + operationSet[operation]);
                    System.out.println("Actual result   : " + result);

            }
        }        
    }

    private boolean doEnvAccess() throws Throwable {
        Context context = new InitialContext();
        Context env = (Context) context.lookup("java:comp/env");
        String  envVar = (String) context.lookup("java:comp/env/EnvVar");
        return true;
    }    

    private boolean doUserTxAccess() throws Throwable {
        UserTransaction userTx = mdc.getUserTransaction();
        return true;
    }

    private boolean doGetCallerPrincipal() throws Throwable {
        mdc.getCallerPrincipal();
        return true;
    }
 
    private boolean doGetRollbackOnly() throws Throwable {
        mdc.getRollbackOnly();
        return true;
    }

    private boolean doSetRollbackOnly() throws Throwable {
        mdc.setRollbackOnly();
        return true;
    }

    private boolean doIsCallerInRole() throws Throwable {
        mdc.isCallerInRole("rolename");
        return true;
    }

    private boolean doGetEjbHome() throws Throwable {
        mdc.getEJBHome();
        return true;
    }

    private boolean doUserTxBegin() throws Throwable {
        Context context = new InitialContext();
        UserTransaction userTx = mdc.getUserTransaction();
        userTx.begin();
        return true;
    }

    private boolean doUserTxCommit() throws Throwable {
        Context context = new InitialContext();
        UserTransaction userTx = mdc.getUserTransaction();
        userTx.commit();
        return true;
    }

    private boolean doUserTxRollback() throws Throwable {
        Context context = new InitialContext();
        UserTransaction userTx = mdc.getUserTransaction();
        userTx.begin();
        userTx.rollback();
        return true;
    }    

}
