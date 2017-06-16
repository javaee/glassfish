/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package myejb;

import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;

public class InterestEJB implements SessionBean {

    private SessionContext sc;
    
    public InterestEJB(){}
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In InterestEJB::ejbCreate !!");
    }

    public void calculateInterest(double balance, double period) {
        System.out.println(" Inside calculateInterest interest is : " +  (balance * period * 0.1));
    }

    public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}
}
