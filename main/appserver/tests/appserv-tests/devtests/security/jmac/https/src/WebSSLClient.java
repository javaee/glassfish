/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.jmac.https;

import java.io.*;
import java.util.regex.Pattern;
import java.net.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

public class WebSSLClient {

    private static final String TEST_NAME = "security-jmac-https";
    private static final String EXPECTED_RESPONSE_PATTERN = "Hello, CN=.* from com.sun.s1asdev.security.jmac.https.HttpsTestAuthModule";
    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) throws Exception {

        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];

        System.out.println("host/port=" + host + "/" + port);

        try {
            stat.addDescription(TEST_NAME);
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            HttpsURLConnection connection = connect("https://" + host + ":"
                    + port + "/" + contextRoot
                    + "/index.jsp",
                    ssf);

            parseResponse(connection);

        } catch (Throwable t) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            t.printStackTrace();
        }
        stat.printSummary(TEST_NAME);
    }

    private static void parseResponse(HttpsURLConnection connection)
            throws Exception {

        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            String line = null;
            String matched = null;
            try {
                Pattern p = Pattern.compile(EXPECTED_RESPONSE_PATTERN);
                while ((line = in.readLine()) != null) {
                    if (p.matcher(line).matches()) {
                        stat.addStatus(TEST_NAME, stat.PASS);
                        matched = line;
                    }
                    System.out.println(line);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (matched == null) {
                System.err.println("Wrong response. Expected Pattern: "
                        + EXPECTED_RESPONSE_PATTERN
                        + ", received: " + matched);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private static HttpsURLConnection connect(String urlAddress,
            SSLSocketFactory ssf)
            throws Exception {

        URL url = new URL(urlAddress);
        HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setHostnameVerifier(
                new HostnameVerifier() {

                    public boolean verify(String rserver, SSLSession sses) {
                        return true;
                    }
                });

        connection.setDoOutput(true);

        return connection;
    }
}
