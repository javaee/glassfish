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

import javax.xml.ws.WebServiceRefs;
import javax.xml.ws.WebServiceRef;

import servlet_endpoint.ServletHelloService;
import servlet_endpoint.ServletHello;

import ejb_endpoint.WSHelloEJBService;
import ejb_endpoint.WSHello;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

@WebServiceRefs({
        @WebServiceRef(name="service/MyServletService", type=servlet_endpoint.ServletHelloService.class, wsdlLocation="http://HTTP_HOST:HTTP_PORT/webservicerefs/webservice/ServletHelloService?WSDL"),
        @WebServiceRef(name="service/MyEjbService", type=ejb_endpoint.WSHelloEJBService.class, wsdlLocation="http://HTTP_HOST:HTTP_PORT/WSHelloEJBService/WSHelloEJB?WSDL") })
public class Client {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");

        public static void main(String[] args) {
	    stat.addDescription("webservicerefs-test");
            Client client = new Client();
            client.doServletTest();
            client.doEjbTest();
	    stat.printSummary("webservicerefs-test");
       }

       public void doServletTest() {
            try {
                javax.naming.InitialContext ic = new javax.naming.InitialContext();
                ServletHelloService svc = (ServletHelloService)ic.lookup("java:comp/env/service/MyServletService");
                ServletHello port = svc.getServletHelloPort();
                for (int i=0;i<10;i++) {
                    String ret = port.sayServletHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-Servlet-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("WebServiceRefs-Servlet-Endpoint", stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus("WebServiceRefs-Servlet-Endpoint", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("WebServiceRefs-Servlet-Endpoint", stat.FAIL);
            }
       }

       public void doEjbTest() {
            try {
                javax.naming.InitialContext ic = new javax.naming.InitialContext();
                WSHelloEJBService svc = (WSHelloEJBService)ic.lookup("java:comp/env/service/MyEjbService");
                WSHello port = svc.getWSHelloEJBPort();
                for (int i=0;i<10;i++) {
                    String ret = port.sayEjbHello("Appserver Tester !");
		    if(ret.indexOf("WebSvcTest-EJB-Hello") == -1) {
                        System.out.println("Unexpected greeting " + ret);
                        stat.addStatus("WebServiceRefs-EJB-Endpoint", stat.FAIL);
                        return;
		    }
                    System.out.println(ret);
                }
                stat.addStatus("WebServiceRefs-EJB-Endpoint", stat.PASS);
            } catch(Exception e) {
                e.printStackTrace();
                stat.addStatus("WebServiceRefs-EJB-Endpoint", stat.FAIL);
            }
       }
}

