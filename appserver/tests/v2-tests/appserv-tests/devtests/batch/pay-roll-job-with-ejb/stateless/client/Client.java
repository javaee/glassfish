package com.sun.ejb.devtest.client;
  
import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.ejb.EJB;
import com.oracle.javaee7.samples.batch.simple.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

@EJB(name="ejb/GG", beanInterface=Sless.class)
public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("batch-pay-rool-job-ejb-stateless");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("batch-pay-rool-job-ejb-stateless");
    }  
    
    public Client (String[] args) {
    }
    
    private static @EJB(name="ejb/kk") Sless sless;

    public void doTest() {
        try {
            (new InitialContext()).lookup("java:comp/env/ejb/GG");
	    long result = sless.submitJob();
	    System.out.println("************************************************");
	    System.out.println("******* JobID: " + result + " ******************");
	    System.out.println("************************************************");
            stat.addStatus("batch payroll", stat.PASS);
	} catch (Exception ex) {
            stat.addStatus("batch payroll", stat.FAIL);
        }
    }

}

