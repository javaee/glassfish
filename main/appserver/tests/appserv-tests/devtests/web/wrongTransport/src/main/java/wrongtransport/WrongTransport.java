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

package wrongtransport;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.appserv.test.BaseDevTest;
import org.glassfish.grizzly.config.portunif.HttpProtocolFinder;

public class WrongTransport extends BaseDevTest {
    private static final String TEST_NAME = "wrongTransport";
    private String secureURL;

    public WrongTransport(final String host, final String port) {
        createPUElements();
        try {
            secureURL = "https://" + host + ":" + port + "/";
            final String url = "http://" + host + ":" + port + "/";
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setInstanceFollowRedirects(true);
            checkStatus(connection);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            deletePUElements();
        }
        stat.printSummary();
    }

    @Override
    protected String getTestName() {
        return TEST_NAME;
    }

    @Override
    protected String getTestDescription() {
        return "Wrong Protocol SSL test";
    }

    public static void main(String args[]) throws Exception {
        new WrongTransport(args[0], args[1]);
    }

    private void createPUElements() {
        // http-redirect
        report("create-http-redirect-protocol", asadmin("create-protocol",
            "http-redirect"));
        report("create-protocol-filter-redirect", asadmin("create-protocol-filter",
            "--protocol", "http-redirect",
            "--classname", "org.glassfish.grizzly.config.portunif.HttpRedirectFilter",
            "redirect-filter"));

        //  pu-protocol
        report("create-pu-protocol", asadmin("create-protocol",
            "pu-protocol"));
        report("create-protocol-finder-http-finder", asadmin("create-protocol-finder",
            "--protocol", "pu-protocol",
            "--targetprotocol", "http-listener-2",
            "--classname", HttpProtocolFinder.class.getName(),
            "http-finder"));
        report("create-protocol-finder-http-redirect", asadmin("create-protocol-finder",
            "--protocol", "pu-protocol",
            "--targetprotocol", "http-redirect",
            "--classname", HttpProtocolFinder.class.getName(),
            "http-redirect"));
        // reset listener
        report("set-http-listener-protocol", asadmin("set",
            "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.protocol=pu-protocol"));
    }

    private void deletePUElements() {
        // reset listener
        report("reset-http-listener-protocol", asadmin("set",
            "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.protocol=http-listener-1"));
        report("delete-pu-protocol", asadmin("delete-protocol",
            "pu-protocol"));
        report("delete-http-redirect", asadmin("delete-protocol",
            "http-redirect"));
    }
    
    private void checkStatus(HttpURLConnection connection)
        throws Exception {
        int responseCode = connection.getResponseCode();
        String location = connection.getHeaderField("location");
        report("response-code", responseCode == 302);
        report("returned-location", secureURL.equals(location));
    }
}
