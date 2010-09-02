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
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=3374
 * (FORM authenticator should issue a redirect (instead of a request
 * dispatch "forward") to the login page).
 *
 * This unit test has been reworked in light of the fix for CR 6633257:
 * Rather than issuing a redirect over https to the login.jsp login page
 * (which is protected by a transport-guarantee of CONFIDENTIAL), the 
 * redirect over https will be applied to the original request (that is,
 * to protected.jsp), followed by a FORWARD dispatch to login.jsp.
 *
 * This unit test verifies only that the target of the https redirect is as
 * expected, and does not perform the actual FORM-based login.
 */
public class WebTest {

    private static final String TEST_NAME =
        "form-login-transport-guarantee-confidential";

    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String httpPort;
    private String httpsPort;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        httpPort = args[1];
        httpsPort = args[2];
        contextRoot = args[3];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for GlassFish Issue 3374");
        WebTest webTest = new WebTest(args);

        try {
            webTest.run();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void run() throws Exception {

        URL url = new URL("http://" + host  + ":" + httpPort + contextRoot
                          + "/protected.jsp");
        System.out.println(url.toString());
        URLConnection conn = url.openConnection();
        java.util.Map fields = conn.getHeaderFields();
        for (Object header: fields.keySet().toArray()) {
            System.out.println("Header: "+header+" : "+conn.getHeaderField((String)header));
        }
        String redirectLocation = conn.getHeaderField("Location");
        System.out.println("Location: " + redirectLocation);
        
        String expectedRedirectLocation = "https://" + host + ":" + httpsPort
            + contextRoot + "/protected.jsp";
        if (!expectedRedirectLocation.equals(redirectLocation)) {
            throw new Exception("Unexpected redirect location");
        }   
    }
}
