package com.sun.s1asdev.jdbc.notxconn.test1.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.notxconn.test1.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    public static void main(String[] args)
        throws Exception {

 	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "jdbc-notxconn ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/NoTxConnTestEJB");
	NoTxConnTestHome home = (NoTxConnTestHome)
            javax.rmi.PortableRemoteObject.narrow(objRef, NoTxConnTestHome.class);

        NoTxConnTest bean = home.create();

        if ( bean.test1() ) {
	    stat.addStatus(testSuite+" test1 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+" test1 : ", stat.FAIL);
	}
        if ( bean.test2() ) {
	    stat.addStatus(testSuite+" test2 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+" test2 : ", stat.FAIL);
	}
	stat.printSummary();
    
        
    }
}
