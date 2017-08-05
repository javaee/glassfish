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



package myclient;

import javax.naming.*;
import javax.xml.rpc.Stub;

import com.sun.ejte.ccl .reporter.SimpleReporterAdapter;

public class FindInterestClientNonHttpBC {

    private double balance = 300.00;
    private double period = 3.5;

    private static SimpleReporterAdapter status = new SimpleReporterAdapter();
    private static String testId = "jbi-serviceengine/jax-rpc/consumer";

    public FindInterestClientNonHttpBC() {
	status.addDescription(testId);
    }

    public static void main (String[] args) {

        FindInterestClientNonHttpBC client = new FindInterestClientNonHttpBC();

        client.doTest();
  //      client.doServletTest();
    }
    
    public double doTest() {

        String targetEndpointAddress =
			"http://localhost:8080/findintr/FindInterest";

    	try {
            Context ic = new InitialContext();
            FindInterest findIntrService = (FindInterest)
                    ic.lookup("java:comp/env/service/FindInterest");

            InterestIF interestIFPort = findIntrService.getInterestIFPort();

            ((Stub)interestIFPort)._setProperty (Stub.ENDPOINT_ADDRESS_PROPERTY,
                 				targetEndpointAddress);

	    double interest = interestIFPort.calculateInterest(balance, period);
            System.out.println("Interest on $300 for a period of 3.5 years is "
				+ interest);
                
	    if (interest == 105.0) {
		status.addStatus(testId +"1 : EJB Endpoint Test", status.PASS);
	    }
            return interest;

    	} catch (Exception ex) {
		status.addStatus(testId +"1 : EJB Endpoint and Servlet Endpoint Test", status.FAIL);
            System.out.println("findintr client failed");
            ex.printStackTrace();
	} 
        return -1;
    }

   /* public void doServletTest() {
    	try {
	    String targetEndpointAddress =
		"http://localhost:8080/FindInterestServlet/FindInterest";

            Context ic = new InitialContext();
            FindInterest findIntrService = (FindInterest)
                    ic.lookup("java:comp/env/service/FindInterest");

            InterestIF interestIFPort = findIntrService.getInterestIFPort();

            ((Stub)interestIFPort)._setProperty (Stub.ENDPOINT_ADDRESS_PROPERTY,
						targetEndpointAddress);

	    double interest = interestIFPort.calculateInterest(balance, period);

            System.out.println("Interest on $300 for a period of 3.5 years is "
				+ interest);
                
	    if (interest == 210.0) {
		status.addStatus(TEST_SUITE_ID+"2 : EJB Endpoint and Servlet Endpoint Test", status.PASS);
	    }
    	} catch (Exception ex) {
		status.addStatus(TEST_SUITE_ID+"2 : EJB Endpoint and Servlet Endpoint Test", status.FAIL);
            System.out.println("findintr client failed");
            ex.printStackTrace();
	} 
	status.printSummary("JSR109 - FindInterestTest");
    }*/
}

