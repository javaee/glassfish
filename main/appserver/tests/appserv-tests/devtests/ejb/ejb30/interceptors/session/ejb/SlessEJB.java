/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;


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

    @Interceptors(ArgumentsVerifier.class)
    public void setFoo(Foo foo) {
    }

    @Interceptors(ArgumentsVerifier.class)
    public void setBar(Bar bar) {
    }

    @Interceptors(ArgumentsVerifier.class)
    public void emptyArgs() {
    }

    @Interceptors(ArgumentsVerifier.class)
    public void objectArgs(Object obj) {
    }

    @Interceptors(ArgumentsVerifier.class)
    public void setInt(int val)
        throws MyBadException {
    }

    @Interceptors(ArgumentsVerifier.class)
    public int addIntInt(int i, int j)
        throws WrongResultException, MyBadException {
        return i + j;
    }

    @Interceptors(ArgumentsVerifier.class)
    public void setLong(long obj)
        throws MyBadException {
    }

    @Interceptors(ArgumentsVerifier.class)
    public long addLongLong(long obj, long k)
        throws MyBadException {
        return obj + k;
    }

}


