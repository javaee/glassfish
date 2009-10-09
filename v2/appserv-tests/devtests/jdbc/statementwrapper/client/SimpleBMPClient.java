package com.sun.s1asdev.jdbc.statementwrapper.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.statementwrapper.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.statementwrapper.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    public static void main(String[] args)
        throws Exception {

 	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "statementWrapper ";

    InitialContext ic = new InitialContext();
    Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
    javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

    SimpleBMP simpleBMP = simpleBMPHome.create();
    stat.addDescription("JDBC Statement Wrapper Tests");

    if ( simpleBMP.statementTest() ) {
	    stat.addStatus(testSuite+" statementTest : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+" statementTest : ", stat.FAIL);
	}

	if ( simpleBMP.preparedStatementTest() ) {
	    stat.addStatus(testSuite+" preparedStatementTest : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+" preparedStatementTest : ", stat.FAIL);
	}

    if ( simpleBMP.callableStatementTest() ) {
        stat.addStatus(testSuite+" callableStatementTest : ", stat.PASS);
    } else {
        stat.addStatus(testSuite+" callableStatementTest : ", stat.FAIL);
    }

    if ( simpleBMP.metaDataTest() ) {
        stat.addStatus(testSuite+" metaDataTest : ", stat.PASS);
    } else {
        stat.addStatus(testSuite+" metaDataTest : ", stat.FAIL);
    }

    if ( simpleBMP.resultSetTest() ) {
        stat.addStatus(testSuite+" resultSetTest : ", stat.PASS);
    } else {
        stat.addStatus(testSuite+" resultSetTest : ", stat.FAIL);
    }

    stat.printSummary();
    }
}
