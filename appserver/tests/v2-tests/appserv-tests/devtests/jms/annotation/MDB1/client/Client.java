package org.glassfish.test.jms.annotation.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.glassfish.test.jms.annotation.ejb.MySessionBeanRemote;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");
    private final static String name = "annotation-mdb1";

    public static void main (String[] args) {
        STAT.addDescription(name);
        Client client = new Client(args);
        client.doTest();
        STAT.printSummary(name + "ID");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        String text = "Hello JMS 2.0!";
        try {
            Context ctx = new InitialContext();
            MySessionBeanRemote beanRemote = (MySessionBeanRemote) ctx.lookup(MySessionBeanRemote.RemoteJNDIName);
            beanRemote.sendMessage(text);

            boolean received = beanRemote.checkMessage(text);
            if (received)
                STAT.addStatus(name, STAT.PASS);
            else
                STAT.addStatus(name, STAT.FAIL);
        } catch(NamingException e) {
            e.printStackTrace();
            STAT.addStatus(name, STAT.FAIL);
        }
    }
}
