/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.clientview.adapted;

import javax.ejb.*;

public interface Common 
{
    public static final int STATELESS = 0;
    public static final int STATEFUL = 1;

    void notSupported();
    void required();
    void requiresNew();
    void mandatory();
    void never();
    void supports();

}
