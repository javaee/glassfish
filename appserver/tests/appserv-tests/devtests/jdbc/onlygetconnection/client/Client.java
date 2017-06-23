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

package com.sun.s1asdev.jdbc.onlygetconnection.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.onlygetconnection.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    private SimpleReporterAdapter stat = new SimpleReporterAdapter();
    private String testSuite = "jdbc-onlygetconnection ";
    private NoTxConnTest bean;


    public static void main(String[] args)
        throws Exception {

// 	SimpleReporterAdapter stat = new SimpleReporterAdapter();
//
//        InitialContext ic = new InitialContext();
//        Object objRef = ic.lookup("java:comp/env/ejb/NoTxConnTestEJB");
//	NoTxConnTestHome home = (NoTxConnTestHome)
//            javax.rmi.PortableRemoteObject.narrow(objRef, NoTxConnTestHome.class);
//
//        NoTxConnTest bean = home.create();

//      if ( bean.test1() ) {
//	    stat.addStatus(testSuite+" test1 : ", stat.PASS);
//	} else {
//	    stat.addStatus(testSuite+" test1 : ", stat.FAIL);
//	}
//	stat.printSummary();
        
        if ( args.length == 0 ) {
            (new Client()).runSingleThreaded();
        } else {
            int numRuns = 10;
            try {
                numRuns = Integer.parseInt( args[0] );
            } catch(NumberFormatException e) {
            } finally {
                (new Client()).runMultiThreaded( numRuns );
            }
        }
        
    }

    public Client() throws Exception {
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/NoTxConnTestEJB");
	NoTxConnTestHome home = (NoTxConnTestHome)
            javax.rmi.PortableRemoteObject.narrow(objRef, NoTxConnTestHome.class);

        bean = home.create();
    }
   
    public void runSingleThreaded() throws Exception {
        if ( bean.test1() ) {
	    stat.addStatus(testSuite+" test1 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+" test1 : ", stat.FAIL);
	}
	stat.printSummary();
 
    }

    
    public void runMultiThreaded( int numThreads ) throws Exception {
        MTClient[] mtc = new MTClient[ numThreads ];
        boolean result = true;
        int failCount = 0;

        for ( int i = 0; i < numThreads; i++) { 
            mtc[i] = new MTClient( i );
            mtc[i].start();
        }

        //Wait for all threads to end before printing summary
        for ( int i = 0; i < numThreads; i++ ) {
            mtc[i].join();
        }
        
        for ( int i = 0; i < numThreads; i++ ) {
            result &= mtc[i].result;
            if ( !mtc[i].result ) {
                failCount++;
            }
            
        }
        
        System.out.println("Total Threads: " + numThreads );
        System.out.println("Total Passed : " + (numThreads - failCount ) );
        System.out.println("Total Failed : " + failCount );

        if ( result ) {
            stat.addStatus( testSuite + " multithreaded-test : " , stat.PASS );
        } else {
            stat.addStatus( testSuite + " multithreaded-test : " , stat.FAIL );
        }
        
        stat.printSummary();
    }

    class MTClient extends Thread {
        int id_;
        boolean result = false;

        MTClient( int id ) {
            id_ = id;
        }
        
        public void run() {
            try {
                result = bean.test1();
            } catch( Exception e ) {
            }
        }
    }
}
