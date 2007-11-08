/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.hello.session2;

// Remote business interface

public interface Sless
{
    public String hello();

    public String hello2() throws javax.ejb.CreateException;

    public String getId();

    public Sless roundTrip(Sless s);

    public java.util.Collection roundTrip2(java.util.Collection collOfSless);
}
