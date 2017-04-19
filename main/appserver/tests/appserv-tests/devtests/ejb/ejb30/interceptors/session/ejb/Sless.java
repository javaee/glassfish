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

    public void setFoo(Foo foo)
        throws MyBadException;

    public void setBar(Bar bar)
        throws MyBadException;

    public void emptyArgs()
        throws MyBadException;

    public void objectArgs(Object obj)
        throws MyBadException;

    public void setInt(int val)
        throws MyBadException;

    public int addIntInt(int i, int j)
        throws WrongResultException, MyBadException;

    public void setLong(long obj)
        throws MyBadException;

    public long addLongLong(long obj, long k)
        throws MyBadException;

}

class Foo implements java.io.Serializable  {}

class SubFoo extends Foo {}

class Bar implements java.io.Serializable  {}

