/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.webservice.ksink.googleserverweb;

import java.util.Date;
import java.util.Iterator;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.naming.*;
import javax.xml.rpc.Service;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.namespace.QName;

public class GoogleServletImpl implements javax.servlet.SingleThreadModel, ServiceLifecycle {

    private static final QName PORT_QNAME = 
        new QName("urn:GoogleSearch", "GoogleSearchPort");
    private static final QName OPERATION_QNAME =
        new QName("urn:GoogleSearch", "doSpellingSuggestion");
    
    private boolean gotInit = false;

    public GoogleServletImpl() {
        System.out.println("GoogleServletImpl() instantiated");
    }

    public void init(Object context) {
        System.out.println("Got ServiceLifecycle::init call " + context);
        gotInit = true;
    }

    public void destroy() {
        System.out.println("Got ServiceLifecycle::destroy call");
    }

    public byte[] doGetCachedPage(java.lang.String key, java.lang.String url) 
    { 
        return null; 
    }

    public void helloOneWay(String s1) {

        System.out.println("In GoogleServletImpl::helloOneWay. param = " +
                           s1);
        System.out.println("Sleeping for " + s1 + " milliseconds starting "
                           + " at " + new Date());

        try {
            Thread.sleep((new Integer(s1)).intValue());
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("Exiting GoogleServletImpl:helloOneWay at " +
                           new Date());
    }

    public String doSpellingSuggestion(java.lang.String key, 
                                       java.lang.String phrase) 

        throws RemoteException {
        System.out.println("GoogleServletImpl.doSpellingSuggestion() " +
                           " called with " + phrase);

        if( !gotInit ) {
            throw new RuntimeException("Got business method before init()");
        }

        String returnValue = "spelling suggestion from web";
        if( phrase.equals("forwardejb") ) {
            System.out.println("Forwarding spelling suggestion to ejbendpoint");
            Service genericServiceWithWSDL = null;
            try {
                InitialContext ic = new InitialContext();
                Service service = (Service) 
                    ic.lookup("java:comp/env/service/EjbDIIReference");
                doDynamicProxyTest(service);
                GoogleSearchPort ejbPort = (GoogleSearchPort) 
                    service.getPort(GoogleSearchPort.class);
                returnValue = ejbPort.doSpellingSuggestion(key, phrase);
            } catch(Exception e) {
                e.printStackTrace();
                throw new RemoteException(e.getMessage(), e);
            }
        }               
        
        System.out.println("GoogleServletImpl returning " + returnValue);
        return returnValue;
    }
        
    public GoogleSearchResult doGoogleSearch(java.lang.String key, java.lang.String q, int start, int maxResults, boolean filter, java.lang.String restrict, boolean safeSearch, java.lang.String lr, java.lang.String ie, java.lang.String oe) {
        return null;
    }

    private void doDynamicProxyTest(Service service) throws Exception {

        Call c1 = service.createCall();
        Call c2 = service.createCall(PORT_QNAME);
        Call c3 = service.createCall(PORT_QNAME, OPERATION_QNAME);
        Call c4 = service.createCall(PORT_QNAME, 
                                     OPERATION_QNAME.getLocalPart());
        Call[] calls = service.getCalls(PORT_QNAME);

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
        
    }
    
}
