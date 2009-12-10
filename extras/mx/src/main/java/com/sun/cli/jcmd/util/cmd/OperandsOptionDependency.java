/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/OperandsOptionDependency.java,v 1.4 2005/11/08 22:39:18 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/08 22:39:18 $
 */
package com.sun.cli.jcmd.util.cmd;


/**
	A dependency denoting the number of required operands.
 */
public final class OperandsOptionDependency extends OptionDependency
{
	private final int	mMinRequired;
	private final int	mMaxRequired;
	
	public int		getMin()	{ return( mMinRequired ); }
	public int		getMax()	{ return( mMaxRequired ); }
	
	
	/**
		Create a new instance specifying the number of required operands
	 */
		public
	OperandsOptionDependency( final int minRequired, final int maxRequired )
	{
		super( (OptionInfo)null );
		mMinRequired	= minRequired;
		mMaxRequired	= maxRequired;
	}
}


