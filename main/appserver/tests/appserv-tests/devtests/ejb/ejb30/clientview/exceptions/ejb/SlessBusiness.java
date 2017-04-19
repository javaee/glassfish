/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.clientview.exceptions;

public interface SlessBusiness 
{

    void forceTransactionRequiredException();

    void forceTransactionRolledbackException();

    void throwRuntimeAppException() throws RuntimeAppException;

    void throwRollbackAppException() throws RollbackAppException;
    
    void denied();
}
