/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package statelesshello;

import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;

public class StatelesshelloEJB implements SessionBean {
    private SessionContext sc;
    
    public StatelesshelloEJB(){}
    
    public void ejbCreate() throws RemoteException {
	System.out.println("In ejbCreate !!");
    }

    public String sayStatelesshello() throws EJBException {
	System.out.println("StatelesshelloEJB is saying hello to user\n");
        return("A Big hello from stateless HELLO");
    }
    
    public void setSessionContext(SessionContext sc) {
	
        this.sc = sc;
    }
    
    public void ejbRemove() throws RemoteException {}
    
    public void ejbActivate() {}
    
    public void ejbPassivate() {}
}
