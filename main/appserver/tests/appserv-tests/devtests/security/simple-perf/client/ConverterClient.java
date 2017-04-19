/**
 * Copyright � 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.security.ssl.converter.client;

import java.util.Properties;
import java.math.BigDecimal;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.s1peqe.security.ssl.converter.ejb.*;

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
	String testId = null;
        String jndiName = null;
        Context context = null;
        String ctxFactory = null;
	java.lang.Object obj = null;
        try {
            stat.addDescription("Security::SSL One way ssl.");

            if (args.length == 3) {
                url = args[0];
                ctxFactory = args[1];
		jndiName = args[2];
            }

            if ( (url == null) || (ctxFactory == null) ) {
		testId = "Sec::Simple Perf Converter Sample AppClient";
                // Initialize the Context with default properties
                context = new InitialContext();
                System.out.println("Simple Perf  Test \n Default Context Initialized...");
                // Create Home object
                obj = context.lookup("java:comp/env/ejb/SSLSimpleConverter");
            } else {
		testId = "Sec::Perf Standalone-Client";
                Properties env = new Properties();
                env.put("java.naming.provider.url", url);
                env.put("java.naming.factory.initial", ctxFactory);
                // Initialize the Context with JNDI specific properties
                context = new InitialContext(env);
                System.out.println("Context Initialized with " +
                                   "URL: " + url + ", Factory: " + ctxFactory);
                // Create Home object
                obj = context.lookup(jndiName);
            }
            String mytimes = "5000";//(String)context.lookup("java:comp/env/times");
            System.out.println("No of times the test is run ->"+mytimes);
            ConverterRemoteHome home =
               (ConverterRemoteHome) PortableRemoteObject.narrow(obj,
                                            ConverterRemoteHome.class);

            ConverterRemote currencyConverter = home.create();
            
            System.out.println("\n\n\n===========Beginning Simple Test=====\n\n");
            
            BigDecimal param = new BigDecimal ("100.00");
            int times = 1; 
            try{
                times = Integer.parseInt(mytimes); 
            } catch(java.lang.NumberFormatException ne){
                System.out.println("Times in descriptor declared incorrectly");
            }
            BigDecimal amount = new BigDecimal(1); 
            BigDecimal amount2 = new BigDecimal(1);
            long starttime = System.currentTimeMillis();
            
            for(int i = 0; i<times; i++){
                amount = currencyConverter.dollarToYen(param);
                amount2 = currencyConverter.yenToEuro(param);
            }
            long endtime = System.currentTimeMillis();
            double avg = (endtime-starttime)/(2.0*times);
            System.out.println("$100 is : "+amount+"Yen");
            System.out.println("Yen is :"+amount2+"Euro");
            System.out.println("Average time to run 1 method = "+ avg);
            stat.addStatus(testId, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(testId, stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        } finally {
	    stat.printSummary(testId);
        }
    }
}
