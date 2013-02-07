package org.glassfish.test.jms.injection.client;

import javax.naming.*;
import javax.jms.*;
import org.glassfish.test.jms.injection.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");
    private String TransactionScopeFlag = "";

    public static void main (String[] args) {
        STAT.addDescription("Use case H. A bean which uses a context both outside and within a transaction.");
        Client client = new Client(args);
        client.doTest();
        STAT.printSummary();
    }

    public Client (String[] args) {
    }

    public void doTest() {
        String ejbName = "jms-injection-ejb-jmsContextInjectionH";
        String text = "Hello World!";
        try {
            Context ctx = new InitialContext();
            SessionBeanInjectionRemote beanRemote = (SessionBeanInjectionRemote) ctx.lookup(SessionBeanInjectionRemote.RemoteJNDIName);
            Boolean flag = beanRemote.sendMessage(text);

            if(flag == true){
                STAT.addStatus(ejbName, STAT.PASS);
            }else{
                STAT.addStatus(ejbName, STAT.FAIL);
            }
            
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus(ejbName, STAT.FAIL);
        }
    }
    
}