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
 * Unit test for 6181949 ("JspServlet init param 'modificationTestInterval'
 * ignored").
 *
 * This test:
 * 
 *  - accesses a JSP,
 *
 *  - updates the JSP (through a servlet),
 *
 *  - accesses the JSP again within modificationTestInterval seconds (and
 *    therefore is expected to get the old contents), and
 *
 *  - accesses the JSP once again after modificationTestInterval seconds have
 *    expired (and therefore is expected to get the updated contents).
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME
        = "jsp-config-modification-test-interval";
    private static final String ORIGINAL_CONTENT = "original jsp";
    private static final String UPDATED_CONTENT = "updated jsp";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for modificationTestInterval "
                            + "jsp-config property");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {     
        try { 
            run();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void run() throws Exception {

        String bodyLine = null;

        // Access JSP
        bodyLine = getBodyLine("jsp/test.jsp");
        if (!ORIGINAL_CONTENT.equals(bodyLine)) {
            throw new Exception("Wrong response: Expected: " +
                ORIGINAL_CONTENT);
        }

        // Update JSP
        System.out.println("Updating JSP ...");
        bodyLine = getBodyLine("UpdateJsp");
        System.out.println(bodyLine);
                
        /*
         * Access JSP. Must get original contents, because the 
         * modificationTestInterval specified in sun-web.xml has not yet 
         * expired, which means that we must not (yet) check for any
         * modifications of the JSP
         */
        bodyLine = getBodyLine("jsp/test.jsp");
        if (!ORIGINAL_CONTENT.equals(bodyLine)) {
            throw new Exception("Wrong response: Expected: " +
                ORIGINAL_CONTENT + ", received: " + bodyLine);
        }

        /*
         * Sleep for the amount of seconds specified for
         * modificationTestInterval jsp-config property in sun-web.xml
         */
        System.out.println("Sleeping for 60s ...");
        Thread.sleep(60 * 1000L);

        /*
         * Access JSP. In this case, we do check for JSP's modification date,
         * recompile, and return updated content
         */
        bodyLine = getBodyLine("jsp/test.jsp");
        if (!UPDATED_CONTENT.equals(bodyLine)) {
            throw new Exception("Wrong response: Expected: " +
                UPDATED_CONTENT + ", received: " + bodyLine);
        }
    }

    private String getBodyLine(String resource) throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/" + resource + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("\n".getBytes());

            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));

            String line = null;
            String bodyLine = null;
            while ((line = bis.readLine()) != null) {
                bodyLine = line;
            }
            return bodyLine;
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException ex) {}
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (sock != null) sock.close();
            } catch (IOException ex) {}
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }   
}
