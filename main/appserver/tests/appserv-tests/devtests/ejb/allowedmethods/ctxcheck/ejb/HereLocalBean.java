package com.sun.s1asdev.ejb.allowedmethods.ctxcheck;

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

    public void doSomethingHere() {
        System.out.println("in doSomethingHere()");
    }

    public void accessEJBObject() {
        sc.getEJBObject();      //must throw exception
    }

    public void accessEJBLocalObject() {
        sc.getEJBLocalObject();      
    }

    public void accessEJBHome() {
        sc.getEJBHome();      //must throw exception
    }

    public void accessEJBLocalHome() {
        sc.getEJBLocalHome();      
    }

    public void ejbRemove()
        throws RemoteException
    {
    }

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
