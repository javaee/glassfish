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

package com.sun.s1asdev.jdbc.pooling.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;
import javax.sql.DataSource;
import com.sun.s1asdev.jdbc.pooling.ejb.SimpleSessionHome;
import com.sun.s1asdev.jdbc.pooling.ejb.SimpleSession;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
   
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter();
    private static boolean rollback;
    private static boolean isXA;
    private static String testSuite = "Pooling ";

    private static InitialContext ic;
    public static void main(String[] args)
        throws Exception {
        
        try {
	    ic = new InitialContext();
	} catch(NamingException ex) {
	    ex.printStackTrace();
	}

        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
	SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);
	stat.addDescription("Running pooling testSuite ");
        SimpleSession simpleSession = simpleSessionHome.create();
        
        System.out.println("verifying uniqueness of all connection");
        if(simpleSession.openAndCloseConnection(40)) {
            stat.addStatus( testSuite + " openAndCloseConnection (non-xa) : ", stat.PASS );
	} else {
            stat.addStatus( testSuite + " openAndCloseConnection (non-xa) : ", stat.PASS );
	}

        System.out.println("creating connection upto max-pool-size of 32");
        if(simpleSession.openMaxConnections(32)) {
            stat.addStatus( testSuite + " openMaxConnections (non-xa) : ", stat.PASS );
	} else {
            stat.addStatus( testSuite + " openMaxConnections (non-xa) : ", stat.PASS );
	}


        rollback = false;
        System.out.println("rollback set to " + rollback);
        runTest(simpleSession, rollback);

        rollback = true;
        System.out.println("rollback set to " + rollback);
        runTest(simpleSession, rollback);

        stat.printSummary();
    }
    
    private static void runTest(SimpleSession simpleSession, boolean rollback) {
        try {        
        //Connection opened and closed within transaction
        //non-xa resource
        isXA = false;
        if ( simpleSession.test1(isXA, rollback) ) {
            stat.addStatus( testSuite + " test1 rollback=" + rollback + " (non-xa. con opened closed within tx) : ", stat.PASS );
        } else {
            stat.addStatus( testSuite + " test1 rollback=" + rollback + " (non-xa. con opened closed within tx) : ", stat.FAIL );
        }

        //xa resource
        isXA = true;
        if ( simpleSession.test1(isXA, rollback) ) {
            stat.addStatus( testSuite + " test1 rollback=" + rollback + " (xa. con opened closed within tx) : ", stat.PASS );
        } else {
            stat.addStatus( testSuite + " test1 rollback=" + rollback + " (xa. con opened closed within tx) : ", stat.FAIL );
        }
        
        //Connection opened within transaction 
        //but closed after transaction
        isXA = false;
        //non-xa resource
        if ( simpleSession.test2(isXA, rollback) ) {
            stat.addStatus( testSuite + " test2 rollback=" + rollback + " (non-xa. con opened closed after tx) : ", stat.PASS );
        } else {
            stat.addStatus( testSuite + " test2 rollback=" + rollback + " (non-xa. con opened closed after tx) : ", stat.FAIL );
        }

        //xa resource
        isXA = true;
        if ( simpleSession.test2(isXA, rollback) ) {
            stat.addStatus( testSuite + " test2 rollback=" + rollback + " (xa. con opened closed after tx) : ", stat.PASS );
        } else {
            stat.addStatus( testSuite + " test2 rollback=" + rollback + " (xa. con opened closed after tx) : ", stat.FAIL );
        }
                
        //XA and Non-XA resource within same transaction
        //non-xa resource and xa  resource together
        if ( simpleSession.test3(rollback) ) {
            stat.addStatus( testSuite + " test3 rollback=" + rollback + " (xa  non-xa within same tx) : ", stat.PASS );
        } else {
            stat.addStatus( testSuite + " test3 rollback=" + rollback + " (xa  non-xa within same tx) : ", stat.FAIL );
        } 
	}catch(Exception ex) {
		ex.printStackTrace();
	}
    }
}
