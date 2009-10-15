/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdEventBase.java,v 1.2 2003/11/12 00:59:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:45 $
 */
 

package com.sun.cli.jcmd.framework;

/**
	This even signifies that the process is about to die.
 */
public class CmdEventBase implements CmdEvent
{
	final Object	mSource;
	
		public
	CmdEventBase( Object source )
	{
		mSource	= source;
	}
	
		public Object
	getSource()
	{
		return( mSource );
	}
	
		public String
	toString()
	{
		return( this.getClass().getName() );
	}
};



