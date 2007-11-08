package com.sun.s1asdev.ejb.stubs.stubcount;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

public class FooBean implements SessionBean {

    private SessionContext sc;

    public FooBean() {}

    public void ejbCreate() throws RemoteException {
	System.out.println("In FooBean::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
    }

    public void sayFoo()  {       
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
