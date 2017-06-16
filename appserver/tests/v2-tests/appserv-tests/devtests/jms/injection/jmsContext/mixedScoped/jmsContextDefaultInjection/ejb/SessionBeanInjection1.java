package org.glassfish.test.jms.injection.ejb;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;
import java.lang.String;
import org.glassfish.test.jms.injection.ejb.SessionBeanInjection2;

/**
 *
 * @author JIGWANG
 */
@Stateless(mappedName="SessionBeanInjection1/remote1")
@TransactionManagement(TransactionManagementType.CONTAINER)
public class SessionBeanInjection1 implements SessionBeanInjectionRemote1 {

    private static String scope = "around";
    private static String preIdentical = "fingerPrint";

    @EJB 
    SessionBeanInjectionRemote2 bean2;

    @Resource(mappedName = "jms/jms_unit_test_Queue")
    private Queue queue;
    
    @Inject
    private JMSContext jmsContext;

    @Resource
    private EJBContext ctx;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Boolean sendMessage(String text) {
        String context1 = "";
        try {
            JMSProducer producer = jmsContext.createProducer();
            TextMessage msg = jmsContext.createTextMessage(text);
            producer.send(queue, msg);
            context1 = jmsContext.toString();
            System.out.println("JMSContext1:"+context1);
        } catch (Exception e) {
            throw new EJBException(e);
        }

        String context2 = bean2.sendMessage(text);

        return checkResult(context1, context2);
    }
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Boolean checkResult(String context1, String context2){

        if (context1.indexOf(preIdentical) == -1 || context1.indexOf(scope) == -1)
            return false;
            
        
        if (context2.indexOf(preIdentical) == -1 || context2.indexOf(scope) == -1)
            return false;
        
        String context1Annotation = context1.substring(context1.indexOf(preIdentical),context1.indexOf(scope));
        String context2Annotation = context2.substring(context2.indexOf(preIdentical),context2.indexOf(scope));
        
        if(context1Annotation.equals(context2Annotation)) {
            System.out.println("Injected using identical annotations.");
        }else{
            System.out.println("Injected not using identical annotations.");
            return false;
        }

        return true;
    }
}
