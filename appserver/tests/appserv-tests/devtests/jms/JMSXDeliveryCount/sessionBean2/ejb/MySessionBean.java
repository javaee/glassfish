package org.glassfish.test.jms.jmsxdeliverycount.ejb;

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
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Resource(mappedName = "jms/jms_unit_test_QCF")
    private ConnectionFactory myQueueFactory;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @Resource
    EJBContext ctx;

    @Override
    public void sendMessage(String text) {
        try {
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(queue, msg);
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    @Override
    public boolean checkMessage1(String text) {
        Connection conn = null;
        Session session = null;
        try {
            conn = myQueueFactory.createConnection();
            conn.start();
            session = conn.createSession();

            MessageConsumer consumer = session.createConsumer(queue);
            TextMessage msg = (TextMessage) consumer.receive(1000);
            if (msg == null) {
                Logger.getLogger("MySessionBean").severe("No message received 1.");
                return false;
            }
            int deliveryCount = msg.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount != 1) {
                Logger.getLogger("MySessionBean").severe("Invalid JMSXDeliveryCount - Got <" + deliveryCount + ">, but expected <1>.");
                return false;
            }
            ctx.setRollbackOnly();
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            try {
                session.close();
            }catch(Exception e) {
                throw new EJBException(e);
            }
        }
        return true;
    }

    @Override
    public boolean checkMessage2(String text) {
        Connection conn = null;
        Session session = null;
        try {
            conn = myQueueFactory.createConnection();
            conn.start();
            session = conn.createSession();

            MessageConsumer consumer = session.createConsumer(queue);
            TextMessage msg = (TextMessage) consumer.receive(1000);
            if (msg == null) {
                Logger.getLogger("MySessionBean").severe("No message received 2.");
                return false;
            }
            int deliveryCount = msg.getIntProperty("JMSXDeliveryCount");
            if (deliveryCount != 2) {
                Logger.getLogger("MySessionBean").severe("Invalid JMSXDeliveryCount - Got <" + deliveryCount + ">, but expected <2>.");
                return false;
            }
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            try {
                session.close();
                conn.close();
            }catch(Exception e) {
                throw new EJBException(e);
            }
        }
        return true;
    }
}
