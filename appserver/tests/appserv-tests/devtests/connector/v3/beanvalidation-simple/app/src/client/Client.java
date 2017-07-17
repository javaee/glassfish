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

package client;

import beans.*;

import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private String prefix = "";

    public Client(String[] args) {
        //super(args);
        if(args != null && args.length > 0){
            prefix = args[0];
        }
    }

    public static void main(String[] args) {
        Client client = new Client(args);
        client.doTest();
    }

    public String doTest() {
        stat.addDescription("This is to test Bean Validation in Resource Adapter artifacts ");

        String res = "NOT RUN";
        debug("doTest() ENTER...");
        boolean pass = false;
        try {
            res = "ALL TESTS PASSED";
            int testCount = 1;
            while (!done()) {

                notifyAndWait();
                if (!done()) {
                    debug("Running...");
                    pass = checkResults(expectedResults());
                    debug("Got expected results = " + pass);

                    //do not continue if one test failed
                    if (!pass) {
                        res = "SOME TESTS FAILED";
                        stat.addStatus(" " + prefix + "Connector-Bean-Validation Test - " + testCount, stat.FAIL);
                    } else {
                        stat.addStatus(" " + prefix + "Connector-Bean-Validation Test - "+ testCount, stat.PASS);
                    }
                } else {
                    break;
                }
                testCount++;
            }

            runAdminObjectTest(res, testCount++, "eis/testAdmin", true);
            runAdminObjectTest(res, testCount++, "eis/testAdmin1", false);

            //any Default validations specified on the bean must work
            /*
                @Readme : setting a value that violates validation constraint
                Admin Object will set the 'intValue' on the RA bean, get a bean validator and validate the RA bean
            */

            runRABeanTest(1000, res, testCount++,false);
            /*
                @Readme : setting a value that violates validation constraint
                Admin Object will set the 'intValue' on the RA bean, get a bean validator and validate the RA bean
            */
            runRABeanTest(5, res, testCount++,true);

        } catch (Exception ex) {
            System.out.println(" " + prefix + "Connector-Bean-Validation Test "+ " failed.");
            ex.printStackTrace();
            res = "TEST FAILED";
        }
        stat.printSummary(" " + prefix + "Connector-Bean-Validation Test ");


        debug("EXITING... STATUS = " + res);
        return res;
    }

    private void runRABeanTest(int intValue, String res, int testCount, boolean expectSuccess) throws Exception{
        boolean pass;
        pass = testRABean(intValue, expectSuccess);
        if (!pass) {
            res = "SOME TESTS FAILED";
            stat.addStatus(" " + prefix + "Connector-Bean-Validation Test - " + testCount, stat.FAIL);
        } else {
            stat.addStatus(" " + prefix + "Connector-Bean-Validation Test - " + testCount, stat.PASS);
        }
    }

    private boolean testRABean(int intValue, boolean expectSuccess) throws Exception{
        Object o = (new InitialContext()).lookup("MyMessageChecker");
        MessageCheckerHome home = (MessageCheckerHome)
                PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        boolean result = checker.testRA(intValue);
        return result == expectSuccess;
    }

    private void runAdminObjectTest(String res, int testCount, String jndiName, boolean expectLookupSuccess) throws Exception {
        boolean pass;
        pass = testAdminObject(jndiName, expectLookupSuccess);

        if (!pass) {
            res = "SOME TESTS FAILED";
            stat.addStatus(" " + prefix + "Connector-Bean-Validation Test - " + testCount, stat.FAIL);
        } else {
            stat.addStatus(" " + prefix + "Connector-Bean-Validation Test - " + testCount, stat.PASS);
        }
    }

    private boolean testAdminObject(String adminObjectName, boolean expectSuccessfulLookup) throws Exception {
        Object o = (new InitialContext()).lookup("MyMessageChecker");
        MessageCheckerHome home = (MessageCheckerHome)
                PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        return checker.testAdminObject(adminObjectName, expectSuccessfulLookup);
    }

    private boolean checkResults(int num) throws Exception {
        Object o = (new InitialContext()).lookup("MyMessageChecker");
        MessageCheckerHome home = (MessageCheckerHome)
                PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        int result = checker.getMessageCount();
        return result == num;
    }

    private boolean done() throws Exception {
        Object o = (new InitialContext()).lookup("MyMessageChecker");
        MessageCheckerHome home = (MessageCheckerHome)
                PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        return checker.done();
    }

    private int expectedResults() throws Exception {
        Object o = (new InitialContext()).lookup("MyMessageChecker");
        MessageCheckerHome home = (MessageCheckerHome)
                PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        return checker.expectedResults();
    }

    private void notifyAndWait() throws Exception {
        Object o = (new InitialContext()).lookup("MyMessageChecker");
        MessageCheckerHome home = (MessageCheckerHome)
                PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        checker.notifyAndWait();
    }


    private void debug(String msg) {
        System.out.println("[CLIENT]:: --> " + msg);
    }
}

