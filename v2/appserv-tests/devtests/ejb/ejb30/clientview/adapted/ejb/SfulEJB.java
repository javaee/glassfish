/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.clientview.adapted;

import javax.ejb.*;
import javax.annotation.Resource;
import javax.annotation.PreDestroy;

@Stateful
@Remote({SfulRemoteBusiness.class, SfulRemoteBusiness2.class})
@Local({SfulBusiness.class, SfulBusiness2.class})
@RemoteHome(SfulRemoteHome.class)
@LocalHome(SfulHome.class)
    public class SfulEJB implements SfulBusiness, SfulBusiness2, SfulRemoteBusiness, SfulRemoteBusiness2, java.io.Serializable
{
    int state = 0;

    private @Resource SessionContext ctx;

    public void foo() {
        System.out.println("In SfulEJB::SfulBusiness2::foo()");

        Class clazz = ctx.getInvokedBusinessInterface();
        if( clazz == SfulBusiness2.class ||
            clazz == SfulRemoteBusiness2.class ) {
            System.out.println("Got correct value for " +
                               "getInvokedBusinessInterface = " + clazz);
        } else {
            throw new EJBException("Wrong invoked business interface = " +
                                   clazz);
        }
    }

    public void bar() {
        System.out.println("In SfulEJB::SfulBusiness2::bar()");
    }

    public SfulBusiness2 getSfulBusiness2() {

        Class clazz = ctx.getInvokedBusinessInterface();
        if( clazz == SfulBusiness.class ) {
            System.out.println("Got correct value for " +
                               "getInvokedBusinessInterface = " + clazz);
        } else {
            throw new EJBException("Wrong invoked business interface = " +
                                   clazz);
        }

        try {
            ctx.getBusinessObject(java.io.Serializable.class);
            throw new EJBException("Should have gotten exception when " +
                                   "calling getBusinessObject with invalid " +
                                   "business interface");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got exception for invalid call " +
                               "to ctx.getBusinessObject()");
        }

        try {
            ctx.getBusinessObject(null);
            throw new EJBException("Should have gotten exception when " +
                                   "calling getBusinessObject with null " +
                                   "business interface");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got exception for invalid call " +
                               "to ctx.getBusinessObject()");
        }

        return (SfulBusiness2) ctx.getBusinessObject(SfulBusiness2.class);
    }

    @Init
    public void adaptedCreate() {
        System.out.println("In SfulEJB::adaptedCreate method");

        try {
            ctx.getInvokedBusinessInterface();
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got exception when invoking " +
                               "ctx.getInvokedBusinessInterface() from an " +
                               "adapted create method.");
        }

        System.out.println("Caller principal = " + ctx.getCallerPrincipal());
    }

    @Init("create")
    public void adaptedCreate1(int ignore) {
        // Ignore input parameter.  client will use resulting unset state to
        // ensure correct mapping of @Init

        System.out.println("In SfulEJB::adaptedCreate1(int ignore) method. " 
                           + "**ignoring** state=" + state);

        System.out.println("Caller principal = " + ctx.getCallerPrincipal());

    }

    @Init("createFoo")
    public void adaptedCreate2(int argument1) {
        state = argument1;
        System.out.println("In SfulEJB::adaptedCreate2(int arg) method " 
                           + "state=" + state);

        System.out.println("Caller principal = " + ctx.getCallerPrincipal());

    }

    public int getState() {
        try {
            ctx.getInvokedBusinessInterface();
        } catch(IllegalStateException ise) {
            System.out.println("Successfully got exception when invoking " +
                               "ctx.getInvokedBusinessInterface() from a " +
                               "Remote component interface method.");
        }

        return state;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void notSupported() {}

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void required() {}

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void requiresNew() {}

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void mandatory() {}

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void never() {}

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void supports() {}

    @Remove
    public void remove() {}

    @Remove(retainIfException=true)
    public void removeRetainIfException(boolean throwException) 
        throws Exception {
        if( throwException ) {
            throw new Exception("exception from retainIfException. " +
                                "throwException = " + throwException);
        }
    }

    @PreDestroy
    public void beforeDestroy() {
        System.out.println("In @PreDestroy callback in SfulEJB");
    }
}
