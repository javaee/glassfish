package com.sun.s1asdev.ejb.stubs.ejbapp;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;


public class HelloBean implements SessionBean {

    private SessionContext sc;

    public HelloBean() {}

    public void ejbCreate() throws RemoteException {
	System.out.println("In HelloBean::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
    }

    public void sayHello() {
        System.out.println("hello from HelloBean::sayHello()");
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
