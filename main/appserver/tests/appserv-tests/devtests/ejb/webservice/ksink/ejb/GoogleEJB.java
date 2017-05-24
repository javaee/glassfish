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
