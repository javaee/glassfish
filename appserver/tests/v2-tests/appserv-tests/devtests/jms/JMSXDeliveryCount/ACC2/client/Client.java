package org.glassfish.test.jms.jmsxdeliverycount.client;

import javax.naming.*;
import javax.jms.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        STAT.addDescription("JMSXDeliveryCount-acc1-2");
        Client client = new Client(args);
        client.sendMessages();
        client.doTest();
        STAT.printSummary("JMSXDeliveryCount-acc1-2ID");
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
            STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
        }
    }

    public void doTest() {
        try {
            Context ctx = new InitialContext();
            Queue queue = (Queue) ctx.lookup("jms/jms_unit_test_Queue");
            QueueConnectionFactory qconFactory = (QueueConnectionFactory) ctx.lookup("jms/jms_unit_test_QCF");
            QueueConnection qcon = qconFactory.createQueueConnection();
            qcon.start();
            QueueSession qsession = qcon.createQueueSession(true, Session.SESSION_TRANSACTED);
            MessageConsumer qreceiver = qsession.createConsumer(queue);
            TextMessage message = (TextMessage) qreceiver.receive(10000);
            if (message == null) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount != 1) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            qreceiver.close();
            qsession.rollback();

            MessageConsumer qreceiver1 = qsession.createConsumer(queue);
            message = (TextMessage) qreceiver1.receive(10000);
            if (message == null) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount != 2) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            qreceiver1.close();
            qsession.rollback();

            MessageConsumer qreceiver2 = qsession.createConsumer(queue);
            message = (TextMessage) qreceiver2.receive(1000);
            if (message == null) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount != 3) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            qsession.commit();

            for (int i=1; i<10; i++) {
                message = (TextMessage) qreceiver2.receive(1000);
                if (message == null) {
                    STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                    return;
                }
                deliveryCount = message.getIntProperty("JMSXDeliveryCount");
                if (deliveryCount != 1) {
                    STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                    return;
                }
                qsession.commit();
            }
            qcon.close();
            STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
        }
    }
}