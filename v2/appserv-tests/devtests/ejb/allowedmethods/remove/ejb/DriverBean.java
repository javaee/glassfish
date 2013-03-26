package com.sun.s1asdev.ejb.allowedmethods.remove;

import java.util.Enumeration;
import java.io.Serializable;
import java.rmi.RemoteException; 
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBException;
import javax.ejb.RemoveException;
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

    public boolean test() {
        try {
            Context ic = new InitialContext();
            HereLocalHome localHome = (HereLocalHome) ic.lookup("java:comp/env/ejb/HereLocal");

            HereLocal local = (HereLocal) localHome.create();
            local.test();
            local.remove();
        } catch (RemoveException rte) {
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            RuntimeException rt = new RuntimeException();
            rt.initCause(ex);
            throw rt;
        }

        return false;
    }
        
    public void ejbRemove() throws RemoteException {}

    public void ejbActivate() {}

    public void ejbPassivate() {}
    
}
