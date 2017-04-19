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

public class SFSBEJB
    implements SessionBean 
{

    private SessionContext              sessionCtx;
    
    private String                      accountHolderName;
    private transient int		balance;
    private int				checkpointedBalance;

    public void ejbCreate(String accountHolderName, int balance) {
        this.accountHolderName = accountHolderName;
	this.balance = balance;
    }

    public void setSessionContext(SessionContext sc) {
        this.sessionCtx = sc;
    }

    public void ejbRemove() {}

    public void ejbActivate() {
	balance = checkpointedBalance;
    }

    public void ejbPassivate() {
	checkpointedBalance = balance;
    }

    public String getAccountHolderName() {
        return this.accountHolderName;
    }

    public int getBalance() {
	return balance;
    }

    public void incrementBalance(int val) {
	balance += val;
    }

    public int getCheckpointedBalance() {
	return checkpointedBalance;
    }

    public void nonTxNonCheckpointedMethod() {
    }

    public void nonTxCheckpointedMethod() {
    }

    public void txNonCheckpointedMethod() {
    }

    public void txCheckpointedMethod() {
    }

}
