/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.annotation.Resource;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static final String TEST_NAME = "normal-lookup-in-ear";

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter(
            "appserv-tests");

    private static String appName;
    private String host;
    private String port;

    @Resource(lookup = "java:app/env/value1")
    private static Integer appLevelViaLookup;

    public static void main(String args[]) {
        appName = args[0];
        stat.addDescription(appName);
        Client client = new Client(args);
        client.doTest();
        stat.printSummary(appName + "ID");
        System.out.println("appLevelViaLookup = '" + appLevelViaLookup + "'");
    }

    public Client(String[] args) {
        host = args[1];
        port = args[2];
    }

    public void doTest() {

        try {

            String url = "http://" + host + ":" + port + "/" + appName
                    + "/HelloServlet";

            System.out.println("invoking webclient servlet at " + url);

            URL u = new URL(url);

            HttpURLConnection c1 = (HttpURLConnection) u.openConnection();
            int code = c1.getResponseCode();
            InputStream is = c1.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    is));
            String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println("<response>:" + line);
                if (line.trim().length() > 0) {
                    stat.addStatus(TEST_NAME, stat.FAIL);
                    return;
                }
            }
            if (code != 200) {
                stat.addStatus(TEST_NAME, stat.FAIL);
                return;
            }
            stat.addStatus(TEST_NAME, stat.PASS);

        } catch (Exception e) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            e.printStackTrace();
        }
    }

}
