/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.jms.msgdest.jmsweb.client;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.naming.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    private String host;
    private String port;

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public Client (String[] args) {
        host = ( args.length > 0) ? args[0] : "localhost";
        port = ( args.length > 1) ? args[1] : "4848";
    }
    
    public static void main(String[] args) {
        stat.addDescription("jms-msgdest-jmsweb");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("jms-msgdest-jmswebID");
    }
    
    public void doTest() {
        
        String env = null;
        try {
            String url = "http://" + host + ":" + port + 
                "/jms-msgdest-jmsweb/jmsservlet";
            System.out.println("invoking jmsweb servlet at " + url);
            int code = invokeServlet(url);
            
            if(code != 200) {
                System.out.println("Incorrect return code: " + code);
                stat.addStatus("jmsweb main", stat.FAIL);
            } else {
                stat.addStatus("jmsweb main", stat.PASS);
            }
        } catch (Exception ex) {
            System.out.println("Jms web test failed.");
            stat.addStatus("jmsweb main", stat.FAIL);
            ex.printStackTrace();
        }
        
        return;
        
    }

    private int invokeServlet(String url) throws Exception {
            
        URL u = new URL(url);
        
        HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
        int code = c1.getResponseCode();
        InputStream is = c1.getInputStream();
        BufferedReader input = new BufferedReader (new InputStreamReader(is));
        String line = null;
        while((line = input.readLine()) != null)
            System.out.println(line);
        if(code != 200) {
            System.out.println("Incorrect return code: " + code);
        }
        return code;
    }
    
}

