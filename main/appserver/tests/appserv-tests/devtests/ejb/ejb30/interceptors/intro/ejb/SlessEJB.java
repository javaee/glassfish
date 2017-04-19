/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.interceptors.intro;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;


@Stateless
public class SlessEJB
	implements Sless
{
    public String sayHello() {
	    return "Hello";
    }

    @Interceptors(ArgumentsVerifier.class)
    public String concatAndReverse(String one, String two) {
        String str = null;
        if (one != null) {
            str = one;
        }
        if (two != null) {
            str = str + two;
        }

        String result = null;
        if (str != null) {
            result = "";
            int len = str.length()-1;
            for (int i=str.length()-1; i>=0; i--) {
                result += str.charAt(i);
            }
        }

        return result;
    }

    @Interceptors(ArgumentsVerifier.class)
    public double plus(short s, int ival, long lval) {
        return s + ival + lval;
    }

    @Interceptors(ArgumentsVerifier.class)
    public boolean isGreaterShort(Number one, Number two) {
        return one.shortValue() > two.shortValue();
    }

}


