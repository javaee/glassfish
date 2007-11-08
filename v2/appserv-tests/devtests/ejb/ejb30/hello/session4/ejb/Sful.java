/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.hello.session4;

import javax.ejb.Remote;

@Remote
public interface Sful
{
    public void setId(String id);

    public Sful2 getSful2();

    public String hello();

    // defined on both remote business interfaces
    public void sameMethod();
    
}
