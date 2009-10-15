/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/test/server/CLISupportTester.java,v 1.2 2003/11/21 22:15:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/21 22:15:45 $
 */
 
package com.sun.cli.jmxcmd.test.server;

// java imports
//
import java.lang.reflect.Array;

// RI imports
//
import javax.management.*;


import org.glassfish.admin.amx.util.stringifier.*;

import com.sun.cli.jmxcmd.support.CLISupportStrings;
import com.sun.cli.jmxcmd.support.StandardAliases;
import com.sun.cli.jmxcmd.support.CLISupportMBeanProxy;
import com.sun.cli.jmxcmd.support.InspectRequest;
import com.sun.cli.jmxcmd.support.InspectResult;
import com.sun.cli.jmxcmd.support.InvokeResult;
import com.sun.cli.jmxcmd.support.ResultsForGetSet;
import com.sun.cli.jmxcmd.support.InspectResultStringifier;
import com.sun.cli.jmxcmd.support.ResultsForGetSetStringifier;
import com.sun.cli.jmxcmd.support.InvokeResultStringifier;
import org.glassfish.admin.amx.util.ClassUtil;


public final class CLISupportTester
{
	final MBeanServerConnection	mServer;
	final CLISupportMBeanProxy	mProxy;
	final TestLog				mLog;
	
	final static private String		ALIAS_BASE	= "test-alias-";
	private final static String		CLI_TEST_ALIAS_NAME	= ALIAS_BASE + "generic-test";

		public
	CLISupportTester( MBeanServerConnection conn, CLISupportMBeanProxy proxy )
		throws Exception
	{
		this( conn, proxy, new StdOutTestLog() );
	}
	
		public
	CLISupportTester( MBeanServerConnection conn, CLISupportMBeanProxy proxy, TestLog logger)
		throws Exception
	{
		mServer	= conn;
		mLog	= logger;
		
		mProxy	= proxy;
	}

		private void
	p( Object arg )
	{
		mLog.println( arg );
	}
	
		private void
	p( Object arg, boolean newline)
	{
		if ( newline )
		{
			p( arg );
		}
		else
		{
			mLog.print( arg );
		}
	}
	
		private void
	begin( String msg )
	{
		p( msg + "...", false);
	}
	
		private static String
	quote( Object str )
	{
		return( "\"" + str.toString() + "\"" );
	}
	

		private void
	TestMBeanList( CLISupportMBeanProxy test ) throws Exception
	{
		final String []	testStrings	= new String [] { StandardAliases.ALL_ALIAS };
		
		final ObjectName []	results	= test.mbeanFind( testStrings );
		final int			numObjects	= Array.getLength( results );
		
		final ArrayStringifier	testStringsStringifier	= new ArrayStringifier( "," );
		
		p( "\nmbean-list results: " + numObjects +
			" mbeans for expr " + testStringsStringifier.stringify( testStrings ) );
		
		final String	str	= ArrayStringifier.stringify( results, "\n", ObjectStringifier.DEFAULT);
		p ( str );
		
		p( "TestMBeanList...DONE: listed " + numObjects + " for " + testStrings[ 0 ] );
	}
	
		private void
	TestMBeanInspect( CLISupportMBeanProxy test, String [] patterns ) throws Exception
	{
		begin( "TestMBeanInspect" );
		
		final InspectRequest	request	= new InspectRequest();
		
		final InspectResult []	results	= test.mbeanInspect( request, patterns );
		final int			numResults	= Array.getLength( results );
		
		final String	summary	= "" + numResults +
				" mbeans for expr " + ArrayStringifier.stringify( patterns, ",");
				
		final String	str	= ArrayStringifier.stringify( results, "\n\n", new InspectResultStringifier());
		
		p( "DONE: inspected " + summary);
	}
	
		private void
	TestMBeanGet( CLISupportMBeanProxy test, String [] targets ) throws Exception
	{
		begin( "TestMBeanGet" );
		
		final ResultsForGetSet []	results	= test.mbeanGet( "*", targets );
		final int					numResults	= Array.getLength( results );
		
		final String	summary	= "" + numResults +
				" mbeans for expr " + ArrayStringifier.stringify( targets, ",");
				
		final ResultsForGetSetStringifier resultStringifier =
			new ResultsForGetSetStringifier(  );
			
		final String str	= ArrayStringifier.stringify( results, "\n\n", resultStringifier);
		
		p ( str );
		
		p( "DONE: inspected " + summary);
	}
	
	
		private String
	InvokeResultsToString( InvokeResult []	results)
	{
		return( ArrayStringifier.stringify( results, "\n", new InvokeResultStringifier() ) );
	}
	
	
		private void
	testInvoke( String operationName, String args, String [] targets )
		throws Exception
	{
		InvokeResult []	results = null;
		
		results = mProxy.mbeanInvoke( operationName, args, targets );
		
		p( InvokeResultsToString( results ) + "\n" );
	}
	
		private void
	TestNamedInvoke( CLISupportMBeanProxy test, String [] targets ) throws Exception
	{
		begin( "TestNamedInvoke" );
		
		testInvoke( "testNamed", "p1=hello", targets );
		
		testInvoke( "testNamed", "p1=hello,p2=there", targets );
		
		testInvoke( "testNamed", "p1=hello,p2=there,p3=!!!", targets );
		
		testInvoke( "testNamed", "p1=hello,p2=there,p3=!!!,p4=foobar", targets );
		
		p( "DONE ");
	}


		private void
	TestMBeanInvoke( CLISupportMBeanProxy test, String [] targets ) throws Exception
	{
		TestNamedInvoke( test, targets );
	}

	
	
		private void
	deleteTestAliases() throws Exception
	{
		final String []	aliases	= mProxy.listAliases( false );
		
		for( int i = 0; i < aliases.length; ++i )
		{
			final String	name	= aliases[ i ];
			
			if ( name.startsWith( ALIAS_BASE ) )
			{
				p( "deleteTestAliases: deleting: " + name );
				mProxy.deleteAlias( name );
			}
		}
	}
	
		private void
	TestAliases(  ) throws Exception
	{
		begin( "TestAliases" );
		
		deleteTestAliases();
		
		int	failureCount	= 0;
		
		// create an alias for each MBean
		final ObjectName []	names	= mProxy.mbeanFind( new String [] { StandardAliases.ALL_ALIAS } );
		final int			numNames	= Array.getLength( names );
		
		// create  test alias for each existing MBean
		for( int i = 0; i < numNames; ++i )
		{
			final String	aliasName	= ALIAS_BASE + (i+1);
			mProxy.createAlias( aliasName, names[ i ].toString() );
		}
		
		// now verify that each of them resolves correctly
		for( int i = 0; i < numNames; ++i )
		{
			final String	aliasName	= ALIAS_BASE + (i+1);
			
			final String	aliasValue	= mProxy.getAliasValue( aliasName );
			if ( aliasValue == null || ! names[ i ].toString().equals( aliasValue ))
			{
				++failureCount;
				p( "FAILURE: alias " + aliasName + ": " +
					quote( aliasValue ) + " != " + quote( names[ i ].toString() ) );
			}
		}
		
		// create an alias consisting of all aliases
		final String	ALL_ALIASES_NAME	= ALIAS_BASE + "all";
		final String []	aliases	= mProxy.listAliases( false );
		final String	allAliases	= ArrayStringifier.stringify( aliases, " " );
		mProxy.createAlias( ALL_ALIASES_NAME, allAliases );
		
		// create a recursive alias
		String	allAliasesName	= ALL_ALIASES_NAME;
		for( int i = 0; i < 5; ++i )
		{
			mProxy.createAlias( allAliasesName + i, allAliasesName );
			allAliasesName	= allAliasesName + i;
		}
		
		// verify that the alias to all of them produces the same set of names as we started with
		final ObjectName []	resolvedNames	= mProxy.resolveTargets( new String [] { allAliasesName } );
		//p( "all aliases = " + ArrayStringifier.stringify( resolvedNames, "\n" ) );
		if ( Array.getLength( resolvedNames ) !=  numNames )
		{
			++failureCount;
		}
		
		deleteTestAliases();
		
		if ( failureCount == 0 )
		{
			p( "DONE" );
		}
		else
		{
			p( "FAILURES = " + failureCount );
		}
	}
	
	/*
		Convert the parameters to String suitable for consumption by the CLISupportMBean
	 */
		private String
	MakeArgList( final String [] args )
	{
		final int			numArgs	= Array.getLength( args );
		String				result	= null;
		
		if ( numArgs != 0 )
		{
			final StringBuffer	buf	= new StringBuffer();
		
			for( int i = 0; i < numArgs; ++i )
			{
				buf.append( args[ i ] );
				buf.append( "," );
			}
			// strip trailing ","
			buf.setLength( buf.length() - 1 );
			
			result	= new String( buf ) ;
		}
		
		return( result );
	}
	
	
		private String
	getCastType( String type )
		throws ClassNotFoundException
	{
		String	result	= type;
		
		if ( ClassUtil.classnameIsArray( result ) )
		{
			final Class	theClass	= ClassUtil.getClassFromName(result);
			
			final Class elementClass	= ClassUtil.getInnerArrayElementClass( theClass );
			
			result	= elementClass.getName();
		}
		
		return( result );
	}
	
	
	
		private InvokeResult.ResultType
	TestOperationGenerically(
		final CLISupportMBeanProxy	test,
		final boolean				namedArgs,
		final ObjectName			targetName,
		final MBeanOperationInfo	operationInfo )
		throws Exception
	{
		final MBeanParameterInfo []	paramInfos	= operationInfo.getSignature();
		final int					numParams	= Array.getLength( paramInfos );
		
		final String []	strings	= new String [ numParams ];
		final String	operationName	= operationInfo.getName();
		
		// create an object of the correct type for each parameter.
		// The actual value is not important.
		for( int i = 0; i < numParams; ++i )
		{
			final MBeanParameterInfo	paramInfo	= paramInfos[ i ];
			final String				paramType	= paramInfos[ i ].getType();
			final Class					theClass	= ClassUtil.getClassFromName( paramType );
			
			final Object paramObject	= ClassUtil.InstantiateDefault( theClass );
			final String paramString	= SmartStringifier.toString( paramObject );
			final String castString		= "(" + getCastType( paramType ) + ")";
			
			final String paramName		= namedArgs ? (paramInfo.getName() + '=') : "";
			
			strings[ i ]	= paramName + castString + paramString;
		}
		
		// convert the arguments to strings
		final String	argString	= MakeArgList( strings );
		
		final String []	args	= new String [] { targetName.toString() };
		
		final InvokeResult []	results	= (InvokeResult [])test.mbeanInvoke( operationName, argString, args );
		final InvokeResult	result	= results[ 0 ];
		
		if ( result.getResultType() == InvokeResult.SUCCESS )
		{
			// p( "SUCCESS: " + operationName + "(" + SmartStringifier.toString( paramInfos ) + ")");
		}
		else
		{
			final String paramInfosString	= SmartStringifier.toString( paramInfos );
			
			p( "FAILURE: " + operationName + "(" + paramInfosString + ")" +
				" with " + argString );
			result.getThrowable().printStackTrace();
		}
		
		return( result.getResultType() );
	}
	
	static private final Class []	GENERICALLY_TESTABLE_CLASSES	= 
	{
		boolean.class,
		char.class,
		byte.class, short.class, int.class, long.class,
		float.class, double.class,
		Boolean.class,
		Character.class,
		Byte.class, Short.class, Integer.class, Long.class,
		Float.class,
		Double.class,
		Number.class,
		String.class,
		Object.class,
		java.math.BigDecimal.class,
		java.math.BigInteger.class,
		java.net.URL.class,
		java.net.URI.class

	};
		private boolean
	IsGenericallyTestableClass( final Class theClass )
		throws ClassNotFoundException
	{
		boolean	isTestable	= false;
		
		Class	testClass	= theClass;
		if ( ClassUtil.classIsArray( theClass ) )
		{
			// we can test all arrays of supported types
			testClass	= ClassUtil.getInnerArrayElementClass( theClass );
		}
		
		final Class []	classes	= GENERICALLY_TESTABLE_CLASSES;
		final int	numClasses	= Array.getLength( classes );
		for( int i = 0; i < numClasses; ++i )
		{
			if ( testClass == classes[ i ] )
			{
				isTestable	= true;
				break;
			}
		}
		
		if ( ! isTestable  )
		{
			assert( testClass == java.util.Properties.class );
		}
		
		return( isTestable );
	}
	
		private boolean
	IsGenericallyTestable( final MBeanOperationInfo operationInfo )
		throws ClassNotFoundException
	{
		boolean	isTestable	= true;
		
		final MBeanParameterInfo []	paramInfos	= operationInfo.getSignature();
		final int					numParams	= Array.getLength( paramInfos );
		for( int i = 0; i < numParams; ++i )
		{
			final Class	theClass	= ClassUtil.getClassFromName( paramInfos[i].getType() );
			
			if ( ! IsGenericallyTestableClass( theClass ) )
			{
				isTestable	= false;
				break;
			}
		}
		
		return( isTestable );
	}
	
	
		private void
	TestGeneric( CLISupportMBeanProxy test, boolean namedTest, ObjectName objectName ) throws Exception
	{
		final MBeanInfo				info	= mServer.getMBeanInfo( objectName );
		final MBeanOperationInfo []	opInfo	= info.getOperations();
		
		begin( "TestGeneric" );
		
		int	successCount	= 0;
		int	failureCount	= 0;
		int	notTestedCount	= 0;
		for( int i = 0; i < Array.getLength( opInfo ); ++i )
		{
			try
			{
				if ( IsGenericallyTestable( opInfo[ i ] ) )
				{
					final InvokeResult.ResultType resultType	= TestOperationGenerically( test,namedTest, objectName, opInfo[ i ] );
					if ( resultType == InvokeResult.SUCCESS )
					{
						++successCount;
					}
					else
					{
						++failureCount;
					}
				}
				else
				{
					++notTestedCount;
				}
			}
			catch( Exception e )
			{
				p( "FAILURE: " + SmartStringifier.toString( opInfo[ i ] ) );
			}
		}
		
		p( "DONE " + (namedTest ? "NAMED":"ORDERED") +
			": SUCCESSES = " + successCount +
			", FAILURES = " + failureCount +
			", NOT TESTABLE = " + notTestedCount );
	}
	
		private void
	TestGeneric( CLISupportMBeanProxy test, String [] targets ) throws Exception
	{
		final ObjectName []	allObjects	= test.mbeanFind( targets );
		
		assert( allObjects.length >= 1 );
		
		for( int i = 0; i < allObjects.length; ++i )
		{
			TestGeneric( test, false, allObjects[ i ] );
		}
		
		for( int i = 0; i < allObjects.length; ++i )
		{
			TestGeneric( test, true, allObjects[ i ] );
		}
	}
	
		private void
	VerifySetup( CLISupportMBeanProxy proxy ) throws Exception
	{
		// must be at least one MBean
		final ObjectName []	all	= proxy.resolveTargets( new String [] { "*" } );
		assert( all.length != 0 );
		
		// verify that the AliasMgr and CLI are available.
		final String []	aliases	= proxy.listAliases( false );
		assert( aliases.length != 0 );
		
		// verify that required aliases are in place
		assert( proxy.getAliasValue( StandardAliases.ALL_ALIAS ) != null );
		assert( proxy.getAliasValue( StandardAliases.CLI_ALIAS ) != null );
		assert( proxy.getAliasValue( StandardAliases.ALIAS_MGR_ALIAS ) != null );
		
	}
	
	
		public void
	Run() throws Exception
	{
		final CLISupportMBeanProxy	proxy	= mProxy;
		
		try
		{
			// ensure certain aliases are present, as we use them
			final String []	all			= new String [] { StandardAliases.ALL_ALIAS };
		
			VerifySetup( proxy );
			
			TestAliases(  );
			
			TestMBeanList( proxy );
			
			TestMBeanGet( proxy, all );
			
			TestMBeanInspect( proxy, all );
			
			proxy.deleteAlias( CLI_TEST_ALIAS_NAME );
			proxy.createAlias( CLI_TEST_ALIAS_NAME, CLISupportStrings.CLI_SUPPORT_TESTEE_TARGET );
			final String []	testMBean	= new String [] { CLI_TEST_ALIAS_NAME };
			
			TestMBeanInvoke( proxy, testMBean );
			
			TestGeneric( proxy, testMBean);
			
			
			p( "DONE" );
			
				
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}


};


