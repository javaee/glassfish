package com.acme;

import org.glassfish.tests.ejb.mdb.SimpleEjb;

import javax.ejb.*;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String[] s) {
        appName = s[0];
        System.out.println(".......... Testing module: " + appName);
        stat.addDescription(appName);
        Client t = new Client();
        try {
            t.test(appName);
            stat.addStatus("EJB embedded with MDB", stat.PASS);
        } catch (Exception e) {
            e.printStackTrace();
            stat.addStatus("EJB embedded with MDB", stat.FAIL);
        }
        stat.printSummary(appName + "ID");
        System.exit(0);

    }

    private void test(String module) throws Exception {

        EJBContainer c = null;
        try {
            c = EJBContainer.createEJBContainer();
            Context ic = c.getContext();
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/" + module + "/SimpleEjb");
            System.out.println("Invoking EJB...");
            String result = ejb.saySomething();
            System.out.println("EJB said: " + result);

            System.out.println("Waiting for ack...");
            Thread.sleep(3000);
            boolean ack = ejb.getAck();
            System.out.println("Ack: " + ack);
            if (!ack)
                throw new EJBException("MDB wasn't called!!!");

        } finally {
            if (c!=null) c.close();
        }
        System.out.println("Done calling EJB");
    }

}
