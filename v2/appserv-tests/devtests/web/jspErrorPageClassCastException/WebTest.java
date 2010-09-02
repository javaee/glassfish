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
 * Unit test for Bugzilla 31171 ("ClassCastException in
 * org.apache.jasper.runtime.PageContextImpl.getException"), see
 * http://issues.apache.org/bugzilla/show_bug.cgi?id=31171
 */
public class WebTest {

    private static final String TEST_NAME
        = "jsp-error-page-class-cast-exception";

    private static final String EXPECTED
        = "javax.servlet.jsp.JspException: java.lang.Throwable: "
            + "The cake fell in the mud";

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
        stat.addDescription("Unit test for Bugzilla 31171");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
	stat.printSummary();
    }

    public void doTest() {

        InputStream is = null;
        BufferedReader bis = null;
        try {
            Socket s = new Socket(host, new Integer(port).intValue());
            OutputStream os = s.getOutputStream();
            String requestUri = contextRoot + "/causeError.jsp";

            System.out.println("GET " + requestUri + " HTTP/1.0");
            os.write(("GET " + requestUri + " HTTP/1.0\n").getBytes());
            os.write("\n".getBytes());
        
            is = s.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (line.equals(EXPECTED)) {
                    break;
                }
            }
   
            if (line != null) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                stat.addStatus(TEST_NAME, stat.FAIL);
            }

        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }

}
