/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
