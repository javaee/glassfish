/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package soapfaults;

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
        stat.addDescription("webservices-soapfaults");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("webservices-soapfaultsID");
    }
    
    public void doTest(String[] args) {
    	try {

           String targetEndpointAddress = args[0];

	    Context ic = new InitialContext();
            
            TestService testService =
                (TestService) ic.lookup("java:comp/env/service/soapfaults");
            Test test = testService.getTestPort();
            
            ((Stub)test)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                                            targetEndpointAddress);

            System.out.println("endpoint address = " + targetEndpointAddress);

            Test2RequestType c = new Test2RequestType("recess", "weekender");

            try {
              Test1ResponseType ret = test.test1("1", "test fault one", c);
            } catch (FaultOne ex) {
              System.out.println("CAUGHT EXPECTED EXCEPTION: FaultOne: " + ex.getMessage1());
            }

            try {
              Test1ResponseType ret = test.test1("2", "test fault two", c);
            } catch (FaultTwo ex) {
              System.out.println("CAUGHT EXPECTED EXCEPTION: FaultTwo: " + ex.getMessage2());
            }

            try {
              Test1ResponseType ret = test.test1("3", "test fault three", c);
            } catch (FaultThree ex) {
              System.out.println("CAUGHT EXPECTED EXCEPTION: FaultThree: " + ex.getMessage3());
            }

            stat.addStatus("soapfaultsclient main", stat.PASS);
    	} catch (Exception ex) {
            System.out.println("soapfaults client test failed");
            ex.printStackTrace();
            stat.addStatus("soapfaultsclient main", stat.FAIL);
            //System.exit(15);
	} 
    }
}
