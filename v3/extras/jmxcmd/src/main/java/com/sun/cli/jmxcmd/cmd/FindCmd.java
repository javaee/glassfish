/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/FindCmd.java,v 1.14 2005/11/15 20:59:53 llc Exp $
 * $Revision: 1.14 $
 * $Date: 2005/11/15 20:59:53 $
 */
 
package com.sun.cli.jmxcmd.cmd;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.Collections;

import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;
import javax.management.IntrospectionException;
import javax.management.InstanceNotFoundException;


import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;

import com.sun.cli.jcmd.framework.IllegalUsageException;
import org.glassfish.admin.amx.util.stringifier.IteratorStringifier;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.util.cmd.DisallowedOptionDependency;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;
import java.util.Collection;
import org.glassfish.admin.amx.util.ArrayConversion;
import org.glassfish.admin.amx.util.RegexUtil;



/*
	Find MBeans by name (alias, full or partial name).
 */
public class FindCmd extends JMXCmd
{
		public
	FindCmd( final CmdEnv env )
	{
		super( env );
	}

	

	static final class FindCmdHelp extends CmdHelpImpl
	{
		public	FindCmdHelp()	{ super( getCmdInfos() ); }
		
		
		private final static String	SYNOPSIS	= "search for MBeans matching ObjectName properties, Attribute names, operations, values or patterns";
		private final static String	FIND_TEXT		=
	"Finds mbeans within the set of MBeans denoted by the target(s).  All matching MBeans will be displayed " +
	"with their fully-qualified names. " +
	"The target '*' may be used to specify all MBeans.\n" +
	"\nExamples:\n" +
	"\nfind           -- displays all MBeans" +
	"\nfind :*        -- displays all MBeans in the default domain" +
	"\nfind type=test -- displays all MBeans whose ObjectName contains the property 'type' with value 'test'\n" +
	"\nfind --regex=n*=test* * -- displays all MBeans whose ObjectName contains a property whose name " +
	"starts with 'n' and whose value starts with 'test'\n" +
	"\nfind --operations=test* * -- displays all MBeans which have an operation name beginning with 'test'\n" +
	"\n\nOptions:\n" +
	CURRENT_OPTION.getLongName() + " -- display the results of the last find\n" +
	ADD_OPTION.getLongName()	 + " -- add the results of this invocation to the current set\n" +
	REMOVE_OPTION.getLongName()	 + " -- remove the results of this invocation from the current set\n" +
	WILD_PROP_OPTION.getLongName()	 + " -- utilize a (possibly wildcard) expression for MBean Property name(s) or value(s)\n" +
	REGEX_PROP_OPTION.getLongName()	 + " -- utilize a java.util.regex regular expression for MBean Property name(s) or value(s)\n" +
	OPERATIONS_OPTION.getLongName()	 + " -- comma-separated list of (possibly wildcarded) expression(s) for operation names\n" +
	ATTRIBUTES_OPTION.getLongName()	 + " -- comma-separated list of (possibly wildcarded) expression(s) for Attribute names\n" +
	TERSE_OPTION.getLongName()		 + " -- display only a summary of the results\n" +
	"\n\nAlthough the find command cannot form complex and/or/not constructions, you can utilize " +
	"the --add and --remove options to refine the set." +
	"\nThe 'find' command maintains an alias each time it runs called 'last-found'.  This alias may be used in any " +
	" place an alias is normally used and/or other aliases may refer to it." +
	"";
		
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( FIND_TEXT ); }
	}

		public CmdHelp
	getHelp()
	{
		return( new FindCmdHelp() );
	}
	
	final static String	NAME			= "find";


		public static String []
	getNames( )
	{
		return( new String [] { NAME } );
	}
	
	private final static OptionInfo CURRENT_OPTION		= new OptionInfoImpl( "current", "c" );
	private final static OptionInfo ADD_OPTION			= new OptionInfoImpl( "add", "a" );
	private final static OptionInfo REMOVE_OPTION		= new OptionInfoImpl( "remove", "r" );
	private final static OptionInfo WILD_PROP_OPTION	= new OptionInfoImpl( "wild-prop", "w", "expr");
	private final static OptionInfo REGEX_PROP_OPTION	= new OptionInfoImpl( "jregex-prop", "j", "expr");
	private final static OptionInfo ATTRIBUTES_OPTION	= new OptionInfoImpl( "attributes", "A", "attr-expr[,attr-expr]*");
	private final static OptionInfo OPERATIONS_OPTION	= new OptionInfoImpl( "operations", "o", "operation-expr[,operation-expr]*");
	private final static OptionInfo TERSE_OPTION		= new OptionInfoImpl( "terse", "t" );
	
	static private final OptionInfo[]	OPTION_INFOS	=
	{
		CURRENT_OPTION,
		ADD_OPTION,
		REMOVE_OPTION,
		WILD_PROP_OPTION,
		REGEX_PROP_OPTION,
		ATTRIBUTES_OPTION,
		OPERATIONS_OPTION,
		TERSE_OPTION,
	};
		
	
	static
	{
		try
		{
			WILD_PROP_OPTION.addDependency( new DisallowedOptionDependency( REGEX_PROP_OPTION ) );
			
			ADD_OPTION.addDependency( new DisallowedOptionDependency( REMOVE_OPTION ) );
			
			CURRENT_OPTION.addDependency( new DisallowedOptionDependency(
				ADD_OPTION, REMOVE_OPTION, WILD_PROP_OPTION, REGEX_PROP_OPTION ) );
		}
		catch( Exception e )
		{
			assert( false );
		}
	}
	
	private final static CmdInfo	FIND_INFO	=
		new CmdInfoImpl( NAME,
		new OptionsInfoImpl( OPTION_INFOS ),
		TARGETS_OPERAND_INFO );
		
		
		public static CmdInfos
	getCmdInfos(  )
	{
		return( new CmdInfos( FIND_INFO ) );
	}
	
	
		void
	display( final Set<String>	names )
	{
		final Object[]	all	= ArrayConversion.setToArray( names );

		Arrays.sort( all );
		
		println( ArrayStringifier.stringify( all, "\n" ) );
	}
	
		Set<String>
	getCurrent()
	{
        @SuppressWarnings("unchecked")
        final Set<String> s = (Set)envGet( JMXCmdEnvKeys.FIND_RESULT );
        return s;
	}
	
	private final static String	ARG_DELIM	= ",";
		private String[]
	argListToExprs( final String argList )
	{
		return( argList.split( ARG_DELIM ) );
	}
	
		private boolean
	matchesPattern( final Pattern[] patterns, String candidate )
	{
		boolean	matches	= false;
		
		for( int i = 0; i < patterns.length; ++i )
		{
			if ( patterns[ i ].matcher( candidate ).matches() )
			{
				matches	= true;
				break;
			}
		}
		return( matches );
	}


		private boolean
	matches( final Pattern[] patterns, final MBeanFeatureInfo[]	infos )
	{
		boolean	matches	= false;
		
		for( int i = 0; i < infos.length; ++i )
		{
			if ( matchesPattern( patterns, infos[ i ].getName() ) )
			{
				matches	= true;
				break;
			}
		}
		return( matches );
	}
	
		private Set<ObjectName>
	filterForOperations( final Set<ObjectName> objectNames, final String argList )
		throws java.io.IOException, ReflectionException, IntrospectionException,InstanceNotFoundException
	{
		final MBeanServerConnection	conn	= getConnection();
		final Set<ObjectName>	resultSet		= new HashSet<ObjectName>();
		final Pattern[]	patterns		= RegexUtil.exprsToPatterns( argListToExprs( argList ) );
		
		for( final ObjectName objectName : objectNames )
		{
			printDebug( "filterForOperations: " + objectName);
			
			final MBeanInfo				mbeanInfo 		= conn.getMBeanInfo( objectName );
			final MBeanOperationInfo[]	operationInfos	= mbeanInfo.getOperations();
			
			if ( matches( patterns, operationInfos ) )
			{
				resultSet.add( objectName );
			}
		}
		
		return( resultSet );
	}

	
		private Set<ObjectName>
	filterForAttributes( final Set<ObjectName> objectNames, final String argList )
		throws java.io.IOException, ReflectionException, IntrospectionException,InstanceNotFoundException
	{
		final MBeanServerConnection	conn	= getConnection();
		final Set<ObjectName>	resultSet		= new HashSet<ObjectName>();
		final Pattern[]	patterns		= RegexUtil.exprsToPatterns( argListToExprs( argList ) );
		
		for( final ObjectName objectName : objectNames )
		{
			printDebug( "filterForAttributes: " + objectName);
			
			final MBeanInfo				mbeanInfo 		= conn.getMBeanInfo( objectName );
			final MBeanAttributeInfo[]	attributeInfos	= mbeanInfo.getAttributes();
			
			if ( matches( patterns, attributeInfos ) )
			{
				resultSet.add( objectName );
			}
		}
		
		return( resultSet );
	}
	
	static final char	ADD_CHAR	= '+';
	static final char	REMOVE_CHAR	= '-';
	static final char	AND_CHAR	= '&';
	static final String	QUALIFIERS	= "" + ADD_CHAR + REMOVE_CHAR + AND_CHAR;
	
	/**
	 	<p>
	 	Resolve the targets, taking into account meta characters controlling how the set
	 	is formed.
	 	<p>
	 	Several special constructs may be used to manipulate the results:
	 	ADD_CHAR	the MBeans are added to the set so far
	 	REMOVE_CHAR	the MBeans are removed from the set so far
	 	AND_CHAR	only MBeans already in both the set and this target are kept
	 	<p>
	 	<p>
	 	Examples:
	 	*			all MBeans
	 	-*		no MBeans
	 	* -:* +Test:*	all MBeans, then remove all MBeans in default domain, then add all MBeans in 'Test' domain
	*/
		Set<ObjectName>
	resolveQualifiedTargets( final String[] qualifiedTargets )
		throws Exception
	{
		final char[]	qualifiers	= new char[ qualifiedTargets.length ];
		final String[]	targets		= new String[ qualifiedTargets.length ];
		
		for( int i = 0; i < qualifiedTargets.length; ++i )
		{
			final String	qualifiedTarget	= qualifiedTargets[ i ];
			
			final char	firstChar	= qualifiedTarget.charAt( 0 );
			if ( QUALIFIERS.indexOf( firstChar ) >= 0 )
			{
				qualifiers[ i ]	= firstChar;
				targets[ i ]	= qualifiedTarget.substring( 1, qualifiedTarget.length() );
			}
			else
			{
				qualifiers[ i ]	= 0;
				targets[ i ]	= qualifiedTarget;
			}
		}
		
		final Set<ObjectName>	all	= new HashSet<ObjectName>();
		
		// now we have targets without qualification--resolve them
		final CLISupportMBeanProxy	proxy	= getProxy();
		final String[]	arrayOfOne	= new String[ 1 ];
		for( int i = 0; i < targets.length; ++i )
		{
			final String	target	= targets[ i ];
			
			arrayOfOne[ 0 ]	= target;
			
			final ObjectName[]	objectNames	= resolveTargets( proxy, arrayOfOne );
			
			final Set<ObjectName>	objectNameSet	= ArrayConversion.arrayToSet( objectNames );
			switch( qualifiers[ i ] )
			{
				default:
				case ADD_CHAR:
					all.addAll( objectNameSet );
					break;

				case REMOVE_CHAR:
					all.removeAll( objectNameSet );
					break;
					
				case AND_CHAR:
					all.retainAll( objectNameSet );
					break;
			}
		}
		
		return( all );
	}
    
    
		public static List<String>
	objectNamesToStrings( final Collection<ObjectName> objectNames )
	{
		// sorting doesn't work on returned array, so convert to Strings first,then sort
		final List<String>	result	= new ArrayList<String>();
		
		for( final ObjectName objectName : objectNames )
		{
			result.add( "" + objectName );
		}
		
		return( result );
	}


		protected void
	executeInternal()
		throws Exception
	{
		final String	cmd	= getSubCmdNameAsInvoked();
		
		String [] targets	= getOperands();
		
		final boolean	add				= getBoolean( ADD_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
		final boolean	remove			= getBoolean( REMOVE_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
		final boolean	displayCurrent	= getBoolean( CURRENT_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
		final boolean	terse			= getBoolean( TERSE_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
		final String	wildPropExpr	= getString( WILD_PROP_OPTION.getShortName(), null );
		final String	regexPropExpr	= getString( REGEX_PROP_OPTION.getShortName(), null );
		final String	attrsExpr		= getString( ATTRIBUTES_OPTION.getShortName(), null );
		final String	operationsExpr	= getString( OPERATIONS_OPTION.getShortName(), null );
		
		Set<String> currentSet	= getCurrent();
		if ( currentSet == null )
		{
			currentSet	= new HashSet<String>();
		}
		
		if ( add || remove || wildPropExpr != null || regexPropExpr != null )
		{
			requireNumOperands( 1 );
		}
		
		if ( targets.length == 0 || displayCurrent )
		{
			if ( displayCurrent && getOperands().length != 0 )
			{
				throw new IllegalUsageException( cmd,
					CURRENT_OPTION.getLongName() + " option may not specify any operands" );
			}
			
			if ( terse )
			{
				println( "find: " + currentSet.size() );
			}
			else
			{
				if ( currentSet.size() == 0 )
				{
					println( "Nothing in current set." );
				}
				else
				{
					display( currentSet  );
				}
			}
		}
		else
		{
			establishProxy();
			
			final Set<ObjectName> objectNames = resolveQualifiedTargets( targets );
			final List<String> objectNameStrings	= objectNamesToStrings( objectNames );
			final String[]	nameStrings			= (String[])
								objectNameStrings.toArray( new String[ objectNameStrings.size() ] );
			
			final String	actualRegex	= (regexPropExpr != null) ?
								regexPropExpr : RegexUtil.wildcardToJavaRegex( wildPropExpr );
			final ObjectName[]	namesToFilter	= getProxy().mbeanFind( nameStrings, actualRegex);
			final Set<ObjectName> initialSet		= ArrayConversion.arrayToSet( namesToFilter );
			
			Set<ObjectName>	filteredSet	= new HashSet<ObjectName>();
			if ( operationsExpr != null || attrsExpr != null )
			{
				Set<ObjectName>	operationsSet	= Collections.emptySet();
				Set<ObjectName>	attrsSet		= Collections.emptySet();
				
				if ( operationsExpr != null )
				{
					operationsSet	= filterForOperations( initialSet, operationsExpr );
				}
				if ( attrsExpr != null )
				{
					attrsSet	= filterForAttributes( initialSet, attrsExpr );
				}
				
				// the resulting set is the logical OR of operations and attribute sets
				
				filteredSet.addAll( operationsSet );
				filteredSet.addAll( attrsSet );
			}
			else
			{
				filteredSet	= initialSet;
			}
			
			Set<String>	resultSet	= new HashSet<String>( objectNamesToStrings( filteredSet ) );
			
			if ( add )
			{
				currentSet.addAll( resultSet );
				resultSet	= currentSet;
			}
			else if ( remove )
			{
				currentSet.removeAll( resultSet );
				resultSet	= currentSet;
			}
			
			envPut( JMXCmdEnvKeys.FIND_RESULT, resultSet, false );
			
			// maintain an alias for the result of find command
			final String	aliasValue	= IteratorStringifier.stringify( resultSet.iterator(), ALIAS_VALUE_DELIM );
			getAliasMgr().deleteAlias( JMXCmdEnvKeys.LAST_FOUND_ALIAS );
			getAliasMgr().createAlias( JMXCmdEnvKeys.LAST_FOUND_ALIAS, aliasValue );
			assert( getAliasMgr().getAliasValue( JMXCmdEnvKeys.LAST_FOUND_ALIAS ).equals( aliasValue ) );
			
			if ( terse )
			{
				println( "find: " + resultSet.size() );
			}
			else
			{
				if ( resultSet.size() == 0 )
				{
					if ( remove )
					{
						println( "Current set is now empty." );
					}
					else
					{
						println( "No objects match the targets " + SmartStringifier.toString( targets ) );
					}
				}
				else
				{
					display( resultSet );
				}
			}
		}
	}
}





