/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.clientview.adapted;


public interface SfulBusiness extends Common, CommonFoo
{
    // The local business interface has no relationship to EJBLocalObject
    // so it's not a problem to define a method that happens to have the
    // same signature as one of EJBLocalObject's methods.  remove() is
    // a likely name for a method that has @Remove behavior so it needs
    // to work.  
    public void remove();

    public void removeRetainIfException(boolean throwException) 
        throws Exception;

    public SfulBusiness2 getSfulBusiness2();

}
