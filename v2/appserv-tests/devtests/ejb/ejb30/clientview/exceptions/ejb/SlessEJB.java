/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.clientview.exceptions;

import javax.ejb.*;
import javax.annotation.Resource;
import javax.annotation.security.DenyAll;

@Local(SlessBusiness.class)
@Stateless
public class SlessEJB implements SlessBusiness, SlessRemoteBusiness, SlessRemoteBusiness2
{

    private @Resource SessionContext ctx;

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void forceTransactionRequiredException() {}

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public void forceTransactionRolledbackException() {
        System.out.println("In SlessEJB::forceTransactionRolledbackException");
    }

    public void throwRuntimeAppException() throws RuntimeAppException {
        throw new RuntimeAppException();
    }

    public void throwRollbackAppException() throws RollbackAppException {
        throw new RollbackAppException();
    }

    @DenyAll
    public void denied() {


    }

}
