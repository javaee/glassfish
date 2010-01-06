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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/framework/CmdAliasesCmd.java,v 1.5 2003/12/18 20:59:16 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2003/12/18 20:59:16 $
 */
 
package com.sun.cli.jcmd.framework;

import java.util.Arrays;

import com.sun.cli.jcmd.util.cmd.IllegalOptionException;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;

	
/**
	Manages aliases for commands
 */
public class CmdAliasesCmd extends CmdBase
{
		public
	CmdAliasesCmd( final CmdEnv env )
	{
		super( env );
	}	
	
	static final class CmdAliasesCmdHelp extends CmdHelpImpl
	{
		public	CmdAliasesCmdHelp()	{ super( getCmdInfos() ); }
		
		// alias commands
		private final static String	SYNOPSIS	= "manage command aliases";
			
			
		private final static String	TEXT		=
	CREATE_ALIAS_INFO.getName() +
	" -- creates an alias with the specified name and value. " +
	"If the value contains white space, it should be quoted." +
	"\n\n" +
	"create-cmd-alias Examples: \n" +
	"   create-cmd-alias f=find\n" +
	"\n\n" +
	DELETE_ALIAS_INFO.getName() +
	" -- Deletes an existing command alias.\n" +
	"\n\n" +
	LIST_ALIASES_INFO.getName() +
	" -- list command aliases, along with their values.  To list all command aliases prefixed " +
	"with one or more strings, specify the prefixes as operands.\n" +
	"\n\n" +
	"";
		
		public String	getName()		{	return( NAME ); }
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( TEXT ); }
	}

		public CmdHelp
	getHelp()
	{
		return( new CmdAliasesCmdHelp() );
	}
	
		
		
	private final static String	NAME						= "cmd-aliases";
	private final static String	CREATE_ALIAS_NAME			= "create-cmd-alias";
	private final static String	DELETE_ALIAS_NAME			= "delete-cmd-alias";
	private final static String	LIST_ALIASES_NAME			= "list-cmd-aliases";

	private final static CmdInfo	CREATE_ALIAS_INFO	=
		new CmdInfoImpl( CREATE_ALIAS_NAME, null, new OperandsInfoImpl( "<name>=<value>", 1 ) );
		
	private final static CmdInfo	DELETE_ALIAS_INFO	=
		new CmdInfoImpl( DELETE_ALIAS_NAME, null, new OperandsInfoImpl( "name", 1 ) );
		
	private final static CmdInfo	LIST_ALIASES_INFO	=
		new CmdInfoImpl( LIST_ALIASES_NAME, null, new OperandsInfoImpl( "[<prefix>[ <prefix>]*]", 0 ) );
		
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( CREATE_ALIAS_INFO, DELETE_ALIAS_INFO, LIST_ALIASES_INFO ) );
	}

		void
	handleCreateAlias()
		throws Exception
	{
		requireNumOperands( 1 );
		final String[]	operands	= getOperands();
		
		// first operand must contain the "name=" part
		// subsequent ones are interpreted as part of the alias
		final int		index	= operands[ 0 ].indexOf( '=' );
		if ( index <= 0 )
		{
			throw new IllegalUsageException( getSubCmdNameAsInvoked() );
		}

		final String	name	= operands[ 0 ].substring( 0, index );
		String			value	= operands[ 0 ].substring( index + 1, operands[ 0 ].length() );
		
		// a subsequent operands are part of the alias value, with white space between them
		for( int i = 1; i < operands.length; ++i )
		{
			value	= value + " " + operands[ i ];
		}
		
		getCmdAliasMgr().createAlias( name, value );
		println( name + "=" + value );
	}
	
	
		private void
	handleDeleteAlias()
		throws Exception
	{
		requireNumOperands( 1 );
		final String[]	operands	= getOperands();
		
		final CmdAliasMgr		mgr	= getCmdAliasMgr();
		
		for( int i = 0; i < operands.length; ++i )
		{
			final String	alias	= operands[ i ];
			
			if ( mgr.getAliasValue( alias ) != null )
			{
				mgr.deleteAlias( alias );
			}
		}
	}
	
	
		private void
	handleListAliases()
		throws Exception
	{
		final CmdAliasMgr		mgr	= getCmdAliasMgr();
		
		final String []	aliases		= mgr.getAliasNames( );
		Arrays.sort( aliases );
		final String[]	prefixes	= getOperands();
			
		// do one by one for control over output
		for( int i = 0; i < aliases.length; ++i )
		{
			String	alias	= aliases[ i ];
			
			boolean	doShow	= false;
			if ( prefixes.length == 0 )
			{
				doShow	= true;
			}
			else
			{
				for( int p = 0; p < prefixes.length; ++p )
				{
					if ( alias.startsWith( prefixes[ p ] ) )
					{
						doShow	= true;
						break;
					}
				}
			}
			
			if ( doShow )
			{
				println( alias + "=" + quote( mgr.getAliasValue( alias ) ));
			}
			
		}
	}
	
	
		protected void
	executeInternal()
		throws Exception
	{
		final String	cmd	= getSubCmdNameAsInvoked();
		
		if ( cmd.equals( CREATE_ALIAS_NAME ) )
		{
			handleCreateAlias();
		}
		else if ( cmd.equals( DELETE_ALIAS_NAME ) )
		{
			handleDeleteAlias();
		}
		else if ( cmd.equals( LIST_ALIASES_NAME ) )
		{
			handleListAliases();
		}
		else
		{
			throw new IllegalUsageException( cmd );
		}
	}
}
