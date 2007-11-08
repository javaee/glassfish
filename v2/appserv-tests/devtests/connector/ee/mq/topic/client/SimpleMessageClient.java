
package com.sun.s1peqe.mq.queue.test.client;

import javax.jms.*;
import javax.naming.*;
import java.sql.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleMessageClient {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {

        stat.addDescription("This is to test queue  "+
             "in EE scenario");

        Context                 jndiContext = null;
        TopicConnectionFactory  queueConnectionFactory = null;
        TopicConnection         queueConnection = null;
        TopicSession            queueSession = null;
        Topic                   queue = null;
        TopicPublisher          queueSender = null;
        TextMessage             message = null;
        final int               NUM_MSGS = 3;
	boolean                 passed = true;

        try {
            jndiContext = new InitialContext();
            System.out.println("jndiContext => " + jndiContext);
            //stat.addStatus("simple mdb main", stat.PASS);
        } catch (NamingException e) {
            System.out.println("Could not create JNDI " +
                "context: " + e.toString());
            stat.addStatus("simple queue test", stat.FAIL);
            stat.printSummary("simpleTopicTest");
            System.exit(1);
        }

        try {
            queueConnectionFactory = (TopicConnectionFactory)
                jndiContext.lookup
                ("java:comp/env/jms/TFactory");
            queue = (Topic) jndiContext.lookup("java:comp/env/jms/SampleTopic");
            //stat.addStatus("simple mdb main", stat.PASS);
        } catch (NamingException e) {
            e.printStackTrace();
            System.out.println("JNDI lookup failed: " +
                e.toString());
            stat.addStatus("simple queue test", stat.FAIL);
            stat.printSummary("simpleTopicTest");
            System.exit(1);
        }

        try {
            queueConnection =
                queueConnectionFactory.createTopicConnection();
            queueSession =
                queueConnection.createTopicSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            queueSender = queueSession.createPublisher(queue);
            message = queueSession.createTextMessage();

            for (int i = 0; i < NUM_MSGS; i++) {
                message.setText("This is message " + (i + 1));
		message.setIntProperty("Id",i);
                System.out.println("Sending message: " +
                    message.getText());
                queueSender.publish(message);
            }

           Thread.sleep(10000);
	    Class.forName("com.pointbase.jdbc.jdbcUniversalDriver");
	    String url = "jdbc:pointbase:server://localhost:9092/sqe-samples,new";
	    java.sql.Connection con = DriverManager.getConnection(url,"DBUSER","DBPASSWORD");
	    ResultSet rs = con.createStatement().executeQuery("select msg from mq_queue_test");
	    int count = 0;
	    while (rs.next()){
               count++;
               System.out.println("Value :" + rs.getString(0));
	    }
            rs.close();
	    con.close();
	    if (count != 3) {
	       throw new Exception("test failed because the exception count was " + count);
	    }
        } catch (Throwable e) {
            System.out.println("Exception occurred: " + e.toString());
	    passed = false;
            stat.addStatus("simple queue test", stat.FAIL);
        } finally {
            if (queueConnection != null) {
                try {
                    queueConnection.close();
                } catch (JMSException e) {}
            } // if
            if (passed) stat.addStatus("simple queue test", stat.PASS);
            stat.printSummary("simpleTopicTest");
            System.exit(0);
        } // finally
    } // main
} // class

