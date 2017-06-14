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

import javax.naming.*;
import javax.rmi.*;

import java.util.Properties;

import javax.ejb.EJBObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import com.sun.enterprise.security.auth.login.LoginCallbackHandler;
import com.sun.enterprise.security.auth.login.LoginContextDriver;
import com.sun.enterprise.security.common.SecurityConstants;

/**
 * This test is for BACKWARD COMPATIBILITY ONLY
 * Clients should NOT be using LoginContextDriver.doClientLogin
 * They should be using the ProgrammaticLogin API
 */
public class LoginContextDriverTest {

	private static String testId="Standalone-client-login-context-driver";
    private static boolean testStatus=false;
    private static SimpleReporterAdapter stat =  new SimpleReporterAdapter();

    private static InitialContext ic = null;
    
    private static MySession1Remote my1r = null;
        
    public static void main(String[] args) {

        stat.addDescription("Security::EJB Method permissions test using " +
                "Login Context Driver Standalone Client");
    
        System.out.println("*** EJBMethod Permission Test using Login Context Driver Standalone client ***");  


        try{
            // Use the default callback handler for login - using textauth (false)
            LoginCallbackHandler handler = new LoginCallbackHandler(false);
            LoginContextDriver.doClientLogin(
                SecurityConstants.USERNAME_PASSWORD, handler);

            // Initialize the Context
            ic = new InitialContext();
            
            System.out.println("EJB lookup start...");
            java.lang.Object objref = ic.lookup("ejb/MySession1Bean");		
            
            MySession1RemoteHome my1rh = (MySession1RemoteHome)
              PortableRemoteObject.narrow(objref, MySession1RemoteHome.class);

            my1r = my1rh.create(); 
            
	     	String retValue = my1r.businessMethod("blah");
            System.out.println("retValue="+retValue);

            testStatus = true;

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if( testStatus) 
                stat.addStatus(testId, stat.PASS);
            else
                stat.addStatus(testId, stat.FAIL);

            stat.printSummary(testId);
        }
    }
    
}
