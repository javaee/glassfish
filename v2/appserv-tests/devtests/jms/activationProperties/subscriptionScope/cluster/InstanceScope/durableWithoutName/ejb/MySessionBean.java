package org.glassfish.test.jms.activationproperties.ejb;

import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;

/**
 *
 * @author LILIZHAO
 */
@Remote
@Stateless(mappedName="MySessionBean/remote")
public class MySessionBean implements MySessionBeanRemote {
    @Resource(mappedName = "jms/jms_unit_test_Topic")
    private Topic topic;

    @Resource(mappedName = "jms/jms_unit_result_Queue")
    private Queue resultQueue;

    @Resource(mappedName = "jms/jms_unit_test_QCF")
    private ConnectionFactory myQueueFactory;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @Resource
    EJBContext ctx;

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void sendMessage(String text) {
        try {
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(topic, msg);
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String checkMessage(String text) {
        Connection conn = null;
        Session session = null;
        int count = 0;
        int expectedCount = 4;
        try {
            conn = myQueueFactory.createConnection();
            conn.start();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer consumer = session.createConsumer(resultQueue);
            TextMessage msg = null;
            while (count < expectedCount) {
                msg = (TextMessage) consumer.receive(300000);
                if (msg != null)
                    count++;
                else
                    break;
            }
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            try {
                if (session != null)
                    session.close();
                if (conn != null)
                    conn.close();
            } catch(Exception e) {
                throw new EJBException(e);
            }
        }
        if (count != expectedCount) {
            String message = expectedCount + " messages are expected, but got " + count;
            Logger.getLogger("MySessionBean").severe(message);
            return "Fail: " + message;
        }
        return "Success: count=" + count;
    }
}
