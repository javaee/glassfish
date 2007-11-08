/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package client;

import beans.*;
import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client   {

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    
    public static void main(String[] args) {
        Client client = new Client();
	System.out.println("Client: Args: ");
	for(int i = 0 ; i< args.length; i++) {
		System.out.println("Client: Args: " + args[i]);
	}
	Integer versionNumber = new Integer(args[0]);
        client.doTest(versionNumber.intValue());
    }
    
    public String doTest(int versionToTest) {
        stat.addDescription("This is to test redeployment of connectors "+
	             "contracts.");
        
        String res = "NOT RUN";
	debug("doTest() ENTER...");
        boolean pass = false;
        try {
		pass = checkResults(versionToTest);
		debug("Got expected results = " + pass);
		
		//do not continue if one test failed
		if (!pass) {
			res = "SOME TESTS FAILED";
			stat.addStatus("Redeploy Connector 1.5 test - ", stat.FAIL);
		} else {
			res  = "ALL TESTS PASSED";
			stat.addStatus("Redeploy Connector 1.5 test - " , stat.PASS);
		}

        } catch (Exception ex) {
            System.out.println("Redeploy connector test failed.");
            ex.printStackTrace();
            res = "TEST FAILED";
        }
        stat.printSummary("Redeploy Connector 1.5");

        debug("EXITING... STATUS = " + res);
        return res;
    }
    
    private boolean checkResults(int num) throws Exception {
	    debug("checkResult" + num);
	    debug("got initial context" + (new InitialContext()).toString());
        Object o = (new InitialContext()).lookup("MyVersionChecker");
	debug("got o" + o);
        VersionCheckerHome  home = (VersionCheckerHome) 
            PortableRemoteObject.narrow(o, VersionCheckerHome.class);
        debug("got home" + home);
	    VersionChecker checker = home.create();
	    debug("got o" + checker);
        //problem here!
	int result = checker.getVersion();
	debug("checkResult" + result);
        return result == num;
    }

    private void debug(String msg) {
        System.out.println("[Redeploy Connector CLIENT]:: --> " + msg);
    }
}

