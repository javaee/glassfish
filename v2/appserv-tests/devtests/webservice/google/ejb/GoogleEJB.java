/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package googleejb;

import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;
import javax.xml.rpc.handler.MessageContext;
import java.security.Principal;

public class GoogleEJB implements SessionBean {
    private SessionContext sc;

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
            Principal p = sc.getCallerPrincipal();
            if( p != null ) {
                System.out.println("getCallerPrincipal() was successful");
            } else {
                throw new EJBException("getCallerPrincipal() returned null");
            }
        } catch(Exception e) {
            EJBException ejbEx = new EJBException("getCallerPrincipal exception");
            ejbEx.initCause(e);
            throw ejbEx;          
        }
        
        MessageContext msgContext = sc.getMessageContext();
        System.out.println("msgContext = " + msgContext);

        System.out.println("GoogleEJB.doSpellingSuggestion() called with " +
                           phrase);

        String returnValue = phrase + "spelling suggestion";

        System.out.println("GoogleEJB returning " + returnValue);

        return returnValue;
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

}
