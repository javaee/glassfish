/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.transaction.txlao.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1peqe.transaction.txlao.ejb.beanA.*;

import java.rmi.RemoteException;


public class Client {

    private TxRemoteHomeA home = null;
    private SimpleReporterAdapter status =
        new SimpleReporterAdapter("appserv-tests");

    public Client() {
    }

    public static void main(String[] args) { 
        System.out.println("\nStarting Txglobal Test Suite");  
        Client client = new Client(); 

        // initialize the context and home object
        client.setup();

        // run the tests
        client.runTestClient();   
    }

    public void setup() {
        Class homeClass = TxRemoteHomeA.class;
        try {
            // Initialize the Context
            Context context = new InitialContext();
            System.out.println("Context Initialized...");

            // Create Home object
            java.lang.Object obj = context.lookup("java:comp/env/ejb/TxBeanA");
            home = (TxRemoteHomeA) PortableRemoteObject.narrow(obj, homeClass);
            System.out.println("Home Object Initialized...");
        } catch (Throwable ex) {
            System.out.println("Exception in setup: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void runTestClient() {
        try{
            status.addDescription("This is to test the global transaction!");
            firstXAJDBCSecondNonXAJDBC();
            firstNonXAJDBCSecondXAJDBC();
            firstXAJDBCSecondXAJDBC();
            firstXAJMSSecondNonXAJDBC() ;
            firstNonXAJDBCOnly()  ;
            rollbackXAJDBCNonXAJDBC();
            rollbackNonXAJDBCXAJDBC();
            cleanup();
            status.printSummary("txglobalID");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }
    public void firstXAJDBCSecondNonXAJDBC() {
       try {
            System.out.println("Execute BeanA::firstXAJDBCSecondNonXAJDBC");

            TxRemoteA beanA = home.create();
            boolean result = beanA.firstXAJDBCSecondNonXAJDBC();

            if (result) {
                status.addStatus("txlao firstXAJDBCSecondNonXAJDBC: ", status.PASS);
            } else {
                status.addStatus("txlao firstXAJDBCSecondNonXAJDBC: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao firstXAJDBCSecondNonXAJDBC: ", status.FAIL);
            System.out.println("Exception in firstXAJDBCSecondNonXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
    }
    public void firstNonXAJDBCSecondXAJDBC(){
      try {
            System.out.println("Execute BeanA::firstNonXAJDBCSecondXAJDBC");

            TxRemoteA beanA = home.create();
            boolean result = beanA.firstNonXAJDBCSecondXAJDBC();

            if (result) {
                status.addStatus("txlao firstNonXAJDBCSecondXAJDBC: ", status.PASS);
            } else {
                status.addStatus("txlao firstNonXAJDBCSecondXAJDBC: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao firstNonXAJDBCSecondXAJDBC: ", status.FAIL);
            System.out.println("Exception in firstNonXAJDBCSecondXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
    }
    public void firstXAJDBCSecondXAJDBC() {
       try {
            System.out.println("Execute BeanA::firstXAJDBCSecondXAJDBC");

            TxRemoteA beanA = home.create();
            boolean result = beanA.firstXAJDBCSecondXAJDBC();

            if (result) {
                status.addStatus("txlao firstXAJDBCSecondXAJDBC: ", status.PASS);
            } else {
                status.addStatus("txlao firstXAJDBCSecondXAJDBC: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao firstXAJDBCSecondXAJDBC: ", status.FAIL);
            System.out.println("Exception in firstXAJDBCSecondXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
    }
    public void firstNonXAJDBCSecondNonXAJDBC() {
        try {
            System.out.println("Execute BeanA::firstNonXAJDBCSecondNonXAJDBC");

            TxRemoteA beanA = home.create();
            boolean result = beanA.firstNonXAJDBCSecondNonXAJDBC();

            if (result) {
                status.addStatus("txlao firstNonXAJDBCSecondNonXAJDBC: ", status.PASS);
            } else {
                status.addStatus("txlao firstNonXAJDBCSecondNonXAJDBC: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao firstNonXAJDBCSecondNonXAJDBC: ", status.FAIL);
            System.out.println("Exception in firstNonXAJDBCSecondNonXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
    }
    public void firstXAJMSSecondNonXAJDBC() {
        try {
            System.out.println("Execute BeanA::firstXAJMSSecondNonXAJDBC");

            TxRemoteA beanA = home.create();
            boolean result = beanA.firstXAJMSSecondNonXAJDBC();

            if (result) {
                status.addStatus("txlao firstXAJMSSecondNonXAJDBC: ", status.PASS);
            } else {
                status.addStatus("txlao firstXAJMSSecondNonXAJDBC: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao firstXAJMSSecondNonXAJDBC: ", status.FAIL);
            System.out.println("Exception in firstXAJMSSecondNonXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
    }
     public void firstNonXAJDBCOnly() {
      try {
            System.out.println("Execute BeanA::firstNonXAJDBCOnly");

            TxRemoteA beanA = home.create();
            boolean result = beanA.firstNonXAJDBCOnly();

            if (result) {
                status.addStatus("txlao firstNonXAJDBCOnly: ", status.PASS);
            } else {
                status.addStatus("txlao firstNonXAJDBCOnly: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao firstNonXAJDBCOnly: ", status.FAIL);
            System.out.println("Exception in firstNonXAJDBCOnly: " + ex.toString());
            ex.printStackTrace();
        }
    }

     public void rollbackXAJDBCNonXAJDBC() {
        try {
            System.out.println("Execute BeanA::rollbackXAJDBCNonXAJDBC");

            TxRemoteA beanA = home.create();
            boolean result = beanA.rollbackXAJDBCNonXAJDBC();

            if (result) {
                status.addStatus("txlao rollbackXAJDBCNonXAJDBC: ", status.PASS);
            } else {
                status.addStatus("txlao rollbackXAJDBCNonXAJDBC: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao rollbackXAJDBCNonXAJDBC: ", status.FAIL);
            System.out.println("Exception in rollbackXAJDBCNonXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
     }

     public void rollbackNonXAJDBCXAJDBC() {
     try {
            System.out.println("Execute BeanA::rollbackNonXAJDBCXAJDBC");

            TxRemoteA beanA = home.create();
            boolean result = beanA.rollbackNonXAJDBCXAJDBC();

            if (result) {
                status.addStatus("txlao rollbackNonXAJDBCXAJDBC: ", status.PASS);
            } else {
                status.addStatus("txlao rollbackNonXAJDBCXAJDBC: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao rollbackNonXAJDBCXAJDBC: ", status.FAIL);
            System.out.println("Exception in rollbackNonXAJDBCXAJDBC: " + ex.toString());
            ex.printStackTrace();
        }
     }
     public void cleanup() {
      try {
            System.out.println("Execute BeanA::cleanup");

            TxRemoteA beanA = home.create();
            beanA.cleanup();

            beanA.remove();
        } catch (Exception ex) {
            System.out.println("Exception in cleanup: " + ex.toString());
            ex.printStackTrace();
        }
    }
    public void testTxCommit() {
        try {  
            System.out.println("Execute BeanA::testTxCommit");

            TxRemoteA beanA = home.create();
            boolean result = beanA.txCommit();

            if (result) {
                status.addStatus("txlao testTxCommit: ", status.PASS);
            } else {	 
                status.addStatus("txlao testTxCommit: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao testTxCommit: ", status.FAIL);
            System.out.println("Exception in testTxCommit: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void testTxRollback() {
        try {  
            System.out.println("Execute BeanA::testTxRollback");

            TxRemoteA beanA = home.create();
            boolean result = beanA.txRollback();

            if (result) {
                status.addStatus("txlao testTxRollback: ", status.PASS);
            } else {	 
                status.addStatus("txlao testTxRollback: ", status.FAIL);
            }

            beanA.remove();
        } catch (Exception ex) {
            status.addStatus("txlao testTxRollback: ", status.FAIL);
            System.out.println("Exception in testTxRollback: " + ex.toString());
            ex.printStackTrace();
        }
    }
}
