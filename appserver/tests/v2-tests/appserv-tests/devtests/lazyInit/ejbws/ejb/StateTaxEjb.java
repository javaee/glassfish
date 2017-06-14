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

public class StateTaxEjb implements SessionBean {

    private SessionContext sc;
    
    public StateTaxEjb(){}
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In StateTaxEjb::ejbCreate !!");
    }

    public double getStateTax(double income, double deductions) {
        return ((income -  deductions) * 0.3);
    }

    public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}
}
