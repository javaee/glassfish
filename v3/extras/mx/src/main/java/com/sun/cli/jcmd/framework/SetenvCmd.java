/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/SetenvCmd.java,v 1.11 2005/11/15 20:21:42 llc Exp $
 * $Revision: 1.11 $
 * $Date: 2005/11/15 20:21:42 $
 */
 
package com.sun.cli.jcmd.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import org.glassfish.admin.amx.util.stringifier.IteratorStringifier;

import com.sun.cli.jcmd.util.cmd.ArgHelper;
import com.sun.cli.jcmd.util.cmd.IllegalOptionException;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdBase;


import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;



/**
	Manages environment variables.
 */
public class SetenvCmd extends CmdBase
{
	
		public
	SetenvCmd( final CmdEnv env )
	{
		super( env );
	}
	
	
	static final class SetenvCmdHelp extends CmdHelpImpl
	{
			public
		SetenvCmdHelp()	{ super( getCmdInfos() ); }
		
		static final String	SYNOPSIS		= "manage environment variables";
			
		static final String	TEXT		= 
	"\nNote: variables persist across invocations launches of the CLI. " +
	"However, they are neither imported " +
	"nor exported to/from the shell.\n" +
	"";

		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( TEXT ); }
	}
	
		public CmdHelp
	getHelp()
	{
		return( new SetenvCmdHelp() );
	}

	static final String	SETENV_NAME			= "setenv";
	static final String	ENV_NAME			= "env";
	static final String	UNSETENV_NAME		= "unsetenv";
	
	
	public final static OptionInfo		ALL_OPTION	= new OptionInfoImpl( "all", "a" );
	private static final OptionsInfo	ENV_OPTIONS	= new OptionsInfoImpl( new OptionInfo[] { ALL_OPTION } );


	private final static CmdInfo	ENV_CMD_INFO	=
		new CmdInfoImpl( ENV_NAME, ENV_OPTIONS,
			new OperandsInfoImpl( "[<prefix>[ <prefix>]*]", 0));
			
	private final static CmdInfo	SETENV_CMD_INFO	=
		new CmdInfoImpl( SETENV_NAME, null,
			new OperandsInfoImpl( "<name>=<value>", 1, 1) );
			
	private final static CmdInfo	UNSETENV_CMD_INFO	=
		new CmdInfoImpl( UNSETENV_NAME, null,
			new OperandsInfoImpl( "<name>", 1, 1) );
	
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( SETENV_CMD_INFO, ENV_CMD_INFO, UNSETENV_CMD_INFO ) );
	}


		void
	displayVariable( String name )
	{
		boolean	displayAll	= false;
		
		try
		{
			displayAll	= getBoolean( ALL_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
		}
		catch( IllegalOptionException e )
		{
			assert( false );
		}
		
		if ( displayAll || envIsPersistable( name ) )
		{
			final Object value	= envGet( name );
			
			if ( value != null )
			{
				println( name + "=" + SmartStringifier.toString( value ) );
			}
			else
			{
				println( "Variable " + name + " does not exist." );
			}
		}
	}
	
		void
	displayEnv( String[] names)
	{
		final Iterator	iter	= getEnvKeys().iterator();
		
		final Set<String>	matchingKeys	= new HashSet<String>();
		
		while ( iter.hasNext() )
		{
			final String	key	= (String)iter.next();
			
			if ( names.length == 0 )
			{
				matchingKeys.add( key );
			}
			else
			{
				for( int i = 0; i < names.length; ++i )
				{
					if ( key.startsWith( names[ i ] ) )
					{
						matchingKeys.add( key );
						break;
					}
				}
			}
		}
		
		final String[]	keys	= new String[ matchingKeys.size() ];
		matchingKeys.toArray( keys );
		Arrays.sort( keys );
		for( int i = 0; i < keys.length; ++i )
		{
			displayVariable( keys[ i ] );
		}
	}
	
	public final static char	DELIM	= '=';
	
		protected void
	executeInternal()
		throws Exception
	{
		final String	cmd			= getSubCmdNameAsInvoked();
		final String []	operands	= getOperands();
		
		if ( cmd.equals( ENV_NAME ) )
		{
			displayEnv( operands );
		}
		else if ( cmd.equals( SETENV_NAME ) )
		{
			if ( operands.length == 1 )
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
					throw new IllegalUsageException( "Requires syntax name=value" );
				}
			}
			else
			{
				throw new IllegalUsageException( "Requires one operand." );
			}
		}
		else if ( cmd.equals( UNSETENV_NAME ) )
		{
			if ( operands.length == 1 )
			{
				final String	name	= operands[ 0 ];
				if ( envGet( name ) != null )
				{
					envRemove( name );
					println( "Variable " + name + " removed." );
				}
				else
				{
					println( "Variable " + name + " does not exist." );
				}
			}
			else
			{
				throw new IllegalUsageException( "Requires one operand." );
			}
		}
		else
		{
			throw new IllegalUsageException( cmd );
		}
	}
}






