/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/IllegalUsageException.java,v 1.4 2005/11/15 20:21:42 llc Exp $
 * $Revision: 1.4 $
 * $Date: 2005/11/15 20:21:42 $
 */
package com.sun.cli.jcmd.framework;
 
public class IllegalUsageException extends CmdException
{
    static final long serialVersionUID = -2241216017353938067L;
    
		public
	IllegalUsageException( String cmdName )
	{
		super( cmdName, ILLEGAL_USAGE_ERROR );
	}
		public
	IllegalUsageException( String cmdName, String msg)
	{
		super( cmdName, msg );
	}
}
