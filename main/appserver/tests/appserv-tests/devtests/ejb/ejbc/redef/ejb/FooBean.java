package com.sun.s1asdev.ejb.ejbc.redef;

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

    public String sayHello() {
        System.out.println("in sayHello()");
        return "foo";
    }

    public void callHello()  {
        try {
            System.out.println("in FooBean::callHello()");
            FooLocalHome fooLocalHome = (FooLocalHome) sc.getEJBLocalHome();
            FooLocal fooLocal = fooLocalHome.create();
            fooLocal.sayHello();
            FooLocalHome fooLocalHome2 = (FooLocalHome) fooLocal.getEJBLocalHome();
            FooLocal fooLocal2 = fooLocalHome2.create();
            if( !fooLocal.isIdentical(fooLocal2) ) {
                throw new EJBException("equality test failed");
            }
            if( !fooLocal2.isIdentical(fooLocal) ) {
                throw new EJBException("equality test failed");
            }
            fooLocal.remove();
            fooLocal2.remove();
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
