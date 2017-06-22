package com.sun.ejb.devtest.client;
  
import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.ejb.EJB;
import com.oracle.javaee7.samples.batch.simple.batchlet.*;
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
	    long executionId = sless.submitJob();
	    System.out.println("************************************************");
	    System.out.println("******* JobID: " + executionId + " ******************");
	    System.out.println("************************************************");
	    String jobBatchStatus = "";
	    for (int sec=10; sec>0; sec--) {
	        try {
		    jobBatchStatus = sless.getJobExitStatus(executionId);
		    if (! "COMPLETED".equals(jobBatchStatus)) {
		        System.out.println("Will sleep for " + sec + " more seconds...: " + jobBatchStatus);
		        Thread.currentThread().sleep(1000);
		    }
		} catch (Exception ex) {
		}
	    }
            stat.addStatus("simple-batchlet payroll", ("COMPLETED".equals(jobBatchStatus) ? stat.PASS : stat.FAIL));
	} catch (Exception ex) {
            stat.addStatus("simple-batchlet payroll", stat.FAIL);
        }
    }

}

