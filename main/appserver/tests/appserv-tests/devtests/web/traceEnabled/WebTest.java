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

import java.io.*;
import java.net.*;
import java.util.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for:
 *
 * - 6185574 ("[8.1 PE] Disabling TRACE returns wrong response code
 *   and does not include Allow response header")
 *
 * - 6182013 ("[8.1 EE] HTTP spec violation: response does not
 *   include any "Allow" header if TRACE disabled")
 */
public class WebTest{

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter(
                                                            "appserv-tests");
    
    public static void main(String args[]) {

        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];

        try {
            stat.addDescription("Trace not allowed test");
            
            URL url = new URL("http://" + host  + ":" + port + contextRoot
                          + "/ServletTest");
            System.out.println("Invoking url: " + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("TRACE");
            try {
                conn.getInputStream().close();
            } catch (IOException ex) {
                // Do nothing: If TRACE is disabled, we get IOException
                // here if response body is empty
            }

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_BAD_METHOD){
                stat.addStatus("traceEnabled", stat.FAIL);
            } else {
                String allowHeader = conn.getHeaderField("Allow");
                System.out.println("Allow response header: " + allowHeader);
                if (allowHeader != null && !allowHeader.toUpperCase().contains("GET")) {
                    stat.addStatus("traceEnabled", stat.PASS);
                } else {
                    stat.addStatus("traceEnabled", stat.FAIL);
                }
            }

            stat.printSummary("web/traceEnabled");

        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus("traceEnabled", stat.FAIL);
        }
    }
}
