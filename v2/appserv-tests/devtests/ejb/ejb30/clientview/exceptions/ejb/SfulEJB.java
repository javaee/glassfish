/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.clientview.exceptions;

import javax.ejb.*;
import javax.annotation.Resource;
import javax.annotation.PreDestroy;
import javax.annotation.security.DenyAll;

@Stateful
@AccessTimeout(0)
public class SfulEJB implements 
    SfulBusiness, SfulRemoteBusiness, SfulRemoteBusiness2
{
    int state = 0;

    private @Resource SessionContext ctx;

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void forceTransactionRequiredException() {}

    @Remove
    public void remove() {}

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void forceTransactionRolledbackException() {
        System.out.println("In SfulEJB::forceTransactionRolledbackException");
    }

    public void throwRuntimeAppException() throws RuntimeAppException {
        throw new RuntimeAppException();
    }

    public void throwRollbackAppException() throws RollbackAppException {
        throw new RollbackAppException();
    }

    @PreDestroy
    public void beforeDestroy() {
        System.out.println("In @PreDestroy callback in SfulEJB");
    }

    public void sleepFor(int sec) {
	try {
	    for (int i=0 ; i<sec; i++) {
		Thread.currentThread().sleep(1000);
	    }
	} catch (Exception ex) {
	}
    }

    public void ping() {
    }

    public void pingRemote() {
    }

    public void foo() {
    }

    @DenyAll
    public void denied() {


    }
}
