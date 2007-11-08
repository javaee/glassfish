/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import javax.ejb.Stateless;


//Default is @Local
@Stateless
public class SlessEJB
	implements Sless
{
    public String sayHello() {
	    return "Hello";
    }

    public double computeMidPoint(int min, int max)
	    throws SwapArgumentsException
    {
	    if (min > max) {
		    throw new SwapArgumentsException("("+min+", "+max+")");
	    }
	    return (min*1.0+max)/2.0;
    }
}

