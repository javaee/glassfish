/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.ejb.ejb30.clientview.exceptions;

import javax.ejb.ApplicationException;

@ApplicationException(rollback=true)
public class RollbackAppException extends RuntimeException
{

}
