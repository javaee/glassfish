package org.glassfish.test.jms.jmsxdeliverycount.ejb;

import java.util.logging.*;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;

@MessageDriven(mappedName = "jms/jms_unit_test_Queue", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "endpointExceptionRedeliveryAttempts", propertyValue = "1")
})
public class NewMessageBean implements MessageListener {
    private static final Logger logger = Logger.getLogger(NewMessageBean.class.getName());
    
    private static int count;
    
    @Resource
    private MessageDrivenContext mdc;

    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Resource(mappedName = "jms/jms_unit_result_Queue")
    private Queue resultQueue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;
    
    public NewMessageBean() {
    }
    
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void onMessage(Message message) {
        count++;
        try {
            int deliveryCount = message.getIntProperty("JMSXDeliveryCount");
            if (count == 1) {
                if (deliveryCount != 1) {
                    sendMsg(false, "Invalid JMSXDeliveryCount - Got <" + deliveryCount + ">, but expected <1>.");
                    return;
                }
                mdc.setRollbackOnly();
                return;
            } else if (count ==2) {
                if (deliveryCount != 2) {
                    sendMsg(false, "Invalid JMSXDeliveryCount - Got <" + deliveryCount + ">, but expected <2>.");
                    return;
                } else {
                    sendMsg(true, null);
                }
            }
        } catch (JMSException ex) {
            Logger.getLogger(NewMessageBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void sendMsg(boolean success, String msg) {
        JMSProducer producer = jmsContext.createProducer();
        TextMessage tmsg = jmsContext.createTextMessage(success + ":" + msg);
        producer.send(resultQueue, tmsg);
    }
}
