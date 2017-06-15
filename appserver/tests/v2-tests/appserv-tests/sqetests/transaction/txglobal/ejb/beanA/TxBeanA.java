/**
 * Copyright š 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.transaction.txglobal.ejb.beanA;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.transaction.UserTransaction;
import java.rmi.RemoteException;
import com.sun.s1peqe.transaction.txglobal.ejb.beanB.*;

public class TxBeanA implements SessionBean {

    private TxRemoteHomeB home = null;
    private UserTransaction tx = null;
    private SessionContext context = null;

    // ------------------------------------------------------------------------
    // Container Required Methods
    // ------------------------------------------------------------------------
    public void ejbCreate() throws RemoteException {
        Class homeClass = TxRemoteHomeB.class;
        System.out.println("ejbCreate in BeanA");
        try {
            Context ic = new InitialContext();
            java.lang.Object obj = ic.lookup("java:comp/env/ejb/TxBeanB");
            home = (TxRemoteHomeB) PortableRemoteObject.narrow(obj, homeClass);
         } catch (Exception ex) {
            System.out.println("Exception in ejbCreate: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void setSessionContext(SessionContext sc) {
        System.out.println("setSessionContext in BeanA");
        this.context = sc;
    }

    public void ejbRemove() {
        System.out.println("ejbRemove in BeanA");
    }
  
    public void ejbDestroy() {
        System.out.println("ejbDestroy in BeanA");
    }

    public void ejbActivate() {
        System.out.println("ejbActivate in BeanA");
    }

    public void ejbPassivate() {
        System.out.println("ejbPassivate in BeanA");
    }


    // ------------------------------------------------------------------------
    // Business Logic Methods
    // ------------------------------------------------------------------------
    public boolean txCommit() throws RemoteException {
        boolean result = false;
        System.out.println("txCommit in BeanA");
        try {
            TxRemoteB beanB = home.create();
            tx = context.getUserTransaction();

            if ( (beanB == null) || (tx == null) ) {
                throw new NullPointerException();
            }

            tx.begin();
            beanB.delete("A1000");
            beanB.insert("A1001", 3000);
            beanB.sendJMSMessage("JMS Message-1");
            beanB.insert("A1002", 5000);
            tx.commit();

            result = beanB.verifyResults("A1002", "DB1");
            result = result && beanB.verifyResults("A1002", "DB2");
            result = result && beanB.verifyResults("JMS Message-1", "JMS");

            beanB.remove();
        } catch (Exception ex) {
            System.out.println("Exception in txCommit: " + ex.toString());
            ex.printStackTrace();
        }
        return result;
    }

    public boolean txRollback() throws RemoteException {
        boolean result = false;
        System.out.println("txRollback in BeanA");
        try {
            TxRemoteB beanB = home.create();
            tx = context.getUserTransaction();

            if ( (beanB == null) || (tx == null) ) {
                throw new NullPointerException();
            }
     
            tx.begin();
            beanB.delete("A1001");
            beanB.insert("A1003", 8000);
            beanB.sendJMSMessage("JMS Message-2");
            tx.rollback();

            result = !beanB.verifyResults("A1003", "DB1");
            result = result && !beanB.verifyResults("A1003", "DB2");
            result = result && !beanB.verifyResults("JMS Message-2", "JMS");

            beanB.remove();
        } catch (Exception ex) {
            System.out.println("Exception in txCommit: " + ex.toString());
            ex.printStackTrace();
        }
        return result;
    }
}

