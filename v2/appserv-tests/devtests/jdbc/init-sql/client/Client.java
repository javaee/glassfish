package com.sun.s1asdev.jdbc.initsql.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;
import javax.sql.DataSource;
import com.sun.s1asdev.jdbc.initsql.ejb.SimpleSessionHome;
import com.sun.s1asdev.jdbc.initsql.ejb.SimpleSession;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
   
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter();
    private static String testSuite = "initsql-test";

    private static InitialContext ic;
    public static void main(String[] args)
        throws Exception {
        
        try {
	    ic = new InitialContext();
	} catch(NamingException ex) {
	    ex.printStackTrace();
	}

        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
	SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);
	stat.addDescription("Running initsql testSuite ");
        SimpleSession simpleSession = simpleSessionHome.create();

        if (args != null && args.length > 0) {
            String param = args[0];

            switch (Integer.parseInt(param)) {
                case 1: {
                    if (simpleSession.test1(false)) { //Case sensitive test
                        stat.addStatus(testSuite + "test-1 ", stat.PASS);
                    } else {
                        stat.addStatus(testSuite + "test-1 ", stat.FAIL);
                    }
                    break;
                }
                case 2: {
                    if (simpleSession.test1(true)) { //Case insensitivity test
                        stat.addStatus(testSuite + "test-2 ", stat.PASS);
                    } else {
                        stat.addStatus(testSuite + "test-2 ", stat.FAIL);
                    }
                    break;
                }
	    }
	}
        
        stat.printSummary();
    }
}
