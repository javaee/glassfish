package com.sun.s1asdev.ejb.timer.mdbtimer.client;

/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.jms.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        Client client = new Client(args);

        stat.addDescription("ejb-timer-mdbtimer");
        client.go();
        stat.printSummary("ejb-timer-mdbtimerID");
        System.exit(0);
    }

    private InitialContext context;
    private QueueConnection queueCon;
    private QueueSession queueSession;
    private QueueSender queueSender;
    private QueueReceiver queueReceiver;
    private javax.jms.Queue clientDest;
    private javax.jms.Queue targetDest;

    private int numMessages = 2;
    public Client(String[] args) {
        
        if( args.length == 1 ) {
            numMessages = new Integer(args[0]).intValue();
        }
    }

    public void go() {
        try {
            setup();
            doTest();
            stat.addStatus("mdbtimer main", stat.PASS);
        } catch(Throwable t) {
            stat.addStatus("mdbtimer main", stat.FAIL);
            t.printStackTrace();
        } finally {
            cleanup();
        }
    }

    public void setup() throws Exception {
        context = new InitialContext();
        
        QueueConnectionFactory queueConFactory = 
            (QueueConnectionFactory) context.lookup
            ("java:comp/env/FooCF");

        queueCon = queueConFactory.createQueueConnection();

        queueSession = queueCon.createQueueSession
            (false, Session.AUTO_ACKNOWLEDGE); 

        targetDest = (javax.jms.Queue) context.lookup("java:comp/env/jms/MsgBeanQueue");

        // Producer will be specified when actual msg is sent.
        queueSender = queueSession.createSender(targetDest);        

        clientDest = (javax.jms.Queue) context.lookup("java:comp/env/jms/ClientQueue");

        queueReceiver = queueSession.createReceiver(clientDest);

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

    public void sendMsg(Message msg) 
        throws JMSException {
        System.out.println("Sending message to " +
                               " at time " + System.currentTimeMillis());
        queueSender.send(msg);
        System.out.println("Sent message " +
                           " at time " + System.currentTimeMillis());
    }

    public Message recvQueueMsg(long timeout) throws JMSException {
        System.out.println("Waiting for queue message ");
        Message recvdmessage = queueReceiver.receive(timeout);
        if( recvdmessage != null ) {
            System.out.println("Received message : " + recvdmessage + 
                               " at " + new Date());
        } else {
            System.out.println("timeout after " + timeout + " seconds");
            throw new JMSException("timeout" + timeout + " seconds");
        }
        return recvdmessage;
    }

    public void doTest() throws Exception {

        TextMessage message = queueSession.createTextMessage();
        message.setText("ejb-timer-mdbtimer");
        sendMsg(message);

        ObjectMessage recvMsg1 = (ObjectMessage) recvQueueMsg(20000);
        Date cancellation = (Date) recvMsg1.getObject();
        
        Date now = new Date();
        
        // wait for message after timer is cancelled (plus some buffer time)
        long wait = now.before(cancellation) ? 
            (cancellation.getTime() - now.getTime()) + 30000 : 0;
        System.out.println("Timer will be cancelled after " + cancellation);
        System.out.println("Waiting for cancellation notification until " +
                           new Date(now.getTime() + wait));

        ObjectMessage recvMsg2 = (ObjectMessage) recvQueueMsg(wait);
        System.out.println("got message after periodic timer was cancelled!!");
        System.out.println("Cancellation time = " + recvMsg2.getObject());

    }
}

