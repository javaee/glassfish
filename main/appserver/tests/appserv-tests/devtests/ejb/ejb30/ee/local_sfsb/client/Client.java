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

package com.sun.s1asdev.ejb.ejb30.ee.local_sfsb.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import javax.naming.InitialContext;

import com.sun.s1asdev.ejb.ejb30.ee.local_sfsb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

@EJB(name="ejb/SfulDriverRef", beanInterface=SfulDriver.class,
	mappedName="corbaname:iiop:localhost:3702#ejb/SfulDriverRef")
public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-ee-local_sfsb");
        Client client = new Client(args);
lookupUsingJndi();
        //client.doTest();
        stat.printSummary("ejb-ejb30-ee-local_sfsbID");
    }  
    
    public Client (String[] args) {
    }
    
    @EJB(name="ejb/SfulDriver", beanInterface=SfulDriver.class)
    private static SfulDriver driver;

    public void doTest() {

	driver.initialize();

        try {
            System.out.println("invoking stateless");
            String result = driver.sayHello();
            stat.addStatus("local hello",
		"Hello".equals(result) ? stat.PASS : stat.FAIL);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local hello" , stat.FAIL);
        }

	try {
            boolean result = driver.doRefAliasingTest();
            stat.addStatus("local PRE_refAliasingTest",
		result ? stat.PASS : stat.FAIL);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local refAliasingTest" , stat.FAIL);
        }

	//driver.createManySfulEJBs(16);
	//createManySfulEJBs(16);

	sleepFor(5);
	driver.checkGetRef();
        
	try {
            boolean result = driver.doRefAliasingTest();
            stat.addStatus("local POST_refAliasingTest",
		result ? stat.PASS : stat.FAIL);

	    boolean greeterTest = driver.useSfulGreeter();
            stat.addStatus("local GreeterRefTest",
		result ? stat.PASS : stat.FAIL);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local refAliasingTest" , stat.FAIL);
        }

        System.out.println("test complete");
    }


    private static void sleepFor(int seconds) {
	while (seconds-- > 0) {
	    try { Thread.sleep(1000); } catch (Exception ex) {}
	    System.out.println("Sleeping for 1 second. Still " + seconds + " seconds left...");
	}
    }

    private static void createManySfulEJBs(int count) {
	while (count-- > 0) {
	    try {
		SfulDriver dr = (SfulDriver) (new InitialContext()).lookup("java:comp/env/ejb/SfulDriver");
		dr.initialize();
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	}
    }

    private static void lookupUsingJndi() {
	try {

/*
	    Properties props = new Properties();
	    props.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
	    props.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
	    props.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
	    props.setProperty("org.omg.CORBA.ORBInitialHost", "129.145.133.197");
	    props.setProperty("org.omg.CORBA.ORBInitialPort", "3702");

	    InitialContext ctx = new InitialContext(props);
*/

	    InitialContext ctx = new InitialContext();
	    String name = "ejb/SfulDriverRef";
	    name = "corbaname:iiop:129.145.133.197:3700#" + name;

System.out.println("***Looking up SfulDriver...");
	    SfulDriver driver = (SfulDriver) ctx.lookup("java:comp/env/ejb/SfulDriverRef");
System.out.println("***Got SfulDriver...");
/**/
            stat.addStatus("local LookupUsingJndi", stat.PASS);
	} catch (Exception ex) {
            stat.addStatus("local LookupUsingJndi", stat.FAIL);
	    ex.printStackTrace();
	}
    }

}
