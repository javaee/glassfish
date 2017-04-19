/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.hello.session2full;

// Remote business interface

public interface Sful
{


    public void set(String value);
    public String get();

    public String hello();

    public void foo();
    public void foo(int a, String b);

    // The remote business interface has no relationship to EJBObject
    // so it's not a problem to define a method that happens to have the
    // same signature as one of EJBObject's methods.  remove() is the
    // most likely signature to match and it's a likely name
    // for a method that has @Remove behavior so it needs to work.
    public void remove();

    // Associated with an @Remove method that has retainIfException=true.    
    // If argument is true, the method will throw an exception, which should
    // keep the bean from being removed.  If argument is false, the bean
    // should stll be removed.
    public void removeRetainIfException(boolean throwException) 
        throws Exception;

    // Associated with an @Remove method that has retainIfException=false.    
    // Whether the argument is true or false, the bean should still be
    // removed.
    public void removeNotRetainIfException(boolean throwException) 
        throws Exception;
}
