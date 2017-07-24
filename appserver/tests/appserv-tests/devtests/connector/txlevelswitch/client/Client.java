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

package com.sun.s1asdev.connector.txlevelswitch.test1.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;
import java.io.*;
import com.sun.s1asdev.connector.txlevelswitch.test1.ejb.SimpleSessionHome;
import com.sun.s1asdev.connector.txlevelswitch.test1.ejb.SimpleSession;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import javax.jms.*;

public class Client {
    
    public static void main(String[] args)
        throws Exception {

	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "txlevelswitch-test1 ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
	SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);
        
	stat.addDescription("Running txlevelswitch testSuite1 ");
        SimpleSession simpleSession = simpleSessionHome.create();
	if ( "2".equals(args[0]) ) {
	    //Here we are using 2 non-XA datasources
	    try {
                if (simpleSession.test2() ) {
	            stat.addStatus( testSuite + " test2 : " , stat.PASS );
	        } else {
	            stat.addStatus( testSuite + " test2 : " , stat.FAIL );
	        }
	    } catch( Exception e) {
	        System.out.println("app threw exception********");
	        stat.addStatus(testSuite + " test2 : ", stat.PASS);
	    }
	} else if ( "1".equals(args[0]) || "3".equals(args[0])) {
	    //Here we are using 2 XA datasources
            if (simpleSession.test1() ) {
	        stat.addStatus( testSuite + " test1 : " , stat.PASS );
	    } else {
	        stat.addStatus( testSuite + " test1 : " , stat.FAIL );
	    }
    
	} else if ("jms-jdbc-1".equals(args[0]) ) {
	     if (simpleSession.jmsJdbcTest1() ) {
	        
	        System.out.println(" Now waiting for message in client ");
		System.out.println(" Before Lookup of QCF");
		QueueConnectionFactory qcf = (QueueConnectionFactory)
		    //ic.lookup("java:comp/env/jms/jms-jdbc-res-1");
		    ic.lookup("jms/jms-jdbc-res-1");
		System.out.println(" After Lookup of QCF");
	        
		System.out.println(" Before lookup of Q");
		javax.jms.Queue queue = (javax.jms.Queue) ic.lookup("jms/SampleQueue");
		System.out.println(" After lookup of Q");
		System.out.println(" Before createQueueConnection");
		QueueConnection qConn = qcf.createQueueConnection("guest", "guest");
		qConn.start();
		System.out.println(" After createQueueConnection");
		
		System.out.println(" Before createQueueSession");
		QueueSession qSess = qConn.createQueueSession(false,
		    Session.AUTO_ACKNOWLEDGE);
		System.out.println(" After createQueueSession");
		QueueReceiver qRecv = qSess.createReceiver(queue);
		TextMessage textMsg = (TextMessage) qRecv.receive( 20000 );
		//if ( textMsg != null && "Hello World".equals( textMsg.getText() ) ) {
		if ( textMsg != null ) {
		    System.out.println(" Text message is : " + textMsg.getText() );
		    
	            stat.addStatus( testSuite + " jms-jdbc-test1 : " , stat.PASS );
		} else {
		    System.out.println(" TextMEssage is null");
		    stat.addStatus( testSuite + " jms-jdbc-test1 : ", stat.FAIL );
		}
		qSess.close();
		qConn.close();
		
	        stat.addStatus( testSuite + " jms-jdbc-test1 : " , stat.PASS );
	    } else {
	        stat.addStatus( testSuite + " jms-jdbc-test1 : " , stat.FAIL );
	    }

	} else if ("jms-jdbc-2".equals(args[0]) ) {
	    if ( simpleSession.jmsJdbcTest2() ) {
	        stat.addStatus( testSuite + " jms-jdbc-test2 : " , stat.PASS );
                
	        System.out.println(" Now waiting for message in client ");
		System.out.println(" Before Lookup of QCF");
		QueueConnectionFactory qcf = (QueueConnectionFactory)
		    //ic.lookup("java:comp/env/jms/jms-jdbc-res-1");
		    ic.lookup("jms/jms-jdbc-res-1");
		System.out.println(" After Lookup of QCF");
	        
		System.out.println(" Before lookup of Q");
		javax.jms.Queue queue = (javax.jms.Queue) ic.lookup("jms/SampleQueue");
		System.out.println(" After lookup of Q");
		System.out.println(" Before createQueueConnection");
		QueueConnection qConn = qcf.createQueueConnection("guest", "guest");
		qConn.start();
		System.out.println(" After createQueueConnection");
		
		System.out.println(" Before createQueueSession");
		QueueSession qSess = qConn.createQueueSession(false,
		    Session.AUTO_ACKNOWLEDGE);
		System.out.println(" After createQueueSession");
		QueueReceiver qRecv = qSess.createReceiver(queue);
		TextMessage textMsg = (TextMessage) qRecv.receive( 20000 );
		//if ( textMsg != null && "Hello World".equals( textMsg.getText() ) ) {
		if ( textMsg != null ) {
		    System.out.println(" Text message is : " + textMsg.getText() );
		    
	            stat.addStatus( testSuite + " jms-jdbc-test2 : " , stat.PASS );
		} else {
		    System.out.println(" TextMessage is null");
		    stat.addStatus( testSuite + " jms-jdbc-test2 : ", stat.FAIL );
		}
		qSess.close();
		qConn.close();


	    } else {
	        stat.addStatus( testSuite + " jms-jdbc-test2 : " , stat.FAIL );
	    }

	} else if ( "jms-jdbc-3".equals(args[0]) ) {
            if ( simpleSession.jmsJdbcTest3()) {
	        stat.addStatus( testSuite + " jms-jdbc-test3 : " , stat.PASS );
	    } else {
	        stat.addStatus( testSuite + " jms-jdbc-test3 : " , stat.FAIL );
	    }
	}	

	
	stat.printSummary();
    }
}
