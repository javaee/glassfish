/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Expression.equals().
 * 6524656
 */

public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static String testName = "EL Expression equals test";
    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit tests for Expression.equals");
        WebTest webTest = new WebTest(args);

        try {
            webTest.test("/equals.jsp");
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(testName, stat.FAIL);                
        }

        stat.printSummary();
    }

    public void test(String path) throws Exception {
        URL url = new URL("http://" + host  + ':' + port + '/' + contextRoot
 + path);
                    System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(testName, stat.FAIL);
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            boolean error = false;
            String line;
            while ((line = input.readLine()) != null) {
                if (line.trim().length()> 0)
                    System.out.println(line);
                if (line.indexOf("FAIL") > 0)
                    error = true;
            }
            stat.addStatus(testName, error? stat.FAIL: stat.PASS);
        }
    }
}

