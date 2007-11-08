/**
 * Copyright š 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.transaction.txstress.ejb.beanA;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.transaction.UserTransaction;
import java.rmi.RemoteException;
import com.sun.s1peqe.transaction.txstress.ejb.beanB.*;

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
    public void txCommit(int identity) throws RemoteException {
        System.out.println("txCommit in BeanA");
        try {
            TxRemoteB beanB = home.create();
            beanB.insert("A1001", 3000,identity);
            beanB.remove();
            System.out.println("After beanB Remove call");
        } catch (Exception ex) {
            Thread.dumpStack();
            System.out.println(" Exception is " + ex);
            System.out.println("Exception in txCommit: " + ex.toString());
            ex.printStackTrace();
        }
    }


}

