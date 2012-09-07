package org.glassfish.test.jms.injection.ejb;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;

/**
 *
 * @author LILIZHAO
 */
@Stateless(mappedName="MessageReceiverBean/remote")
public class MessageReceiverBean implements MessageReceiverRemote {
    @Resource(mappedName = "jms/ejb_jms_jmsejb_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/ejb_jms_jmsejb_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @Override
    public boolean checkMessage(String text) {
        try {
            JMSConsumer consumer = jmsContext.createConsumer(queue);
            Message msg = consumer.receive(30000L);
            if (msg instanceof TextMessage) {
                String content = ((TextMessage) msg).getText();
                if (text.equals(content))
                    return true;
            }
            return false;
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }
}
