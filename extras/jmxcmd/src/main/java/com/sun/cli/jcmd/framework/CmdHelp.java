/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdHelp.java,v 1.3 2004/02/06 02:11:22 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/02/06 02:11:22 $
 */
 
package com.sun.cli.jcmd.framework;

/**
	Interface for getting help.
 */
public interface CmdHelp
{
	/**
		Get the collective name for this Cmd class
	 */
	public String	getName();
	
	/**
		Get all the names by which this command is known.
	 */
	public String[]	getNames();
	
	/**
		Get a brief summary of this command.
	 */
	public String	getSynopsis();
	
	/**
		Get the syntax for this command
	 */
	public String	getSyntax();
	
	/**
		Get the full content describing this command.
	 */
	public String	getText();
	
	
	/**
		Get a complete help text describing the command.
	 */
	public String	toString();
}





