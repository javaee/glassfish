/**
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.devtest.security.plogin.converter.client;

import java.util.Properties;
import java.math.BigDecimal;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.transaction.UserTransaction;
import com.sun.appserv.security.*;
import com.sun.devtest.security.plogin.converter.ejb.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 * A simple java client. This uses the services provided by the <code>ConverterBean</code> and
 * converts 100 US dollars to Yen and 100 Yen to Euro. 
 * <p>In this regard, it does the following in order
 * <ul>
 * <li>Locates the home interface of the enterprise bean
 * <li>Gets a reference to the remote interface
 * <li>Invokes business methods
 * </ul>
 * <br>
 * <b>Locating the home interface:</b>
 * <blockquote><pre>
 *	Context initial = new InitialContext();
 *	Context myEnv = (Context)initial.lookup("java:comp/env");
 *	Object objref = myEnv.lookup("ejb/SimpleConverter");
 *  ConverterHome home = (ConverterHome)PortableRemoteObject.narrow(objref, ConverterHome.class);
 * </pre></blockquote>
 * <br>
 * <b>Creating the remote interface:</b>
 * <blockquote><pre>
 *	Converter currencyConverter = home.create();
 * </pre></blockquote>
 * <br>
 * <b>Invoking business methods:</b>
 * <blockquote><pre>
 *  BigDecimal param = new BigDecimal ("100.00");
 *	amount = currencyConverter.dollarToYen(param);
 *  amount = currencyConverter.yenToEuro(param);
 * </pre></blockquote>
 * <br>
 * <b>Output:</b>
 * <pre>
 * 12160.00
 * 0.77
 * </pre>
 *
 *
 */

public class ConverterClient {

    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

    ConverterClient() {
    }

   /**
    * The main method of the client. This invokes the <code>ConverterBean</code> to use
    * its services. It then asks the bean to convert 100 dollars to yen and
    * 100 yen to euro. The results are printed at the terminal where the client is run.
    * See <code>appclient</code> documentation in SunONE app server to run the clinet.
    *
    */
    public static void main(String[] args) {
	ConverterClient client = new ConverterClient();
	client.run(args);
    }

    private void run(String[] args) {
        String url = null;
	String testIdPrefix = null;
	String testId = "";
        String jndiName = null;
        Context context = null;
        String ctxFactory = null;
	java.lang.Object obj = null;
        try {
            stat.addDescription("Security::client side programmatic login");

            if (args.length == 3) {
                url = args[0];
                ctxFactory = args[1];
		jndiName = args[2];
            }

            String user = "shingwai";
            String password = "shingwai";

            ProgrammaticLogin plogin = new ProgrammaticLogin();
            plogin.login(user, password);
            boolean isAppClientTest = (url == null) || (ctxFactory == null);
            
            /*
             * User tx is to test issue:
             * https://glassfish.dev.java.net/issues/show_bug.cgi?id=1568
             */
            UserTransaction ut = null;

            if (isAppClientTest) {
		testIdPrefix = "Sec::PLogin Converter Sample AppClient";
                testId = testIdPrefix;
                // Initialize the Context with default properties
                context = new InitialContext();
                System.out.println("PLogin Test \nDefault Context Initialized...");
                // Create Home object
                obj = context.lookup("java:comp/env/ejb/PLoginSimpleConverter");
                ut = (UserTransaction) context.lookup("UserTransaction");
            } else {
		testIdPrefix = "Sec::PLogin Standalone-Client";
                testId = testIdPrefix;
                Properties env = new Properties();
                env.put("java.naming.provider.url", url);
                env.put("java.naming.factory.initial", ctxFactory);
                // Initialize the Context with JNDI specific properties
                context = new InitialContext(env);
                System.out.println("Context Initialized with " +
                                   "URL: " + url + ", Factory: " + ctxFactory);
                // Create Home object
                obj = context.lookup(jndiName);
                
                ut = (UserTransaction) context.lookup("UserTransaction");
            }
            ConverterRemoteHome home =
               (ConverterRemoteHome) PortableRemoteObject.narrow(obj,
                                            ConverterRemoteHome.class);

            ConverterRemote currencyConverter = home.create();
            
            System.out.println("\n\n\n===========Beginning Simple Test=====\n\n");
            testId = testIdPrefix + " Programmatic Login";
            processRequest(home, "100");
            processRequest(home, "200");
            processRequest(home, "300", ut);
            stat.addStatus(testId, stat.PASS);
            try {
                System.out.println("Logout ...");
                testId = testIdPrefix + " Programmatic Logout";
                plogin.logout();
                if (!isAppClientTest) {
                    //in case of appclient, this will prompt for password in GUI
                    processRequest(home, "400");
                    stat.addStatus(testId, stat.FAIL);
                    System.out.println("Unauthorized Access to Converter!");
                }
            } catch(java.rmi.AccessException ae) {
                stat.addStatus(testId, stat.PASS);
                System.out.println("OK! Got an expected java.rmi.AccessException");
            }
            
            testId = testIdPrefix + " Programmatic Relogin after logout";
            plogin.login(user, password);
            processRequest(home, "500");
            processRequest(home, "600");
            stat.addStatus(testId, stat.PASS);

            try {
                System.out.println("Incorrect Login ...");
                testId = testIdPrefix + " Programmatic Incorrect login";
                plogin.login(user, password + "123");
                processRequest(home, "700");
                stat.addStatus(testId, stat.FAIL);
                System.out.println("Unauthorized Access to Converter!");
            } catch(java.rmi.AccessException ae) {
                stat.addStatus(testId, stat.PASS);
                System.out.println("OK! Got an expected java.rmi.AccessException");
            }

            testId = testIdPrefix + " Programmatic Relogin after fail login";
            plogin.login(user, password);
            processRequest(home, "800");
            stat.addStatus(testId, stat.PASS);
        } catch (Throwable ex) {
	    System.err.println("TestID" +testId);
            stat.addStatus(testId, stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        } finally {
            stat.printSummary();
        }
    }

    // default method that does not require transaction
    private void processRequest(ConverterRemoteHome home, String amt)
        throws Exception {
        
        processRequest(home, amt, null);
    }
    
    private void processRequest(ConverterRemoteHome home, String amt,
        UserTransaction tx) throws Exception {

        ConverterRemote currencyConverter = home.create();
            
        String s = currencyConverter.myCallerPrincipal();            
        System.out.println(" The caller principal received from ejb ->"+s);
            
        BigDecimal param = new BigDecimal (amt);
        if (tx != null) {
            System.out.println("Beginning user transaction");
            tx.begin();
        }
        
        BigDecimal amount = currencyConverter.dollarToYen(param);
        System.out.println("$" + amt + " is : "+amount+"Yen");
        amount = currencyConverter.yenToEuro(param);
        System.out.println("Yen is :"+amount+"Euro\n");
        
        if (tx != null) {
            System.out.println("Committing user transaction");
            tx.commit();
        }
    }
    
}
