package org.glassfish.test.jms.activationproperties.ejb;

import java.util.logging.*;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;

@MessageDriven(mappedName = "jms/jms_unit_test_Topic", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "subscriptionScope", propertyValue = "Cluster"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable")
})
public class NewMessageBean2 implements MessageListener {
    private static final Logger logger = Logger.getLogger(NewMessageBean2.class.getName());
    
    @Resource
    private MessageDrivenContext mdc;

    @Resource(mappedName = "jms/jms_unit_result_Queue")
    private Queue resultQueue;

    @Resource(mappedName = "jms/jms_unit_test_QCF")
    private QueueConnectionFactory qconFactory;
    
    public NewMessageBean2() {
    }
    
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message message) {
        sendMsg(message);
    }
    
    private void sendMsg(Message msg) {
        QueueConnection qcon = null;
        QueueSession qsession = null;
        QueueSender qsender = null;
        try {
            qcon = qconFactory.createQueueConnection();
            qcon.start();
            qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            qsender = qsession.createSender(resultQueue);
            TextMessage message = qsession.createTextMessage();
            message.setText("Hello World!");
            qsender.send(message);
        } catch (Exception e) {
            throw new EJBException(e);
        } finally {
            try {
                if (qsender != null)
                    qsender.close();
                if (qsession != null)
                    qsession.close();
                if (qcon != null)
                    qcon.close();
            } catch (Exception e) {
                throw new EJBException(e);
            }
        }
    }
}
