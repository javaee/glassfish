/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.devtests.web.httpcompression;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import com.sun.appserv.test.BaseDevTest;
import com.sun.appserv.test.util.results.SimpleReporterAdapter;

/*
* Unit test for http compression
*/
public class HttpCompressionTest extends BaseDevTest {
    @Override
    protected String getTestName() {
        return "http-compression";
    }

    @Override
    protected String getTestDescription() {
        return "Unit test for setting http compression levels";
    }

    public void run() {
        try {
            get("localhost", 8080, "", false, "compressed-output-off");

            report("set-compression-on", asadmin("set",
                "configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.compression=on"));
            get("localhost", 8080, "", true, "compressed-output-on");

            report("set-compression-force", asadmin("set",
                "configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.compression=force"));
            get("localhost", 8080, "", true, "compressed-output-force");

            report("set-compression-false", !asadmin("set",
                "configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.compression=false"));
            report("set-compression-true", !asadmin("set",
                "configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.compression=true"));
            report("set-compression-off", asadmin("set",
                "configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.compression=off"));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            stat.printSummary();
        }
    }

    private void get(String host, int port, String result, boolean zipped, final String test) throws Exception {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();
        send(os, "GET /index.html HTTP/1.1");
        send(os, "Host: localhost:8080");
        if (zipped) {
            send(os, "Accept-Encoding: gzip");
        }
        send(os, "\n");
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;
        int tripCount = 0;
        boolean found = false;
        try {
            while ((line = bis.readLine()) != null && !"".equals(line.trim())) {
                write("from server: " + line);
                found |= line.contains("Content-Encoding: gzip");
            }
        } finally {
            s.close();
        }
        if (zipped) {
            stat.addStatus(test, found ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
        } else {
            stat.addStatus(test, found ? SimpleReporterAdapter.FAIL : SimpleReporterAdapter.PASS);
        }
    }

    private void send(final OutputStream os, final String text) throws IOException {
        write(text);
        os.write((text + "\n").getBytes());
    }

    public static void main(String[] args) throws FileNotFoundException {
        new HttpCompressionTest().run();
    }

}
