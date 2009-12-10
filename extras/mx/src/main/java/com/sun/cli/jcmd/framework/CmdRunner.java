/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdRunner.java,v 1.2 2003/11/12 00:59:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/12 00:59:45 $
 */
package com.sun.cli.jcmd.framework;

/**
	Interface for running commands
 */
public interface CmdRunner
{
	public interface Hook
	{
		void	preExecute( String cmdName, String[] tokens );
		void	postExecute( String cmdName, String[] tokens, int errorCode);
	}
	
	/**
		Execute the command given by 'cmdName' using parameters
		found in tokens.
		
		@param cmdName	the name of the command to execute
		@param tokens	parameters that were entered for the command
		@return result code of command, 0 if success
	 */
	int execute( String cmdName, String [] tokens );
};
 
 
 
 