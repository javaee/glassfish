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
import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for customizing complete list of session tracking cookie
 * properties via web.xml
 */
public class WebTest {

    private static String TEST_NAME = "session-cookie-config-declarative";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {

        stat.addDescription("Unit test for customizing complete list of " +
                            "session tracking cookie properties via " +
                            "web.xml");
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

        String url = "http://" + host + ":" + port + contextRoot
                     + "/CreateSession";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        }

        String sessionCookie = conn.getHeaderField("Set-Cookie");
        System.out.println("Response cookie: " + sessionCookie);

        if (sessionCookie == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        // name
        if (sessionCookie.indexOf("MYJSESSIONID=") == -1) {
            throw new Exception("Missing session id");
        }

        // comment
        if (sessionCookie.indexOf("Comment=myComment") == -1) {
            throw new Exception("Missing cookie comment");
        }      

        // domain
        if (sessionCookie.indexOf("Domain=mydomain") == -1) {
            throw new Exception("Missing cookie domain");
        }      

        // path
        if (sessionCookie.indexOf("Path=/myPath") == -1) {
            throw new Exception("Missing cookie path");
        }      

        // secure
        if (sessionCookie.indexOf("Secure") == -1) {
            throw new Exception("Missing Secure attribute");
        }      

        // http-only
        if (sessionCookie.indexOf("HttpOnly") == -1) {
            throw new Exception("Missing HttpOnly attribute");
        }      

        // max-age
        if (sessionCookie.indexOf("Max-Age=123") == -1) {
            throw new Exception("Missing max-age");
        }      
    }
}
