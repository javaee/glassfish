package org.glassfish.test.jms.annotation.ejb;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSSessionMode;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

@MessageDriven(mappedName = "java:module/env/annotation_testQueue", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "endpointExceptionRedeliveryAttempts", propertyValue = "1")
})
public class MyMessageBean implements MessageListener {
    private static final Logger logger = Logger.getLogger(MyMessageBean.class.getName());

    @Resource(mappedName = "java:module/env/annotation_resultQueue")
    private Queue resultQueue;

    @Resource(mappedName = "java:module/env/annotation_CF")
    private ConnectionFactory myConnectionFactory;

    @Inject
    @JMSConnectionFactory("java:module/env/annotation_CF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    public MyMessageBean() {
    }

    @Override
    public void onMessage(Message message) {
        try {
System.out.println("simon-debug in MyMessageBean - 10");
            if (message instanceof TextMessage) {
System.out.println("simon-debug in MyMessageBean - 20");
                sendMessage(true, ((TextMessage) message).getText());
System.out.println("simon-debug in MyMessageBean - 30");
            } else {
                sendMessage(false, "The received message is not a expected TextMessage.");
            }
        } catch (JMSException ex) {
            Logger.getLogger(MyMessageBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new EJBException(ex);
        }
    }

    private void sendMessage(boolean success, String text) {
        JMSProducer producer = jmsContext.createProducer();
        TextMessage message = jmsContext.createTextMessage(success + ":" + text);
        producer.send(resultQueue, message);
    }
}
