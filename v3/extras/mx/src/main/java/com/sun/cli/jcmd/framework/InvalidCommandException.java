/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/InvalidCommandException.java,v 1.3 2005/11/15 20:21:42 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/15 20:21:42 $
 */
package com.sun.cli.jcmd.framework;

/**
	Thrown if the command is not a valid command.
 */
public class InvalidCommandException extends CmdException
{
    static final long serialVersionUID = 6412234020958060869L;
    
		public
	InvalidCommandException( String cmdName, String msg)
	{
		super( cmdName, msg, INVALID_COMMAND_ERROR );
	}
}
