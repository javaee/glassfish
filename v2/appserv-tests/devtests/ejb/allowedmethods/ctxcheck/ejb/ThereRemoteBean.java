package com.sun.s1asdev.ejb.allowedmethods.ctxcheck;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;


public class ThereRemoteBean implements SessionBean {

    private SessionContext sc;

    public ThereRemoteBean() {}

    public void ejbCreate() throws RemoteException {
	System.out.println("In ThereRemoteBean::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
    }

    public void doSomethingHere() {
        System.out.println("in doSomethingHere()");
    }

    public void accessEJBObject() {
        sc.getEJBObject();      
    }

    public void accessEJBLocalObject() {
        sc.getEJBLocalObject();      //must throw exception
    }

    public void accessEJBHome() {
        sc.getEJBHome();      
    }

    public void accessEJBLocalHome() {
        sc.getEJBLocalHome();      //must throw exception
    }

    public void ejbRemove()
        throws RemoteException
    {
    }

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
