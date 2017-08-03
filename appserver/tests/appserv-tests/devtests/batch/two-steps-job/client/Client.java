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
import com.oracle.javaee7.samples.batch.twosteps.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

@EJB(name="ejb/GG", beanInterface=Sless.class)
public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("batch-two-steps-stateless");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("batch-two-steps-stateless");
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
            stat.addStatus("batch-two-steps-stateless", ("COMPLETED".equals(jobBatchStatus) ? stat.PASS : stat.FAIL));
	} catch (Exception ex) {
            stat.addStatus("batch-two-steps-stateless", stat.FAIL);
        }
    }

}

