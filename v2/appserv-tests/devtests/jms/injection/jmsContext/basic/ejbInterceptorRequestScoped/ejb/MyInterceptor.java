package org.glassfish.test.jms.injection.ejb;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jms.*;
import javax.ejb.*;
import javax.naming.*;

public class MyInterceptor {
    static String context;

    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @AroundInvoke
    public Object sendMsg(InvocationContext ctx) throws Exception {
        Object[] params = ctx.getParameters();
        try {
            lookupQueue();
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage((String) params[0]);
            producer.send(queue, msg);
            context = jmsContext.toString();
        } catch (Exception e) {
            throw new EJBException(e);
        }
        return ctx.proceed();
    }

    private Queue lookupQueue() throws Exception {
        InitialContext ctx = new InitialContext();
        queue = (Queue) ctx.lookup("jms/jms_unit_test_Queue");
        if (queue == null)
            throw new Exception("jms/jms_unit_test_Queue not found.");
        return queue;
    }
}