package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.naming.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    private final static String[] nonPortableJndiNames = {"HH", "HH#com.acme.Hello"};

    public static void main(String args[]) {

	appName = args[0]; 
	stat.addDescription(appName);
	Client client = new Client(args);       
        client.doTest();	
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {}

    public void doTest() {
        boolean failed = false;
        StringBuilder sb = new StringBuilder();
	try {
            InitialContext ic = new InitialContext();
	    // non-portable global
            for(String s : nonPortableJndiNames) {
                try {
                    sb.append("About to lookup " + s);
                    Hello hello = (Hello) ic.lookup(s);
                    sb.append("Expecting lookup to fail, but got " + hello);
                    failed = true;
                } catch (NamingException e) {
                    sb.append("Got expected " + e);
                }
            }
            System.out.println(sb);
	    stat.addStatus("disable_nonportable_jndi", (failed ? stat.FAIL: stat.PASS));
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }
}
