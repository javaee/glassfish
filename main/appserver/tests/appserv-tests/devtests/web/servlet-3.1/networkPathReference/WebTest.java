/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import com.sun.ejte.ccl.reporter.*;
import org.apache.catalina.startup.SimpleHttpClient;

/*
 * Unit test for HttpResponse.sendRedirect for network path reference.
 */
public class WebTest {

    private static String TEST_NAME = "servlet-3.1-network-path-reference";
    private static String LOCATION_PREFIX = "Location:";
    private static String EXPECTED_RESPONSE = "redirect a";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String contextRoot = args[2];
        stat.addDescription("Unit test for HttpResponse.sendRedirect for network path reference.");

        try {
            HttpClient client = new HttpClient(host, port);
            client.get(contextRoot + "/index.jsp");
            String location = client.getLocationHeader();
            if (location == null) {
                throw new Exception();
            }


            int ds = location.indexOf("//");
            int c = location.indexOf(":", ds);
            int ss = location.indexOf("/", c);
            host = location.substring(ds + 2, c);
            port = Integer.parseInt(location.substring(c + 1, ss));
            String path = location.substring(ss);

            HttpClient client2 = new HttpClient(host, port);
            client2.get(path);
            client2.setExpectedResponse(EXPECTED_RESPONSE);
            stat.addStatus(TEST_NAME, ((client2.isResponseBodyOK())? stat.PASS : stat.FAIL));
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }


    private static final class HttpClient extends SimpleHttpClient {
        private String expectedResponse;

        private HttpClient(String host, int port) {
            setHost(host);
            setPort(port);
        }

        private void setExpectedResponse(String expRes) {
            expectedResponse = expRes;
        }

        private void get(String path) throws Exception {
            String[] req = { "GET " + path + " HTTP/1.0" + CRLF + CRLF };
            setRequest(req);
            try {
                connect();
                processRequest();
            } finally {
                disconnect();
            }
        }

        private String getLocationHeader() {
            List<String> responseHeaders = getResponseHeaders();
            for (String header : responseHeaders) {
                if (header.startsWith(LOCATION_PREFIX)) {
                    header = header.substring(LOCATION_PREFIX.length());
                    header = header.trim();
                    return header;
                }
            }
            return null;
        }

        @Override
        public boolean isResponseBodyOK() {
            if (expectedResponse == null) {
                return true;
            }

            String body = getResponseBody();
            boolean valid = body.contains(expectedResponse);
            if (!valid) {
                System.out.println("Expected to contain: " + expectedResponse
                        + "\nActual: " + body);
            }
            return valid;
        }
    }
}
