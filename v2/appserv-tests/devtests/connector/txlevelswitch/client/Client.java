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
