package com.sun.s1asdev.ejb.classload.lifecycle;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import javax.xml.rpc.Service;

public class FooBean implements SessionBean {

    private SessionContext sc;

    public FooBean() {}

    public void ejbCreate() {
	System.out.println("In FooBean::ejbCreate()");
        try {
            InitialContext ic = new InitialContext();
            Service service = (Service) ic.lookup("java:comp/env/service/GoogleSearch_ejbCreate");
            System.out.println("Successfully looked up service");
        } catch(Exception e) {
            e.printStackTrace();
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(e);
            throw ejbEx;
        }
    }

    public void setSessionContext(SessionContext sc) {
	System.out.println("In FooBean::setSessionContext()");
	this.sc = sc;
    }

    public void callHello() {
        System.out.println("In FooBean::callHello()");
    }

    public void ejbRemove() {
	System.out.println("In FooBean::ejbRemove()");
        try {
            InitialContext ic = new InitialContext();
            Service service = (Service) ic.lookup("java:comp/env/service/GoogleSearch_ejbRemove");
            System.out.println("Successfully looked up service");
        } catch(Exception e) {
            e.printStackTrace();
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(e);
            throw ejbEx;
        }

    }

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
