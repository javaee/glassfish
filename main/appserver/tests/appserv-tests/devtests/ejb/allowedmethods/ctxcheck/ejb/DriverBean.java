package com.sun.s1asdev.ejb.allowedmethods.ctxcheck;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;


public class DriverBean
    implements SessionBean
{

    private SessionContext sc;

    public DriverBean() {
    }

    public void ejbCreate() throws RemoteException {
        System.out.println("In DriverBean::ejbCreate !!");
    }

    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
    }

    public void localSlsbGetEJBObject() {
        try {
            Context ic = new InitialContext();
            HereLocalHome localHome = (HereLocalHome) ic.lookup("java:comp/env/ejb/HereLocal");

            HereLocal local = (HereLocal) localHome.create();
            local.doSomethingHere();
            local.accessEJBObject();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }
        
    public void localSlsbGetEJBLocalObject() {
        try {
            Context ic = new InitialContext();
            HereLocalHome localHome = (HereLocalHome) ic.lookup("java:comp/env/ejb/HereLocal");

            HereLocal local = (HereLocal) localHome.create();

            local.doSomethingHere();
            local.accessEJBLocalObject();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localSlsbGetEJBLocalHome() {
        try {
            Context ic = new InitialContext();
            HereLocalHome localHome = (HereLocalHome) ic.lookup("java:comp/env/ejb/HereLocal");

            HereLocal local = (HereLocal) localHome.create();

            local.doSomethingHere();
            local.accessEJBLocalHome();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localSlsbGetEJBHome() {
        try {
            Context ic = new InitialContext();
            HereLocalHome localHome = (HereLocalHome) ic.lookup("java:comp/env/ejb/HereLocal");

            HereLocal local = (HereLocal) localHome.create();

            local.doSomethingHere();
            local.accessEJBHome();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localEntityGetEJBObject() {
        try {
            Context ic = new InitialContext();
            LocalEntityHome localHome = (LocalEntityHome) ic.lookup("java:comp/env/ejb/LocalEntity");

            localHome.create("5", "5");
            LocalEntity local = (LocalEntity) localHome.findByPrimaryKey("5");
            local.localEntityGetEJBObject();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }
        
    public void localEntityGetEJBLocalObject() {
        try {
            Context ic = new InitialContext();
            LocalEntityHome localHome = (LocalEntityHome) ic.lookup("java:comp/env/ejb/LocalEntity");

            localHome.create("6", "6");
            LocalEntity local = (LocalEntity) localHome.findByPrimaryKey("6");
            local.localEntityGetEJBLocalObject();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localEntityGetEJBLocalHome() {
        try {
            Context ic = new InitialContext();
            LocalEntityHome localHome = (LocalEntityHome) ic.lookup("java:comp/env/ejb/LocalEntity");

            localHome.create("7", "7");
            LocalEntity local = (LocalEntity) localHome.findByPrimaryKey("7");
            local.localEntityGetEJBLocalHome();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void localEntityGetEJBHome() {
        try {
            Context ic = new InitialContext();
            LocalEntityHome localHome = (LocalEntityHome) ic.lookup("java:comp/env/ejb/LocalEntity");

            localHome.create("8", "8");
            LocalEntity local = (LocalEntity) localHome.findByPrimaryKey("8");
            local.localEntityGetEJBHome();
        } catch (IllegalStateException illEx) {
            throw illEx;
        } catch (Exception ex) {
            ex.printStackTrace();
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
    
}
