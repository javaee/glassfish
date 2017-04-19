/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.acme;

import org.glassfish.tests.ejb.sample.Simple;

import java.util.Map;
import java.util.HashMap;
import javax.ejb.*;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;
import java.net.*;
import java.io.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String[] s) {
        appName = s[0];
        stat.addDescription(appName);
        Client t = new Client();
        if (s.length == 2 && s[1].equals("servlet")) {
            try {
                t.testServlet(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                t.test(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stat.printSummary(appName + "ID");
    }

    private void test(String[] args) {

        boolean pass = true;
        EJBContainer c = null;
        if (args.length == 2 && args[1].equals("installed_instance")) {
            Map p = new HashMap();
            p.put("org.glassfish.ejb.embedded.glassfish.instance.reuse", "true");
            p.put("org.glassfish.ejb.embedded.keep-temporary-files", "true");
            c = EJBContainer.createEJBContainer(p);
        } else {
            c = EJBContainer.createEJBContainer();
        }
        // ok now let's look up the EJB...
        Context ic = c.getContext();
        try {
            System.out.println("Looking up EJB...");
            Simple ejb = (Simple) ic.lookup("java:global/sample/SimpleEjb");
            System.out.println("Invoking EJB...");
            System.out.println("EJB said: " + ejb.saySomething());
            System.out.println("JPA call returned: " + ejb.testJPA());

        } catch (Exception e) {
            pass = false;
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
        }
        System.out.println("Done calling EJB");

        System.out.println("Closing container ...");
        try {
            c.close();
        } catch (Exception e) {
            pass = false;
            System.out.println("ERROR Closing container:");
            e.printStackTrace();
        }
        stat.addStatus("EJB embedded with JPA", (pass)? stat.PASS : stat.FAIL);
        System.out.println("..........FINISHED Embedded test");
    }

    private void testServlet(String[] args) {

        boolean pass = true;
        EJBContainer c = null;
        try {
            Map p = new HashMap();
            p.put("org.glassfish.ejb.embedded.glassfish.web.http.port", "8080");
            // p.put("org.glassfish.ejb.embedded.glassfish.instance.reuse", "true");
            System.setProperty("org.glassfish.ejb.embedded.keep-temporary-files", "true");
            c = EJBContainer.createEJBContainer(p);
            String url = "http://localhost:8080/" + "ejb-ejb31-embedded-ejbwinwar-web/mytest";

            System.out.println("invoking webclient servlet at " + url);

            URL u = new URL(url);

            HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
            int code = c1.getResponseCode();
            InputStream is = c1.getInputStream();
            BufferedReader input = new BufferedReader (new InputStreamReader(is));
            String line = null;
            while((line = input.readLine()) != null)
                System.out.println(line);
            if(code != 200) {
                throw new RuntimeException("Incorrect return code: " + code);
            }

            System.out.println("Testing EJB via Servlet...");

        } catch (Exception e) {
            pass = false;
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    pass = false;
                    System.out.println("ERROR Closing container:");
                    e.printStackTrace();
                }
            }
        }
        stat.addStatus("EJB embedded " + appName + " EJB", (pass)? stat.PASS : stat.FAIL);
        System.out.println("..........FINISHED test");
    }

}
