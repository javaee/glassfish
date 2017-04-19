/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.clientview.exceptions;

// Remote business interface
import javax.ejb.Remote;

@Remote
public interface SlessRemoteBusiness 
{

    void forceTransactionRequiredException();

    void forceTransactionRolledbackException();

    void throwRuntimeAppException() throws RuntimeAppException;

    void throwRollbackAppException() throws RollbackAppException;
    
    void denied();
}
