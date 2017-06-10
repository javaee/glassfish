package org.glassfish.test.jms.injection.client;

import javax.naming.*;
import javax.jms.*;
import org.glassfish.test.jms.injection.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");
    private static String transactionScope = "around TransactionScoped";
    private static String preIdentical = "fingerPrint";

    public static void main (String[] args) {
        STAT.addDescription("Use case A: Two methods on the same bean within separate transactions.");
        Client client = new Client(args);
        client.doTest();
        STAT.printSummary();
    }

    public Client (String[] args) {
    }

    public void doTest() {
        String ejbName = "jms-injection-ejb-jmsContextInjectionA";
        String text = "Hello World!";
        try {
            Context ctx = new InitialContext();
            SessionBeanInjectionRemote beanRemote = (SessionBeanInjectionRemote) ctx.lookup(SessionBeanInjectionRemote.RemoteJNDIName);
            String context1 = beanRemote.sendMessage1(text);
            String context2 = beanRemote.sendMessage2(text);
            System.out.println("context1:"+context1);
            System.out.println("context1:"+context2);
            
            if (context1.indexOf(transactionScope) != -1){
                System.out.println("The context variables used in the first call are in transaction scope.");
            }else{
                System.out.println("TThe context variables used in the first call are NOT in transaction scope.");
                STAT.addStatus(ejbName, STAT.FAIL);
                return ;
            }
            
            if (context2.indexOf(transactionScope) != -1){
                 System.out.println("The context variables used in the second call are in transaction scope.");
            }else{
                System.out.println("The context variables used in the second call are NOT in transaction scope.");
                STAT.addStatus(ejbName, STAT.FAIL);
                return ;
            }
            
            String context1Annotation = context1.substring(context1.indexOf(preIdentical),context1.indexOf(transactionScope));
            String context2Annotation = context2.substring(context2.indexOf(preIdentical),context2.indexOf(transactionScope));
            
            if(context1Annotation.equals(context2Annotation)){
                System.out.println("The context variables in the first and second calls to context.send() injected are using identical annotations.");
            }else{
                System.out.println("The context variables in the first and second calls to context.send() injected are not using identical annotations.");
                STAT.addStatus(ejbName, STAT.FAIL);
                return ;
            }
            
            if (context1.substring(context1.indexOf(transactionScope)).equals(context2.substring(context2.indexOf(transactionScope)))) {
                System.out.println("The context variables used in the first and second calls to context.send() take place in the same transaction.");
                STAT.addStatus(ejbName, STAT.FAIL);
                return ;
            }else{
                System.out.println("The context variables used in the first and second calls to context.send() take place in the different transaction.");
            }
            STAT.addStatus(ejbName, STAT.PASS);
            

        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus(ejbName, STAT.FAIL);
        }
    }
    
}