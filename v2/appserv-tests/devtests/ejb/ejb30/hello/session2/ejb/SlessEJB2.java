/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session2;

import javax.ejb.Stateless;
import javax.ejb.Remote;
import javax.ejb.EJBException;
import javax.annotation.PostConstruct;
import javax.ejb.SessionBean;
import javax.ejb.CreateException;
import javax.ejb.SessionContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import javax.transaction.Status;

import java.util.Collection;
import java.util.Iterator;

@Stateless
@Remote({Sless.class})
public class SlessEJB2 implements Sless, SessionBean
{

    private SessionContext sc_ = null;
    private boolean postConstructCalled = false;

    @PostConstruct
    public void ejbCreate() {
        System.out.println("In SlessEJB2::ejbCreate()");        
        postConstructCalled = true;
    }

    public String hello() {
        System.out.println("In SlessEJB2:hello()");

        if( !postConstructCalled ) {
            throw new EJBException("post construct wasn't called");
        }

        try {
            sc_.getUserTransaction();
            throw new EJBException("should have gotten exception when accessing SessionContext.getUserTransaction()");
        } catch(IllegalStateException ise) {
            System.out.println("Got expected exception when accessing SessionContext.getUserTransaction()");
        }

        return "hello from SlessEJB2";
    }

    public String hello2() throws javax.ejb.CreateException {
        throw new javax.ejb.CreateException();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String getId() {

        try {
            // Proprietary way to look up tx manager.  
            TransactionManager tm = (TransactionManager)
                new InitialContext().lookup("java:appserver/TransactionManager");
            // Use an implementation-specific check to ensure that there
            // is no tx.  A portable application couldn't make this check
            // since the exact tx behavior for TX_NOT_SUPPORTED is not
            // defined.
            int txStatus = tm.getStatus();
            if( txStatus == Status.STATUS_NO_TRANSACTION ) {
                System.out.println("Successfully verified tx attr = " +
                                   "TX_NOT_SUPPORTED in SlessEJB2::getId()");
            } else {
                throw new EJBException("Invalid tx status for TX_NOT_SUPPORTED" +
                                       " method SlessEJB2::getId() : " + txStatus);
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }

        return "SlessEJB2";
    }
    
    public Sless roundTrip(Sless s) {
        System.out.println("In SlessEJB2::roundTrip " + s);
        System.out.println("input Sless.getId() = " + s.getId());
        return s;
    }

    public Collection roundTrip2(Collection collectionOfSless) {
        System.out.println("In SlessEJB2::roundTrip2 " + 
                           collectionOfSless);

        if( collectionOfSless.size() > 0 ) {
            Sless sless = (Sless) collectionOfSless.iterator().next();
            System.out.println("input Sless.getId() = " + sless.getId());  
        }

        return collectionOfSless;
    }

    public void setSessionContext(SessionContext sc)
    {
        sc_ = sc;
    }

    public void ejbRemove() 
    {}

    public void ejbActivate() 
    {}

    public void ejbPassivate()
    {}


}
