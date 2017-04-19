package com.sun.s1asdev.ejb.bmp.simple.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.ejb.bmp.simple.ejb.SimpleBMPHome;
import com.sun.s1asdev.ejb.bmp.simple.ejb.SimpleBMP;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args)
        throws Exception
    {
        try {
            stat.addDescription("Testing bmp simple app.");
	    InitialContext ic = new InitialContext();
            Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	    SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);
            
            int id= (int) System.currentTimeMillis();
 	    System.out.println("Starting test for id: " + id);
 	    SimpleBMP simpleBMP = simpleBMPHome.create(id);

	    boolean threadPoolIDTestStatus =
		simpleBMP.isServicedBy("express-service-thread-pool");
            stat.addStatus("bmp ThreadPoolTest",
		((threadPoolIDTestStatus == true) ?  stat.PASS : stat.FAIL));
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("bmp simple", stat.FAIL);
        }
        stat.printSummary("simple");
    }
}
