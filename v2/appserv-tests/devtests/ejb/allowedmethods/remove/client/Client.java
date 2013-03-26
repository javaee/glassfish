package com.sun.s1asdev.ejb.allowedmethods.remove.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.allowedmethods.remove.DriverHome;
import com.sun.s1asdev.ejb.allowedmethods.remove.Driver;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-allowedmethods-remove");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-allowedmethods-remove");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        try {
            Context ic = new InitialContext();
            Object objref = ic.lookup("java:comp/env/ejb/Driver");
            DriverHome  home = (DriverHome)PortableRemoteObject.narrow
                (objref, DriverHome.class);
            Driver driver = home.create();
            boolean ok = driver.test();
            System.out.println("Test returned: " + ok);
            stat.addStatus("ejbclient remote test", ((ok)? stat.PASS : stat.FAIL));
        } catch(Exception e) {
            System.out.println("Got exception: " + e.getMessage());
            stat.addStatus("ejbclient remote test(-)" , stat.FAIL);
        }
    }

}

