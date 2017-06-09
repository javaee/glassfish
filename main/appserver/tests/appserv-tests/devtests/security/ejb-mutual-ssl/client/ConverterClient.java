/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
            stat.addDescription("Security::SSL tester -converter sample.");

            if (args.length == 3) {
                url = args[0];
                ctxFactory = args[1];
		jndiName = args[2];
            }

            if ( (url == null) || (ctxFactory == null) ) {
		testId = "Sec::Mutual_SSL_simple Converter Sample AppClient";
                // Initialize the Context with default properties
                context = new InitialContext();
                System.out.println("Default Context Initialized...");
                // Create Home object
                obj = context.lookup("java:comp/env/ejb/SSLSimpleConverter");
            } else {
		testId = "Sec::Mutual_SSL_simple Standalone-Client";
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
            String mys = "CN=SSLTest, OU=Sun Java System Application Server, O=Sun Microsystems, L=Santa Clara, ST=California, C=US";
            System.out.println("Certname in appclication-client ->"+mys);
            ConverterRemoteHome home =
               (ConverterRemoteHome) PortableRemoteObject.narrow(obj,
                                            ConverterRemoteHome.class);

            ConverterRemote currencyConverter = home.create();
            
            
            String s = currencyConverter.myCallerPrincipal();            
            
            System.out.println(" The caller principal received from ejb ->"+s);
            
            if(!mys.equalsIgnoreCase(s)){
                String error = "The caller principals dont match!! - Test Failed";
                System.out.println(error);
                throw new Exception(error);
            }else{
                System.out.println("Mutual Authentication worked - calling other methods");
            }
            BigDecimal param = new BigDecimal ("100.00");
            BigDecimal amount = currencyConverter.dollarToYen(param);
            System.out.println("\n\n\n===========Beginning Simple Test=====\n\n");
            System.out.println("$100 is : "+amount+"Yen");
            amount = currencyConverter.yenToEuro(param);
            System.out.println("Yen is :"+amount+"Euro");
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
