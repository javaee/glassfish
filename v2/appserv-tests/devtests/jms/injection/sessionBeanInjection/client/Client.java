package org.glassfish.test.jms.injection.client;

import javax.naming.*;
import javax.jms.*;
import org.glassfish.test.jms.injection.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        STAT.addDescription("jms-injection-stateless-ejb");
        Client client = new Client(args);
        client.doTest();
        STAT.printSummary("jms-injection-stateless-ejbID");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        String ejbName = "SessionBeanInjection";
        String text = "Hello World!";
        try {
            Context ctx = new InitialContext();
            SessionBeanInjectionRemote beanRemote = (SessionBeanInjectionRemote) ctx.lookup(SessionBeanInjectionRemote.RemoteJNDIName);
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