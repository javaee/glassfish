package org.glassfish.test.jms.injection.ejb;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;
import javax.transaction.UserTransaction;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
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
    
    private static String requestScope = "around RequestScoped";
    private static String transactionScope = "around TransactionScoped";
    private static String preIdentical = "fingerPrint";

    public Boolean sendMessage(String text) {

        String context1 = "";
        String context2 = "";
        String context3 = "";
        String context4 = "";
        String context5 = "";
        try {
            TextMessage msg = jmsContext.createTextMessage(text);
            
            jmsContext.createProducer().send(queue, msg);
            context1 = jmsContext.toString();
            System.out.println("JMSContext1:"+context1);
            
            jmsContext.createProducer().send(queue, msg);
            context2 = jmsContext.toString();
            System.out.println("JMSContext2:"+context2);

            ut.begin();
            jmsContext.createProducer().send(queue, msg);
            context3 = jmsContext.toString();
            System.out.println("JMSContext3:"+context3);
            ut.commit();
            
            jmsContext.createProducer().send(queue, msg);
            context4 = jmsContext.toString();
            System.out.println("JMSContext4:"+context4);

            jmsContext.createProducer().send(queue, msg);
            context5 = jmsContext.toString();
            System.out.println("JMSContext5:"+context5);

            return checkScope(context1, context2, context3, context4, context5);
        } catch (Exception e) {
            e.printStackTrace();
            try{
                ut.rollback();
            }catch(Exception e1){
                e1.printStackTrace();
            }
        }
        return false;
    }

    public Boolean checkScope(String context1, String context2, String context3, String context4, String context5){

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
        
        if (context3.indexOf(transactionScope) != -1){
            System.out.println("The context variables used in the third call are in transaction scope.");
        }else{
            System.out.println("The context variables used in the third call are NOT in transaction scope.");
            return false;
        }
        
        if (context4.indexOf(requestScope) != -1){
            System.out.println("The context variables used in the fourth call are in request scope.");
        }else{
            System.out.println("The context variables used in the fourth call are NOT in request scope.");
            return false;
        }
            
        if (context5.indexOf(requestScope) != -1){
            System.out.println("The context variables used in the fifth call are in request scope.");
        }else{
            System.out.println("The context variables used in the fifth call are NOT in request scope.");
            return false;
        }
        
        String context1Annotation = context1.substring(context1.indexOf(preIdentical),context1.indexOf(requestScope));
        String context2Annotation = context2.substring(context2.indexOf(preIdentical),context2.indexOf(requestScope));
        String context3Annotation = context3.substring(context3.indexOf(preIdentical),context3.indexOf(transactionScope));
        String context4Annotation = context4.substring(context4.indexOf(preIdentical),context4.indexOf(requestScope));
        String context5Annotation = context5.substring(context5.indexOf(preIdentical),context5.indexOf(requestScope));
        
        if(context1Annotation.equals(context2Annotation)){
            System.out.println("The context variables in the first and second calls to context.send() injected are using identical annotations.");
            if(context1Annotation.equals(context3Annotation)){
                System.out.println("The context variables in the first,second and third calls to context.send() injected are using identical annotations.");
                if(context1Annotation.equals(context4Annotation)){
                    System.out.println("The context variables in the first,second,third and fourth calls to context.send() injected are using identical annotations.");
                    if(context1Annotation.equals(context5Annotation)){
                        System.out.println("The context variables in the first,second,third,fourth and fifth calls to context.send() injected are using identical annotations.");
                    }else{
                        System.out.println("The context variables in the first and fifth calls to context.send() injected are not using identical annotations.");
                        return false;
                    }
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

        if (context1.substring(context1.indexOf(requestScope)).equals(context2.substring(context2.indexOf(requestScope)))){
            System.out.println("The context variables used in the first and second calls to context.send() take place in the same request.");
        }else{
            System.out.println("The context variables used in the first and second calls to context.send() take place in the different request.");
            return false;
        }
        
        if (context4.substring(context4.indexOf(requestScope)).equals(context5.substring(context5.indexOf(requestScope)))){
            System.out.println("The context variables used in the fourth and fifth calls to context.send() take place in the same request.");
        }else{
            System.out.println("The context variables used in the fourth and fifth calls to context.send() take place in the different request.");
            return false;
        }

        if (context1.substring(context1.indexOf(requestScope)).equals(context4.substring(context4.indexOf(requestScope)))){
            System.out.println("The context variables used in the first,second,fourth and fifth calls to context.send() take place in the same request.");
        }else{
            System.out.println("The context variables used in the first and fourth calls to context.send() take place in the different request.");
            return false;
        }

        return true;
    }
}
