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

package com.sun.s1peqe.transaction.txglobal.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1peqe.transaction.txglobal.ejb.beanB.*;


public class Client {

    private TxRemoteHomeB home = null;
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
        Class homeClass = TxRemoteHomeB.class;
        try {
            // Initialize the Context
            Context context = new InitialContext();
            System.out.println("Context Initialized...");

            // Create Home object
            java.lang.Object obj = context.lookup("java:comp/env/ejb/TxBeanB");
            home = (TxRemoteHomeB) PortableRemoteObject.narrow(obj, homeClass);
            System.out.println("Home Object Initialized...");
        } catch (Throwable ex) {
            System.out.println("Exception in setup: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void runTestClient() {
        try{
            status.addDescription("This tests the commitStatus of two read only resources");
            test1();
            test2();
            status.printSummary("txglobalID");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void test1() {
        try {  
            System.out.println("Execute BeanB::test1");

            TxRemoteB beanB = home.create();
            beanB.test1();
            int result = beanB.getCommitStatus();

            if (result == 0) {
                status.addStatus("txglobal test1: ", status.PASS);
            } else {	 
                status.addStatus("txglobal test1: ", status.FAIL);
            }

            beanB.remove();
        } catch (Exception ex) {
            status.addStatus("txglobal test1: ", status.FAIL);
            System.out.println("Exception in test1: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void test2() {
        TxRemoteB beanB = null;
        try {  
            System.out.println("Execute BeanB::test2");

            beanB = home.create();
            beanB.test2();
            int result = beanB.getCommitStatus();

            if (result == 1) {
                status.addStatus("txglobal test2: ", status.PASS);
            } else {	 
                status.addStatus("txglobal test2: ", status.FAIL);
            }
            beanB.remove();
        } catch (Throwable ex) {
            status.addStatus("txglobal test2: ", status.FAIL);
            System.out.println("Exception in test2: " + ex.toString());
            ex.printStackTrace();
        }
    }

}
