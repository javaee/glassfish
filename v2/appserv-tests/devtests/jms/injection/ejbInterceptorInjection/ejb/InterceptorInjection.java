package org.glassfish.test.jms.injection.ejb;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.interceptor.*;
import javax.ejb.*;
import javax.jms.*;

/**
 *
 * @author LILIZHAO
 */
@Stateless(mappedName="InterceptorInjection/remote")
public class InterceptorInjection implements InterceptorInjectionRemote {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @Override
    @Interceptors(MyInterceptor.class) 
    public void sendMessage(String text) {
    }

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
