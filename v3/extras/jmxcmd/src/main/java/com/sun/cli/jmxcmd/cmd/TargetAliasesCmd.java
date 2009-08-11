/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/TargetAliasesCmd.java,v 1.5 2004/04/25 07:14:09 llc Exp $
 * $Revision: 1.5 $
 * $Date: 2004/04/25 07:14:09 $
 */
 
package com.sun.cli.jmxcmd.cmd;

import javax.management.ObjectName;

import com.sun.cli.jmxcmd.support.ResultsForGetSet;
import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
import org.glassfish.admin.amx.util.stringifier.*;
import org.glassfish.admin.amx.util.ExceptionUtil;
import com.sun.cli.jcmd.util.cmd.ArgHelper;
import com.sun.cli.jcmd.util.cmd.IllegalOptionException;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.framework.WrongNumberOfOperandsException;
import com.sun.cli.jcmd.framework.IllegalUsageException;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import com.sun.cli.jcmd.util.cmd.OperandsInfo;
import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;



	
/**
	Creates an alias.
 */
public class TargetAliasesCmd extends JMXCmd
{
		public
	TargetAliasesCmd( final CmdEnv env )
	{
		super( env );
	}	
	
	static final class TargetAliasesCmdHelp extends CmdHelpImpl
	{
		public	TargetAliasesCmdHelp()	{ super( getCmdInfos() ); }
		
		// alias commands
		private final static String	SYNOPSIS	= "manage persistent aliases for an MBean name or pattern";
		
		private static final String	INDENT	= "    ";
		private final static String	TEXT		=
	CREATE_ALIAS_NAME +
	" -- creates an alias for the specified value.  No interpretation is given to the value supplied when creating an alias.  However, " +
	"an alias must ultimately resolve to an ObjectName or ObjectName pattern.  Aliases may contain other aliases in any combination " +
	"with ObjectNames.  The only restriction is that the space character is reserved as a delimiter. Therefore, ObjectNames " +
	"containing spaces may not be aliased.\n\n" +
	"Once created, an alias may be used anywhere an ObjectName may be used. Aliases are persisted in the file system local to where " +
	"the CLISupportMBean is running.\n\n" +
	"The following options are available:\n" +
	"--replace | -k  if the alias already exists, replace it without warning\n" +
	"--resolve       recursively evaluate operands that are aliases so that the resulting value(s) are all MBeans\n" +
	"\n\n" +
	"create-alias Examples: \n" +
	INDENT + CREATE_ALIAS_NAME + " test=:type=test,name=TestStandard\n" +
	INDENT + CREATE_ALIAS_NAME + " system=system:*\n" +
	INDENT + CREATE_ALIAS_NAME + " all-test=*:type=test\n" +
	"\n\n" +
	DELETE_ALIAS_NAME +
	" -- Deletes an existing alias or aliases.\n" +
	"\n\n" +
	RESOLVE_ALIAS_NAME + 
	" -- the alias name(s) are shown with their currently resolved values (ObjectNames)\n" +
	"\n\n" +
	SHOW_ALIAS_NAME + 
	" -- the alias name(s) are shown with their String values.  No evaluation is performed\n" +
	"\n\n" +
	LIST_ALIASES_NAME +
	" -- aliases are listed, along with their values.  Use the " + TRUNCATE_OPTION.getLongName() +
	" option to limit the output to no more than one line per alias. To list all aliases prefixed " +
	"with one or more prefixes, specify the prefixes as operands.\n" +
	"\n\n" +
	"";
		
		public String	getName()		{	return( NAME ); }
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( TEXT ); }
	}

		public CmdHelp
	getHelp()
	{
		return( new TargetAliasesCmdHelp() );
	}
	
		
		
	private final static String	NAME						= "target-aliases";
	private final static String	CREATE_ALIAS_NAME			= "create-target-alias";
	private final static String	DELETE_ALIAS_NAME			= "delete-target-alias";
	private final static String	RESOLVE_ALIAS_NAME			= "resolve-target-alias";
	public final static String	SHOW_ALIAS_NAME				= "show-target-alias";
	private final static String	LIST_ALIASES_NAME			= "list-target-aliases";

		public static String []
	getNames( )
	{
		return( new String []
		{
			CREATE_ALIAS_NAME,
			DELETE_ALIAS_NAME,
			RESOLVE_ALIAS_NAME,
			SHOW_ALIAS_NAME,
			LIST_ALIASES_NAME,
		} );
	}

	
	private final static OptionInfo TRUNCATE_OPTION	= new OptionInfoImpl( "truncate", "t" );
	
	private final static OptionInfo REPLACE_OPTION	= new OptionInfoImpl( "replace", "k" );
	private final static OptionInfo RESOLVE_OPTION	= new OptionInfoImpl( "resolve", "r" );
	
	static private final OptionInfo[]	CREATE_OPTIONS_INFO	= { REPLACE_OPTION, RESOLVE_OPTION};
	
	private final static OperandsInfo	PREFIX_OPERANDS =
		new OperandsInfoImpl( "[prefix[ prefix]*", 0 );
		
	private final static CmdInfo	CREATE_ALIAS_INFO	=
		new CmdInfoImpl( CREATE_ALIAS_NAME,
				new OptionsInfoImpl( CREATE_OPTIONS_INFO ),
				new OperandsInfoImpl( "<name>=<value>[ <name>=<value>]*" ) );
		
	private final static CmdInfo	DELETE_ALIAS_INFO	=
		new CmdInfoImpl( DELETE_ALIAS_NAME, new OperandsInfoImpl( "name", 1 ) );
		
	private final static CmdInfo	RESOLVE_ALIAS_INFO	=
		new CmdInfoImpl( RESOLVE_ALIAS_NAME, PREFIX_OPERANDS );
		
	private final static CmdInfo	SHOW_ALIAS_INFO	=
		new CmdInfoImpl( SHOW_ALIAS_NAME, PREFIX_OPERANDS );
		
	private final static CmdInfo	LIST_ALIASES_INFO	=
		new CmdInfoImpl( LIST_ALIASES_NAME,
				new OptionsInfoImpl( new OptionInfo[] { TRUNCATE_OPTION }),
				PREFIX_OPERANDS );
	
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( CREATE_ALIAS_INFO,
			DELETE_ALIAS_INFO, RESOLVE_ALIAS_INFO, SHOW_ALIAS_INFO, LIST_ALIASES_INFO) );
	}


	
		private void
	aliasCreationFailed( String name, Exception e)
		throws Exception
	{
		final String value	= getAliasMgr().getAliasValue( name );
		
		if ( value != null )
		{
			printError( "Failed to create alias: " + name + " (already exists with value " + value + ")" );
			printError( "If you want to change it, delete it first or specify " +
				REPLACE_OPTION.getLongName());
		}
		else if ( ExceptionUtil.getRootCause( e ) instanceof IllegalArgumentException )
		{
			printError( "Illegal alias name: " + name );
		}
		else
		{
			printError( "Failed to create alias: " + name );
		}
	}
		
	
		public static String[]
	aliasValueToComponents( String value )
	{
		String[]	components	= null;
		
		if ( value.indexOf( ALIAS_VALUE_DELIM ) >= 0 )
		{
			components	= value.trim().split( ALIAS_VALUE_DELIM );
		}
		else
		{
			components	= new String[] { value };
		}
		
				
		return( components );
	}
	

		void
	handleCreateAlias()
		throws Exception
	{
		requireNumOperands( 1 );
		final String [] pairs	= getOperands();
		
		final boolean	replace	= getBoolean( REPLACE_OPTION.getShortName(), Boolean.FALSE).booleanValue();
		final boolean	resolve	= getBoolean( RESOLVE_OPTION.getShortName(), Boolean.FALSE).booleanValue();
		
		for( int i = 0; i < pairs.length; ++i )
		{
			final String	pair	= pairs[ i ];
			final int		separatorIndex	= pair.indexOf( '=' );
			
			if ( separatorIndex < 0 )
			{
				printError( "Alias request must be of form name=value: " + pair );
				continue;
			}
			
			final String	name	= pair.substring( 0,separatorIndex);
			final String	value	= pair.substring( separatorIndex + 1, pair.length() );
			
			try
			{
				
				String	newValue	= value;
				if ( resolve )
				{
					final ObjectName[]	objectNames	=
						resolveTargets( getProxy(),  aliasValueToComponents( value ) );
					if ( objectNames.length != 0 )
					{
						newValue	= ArrayStringifier.stringify( objectNames, "\n" );
					}
					else
					{
						newValue	= "";
					}
				}
				
				if ( replace )
				{
					getAliasMgr().deleteAlias( name );
				}
				getAliasMgr().createAlias( name, newValue );
				println( "Created alias: " + name + " resolves to:\n" + newValue);
			}
			catch( Exception e )
			{
				aliasCreationFailed( name, e);
				throw e;
			}
		}
	}
	
	
		private void
	handleDeleteAlias()
		throws Exception
	{
		requireNumOperands( 1 );
		
		final String [] aliases	= getOperands();
		
		for( int i = 0; i < aliases.length; ++i )
		{
			final String	name	= aliases[ i ];
			
			if ( getAliasMgr().getAliasValue( name ) == null )
			{
				println( "alias does not exist: " + name );
			}
			else
			{
				getAliasMgr().deleteAlias(  name );
				println( "deleted alias: " + name );
			}
		}
	}
	
		private void
	handleResolveAlias()
		throws Exception
	{
		requireNumOperands( 1 );
		
		final String	cmdName	= getSubCmdNameAsInvoked();
		final String [] aliases	= getOperands();
		
		final boolean	isResolve	= cmdName.equals( RESOLVE_ALIAS_NAME );
		
		if ( isResolve )
		{
			establishProxy();
		}
		
		final String [] values	= new String[ aliases.length ];
	
		for( int i = 0; i < aliases.length; ++i )
		{
			final String	name	= aliases[ i ];
			final String	value	= getAliasMgr().getAliasValue(  name );
			
			final String	summary	= name + " = " + value;
			
			if ( value == null )
			{
				println( "Alias does not exist: " + name );
			}
			else if ( isResolve  )
			{
				println( summary + " resolves to: " );
				
				final ObjectName[]	objectNames	=
					resolveTargets( getProxy(), aliasValueToComponents( value ) );
				if ( objectNames.length != 0 )
				{
					println( ArrayStringifier.stringify( objectNames, "\n" ) );
				}
				else
				{
					println( "<nothing>" );
				}
				
				if ( aliases.length > 1 )
				{
					println( "\n" );	// emit one blank line between
				}
			}
			else
			{
				println( summary );
			}
		}
	}
	
	
	
	static final String	CONTINUES	= "...";
	static final int	MAX_LENGTH	= 77 - CONTINUES.length();
		private void
	handleListAliases()
		throws Exception
	{
		final String []	aliases		= getAliasMgr().listAliases( true );
		final String[]	prefixes	= getOperands();
		
		final boolean	truncate	=
			getBoolean( TRUNCATE_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
			
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
				if ( truncate && alias.length() > MAX_LENGTH )
				{
					alias	= alias.substring( 0, MAX_LENGTH ) + CONTINUES;
				}
				
				println( alias );
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
		else if ( cmd.equals( DELETE_ALIAS_NAME )  )
		{
			handleDeleteAlias();
		}
		else if ( cmd.equals( RESOLVE_ALIAS_NAME ) ||
			 cmd.equals( SHOW_ALIAS_NAME ))
		{
			handleResolveAlias();
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