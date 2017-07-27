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

package stressclient;

import javax.xml.ws.WebServiceRef;
import javax.xml.ws.BindingProvider;

import endpoint.jaxws.HelloEJBService;
import endpoint.jaxws.Hello;
import endpoint.jaxws.HiEJBService;
import endpoint.jaxws.Hi;
import com.example.subtractor.Subtractor;
import com.example.subtractor.SubtractorService;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class StressSOAPEjbConsumer {

        private static SimpleReporterAdapter stat =
                new SimpleReporterAdapter("appserv-tests");
	private static String testId = "jbi-serviceengine/server/ejb/hello/stressclient";

        @WebServiceRef
        static HelloEJBService service;

	 @WebServiceRef
         static HiEJBService service1;

         @WebServiceRef(wsdlLocation="http://localhost:8080/subtractorservice/webservice/SubtractorService?WSDL")
         static SubtractorService service2;


        static long startTime  = 0;
	static int minutesToRun = 0;
	static long endTime = 0;

        StressSOAPEjbConsumer() {
	    //create multiple instances of iterative test clients.
            StressClient clients[] = new StressClient[100];
            for(int i = 0 ; i < 100 ; i++) {
	        clients[i] = new StressClient(i,stat);
                clients[i].setServiceHandle(service,service1,service2);
                clients[i].start();
            }
	}
        public static void main(String[] args) throws Exception {
           stat.addDescription(testId);
	   
	   if( args != null && args.length > 0 && args[0] != null) 
	       try {
                   minutesToRun = Integer.parseInt(args[0]);
	       } catch(NumberFormatException numEx) {
		   minutesToRun = 3;
	       }
	   System.out.println("Time to run is: "+minutesToRun); 
	   Thread.currentThread().sleep(2000);
	   StressClient.setTimeToRun(minutesToRun);
           StressSOAPEjbConsumer stressClient = new StressSOAPEjbConsumer();
           //stat.addStatus("jsr108-serverside-webservices-ejb-noname-annotation", stat.PASS);
	   //stat.printSummary("jsr108-serverside-webservices-ejb-noname-annotation");
       }
}
