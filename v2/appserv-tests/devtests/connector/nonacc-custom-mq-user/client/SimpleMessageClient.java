
package com.sun.s1peqe.nonacc.simple.client;

import javax.jms.*;
import javax.naming.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleMessageClient {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {

        stat.addDescription("This is to test simple "+
             "message driven bean sample.");

        Context                 jndiContext = null;
        QueueConnectionFactory  queueConnectionFactory = null;
        QueueConnection         queueConnection = null;
        QueueSession            queueSession = null;
        Queue                   queue = null;
        QueueSender             queueSender = null;
        TextMessage             message = null;
        final int               NUM_MSGS = 3;

        try {
            jndiContext = new InitialContext();
            stat.addStatus("simple mdb main", stat.PASS);
        } catch (NamingException e) {
            System.out.println("Could not create JNDI " +
                "context: " + e.toString());
            stat.addStatus("simple mdb main", stat.FAIL);
            stat.printSummary("simpleMdbID");
            System.exit(1);
        }

        try {
            queueConnectionFactory = (QueueConnectionFactory)
                jndiContext.lookup
                ("jms/QCFactory");
            queue = (Queue) jndiContext.lookup("jms/SampleQueue");
            stat.addStatus("simple mdb main", stat.PASS);
        } catch (NamingException e) {
            System.out.println("JNDI lookup failed: " +
                e.toString());
            stat.addStatus("simple mdb main", stat.FAIL);
            stat.printSummary("simpleMdbID");
            System.exit(1);
        }

        try {
System.out.println("using foo/bar");
            queueConnection =
                queueConnectionFactory.createQueueConnection("foo", "bar");
            queueSession =
                queueConnection.createQueueSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            queueSender = queueSession.createSender(queue);
            message = queueSession.createTextMessage();

            for (int i = 0; i < NUM_MSGS; i++) {
                message.setText("This is message " + (i + 1));
                Thread.sleep(1000);System.out.println("Sending message: " +
                    message.getText());
                queueSender.send(message);
            }

        } catch (Throwable e) {
            System.out.println("Exception occurred: " + e.toString());
            stat.addStatus("simple mdb main exception hcek", stat.FAIL);
        } finally {
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (JMSException e) {}
            } // if
            stat.addStatus("simple mdb main", stat.PASS);
            stat.printSummary("simpleMdbID");
            System.exit(0);
        } // finally
    } // main
} // class

