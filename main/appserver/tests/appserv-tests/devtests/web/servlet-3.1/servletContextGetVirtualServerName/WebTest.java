/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import java.lang.*;
import java.io.*;
import java.util.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
/**
 * Test HttpServletResponse#reset and #setCharacterEncoding.
 */
public class WebTest {
    
    private static final String TEST_NAME = "servlet-3.1-servlet-context-get-virtual-server-name";
    
    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String args[]) {

        stat.addDescription("Test ServletContext.getVirtualServerName");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];
        String virtualServerName = args[3];

        int port = new Integer(portS).intValue();
        try {
            boolean result = goGet(host, port, contextRoot + "/test", virtualServerName);
            stat.addStatus(TEST_NAME, (result)? stat.PASS : stat.FAIL);
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    private static boolean goGet(String host, int port, String contextPath,
            String expectedResult) throws Exception {
        
        boolean result = false;

        try (Socket sock = new Socket(host, port);
             OutputStream os = sock.getOutputStream();
             InputStream is = sock.getInputStream();
             BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        ) {
            String getPath = "GET " + contextPath + " HTTP/1.0\n";
            System.out.println(getPath);
            os.write(getPath.getBytes());
            os.write("\n".getBytes());

            String line = null;

            int lineNum=0;
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (line.equals(expectedResult)) {
                    result = true;
                    break;
                }
            }
            
        } 

        return result;
    }
}
