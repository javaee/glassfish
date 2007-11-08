/**
 * Copyright ¨ 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.ejb.stateless.converter.client;

import java.util.Properties;
import java.math.BigDecimal;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.s1peqe.ejb.stateless.converter.ejb.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class ConverterClient {

    private SimpleReporterAdapter stat = 
            new SimpleReporterAdapter("appserv-tests");

    ConverterClient() {
    }

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
            stat.addDescription("This is to test parser overrideability for EJB jars");

            if (args.length == 3) {
                url = args[0];
                ctxFactory = args[1];
		jndiName = args[2];
            }

            if ( (url == null) || (ctxFactory == null) ) {
		testId = "ParserOverrideablityEJB::Sample AppClient";
                // Initialize the Context with default properties
                context = new InitialContext();
                System.out.println("Default Context Initialized...");
                // Create Home object
                obj = context.lookup("java:comp/env/ejb/SimpleConverter");
            } else {
		testId = "ParserOverrideabilityEJB::Sample Standalone-Client";
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

            ConverterRemoteHome home =
               (ConverterRemoteHome) PortableRemoteObject.narrow(obj,
                                            ConverterRemoteHome.class);

            ConverterRemote currencyConverter = home.create();

            String expectedParser = "org.apache.xerces.jaxp.SAXParserFactoryImpl";
          
            System.out.println("\n\n\n===========Beginning Simple Test=====\n\n");
            String parserFactoryClassName = currencyConverter.getParserFactoryClassName();

            System.out.println("Parser Factory classname in EJB is " +  parserFactoryClassName);
            System.out.println("Expected parser is " + expectedParser);
            if (expectedParser.equals(parserFactoryClassName)) {
               stat.addStatus(testId, stat.PASS);
            } else {
               stat.addStatus(testId, stat.FAIL);
            }
        } catch (Exception ex) {
            stat.addStatus(testId, stat.FAIL);
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        } finally {
	    stat.printSummary(testId);
        }
    }
}
