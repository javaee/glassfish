/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
            //    System.out.println("test count : " + testCount);
                testCount++;
            }
            
                System.out.println("test count : " + testCount);
            runAdminObjectTest(res, testCount++, "java:app/eis/testAdmin", true);
                System.out.println("test count : " + testCount);
            runAdminObjectTest(res, testCount++, "java:app/eis/testAdmin1", false);

            //any Default validations specified on the bean must work
            /*
                @Readme : setting a value that violates validation constraint
                Admin Object will set the 'intValue' on the RA bean, get a bean validator and validate the RA bean
            */

    //            System.out.println("test count : " + testCount);
            runRABeanTest(1000, res, testCount++,false);
            /*
                @Readme : setting a value that violates validation constraint
                Admin Object will set the 'intValue' on the RA bean, get a bean validator and validate the RA bean
            */
//                System.out.println("test count : " + testCount);
            runRABeanTest(5, res, testCount++,true);
  //              System.out.println("test count : " + testCount);

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

