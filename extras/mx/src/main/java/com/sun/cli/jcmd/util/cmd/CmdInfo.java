/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/cmd/CmdInfo.java,v 1.3 2005/11/08 22:39:18 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:18 $
 */
 
package com.sun.cli.jcmd.util.cmd;

import com.sun.cli.jcmd.util.cmd.OptionsInfo;

/**
	Interface for info about a particular subcommand.  There should be one
	of these for every command that a Cmd class implements.
 */
public interface CmdInfo
{
	public String		getName();
	public OptionsInfo	getOptionsInfo();
	public OperandsInfo	getOperandsInfo();
	
	public String		toString();
	public String		getSyntax();
}





