package com.acme;

import org.glassfish.tests.ejb.autoclose.SimpleEjb;

import java.util.Map;
import java.util.HashMap;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        stat.printSummary(appName + "ID");
        //System.exit(0);

    }

    private void test(String module) {

        boolean res = true;
        try (EJBContainer c = EJBContainer.createEJBContainer()) {
            Context ic = c.getContext();
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/" + module + "/SimpleEjb");
            String result = ejb.saySomething();
            System.out.println("EJB said in try-with-resource: " + result);
        } catch (Exception e) {
            res  = false;
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
        }

        // Try again after it was suppose to be closed
        System.out.println(".....Verifying auto-close closed ........");
        EJBContainer c = null;
        try {
            c = EJBContainer.createEJBContainer();
            Context ic = c.getContext();
            SimpleEjb ejb = (SimpleEjb) ic.lookup("java:global/" + module + "/SimpleEjb");
            String result = ejb.saySomething();
            System.out.println("EJB said in try-without-resource: " + result);
        } catch (Exception e) {
            res  = false;
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
        }
        stat.addStatus("EJB embedded with autoclosable", ((res)? stat.PASS : stat.FAIL));
    }

}
