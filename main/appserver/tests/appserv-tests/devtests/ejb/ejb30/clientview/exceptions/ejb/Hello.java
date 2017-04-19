/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.clientview.exceptions;

import javax.ejb.FinderException;

// Remote business interface

public interface Hello
{

    void runTxRolledbackTest();
    void runTxRequiredTest();
    void runNoSuchObjectTest();

    void runAppExceptionTest();
    void runRollbackAppExceptionTest();
    void runAccessDeniedExceptionTest();

}
