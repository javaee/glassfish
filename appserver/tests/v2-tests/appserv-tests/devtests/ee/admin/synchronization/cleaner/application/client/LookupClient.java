/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.devtest.admin.notification.lookup.client;

import java.util.Properties;
import java.math.BigDecimal;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.devtest.admin.notification.lookup.ejb.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 * A simple java client. This uses the services provided by the 
 * <code>LookupBean</code>.
 */
public class LookupClient {

    private SimpleReporterAdapter stat = 
            new SimpleReporterAdapter("appserv-tests");

    LookupClient() {
    }

   /**
    * The main method of the client.
    */
    public static void main(String[] args) {
        LookupClient client = new LookupClient();
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
            stat.addDescription("Tests dynamic reconfig of resources");

            if (args.length == 3) {
                url = args[0];
                ctxFactory = args[1];
                jndiName = args[2];
            }
            testId = System.getProperty("testId", "0");

            if ( (url == null) || (ctxFactory == null) ) {
                // Initialize the Context with default properties
                context = new InitialContext();
                System.out.println("Default Context Initialized...");
                // Create Home object
                obj = context.lookup("java:comp/env/ejb/lookupBean");
            } else {
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

            LookupRemoteHome home =
               (LookupRemoteHome) PortableRemoteObject.narrow(obj,
                                            LookupRemoteHome.class);
            LookupRemote bean = home.create();

            runDefaultTest(bean, "converter");
        } catch (Exception ex) {
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    }

    private void runDefaultTest(LookupRemote bean, String testId) {
        try {
            BigDecimal param = new BigDecimal ("100.00");
            BigDecimal amount = bean.dollarToYen(param);
            System.out.println("\n\n\n===========Beginning Simple Test=====\n\n");
            System.out.println("$100 is : "+amount+"Yen");
            amount = bean.yenToEuro(param);
            System.out.println("Yen is :"+amount+"Euro");
            stat.addStatus(testId, stat.PASS);
        } catch (Exception e) {
            stat.addStatus(testId, stat.FAIL);
            e.printStackTrace();
        } finally {
            stat.printSummary("Test Result for #" + testId);
        }
    }

}
