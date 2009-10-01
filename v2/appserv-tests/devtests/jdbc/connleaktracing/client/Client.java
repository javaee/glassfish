package com.sun.s1asdev.jdbc.connectionleaktracing.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.connectionleaktracing.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.connectionleaktracing.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new
                SimpleReporterAdapter("appserv-tests");
        String testSuite = "connectionleaktracing";
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");

	for(int i=0; i<3; i++){
            SimpleBMPHome convalBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            SimpleBMP convalBMP = convalBMPHome.create();
	    if(!convalBMP.test1()){
		stat.addStatus("jdbc-connectionleakttracing : test ", stat.FAIL);
		break;
	    }
	    Thread.sleep(20000);
	}
	stat.addStatus("jdbc-connectionleakttracing : test ", stat.PASS);
        stat.printSummary("connection leak tracing tests");
    }
}
