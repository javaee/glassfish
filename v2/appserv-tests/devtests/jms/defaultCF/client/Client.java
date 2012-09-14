package org.glassfish.test.jms.defaultcf.client;

import javax.naming.*;
import javax.jms.*;
import org.glassfish.test.jms.defaultcf.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        STAT.addDescription("jms-default-connection-factory-ejb");
        Client client = new Client(args);
        client.doTest();
        STAT.printSummary("jms-default-connection-factory-ejbID");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        String ejbName = "SessionBeanDefault";
        String text = "Hello World Default!";
        try {
            Context ctx = new InitialContext();
            SessionBeanDefaultRemote beanRemote = (SessionBeanDefaultRemote) ctx.lookup(SessionBeanDefaultRemote.RemoteJNDIName);
            beanRemote.sendMessage(text);
            STAT.addStatus("jms-default-connection-factory-ejb " + ejbName, STAT.PASS);
            boolean received = beanRemote.checkMessage(text);
            if (received)
                STAT.addStatus("jms-default-connection-factory-ejb " + ejbName, STAT.PASS);
            else
                STAT.addStatus("jms-default-connection-factory-ejb " + ejbName, STAT.FAIL);
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus("jms-default-connection-factory-ejb " + ejbName, STAT.FAIL);
        }
    }
}