package com.sun.s1asdev.ejb.ejbc.sameimpl;

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
        String returnValue = null;
        try {
            Context ic = new InitialContext();
            returnValue = (String) ic.lookup("java:comp/env/foo");
            // if the lookup succeeded, we're in hello2.  make sure we can call
            // cmt-related ejb context methods.
            System.out.println("getRollbackOnly = " + sc.getRollbackOnly());
        } catch(NamingException ne) {
            // we must be in hellobean(vs. hellobean2), so ignore this.
        }
        return returnValue;
    }

    public void callHello()  {
        System.out.println("in FooBean::callHello()");

        try {
            Context ic = new InitialContext();

            //
                
            System.out.println("Looking up ejb ref hello ");
            // create EJB using factory from container 
            Object objref = ic.lookup("java:comp/env/ejb/hello");
            System.out.println("objref = " + objref);
            System.err.println("Looked up home!!");
                
            HelloHome  home = (HelloHome)PortableRemoteObject.narrow
                (objref, HelloHome.class);
                                                                     
            System.err.println("Narrowed home!!");
                
            Hello hr = home.create();
            System.err.println("Got the EJB!!");
                
            System.out.println("invoking hello ejb");

            hr.sayHello();

            System.out.println("successfully invoked ejb");

            //

            System.out.println("Looking up ejb local ref hellolocal");

            HelloLocalHome  localHome = (HelloLocalHome)
                ic.lookup("java:comp/env/ejb/hellolocal");
            System.err.println("Looked up home!!");
                
            HelloLocal hl = localHome.create();
            System.err.println("Got the EJB!!");
                
            System.out.println("invoking hello ejb");

            hl.sayHello();

            System.out.println("successfully invoked local ejb");


            //

            System.out.println("Looking up ejb ref hello ");
            // create EJB using factory from container 
            objref = ic.lookup("java:comp/env/ejb/hello2");
            System.out.println("objref = " + objref);
            System.err.println("Looked up home!!");
                
            HelloHome  home2 = (HelloHome)PortableRemoteObject.narrow
                (objref, HelloHome.class);
                                                                     
            System.err.println("Narrowed home!!");
                
            Hello hr2 = home2.create();
            System.err.println("Got the EJB!!");
                
            System.out.println("invoking hello ejb");

            String said = hr2.sayHello();

            System.out.println("successfully invoked ejb");

            System.out.println("Looking up ejb local ref hellolocal2");

            HelloLocalHome  localHome2 = (HelloLocalHome)
                ic.lookup("java:comp/env/ejb/hellolocal2");
            System.err.println("Looked up home!!");
                
            HelloLocal hl2 = localHome2.create();
            System.err.println("Got the EJB!!");
                
            System.out.println("invoking hello2 ejb");

            String saidLocal = hl2.sayHello();

            System.out.println("successfully invoked local 2 ejb");

            if( (said != null) && said.equals(saidLocal) ) {
                System.out.println("successful return values from hello2");
            } else {
                throw new IllegalStateException("got wrong values " + said + ":" +
                                                saidLocal);
            }
        } catch(Exception e) {
            e.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(e);
            throw ise;
        }

    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
}
