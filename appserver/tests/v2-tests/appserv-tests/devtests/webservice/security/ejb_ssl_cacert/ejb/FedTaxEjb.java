/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1peqe.webservices.ejb.taxcal;

import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;

public class FedTaxEjb implements SessionBean {

    private SessionContext sc;
    
    public FedTaxEjb(){}
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In FedTaxEjb::ejbCreate !!");
    }

    public double getFedTax(double income, double deductions) {
        return ((income -  deductions) * 0.2);
    }

    public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}
}
