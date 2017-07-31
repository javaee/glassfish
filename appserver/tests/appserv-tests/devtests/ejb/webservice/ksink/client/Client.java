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

package com.sun.s1asdev.ejb.webservice.ksink.googleclient;

import java.io.*;
import java.util.*;
import java.net.URL;
import javax.naming.*;
import javax.xml.namespace.QName;
import javax.xml.rpc.Stub;
import javax.xml.rpc.Call;
import javax.xml.rpc.Service;
import java.rmi.Remote;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.lang.reflect.Method;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static final QName PORT_QNAME = 
        new QName("urn:GoogleSearch", "GoogleSearchPort");
    private static final QName OPERATION_QNAME =
        new QName("urn:GoogleSearch", "doSpellingSuggestion");

    private String word;
    private String targetEndpointAddress;
    private String testName;

    public static void main (String[] args) {

        stat.addDescription("googleserver appclient");	
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("googleserver-applientID");
    }
    
    public Client(String[] args) {
        word = (args.length > 0) ? args[0] : "spellng";
        targetEndpointAddress = (args.length > 1) ? args[1] : null;
        testName = (args.length > 1) ? args[1] : "ksink_standalone";
    }

    public void doTest() {

        
    	try {
            Context ic = new InitialContext();
            String googleKey = (String)
                ic.lookup("java:comp/env/googlekey");
            GoogleSearchService googleSearchService = (GoogleSearchService)
                    ic.lookup("java:comp/env/service/GoogleSearch");

            Class fooHomeClazz = Class.forName("com.sun.s1asdev.ejb.webservice.ksink.googleserver.FooHome");
            Object obj = ic.lookup("java:comp/env/ejb/googleserverejb");
            Object fooHome = javax.rmi.PortableRemoteObject.narrow
                (obj, fooHomeClazz);
            Method createMethod = fooHomeClazz.getMethod("create",
                                                         new Class[0]);
            Object foo = createMethod.invoke(fooHome, new Object[0]);
            Method helloOneWay = foo.getClass().getMethod("helloOneWay",
                                                          new Class[] { java.lang.String.class });
            helloOneWay.invoke(foo, new Object[] { "1000" } );
                                                                   
            try {
                googleSearchService.getHandlerRegistry();
            } catch(java.lang.UnsupportedOperationException uoe) {
                System.out.println
                    ("Successfully caught unsupported operation exception " +
                     "for Service.getHandlerRegistry()");
            }

            try {
                googleSearchService.getTypeMappingRegistry();
            } catch(java.lang.UnsupportedOperationException uoe) {
                System.out.println
                    ("Successfully caught unsupported operation exception " +
                     "for Service.getTypeMappingRegistry()");
            }

            doDynamicProxyTest(googleSearchService, googleKey);

            GoogleSearchPort googleSearchPort = 
                googleSearchService.getGoogleSearchPort();
            doSpellingSuggestion(googleSearchPort, googleKey,
                                 targetEndpointAddress);

            System.out.println("Using dynamic proxy to invoke endpoint");
            Service genericServiceWithWsdl = (Service)
                ic.lookup("java:comp/env/service/GoogleSearchProxy");

            doDynamicProxyTest(genericServiceWithWsdl, googleKey);

            googleSearchPort = (GoogleSearchPort)
                genericServiceWithWsdl.getPort(GoogleSearchPort.class);
            doSpellingSuggestion(googleSearchPort, googleKey,
                                 targetEndpointAddress);

            doDIINoWSDLTest(ic, googleKey, targetEndpointAddress);

            System.out.println("Doing wsdl override tests");

            Service partialWsdlServiceGeneric = (Service)
                ic.lookup("java:comp/env/service/partialGeneric");
            GoogleSearchPort googleSearch = (GoogleSearchPort)
                partialWsdlServiceGeneric.getPort(GoogleSearchPort.class);
            googleSearch.doSpellingSuggestion(googleKey, word);

/**            GoogleSearchService partialWsdlServiceGenerated = 
                (GoogleSearchService)
                ic.lookup("java:comp/env/service/partialGenerated");
            googleSearch = partialWsdlServiceGenerated.getGoogleSearchPort();
            googleSearch.doSpellingSuggestion(googleKey, word);
**/
            stat.addStatus("appclient " + testName, stat.PASS);


    	} catch (Exception ex) {
            System.out.println("google client test failed");
            ex.printStackTrace();
            stat.addStatus("appclient " + testName, stat.FAIL);
	} 

    }

    private void doSpellingSuggestion(GoogleSearchPort googleSearchPort,
                                      String googleKey,
                                      String endpointAddress) 
        throws Exception {

        if( endpointAddress != null ) {
            ((Stub)googleSearchPort)._setProperty
                (Stub.ENDPOINT_ADDRESS_PROPERTY, targetEndpointAddress);
            System.out.println("Setting target endpoint address to " +
                               endpointAddress);
        } else {
            // if not set, use default
            endpointAddress = (String)
                ((Stub)googleSearchPort)._getProperty
                (Stub.ENDPOINT_ADDRESS_PROPERTY);
        }

        System.out.println("Contacting google for spelling " +
                           "suggestion at " + endpointAddress);
        
        String spellingSuggestion =
            googleSearchPort.doSpellingSuggestion(googleKey, word);
        System.out.println("Gave google the word '" + word + "' ... " +
                           " and the suggested spelling is '" +
                           spellingSuggestion + "'");
    }

    private void doDynamicProxyTest(Service service, String googleKey) throws Exception {

        Call c1 = service.createCall();
        Call c2 = service.createCall(PORT_QNAME);
        Call c3 = service.createCall(PORT_QNAME, OPERATION_QNAME);
        Call c4 = service.createCall(PORT_QNAME, 
                                     OPERATION_QNAME.getLocalPart());
        Call[] calls = service.getCalls(PORT_QNAME);

        if( targetEndpointAddress != null ) {
            c3.setTargetEndpointAddress(targetEndpointAddress);            
        } 
        Object params[] = new Object[] {googleKey, "hello" };
        String response = (String) c3.invoke(params);
        System.out.println("Response = " + response);

        // container-managed port selection
        GoogleSearchPort sei = (GoogleSearchPort) 
            service.getPort(GoogleSearchPort.class);
        sei = (GoogleSearchPort) 
            service.getPort(PORT_QNAME, GoogleSearchPort.class);

        QName serviceName = service.getServiceName();
        URL wsdlLocation = service.getWSDLDocumentLocation();
        if( wsdlLocation != null ) {
            System.out.println("wsdlLocation = " + wsdlLocation);
        }
        Iterator ports = service.getPorts();

        System.out.println("Calling oneway operation");
        Call oneway = service.createCall(PORT_QNAME, "helloOneWay");
        if( targetEndpointAddress != null ) {
            oneway.setTargetEndpointAddress(targetEndpointAddress);            
        } 

        long oneWayMethodWaitTimeInMillis = 4000;

        Date before = new Date();        
        oneway.invokeOneWay(new Object[] 
               { oneWayMethodWaitTimeInMillis + "" });
        Date after = new Date();
        long elapsedTime = after.getTime() - before.getTime();
        System.out.println("one way operation began at " + before + 
                           " and returned at " + after + 
                           " and took " +  elapsedTime + " milli-seconds");
        if( elapsedTime > oneWayMethodWaitTimeInMillis ) {
            throw new Exception("one way operation blocked for too long ");
        }

        // now wait for the remainder of the time.  this is to
        // avoid race conditions where we finish the test and begin
        // to undeploy, but the endpoint is still executing its
        // oneway operation.
        long sleepTime = (oneWayMethodWaitTimeInMillis - elapsedTime);
                          
        System.out.println("now sleeping for " + sleepTime + " milli secs");
        Thread.sleep(sleepTime);
        System.out.println("returning from doDynamicProxyTest");
    }

    private void doDIINoWSDLTest(Context ic, String googleKey,
                                 String endpointAddress) throws Exception {

        System.out.println("Doing DII without WSDL tests");

        Service service =(Service) ic.lookup("java:comp/env/service/DIINoWSDL");

        try {
            Call call = service.createCall(PORT_QNAME);
        } catch(java.lang.UnsupportedOperationException uoe) {
            System.out.println("Successfully caught unsupported operation " +
                               "for Service.createCall(QName port)");
        }

        try {
            Call call = service.createCall(PORT_QNAME, OPERATION_QNAME);
        } catch(java.lang.UnsupportedOperationException uoe) {
            System.out.println("Successfully caught unsupported operation " +
                               "for Service.getCall(QName, QName)");
        }


        try {
            Call call = service.createCall(PORT_QNAME, "doSpellingSuggestion");
        } catch(java.lang.UnsupportedOperationException uoe) {
            System.out.println("Successfully caught unsupported operation " +
                               "for Service.getCall(QName, String)");
        }

        try {
            Call[] calls = service.getCalls(PORT_QNAME);
        } catch(java.lang.UnsupportedOperationException uoe) {
            System.out.println("Successfully caught unsupported operation " +
                               "for Service.getCalls()");
        }

        try {
            service.getHandlerRegistry();
        } catch(java.lang.UnsupportedOperationException uoe) {
            System.out.println
                ("Successfully caught unsupported operation exception " +
                 "for Service.getHandlerRegistry()");
        }

        try {
            Remote remote = service.getPort(GoogleSearchPort.class);
        } catch(java.lang.UnsupportedOperationException uoe) {
            System.out.println("Successfully caught unsupported operation " +
                               "for Service.getPort(SEI)");
        }
        
        try {
            Remote remote = service.getPort(PORT_QNAME, GoogleSearchPort.class);
        } catch(java.lang.UnsupportedOperationException uoe) {
            System.out.println("Successfully caught unsupported operation " +
                               "for Service.getPort(QName, SEI)");
        }

        try {
            Iterator ports = service.getPorts();
        } catch(java.lang.UnsupportedOperationException uoe) {
            System.out.println("Successfully caught unsupported operation " +
                               "for Service.getPorts()");
        }

        try {
            QName serviceName = service.getServiceName();
        } catch(java.lang.UnsupportedOperationException uoe) {
            System.out.println("Successfully caught unsupported operation " +
                               "for Service.getName()");
        }

        try {
            service.getTypeMappingRegistry();
        } catch(java.lang.UnsupportedOperationException uoe) {
            System.out.println
                ("Successfully caught unsupported operation exception " +
                 "for Service.getTypeMappingRegistry()");
        }

        Call untypedCall = service.createCall();

    }
}

