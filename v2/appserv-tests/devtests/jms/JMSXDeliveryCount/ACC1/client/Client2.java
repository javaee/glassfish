package org.glassfish.test.jms.jmsxdeliverycount.client;

import javax.naming.*;
import javax.jms.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client2 {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        STAT.addDescription("JMSXDeliveryCount-acc1-2");
        Client2 client2 = new Client2(args);
        client2.doTest();
        STAT.printSummary("JMSXDeliveryCount-acc1-2ID");
    }

    public Client2 (String[] args) {
    }

    public void doTest() {
        try {
            Context ctx = new InitialContext();
            Queue queue = (Queue) ctx.lookup("jms/jms_unit_test_Queue");
            QueueConnectionFactory qconFactory = (QueueConnectionFactory) ctx.lookup("jms/jms_unit_test_QCF");
            QueueConnection qcon = qconFactory.createQueueConnection();
            qcon.start();
            QueueSession qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer qreceiver = qsession.createConsumer(queue);
            TextMessage message = (TextMessage) qreceiver.receive(10000);
            if (message == null) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount != 3) {
                STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                return;
            }
            for (int i=1; i<10; i++) {
                message = (TextMessage) qreceiver.receive(10000);
                if (message == null) {
                    STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                    return;
                }
                deliveryCount = message.getIntProperty("JMSXDeliveryCount");
                if (deliveryCount != 1) {
                    STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
                    return;
                }
            }
            qcon.close();
            STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus("JMSXDeliveryCount-acc1-2 ", STAT.FAIL);
        }
    }
}