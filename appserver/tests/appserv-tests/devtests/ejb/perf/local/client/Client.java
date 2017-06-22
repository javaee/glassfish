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

package com.sun.s1asdev.ejb.perf.local.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.s1asdev.ejb.perf.local.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-perf-local");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-perf-localID");
    }  
    
    public Client (String[] args) {
    }
    
    public void doTest() {

        try {

	    Context ic = new InitialContext();

	    // create EJB using factory from container 
	    java.lang.Object objref = 
                ic.lookup("java:comp/env/ejb/PerformanceApp");

	    System.out.println("Looked up home!!");

	    HelloHome  home = (HelloHome)
                PortableRemoteObject.narrow(objref, HelloHome.class);
	    System.out.println("Narrowed home!!");

	    Hello hr = home.create();
	    System.out.println("Got the EJB!!");

	    // invoke method on the EJB
	    doPerfTest(hr, true);
            doPerfTest(hr, false);

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

    private void doPerfTest(Hello hr, boolean local) 
	throws Exception
    {
	System.out.println("\nStateful Session results (microsec): \twith tx \tno tx:");
	hr.warmup(Common.STATEFUL, local);
	runTests(Common.STATEFUL, hr, local);

	System.out.println("\nStateless Session results (microsec): \twith tx \tno tx:");
	hr.warmup(Common.STATEFUL, local);
	runTests(Common.STATELESS, hr, local);

	System.out.println("\nBMP Entity results (microsec): \t\twith tx \tno tx:");
	hr.warmup(Common.BMP, local);
	runTests(Common.BMP, hr, local);
    }
    
    private void runTests(int type, Hello hr, boolean local)
	throws Exception
    {
        System.out.println("Testing " + (local ? " Local " : 
                                         " Collocated Remote ") +
                           "performance...");

        if( local ) {
            System.out.println("notSupported : \t\t\t\t" 
                               + hr.notSupported(type, true) + "\t\t" +
                               + hr.notSupported(type, false) );
            System.out.println("supports : \t\t\t\t" 
                               + hr.supports(type, true) + "\t\t" +
                               + hr.supports(type, false) );
            System.out.println("required : \t\t\t\t" 
                               + hr.required(type, true) + "\t\t" +
                               + hr.required(type, false) );
            System.out.println("requiresNew : \t\t\t\t" 
                               + hr.requiresNew(type, true) + "\t\t" +
                               + hr.requiresNew(type, false) );
            System.out.println("mandatory : \t\t\t\t" 
                               + hr.mandatory(type, true));
            System.out.println("never : \t\t\t\t\t\t" 
                               + hr.never(type, false) );
        } else {
            System.out.println("notSupported : \t\t\t\t" 
                               + hr.notSupportedRemote(type, true) + "\t\t" +
                               + hr.notSupportedRemote(type, false) );
            System.out.println("supports : \t\t\t\t" 
                               + hr.supportsRemote(type, true) + "\t\t" +
                               + hr.supportsRemote(type, false) );
            System.out.println("required : \t\t\t\t" 
                               + hr.requiredRemote(type, true) + "\t\t" +
                               + hr.requiredRemote(type, false) );
            System.out.println("requiresNew : \t\t\t\t" 
                               + hr.requiresNewRemote(type, true) + "\t\t" +
                               + hr.requiresNewRemote(type, false) );
            System.out.println("mandatory : \t\t\t\t" 
                               + hr.mandatoryRemote(type, true));
            System.out.println("never : \t\t\t\t\t\t" 
                               + hr.neverRemote(type, false) );
        }
    }
}

