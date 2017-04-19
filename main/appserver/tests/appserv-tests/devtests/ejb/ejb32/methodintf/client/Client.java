package client.methodintf;

import javax.ejb.*;
import javax.naming.*;

import ejb32.methodintf.St;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    InitialContext context;

    public static void main(String args[]) { 
        stat.addDescription("ejb32-methodintf");

        try {
            St stles = (St) new InitialContext().lookup("java:global/ejb32-methodintf-ejb/StlesEJB");
            St stful = (St) new InitialContext().lookup("java:global/ejb32-methodintf-ejb/StfulEJB");
            stles.test();
            stful.test();
            System.out.println("Waiting timer to expire to verify the results");
            Thread.sleep(3000);
            boolean pass = stles.verify() && stful.verify(); 
            stat.addStatus("ejb32-methodintf: ", ((pass)? stat.PASS : stat.FAIL) );

        } catch(Exception e) {
            stat.addStatus("ejb32-methodintf: ", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary("ejb32-methodintf");
    }

}
