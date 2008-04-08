/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/VersionOptionException.java,v 1.3 2005/11/15 20:21:42 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/15 20:21:42 $
 */
package com.sun.cli.jcmd.framework;


/**
	A command cannot be run normally because it contains a global version option.
	
	Higher level processing takes care of displaying the version.
 */
public class VersionOptionException extends CmdException
{
    static final long serialVersionUID = 780036082796136671L;
    
		public
	VersionOptionException( String cmdName, String msg )
	{
		super( cmdName, msg );
	}
}
