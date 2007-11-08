/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

import java.util.Map;
import java.util.HashMap;

import javax.ejb.Remote;

@Remote
public interface Sful
{
    public String hello();

    public int getCount();
    
    public void throwAppException(String msg)
        throws AppException;

    public String computeMid(int min, int max)
	    throws SwapArgumentsException;

    public String callDummy()
	    throws Exception;

    public String eatException() 
        throws EatException;

    public void  resetLifecycleCallbackCounters();

    public int getPrePassivateCallbackCount();

    public int getPostActivateCallbackCount();
    
    public void setID(int val);
    
    public boolean isStateRestored();

    public Map<String, Boolean> checkSetParams();

    public void assertIfTrue(boolean val)
        throws AssertionFailedException;

    public int getAssertionFailedCount();

}

