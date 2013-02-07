package org.glassfish.test.jms.mdbdest.ejb;

import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;

/**
 *
 * @author LILIZHAO
 */
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
    public int checkMessage(String text, int expectedCount) {
        Connection conn = null;
        Session session = null;
        int count = 0;
        try {
            conn = myQueueFactory.createConnection();
            conn.start();
            session = conn.createSession();

            MessageConsumer consumer = session.createConsumer(resultQueue);
            while (count < expectedCount) {
                TextMessage msg = (TextMessage) consumer.receive(120000);
                if (msg != null)
                    count ++;
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
            }catch(Exception e) {
                throw new EJBException(e);
            }
        }
        return count;
    }
}
