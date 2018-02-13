/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1peqe.security.authoriz.methodperms;

import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;
import com.sun.enterprise.security.LoginContext;
import com.sun.enterprise.security.LoginException;
import java.rmi.RemoteException;
import java.security.*;

import com.sun.ejte.ccl.reporter.*;

public class MethodPermClient {
    private static SimpleReporterAdapter stat=new SimpleReporterAdapter("appserv-tests");
    public static void main (String[] args) {

        MethodPermClient client = new MethodPermClient(args);
        System.out.print("-->EJB method permissions!");
        stat.addDescription("EJB method permissions");
        client.doTest();
	stat.printSummary("Authorize_methodperms");
        
    }
    
    public MethodPermClient (String[] args) {
        //super(args);
    }
    
    public String doTest() {
        
	MethodPermRemote hr=null;
        String res=null;
        Context ic = null;
        LoginContext lc=null;
        MethodPermRemoteHome home=null;
    	try{
	    ic = new InitialContext();
//	    Security.setProperty("policy.allowSystemProperty", "true");
	    lc = new LoginContext();
	    lc.login("j2ee","j2ee");	
		// create EJB using factory from container 
            java.lang.Object objref = ic.lookup("MyMethodPerm");
		
	    System.err.println("Looked up home!!");
		
	     home = (MethodPermRemoteHome)PortableRemoteObject.narrow(
										   objref, MethodPermRemoteHome.class);
	    System.err.println("Narrowed home!!");
				
		hr = home.create(helloStr);
		System.err.println("Got the EJB!!");
            }catch (Exception ex) {
               ex.printStackTrace();
               //res = Tester.kTestFailed;
               stat.addStatus("Sec::Authorize_methodperms Testsuite",stat.FAIL);
               res="FAIL";
            }
		// invoke 3 overloaded methods on the EJB
             try{
		System.out.println ("Calling authorized method - authorizedMethod");
		System.out.println(hr.authorizedMethod());

		System.out.println ("Calling authorized method - authorizedMethod - hi 129");
		System.out.println(hr.authorizedMethod("Hi", 129));
		
		System.out.println ("Calling authorized method - authorizedMethod 115");
		System.out.println(hr.authorizedMethod(115));

		//res  = Tester.kTestPassed;
                stat.addStatus("Sec::Authorize_methodperms Test1-Calling authorized method",stat.PASS);
		
	    } catch(Exception re){
                re.printStackTrace();
		System.out.println("Test Failed");
                stat.addStatus("Sec::Authorize_methodperms Test1-Calling authorized method",stat.FAIL);
                res="FAIL";

		//return Tester.kTestFailed;
	    }
	    try{
		// invoke unauthorized method on the EJB
		System.out.println ("Calling unauthorized method - sayGoodBye");
		System.out.println(hr.sayGoodbye());
		System.out.println (" Test failed: able to call good bye method!"); 
		//return Tester.kTestFailed;
                stat.addStatus("Sec::Authorize_methodperms Test2-Calling unauthorized method",stat.FAIL);
                res="FAIL";

	    } catch(Exception gbye){
		//res = Tester.kTestPassed;
                stat.addStatus("Sec::Authorize_methodperms Test2-Calling unauthorized method",stat.PASS);
                res="PASS";

	    }

            try{
                // invoke method on the EJB not authorized
                System.out.println ("Calling unauthorized method - unauthorizedMethod");
                hr.unauthorizedMethod();
            
                //res  = Tester.kTestFailed;
                stat.addStatus("Sec::Authorize_methodperms Test3-expected Exception-Calling unauthorized method",stat.FAIL);
                res="FAIL";
            
            } catch (RemoteException remex) {
                System.out.println("Caught expected RemoteException from unauthorizedMethod()");
                //res  = Tester.kTestPassed;
                stat.addStatus("Sec::Authorize_methodperms Test3-expected Exception-Calling unauthorized method",stat.PASS);
                res="PASS";
            } catch(Exception ex){
                stat.addStatus("Sec::Authorize_methodperms Test3-expected Exception-Calling unauthorized method",stat.FAIL);
                res="FAIL";
            }
    	return res;
        
    }

    
    public final static String helloStr = "Hello MethodPerm!!!";
}

