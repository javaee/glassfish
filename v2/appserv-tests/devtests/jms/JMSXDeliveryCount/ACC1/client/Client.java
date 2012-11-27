package org.glassfish.test.jms.jmsxdeliverycount.client;

import javax.naming.*;
import javax.jms.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        STAT.addDescription("JMSXDeliveryCount-acc1-1");
        Client client = new Client(args);
        client.sendMessages();
        client.doTest();
        STAT.printSummary("JMSXDeliveryCount-acc1-1ID");
    }

    public Client (String[] args) {
    }

    public void sendMessages() {
        try {
            Context ctx = new InitialContext();
            Queue queue = (Queue) ctx.lookup("jms/jms_unit_test_Queue");
            QueueConnectionFactory qconFactory = (QueueConnectionFactory) ctx.lookup("jms/jms_unit_test_QCF");
            QueueConnection conn = qconFactory.createQueueConnection();
            conn.start();
            QueueSession qsession = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender qsender = qsession.createSender(queue);
            for (int i=0; i<10; i++) {
                TextMessage msg = qsession.createTextMessage("" + i);
                qsender.send(msg);
            }
            qsession.close();
            conn.close();
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus("JMSXDeliveryCount-acc1-1 ", STAT.FAIL);
        }
    }

    public void doTest() {
        try {
            Context ctx = new InitialContext();
            Queue queue = (Queue) ctx.lookup("jms/jms_unit_test_Queue");
            QueueConnectionFactory qconFactory = (QueueConnectionFactory) ctx.lookup("jms/jms_unit_test_QCF");
            QueueConnection qcon = qconFactory.createQueueConnection();
            qcon.start();
            QueueSession qsession = qcon.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            MessageConsumer qreceiver = qsession.createConsumer(queue);
            TextMessage message = (TextMessage) qreceiver.receive(10000);
            if (message == null) {
                STAT.addStatus("JMSXDeliveryCount-acc1-1 ", STAT.FAIL);
                return;
            }
            int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount != 1) {
                STAT.addStatus("JMSXDeliveryCount-acc1-1 ", STAT.FAIL);
                return;
            }
            qsession.recover();
            message = (TextMessage) qreceiver.receive(10000);
            if (message == null) {
                STAT.addStatus("JMSXDeliveryCount-acc1-1 ", STAT.FAIL);
                return;
            }
            qcon.close();
            STAT.addStatus("JMSXDeliveryCount-acc1-1 ", STAT.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus("JMSXDeliveryCount-acc1-1 ", STAT.FAIL);
        }
    }
}