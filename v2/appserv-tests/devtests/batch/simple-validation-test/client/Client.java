package com.sun.ejb.devtest.client;
  
import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.ejb.EJB;
import com.oracle.javaee7.samples.batch.cmt.chunk.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("batch-cmt-chunk");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("batch-cmt-chunk");
    }  
    
    public Client (String[] args) {
    }
    
    private static @EJB JobSubmitter jobSubmitter;

    int MAX_JOB_SIZE = 2;
    long[] executionIds = new long[MAX_JOB_SIZE];

    public void doTest() {
	for (int i=0; i<MAX_JOB_SIZE; i++)
	    executionIds[i] = -1;
        submitJobs();
        listBatchJobs();
        listBatchJobsInLongFormat();
        checkJobExecution(executionIds[0]);
    	isJobExecutionOK(executionIds[0]);
    	checkAllJobExecutions();
    	checkIfOnlyJobsFromThisAppAreVisible();
    }

    public void submitJobs() {
        try {
	    for (int i = 0; i< MAX_JOB_SIZE; i++) {
	        executionIds[i] = jobSubmitter.submitJob("Simple-Validation-Job");
	        checkBatchJobStatus(executionIds[i], 20);
	    }
	    boolean result = true;
	    for (int i=0; i<MAX_JOB_SIZE; i++) {
	       result = result && executionIds[i] != -1;
	    }
            stat.addStatus("batch-cmt-chunk-test1", (result ? stat.PASS : stat.FAIL));
	} catch (Exception ex) {
            stat.addStatus("batch-cmt-chunk-test1", stat.FAIL);
        }
    }

    public void listBatchJobs() {
        try {
	    boolean status = true;
	    Collection<String> jobs = jobSubmitter.listJobs(false);
	    for (String job : jobs) {
	        System.out.println("** JOB ==> " + job);
	    }
            stat.addStatus("batch-cmt-chunk-test2", (status ? stat.PASS : stat.FAIL));
	} catch (Exception ex) {
            stat.addStatus("batch-cmt-chunk-test2", stat.FAIL);
        }
    }

    public void listBatchJobsInLongFormat() {
        try {
	    boolean status = true;
	    Collection<String> jobs = jobSubmitter.listJobs(true);
	    for (String job : jobs) {
	        System.out.println("** JOB ==> " + job);
	    }
            stat.addStatus("batch-cmt-chunk-test3", (status ? stat.PASS : stat.FAIL));
	} catch (Exception ex) {
            stat.addStatus("batch-cmt-chunk-test3", stat.FAIL);
        }
    }

    public void checkJobExecution(long executionId) {
        try {
	    boolean status = true;
	    Map<String, String> map = jobSubmitter.toMap(executionId);

            stat.addStatus("batch-cmt-status-checkExe-jobName", map.get("jobName") != null ? stat.PASS : stat.FAIL);
            stat.addStatus("batch-cmt-status-checkExe-appName", map.get("appName") != null ? stat.PASS : stat.FAIL);
            stat.addStatus("batch-cmt-status-checkExe-instanceCount", map.get("instanceCount") != null ? stat.PASS : stat.FAIL);
            stat.addStatus("batch-cmt-status-checkExe-instanceID", map.get("instanceID") != null ? stat.PASS : stat.FAIL);
            stat.addStatus("batch-cmt-status-checkExe-executionID", map.get("executionID") != null ? stat.PASS : stat.FAIL);
            stat.addStatus("batch-cmt-status-checkExe-startTime", map.get("startTime") != null ? stat.PASS : stat.FAIL);
            stat.addStatus("batch-cmt-status-checkExe-batchStatus", map.get("batchStatus") != null ? stat.PASS : stat.FAIL);
            stat.addStatus("batch-cmt-status-checkExe-endTime", map.get("endTime") != null ? stat.PASS : stat.FAIL);

	} catch (Exception ex) {
            stat.addStatus("batch-cmt-chunk-test3", stat.FAIL);
        }
    }

    public void isJobExecutionOK(long executionId) {
        stat.addStatus("batch-cmt-status-checkExe-isJobExecutionOK", checkOneJobExecution(executionId) ? stat.PASS : stat.FAIL);
    }

    private boolean checkOneJobExecution(long executionId) {
        try {
	    Map<String, String> map = jobSubmitter.toMap(executionId);
	    return map != null &&
            	map.get("jobName") != null &&
            	map.get("appName") != null &&
            	map.get("instanceCount") != null &&
            	map.get("instanceID") != null &&
            	map.get("executionID") != null &&
            	map.get("startTime") != null &&
            	map.get("batchStatus") != null &&
            	map.get("endTime") != null;

	} catch (Exception ex) {

        }
        return false;
    }

    public void checkAllJobExecutions() {
       boolean result = true;
       for (long exeId : jobSubmitter.getAllExecutionIds(null)) {
           if (!checkOneJobExecution(exeId)) {
              result = false;
	      break;
	   }
       }
        stat.addStatus("batch-cmt-status-checkExe-checkAllJobExecutions", result ? stat.PASS : stat.FAIL);
    }

    public void checkIfOnlyJobsFromThisAppAreVisible() {
        try {
	    boolean status = true;
            for (long exeId : jobSubmitter.getAllExecutionIds(null)) {
	        Map<String, String> map = jobSubmitter.toMap(exeId);
		String jobName = map.get("jobName");
		String appName = map.get("appName");
	        if (!jobName.startsWith("Simple-Validation-Job") || !appName.startsWith("server-config:Simple-ValidationApp")) {
		    System.out.println("***********************************************");
		    System.out.println("*** Job From another app? " + jobName + "; " + appName + " ***");
		    System.out.println("***********************************************");
		    status = false;
		    break;
		}
	    }
            stat.addStatus("batch-cmt-chunk-checkIfOnlyJobsFromThisAppAreVisible", (status ? stat.PASS : stat.FAIL));
	} catch (Exception ex) {
            stat.addStatus("batch-cmt-chunk-checkIfOnlyJobsFromThisAppAreVisible", stat.FAIL);
        }
    }

    public void checkBatchJobStatus(long executionId, int sec) {
	while (sec-- > 0) {
            try {
		String status = jobSubmitter.getJobExitStatus(executionId);
		if ("COMPLETED".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status)) {
		    System.out.println("** checkBatchJobStatus[" + executionId + "]  ==> " + status);
		    break;
		}
                System.out.println("Will sleep for " + sec + " more seconds...");
                Thread.currentThread().sleep(1000);
            } catch (Exception ex) {
	    }
        }
    }

}

