package com.sun.s1asdev.jdbc.simplemysql.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.simplemysql.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.simplemysql.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    public static void main(String[] args)
        throws Exception {

 	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "jdbcsimplemysql ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simplemysqlBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simplemysqlBMP = simplemysqlBMPHome.create();

        if ( simplemysqlBMP.test1(10) ) {
	    stat.addStatus(testSuite+"test1 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+"test1 : ", stat.FAIL);
	}

	if ( simplemysqlBMP.test2() ) {
	    stat.addStatus(testSuite+"test2 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+"test2 : ", stat.FAIL);
	}

	if ( simplemysqlBMP.test3() ) {
	    stat.addStatus(testSuite+"test3 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+"test3 : ", stat.FAIL);
	}

	System.out.println("jdbc simplemysql status: ");
	stat.printSummary();
    
        
    }
}
