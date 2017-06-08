/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.anyone.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import javax.ejb.EJB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec::Anyone test ";
    private static @EJB com.sun.s1asdev.security.anyone.ejb.Hello hello;
    private String host;
    private int port;

    public static void main(String[] args) {
        Client client = new Client(args);
        client.doTest();
    }

    public Client(String[] args) {
        host = (args.length > 0) ? args[0] : "localhost";
        port = (args.length > 1) ? Integer.parseInt(args[1]) : 8080;
    }

    public void doTest() {
        stat.addDescription("security-anyone");

        String description = null;
        System.out.println("Invoking ejb");
        try {
            description = testSuite + " ejb: hello";
            hello.hello("Sun");
            stat.addStatus(description, stat.PASS);  
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(description, stat.FAIL);
        }

        System.out.println("Invoking servlet");
        description = testSuite + " servlet";
        try {
            int count = goGet(host, port, "/security-anyone/servlet");
            if (count == 1) {
                stat.addStatus(description, stat.PASS);
            } else {
                System.out.println("Servlet does not return expected result.");
                stat.addStatus(description, stat.FAIL);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(description, stat.FAIL);
        }
 
        stat.printSummary("security-anyone");
    }

    private static int goGet(String host, int port, String contextPath)
            throws Exception {
        Socket s = new Socket(host, port);

        OutputStream os = s.getOutputStream();
        System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("Authorization: Basic amF2YWVlOmphdmFlZQ==\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        int count = 0;
        int lineNum = 0;
        while ((line = bis.readLine()) != null) {
            System.out.println(lineNum + ": " + line);
            if (line.equals("Hello World")) {
                count++;
            }
            lineNum++;
        }

        return count;
    }
}
