/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package client;

import beans.*;
import connector.*;
import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client   {

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    
    public Client (String[] args) {
        //super(args);
    }
    
    public static void main(String[] args) {
        Client client = new Client(args);
        client.doTest();
    }
    
    public String doTest() {
        stat.addDescription("This is to test connector 1.5 "+
	             "contracts.");
        
        String res = "NOT RUN";
	debug("doTest() ENTER...");
        boolean pass = false;
        try {
            res  = "ALL TESTS PASSED";
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
                        stat.addStatus("ID Connector 1.5 test - " + testCount, stat.FAIL);
                    } else {
                        stat.addStatus("ID Connector 1.5 test - " + testCount, stat.PASS);
		    }
                } else {
                    break;
                }
		testCount ++;
            }

        } catch (Exception ex) {
            System.out.println("Importing transaction test failed.");
            ex.printStackTrace();
            res = "TEST FAILED";
        }
        stat.printSummary("annotation-connector15ID");

        
        debug("EXITING... STATUS = " + res);
        return res;
    }
    
    private boolean checkResults(int num) throws Exception {
        Object o = (new InitialContext()).lookup("MyMessageChecker");
        MessageCheckerHome  home = (MessageCheckerHome) 
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        int result = checker.getMessageCount();
        return result == num;
    }

    private boolean done() throws Exception {
        Object o = (new InitialContext()).lookup("MyMessageChecker");
        MessageCheckerHome  home = (MessageCheckerHome) 
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        return checker.done();
    }

    private int expectedResults() throws Exception {
        Object o = (new InitialContext()).lookup("MyMessageChecker");
        MessageCheckerHome  home = (MessageCheckerHome) 
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        return checker.expectedResults();
    }

    private void notifyAndWait() throws Exception {
        Object o = (new InitialContext()).lookup("MyMessageChecker");
        MessageCheckerHome  home = (MessageCheckerHome) 
            PortableRemoteObject.narrow(o, MessageCheckerHome.class);
        MessageChecker checker = home.create();
        checker.notifyAndWait();
    }


    private void debug(String msg) {
        System.out.println("[CLIENT]:: --> " + msg);
    }
}

