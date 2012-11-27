package org.glassfish.test.jms.jmsxdeliverycount.client;

import javax.naming.*;
import javax.jms.*;
import org.glassfish.test.jms.jmsxdeliverycount.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        STAT.addDescription("JMSXDeliveryCount-stateless-mdb2");
        Client client = new Client(args);
        client.doTest();
        STAT.printSummary("JMSXDeliveryCount-stateless-mdb2ID");
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

            boolean received = beanRemote.checkMessage(text);
            if (received)
                STAT.addStatus("JMSXDeliveryCount-mdb2 " + ejbName, STAT.PASS);
            else
                STAT.addStatus("JMSXDeliveryCount-mdb2 " + ejbName, STAT.FAIL);
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus("JMSXDeliveryCount-mdb2 " + ejbName, STAT.FAIL);
        }
    }
}