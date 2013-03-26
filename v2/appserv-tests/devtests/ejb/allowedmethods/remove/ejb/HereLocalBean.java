package com.sun.s1asdev.ejb.allowedmethods.remove;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;


public class HereLocalBean implements SessionBean {

    private SessionContext sc;

    public HereLocalBean() {}

    public void ejbCreate() throws RemoteException {
	System.out.println("In HereLocalBean::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
    }

    public void test() {
	System.out.println("In HereLocalBean::test !!");
    }

    public void ejbRemove()
        throws RemoteException
    {
	System.out.println("In HereLocalBean::ejbRemove !!");
    }

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
