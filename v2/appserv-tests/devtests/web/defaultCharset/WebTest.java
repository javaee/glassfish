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
 * Unit test for 4921329 ("default-charset" attribute of <parameter-encoding>
 * in sun-web.xml is ignored).
 *
 * This client invokes a JSP which retrieves the request charset, which must
 * correspond to the value of the default-charset attribute of the
 * parameter-encoding element in this web module's sun-web.xml.
 *
 * The JSP sets the response charset to be the same as the request charset.
 *
 * This client then checks to see if the response charset matches the value
 * of the default-charset attribute. The test fails if there is no match.
 */
public class WebTest {

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
        stat.addDescription("Unit test for 4921329");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary("default-charset");
    }

    public void doTest() {     
        try { 
            invokeJsp();
        } catch (Exception ex) {
            System.out.println("default-charset test failed.");
            stat.addStatus("default-charset", stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJsp() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        System.out.println(("GET " + contextRoot + "/jsp/getRequestCharset.jsp"
            + " HTTP/1.0\n"));
        os.write(("GET " + contextRoot + "/jsp/getRequestCharset.jsp"
            + " HTTP/1.0\n").getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        boolean success = false;

        int i = 0;
        while ((line = bis.readLine()) != null) {
            System.out.println(i++ + ": " + line);
            if (!line.toLowerCase().startsWith("content-type:")) {
                continue;
            }
            int index = line.indexOf("charset=GB18030");
            if (index != -1) {
                success = true;
            }
        }
        if (line == null)
            System.out.println("Request failed, no response");

        if (success) {
            stat.addStatus("default-charset", stat.PASS);
        } else {
            stat.addStatus("default-charset", stat.FAIL);
        }
    }
}
