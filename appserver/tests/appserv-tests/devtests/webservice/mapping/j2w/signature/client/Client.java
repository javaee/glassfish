/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
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
