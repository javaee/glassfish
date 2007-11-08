/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.ejb.ejb30.interceptors.session;

public class AppException
    extends Exception
{
    AppException(String msg) {
        super(msg);
    }
}
