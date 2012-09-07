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
    public boolean checkMessage(String[] texts) {
        try {
            JMSConsumer consumer = jmsContext.createConsumer(queue);
            boolean[] found = new boolean[texts.length];
            for (int i=0; i<texts.length; i++) {
                Message msg = consumer.receive(30000L);
                if (msg instanceof TextMessage) {
                    String content = ((TextMessage) msg).getText();
                    for (int j=0; j<texts.length; j++) {
                        if (!found[j] && texts[i].equals(content))
                            found[j] = true;
                            break;
                    }
                }
            }
            boolean result = true;
            for (int i=0; i<found.length; i++)
                result = result & found[i];
            return result;
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }
}
