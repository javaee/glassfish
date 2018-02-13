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

package jaxr;


import java.io.*;
import java.util.*;
import javax.ejb.EJBHome;
import jaxr.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.ORB;

public class JaxrClient {

    String company = "Sun";
    String url = null;
    String ctxFactory = null;
    String jndiName = null;
    public static void main (String[] args) {
        JaxrClient client = new JaxrClient(args);
        client.doTest();
    }
    
    public JaxrClient (String[] args) {
             if (args.length == 3) {
                url = args[0];
                ctxFactory = args[1];
                jndiName = args[2];
            }
    }

    public String doTest() {
        
        String res = "fail";
        
    	try {          
                Properties env = new Properties();
                env.put("java.naming.provider.url", url);
                env.put("java.naming.factory.initial", ctxFactory);
                // Initialize the Context with JNDI specific properties
                InitialContext context = new InitialContext(env);
                System.out.println("Context Initialized with " +
                                   "URL: " + url + ", Factory: " + ctxFactory);
                // Create Home object
                System.out.println("*****"+jndiName);
                java.lang.Object obj = context.lookup(jndiName);
                // create EJB using factory from container 
                //java.lang.Object objref = ic.lookup("MyJaxr");

                System.out.println("Looked up home!!");

                JaxrHome  home = (JaxrHome)PortableRemoteObject.narrow(
			                     obj, JaxrHome.class);
                System.out.println("Narrowed home!!");

                JaxrRemote hr = home.create();
                System.out.println("Got the EJB!!");

                // invoke method on the EJB
                System.out.println (" Looking up company information for "+company); 
                System.out.println(hr.getCompanyInformation(company));
                hr.remove();
	} catch(NamingException ne){
            System.out.println("Caught exception while initializing context.\n");
            ne.printStackTrace();
	    System.out.println (" Test Failed !"); 
            return res;
	} catch(Exception re) {
            re.printStackTrace();
	    System.out.println (" Test Failed !"); 
            return res;
	} 
        res = "pass";
	System.out.println (" Test Passed !"); 
        return res;
        
    }
    
}

