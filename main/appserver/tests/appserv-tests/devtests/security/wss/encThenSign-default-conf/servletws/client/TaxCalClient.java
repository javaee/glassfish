/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.security.wss.defprovider.servlet.taxcal.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import javax.naming.*;
import javax.xml.rpc.Stub;


public class TaxCalClient {

	private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
	private static String stateTaxEndpoint = null;
	private static String fedTaxEndpoint = null;
	private static String testSuite = "Sec::Servlet_Based_WSS_test Encrypt then Sign";
	private static String testCase = null;
	private static TaxCalServletService taxCalService = null;

	public static void main (String[] args) {

		if(args[0] == null || args[1] == null){
			System.out.println("TaxCal client: Argument missing. Please provide target" +
				 "endpoint address as argument");
			System.exit(1);
		} else {
			stateTaxEndpoint = args[0];
			fedTaxEndpoint = args[1];
		}

		stat.addDescription(testSuite);
		try { 
			TaxCalClient client = new TaxCalClient();
			Context ic = new InitialContext();
			taxCalService = (TaxCalServletService)
				ic.lookup("java:comp/env/service/TaxCalServletService");

			client.callStateTaxService();
			client.callFedTaxService();
			stat.addStatus(testSuite, stat.PASS);
		}catch(Exception e){
			stat.addStatus(testSuite, stat.FAIL);
			e.printStackTrace();
		}

		stat.printSummary(testSuite);
    }
    
	public void callStateTaxService() {
		double income = 85000.00;
		double deductions = 5000.00;

		//String targetEndpointAddress =
		//	"http://localhost:1024/taxcalculator";

		try {

			StateTaxIF taxCalIFPort = taxCalService.getStateTaxIFPort();

			((Stub)taxCalIFPort)._setProperty (Stub.ENDPOINT_ADDRESS_PROPERTY,
				stateTaxEndpoint);

			double stateTax = taxCalIFPort.getStateTax(income, deductions);
			System.out.println("State tax from servlet based TaxCalService :" + stateTax);

			if(stateTax == 24000.00)
				stat.addStatus(testSuite + " StateTaxPort", stat.PASS);
			else
				stat.addStatus(testSuite + " StateTaxPort", stat.FAIL);

		} catch (Exception ex) {
			System.out.println("TaxCalEjbWebservice client failed");
			stat.addStatus(testSuite + " StateTaxPort", stat.FAIL);
			ex.printStackTrace();
		} 
	}

	public void callFedTaxService() {
		double income = 97000.00;
		double deductions = 7000.00;
		try {
			//String targetEndpointAddress =
			//"http://localhost:1024/FindInterestServlet/FindInterest";

			FedTaxIF taxCalIFPort = taxCalService.getFedTaxIFPort();
			((Stub)taxCalIFPort)._setProperty (Stub.ENDPOINT_ADDRESS_PROPERTY,
				fedTaxEndpoint);

			double fedTax = taxCalIFPort.getFedTax(income, deductions);
			System.out.println("Fed tax from Servlet based TaxCalService :" + fedTax);

			if(fedTax == 18000.00)
				stat.addStatus(testSuite + " FedTaxPort", stat.PASS);
			else
				stat.addStatus(testSuite + " FedTaxPort", stat.FAIL);
                
		} catch (Exception ex) {
			System.out.println("TaxCalServletWebService client failed");
			stat.addStatus(testSuite + " FedTaxPort", stat.FAIL);
			ex.printStackTrace();
		} 
	}
}

