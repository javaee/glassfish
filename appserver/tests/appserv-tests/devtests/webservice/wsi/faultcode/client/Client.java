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

package faultcode;

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
        stat.addDescription("webservices-faultcode");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("webservices-faultcodeID");
    }
    
    public void doTest(String[] args) {
    	try {

           String targetEndpointAddress = args[0];

	    Context ic = new InitialContext();
            
            SimpleTestService testService =
                (SimpleTestService) ic.lookup("java:comp/env/service/faultcode");
            SimpleTest test = testService.getSimpleTest();
            
            ((Stub)test)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                                            targetEndpointAddress);

            System.out.println("endpoint address = " + targetEndpointAddress);

            //SingleWildcardType c = new SingleWildcardType(8, "Single", new SOAPElement());
/*
            SingleWildcardType c = new SingleWildcardType(8, "Single", "BUGGGY how to do single soapelement");

            String[] dd = new String[2];
            dd[0] = "wild";
            dd[1] = "card";
            RepeatedWildcardType d = new RepeatedWildcardType(88, "Repeated", new SOAPElement[0]);

            int ret = test.test1(888, c, d);

            System.out.println("RETURN LUCKY NUMBER = " + ret);
*/
            stat.addStatus("xsdanyclient main", stat.PASS);
                
    	} catch (Exception ex) {
            System.out.println("xsdany client test failed");
            ex.printStackTrace();
            stat.addStatus("xsdanyclient main", stat.FAIL);
	} 
    }
}
