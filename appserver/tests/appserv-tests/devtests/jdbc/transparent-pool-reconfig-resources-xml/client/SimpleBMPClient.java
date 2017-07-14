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

package com.sun.s1asdev.jdbc.transparent_pool_reconfig.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.transparent_pool_reconfig.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.transparent_pool_reconfig.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    static SimpleReporterAdapter stat = new SimpleReporterAdapter();
    static String testSuite = "transparent_pool_reconfig ";


    public static void main(String[] args)
            throws Exception {
        SimpleBMPClient sbc = new SimpleBMPClient();
        sbc.start();

        stat.printSummary();
    }


    public void start() {
        Executor exec = new Executor();
        exec.start();

        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            //
        }

        int totalClients = 10;
        Client clients[] = new Client[totalClients];
        for (int i = 0; i < totalClients; i++) {
            clients[i] = new Client("User", "APP_" + i, false);
        }

        for (int i = 0; i < totalClients; i++) {
            clients[i].start();
        }

        for (int i = 0; i < totalClients; i++) {
            try {
                clients[i].join();
            } catch (InterruptedException e) {
                //do nothing
            }
        }

        try {
            exec.join();
        } catch (Exception e) {

        }

        exec.testFailure();
        exec.reconfigure("User", "APP", false);
        exec.reconfigure("Password", "APP", false);
        try {
            Thread.currentThread().sleep(2000);
        } catch (Exception e) {

        }
        exec.testSuccess();
    }


    private class Client extends Thread {

        private String name;
        private String value;
        private boolean isAttribute;

        private SimpleBMP simpleBMP;


        public Client(String name, String value, boolean attribute) {
            this.name = name;
            this.value = value;
            this.isAttribute = attribute;

            try {
                InitialContext ic = new InitialContext();
                Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
                SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

                simpleBMP = simpleBMPHome.create();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            if (simpleBMP != null) {
                try {
                    System.out.println("Client : Name - " + name + " : Value - " + value);

                    if (isAttribute) {
                        simpleBMP.setAttribute(name, value);
                    } else {
                        simpleBMP.setProperty(name, value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void testFailure() {

        }
    }


    private class Executor extends Thread {

        SimpleBMP simpleBMP;

        public void run() {
            try {
                InitialContext ic = new InitialContext();
                Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
                SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

                simpleBMP = simpleBMPHome.create();
                stat.addDescription("JDBC Statement Timeout Tests");

                System.out.println("Client : calling acquireConnectionsTest()");

                if (simpleBMP.acquireConnectionsTest(false, 30000)) {
                    stat.addStatus(testSuite + " acquireConnectionsTest : ", SimpleReporterAdapter.PASS);
                } else {
                    stat.addStatus(testSuite + " acquireConnectionsTest : ", SimpleReporterAdapter.FAIL);
                }

                System.out.println("Client : completed acquireConnectionsTest()");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void reconfigure(String name, String value, boolean attribute) {
            try {
                if (attribute) {
                    simpleBMP.setAttribute(name, value);
                } else {
                    simpleBMP.setProperty(name, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void testFailure() {
            try {
                if (simpleBMP.acquireConnectionsTest(true, 0)) {
                    stat.addStatus(testSuite + " expect-failure-Test : ", SimpleReporterAdapter.PASS);
                } else {
                    stat.addStatus(testSuite + " expect-failure-Test : ", SimpleReporterAdapter.FAIL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void testSuccess() {
            try {
                if (simpleBMP.acquireConnectionsTest(false, 0)) {
                    stat.addStatus(testSuite + " expect-success-Test : ", SimpleReporterAdapter.PASS);
                } else {
                    stat.addStatus(testSuite + " expect-success-Test : ", SimpleReporterAdapter.FAIL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
