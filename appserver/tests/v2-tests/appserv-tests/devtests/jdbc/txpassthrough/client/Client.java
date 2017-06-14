package com.sun.s1asdev.jdbc.txpassthrough.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.txpassthrough.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    public static void main(String[] args)
        throws Exception {

 	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "TxPassThrough ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/NoTxConnTestEJB");
	NoTxConnTestHome home = (NoTxConnTestHome)
            javax.rmi.PortableRemoteObject.narrow(objRef, NoTxConnTestHome.class);

        NoTxConnTest bean = home.create();
	stat.addDescription("TX Pass Through tests ");

        if ( bean.test1() ) {
	    stat.addStatus(testSuite+" test1 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+" test1 : ", stat.FAIL);
	}
	stat.printSummary();
    
        
    }
}
