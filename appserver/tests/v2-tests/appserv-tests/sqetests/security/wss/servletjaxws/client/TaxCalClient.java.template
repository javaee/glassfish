/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.sqe.security.wss.annotations.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.appserv.sqe.security.taxws.Tax;
import com.sun.appserv.sqe.security.taxws.TaxService;
import javax.xml.ws.WebServiceRef;
import javax.xml.rpc.Stub;

/**
 * This is AppClient program that access StateTaxEJB and FedTax EJB Webservices.
 * It expects StateTax webservice endpoint and FedTax endpoint URLs.
 * This client is accessed in the ant run target after configuring the webservices
 * message security at system level or applevel.  
 *
 * @version 1.1  05 Aug 2005
 * @author Jagadesh Munta
 */
        
public class TaxCalClient {
    /*
     * Tests the getStateTax and getFedTax with expected values. If value 
     * matched,then test PASSED else FAILED.
     */
 	private static SimpleReporterAdapter stat = new SimpleReporterAdapter();
            // J2EE simple reporter for logging the test status.
	private static String taxEndpoint = null;
	private static String testSuite = "sec-wss-annotate-servletendpoint";
	private static String testCase = null;
        @WebServiceRef(wsdlLocation="http://localhost:8080/wss-tax-web/wss/TaxService?wsdl")
        static TaxService service;

    public static void main (String[] args) {

        if(args.length<1){
            System.out.println("TaxCal client: Argument missing."+
                    " Please provide target" +
                     "endpoint address as argument");
                System.exit(1);
        } else {
                taxEndpoint = args[0];
        }

        stat.addDescription("Security-WSS-ejb webservice"); 
        try { 
            TaxCalClient client = new TaxCalClient();
            client.callTaxService();
        }catch(Exception e){
                e.printStackTrace();
        }

        stat.printSummary(testSuite);
    }
    

    public void callTaxService() {
        double income = 97000.00;
        double deductions = 7000.00;
        double expectedTax = 18000.00;
        String testStatus = "fail";
        
        try {
            if (service!=null) {
                Tax port = service.getTaxPort();
                //((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                //        taxEndpoint);

                double fedTax = port.getFedTax(income, deductions);
                System.out.println("Fed tax from annotations based TaxCalService endpoint:" +
                        fedTax);

                if(fedTax == expectedTax) {
                        testStatus = stat.PASS;
                } else {
                        testStatus = stat.FAIL;
                }
            }else {
                System.out.println("Error: Not able to get the service and is null!");
                testStatus = stat.FAIL;                
            }

        } catch (Exception ex) {
                System.out.println("TaxCal client failed");
                ex.printStackTrace();
                testStatus = stat.FAIL;
        } finally {
                stat.addStatus(testSuite+"-getFedTax" , testStatus);            
        } 
    }
}

