package com.sun.s1asdev.ejb.sfsb.stress.ejb;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import javax.transaction.UserTransaction;

import java.rmi.RemoteException;

public class StressSFSBBean
    implements SessionBean 
{
    private SessionContext              sessionCtx;
    private Context                     initialCtx;
    private String                      name;

    public void ejbCreate(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean hasSameName(String name) {
        return  this.name.equals(name);
    }

    public void ping() {
    }

    public boolean doWork(long millis) {
	return true;
    }

    public void setSessionContext(SessionContext sc) {
        this.sessionCtx = sc;
        try {
            this.initialCtx = new InitialContext();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void ejbRemove() {}

    public void ejbActivate() {
        //System.out.println ("In SFSB.ejbActivate() for name -> " + sfsbName);
    }

    public void ejbPassivate() {
        //System.out.println ("####In SFSB.ejbPassivate() for: " + sfsbName);
    }

    /*
    private boolean lookupEntityHome() {
        boolean status = false;
        try {
            Object homeRef = initialCtx.lookup("java:comp/env/ejb/SimpleEntityHome");
            this.entityHome = (SimpleEntityHome)
                PortableRemoteObject.narrow(homeRef, SimpleEntityHome.class);

            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return status;
    }

    private boolean lookupEntityLocalHome() {
        boolean status = false;
        try {
            Object homeRef = envSubCtx.lookup("SimpleEntityLocalHome");
            this.entityLocalHome = (SimpleEntityLocalHome) homeRef;

            status = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return status;
    }
    */
}
