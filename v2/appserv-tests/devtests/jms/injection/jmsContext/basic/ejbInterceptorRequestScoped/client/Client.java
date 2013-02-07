package org.glassfish.test.jms.injection.client;

import javax.naming.*;
import javax.jms.*;
import org.glassfish.test.jms.injection.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        STAT.addDescription("jms-injection-ejb-interceptor(RequestScoped)");
        Client client = new Client(args);
        client.doTest();
        STAT.printSummary();
    }

    public Client (String[] args) {
    }

    public void doTest() {
        String ejbName = "InterceptorInjection(RequestScoped)";
        String text = "Hello World!";
        try {
            Context ctx = new InitialContext();
            InterceptorInjectionRemote beanRemote = (InterceptorInjectionRemote) ctx.lookup(InterceptorInjectionRemote.RemoteJNDIName);
            beanRemote.sendMessage(text);

            boolean flag = beanRemote.checkMessageAndScoped(text);
            if (flag)
                STAT.addStatus(ejbName, STAT.PASS);
            else
                STAT.addStatus(ejbName, STAT.FAIL);
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus(ejbName, STAT.FAIL);
        }
    }
}