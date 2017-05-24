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
import org.glassfish.grizzly.test.http2.*;

/*
 * Unit test for Http2 Push static file
 */
public class WebTest {

    private static String TEST_NAME = "servlet-4.0-push-arbitrary-method";
    private static String EXPECTED_PUSH_BODY = "foo: bar";
    private static String EXPECTED_BODY = "my.css";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for http2 push static file");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try (HttpClient httpClient = HttpClient.builder().
                host(host).port(port).build()) {
            httpClient.request().path(contextRoot + "/test").build().send();
            HttpResponse httpResponse = httpClient.getHttpResponse();
            HttpResponse httpResponse2 = httpClient.getHttpResponse();
            if (!verify(httpResponse) || !verify(httpResponse2)) {
                throw new Exception("Incorrect result");
            }
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch(Throwable t) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            t.printStackTrace();
        }
    }

    private boolean verify(HttpResponse response) {
        if (response == null) {
            System.out.println("--> response is null");
            return false;
        }

        String body = response.getBody().trim();
        System.out.println("--> body: " + body);

        return (response.isPush() ? body.contains(EXPECTED_PUSH_BODY) : body.contains(EXPECTED_BODY));
    }
}
