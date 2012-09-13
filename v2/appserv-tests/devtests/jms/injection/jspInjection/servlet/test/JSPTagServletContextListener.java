package test;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.*;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class JSPTagServletContextListener implements ServletContextListener {
//    @Resource(mappedName = "jms/jms_unit_test_Queue")
//    private Queue queue;
//
//    @Inject
//    @JMSConnectionFactory("jms/jms_unit_test_QCF")
//    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
//    private JMSContext jmsContext;

    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("contextInitialized...");
//        try {
//            JMSProducer producer = jmsContext.createProducer();
//            TextMessage msg = jmsContext.createTextMessage("Hello Servlet Context Listener");
//            producer.send(queue, msg);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}