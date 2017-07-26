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

import java.net.URL;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.Service;
import javax.xml.rpc.Call;
import javax.xml.namespace.QName;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import test.webservice.WebServiceTest;

/**
 *
 * @author dochez
 */

public class Client {
    
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");    

    public static void main(String[] args) {
        stat.addDescription("webservices-web-stubs-properties");
        Client client = new Client();
        client.doTest(args);
        stat.printSummary("webservices-web-stubs-properties");        
    }

    public void doTest(String[] args) {
        try {
            dynamic(args);
            stat.addStatus("web-stubs-properties Dynamic Proxy", stat.PASS);        
        } catch(Exception e) {
            System.out.println("Failure " + e.getMessage());
            e.printStackTrace();
            stat.addStatus("web-stubs-properties Dynamic Proxy", stat.FAIL);                    
        }
    }
 
    public void dynamic(String[] args) throws Exception {
        String endpoint = args[0];
        System.out.println("Invoking dynamic proxies with endpoint at " + endpoint);
        URL wsdlURL = new URL(endpoint+"?WSDL");
        ServiceFactory factory = ServiceFactory.newInstance();
        Service service = factory.createService(wsdlURL, 
            new QName("urn:WebServiceTest","WebServiceServletTest"));
        System.out.println("Obtained Service");
        WebServiceTest intf = (WebServiceTest) service.getPort(
            new QName("urn:WebServiceTest","WebServiceTestPort"),          
            WebServiceTest.class);
        String[] params = new String[1];
        params[0] = " from client";       
        System.out.println(intf.doTest(params));
    }
    

}
