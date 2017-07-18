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

package client;

import java.util.Map;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.BindingProvider;

import endpoint.HelloImplService;
import endpoint.HelloImpl;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(wsdlLocation="http://HTTP_HOST:HTTP_PORT/HelloImplService/HelloImpl?WSDL")
        static HelloImplService service;

        public static void main(String[] args) {
            String username=null;
            String password=null;
            boolean successExpected=true;
            String description;
	    if (args.length>1) {
                username=args[0];
                password=args[1];
		description="webservices-ejb-rolesAllowed-annotation-positive";
                if (args.length>2) {
                    description="webservices-ejb-rolesAllowed-annotation-negative-2";
                    successExpected = !(args[2].equalsIgnoreCase("FAILURE"));
                } 
            } else {
                successExpected = false;
		description="webservices-ejb-rolesAllowed-annotation-negative";
	    }
	    stat.addDescription(description);
            Client client = new Client();
            client.doTest(description, username, password, successExpected);
	    stat.printSummary(description);
       }

       public void doTest(String desc, String username, String password, boolean successExpected) {
           
            try {
                HelloImpl port = service.getHelloImplPort();
                if (username!=null && password!=null) {
                    BindingProvider bd = (BindingProvider) port;
                    Map<String, Object> requestContext = bd.getRequestContext();
                    requestContext.put("javax.xml.ws.security.auth.username",username);
                    requestContext.put("javax.xml.ws.security.auth.password",password);
                }
                
                // @PermitAll invocation, it should always work
                try {
                    String ret = port.permitAll("Appserver Tester !");
                    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus(desc, stat.FAIL);
                        return;
                    }
                    System.out.println("@PermitAll method invocation passed - good...");
                } catch(Exception e) {
                    if (successExpected) {
                        System.out.println("@PermitAll method invocation failed - TEST FAILED");
                        stat.addStatus(desc, stat.FAIL);
                    } else {
                        System.out.println("@PermitAll method invocation failed - good...");                        
                    }
                }
                
                // @DenyAll invocation, it should always faile
                try {
                    String ret = port.denyAll("Appserver Tester !");
                    if(ret.indexOf("WebSvcTest-Hello") != -1) {
                        System.out.println("@DenyAll Method invocation succeeded, should have failed - TEST FAILED ");
                        stat.addStatus(desc, stat.FAIL);
                        return;
                    }
                } catch(Exception e) {
                    System.out.println("@DenyAll method invocation failed - good...");
                }
                
                // role based invocation
                String ret = port.roleBased("Appserver Tester !");
                if(ret.indexOf("WebSvcTest-Hello") == -1) {
                    System.out.println("Unexpected greeting " + ret);
                    stat.addStatus(desc, stat.FAIL);
                    return;
                }
                System.out.println(ret);
		if (successExpected)
	                stat.addStatus(desc, stat.PASS);
		else 
	                stat.addStatus(desc, stat.FAIL);
                
            } catch(Throwable t) {
		if (successExpected) {
	                t.printStackTrace();
	                stat.addStatus(desc, stat.FAIL);
		} else {
			System.out.println("Got expected failure " + t.getMessage()); 
	                stat.addStatus(desc, stat.PASS);
		}
            }
       }
}

