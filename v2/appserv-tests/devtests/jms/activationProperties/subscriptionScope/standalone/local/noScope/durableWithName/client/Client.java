package org.glassfish.test.jms.activationproperties.client;

import javax.naming.*;
import javax.jms.*;
import org.glassfish.test.jms.activationproperties.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        STAT.addDescription("subscriptionScope-standalone-local-noScopeDurableWithNameID");
        Client client = new Client(args);
        client.doTest();
        STAT.printSummary("subscriptionScope-standalone-local-noScopeDurableWithNameID");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        String ejbName = "MySessionBean";
        String text = "Hello World!";
        try {
            Context ctx = new InitialContext();
            MySessionBeanRemote beanRemote = (MySessionBeanRemote) ctx.lookup(MySessionBeanRemote.RemoteJNDIName);
            beanRemote.sendMessage(text);

            String result = beanRemote.checkMessage(text);
            if (result.startsWith("Success"))
                STAT.addStatus("subscriptionScope-standalone-local-noScopeDurableWithNameID " + ejbName, STAT.PASS);
            else {
                System.out.println(result);
                STAT.addStatus("subscriptionScope-standalone-local-noScopeDurableWithNameID " + ejbName, STAT.FAIL);
            }
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus("subscriptionScope-standalone-local-noScopeDurableWithNameID " + ejbName, STAT.FAIL);
        }
    }
}