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

package com.sun.s1asdev.ejb.perf.local2.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.perf.local2.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private int numIterations = 500000;
    private int numThreads = 1;

    private Hello[] hellos;

    public static void main (String[] args) {

        stat.addDescription("ejb-perf-local2");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-perf-local2ID");
    }  
    
    public Client (String[] args) {
        if (args.length > 0) {
            numIterations = Integer.parseInt(args[0]);
            numThreads = Integer.parseInt(args[1]);
        }
        
    }
    
    public void doTest() {

        try {

            System.out.println("Num iterations set to " + numIterations);
            System.out.println("Num threads set to " + numThreads);

	    Context ic = new InitialContext();

	    // create EJB using factory from container 
	    java.lang.Object objref = 
                ic.lookup("java:comp/env/ejb/PerformanceApp");

	    System.out.println("Looked up home!!");

	    HelloHome  home = (HelloHome)
                PortableRemoteObject.narrow(objref, HelloHome.class);
	    System.out.println("Narrowed home!!");

            hellos = new Hello[numThreads];
            for(int i = 0; i < numThreads; i++) {
                hellos[i] = home.create(numIterations);
            }
	    System.out.println("Got the EJB!!");

	    // invoke method on the EJB
	    doPerfTest();

            stat.addStatus("local2 main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local2 main" , stat.FAIL);
        }
        
    	return;
    }

    private void doPerfTest() 
	throws Exception
    {
	System.out.println("\nStateful Session results (microsec): \twith tx \tno tx:");
        hellos[0].warmup(Common.STATEFUL);
        
	runTests(Common.STATEFUL);

	System.out.println("\nStateless Session results (microsec): \twith tx \tno tx:");

        hellos[0].warmup(Common.STATELESS);
	runTests(Common.STATELESS);

	System.out.println("\nBMP Entity results (microsec): \t\twith tx \tno tx:");
        hellos[0].warmup(Common.BMP);

	runTests(Common.BMP);
    }
    
    private void runTests(int type)
	throws Exception
    {

        System.out.println("car no tx    : \t\t\t\t" 
                           + runTest(type, InvokeType.CAR, false));
        System.out.println("notSupported : \t\t\t\t" 
                           + runTest(type, InvokeType.NOT_SUPPORTED,
                                     true) + "\t\t" +
                           + runTest(type, InvokeType.NOT_SUPPORTED,
                                     false));

        System.out.println("supports : \t\t\t\t" 
                           + runTest(type, InvokeType.SUPPORTS, true) + "\t\t" +
                           + runTest(type, InvokeType.SUPPORTS, false) );

        System.out.println("required : \t\t\t\t" 
                           + runTest(type, InvokeType.REQUIRED,true) + "\t\t" +
                           + runTest(type, InvokeType.REQUIRED, false) );
        System.out.println("requiresNew : \t\t\t\t" 
                           + runTest(type, InvokeType.REQUIRES_NEW, true) + "\t\t" +
                           + runTest(type, InvokeType.REQUIRES_NEW, false) );
        System.out.println("mandatory : \t\t\t\t" 
                           + runTest(type, InvokeType.MANDATORY, true));
        System.out.println("never : \t\t\t\t\t\t" 
                           + runTest(type, InvokeType.NEVER, false) );

    }

    private float runTest(int type, InvokeType invokeType,
                         boolean tx) throws Exception {
        
        InvokeThread[] threads = new InvokeThread[numThreads];
        for(int i = 0; i < numThreads; i++) {
            threads[i] = new InvokeThread(invokeType, type, hellos[i], tx);
        }

        for(InvokeThread next : threads) {
            next.start();
        }

        float total = 0;
        for(InvokeThread next : threads) {
            next.join();
            total += next.getResultTime();
            //System.out.println("individual time = " + next.getResultTime());
        }

        return total / numThreads;
        
    }

    enum InvokeType {
        CAR,
        NOT_SUPPORTED,
        SUPPORTS,
        REQUIRED,
        REQUIRES_NEW,
        MANDATORY,
        NEVER
    }

    private class InvokeThread extends Thread {


        private InvokeType invokeType;
        private Hello invokeTarget;
        private int invokeBeanType;
        private boolean invokeTx;
        private float resultTime;


        public InvokeThread(InvokeType type, int beanType, Hello target,
                      boolean tx) {
            invokeType = type;
            invokeTarget = target;
            invokeBeanType = beanType;
            invokeTx = tx;
        }

        public void run() {

            try {
                switch(invokeType) {
                    
                case CAR :
                    resultTime = invokeTarget.createAccessRemove
                        (invokeBeanType, invokeTx);
                    
                    break;
                    
                case NOT_SUPPORTED :
                    resultTime = invokeTarget.notSupported
                        (invokeBeanType, invokeTx);
                    
                    break;
                    
                case SUPPORTS :
                    resultTime = invokeTarget.supports
                        (invokeBeanType, invokeTx);
                    
                    break;
                    
                case REQUIRED :
                    resultTime = invokeTarget.required
                        (invokeBeanType, invokeTx);
                    
                    break;
                    
                    
                case REQUIRES_NEW :
                    resultTime = invokeTarget.requiresNew
                        (invokeBeanType, invokeTx);
                    
                    break;
                    
                    
                case MANDATORY :
                    resultTime = invokeTarget.mandatory
                        (invokeBeanType, invokeTx);
                    
                    break;
                    
                    
                case NEVER :
                    resultTime = invokeTarget.never
                        (invokeBeanType, invokeTx);
                    
                    break;
                }
            } catch(Exception e) {
                e.printStackTrace();
                resultTime = 0;
            }

        }

        public float getResultTime() {
            return resultTime;
        }

    }
}

