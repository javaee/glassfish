package com.sun.s1asdev.jdbc.autocommit.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.autocommit.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.autocommit.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    SimpleReporterAdapter stat = new SimpleReporterAdapter();
    
    public static void main(String[] args)
        throws Exception {

        Client client = new Client();
	client.runTest();
    }
    
    public void runTest() throws Exception {
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
	stat.addDescription("Autocommit Test");

        if ( simpleBMP.test1() ) {
	    stat.addStatus(" autocommit test1 : ", stat.PASS);
	} else {
	    stat.addStatus(" autocommit test1 : ", stat.FAIL);
	}

        if ( simpleBMP.test2() ) {
	    stat.addStatus(" autocommit test2 : ", stat.PASS);
	} else {
	    stat.addStatus(" autocommit test2 : ", stat.FAIL);
	}

	System.out.println("jdbc autocommit status: ");
	stat.printSummary();
    }
}
