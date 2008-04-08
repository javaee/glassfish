/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/OperandsInfo.java,v 1.3 2005/11/08 22:39:18 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:18 $
 */
 
package com.sun.cli.jcmd.util.cmd;

/**
	Information about the operands a command supports
 */
public interface OperandsInfo
{
	public final static int	NO_MAX	= 0x7FFFFFFF;
	
	public int			getMinOperands();
	public int			getMaxOperands();
	public String		toString();
}





