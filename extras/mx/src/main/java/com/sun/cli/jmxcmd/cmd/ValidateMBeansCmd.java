/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/cmd/ValidateMBeansCmd.java,v 1.17 2005/11/15 20:59:53 llc Exp $
 * $Revision: 1.17 $
 * $Date: 2005/11/15 20:59:53 $
 */
 
package com.sun.cli.jmxcmd.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.io.IOException;
import java.io.NotSerializableException;


import javax.management.ObjectName;
import javax.management.MBeanServerConnection;
import javax.management.MBeanOperationInfo;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.AttributeList;

import com.sun.cli.jcmd.util.cmd.IllegalOptionException;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionInfoImpl;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdHelpImpl;


import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;



import org.glassfish.admin.amx.util.jmx.JMXUtil;
import com.sun.cli.jmxcmd.util.ObjectNameComparator;
import org.glassfish.admin.amx.util.ArrayConversion;
import org.glassfish.admin.amx.util.ArrayUtil;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.core.AMXValidator;



/**
	Validates the correctness of MBeans.
 */
public class ValidateMBeansCmd extends JMXCmd
{
	boolean		mDoInfo;
	boolean		mDoAttributes;
	boolean		mDoOperations;
	boolean		mPrintWarnings;
	
		public
	ValidateMBeansCmd( final CmdEnv env )
	{
		super( env );
		mDoInfo				= false;
		mDoAttributes		= false;
		mDoOperations		= false;
		mPrintWarnings		= true;
	}
	
	

	static final class ValidateMBeansCmdHelp extends CmdHelpImpl
	{
		private final static String	DELIM	= " ";
		
		private final static String	SYNOPSIS	= "checks MBeans for valid MBeanInfo";
			
		private final static String	TEXT		=
		"Checks that MBeans can supply all their advertised attributes; " +
		"some servers may contain MBeans that have non-serializable Attributes, " +
		"fail when Attributes are requested, etc";

		public	ValidateMBeansCmdHelp()	{ super( getCmdInfos() ); }
		
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( TEXT ); }
	}
	
		public CmdHelp
	getHelp()
	{
		return( new ValidateMBeansCmdHelp() );
	}
	
	private final static OptionInfo PRINT_STACK_TRACES_OPTION	= new OptionInfoImpl( "print-stack-traces", "s" );
	private final static OptionInfo VERBOSE_OPTION				= createVerboseOption();
	private final static OptionInfo WARNINGS_OFF_OPTION			= new OptionInfoImpl( "warnings-off", "w" );
	private final static OptionInfo ATTRIBUTES_OPTION			= new OptionInfoImpl( "attributes", "a" );
	private final static OptionInfo OPERATIONS_OPTION			= new OptionInfoImpl( "operations", "o" );
	private final static OptionInfo MBEAN_INFO_OPTION			= new OptionInfoImpl( "mbean-info", "m" );
	
	private static final OptionInfo[]	OPTIONS_INFO_ARRAY	=
	{
		VERBOSE_OPTION,
		PRINT_STACK_TRACES_OPTION,
		ATTRIBUTES_OPTION,
		OPERATIONS_OPTION,
		MBEAN_INFO_OPTION,
		WARNINGS_OFF_OPTION,
	};
	
	final static String	NAME		= "validate-mbeans";



	private final static CmdInfo	VALIDATE_INFO	=
		new CmdInfoImpl( NAME, new OptionsInfoImpl( OPTIONS_INFO_ARRAY ), TARGETS_OPERAND_INFO );
		
		
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( VALIDATE_INFO ) );
	}
	
	
		private Map<String,Exception>
	validateAttributesSingly(
		final ObjectName		objectName,
		final String[]			attrNames,
		Map<String,Exception>	failures,
		Map<String,Exception>	warnings )
		throws Exception
	{
		for( int i = 0; i < attrNames.length; ++i )
		{
			establishProxy();
			MBeanServerConnection conn	= getConnection();
			
			final String	attrName	= attrNames[ i ];
			
			try
			{
				final Object	a	= conn.getAttribute( objectName, attrName );
				
				if ( a == null )
				{
					// null is legal, apparently
				}
			}
			catch( NotSerializableException e )
			{
				warnings.put( attrName, e );
			}
			catch( IOException e )
			{
				connectionIOException( e, true);
				failures.put( attrName, e );
			}
			catch( Exception e )
			{
				failures.put( attrName, e );
			}
		}
		
		return( failures );
	}


		private String
	getExceptionMsg( Exception e )
	{
		String	msg	= null;
		
		if ( e instanceof IOException )
		{
			msg	= "received an exception of class " + e.getClass().getName();
			
			if ( shouldPrintStackTraces() )
			{
				msg	= msg + "Stack trace = \n" + 
				ExceptionUtil.getStackTrace( ExceptionUtil.getRootCause( e ) );
			}
		}
		else
		{
			msg	= "threw an Exception of type " +
				e.getClass().getName() + ", message =  " + e.getMessage();
			
			if ( shouldPrintStackTraces() )
			{
				msg	= msg + "\n" + ExceptionUtil.getStackTrace( e );
			}
		}
		
		final Throwable rootCause	= ExceptionUtil.getRootCause( e );
		
		if ( rootCause != e )
		{
			msg	= msg + "...\nRoot cause was exception of type " + e.getClass().getName() + ", message = " +
				rootCause.getMessage();
				
			
			if ( shouldPrintStackTraces() )
			{
				msg	= msg + "\n" + ExceptionUtil.getStackTrace( rootCause );
			}
		}
		
		return( msg );
	}

		MBeanAttributeInfo
	findAttributeInfo(
		final MBeanAttributeInfo[]	infos,
		String 						attrName )
	{
		MBeanAttributeInfo	info	= null;
		
		for( int i = 0; i < infos.length; ++i )
		{
			if ( infos[ i ] != null && infos[ i ].getName().equals( attrName ) )
			{
				info	= infos[ i ];
				break;
			}
		}
		
		assert( info != null );
		return( info );
	}
	
		private void
	displayAttributeFailuresOrWarnings(
		final boolean				failure,
		final ObjectName			objectName,
		final MBeanAttributeInfo[]	infos,
		final Map					problems )
		throws Exception
	{
		final Iterator	iter	= problems.keySet().iterator();
		
		println( "" );
		println( problems.keySet().size() + (failure ? " Failures: " : " Warnings: ") + objectName  );
		
		int	i = 0;
		while ( iter.hasNext() )
		{
			final String	attrName	= (String)iter.next();
			final Object	value		= problems.get( attrName );
			
			final MBeanAttributeInfo	info	= findAttributeInfo( infos, attrName );
			
			final String prefix	= "(" + (i+1) + ")" + " getting Attribute \"" + attrName + "\" of type " +
				info.getType() + " ";
				
			if ( value == null )
			{
				println( prefix + "returned null" );
			}
			else
			{
				final Exception e	= (Exception)value;
				
				println( prefix + getExceptionMsg( e ) );
			}
			++i;
		}
	}
	
	
		void
	printFailure( String msg  )
	{
		printError( "Failure: " + msg );
	}
	
		void
	printWarning( String msg  )
	{
		if ( mPrintWarnings )
		{
			println( "Warning: " + msg );
		}
	}
	
	
		private boolean
	validateMBeanInfo( final ObjectName objectName, final MBeanInfo info)
	{
		boolean	valid	= true;
		
		if ( ArrayUtil.arrayContainsNulls( info.getAttributes() ) )
		{
			printFailure( "MBean has nulls in its MBeanAttributeInfo[]: " + objectName );
			valid	= false;
		}
		
		if ( ArrayUtil.arrayContainsNulls( info.getConstructors() ) )
		{
			printFailure( "MBean has nulls in its MBeanConstructorInfo[]: " + objectName );
			valid	= false;
		}
		
		if ( ArrayUtil.arrayContainsNulls( info.getOperations() ) )
		{
			printFailure( "MBean has nulls in its MBeanOperationInfo[]: " + objectName );
			valid	= false;
		}
		
		if ( ArrayUtil.arrayContainsNulls( info.getNotifications() ) )
		{
			printFailure( "MBean has nulls in its MBeanNotificationInfo[]: " + objectName );
			valid	= false;
		}
		
		return( valid );
	}
	
	static final private String	SECTION_LINE	=
	"--------------------------------------------------------------------------------";
	
	
	
		private void
	printDuplicateAttributes( final ObjectName objectName, MBeanAttributeInfo[] attrInfos, String name)
	{
		String	msg	= "MBean " + quote( objectName ) + " has the same Attribute listed more than once:\n";
		
		for( int i = 0; i < attrInfos.length; ++i )
		{
			final MBeanAttributeInfo	a	= attrInfos[ i ];
				
			if ( a.getName().equals( name ) )
			{
				msg	= msg + name + ": " + a.getType() + ", " + quote( a.getDescription() );
			}
		}
		
		printFailure( msg );
	}
	
		private boolean
	validateUniqueAttributeNames( final ObjectName objectName, MBeanAttributeInfo[] attrInfos )
	{
		boolean	valid	= true;
		final String[]	names	= JMXUtil.getAttributeNames( attrInfos );
		
		if ( ArrayConversion.arrayToSet( names ).size() != attrInfos.length )
		{
			final HashSet<String>	set		= new HashSet<String>();
			
			for( int i = 0; i < names.length; ++i )
			{
				final String	name	= names[ i ];
			
				if ( set.contains( name ) )
				{
					valid	= false;
					
					printDuplicateAttributes( objectName, attrInfos, name );
				}
				else
				{
					set.add( name );
				}
			}
			set.clear();
		}
		
		return( valid );
	}
	
		private boolean
	validateMissingAndEmptyAttributeNames( final ObjectName objectName  )
		throws IOException
	{
		boolean						valid	= true;
		final MBeanServerConnection	conn	= getConnection();
		
		AttributeList	attrs	= null;
		try
		{
			attrs	= conn.getAttributes( objectName, new String[0] );
			if ( attrs == null )
			{
				printFailure( "MBean " + quote( objectName ) +
					" returned NULL for an empty AttributeList" );
				valid	= false;
			}
			else if ( attrs.size() != 0 )
			{
				printFailure( "MBean " + quote( objectName ) +
					" returned attributes for an empty AttributeList" );
				valid	= false;
			}
		}
		catch( Exception e )
		{
			valid	= false;
			
			printFailure( "MBean " + quote( objectName ) +
				" threw an exception getting an empty attribute list" );
		}
		
		try
		{
			final String	notFoundName	= "bogus." + System.currentTimeMillis();
			attrs	= conn.getAttributes( objectName, new String[] { notFoundName });
			if ( attrs == null )
			{
				printFailure( "MBean " + quote( objectName ) +
					" returned NULL for (deliberately) non-existent attribute "  + StringUtil.quote(notFoundName));
				valid	= false;
			}
			else if ( attrs.size() != 0 )
			{
				printFailure( "MBean " + quote( objectName ) +
					" returned attributes " + attrs + 
                    " for (deliberately) non-existent attribute " + StringUtil.quote(notFoundName) );
				valid	= false;
			}
		}
		catch( Exception e )
		{
			valid	= false;
			
			printFailure( "MBean " + quote( objectName ) +
				" threw an exception when getAttributes() was called with a " +
				"non-existent Attribute, exception class = " +
				e.getClass().getName() );
		}
		
		return( valid );
	}
	
		private boolean
	validateAttributes( final ObjectName objectName, final MBeanAttributeInfo[] attrInfos)
		throws Exception
	{
	
		boolean	valid	= true;
		
		final String[]	attrNames	= JMXUtil.getAttributeNames( attrInfos );
		Arrays.sort( attrNames  );
		
		if ( attrNames.length != 0 )
		{
			// if we can fetch all the attributes, then the MBean is OK;
			// try this first for efficiency
			try
			{
				final AttributeList	attrs	= getConnection().getAttributes( objectName, attrNames );
				
				if ( attrs == null )
				{
					printFailure( "MBean " + quote( objectName ) + " returned NULL for its AttributeList" );
					valid	= false;
				}
				else if ( attrs.size() != attrInfos.length )
				{
					// mismatch between claimed number of attributes and actual
					final ArrayStringifier	as	= new ArrayStringifier( ", ", true );
					final String			claimedString	= as.stringify( attrNames );
					
					final Set<String> actualSet	= JMXUtil.attributeListToValueMap( attrs ).keySet();
					final Set<String> missingSet	= SetUtil.newStringSet( attrNames );
					missingSet.removeAll( actualSet );
					
					final String[]	missingNames	= (String[])ArrayConversion.setToArray( missingSet, true );
					Arrays.sort( missingNames  );
					final String	missingString	= as.stringify( missingNames );
					
					printWarning( "MBean " + quote( objectName ) + " claims it has the " +
						attrNames.length + " attributes " + claimedString +
						", but it did not supply the " + missingNames.length + " attributes " + missingString );
				}
			}
			catch( Exception e )
			{
				println( SECTION_LINE );
				final String	msg	= "getAttributes() failed on " + quote( objectName ) + ", exception =\n" + e;
				
				if ( e instanceof NotSerializableException )
				{
					printWarning( msg );
				}
				else
				{
					printFailure( msg );
					valid	= false;
				}
				// do them one-at-a time to see where failure occurs
				final Map<String,Exception>	failures	= new HashMap<String,Exception>();
				final Map<String,Exception>	warnings	= new HashMap<String,Exception>();
				
				validateAttributesSingly( objectName, attrNames, failures, warnings );
				
				println( "Validating attributes one-at-a-time using getAttribute() for " + quote( objectName ));
				if ( failures.size() == 0 && warnings.size() == 0 )
				{
					printFailure( " during getAttributes(" +
						ArrayStringifier.stringify( attrNames, "," ) + ") for: " + objectName +
						" (but Attributes work when queried one-at-a-time).\nIt " +
						getExceptionMsg( e ) );
				}
				
				if ( failures.size() != 0 )
				{
					displayAttributeFailuresOrWarnings( true, objectName, attrInfos, failures );
				}
				
				if ( warnings.size() != 0 )
				{
					displayAttributeFailuresOrWarnings( false, objectName, attrInfos, warnings );
				}
				
				println( SECTION_LINE );
			}
		}
		else
		{
			valid	= true;
		}
		
		if ( ! validateUniqueAttributeNames( objectName, attrInfos ) )
		{
			valid	= false;
		}
		
		if ( ! validateMissingAndEmptyAttributeNames( objectName ) )
		{
			valid	= false;
		}
		
		return( valid );
	}
	
		private boolean
	isGetter( MBeanOperationInfo info )
	{
		return ( info.getName().startsWith( "get" ) &&
				info.getSignature().length == 0 &&
				! info.getReturnType().equals( "void" ) );
	}

	
		void
	checkObjectNameReturnValue(
		MBeanServerConnection	conn,
		ObjectName				callee,
		MBeanOperationInfo		operationInfo,
		ObjectName				resultOfCall )
		throws Exception
	{
		try
		{
			printDebug( "checking MBean info for: " + resultOfCall );
			final MBeanInfo	mbeanInfo	= conn.getMBeanInfo( resultOfCall );
		}
		catch( InstanceNotFoundException e )
		{
			println( "WARNING: MBean " + resultOfCall + " returned from " +
				operationInfo.getReturnType() + " " + operationInfo.getName() + "() does not exist" );
				
		}
		catch( Exception e )
		{
			println( "WARNING: MBean " + resultOfCall + " returned from " +
				operationInfo.getReturnType() + " " + operationInfo.getName() +
				"() can't supply MBeanInfo: " + getExceptionMsg( e )
				);
			
			if ( e instanceof IOException )
			{
				connectionIOException( (IOException)e, true);
				throw (IOException)e;
			}
		}
	}
	
		void
	checkGetterResult(
		MBeanServerConnection	conn,
		ObjectName				callee,
		MBeanOperationInfo		operationInfo,
		Object					resultOfCall )
		throws Exception
	{
		if ( resultOfCall instanceof ObjectName )
		{
			final ObjectName	name	= (ObjectName)resultOfCall;
			
			checkObjectNameReturnValue( conn, callee, operationInfo, name );
		}
		else if ( resultOfCall instanceof ObjectName[] )
		{
			final ObjectName[]	names	= (ObjectName[])resultOfCall;
			
			for( int i = 0; i < names.length; ++i )
			{
				checkObjectNameReturnValue( conn, callee, operationInfo, names[ i ]);
			}
		}
	}
	
		private boolean
	validateGetters(
		final ObjectName			objectName,
		final MBeanOperationInfo[]	operationInfos )
		throws Exception
	{
		boolean	valid	= true;
		MBeanServerConnection conn	= getConnection();
		
		for( int i = 0; i < operationInfos.length; ++i )
		{
			final MBeanOperationInfo	info	= operationInfos[ i ];
			
			if ( isGetter( info ) )
			{
				boolean	opValid	= false;
					
				try
				{
					printVerbose( "invoking getter: " + info.getName() + "()" );
					final Object	result	= conn.invoke( objectName, info.getName(), null, null );
					
					checkGetterResult( conn, 
					objectName, info, result );
				}
				catch( Exception e )
				{
					printError( "Failure: calling " + info.getName() + "() on " + objectName +
						": " + getExceptionMsg( e ) );
						
					if ( e instanceof IOException)
					{
						connectionIOException( (IOException)e, true);
					}
					valid	= false;
				}
			}
		}
		
		return( valid );
	}
	

		boolean
	getPrintStackTraces()
	{
		boolean	printStackTraces	= false;

		try
		{
			printStackTraces	=
				getBoolean( PRINT_STACK_TRACES_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
		}
		catch( IllegalOptionException e )
		{
			assert( false );
		}
		
		return( printStackTraces );
	}
	
		boolean
	shouldPrintStackTraces()
	{
		return( getVerbose() || getPrintStackTraces() );
	}
	
	
		private boolean
	validate( final ObjectName objectName )
		throws Exception
	{
		boolean	valid	= true;

		MBeanServerConnection conn	= getConnection();
		
		MBeanInfo	info	= null;
		try
		{
			info	= conn.getMBeanInfo( objectName );
		}
		catch( Exception e )
		{
			printFailure( " during getMBeanInfo() for: " + objectName + "\n" +
				" message = " + e.getMessage() );
			// abort--the connection has died
			throw e;
		}
		
		if ( mDoInfo &&
			! validateMBeanInfo( objectName, info ) )
		{
			valid	= false;
		}
		
		
		if ( mDoAttributes &&
			! validateAttributes( objectName, info.getAttributes() ) )
		{
			valid	= false;
		}
		
		if ( mDoOperations &&
			! validateGetters( objectName, info.getOperations() ) )
		{
			valid	= false;
		}
		
		printVerbose( "" );
		
		return( valid );
	}
	
	
		private void
	validate( final ObjectName[] objectNames )
		throws Exception
	{
		int	failureCount	= 0;
		
		final Boolean	infoOption		= getBoolean( MBEAN_INFO_OPTION.getShortName(), null );
		final Boolean	attributesOption	= getBoolean( ATTRIBUTES_OPTION.getShortName(), null );
		final Boolean	operationsOption	= getBoolean( OPERATIONS_OPTION.getShortName(), null );
		
		if ( infoOption == null && attributesOption == null && operationsOption == null )
		{
			mDoInfo			= true;
			mDoAttributes	= true;
			mDoOperations	= true;
		}
		else
		{
			mDoInfo			= infoOption != null && infoOption.booleanValue();
			mDoAttributes	= attributesOption != null && attributesOption.booleanValue();
			mDoOperations	= operationsOption != null && operationsOption.booleanValue();
		}
		
		println( "Validating: " );
		if ( mDoInfo )
		{
			println( "- MBeanInfo" );
		}
		if ( mDoAttributes )
		{
			println( "- Attributes" );
		}
		if ( mDoOperations )
		{
			println( "- Operations (getters)" );
		}
		
		println( "" );
		
		for( int i = 0; i < objectNames.length; ++i )
		{
			printVerbose( "Validating: " + objectNames[ i ] );
			
			final boolean	valid	= validate( objectNames[ i ] );
			if ( ! valid )
			{
				++failureCount;
			}
		}
		
		println( "Total mbeans failing: " + failureCount );
	}
	
	
		protected void
	executeInternal()
		throws Exception
	{
		String [] targets	= getTargets();
		
		if ( targets == null || targets.length == 0 )
		{
			targets	= new String[] { "*" };
		}
		
		mPrintWarnings	= getBoolean( WARNINGS_OFF_OPTION.getShortName(), Boolean.FALSE ).booleanValue();
			
		establishProxy();
		
		final ObjectName[]	objectNames	= resolveTargets( getProxy(),  targets );
		
		if ( objectNames.length != 0 )
		{
			// sort them so output is nicer for the user
			Arrays.sort( objectNames, ObjectNameComparator.INSTANCE );
		
			println( "Validating " + objectNames.length + " mbeans." );
			validate( objectNames );
		}
		else
		{
			println( "No MBeans to validate." );
		}
        
        try
        {
            println( "" );
            
            final AMXValidator validator = new AMXValidator( getConnection(), "FULL", false, true);
            final Set<ObjectName> amxSet = validator.filterAMX( SetUtil.newSet(objectNames) );
            //final Set<ObjectName> amxSet = validator.findAllAMXCompliant();
            
            println("Validating " + amxSet.size() + " AMX MBeans..." );
            final AMXValidator.ValidationResult result = validator.validate(amxSet);
            
            println( "Success:  " + (result.numTested() - result.numFailures()) );
            println( "Failures: " + result.numFailures() );
            if ( result.numFailures() != 0)
            {
                println( result.toString() );
                println( "*** FAILURES FOUND ***" );
            }
        }
        catch( final Throwable t )
        {
            t.printStackTrace();
        }
	}
}






