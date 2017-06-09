/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.wss.permethod.servlet.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.rpc.Stub;

public class Client {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
    private static String testSuite = "Sec:Servlet Per method WSS test ";

    public static void main (String[] args) {
        String helloEndpoint = null;
        if (args[0] == null){
            System.out.println("WSS Permethod client: Argument missing. Please provide target endpoint address as argument");
            System.exit(1);
        } else {
            helloEndpoint = args[0];
        }

        stat.addDescription(testSuite);
        
        HelloIF helloIFPort = null;
        try { 
            Context ic = new InitialContext();
            HelloServletService helloService = (HelloServletService)
                ic.lookup("java:comp/env/service/HelloServletService");
            helloIFPort = helloService.getHelloIFPort();
            ((Stub)helloIFPort)._setProperty(
                    Stub.ENDPOINT_ADDRESS_PROPERTY, helloEndpoint);
            System.out.println("Calling sayHello");
            String reply = helloIFPort.sayHello("Hello World");
            System.out.println("Reply sayHello: " + reply);
            stat.addStatus(testSuite + " sayHello", stat.PASS);
        } catch(Exception e){
            stat.addStatus(testSuite + " sayHello", stat.FAIL);
            e.printStackTrace();
        }
       
        try {
            System.out.println("Calling sendSecret");
            int code = helloIFPort.sendSecret("It is a secret");
            System.out.println("Reply sendSecret: " + code);
            stat.addStatus(testSuite + " sendSecret", stat.PASS);
        } catch(Exception e){
            stat.addStatus(testSuite + "sendSecret", stat.FAIL);
            e.printStackTrace();
        }

        try {
            System.out.println("Calling getSecret");
            String secret = helloIFPort.getSecret(100.0);
            System.out.println("Reply getSecret: " + secret);
            stat.addStatus(testSuite + " getSecret", stat.PASS);
        } catch(Exception e){
            stat.addStatus(testSuite + " getSecret", stat.FAIL);
            e.printStackTrace();
        }

        stat.printSummary(testSuite);
    }
}
