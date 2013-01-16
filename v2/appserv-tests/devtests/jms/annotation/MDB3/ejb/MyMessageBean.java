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
import javax.jms.JMSConnectionFactoryDefinition;
import javax.jms.JMSContext;
import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSSessionMode;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

@JMSConnectionFactoryDefinition(
    description = "global-scope CF defined by @JMSConnectionFactoryDefinition",
    name = "java:global/env/annotation_CF",
    className = "javax.jms.ConnectionFactory",
    resourceAdapterName = "jmsra",
    user = "admin",
    password = "admin",
    properties = {"org.glassfish.connector-connection-pool.transaction-support=XATransaction"},
    minPoolSize = 0
)

@JMSDestinationDefinition(
    description = "global-scope test queue defined by @JMSDestinationDefinition",
    name = "java:global/env/annotation_testQueue",
    className = "javax.jms.Queue",
    resourceAdapterName = "jmsra",
    destinationName = "myPhysicalTestQueue"
)

@MessageDriven(mappedName = "java:global/env/annotation_testQueue", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "endpointExceptionRedeliveryAttempts", propertyValue = "1")
})
public class MyMessageBean implements MessageListener {
    private static final Logger logger = Logger.getLogger(MyMessageBean.class.getName());

    @Resource(mappedName = "java:global/env/annotation_resultQueue")
    private Queue resultQueue;

    @Resource(mappedName = "java:global/env/annotation_CF")
    private ConnectionFactory myConnectionFactory;

    @Inject
    @JMSConnectionFactory("java:global/env/annotation_CF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    public MyMessageBean() {
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                sendMessage(true, ((TextMessage) message).getText());
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
