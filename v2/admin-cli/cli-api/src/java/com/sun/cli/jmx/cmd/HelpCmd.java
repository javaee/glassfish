/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/HelpCmd.java,v 1.3 2005/12/25 03:45:36 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:36 $
 */
 
package com.sun.cli.jmx.cmd;

import com.sun.cli.jmx.support.CLISupportMBeanProxy;
import com.sun.cli.jmx.support.InspectRequest;
import com.sun.cli.jmx.support.InspectResult;
import com.sun.cli.jmx.support.InspectResult;
 
import com.sun.cli.util.stringifier.ArrayStringifier;

public class HelpCmd extends JMXCmd
{
		public
	HelpCmd( final CmdEnv env )
	{
		super( env );
	}
	
		int
	getNumRequiredOperands()
	{
		// there may be some, but there may be none
		return( 0 );
	}
	
	
		String
	getUsage()
	{
		String	usage	= "*** Available commands ***\n\n";
		
		final CmdStrings.CmdHelp [] allHelp	= CmdStrings.getAllHelp();
		
		for( int i = 0; i < allHelp.length; ++i )
		{
			usage	= usage + allHelp[ i ].getSynopsis() + "\n\n";
		}
		
		return( usage );
	}
	
		public static String []
	getNames( )
	{
		return( new String [] { "help", "h", "--h"} );
	}
	
		private String
	stripColon( String cmd )
	{
		String	cmdString	= cmd;
		
		if ( cmdString.endsWith( ":" ) )
		{
			// indicates generic JMX method
			cmdString	= cmdString.substring( 0, cmdString.length() -1);
		}
		return( cmdString );
	}
	
		String
	getHelpUnknown( String cmdString )
	{
		cmdString	= stripColon( cmdString );
		
		String msg	= "";
		
		try
		{
			establishProxy();

			// if there is a proxy, see if any MBeans have matching operations
			final CLISupportMBeanProxy	proxy	= getProxy();
			
			if ( proxy != null && ! cmdString.equals( "*" ) )
			{
				final InspectRequest	request	= new InspectRequest();
				
				// ask for operations only
				request.includeSummary		= true;
				request.includeDescription	= false;
				request.attrs				= null;
				request.notifications		= null;
				request.constructors		= false;
				request.operations			= cmdString;
				final InspectResult []	results	=
						proxy.mbeanInspect( request, new String [] { "*" }  );
			
				String	operationsMsg	= "";
				
				for( int i = 0; i < results.length; ++i )
				{
					final InspectResult	result	= results[ i ];
					
					if ( result.operationsInfo.length != 0 )
					{
						operationsMsg	= operationsMsg + result.objectInstance.getObjectName() + "\n";
						operationsMsg	= operationsMsg + ArrayStringifier.DEFAULT.stringify( result.operationsInfo, "\n");
						operationsMsg	= operationsMsg + "\n\n";
					}
				}
				
				if ( operationsMsg.length() != 0 )
				{
					msg	= msg + operationsMsg;
				}
			}
		}
		catch( Exception e )
		{
			// squelch
		}
		
		return( msg );
	}
	
		void
	executeInternal()
		throws Exception
	{
		final String [] operands	= getOperands();
		
		if ( operands.length == 0 )
		{
			printUsage();
		}
		else
		{
			for ( int i = 0; i < operands.length; ++i )
			{
				final String	cmd	= operands[ i ];
				
				CmdStrings.CmdHelp	help	= CmdStrings.getHelp( cmd );
				
				if ( help == null )
				{
					final Class cmdClass	= getCmdFactory().getClass( cmd );
					
					if ( cmdClass != null )
					{
						final String [] aka	= getCmdNames( cmdClass );
						if ( aka != null && aka.length != 0)
						{
							help	= CmdStrings.getHelp( aka[ 0 ] );
						}
					}
				}
				
				String	msg	= null;
				
				if ( help != null )
				{
					msg	= help.toString();
					
					final Class cmdClass	= getCmdFactory().getClass( cmd );
					if ( cmdClass != null )
					{
						msg	= msg + "\n" + getAlsoKnownAs( cmdClass );
					}
				}
				else
				{
					println( "Searching for MBeans with operation \"" + stripColon( cmd ) + "\"" );
					msg	= getHelpUnknown( cmd );
					if ( msg == null || msg.length() == 0 )
					{
						println( "No matching operations found" );
					}
				}
				
				println( msg );
			}
		}
	}
}
