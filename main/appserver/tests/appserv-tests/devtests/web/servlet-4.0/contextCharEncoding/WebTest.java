/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
import org.glassfish.grizzly.test.http2.*;

/**
 * Unit test for ServletContext#setRequest/ResponseCharacterEncoding
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "servlet-4.0-context-char-encoding";
    private static final String JCHARSET = "Shift_JIS";
    private static final String JSTR = "\u3068\u4eba\u6587";
    private static final String EXPECTED_RESPONSE = "true:" + JSTR;

    private String host;
    private int port;
    private String contextRoot;
    private Socket sock = null;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for ServletContext#setRequest/ResponseCharacterEncoding");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        } finally {
            try {
                if (webTest.sock != null) {
                    webTest.sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        stat.printSummary(TEST_NAME);
    }

    public void doTest() throws Exception {
        try (HttpClient httpClient = HttpClient.builder().
                host(host).port(port).build()) {
            httpClient.request().path(contextRoot + "/TestServlet")
                .method("POST").contentType("application/x-www-form-urlencoded")
                .characterEncoding(JCHARSET)
                .content("japaneseName=" + JSTR)
                .build().send();
            HttpResponse httpResponse = httpClient.getHttpResponse();
            int code = httpResponse.getStatus();
            if (code != 200) {
                throw new Exception("Unexpected return code: " + code);
            }
            String contentType = httpResponse.getHeader("Content-Type");
            int ind = contentType.indexOf(";charset=");
            String charset = JCHARSET;
            if (ind > 0) {
                charset = contentType.substring(ind + 9).trim();
            }
            System.out.println("--> headers = " + httpResponse.getHeaders());
            String line = httpResponse.getBody(charset).trim();
            System.out.println("--> line = " + line);
            if  (!EXPECTED_RESPONSE.equals(line)) {
                throw new Exception("Wrong response. Expected: " + 
                    EXPECTED_RESPONSE + ", received: " + line);
            }
        }
    }
}
