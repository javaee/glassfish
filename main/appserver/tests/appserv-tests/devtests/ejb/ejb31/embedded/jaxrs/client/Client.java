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
import embedded.util.ZipUtil;

import javax.ejb.*;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.*;
import java.net.*;
import java.util.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;
    private static String type = "xxx";
    private static final String LOCALHOST = "http://localhost:8080/";

    public static void main(String[] s) {
        appName = s[0];
        type = s[2];
        stat.addDescription(appName);
        Client t = new Client();
        if (s[1].equals("ejb")) {
            try {
                System.out.println("Running test via EJB....");
                t.testEJB(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (s[1].equals("rest")) {
            /** This doesn't work as there is a problem to access the servlet **/
            try {
                System.out.println("Running test via REST....");
                t.testREST(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("WRONG TEST TYPE: " + s[1]);
        }

        stat.printSummary(appName + "-" + type);
    }

    private void testEJB(String[] args) {

        boolean pass = true;
        EJBContainer c = null;
        try {
           c = EJBContainer.createEJBContainer();
            Context ic = c.getContext();
            System.out.println("Looking up EJB...");
            Simple ejb = (Simple) ic.lookup("java:global/sample/SimpleEjb!org.glassfish.tests.ejb.sample.Simple");
            System.out.println("Invoking EJB...");
            System.out.println("EJB said: " + ejb.saySomething());
            System.out.println("JPA call returned: " + ejb.testJPA());

        } catch (Exception e) {
            pass = false;
            System.out.println("ERROR calling EJB:");
            e.printStackTrace();
            System.out.println("Saving temp instance dir...");
            ZipUtil.zipInstanceDirectory(appName + "-" + type);

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

    private void testREST(String[] args) {

        boolean pass = true;
        EJBContainer c = null;
        try {
            Map p = new HashMap();
            p.put("org.glassfish.ejb.embedded.glassfish.web.http.port", "8080");
            System.setProperty("org.glassfish.ejb.embedded.keep-temporary-files", "true");
            c = EJBContainer.createEJBContainer(p);
        // ok now let's look up the EJB...
            System.out.println("Testing EJB via REST...");
            System.out.println("EJB said: " + testResourceAtUrl(new URL(LOCALHOST + appName + "-web/test/simple")));
            System.out.println("JPA call returned: " + testResourceAtUrl(new URL(LOCALHOST + appName + "-web/test/jpa")));

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

    private static String testResourceAtUrl(URL url) throws Exception {

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String firstLineOfText = reader.readLine();//1 line is enough
            System.out.println("Read: " + firstLineOfText);

            connection.disconnect();
            return firstLineOfText;

        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new Exception("could not establish connection to " + url.toExternalForm());
    }

}
