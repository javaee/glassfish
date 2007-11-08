/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.hello.session4;

import javax.ejb.Remote;

@Remote
public interface Sful2
{
    public String hello2();

    public String getId();

    // defined on both remote business interfaces
    public void sameMethod();

}
