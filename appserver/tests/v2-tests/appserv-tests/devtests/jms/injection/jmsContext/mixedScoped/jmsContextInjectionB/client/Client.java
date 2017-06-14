package org.glassfish.test.jms.injection.client;

import javax.naming.*;
import javax.jms.*;
import org.glassfish.test.jms.injection.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        STAT.addDescription("Use case B: Two methods on the same bean within the same transaction.");
        Client client = new Client(args);
        client.doTest();
        STAT.printSummary();
    }

    public Client (String[] args) {
    }

    public void doTest() {
        String ejbName = "jms-injection-ejb-jmsContextInjectionB";
        String text = "Hello World!";
        try {
            Context ctx = new InitialContext();
            SessionBeanInjectionRemote1 beanRemote = (SessionBeanInjectionRemote1) ctx.lookup(SessionBeanInjectionRemote1.RemoteJNDIName);
            Boolean flag = beanRemote.sendMessage(text);

            if(flag == true)
            {
                STAT.addStatus(ejbName, STAT.PASS);
            }else
            {
                STAT.addStatus(ejbName, STAT.FAIL);
            }
            
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus(ejbName, STAT.FAIL);
        }
    }
    
}