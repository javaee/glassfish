/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

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
        checkJobExecution(executionIds[0]);
    	isJobExecutionOK(executionIds[0]);
    	checkIfOnlyJobsFromThisAppAreVisible();
    }

    public void submitJobs() {
        try {
	    for (int i = 0; i< MAX_JOB_SIZE; i++) {
	        executionIds[i] = jobSubmitter.submitJob("CMT-ChunkJob");
	        checkBatchJobStatus(executionIds[i], 10);
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

    public void checkIfOnlyJobsFromThisAppAreVisible() {
        try {
	    boolean status = true;
            for (long exeId : jobSubmitter.getAllExecutionIds(null)) {
	        Map<String, String> map = jobSubmitter.toMap(exeId);
		String jobName = map.get("jobName");
		String appName = map.get("appName");
	        if (!jobName.startsWith("cmt-chunk-job") || !appName.startsWith("server-config:batch-cmt-chunkApp")) {
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

