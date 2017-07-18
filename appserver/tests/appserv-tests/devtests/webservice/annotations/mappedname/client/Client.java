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

import javax.xml.ws.WebServiceRef;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

   /*     private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        @WebServiceRef(name="ignoredName", mappedName="MyMappedName", wsdlLocation="http://HTTP_HOST:HTTP_PORT/Hello/HelloService?WSDL")
        static HelloService service;

        public static void main(String[] args) {
	    stat.addDescription("webservices-mapped-name");
            Client client = new Client();
            client.doTest(args);
	    stat.printSummary("webservices-mapped-name");
       }

       public void doTest(String[] args) {
	    boolean gotEx = false;
            try {
		javax.naming.InitialContext ic = new javax.naming.InitialContext();
		Object res = ic.lookup("java:comp/env/ignoredName");
	    } catch(Exception ex) {
		System.out.println("Caught Expected exception - " + ex.getMessage());
		gotEx = true;
	    } 
	    if(!gotEx) {
		System.out.println("Mapped name not mapped as expected");
                stat.addStatus("Simple-mapped-name", stat.FAIL);
		return;
	    }
            try {
		javax.naming.InitialContext ic = new javax.naming.InitialContext();
		Object res = ic.lookup("java:comp/env/MyMappedName");
                Hello port = service.getHelloPort();
                for (int i=0;i<10;i++) {
                    String ret = port.sayHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("Simple-Annotation", stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus("Simple-mapped-name", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("Simple-mapped-name", stat.FAIL);
            }
       }
*/
}

