package com.sun.s1asdev.ejb.stress.sfsbcache.ejb;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import java.rmi.RemoteException;

public class SFSBEJB
    implements SessionBean 
{
	private SessionContext context;
    private String sfsbName;
    private Context initialCtx;

	public void ejbCreate(String sfsbName) {
        System.out.println ("In SFSB.ejbCreate() for name -> " + sfsbName);
        this.sfsbName = sfsbName;
    }

    public String getName() {
        return this.sfsbName;
    }

	public void setSessionContext(SessionContext sc) {
		this.context = sc;
        try {
            this.initialCtx = new InitialContext();
        } catch (Throwable th) {
            th.printStackTrace();
        }
	}

	public void ejbRemove() {}

	public void ejbActivate() {
        System.out.println ("In SFSB.ejbActivate() for name -> " + sfsbName);
    }

	public void ejbPassivate() {
        System.out.println ("In SFSB.ejbPassivate() for name -> " + sfsbName);
    }
}
