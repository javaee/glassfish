/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.*;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.transaction.UserTransaction;

/**
 *
 * @author LILIZHAO
 */
@WebService(serviceName = "NewWebService")
public class NewWebService {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @Inject
    UserTransaction ut;

    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "message") String text) {
        String context = "";
        try {
            ut.begin();
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(queue, msg);
            context = jmsContext.toString();
            ut.commit();
            return context;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
