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
        stat.addDescription("This is to test connector ThreadPool "+
	             "contracts.");
        
        String res = "NOT RUN";
	debug("Starting the thread pool test=> Please wait...");
        boolean pass = false;
        try {
            res  = " TEST PASSED";
            test();
            stat.addStatus(" Connector ThreadPool test " , stat.PASS);
        } catch (Exception ex) {
            System.out.println("Thread pool test failed.");
            ex.printStackTrace();
            res = "TEST FAILED";
            stat.addStatus(" Connector ThreadPool test " , stat.FAIL);
        }

        stat.printSummary("Connector-ThreadPool");

        
        debug("EXITING... STATUS = " + res);
        return res;
    }
    
    private void test() throws Exception {
        Object o = (new InitialContext()).lookup("WorkTest");
        WorkTestHome  home = (WorkTestHome) 
            PortableRemoteObject.narrow(o, WorkTestHome.class);
        WorkTest wt = home.create();
        wt.executeTest();
    }

    private void debug(String msg) {
        System.out.println("[CLIENT]:: --> " + msg);
    }
}

