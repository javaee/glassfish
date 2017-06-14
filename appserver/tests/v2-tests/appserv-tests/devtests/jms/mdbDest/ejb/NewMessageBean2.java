package org.glassfish.test.jms.mdbdest.ejb;

import java.util.logging.*;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;

@MessageDriven(name = "test2", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "jms_unit_test_Topic")
})
public class NewMessageBean2 implements MessageListener {
    private static final Logger logger = Logger.getLogger(NewMessageBean.class.getName());
    
    private static int count;
    
    @Resource
    private MessageDrivenContext mdc;

    @Resource(mappedName = "jms/jms_unit_result_Queue")
    private Queue resultQueue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;
    
    public NewMessageBean2() {
    }
    
    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message message) {
        JMSProducer producer = jmsContext.createProducer();
        TextMessage tmsg = jmsContext.createTextMessage("Received: " + this.getClass().getName());
        producer.send(resultQueue, tmsg);
        System.out.println(this.getClass().getName() + " sent message!!!");
    }
}
