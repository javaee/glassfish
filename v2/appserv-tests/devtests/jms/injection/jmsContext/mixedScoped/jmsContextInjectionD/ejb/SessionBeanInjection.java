package org.glassfish.test.jms.injection.ejb;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;

/**
 *
 * @author LILIZHAO
 */
@Stateless(mappedName="SessionBeanInjection/remote")
@TransactionManagement(TransactionManagementType.CONTAINER)
public class SessionBeanInjection implements SessionBeanInjectionRemote {
    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("jms/jms_unit_test_QCF")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;
    
    private static String transactionScope = "around TransactionScoped";
    private static String preIdentical = "fingerPrint";

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Boolean sendMessage(String text) {
        String context1 = "";
        String context2 = "";
        try {
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(queue, msg);
            context1 = jmsContext.toString();
            System.out.println("JMSContex1:"+jmsContext.toString());
            producer.send(queue, msg);
            context2 = jmsContext.toString();
            System.out.println("JMSContex2:"+jmsContext.toString());
            return checkTransactionScope(context1, context2);
        } catch (Exception e) {
            throw new EJBException(e);
        }
    }

    public Boolean checkTransactionScope(String context1, String context2){

        if (context1.indexOf(transactionScope) != -1){
            System.out.println("The context variables used in the first call are in transaction scope.");
        }else{
            System.out.println("The context variables used in the first call are NOT in transaction scope.");
            return false;
        }
            
        if (context2.indexOf(transactionScope) != -1){
            System.out.println("The context variables used in the second call are in transaction scope.");
        }else{
            System.out.println("The context variables used in the second call are NOT in transaction scope.");
            return false;
        }
        
        String context1Annotation = context1.substring(context1.indexOf(preIdentical),context1.indexOf(transactionScope));
        String context2Annotation = context2.substring(context2.indexOf(preIdentical),context2.indexOf(transactionScope));
        
        if(context1Annotation.equals(context2Annotation)) {
            System.out.println("The context variables in the first and second calls to context.send() injected are using identical annotations.");
        }else{
            System.out.println("The context variables in the first and second calls to context.send() injected are not using identical annotations.");
            return false;
        }
       	
        if (context1.substring(context1.indexOf(transactionScope)).equals(context2.substring(context2.indexOf(transactionScope)))){
            System.out.println("The context variables used in the first and second calls to context.send() take place in the same transaction.");
        }else{
            System.out.println("The context variables used in the first and second calls to context.send() take place in the different transaction.");
            return false;
        }
        return true;
    }
}
