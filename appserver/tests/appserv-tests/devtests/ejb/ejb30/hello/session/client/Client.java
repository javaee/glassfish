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

package com.sun.s1asdev.ejb.ejb30.hello.session.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import com.sun.s1asdev.ejb.ejb30.hello.session.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-hello-session");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-hello-sessionID");
    }  
    
    public Client (String[] args) {
    }

    private static @EJB Sful sful;
    
    private static @EJB(mappedName="com.sun.s1asdev.ejb.ejb30.hello.session.Sless") Sless sless;

    //
    // NOTE: Token 3700 will be replaced in @EJB annotations below 
    // with the value of the port from config.properties during the build
    //
    private static @EJB(mappedName="corbaname:iiop:localhost:3700#com.sun.s1asdev.ejb.ejb30.hello.session.Sless") Sless sless2;

    private static @EJB(mappedName="corbaname:iiop:localhost:3700#java:global/ejb-ejb30-hello-sessionApp/ejb-ejb30-hello-session-ejb/SlessEJB!com.sun.s1asdev.ejb.ejb30.hello.session.Sless") Sless sless3;

    private static @EJB(mappedName="corbaname:iiop:localhost:3700#java:global/ejb-ejb30-hello-sessionApp/ejb-ejb30-hello-session-ejb/SlessEJB") Sless sless4;

    public void doTest() {

        try {

//            System.out.println("Creating InitialContext()");
//	    InitialContext ic = new InitialContext();

//	    org.omg.CORBA.ORB orb = (org.omg.CORBA.ORB) ic.lookup("java:comp/ORB"); 

//	    Sful sful = (Sful) ic.lookup("com.sun.s1asdev.ejb.ejb30.hello.session.Sful");
//	    Sless sless = (Sless) ic.lookup("com.sun.s1asdev.ejb.ejb30.hello.session.Sless");

            System.out.println("invoking stateful");
            sful.hello();

            System.out.println("invoking stateless");
            sless.hello();
            System.out.println("invoking stateless2");
	    sless2.hello();
            System.out.println("invoking stateless3");
	    sless3.hello();
            System.out.println("invoking stateless4");
	    sless4.hello();

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

}

