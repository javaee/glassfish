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

package com.sun.s1aspe.naming.nonfailover.client;

import javax.jms.*;
import javax.naming.*;
import java.sql.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
    new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {

        stat.addDescription("This is to test naming nonfailover scenario.\n" + 
			    "Exisitng and New InitialContexts must be able to "+
			    "talk to the server after a restart assuming there "+
			    "were no requests during resart.");

        Context                 jndiContext = null;
        QueueConnectionFactory  queueConnectionFactory = null;
	Queue                   queue = null;
	boolean                 passed = true;

        try {
            jndiContext = new InitialContext();
	    System.out.println("Context created!!!");
	} catch (NamingException e) {
	    System.out.println("Could not create JNDI " +
			     "context: " + e.toString());
	    stat.addStatus("naming nonfailover main", stat.FAIL);
	    stat.printSummary("nonfailoverID");
	    passed = false;
            System.exit(1);
        }

        try {
            queueConnectionFactory = (QueueConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/QCFactory");
	    System.out.println("looked up QueueConnectionFactory...");        
        } catch (NamingException e) {
            System.out.println("JNDI lookup failed: " +
                e.toString());
            stat.addStatus("naming nonfailover main", stat.FAIL);
	    passed = false;
	    stat.printSummary("nonfailoverID");
            System.exit(1);
        }

	try {
	    Thread.sleep(240000);
        } catch (Throwable e) {
            System.out.println("Exception occurred: " + e.toString());
	    passed = false;
	    stat.addStatus("naming nonfailover main", stat.FAIL);
        }  

	try {
	    //see if the existing InitialContext is still alive
	    if (jndiContext != null) {
	        System.out.println("Existing InitialContext is alive after server restart!");
	    }
	    
	    queue = (Queue) jndiContext.lookup("java:comp/env/jms/SampleQueue");
	    System.out.println("looked up Queue...");
	    
	    //creating new InitialContext
	    jndiContext = new InitialContext();
	    System.out.println("Creating new InitialContext...");
	    queueConnectionFactory = (QueueConnectionFactory)
	      jndiContext.lookup
	      ("java:comp/env/jms/QCFactory");
	    System.out.println("looked up QueueConnectionFactory with new InitialContext..." + 
			       queueConnectionFactory); 
	    
        } catch (NamingException e) {
            System.out.println("JNDI lookup failed: " +
                e.toString());
            stat.addStatus("naming nonfailover main", stat.FAIL);
	    stat.printSummary("nonfailoverID");
	    passed = false;
            System.exit(1);
        }
	if (passed) stat.addStatus("naming nonfailover main", stat.PASS);
	stat.printSummary("nonfailoverID");
	System.exit(0);
    } // main
} // class


