package org.glassfish.test.jms.mdbdest.client;

import javax.naming.*;
import javax.jms.*;
import org.glassfish.test.jms.mdbdest.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private final static SimpleReporterAdapter STAT = new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {
        STAT.addDescription("mdbDestID");
        Client client = new Client(args);
        client.doTest();
        STAT.printSummary("mdbDestID");
    }

    public Client (String[] args) {
    }

    public void doTest() {
        String ejbName = "MySessionBean";
        String text = "Hello World!";
        int count = 0;
        int expectedCount = 6;
        try {
            Context ctx = new InitialContext();
            MySessionBeanRemote beanRemote = (MySessionBeanRemote) ctx.lookup(MySessionBeanRemote.RemoteJNDIName);
            beanRemote.sendMessage(text);

            count = beanRemote.checkMessage(text, expectedCount);
            if (count == expectedCount)
                STAT.addStatus("mdbDestID", STAT.PASS);
            else {
                System.out.println("Got " + count + " messages, but " + expectedCount + " are expected.");
                STAT.addStatus("mdbDestID", STAT.FAIL);
            }
        } catch(Exception e) {
            e.printStackTrace();
            STAT.addStatus("mdbDestID " + ejbName, STAT.FAIL);
        }
    }
}