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

package com.sun.s1asdev.ejb.stubs.proxy.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.stubs.proxy.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-stubs-proxy");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-stubs-proxyID");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {

        try {

	    Context ic = new InitialContext();

	    // create EJB using factory from container 
	    java.lang.Object objref = 
                ic.lookup("java:comp/env/ejb/ProxyApp");

	    System.out.println("Looked up home!!");

	    HelloHome  home = (HelloHome)
                PortableRemoteObject.narrow(objref, HelloHome.class);
	    System.out.println("Narrowed home!!");

	    Hello hr = home.create();
	    System.out.println("Got the EJB!!");

            hr.testPassByRef();

	    // invoke method on the EJB
	    doProxyTest(hr);

            testExceptions(hr);

            hr.shutdown();

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

    private void testExceptions(Hello h) throws Exception {

        try {
            h.throwException();
        } catch(Exception e) {
            if( e.getClass() == Exception.class ) {
                System.out.println("Successfully caught exception " + 
                                   e.getClass() + " " + e.getMessage());
            } else {
                throw e;
            }
        }

        try {
            h.throwAppException1();
            throw new Exception("didn't get exception for testException2");
        } catch(javax.ejb.FinderException e) {
            System.out.println("Successfully caught exception " + 
                               e.getClass() + " " + e.getMessage());
        }

        try {
            h.throwAppException2();
            throw new Exception("didn't get exception for testException3");
        } catch(javax.ejb.FinderException e) {
            System.out.println("Successfully caught exception " + 
                               e.getClass() + " " + e.getMessage());
        }

    }

    private void testNotImplemented(Common c) {
        try {
            c.notImplemented();
        } catch(Exception e) {
            System.out.println("Successfully caught exception when calling" +
                               " method that is not implemented" +
                               e.getMessage());
        }
    }

    private void testNotImplemented(CommonRemote cr) {
        try {
            cr.notImplemented();
        } catch(Exception e) {
            System.out.println("Successfully caught exception when calling" +
                               " method that is not implemented" +
                               e.getMessage());
        }
    }

    private void doProxyTest(Hello hr) 
	throws Exception
    {
	System.out.println("\nStateful Session results (microsec): \twith tx \tno tx:");
	hr.warmup(Common.STATEFUL);
	runTests(Common.STATEFUL, hr);

	System.out.println("\nStateless Session results (microsec): \twith tx \tno tx:");
	hr.warmup(Common.STATEFUL);
	runTests(Common.STATELESS, hr);

	System.out.println("\nBMP Entity results (microsec): \t\twith tx \tno tx:");
	hr.warmup(Common.BMP);
	runTests(Common.BMP, hr);
    }

    private void runTests(int type, Hello hr)
	throws Exception
    {
      
        hr.notSupported(type, true);
        hr.notSupported(type, false);
        hr.supports(type, true);
	hr.supports(type, false);
        hr.required(type, true);
	hr.required(type, false);
        hr.requiresNew(type, true);
	hr.requiresNew(type, false);
        hr.mandatory(type, true);
	hr.never(type, false);
    }
}

