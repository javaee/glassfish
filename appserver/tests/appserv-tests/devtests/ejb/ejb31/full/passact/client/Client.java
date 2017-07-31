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

package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import javax.naming.InitialContext;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;


public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String args[]) {

	appName = args[0]; 
	stat.addDescription(appName);
	Client client = new Client(args);       
        client.doTest();	
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {}

    public void doTest() {

	try {
	    int numHellos = 8; // 8 is the batch size, so 8 should be guaranteed to be pasivated

	    Hello[] hellos = new Hello[numHellos];
	    int passivationCount = 0;

	    for(int i = 0; i < numHellos; i++) {
		
		hellos[i] = (Hello) new InitialContext().lookup("java:global/"+appName+"/HelloBean");
		hellos[i].hello();
	    }

	    try {
		System.out.println("Waiting for passivation...");
		Thread.sleep(20000);
	    } catch(Exception e) {
		e.printStackTrace();
	    }

	    for(int i = 0; i < numHellos; i++) {
		
		hellos[i].hello();
		if( hellos[i].passivatedAndActivated() ) {
		    passivationCount++;
		}
	    }

	    if( passivationCount != numHellos ) {
		System.out.println("Passivation failed -- count = " + passivationCount + " instead of expected " + numHellos);
		throw new EJBException("passivation failed");
	    }

	    stat.addStatus("local main", stat.PASS);

	} catch(Exception e) {
	    stat.addStatus("local main", stat.FAIL);
	    e.printStackTrace();
	}

    }


}
