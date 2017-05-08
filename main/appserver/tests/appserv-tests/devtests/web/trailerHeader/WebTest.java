/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Port Tomcat unit test
 *     test/org/apache/coyote/http11/filters/TestChunkedInputFilter.java.
 * Unit test for GLASSFISH-17857.
 */
public class WebTest {

    private static final String TEST_NAME =
        "trailer-header";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String CRLF = "\r\n";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for IT GLASSFISH-16768");
        final WebTest webTest = new WebTest(args);

        try {
            String[] req = null;
            // trailing headers
            req = new String[] {
                "POST /" + webTest.contextRoot + " HTTP/1.1" + CRLF +
                "Host: any" + CRLF +
                "Transfer-encoding: chunked" + CRLF +
                "Content-Type: application/x-www-form-urlencoded" + CRLF +
                "Connection: close" + CRLF +
                CRLF +
                "3" + CRLF +
                "a=0" + CRLF +
                "4" + CRLF +
                "&b=1" + CRLF +
                "0" + CRLF +
                "x-trailer: Test",
                "TestTest0123456789abcdefghijABCDEFGHIJopqrstuvwxyz" + CRLF +
                CRLF };
            webTest.doTest(req, "null7TestTestTest0123456789abcdefghijABCDEFGHIJopqrstuvwxyz", true);

            /* enable this when maxTrailerSize is configurable dynamically to 10
            // trailing headers size limit
            // need to set maxTrailerSize = 10
            String dummy = "01234567890";
            
            req = new String[] {
                "POST /" + webTest.contextRoot + " HTTP/1.1" + CRLF +
                "Host: any" + CRLF +
                "Transfer-encoding: chunked" + CRLF +
                "Content-Type: application/x-www-form-urlencoded" + CRLF +
                "Connection: close" + CRLF +
                CRLF +
                "3" + CRLF +
                "a=0" + CRLF +
                "4" + CRLF +
                "&b=1" + CRLF +
                "0" + CRLF +
                "x-trailer: Test" + dummy + CRLF +
                CRLF };
            webTest.doTest(req, "Http/1.1 500", false);
            */

            // no trailing headers
            req = new String[] {
                "POST /" + webTest.contextRoot + " HTTP/1.1" + CRLF +
                "Host: any" + CRLF +
                "Transfer-encoding: chunked" + CRLF +
                "Content-Type: application/x-www-form-urlencoded" +
                    CRLF +
                "Connection: close" + CRLF +
                CRLF +
                "3" + CRLF +
                "a=0" + CRLF +
                "4" + CRLF +
                "&b=1" + CRLF +
                "0" + CRLF +
                CRLF };
            webTest.doTest(req, "null7null", true);
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest(String[] req, String expectedResult, boolean exact) throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        String cookie = null;

        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            for (String r : req) {
                System.out.print(r);
                os.write(r.getBytes());
            }

            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            boolean found = false;
            // there is no Location header here anymore
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if ((exact && line.equals(expectedResult)) ||
                        ((!exact) && line.startsWith(expectedResult))) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new Exception("Do not find " + expectedResult);
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
        }
    }

    private void close(Socket sock) {
        try {
            if (sock != null) {
                sock.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }
}
