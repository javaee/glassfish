/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.perf.local2;

import javax.ejb.*;

public interface Common extends EJBLocalObject
{
    public static final int STATELESS = 0;
    public static final int STATEFUL = 1;
    public static final int BMP = 2;
    public static final int CMP = 3;

    void notSupported();
    void required();
    void requiresNew();
    void mandatory();
    void never();
    void supports();
}
