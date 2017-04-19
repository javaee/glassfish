package com.sun.s1asdev.ejb.ejb30.interceptors.session.client;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.interceptors.intro.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static @EJB Sless sless;

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-interceptors-session");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-interceptors-sessionID");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {
        try {
            System.out.println("invoking sless");
            String result1 = sless.concatAndReverse("<One>", "<Two>");
            String result2 = sless.concatAndReverse("<One>", "Null");
            String result3 = sless.concatAndReverse("nuLL", "Null");
	    System.out.println("Got : " + result1);
	    System.out.println("Got : " + result2);
	    System.out.println("Got : " + result3);

	    System.out.println("Got : " + sless.plus((byte) 2, (short) 3, 4));
	    System.out.println("Got : " + sless.isGreaterShort(new Short((short) 5), new Long(7)));
            stat.addStatus("local test1" , stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local test1" , stat.FAIL);
        }
    }

}

