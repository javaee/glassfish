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

package com.sun.s1asdev.ejb.stubs.ejbapp.client;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.stubs.ejbapp.HelloHome;
import com.sun.s1asdev.ejb.stubs.ejbapp.Hello;
import com.sun.s1asdev.ejb.stubs.ejbapp.FooHome;
import com.sun.s1asdev.ejb.stubs.ejbapp.Foo;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-stubs-ejbapp");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-stubs-ejbappID");
    }  

    private String host;
    private String port;
    
    public Client (String[] args) {
        host = ( args.length > 0) ? args[0] : "localhost";
        port = ( args.length > 1) ? args[1] : "4848";
    }
    
    public void doTest() {

        Context ic = null;

        try {
            ic = new InitialContext();
   
            System.out.println("Looking up ejbapp ejb ref ");
            // create EJB using factory from container 
            Object objref = ic.lookup("java:comp/env/ejb/hello");
            System.out.println("objref = " + objref);
            System.err.println("Looked up home!!");
                
            HelloHome  home = (HelloHome)PortableRemoteObject.narrow
                (objref, HelloHome.class);
                                                                     
            System.err.println("Narrowed home!!");
                
                
            Hello hr = home.create();
            System.err.println("Got the EJB!!");
                
            // invoke method on the EJB
            System.out.println("invoking ejb");
            hr.sayHello();

            System.out.println("successfully invoked ejb");
            stat.addStatus("ejbapp main", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbapp main" , stat.FAIL);
        }

        try {
             System.out.println("Looking up ejbclient  ejbref ");
            // create EJB using factory from container 
            Object objref = ic.lookup("java:comp/env/ejb/hello_ejbclient");
            System.out.println("objref = " + objref);
            System.err.println("Looked up home!!");
                
            FooHome  fooHome = (FooHome)PortableRemoteObject.narrow
                (objref, FooHome.class);
                                                                     
            System.err.println("Narrowed home!!");
                
                
            Foo foo = fooHome.create();
            System.err.println("Got the EJB!!");
                
            // invoke method on the EJB
            System.out.println("invoking ejb");
            foo.callHello();

            System.out.println("successfully invoked ejb client");
            stat.addStatus("ejbapp ejbclient", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejbapp ejbclient" , stat.FAIL);
        }

        try {
            String url = "http://" + host + ":" + port + 
                "/ejb-stubs-ejbapp/servlet";
            System.out.println("invoking webclient servlet at " + url);
            int code = invokeServlet(url);
            
            if(code != 200) {
                System.out.println("Incorrect return code: " + code);
                stat.addStatus("ejbapp webclient", stat.FAIL);
            } else {
                stat.addStatus("ejbapp webclient", stat.PASS);
            }
        } catch (Exception ex) {
            System.out.println("Jms web test failed.");
            stat.addStatus("ejbapp webclient", stat.FAIL);
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

