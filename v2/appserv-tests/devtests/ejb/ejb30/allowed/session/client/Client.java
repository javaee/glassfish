package com.sun.ejb.devtest.client;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.ejb.EJB;
import com.sun.ejb.devtest.Sless;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-allowed-session");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-allowed-sessionID");
    }  
    
    public Client (String[] args) {
    }
    
    private static @EJB Sless sless;


    public void doTest() {
        try {
	    sless.sayHello();
            stat.addStatus("local setup", stat.PASS);
	} catch (Exception ex) {
            stat.addStatus("local setup", stat.FAIL);
        }

	boolean result = false;

        try {
	    result = sless.lookupUserTransactionFromBMTBean();
	} catch (Exception ex) {
            stat.addStatus("local BMTOp_Ex", stat.FAIL);
	}
        stat.addStatus("local BMTUserTx",
		(result ? stat.PASS : stat.FAIL));

        try {
	    result = sless.lookupUserTransactionFromCMTBean();
	} catch (Exception ex) {
            stat.addStatus("local CMTOp_Ex", stat.FAIL);
	}
        stat.addStatus("local CMTUserTx",
		(result ? stat.PASS : stat.FAIL));

    }

}

