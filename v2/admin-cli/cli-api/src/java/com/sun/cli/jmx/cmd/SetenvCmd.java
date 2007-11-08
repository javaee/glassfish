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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/SetenvCmd.java,v 1.3 2005/12/25 03:45:40 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:40 $
 */
 
package com.sun.cli.jmx.cmd;

import java.util.ArrayList;
import java.util.Iterator;

import com.sun.cli.util.stringifier.IteratorStringifier;

import com.sun.cli.jmx.spi.JMXConnectorProvider;

public class SetenvCmd extends JMXCmd
{
		public
	SetenvCmd( final CmdEnv env )
	{
		super( env );
	}
	
		int
	getNumRequiredOperands()
	{
		return( 0 );
	}
	
	
		String
	getUsage()
	{
		return( CmdStrings.SETENV_HELP.toString() );
		//return( CmdStrings.SETENV_HELP.toString() );
	}
	
	public final static String NAME			= "setenv";
	
		public static String []
	getNames( )
	{
		return( new String [] { NAME } );
	}
	
	
	static private final String	OPTIONS_INFO	= "";
		
	
		ArgHelper.OptionsInfo
	getOptionInfo()
		throws ArgHelper.IllegalOptionException
	{
		return( new ArgHelperOptionsInfo( OPTIONS_INFO ) );
	}


		void
	displayEnv()
	{
		final Iterator	iter	= getEnvKeys().iterator();
		
		while ( iter.hasNext() )
		{
			final String name	= (String)iter.next();
			
			final Object value	= envGet( name );
			
			if ( value instanceof String )
			{
				println( name + "=" + value );
			}
		}
	}
	
	public final static char	DELIM	= '=';
	
		void
	executeInternal()
		throws Exception
	{
		final String []	operands	= getOperands();
		
		if ( operands.length == 0 )
		{
			displayEnv();
		}
		else if ( operands.length == 1 )
		{
			final String oper	= operands[ 0 ];
			final int			delimIndex	= oper.indexOf( DELIM );
			
				
			if ( delimIndex > 0 )
			{
				final String	name	= oper.substring( 0, delimIndex );
				final String	value	= oper.substring( delimIndex + 1, oper.length() );
				
				envPut( name, value, true );
			}
			else
			{
				if ( envGet( oper ) != null )
				{
					envRemove( oper );
					println( "Variable " + oper + " removed." );
				}
				else
				{
					println( "Variable " + oper + " does not exist." );
				}
			}
		}
		else
		{
			printUsage();
		}
	}
}






