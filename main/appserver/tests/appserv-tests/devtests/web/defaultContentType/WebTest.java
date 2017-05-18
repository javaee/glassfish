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

import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.appserv.test.BaseDevTest;

/*
 * Unit test for 6328909
 */
public class WebTest extends BaseDevTest {
    private static final String EXPECTED_CONTENT_TYPE = "text/plain;charset=iso-8859-1";
    private String host;
    private int port;

    public WebTest() {
        host = antProp("http.host");
        port = Integer.valueOf(antProp("http.port"));
    }

    public static void main(String[] args) {
        new WebTest().doTest();
    }

    public void doTest() {
        try {
            invoke(null);
            report("no-content-type", invoke(null));
            report("set-content-type-value", asadmin("set",
                "configs.config.server-config.network-config.protocols.protocol.http-listener-1.http."
                    + "default-response-type=" + EXPECTED_CONTENT_TYPE));
            report("default-content-type", invoke(EXPECTED_CONTENT_TYPE));
            report("set-content-type-value", asadmin("set",
                "configs.config.server-config.network-config.protocols.protocol.http-listener-1.http."
                    + "default-response-type="));
            report("no-content-type-again", invoke(null));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            stat.printSummary();
        }
    }

    private boolean invoke(final String expected) throws Exception {
        URL url = new URL("http://" + host + ":" + port + "/test.xyz");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200, received: " + responseCode);
        }
        String contentType = conn.getHeaderField("Content-Type");
        return expected == null ? contentType == null : contentType.equals(expected);
    }

    @Override
    protected String getTestName() {
        return "default-content-type";
    }

    @Override
    protected String getTestDescription() {
        return "Tests that the default content type can be set correctly";
    }
}
