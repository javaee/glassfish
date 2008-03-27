/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/ConnectionDiedException.java,v 1.1 2003/11/21 21:23:41 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/21 21:23:41 $
 */
package com.sun.cli.jmxcmd.cmd;

import java.io.IOException;
import javax.management.ObjectName;
import com.sun.cli.jcmd.framework.CmdException;

public class ConnectionDiedException extends CmdException
{
		private static String
	getObjectNamePart( ObjectName objectName )
	{
		final String objectNamePart	= (objectName == null) ?
			"" : " on object " + quote( objectName );
	
		return( objectNamePart );
	}

		public
	ConnectionDiedException(
		IOException		cause,
		ObjectName		objectName,
		String			actionName,
		String			subCmdName )
	{
		super( subCmdName, "Connection died while performing " + quote( actionName ) +
			getObjectNamePart( objectName ) +
			" while executing command " + quote( subCmdName ) +
			"(" + cause.getMessage() + ")",
			IO_EXCEPTION,
			cause );
	}
	
		public
	ConnectionDiedException(
		IOException		cause,
		String			actionName,
		String			subCmdName )
	{
		this( cause, null, actionName, subCmdName );
	}
}
