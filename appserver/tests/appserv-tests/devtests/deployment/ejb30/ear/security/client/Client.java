/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.deployment.ejb30.ear.security.client;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import com.sun.s1asdev.deployment.ejb30.ear.security.*;

public class Client {
    private String host;
    private int port;

    public static void main (String[] args) {
        System.out.println("deployment-ejb30-ear-security");
        Client client = new Client(args);
        client.doTest();
    }  
    
    public Client (String[] args) {
        host = (args.length > 0) ? args[0] : "localhost";
        port = (args.length > 1) ? Integer.parseInt(args[1]) : 8080;
    }
    
    private static @EJB Sless sless;
    private static @EJB Sful sful;

    public void doTest() {

        try {

            System.out.println("invoking stateless");
            try {
                System.out.println(sless.hello());
                System.exit(-1);
            } catch(Exception ex) {
                System.out.println("Expected failure from sless.hello()");
            }

            sless.goodMorning();

            try {
                sless.goodBye();
                System.exit(-1);
            } catch(EJBException ex) {
                System.out.println("Expected failure from sless.goodBye()");
            }

            System.out.println("invoking stateful");
            System.out.println(sful.hello());
            System.out.println(sful.goodAfternoon());
            try {
                sful.goodNight();
                System.exit(-1);
            } catch(EJBException ex) {
                System.out.println("Expected failure from sful.goodNight()");
            }

            System.out.println("invoking servlet");
            int count = goGet(host, port, "/deployment-ejb30-ear-security/servlet");
            if (count != 2) {
                System.out.println("Servlet does not return expected result.");
                System.exit(-1);
            }

            System.out.println("test complete");

        } catch(Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }

    	return;
    }

    private static int goGet(String host, int port, String contextPath)
            throws Exception {
        Socket s = new Socket(host, port);

        OutputStream os = s.getOutputStream();
        System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("Authorization: Basic ajJlZTpqMmVl\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = s.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;

        int count = 0;
        int lineNum = 0;
        while ((line = bis.readLine()) != null) {
            System.out.println(lineNum + ": " + line);
            int index = line.indexOf("=");
            if (index != -1) {
                String info = line.substring(index + 1);  
                if (info.startsWith("hello")) {
                    count++;
                }
            }
            lineNum++;
        }

        return count;
    }
}
