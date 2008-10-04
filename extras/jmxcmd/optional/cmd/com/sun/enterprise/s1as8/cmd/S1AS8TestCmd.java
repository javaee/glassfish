/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/optional/cmd/com/sun/enterprise/s1as8/cmd/S1AS8TestCmd.java,v 1.13 2004/06/24 23:31:17 llc Exp $
 * $Revision: 1.13 $
 * $Date: 2004/06/24 23:31:17 $
 */
 
package com.sun.enterprise.s1as8.cmd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import javax.management.ObjectName;
import javax.management.MBeanAttributeInfo;
import javax.management.MalformedObjectNameException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;

import com.sun.cli.jcmd.util.cmd.IllegalOptionException;
import com.sun.cli.jcmd.util.cmd.OptionInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfo;
import com.sun.cli.jcmd.util.cmd.OptionsInfoImpl;
import com.sun.cli.jcmd.framework.CmdHelpImpl;
import com.sun.cli.jcmd.framework.CmdHelp;
import com.sun.cli.jcmd.framework.CmdEnv;
import com.sun.cli.jcmd.framework.CmdException;
import com.sun.cli.jcmd.framework.IllegalUsageException;

import com.sun.cli.jcmd.util.misc.CompareUtil;
import com.sun.cli.jcmd.util.stringifier.Stringifier;
import com.sun.cli.jcmd.util.stringifier.ArrayStringifier;
import com.sun.cli.jcmd.util.stringifier.IteratorStringifier;

import com.sun.cli.jmxcmd.util.jmx.AttributeFilter;
import com.sun.cli.jmxcmd.util.jmx.ReadWriteAttributeFilter;
import com.sun.cli.jmxcmd.util.jmx.JMXUtil;
import com.sun.cli.jmxcmd.cmd.JMXCmd;
import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;

import com.sun.cli.jcmd.util.cmd.CmdInfos;
import com.sun.cli.jcmd.util.cmd.CmdInfo;
import com.sun.cli.jcmd.util.cmd.CmdInfoImpl;


import com.sun.cli.jcmd.util.cmd.OperandsInfoImpl;






/**
	This command supports access to the Sun Java System Appserver dotted-name
	'get', 'set' and 'list' facilities.
 */
public class S1AS8TestCmd extends JMXCmd
{
	static final class S1AS8TestCmdHelp extends CmdHelpImpl
	{
		public	S1AS8TestCmdHelp()	{ super( getCmdInfos() ); }
		
		private final static String	SYNOPSIS	= "tests for Sun Java System Appserver 8 PE";
			
		private final static String	TEXT		=
			"The s1as8-test-set command is intended for testing only; it gets each writeable Attribute's value, then sets it " +
			"to the same value, then verifies that no error occurred, and that it has the same value.\n\n" +
			"The s1as8-test-for-empty-prefixes command tests that there are no empty prefixes in the dotted name space " +
			"(ones that contain no attributes).\n\n" +
			"";

		
		public String	getName()		{	return( NAME ); }
		public String	getSynopsis()	{	return( formSynopsis( SYNOPSIS ) ); }
		public String	getText()		{	return( TEXT ); }
	}
	
		public
	S1AS8TestCmd( final CmdEnv env )
	{
		super( env );
	}
	
	final static String	NAME							= "s1as8-test";
	final static String	TEST_SET_NAME					= "s1as8-test-set";
	final static String	TEST_FOR_EMPTY_NODES_NAME		= "s1as8-test-for-empty-prefixes";


	private final static OptionsInfo	VERBOSE_OPTIONS_INFO	=
		new OptionsInfoImpl( new OptionInfo[] { VERBOSE_OPTION });
		
	private final static CmdInfo	TEST_SET_INFO	=
		new CmdInfoImpl( TEST_SET_NAME, VERBOSE_OPTIONS_INFO, TARGETS_OPERAND_INFO );
		
	private final static CmdInfo	TEST_FOR_EMPTY_NODES_INFO	=
		new CmdInfoImpl( TEST_FOR_EMPTY_NODES_NAME, VERBOSE_OPTIONS_INFO, OperandsInfoImpl.NONE );
		
		
		public static CmdInfos
	getCmdInfos( )
	{
		return( new CmdInfos( TEST_SET_INFO, TEST_FOR_EMPTY_NODES_INFO ) );
	}
	
		public CmdHelp
	getHelp()
	{
		return( new S1AS8TestCmdHelp() );
	}
	
		void
	verifyMinResultsForGet( final String expr, final int minNumberOfResults, final Object result )
		throws CmdException
	{
		// check that each results is an Attribute[]
		if ( result instanceof Attribute[] )
		{
			final Attribute[]	attrs	= (Attribute[])result;
			if ( attrs.length < minNumberOfResults )
			{
				throw new CmdException( getSubCmdNameAsInvoked(),
					"caller specifed that at least " + minNumberOfResults +
					" are required for expression " + quote( expr ));
			}
		}
		else
		{
			throw new CmdException( getSubCmdNameAsInvoked(),
				"caller specifed that at least " + minNumberOfResults +
				" are required, but the resulting value was of class " + result.getClass().getName() );
		}
	}
	
		private void
	testSettingAttributes( ObjectName target )
		throws Exception
	{
		MBeanServerConnection	conn	= getConnection();
		final MBeanAttributeInfo[]	allAttrNames	= JMXUtil.getAttributeInfos( conn, target );
		final MBeanAttributeInfo[]	writeable	= JMXUtil.filterAttributeInfos( allAttrNames, ReadWriteAttributeFilter.WRITEABLE_FILTER );
		final String[]	writeableNames	= JMXUtil.getAttributeNames( writeable );
		
		assert( CompareUtil.objectsEqual( new String[ 0 ], new String[ 0 ] ) );
		
		final ArrayList	successList	= new ArrayList();
		final ArrayList	failureList	= new ArrayList();
		for( int i = 0; i < writeableNames.length; ++i )
		{
			final String	attrName	= writeableNames[ i ];
			
			Object	oldValue	= null;
			// get the value
			try
			{
				oldValue	= conn.getAttribute( target, attrName );
			}
			catch( IOException e )
			{
				println( "WARNING: IOException trying to get Attribute " +
					quote( attrName ) + " of object " + quote( target ) + "\n");
					
				conn	= connectionIOException( e );
				continue;
			}
			catch( Exception e )
			{
				printError( "can't get value of Attribute " + quote( attrName ) +
					" of object " + quote( target ) +
					" caught exception: " + e.getClass().getName() + quote( e.getMessage(), '(') + "\n"
				);
				failureList.add( attrName );
				continue;
			}
			
			// set it to itself
			try
			{
				conn.setAttribute( target, new Attribute( attrName, oldValue ) );
			}
			catch( IOException e )
			{
				println( "WARNING: IOException of class " + e.getClass().getName() +
				" trying to set Attribute " +
					quote( attrName ) + " of object " + quote( target )  + "\n");
				// reestablish
				
				conn	= connectionIOException( e );
				continue;
			}
			catch( Exception e )
			{
				printError( "can't set value of Attribute " + quote( attrName ) +
					" of object " + quote( target )  + "\n");
				failureList.add( attrName );
				continue;
			}
			
			
			// verify that it's the same
			try
			{
				final Object newValue	= conn.getAttribute( target, attrName );
				
				boolean	theSame	= CompareUtil.objectsEqual( oldValue, newValue );
				
				if ( ! theSame )
				{
					final String	oldValueString	= quote( oldValue, '(');
					
					println( "NOTE: Attribute " + quote( attrName ) + " = " +
						quote( newValue ) +
						" of object " + quote( target ) +
						" is not the same as what was set " + oldValueString + "\n" );
					continue;
				}
			}
			catch( Exception e )
			{
				printError( "can't set value of Attribute " + quote( attrName ) +
					" of object " + quote( target )  + "\n");
				failureList.add( attrName );
				continue;
			}
			
			successList.add( attrName );
		}
		
		final String[]	successes	= (String[])successList.toArray( new String[ successList.size() ] );
		final String[]	failures	= (String[])failureList.toArray( new String[ failureList.size() ] );
		
		final Stringifier stringifier	= new ArrayStringifier( ", ", true );
		final String	successesString	= stringifier.stringify( successes );
		
		if ( failures.length == 0 )
		{
			if ( getVerbose() )
			{
				println( "Results for " + quote( target ) + ":" +
					"successes = " + successes.length + ": " + successesString + ", 0 failures" );
			}
		}
		else
		{
			final String	failuresString	= stringifier.stringify( failures );
			
			println( "Results for " + quote( target ) + ":" +
				" successes = " + successes.length + " = " + successesString +
				", Failures: " + failures.length + " = " + failuresString );
		}
		println( "" );
	}
	
	
		private boolean
	existsWriteableAttribute( final MBeanAttributeInfo[]	attrInfos )
	{
		boolean	isWriteable	= false;
		
		for( int i = 0; i < attrInfos.length; ++i )
		{
			if ( attrInfos[ i ].isWritable() )
			{
				isWriteable	= true;
				break;
			}
		}
		
		return( isWriteable );
	}
	
		void
	findReadWriteMBeans(
		final ObjectName[]	objectNames,
		final Set			readOnlyNames,
		final Set			writeableNames )
		throws java.io.IOException, ReflectionException, IntrospectionException, InstanceNotFoundException
	{
		final MBeanServerConnection	conn	= getConnection();
		
		for( int i = 0; i < objectNames.length; ++i )
		{
			final ObjectName	name	= objectNames[ i ];
			assert( name != null );
			
			if ( getVerbose() )
			{
				println( "getMBeanInfo()" + quote( name, '(' ) );
			}
			final MBeanAttributeInfo[]	attrInfos	= conn.getMBeanInfo( name ).getAttributes();
			
			if ( existsWriteableAttribute( attrInfos ) )
			{
				writeableNames.add( name );
			}
			else
			{
				readOnlyNames.add( name );
			}
		}
		
		printDebug( "\n" );
	}
	
	
		private int
	getNumAttributesInPrefix(
		final DottedNameProxy	proxy,
		final String			prefix,
		boolean					monitoring )
		throws CmdException
	{
		int	numAttrs	= 0;
		
		// request all immediate attributes in the prefix
		final String[]	request	= new String[] { prefix + ".*" };
		
		Object[]	results	= null;
		if ( monitoring )
		{
			results	= proxy.dottedNameMonitoringGet( request );
		}
		else
		{
			results	= proxy.dottedNameGet( request );
		}
		
		if ( results.length != request.length )
		{
			// this shouldn't ever happen, unless API is broken
			throw new CmdException( getSubCmdNameAsInvoked(), "Response returned wrong number of results" );
		}
		else
		{
			final Attribute[]	attrs	= (Attribute[])results[ 0 ];
			numAttrs	= attrs.length;
		}
		
		return( numAttrs );
	}
	
		private void
	testForEmptyNodes(
		final DottedNameProxy	proxy,
		final String[]			prefixes,
		boolean					monitoring )
		throws CmdException
	{
		final String	subCmd	= getSubCmdNameAsInvoked();
		
		final String prefixStr	= monitoring ? "monitoring prefix " : "prefix";
		
		for( int i = 0; i < prefixes.length; ++i )
		{
			final String	prefix	= prefixes[ i ];
			
			final int numAttrs	= getNumAttributesInPrefix( proxy, prefix, monitoring );
			if ( numAttrs == 0 )
			{
				println( "Failure: " + prefixStr + " " + quote( prefix ) + " contains no attributes" );
			}
			else if ( getVerbose() )
			{
				println( "Success: " + prefixStr + " " + quote( prefix ) + " contains " + numAttrs + " attributes" );
			}
		}
	}
	
		private void
	handleTestForEmptyNodes()
		throws CmdException, IOException
	{
		final DottedNameProxy	proxy	= DottedNameProxyFactory.createProxy( getConnection() );
		
		final String[]	ALL	= new String[] { "*" };
		
		final Object[]	listResults	= proxy.dottedNameList( ALL );
		testForEmptyNodes( proxy, (String[])listResults, false);
		
		final Object[]	monitoringListResults	= proxy.dottedNameMonitoringList( ALL );
		testForEmptyNodes( proxy, (String[])monitoringListResults, true );
	}
	
		private void
	handleTestSet( final String[]	targets )
		throws Exception
	{
		final CLISupportMBeanProxy	proxy	= getProxy();
		
		if ( getVerbose() )
		{
			println( "Running against targets: " +
				new ArrayStringifier( ", ", true).stringify( targets ) );
		}
		
		final ObjectName[]	objectNames	= resolveTargets( proxy, targets );
		
		println( "Checking " + objectNames.length + " MBeans" );
		
		final Set	readOnlySet		= new HashSet();
		final Set	writeableSet	= new HashSet();
		findReadWriteMBeans( objectNames, readOnlySet, writeableSet );
		println( "----------------------------------------------------------------\n" );
		
		println( "The following MBeans claim to be ENTIRELY READ-ONLY: " );
		println( IteratorStringifier.stringify( readOnlySet.iterator(), "\n" ) );
		println( "----------------------------------------------------------------\n" );
		println( "The following MBeans claim to have writeable Attributes: " );
		println( IteratorStringifier.stringify( writeableSet.iterator(), "\n" ) );
		println( "----------------------------------------------------------------\n" );
		println( "\nChecking the writeable MBeans..." );
		
		final ObjectName[]	testees	= (ObjectName[])writeableSet.toArray( new ObjectName[ writeableSet.size() ] );
		for( int i = 0; i < testees.length; ++i )
		{
			testSettingAttributes( testees[ i ] );
		}
	}
	
	
		protected void
	executeInternal()
		throws Exception
	{
		final String [] operands	= getOperands();
		final String	cmd			= getSubCmdNameAsInvoked();
		
		establishProxy();
		
		if ( cmd.equals( TEST_SET_NAME ) )
		{
			String[]	targets	= operands;
			
			// by default, no operands means all
			if ( targets.length == 0 )
			{
				targets	= new String[] { "all" };
			}
			
			handleTestSet( targets );
		}
		else if ( cmd.equals( TEST_FOR_EMPTY_NODES_NAME ))
		{
			handleTestForEmptyNodes();
		}
		else
		{
			throw new IllegalUsageException( cmd );
		}
	}
}






