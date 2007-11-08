/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import javax.ejb.Local;

@Local
public interface Sless
{
    public String sayHello();

    public double computeMidPoint(int min, int max)
	    throws SwapArgumentsException;
}

