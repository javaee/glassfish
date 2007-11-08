package com.sun.s1asdev.ejb.bmp.txtests.simple.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.ejb.bmp.txtests.simple.ejb.SimpleBMPHome;
import com.sun.s1asdev.ejb.bmp.txtests.simple.ejb.SimpleBMP;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args)
        throws Exception
    {
        try {
            stat.addDescription("Testing txtests simple app.");
	    InitialContext ic = new InitialContext();
            Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	    SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);
            
            int id= (int) System.currentTimeMillis();
	    System.out.println("Starting test for id: " + id);
	    SimpleBMP simpleBMP = simpleBMPHome.create(id);
	    simpleBMP.getID();
            
            SimpleBMP bean = simpleBMPHome.findByPrimaryKey(new Integer(id));
	    simpleBMP.getID();
	    System.out.println("Done for id: " + id);
            stat.addStatus("txtests simple", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("txtests simple", stat.FAIL);
        }
        stat.printSummary("simple");
    }
}
