/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package faultcodeejb;

import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;
import javax.xml.rpc.handler.MessageContext;

public class FaultCodeEJB implements SessionBean {
    private SessionContext sc;

    public FaultCodeEJB(){}
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In FaultCodeEJB::ejbCreate !!");
    }

    public String echoString(String body) {
	System.out.println("In FaultCodeEJB::  echoString = " + body);
        return body;
    }
        
    public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}

}
