package client.intrfaces;

import javax.ejb.*;
import javax.naming.*;

import ejb32.intrfaces.St1;
import ejb32.intrfaces.St2;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    InitialContext context;

    public static void main(String args[]) { 
        stat.addDescription("ejb32-intrfaces");

        try {
            St1 view1 = (St1) new InitialContext().lookup("java:global/ejb32-intrfaces-ejb/SingletonBean!ejb32.intrfaces.St1");
            St2 view2 = (St2) new InitialContext().lookup("java:global/ejb32-intrfaces-ejb/SingletonBean!ejb32.intrfaces.St2");
            boolean pass = view1.st1().equals("st3st1") && view2.st2().equals("st4st2");
            stat.addStatus("ejb32-intrfaces: ", ((pass)? stat.PASS : stat.FAIL) );

        } catch(Exception e) {
            stat.addStatus("ejb32-intrfaces: ", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary("ejb32-intrfaces");
    }

}
