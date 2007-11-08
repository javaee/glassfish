/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.webservice.ksink.googleserver;

import java.util.Iterator;
import java.util.Date;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.Service;
import javax.xml.rpc.Call;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;

public class GoogleEJB implements SessionBean {
    private SessionContext sc;

    private static final QName PORT_QNAME = 
        new QName("urn:GoogleSearch", "GoogleSearchPort");
    private static final QName OPERATION_QNAME =
        new QName("urn:GoogleSearch", "doSpellingSuggestion");
    
    public GoogleEJB(){}
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In GoogleEJB::ejbCreate !!");
    }

    public byte[] doGetCachedPage(java.lang.String key, java.lang.String url) 
    { 
        return null; 
    }

    public String doSpellingSuggestion(java.lang.String key, 
                                       java.lang.String phrase) 
       {

        try {
            InitialContext ic = new InitialContext();
            String entry1 = (String) ic.lookup("java:comp/env/entry1");
            System.out.println("java:comp/env/entry1 = " + entry1);
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e.getMessage(), e);
        }

        MessageContext msgContext = sc.getMessageContext();

        System.out.println("GoogleEJB.doSpellingSuggestion() called with " +
                           phrase);

        try {
            java.security.Principal principal = sc.getCallerPrincipal();
            System.out.println("GoogleEJB.doSpellingSuggestion():getCallerPrincipal() = " + principal);
            if(principal == null) {
                throw new EJBException("GoogleEJB.doSpellingSuggestion():getCallerPrincipal() cannot return NULL");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EJBException("GoogleEJB.doSpellingSuggestion():Unexpected exception caught - ", ex);        
        }
            
        /*
        try {
            
            boolean result = sc.isCallerInRole("foo");
            System.out.println(
                "GoogleEJB.doSpellingSuggestion():isCallerInRole(foo) returned - " + result);
            if(!result) {
                throw new EJBException("GoogleEJB.doSpellingSuggestion():isCallerInRole(foo) should not have returned false");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EJBException("GoogleEJB.doSpellingSuggestion():Unexpected exception caught in isCallerInRole(foo) call ", ex);            
        }
        */

        String returnValue = "spelling suggestion from ejb";
        if( phrase.equals("forwardweb") ) {
            System.out.println("Forwarding spelling suggestion to webendpoint");
            Service genericServiceWithWSDL = null;
            try {
                InitialContext ic = new InitialContext();
                Service service = (Service) 
                    ic.lookup("java:comp/env/service/WebDIIReference");
                doDynamicProxyTest(service);
                GoogleSearchPort webPort = (GoogleSearchPort) 
                    service.getPort(GoogleSearchPort.class);
                returnValue = webPort.doSpellingSuggestion(key, phrase);
            } catch(Exception e) {
                e.printStackTrace();
                throw new EJBException(e.getMessage(), e);
            }
        }

        System.out.println("GoogleEJB returning " + returnValue);
        return returnValue;
    }
        
    public void helloOneWay(String s1) {

        javax.transaction.TransactionManager tm = 
            com.sun.enterprise.Switch.getSwitch().getTransactionManager();
        int txStatus = -1;
        try {
            txStatus = tm.getStatus();
        } catch(Exception e) {
            throw new EJBException(e);
        }

        // The following tests make sure tx attributes are processed 
        // correctly when the same method is defined on both the remote
        // and webservice interface.   Descriptor is set up to have
        // no tx for the webservice case and a TX_REQUIRED for the
        // Remote case.
        try {
            
            sc.getMessageContext();
          
            // we were invoked through a webserivce invocation.  there
            // shouldn't be a transaction.
            if( txStatus == javax.transaction.Status.STATUS_NO_TRANSACTION) {
                System.out.println("Correctly found no tx for helloOneWay " +
                                   " invoked through a webservice");
            } else {
                throw new EJBException("Got wrong tx status = " + txStatus + 
                                       " for helloOneWay invoked through a " +
                                       "web service");
            }
        } catch(Exception e) {
            // since there's no WEB service message context, we were
            // invoked through a remote invocation.  
                    
            if( txStatus == javax.transaction.Status.STATUS_ACTIVE) {
                System.out.println("Correctly found an active tx for " +
                   "helloOneWay invoked through a Remote interface");
            } else {
                throw new EJBException("Got wrong tx status = " + txStatus + 
                                       " for helloOneWay invoked through a " +
                                       "remote interface");
            }
        }

        System.out.println("In GoogleEJB::helloOneWay. param = " +
                           s1);
        System.out.println("Sleeping for " + s1 + " milliseconds starting "
                           + " at " + new Date());

        try {
            Thread.sleep((new Integer(s1)).intValue());
        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("Exiting GoogleEJB:helloOneWay at " +
                           new Date());
    }

    public GoogleSearchResult doGoogleSearch(java.lang.String key, java.lang.String q, int start, int maxResults, boolean filter, java.lang.String restrict, boolean safeSearch, java.lang.String lr, java.lang.String ie, java.lang.String oe) {
        return null;
    }
    
    public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}

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
