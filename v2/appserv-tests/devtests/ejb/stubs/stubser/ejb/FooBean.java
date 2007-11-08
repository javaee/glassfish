package com.sun.s1asdev.ejb.stubs.stubser;

import java.util.Date;
import java.util.Collection;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.ejb.TimerService;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

public class FooBean implements SessionBean {

    private SessionContext sc;
    private HelloHome helloHome;
    private Hello     hello;

    public FooBean() {}

    public void ejbCreate() throws RemoteException {
	System.out.println("In FooBean::ejbCreate !!");

        try {
            Context ic = new InitialContext();
                
            System.out.println("Looking up ejb ref ");
            // create EJB using factory from container 
            Object objref = ic.lookup("java:comp/env/ejb/hello");
            System.out.println("objref = " + objref);
            System.err.println("Looked up home!!");
                
            helloHome = (HelloHome)PortableRemoteObject.narrow
                (objref, HelloHome.class);
                                                                     
            System.err.println("Narrowed home!!");

            hello = helloHome.create();
            System.err.println("Got the EJB!!");

        } catch(Exception e) {
            e.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(e);
            throw ise;
        }
    }

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
    }

    public void callHello()  {
        System.out.println("in FooBean::callHello()");

        try {
                
            hello.sayHello();

            System.out.println("successfully invoked ejb");


        } catch(Exception e) {
            e.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(e);
            throw ise;
        }

        
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {
        System.out.println("In FooBean::ejbActivate");
    }

    public void ejbPassivate() {
        System.out.println("In FooBean::ejbPassivate");
    }
}
