package com.sun.s1asdev.ejb.ee.ejb;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import javax.transaction.UserTransaction;

import java.rmi.RemoteException;

public class BMTSessionBean
    implements SessionBean 
{

    private static final String LOCAL_CHILD_SUFFIX = "_childLocal";

    private transient String message;

    private int activateCount;
    private int passivateCount;
    private int	count;

    private SessionContext              sessionCtx;
    private String                      sfsbName;
    private UserTransaction		userTx;

    public void ejbCreate(String sfsbName) {
        System.out.println ("In SFSB.ejbCreate() for name -> " + sfsbName);
        this.sfsbName = sfsbName;
    }

    public String getName() {
        System.out.println("In getName() for " + sfsbName);
        return this.sfsbName;
    }

    public String getTxName() {
        return this.sfsbName;
    }

    public void checkpoint() {
    }

    public void checkpoint2() {
    }

    public void setSessionContext(SessionContext sc) {
        this.sessionCtx = sc;
    }

    public void startTx() {
	try {
	    userTx = sessionCtx.getUserTransaction();
	    userTx.begin();
	} catch (Exception ex) {
	    throw new EJBException(ex.toString());
	}
    }

    public void commitTx() {
	try {
	    userTx.commit();
	} catch (Exception ex) {
	    throw new EJBException(ex.toString());
	}
    }

    public void rollbackTx() {
	try {
	    userTx.rollback();
	} catch (Exception ex) {
	    throw new EJBException(ex.toString());
	}
    }

    public void incrementCount() {
	count++;
    }

    public int getActivateCount() {
	return activateCount;
    }

    public int getPassivateCount() {
	return passivateCount;
    }

    public void ejbRemove() {}

    public void ejbActivate() {
	activateCount++;
    }

    public void ejbPassivate() {
	passivateCount++;
    }

}
