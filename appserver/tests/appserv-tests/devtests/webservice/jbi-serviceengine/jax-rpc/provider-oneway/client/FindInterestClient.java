/*
 * $Id: FindInterestClient.java,v 1.1 2006/04/24 05:55:58 mu125243 Exp $ 
 */
/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package myclient;

import javax.naming.*;
import javax.xml.rpc.Stub;

import com.sun.ejte.ccl .reporter.SimpleReporterAdapter;

public class FindInterestClient {

    private double balance = 300.00;
    private double period = 3.5;

    private static SimpleReporterAdapter status = new SimpleReporterAdapter();

    private static String testId = "jbi-serviceengine/jax-rpc/provider-oneway";

    public FindInterestClient() {
	status.addDescription(testId);
    }

    public static void main (String[] args) {

        FindInterestClient client = new FindInterestClient();

        client.doTest();
  //      client.doServletTest();
    }
    
    public void doTest() {

        //String targetEndpointAddress =
	//		"http://localhost:8080/findintr/FindInterest";
        String targetEndpointAddress =
			"http://localhost:12013/InterestIFPort";

    	try {
            Context ic = new InitialContext();
            FindInterest findIntrService = (FindInterest)
                    ic.lookup("java:comp/env/service/FindInterest");

            InterestIF interestIFPort = findIntrService.getInterestIFPort();

            ((Stub)interestIFPort)._setProperty (Stub.ENDPOINT_ADDRESS_PROPERTY,
                 				targetEndpointAddress);

	    interestIFPort.calculateInterest(balance, period);
                
	    status.addStatus(testId + "1 : EJB Endpoint and Servlet Endpoint Test", status.PASS);

    	} catch (Exception ex) {
		status.addStatus(testId + "1 : EJB Endpoint and Servlet Endpoint Test", status.FAIL);
            System.out.println("findintr client failed");
            ex.printStackTrace();
	} 
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

