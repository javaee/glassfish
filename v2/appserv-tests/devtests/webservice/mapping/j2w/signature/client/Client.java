/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package signature;

import java.io.*;
import java.util.*;
import javax.naming.*;
import javax.xml.rpc.Stub;
import javax.xml.soap.SOAPElement;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.GregorianCalendar;

public class Client {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    
    public static void main (String[] args) {
        stat.addDescription("webservices-signature");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("webservices-signatureID");
    }
    
    public void doTest(String[] args) {
    	try {

           String targetEndpointAddress = args[0];

	    Context ic = new InitialContext();
            
            SignatureTestService testService =
                (SignatureTestService) ic.lookup("java:comp/env/service/signature");
            SignatureTest test = testService.getSignatureTestPort();
            
            ((Stub)test)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                                            targetEndpointAddress);

            System.out.println("endpoint address = " + targetEndpointAddress);
            stat.addStatus("signature client main", stat.PASS);

            try {
                String msg = test.sayHello("world");
                System.out.println("MSG: " + msg);
                stat.addStatus("signature SEI.sayHello(msg)", stat.PASS);
            } catch (Exception e1) {
		e1.printStackTrace();
                stat.addStatus("signature SEI.sayHello(msg)", stat.FAIL);
            }


/* uncomment the following when the date->calendar is done
            MySecondDateValueType d = 
                new MySecondDateValueType(new java.util.GregorianCalendar(), "Suck it up and do it!");
            MyDateValueType c = new MyDateValueType();
            c.setDate(new java.util.GregorianCalendar());
            c.setWhine("Stop Whining!!");
            c.setMySecondDateValueTypes(new MySecondDateValueType[0]);

            try {
                test.setTestDate(new java.util.GregorianCalendar());
                java.util.Calendar date1 = test.getTestDate();
                System.out.println("RETURNED LUCKY DATE = " + date1);
                stat.addStatus("signature SEI.setDate(java.util.Date)", stat.PASS);
            } catch (Exception e1) {
                stat.addStatus("signature SEI.setDate(java.util.Date)", stat.FAIL);
            }

            try {
                test.setMyDateValueType(c);
                MyDateValueType date2 = test.getMyDateValueType();
                System.out.println("RETURNED ANOTHER LUCKY DATE = " 
                    + date2.getDate() + "; with message = " + date2.getWhine());
                stat.addStatus("signature SEI.setValueTypeWithDate", stat.PASS);

                MySecondDateValueType[] seconddates = date2.getMySecondDateValueTypes();
                if (seconddates != null) {
                    stat.addStatus("signature SEI.setSecondValueTypeArray", stat.PASS);
                } else {
                    stat.addStatus("signature SEI.setSecondValueTypeArray", stat.FAIL);
                }

            } catch (Exception e2) {
                stat.addStatus("signature SEI.setValueTypeWithDate", stat.FAIL);
            }

            try {
                test.setMyDateValueTypes(new MyDateValueType[0]);
                MyDateValueType[] date3 = test.getMyDateValueTypes();
                System.out.println("RETURNED MULTIPLE LUCKY DATES.size = " + date3.length);
                stat.addStatus("signature SEI.setValueTypeWithDates", stat.PASS);
            } catch (Exception e2) {
                stat.addStatus("signature SEI.setValueTypeWithDates", stat.FAIL);
            }
*/
    	} catch (Exception ex) {
            System.out.println("signature client test failed");
            ex.printStackTrace();
            stat.addStatus("signature client main", stat.FAIL);
	} 
    }
}
