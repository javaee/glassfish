/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.enterprise.admin.common.exception;

public class IllegalStateException extends ControlException
{
    /**
        Creates new <code>IllegalStateException</code> without detail message.
    */
    public IllegalStateException()
    {
        super();
    }

    /**
        Constructs an <code>IllegalStateException</code> with the specified 
        detail message.
        @param msg the detail message.
    */
    public IllegalStateException(String msg)
    {
        super(msg);
    }
}