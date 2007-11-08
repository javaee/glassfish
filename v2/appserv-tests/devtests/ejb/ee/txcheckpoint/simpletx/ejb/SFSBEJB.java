package com.sun.s1asdev.ejb.ee.txcheckpoint.simpletx.ejb;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import javax.transaction.UserTransaction;

import java.rmi.RemoteException;

public class SFSBEJB
    implements SessionBean 
{

    private static final String LOCAL_CHILD_SUFFIX = "_childLocal";

    private transient String message;

    private int activateCount;
    private int passivateCount;

    private SessionContext              sessionCtx;
    private String                      sfsbName;

    public void ejbCreate(String sfsbName) {
        this.sfsbName = sfsbName;
    }

    public String getName() {
        return this.sfsbName;
    }

    public String getTxName() {
        return this.sfsbName;
    }

    public void setSessionContext(SessionContext sc) {
        this.sessionCtx = sc;
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
