package com.sun.s1asdev.security.mdb.client;

/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
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

        stat.addDescription("security-mdb");
        client.doTest();
        stat.printSummary("security-mdbID");
        System.exit(0);
    }


    @Resource(name="FooCF", mappedName="jms/security_mdb_QCF") 
    private static QueueConnectionFactory queueConFactory;

    @Resource(name="MsgBeanQueue", mappedName="jms/security_mdb_InQueue")
    private static javax.jms.Queue msgBeanQueue;

//    @Resource(name="ClientQueue", mappedName="foo")
//    private static javax.jms.Queue clientQueue;
@Resource(name="ClientQueue", mappedName="jms/security_mdb_OutQueue") 
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
            stat.addStatus("cmt main", stat.PASS);
        } catch(Throwable t) {
            stat.addStatus("cmt main", stat.FAIL);
            t.printStackTrace();
        } finally {
            cleanup();
        }
    }

    public void setup() throws Exception {
        
        queueCon = queueConFactory.createQueueConnection();

        queueSession = queueCon.createQueueSession
            (false, Session.AUTO_ACKNOWLEDGE); 

        // Producer will be specified when actual msg is sent.
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

    public void sendMsgs(javax.jms.Queue queue, Message msg, int num) 
        throws JMSException {
        for(int i = 0; i < num; i++) {
            System.out.println("Sending message " + i + " to " + queue + 
                               " at time " + System.currentTimeMillis());
            queueSender.send(queue, msg);
            System.out.println("Sent message " + i + " to " + queue + 
                               " at time " + System.currentTimeMillis());
        }
    }

    public void doTest(int num) 
        throws Exception {

        Destination dest = msgBeanQueue;

        Message message = queueSession.createTextMessage("foo");

        message.setBooleanProperty("flag", true);
        message.setIntProperty("num", 2);
        sendMsgs((javax.jms.Queue) dest, message, num);

/*        System.out.println("Waiting for queue message");
        Message recvdmessage = queueReceiver.receive(TIMEOUT);
        if( recvdmessage != null ) {
            System.out.println("Received message : " + 
                                   ((TextMessage)recvdmessage).getText());
        } else {
            System.out.println("timeout after " + TIMEOUT + " seconds");
            throw new JMSException("timeout" + TIMEOUT + " seconds");
        }*/
    }
}

