/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/UnknownCmdHelperImpl.java,v 1.2 2004/10/14 19:06:21 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2004/10/14 19:06:21 $
 */
 
package com.sun.cli.jmxcmd.cmd;

 

import com.sun.cli.jcmd.framework.HelpCmd;
import org.glassfish.admin.amx.util.StringUtil;



public class UnknownCmdHelperImpl implements HelpCmd.UnknownCmdHelper
{
		public
	UnknownCmdHelperImpl( )
	{
	}
	
		private String
	extractCmd( String cmd )
	{
		String	cmdString	= cmd;
		
		int	index	= cmdString.indexOf( ":" );
		if ( index < 0 )
		{
			index	= cmdString.indexOf( "(" );
		}

		if ( index > 0 )
		{
			// indicates generic JMX method
			cmdString	= cmdString.substring( 0, index);
		}
		return( cmdString );
	}
	
		boolean
	likelyAnInvoke( String cmd )
	{
		return( cmd.indexOf( ":" ) > 0 ||
			cmd.indexOf( "(" ) > 0 );
	}
	
		String
	getHelpForLikelyInvoke( String cmdString )
	{
		String	msg	= "";
		
		if ( likelyAnInvoke( cmdString ) )
		{
			cmdString	= extractCmd( cmdString );
			
			msg	= "It appears you are asking for help on the MBean operation " +
				StringUtil.quote( cmdString ) + ".\n" +
				"Use the 'inspect' command to see what operations are available.\n" +
				"Use the 'help invoke' for help on invoking an operation.";
				
		}
		else
		{
			msg	= HelpCmd.getUnknownCmdHelper().getHelpUnknown( cmdString );
		}
		
		return( msg );
	}
	
	
		public String
	getHelpUnknown( String cmd )
	{
		return( getHelpForLikelyInvoke( cmd ) );
	}
}
