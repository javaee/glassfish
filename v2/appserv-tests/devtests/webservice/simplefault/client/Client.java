/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package soapfault;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import javax.xml.rpc.Service;
import javax.xml.soap.SOAPElement;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import soapfault.ejb.*;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    
    public static void main (String[] args) {
        stat.addDescription("webservices-simple-soapfault");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("webservices-soapfaultID");
    }
    
    public void doTest(String[] args) {
    	try {

           String targetEndpointAddress = args[0];

	    Context ic = new InitialContext();
            
            Service testService = (Service) ic.lookup("java:comp/env/service/soapfault");
            SimpleSoapFaultSEI test = (SimpleSoapFaultSEI) 
                testService.getPort(new QName("urn:SoapFaultTest", "SimpleSoapFaultSEIPort"),
                    SimpleSoapFaultSEI.class);
            
            ((Stub)test)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                                            targetEndpointAddress);

            System.out.println("endpoint address = " + targetEndpointAddress);

            try {
              String ret = test.simpleMethod();
            } catch (SimpleSoapException ex) {
              System.out.println("CAUGHT EXPECTED EXCEPTION: FaultOne: " + ex.getReason());
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
