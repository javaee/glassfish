/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.interceptors.intro;

import javax.ejb.Remote;

@Remote
public interface Sless
{
    public String concatAndReverse(String one, String two);

    public boolean isGreaterShort(Number one, Number two);

    public double plus(short s, int ival, long lval);

}

