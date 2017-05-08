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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

/*
* Unit test for 6273998
*/
public class WebTest {
    public static final String TEST_NAME = "keepAliveTimeout";
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", TEST_NAME);
    private String host;
    private String port;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
    }

    public static void main(String[] args) {
        stat.addDescription(TEST_NAME);
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary();
    }

    public void doTest() {
        try {
            invoke();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, SimpleReporterAdapter.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {
        Socket sock = new Socket(host, new Integer(port));
        BufferedReader bis = null;
        try {
            sock.setSoTimeout(50000);
            OutputStream os = sock.getOutputStream();
            String get = "GET /index.html HTTP/1.1\n";
            os.write(get.getBytes());
            os.write("Host: localhost\n".getBytes());
            os.write("\n".getBytes());
            InputStream is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            long start = System.currentTimeMillis();
            while (bis.readLine() != null) {
            }
            long end = System.currentTimeMillis();
            System.out.println("WebTest.invoke: end - start = " + (end - start));
            stat.addStatus(TEST_NAME, end - start >= 10000 ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
        } finally {
            if (sock != null) {
                sock.close();
            }
            if (bis != null) {
                bis.close();
            }
        }
    }
}
