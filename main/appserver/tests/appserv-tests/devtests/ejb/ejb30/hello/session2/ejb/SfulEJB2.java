/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session2;

import javax.ejb.Stateful;
import javax.ejb.Remote;
import javax.ejb.EJB;
import javax.annotation.PostConstruct;
import javax.interceptor.Interceptors;
import javax.ejb.EJBs;
import javax.ejb.Remove;
import javax.ejb.SessionSynchronization;
import javax.annotation.PreDestroy;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;

import javax.annotation.Resource;
import javax.transaction.UserTransaction;

import java.util.Collection;
import java.util.HashSet;

@Stateful
public class SfulEJB2 implements Sful2, SessionSynchronization
{

    // use some package-local mutable static state to check whether
    // session synch callbacks are called correctly for @Remove methods.
    // This provides a simple way to check the results since the bean 
    // instance is no longer available to the caller.  The caller must
    // always at most one SFSBs of this bean type at a time for this
    // to work.  
    static boolean afterBeginCalled = false;
    static boolean beforeCompletionCalled = false;
    static boolean afterCompletionCalled = false;

    private @Resource SessionContext sc;

    public String hello() {
        System.out.println("In SfulEJB2:hello()");

        return "hello";
    }

    @Remove(retainIfException=true)
    public void removeRetainIfException(boolean throwException) 
        throws Exception {

        System.out.println("In SfulEJB2 " +
                           " removeRetainIfException");
        System.out.println("throwException = " + throwException);
        if( throwException ) {
            throw new Exception("throwing app exception from @Remove method");
        }
    }

    @Remove
    public void removeNotRetainIfException(boolean throwException) 
        throws Exception {

        System.out.println("In SfulEJB2 " +
                           "removeNotRetainIfException");
        System.out.println("throwException = " + throwException);
        if( throwException ) {
            throw new Exception("throwing app exception from @Remove method");
        }
    }

    @Remove
    public void removeMethodThrowSysException(boolean throwException) {

        System.out.println("In SfulEJB2 " + 
                           "removeMethodThrowSysException");
        System.out.println("throwException = " + throwException);
        if( throwException ) {
            throw new EJBException
                ("throwing system exception from @Remove method");
        }
    }

    public void afterBegin() {

        afterBeginCalled = true;
        beforeCompletionCalled = false;
        afterCompletionCalled = false;
        
        System.out.println("In SfulEJB2::afterBegin()");
    }

    public void beforeCompletion() {
        System.out.println("In SfulEJB2::beforeCompletion()");
        beforeCompletionCalled = true;
    }

    public void afterCompletion(boolean committed) {
        afterCompletionCalled = true;
        System.out.println("In SfulEJB2::afterCompletion()");
    }

}
