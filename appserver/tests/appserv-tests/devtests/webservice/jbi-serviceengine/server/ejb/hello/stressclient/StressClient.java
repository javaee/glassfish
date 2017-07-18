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

import com.example.subtractor.SubtractorService;
import com.example.subtractor.Subtractor;
 
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class StressClient extends Thread { 

    int clientId;
    Hello port;
    Hi port1;
    Subtractor port2;

	private static String testId = "jbi-serviceengine/server/ejb/hello/stressclient";

     static long startTime  = 0;
     static int minutesToRun = 0;
     static long endTime = 0;
		     

       private SimpleReporterAdapter stat ;
   
       HelloEJBService service;
       HiEJBService service1;
       SubtractorService service2;

       StressClient(int i,SimpleReporterAdapter stat) {
           System.out.println("Instantiating a stress client");
	   //stat.addDescription("jsr108-serverside-webservices-ejb-noname-annotation");
           clientId = i;
	   this.stat = stat;
       }

       void setServiceHandle( HelloEJBService ser, HiEJBService ser1, SubtractorService ser2){
           service = ser;
	   service1 = ser1;
	   service2 = ser2;
       }

       static void setTimeToRun( int minutes ) {
           minutesToRun = minutes;
	   startTime = System.currentTimeMillis();
       }

       public void run() {
           port = service.getHelloEJBPort();
           // Get Stub
           BindingProvider stub = (BindingProvider)port;
           String endpointURI ="http://localhost:12015/HelloEJBPort";
           stub.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpointURI);
           System.out.println(clientId + ":" + " After setting endpoint address URI");

	    port1 = service1.getHiEJBPort();
	    BindingProvider stub1 = (BindingProvider)port1;
            String endpointURI1 ="http://localhost:12017/HiEJBPort";
            stub1.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
        		                  endpointURI1);

	     port2 = service2.getSubtractorPort();
	     BindingProvider stub2 = (BindingProvider)port2; 
             String endpointURI2 ="http://localhost:12018/subtractorendpoint";
             stub2.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
		                          endpointURI2);
		 

	   while((endTime - startTime) < minutesToRun*60*1000) {
               // Iterative tester for stress, per Thread
               for ( int i=0 ; i < 10; i++)
	           doTest("Stress Tester",i);
	       endTime = System.currentTimeMillis();
	   }
       }
         
       public void doTest(String toSend, int id) {
            try {
	        Integer i = (Integer)id;

                String ret = port.sayHello(toSend+i);
		String ret1 = port1.sayHi(toSend+i);
		int res = port2.add(5,2);

		if(ret.indexOf("WebSvcTest-Hello") == -1 || ret.indexOf(i.toString()) == -1) {
                    System.out.println(clientId + ":" + "Unexpected greeting " + ret);
                    stat.addStatus(testId, stat.FAIL);
	            stat.printSummary(testId);
                    return;
		}
		//pass();
                System.out.println(clientId + ":" + ret);

		 if(ret1.indexOf("WebSvcTest-Hi") == -1 || ret1.indexOf(i.toString()) == -1) {
	       	     System.out.println(clientId + ":" + "Unexpected greeting " + ret1);
		     stat.addStatus(testId, stat.FAIL);
		     return;
		 }
		 //pass();
		 System.out.println(clientId + ":" + ret1);

		 if( res != 3) {
	              System.out.println(clientId + ":" + "Unexpected result " + res);
		      stat.addStatus(testId, stat.FAIL);
		      return;
	         }
                 //pass();
		 System.out.println(clientId + "Result is :" + res);

	     }  catch(Exception e) {
                stat.addStatus(testId, stat.FAIL);
	        stat.printSummary(testId);
                e.printStackTrace();
            }
       }

    private void pass() {
        stat.addStatus(testId, stat.PASS);
    }

    private void fail() {
        stat.addStatus(testId, stat.FAIL);
    }


}

