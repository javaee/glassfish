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
import javax.xml.ws.Service;
import javax.xml.ws.BindingProvider;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import service.web.example.calculator.*;
import javax.xml.ws.*;

public class SOAPWebConsumer {
    @WebServiceRef(wsdlLocation="http://localhost:8080/rpc-literal-bundled-wsdl-bc-consumer-se-provider/webservice/CalculatorService?WSDL")
    static CalculatorService service;
    private static SimpleReporterAdapter stat =
            new SimpleReporterAdapter("appserv-tests");

	private static String testId = "jbi-serviceengine/rpc-literal/bc_consumer_sc_provider/bundled-wsdl";

    public static void main(String[] args) {
        stat.addDescription(testId);
        SOAPWebConsumer client = new SOAPWebConsumer();
        client.addUsingSOAPConsumer();
        stat.printSummary(testId);
    }
    
    private void addUsingSOAPConsumer() {
        Calculator port= null;
        try {
            
            System.out.println(" After creating CalculatorService");
            
            port = service.getCalculatorPort();
            System.out.println(" After getting port");
            
            // Get Stub
            BindingProvider stub = (BindingProvider)port;
            String endpointURI ="http://localhost:12011/calculatorendpoint";
            stub.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                    endpointURI);
            System.out.println(" After setting endpoint address URI");
            
            IncomeTaxDetails itDetails = new IncomeTaxDetails();
            itDetails.setFirstName( "bhavani");
            itDetails.setLastName("s");
            itDetails.setAnnualIncome( 400000);
            itDetails.setStatus("salaried");
            
            long startTime = System.currentTimeMillis();
            long ret = 0;
            // Make 100 calls to see how much time it takes.
            //for(int i=0; i<1000; i++) {
            ret = port.calculateIncomeTax(itDetails
                    , itDetails
                    , itDetails
                    , itDetails
                    , itDetails
                    , itDetails
                    , itDetails
                    , itDetails
                    , itDetails
                    , itDetails
                    );
            //}
            long timeTaken = System.currentTimeMillis() - startTime;
            
            //int ret = port.add(1, 2);
            printSuccess("Your income tax is : Rs ", ret, timeTaken);
            startTime = System.currentTimeMillis();
            int k = port.add(505, 50);
            timeTaken = System.currentTimeMillis() - startTime;
            printSuccess("Sum of 505 and 50 is : ", k, timeTaken);
            
            startTime = System.currentTimeMillis();
            String hi = port.sayHi();
            timeTaken = System.currentTimeMillis() - startTime;
            printSuccess("Output from webservice : ",  hi, timeTaken);
            
			/*
            startTime = System.currentTimeMillis();
            port.printHi();
            timeTaken = System.currentTimeMillis() - startTime;
            printSuccess("SUCCESS : ",  "Webservice has successfully printed hi in server.log", timeTaken);
            
            startTime = System.currentTimeMillis();
            port.printHiToMe("JavaEEServiceEngine");
            timeTaken = System.currentTimeMillis() - startTime;
            printSuccess("SUCCESS : ",  "Webservice has successfully printed hi to me in server.log", timeTaken);
            */
            stat.addStatus(testId, stat.PASS);
            
        } catch(java.lang.Exception e) {
            stat.addStatus(testId, stat.FAIL);
        }
    }
    
    public void printFailure(String errMsg) {
        System.out.println("<html>");
        System.out.println("<head>");
        System.out.println("<title>TestServlet</title>");
        System.out.println("</head>");
        System.out.println("<body>");
        System.out.println("<p>");
        System.out.println("Test FAILED: Error message - " + errMsg);
        System.out.println("</p>");
        System.out.println("</body>");
        System.out.println("</html>");
    }
    
    public void printSuccess(String message, long result, long timeTaken) {
        System.out.println("\n\n");
        System.out.println(message + result);
        System.out.println("Time taken to invoke the endpoint operation is  :  " + timeTaken + " milliseconds.");
    }
    
    public void printSuccess(String message, String result, long timeTaken) {
        System.out.println("\n\n");
        System.out.println(message + result);
        System.out.println("Time taken to invoke the endpoint operation is  :  " + timeTaken + " milliseconds.");
    }
    
}
