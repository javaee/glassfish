/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package xsdany;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.xml.rpc.Stub;
import javax.xml.soap.SOAPElement;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    
    public static void main (String[] args) {
        stat.addDescription("webservices-xsdany");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("webservices-xsdanyID");
    }
    
    public void doTest(String[] args) {
    	try {

           String targetEndpointAddress = args[0];

	    Context ic = new InitialContext();
            
            TestService testService =
                (TestService) ic.lookup("java:comp/env/service/xsdany");
            Test test = testService.getTestPort();
            
            ((Stub)test)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                                            targetEndpointAddress);

            System.out.println("endpoint address = " + targetEndpointAddress);

            //SingleWildcardType c = new SingleWildcardType(8, "Single", new SOAPElement());
            SingleWildcardType c = new SingleWildcardType(8, "Single", "BUGGGY how to do single soapelement");

            String[] dd = new String[2];
            dd[0] = "wild";
            dd[1] = "card";
            RepeatedWildcardType d = new RepeatedWildcardType(88, "Repeated", new SOAPElement[0]);

            int ret = test.test1(888, c, d);

            System.out.println("RETURN LUCKY NUMBER = " + ret);
            stat.addStatus("xsdanyclient main", stat.PASS);
                
    	} catch (Exception ex) {
            System.out.println("xsdany client test failed");
            ex.printStackTrace();
            stat.addStatus("xsdanyclient main", stat.FAIL);
	} 
    }
}
