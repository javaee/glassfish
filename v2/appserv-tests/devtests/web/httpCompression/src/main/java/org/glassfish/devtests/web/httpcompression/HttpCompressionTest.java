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

package org.glassfish.devtests.web.httpcompression;

import java.io.BufferedReader;
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
            final int port = Integer.valueOf(antProp("http.port"));
				final String path = "configs.config.server-config.network-config.protocols.protocol.http-listener-1.http.compression=";
            final String[] schemes = {"gzip", "lzma"};
            for (String scheme : schemes) {
                String header = scheme + "-";
                get("localhost", port, false, "compressed-output-off", scheme);

                report(header + "set-compression-on", asadmin("set", path + "on"));
                get("localhost", port, true, "compressed-output-on", scheme);

                report(header + "set-compression-force", asadmin("set", path + "force"));
                get("localhost", port, true, "compressed-output-force", scheme);

                report(header + "set-compression-false", !asadmin("set", path + "false"));

                report(header + "set-compression-true", !asadmin("set", path + "true"));

                report(header + "set-compression-1024", asadmin("set", path + "1024"));
                get("localhost", port, true, "compressed-output-1024", scheme);

                report(header + "set-compression-off", asadmin("set", path + "off"));
                get("localhost", port, false, "compressed-output-off-2", scheme);
            }
        } catch (Exception e) {
			  report(e.getMessage(), false);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            stat.printSummary();
        }
    }

    private void get(String host, int port, boolean zipped, final String test, final String compScheme)
        throws Exception {
        Socket s = new Socket(host, port);
        OutputStream os = s.getOutputStream();
        send(os, "GET /index.html HTTP/1.1");
        send(os, "Host: localhost:8080");
        if (zipped) {
            send(os, "Accept-Encoding: " + compScheme);
        }
        send(os, "\n");
        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line;
        boolean found = false;
        boolean chunked = false;
        boolean contentLength = false;
        try {
            while ((line = bis.readLine()) != null && !"".equals(line.trim())) {
                found |= line.toLowerCase().contains("content-encoding: " + compScheme);
                if (zipped) {
                   chunked |= line.toLowerCase().contains("transfer-encoding: chunked");
                   contentLength |= !line.toLowerCase().contains("content-length"); 
		}
            }
        } finally {
            s.close();
        }
        if (zipped) {
            stat.addStatus(compScheme + "-" + test, (found && chunked && contentLength) ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
        } else {
            stat.addStatus(compScheme + "-" + test, found ? SimpleReporterAdapter.FAIL : SimpleReporterAdapter.PASS);
        }
    }

    private void send(final OutputStream os, final String text) throws IOException {
        os.write((text + "\n").getBytes());
    }

    public static void main(String[] args) {
        try {
            new HttpCompressionTest().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
