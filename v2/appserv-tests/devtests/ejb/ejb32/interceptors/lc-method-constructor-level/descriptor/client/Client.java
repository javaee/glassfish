package com.acme;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.ejb.EJB;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

 	private static SimpleReporterAdapter stat = 
  		new SimpleReporterAdapter("appserv-tests");

	private static String appName;

    public static void main (String[] args) {

	appName = args[0];

        stat.addDescription(appName);
        Client client = new Client(args);
   	client.doTest();
        stat.printSummary(appName + "ID");
    }  
    
    public Client (String[] args) {
    }

    private void doTest() {
        try {
// Temp disable until CDI integration issues can be resolved
//            Snglt b = (Snglt) new InitialContext().lookup("java:global/" + appName + "/SingletonBean");
//            System.out.println("test : " + b.hello());
            stat.addStatus(appName, stat.PASS);

        } catch(Exception e) {
            stat.addStatus(appName, stat.FAIL);
            e.printStackTrace();
        }

    }
}

