package org.glassfish.test.jms.injection.ejb;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;
import javax.transaction.UserTransaction;

/**
 *
 * @author JIGWANG
 */
@Stateless(mappedName="SessionBeanInjection/remote")
@TransactionManagement(TransactionManagementType.BEAN)
public class SessionBeanInjection implements SessionBeanInjectionRemote {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    private static String preIdentical = "fingerPrint";
    private static String requestScope = "around RequestScoped";

    public Boolean sendMessage(String text) {

        String context1 = "";
        String context2 = "";
        try {
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(queue, msg);
            context1 = jmsContext.toString();
            System.out.println("JMSContext1:"+jmsContext.toString());
            producer.send(queue, msg);
            context2 = jmsContext.toString();
            System.out.println("JMSContext2:"+jmsContext.toString());
            return checkRequestScope(context1, context2);
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    public Boolean checkRequestScope(String context1, String context2){

        if (context1.indexOf(requestScope) != -1){
            System.out.println("The context variables used in the first call are in request scope.");
        }else{
            System.out.println("The context variables used in the first call are NOT in request scope.");
            return false;
        }
            
        if (context2.indexOf(requestScope) != -1){
            System.out.println("The context variables used in the second call are in request scope.");
        }else{
            System.out.println("The context variables used in the second call are NOT in request scope.");
            return false;
        }
        
        String context1Annotation = context1.substring(context1.indexOf(preIdentical),context1.indexOf(requestScope));
        String context2Annotation = context2.substring(context2.indexOf(preIdentical),context2.indexOf(requestScope));
        
        if(context1Annotation.equals(context2Annotation)){
            System.out.println("The context variables in the first and second calls to context.send() injected are using identical annotations.");
        }else{
            System.out.println("The context variables in the first and second calls to context.send() injected are not using identical annotations.");
            return false;
        }
       	
        if (context1.substring(context1.indexOf(requestScope)).equals(context2.substring(context2.indexOf(requestScope)))){
            System.out.println("The context variables used in the first and second calls to context.send() take place in the same request.");
        }else{
            System.out.println("The context variables used in the first and second calls to context.send() take place in the different request.");
            return false;
        }
        return true;
    }
}
