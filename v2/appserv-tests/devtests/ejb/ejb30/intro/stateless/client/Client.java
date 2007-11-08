package com.sun.ejb.devtest.client;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.ejb.EJB;
import com.sun.ejb.devtest.Sless;
import com.sun.ejb.devtest.Sless30;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

@EJB(name="ejb/GG", beanInterface=Sless.class)
public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-allowed-session");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-allowed-sessionID");
    }  
    
    public Client (String[] args) {
    }
    
    private static @EJB(name="ejb/kk") Sless sless;

    private static @EJB Sless30 sless30;


    public void doTest() {
        try {
            (new InitialContext()).lookup("java:comp/env/ejb/GG");
	    sless.sayHello();
            stat.addStatus("intro sayHello", stat.PASS);
	} catch (Exception ex) {
            stat.addStatus("intro sayHello", stat.FAIL);
        }

	boolean result = false;

        try {
	    result = sless.wasEjbCreateCalled();
            stat.addStatus("intro wasEjbCreateCalled",
                    result ? stat.PASS : stat.FAIL);
	} catch (Exception ex) {
            stat.addStatus("intro wasEjbCreateCalled", stat.FAIL);
	}


        try {
	    sless30.sayHello();
            stat.addStatus("intro sayHello30", stat.PASS);
	} catch (Exception ex) {
            stat.addStatus("intro sayHello30", stat.FAIL);
        }

	result = false;

        try {
	    result = sless30.wasEjbCreateCalled();
            stat.addStatus("intro wasEjbCreateCalled30",
                    (result == false) ? stat.PASS : stat.FAIL);
	} catch (Exception ex) {
            stat.addStatus("intro wasEjbCreateCalled30", stat.FAIL);
	}




    }

}

