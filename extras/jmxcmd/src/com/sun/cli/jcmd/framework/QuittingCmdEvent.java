/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/QuittingCmdEvent.java,v 1.2 2003/11/12 00:59:46 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:46 $
 */
 

package com.sun.cli.jcmd.framework;

/**
	This even signifies that the process is about to die.
 */
public class QuittingCmdEvent extends CmdEventBase
{
		public
	QuittingCmdEvent( Object source )
	{
		super( source );
	}
};



