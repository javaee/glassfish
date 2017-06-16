package org.glassfish.test.jms.injection.ejb;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;

/**
 *
 * @author JIGWANG
 */
@Stateless(mappedName="SessionBeanInjection2/remote2")
@TransactionManagement(TransactionManagementType.CONTAINER)
public class SessionBeanInjection2 implements SessionBeanInjectionRemote2 {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("java:comp/DefaultJMSConnectionFactory")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String sendMessage(String text) {
        try {
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(queue, msg);
            System.out.println("JMSContext2:"+jmsContext.toString());
            return jmsContext.toString();
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

}
