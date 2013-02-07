package org.glassfish.test.jms.injection.ejb;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;
import javax.transaction.UserTransaction;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import java.lang.SecurityException;
import java.lang.IllegalStateException;


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

    @Inject 
    UserTransaction ut;
    
    private static String transactionScope = "around TransactionScoped";
    private static String preIdentical = "fingerPrint";

    public Boolean sendMessage(String text) {

        String context1 = "";
        String context2 = "";
        String context3 = "";
        String context4 = "";
        try {
            ut.begin();
            TextMessage msg = jmsContext.createTextMessage(text);
            
            jmsContext.createProducer().send(queue, msg);
            context1 = jmsContext.toString();
            System.out.println("JMSContext1:"+jmsContext.toString());
            
            jmsContext.createProducer().send(queue, msg);
            context2 = jmsContext.toString();
            System.out.println("JMSContext2:"+jmsContext.toString());

            ut.commit();
            ut.begin();

            jmsContext.createProducer().send(queue, msg);
            context3 = jmsContext.toString();
            System.out.println("JMSContext3:"+jmsContext.toString());
            
            jmsContext.createProducer().send(queue, msg);
            context4 = jmsContext.toString();
            System.out.println("JMSContext4:"+jmsContext.toString());
            ut.commit();

            return checkScope(context1, context2, context3, context4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boolean checkScope(String context1, String context2, String context3, String context4){
				
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
        
        if (context3.indexOf(transactionScope) != -1){
            System.out.println("The context variables used in the third call are in transaction scope.");
        }else{
            System.out.println("The context variables used in the third call are NOT in transaction scope.");
            return false;
        }
        
        if (context4.indexOf(transactionScope) != -1){
            System.out.println("The context variables used in the fourth call are in transaction scope.");
        }else{
            System.out.println("The context variables used in the fourth call are NOT in transaction scope.");
            return false;
        }
        
        String context1Annotation = context1.substring(context1.indexOf(preIdentical),context1.indexOf(transactionScope));
        String context2Annotation = context2.substring(context2.indexOf(preIdentical),context2.indexOf(transactionScope));
        String context3Annotation = context3.substring(context3.indexOf(preIdentical),context3.indexOf(transactionScope));
        String context4Annotation = context4.substring(context4.indexOf(preIdentical),context3.indexOf(transactionScope));
        
        if(context1Annotation.equals(context2Annotation)){
            System.out.println("The context variables in the first and second calls to context.send() injected are using identical annotations.");
                if(context1Annotation.equals(context3Annotation)){
                    System.out.println("The context variables in the first and second and third calls to context.send() injected are using identical annotations.");
                        if(context1Annotation.equals(context4Annotation)){
                            System.out.println("The context variables in thest four calls to context.send() injected are using identical annotations.");
                        }else{
                            System.out.println("The context variables in the first and fourth calls to context.send() injected are not using identical annotations.");
                            return false;
                        }
                }else{
                    System.out.println("The context variables in the first and third calls to context.send() injected are not using identical annotations.");
                    return false;
                }
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
        
        if (context3.substring(context3.indexOf(transactionScope)).equals(context4.substring(context4.indexOf(transactionScope)))){
            System.out.println("The context variables used in the third and fourth calls to context.send() take place in the same transaction.");
        }else{
            System.out.println("The context variables used in the third and fourth calls to context.send() take place in the different transaction.");
            return false;
        }
        
        if (context1.substring(context1.indexOf(transactionScope)).equals(context3.substring(context3.indexOf(transactionScope)))){
            System.out.println("The context variables used in the first and third calls to context.send() take place in the same transaction.");
            return false;
        }else{
            System.out.println("The context variables used in the first and third calls to context.send() take place in the different transaction.");
        }
        return true;
    }
}
