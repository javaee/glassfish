package org.glassfish.test.jms.injection.client;

import javax.naming.*;
import javax.jms.*;
import org.glassfish.test.jms.injection.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");
    private final static String FACTORY_JNDI_NAME = "jms/ejb_jms_jmsejb_QCF";
    private final static String QUEUE_JNDI_NAME = "jms/ejb_jms_jmsejb_Queue";

    public static void main (String[] args) {
        STAT.addDescription("jms-injection-ejb-interceptor");
        Client client = new Client(args);
        client.doTest();
        STAT.printSummary("jms-injection-ejb-interceptorID");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        String ejbName = "InterceptorInjection";
        String text = "Hello World!";
        try {
            Context ctx = new InitialContext();
            InterceptorInjectionRemote beanRemote = (InterceptorInjectionRemote) ctx.lookup(InterceptorInjectionRemote.RemoteJNDIName);
            beanRemote.sendMessage(text);
            STAT.addStatus("jms-injection-ejb " + ejbName, STAT.PASS);
            boolean received = beanRemote.checkMessage(text);
            if (received)
                STAT.addStatus("jms-injection-ejb " + ejbName, STAT.PASS);
            else
                STAT.addStatus("jms-injection-ejb " + ejbName, STAT.FAIL);
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus("jms-injection-ejb " + ejbName, STAT.FAIL);
        }
    }
}