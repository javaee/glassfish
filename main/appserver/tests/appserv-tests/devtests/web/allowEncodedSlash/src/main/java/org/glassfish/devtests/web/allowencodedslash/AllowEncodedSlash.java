/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.devtests.web.allowencodedslash;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.appserv.test.BaseDevTest;

public class AllowEncodedSlash extends BaseDevTest {
    public static void main(String[] args) throws IOException {
        new AllowEncodedSlash().run();
    }

    @Override
    protected String getTestName() {
        return "allow-encoded-slash";
    }

    @Override
    protected String getTestDescription() {
        return "allow-encoded-slash";
    }

    public void run() throws IOException {
        String adminPort = antProp("admin.port");
        try {
            setAllowed(false);
            fetch(adminPort, 500, 1);
        } finally {
            setAllowed(true);
            fetch(adminPort, 200, 1);
            stat.printSummary();
        }
    }

    private void setAllowed(final boolean allowed) {
        report("set-encoding-" + allowed, asadmin("set",
            "configs.config.server-config.network-config.protocols.protocol.admin-listener.http.encoded-slash-enabled=" + allowed));
        report("stop-domain-" + allowed, asadmin("stop-domain"));
        report("start-domain-" + allowed, asadmin("start-domain"));
    }

    private void fetch(final String adminPort, final int expectedCode, final int count) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL("http://localhost:" + adminPort + "/management/domain/resources/jdbc-resource/jdbc%2F__TimerPool.xml").openConnection();
            connection.setRequestProperty("X-GlassFish-3", "true");
            System.out.println("Connection response code returned "+connection.getResponseCode()); 
            report("response-" + expectedCode + "-try-" + count, expectedCode == connection.getResponseCode());
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
}
