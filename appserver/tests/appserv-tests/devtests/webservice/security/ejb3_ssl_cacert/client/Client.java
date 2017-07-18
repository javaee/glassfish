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

        @WebServiceRef
        static HelloImplService service;

        public static void main(String[] args) {
            boolean successExpected=true;
            String description;
	    if (args.length>1) {
                if (args[0].compareToIgnoreCase("Failure")==0) {
                    successExpected=false;
                } 
                description=args[1];
            } else {
                successExpected = false;
		description="webservices-ejb-rolesAllowed-annotation-negative";
	    }
            System.out.println("Starting test " + description);
	    stat.addDescription(description);
            Client client = new Client();
            client.doTest(description, successExpected);
	    stat.printSummary(description);
       }

       public void doTest(String desc, boolean successExpected) {
           
           try {
               HelloImpl port = service.getHelloImplPort();
               
               String ret = port.sayHello("Appserver Tester !");
               if(ret.indexOf("WebSvcTest-Hello") == -1) {
                   System.out.println("Unexpected greeting " + ret);
                   stat.addStatus(desc, stat.FAIL);
                   return;
               }
               System.out.println("WebService said " + ret);
               if (successExpected) {
                   stat.addStatus(desc, stat.PASS);
               } else {
                   System.out.println("Was expected failure, go an answer...");
                   stat.addStatus(desc, stat.FAIL);
               }
           } catch(Throwable t) {
               if (successExpected) {
                   System.out.println("method invocation failed - TEST FAILED");
                   stat.addStatus(desc, stat.FAIL);
               } else {
                   System.out.println("method invocation failed - good...");
                   stat.addStatus(desc, stat.PASS);
               }
           }
       }
}

