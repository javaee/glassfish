/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.clientview.core;

import javax.ejb.FinderException;

// Remote business interface

public interface Hello
{
    void warmup(int type);
    void shutdown();

    float notSupported(int type, boolean tx);
    float required(int type, boolean tx);
    float requiresNew(int type, boolean tx);
    float mandatory(int type, boolean tx);
    float never(int type, boolean tx);
    float supports(int type, boolean tx);

    void throwException() throws Exception;
    void throwAppException1() throws FinderException;
    void throwAppException2() throws FinderException;

    Object testPassByRef();
}
