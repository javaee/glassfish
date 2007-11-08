package com.sun.s1asdev.ejb.ejb30.hello.mdb.client;

/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.jms.*;
import javax.annotation.Resource;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    // in milli-seconds
    private static long TIMEOUT = 90000;

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        Client client = new Client(args);

        stat.addDescription("ejb-ejb30-hello-mdb");
        client.doTest();
        stat.printSummary("ejb-ejb30-hello-mdbID");
        System.exit(0);
    }

    @Resource(mappedName="jms/ejb_ejb30_hello_mdb_QCF") 
    private static QueueConnectionFactory queueConFactory;
 
    //Target Queue
    @Resource(mappedName="jms/ejb_ejb30_hello_mdb_InQueue")
    private static javax.jms.Queue msgBeanQueue;

    //Reply Queue
    @Resource(mappedName="jms/ejb_ejb30_hello_mdb_OutQueue")
    private static javax.jms.Queue clientQueue;

    private QueueConnection queueCon;
    private QueueSession queueSession;
    private QueueSender queueSender;
    private QueueReceiver queueReceiver;
    private int numMessages = 2;
    
    public Client(String[] args) {
        if( args.length == 1 ) {
            numMessages = new Integer(args[0]).intValue();
        }
    }

    public void doTest() {
        try {
            setup();
            doTest(numMessages);
            stat.addStatus("EJB 3.0 MDB", stat.PASS);
        } catch(Throwable t) {
            stat.addStatus("EJB 3.0 MDB", stat.FAIL);
            t.printStackTrace();
        } finally {
            cleanup();
        }
    }

    public void setup() throws Exception {
        queueCon = queueConFactory.createQueueConnection();
        queueSession = queueCon.createQueueSession
            (false, Session.AUTO_ACKNOWLEDGE); 

        // Destination will be specified when actual msg is sent.
        queueSender = queueSession.createSender(null);        
        queueReceiver = queueSession.createReceiver(clientQueue);
        queueCon.start();
    }

    public void cleanup() {
        try {
            if( queueCon != null ) {
                queueCon.close();
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    public void sendMsgs(javax.jms.Queue queue, int num) 
        throws JMSException {
        for(int i = 0; i < num; i++) {
            Message message = queueSession.createTextMessage("foo #" + (i + 1));
            System.out.println("Sending message " + i + " to " + queue + 
                               " at time " + System.currentTimeMillis());
            queueSender.send(queue, message);
	    
            System.out.println("Sent message " + i + " to " + queue + 
                               " at time " + System.currentTimeMillis());
        }
    }

    public void doTest(int num) 
        throws Exception {
        sendMsgs((javax.jms.Queue) msgBeanQueue, num);
        
	//Now attempt to receive responses to our message
        System.out.println("Waiting for queue message");
        Message recvdmessage = queueReceiver.receive(TIMEOUT);
        if( recvdmessage != null ) {
            System.out.println("Received message : " + 
                                   ((TextMessage)recvdmessage).getText());
        } else {
            System.out.println("timeout after " + TIMEOUT + " seconds");
            throw new JMSException("timeout" + TIMEOUT + " seconds");
        }
    }
}
