
package com.sun.s1peqe.connector.mq.simplestress.client;

import javax.jms.*;
import javax.naming.*;
import java.util.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleMessageClient {
    static int TIME_OUT = 20000;
    static boolean debug = false;

    static String user = null;
    static String password = null;

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    int id =0;

    public SimpleMessageClient(int i) {
        this.id = i;
    }

    public static void main(String[] args) {
        System.out.println(args);

	//if (args.length >0) return;
	
	String testId = args[0];
	String testString = args[1];
	String verify = args[2];
	user = args[3];
	password = args[4];

        try {
            boolean pass = true;
	    System.out.println("Running the test : " + testId);

	    if (testString.startsWith("SEND")) { 
	        sendMessage();
	    }
	    
	    String output = null;
	    if (testString.endsWith("RECEIVE")) {
	        output = receiveMessage();
	    }

	    if (verify.equals("VERIFY")) {
	        pass = output.equals("REPLIED:CLIENT");
	    } else {
	        pass = (output == null);
	    }

            if (pass) {
                stat.addStatus(testId, stat.PASS);
            } else {
                stat.addStatus(testId, stat.FAIL);
	    }
        }catch (Throwable t) {
            t.printStackTrace();
            stat.addStatus(testId, stat.FAIL);
        }finally {
            stat.printSummary(testId);
            System.exit(0);
        }
         
    }

    public static String receiveMessage() {
        Context                 jndiContext = null;
        QueueConnectionFactory  queueConnectionFactory = null;
        QueueConnection         queueConnection = null;
        QueueSession            queueSession = null;
        Queue                   queue = null;
        QueueReceiver           queueReceiver = null;
        TextMessage             message = null;

        try {
            jndiContext = new InitialContext();
            queueConnectionFactory = (QueueConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/QCFactory");
            queue = (Queue) jndiContext.lookup("java:comp/env/jms/clientQueue");

            queueConnection =
                queueConnectionFactory.createQueueConnection(user,password);
            queueSession =
                queueConnection.createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            queueConnection.start();
            queueReceiver = queueSession.createReceiver(queue);

	    HashMap map = new HashMap();

            long startTime = System.currentTimeMillis();
            boolean pass = true;
            TextMessage msg = (TextMessage) queueReceiver.receive(TIME_OUT);
	    if (msg == null) {
	       return null;
	    }
	    Integer id = new Integer(msg.getIntProperty("replyid"));
	    return msg.getText();
        }catch (Throwable t) {
            t.printStackTrace();
        }finally {
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (JMSException e) {}
            } // if
        }
        return null;
    }

    public static void sendMessage() {

        Context                 jndiContext = null;
        QueueConnectionFactory  queueConnectionFactory = null;
        QueueConnection         queueConnection = null;
        QueueSession            queueSession = null;
        Queue                   queue = null;
        QueueSender             queueSender = null;
        TextMessage             message = null;

        try {
            jndiContext = new InitialContext();
            queueConnectionFactory = (QueueConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/QCFactory");
            queue = (Queue) jndiContext.lookup("java:comp/env/jms/SampleQueue");

            queueConnection =
                queueConnectionFactory.createQueueConnection(user,password);
            queueSession =
                queueConnection.createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            queueSender = queueSession.createSender(queue);
            message = queueSession.createTextMessage();
            message.setText("CLIENT");
	    message.setIntProperty("id",1);
            queueSender.send(message);
	    debug("Send the message :" + message.getIntProperty("id") + ":" + message.getText());
        } catch (Throwable e) {
            System.out.println("Exception occurred: " + e.toString());
        } finally {
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (JMSException e) {}
            } // if
        } // finally
    } // main

    static void debug(String msg) {
        if (debug) {
	   System.out.println(msg);
	}
    }
} // class

