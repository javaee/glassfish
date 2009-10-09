package com.sun.s1asdev.jdbc.connectionleaktracing.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.connectionleaktracing.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.connectionleaktracing.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new
                SimpleReporterAdapter();
        String testSuite = "ConnectionLeakTracing";
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	stat.addDescription("Connection Leak Tracing Tests");

	for(int i=0; i<3; i++){
            SimpleBMPHome convalBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            SimpleBMP convalBMP = convalBMPHome.create();
	    if(!convalBMP.test1()){
		stat.addStatus(testSuite + "test1 : ", stat.FAIL);
		break;
	    }
	    Thread.sleep(20000);
	}
	stat.addStatus(testSuite + "test1 : ", stat.PASS);
        stat.printSummary();
    }
}
