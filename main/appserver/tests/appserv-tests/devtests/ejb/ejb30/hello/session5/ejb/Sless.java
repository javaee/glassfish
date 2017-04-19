/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session5;

// Remote business interface

public interface Sless
{
    public String hello();

    // defined on both remote business interfaces
    public String foo(int a);
}
