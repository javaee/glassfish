/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jcmd/util/cmd/OptionsInfoTest.java,v 1.3 2003/12/18 19:10:49 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2003/12/18 19:10:49 $
 */
package com.sun.cli.jcmd.util.cmd;

import java.util.ArrayList;
import java.util.Iterator;
 


public class OptionsInfoTest extends junit.framework.TestCase
{

		public void
	testCreate()
		throws IllegalOptionException
	{
		new OptionsInfoImpl();
		new OptionsInfoImpl( "aa:a bb:b cc:c" );
		new OptionsInfoImpl( "aa:a bb:b,2 cc:c" );
		new OptionsInfoImpl( "long-option-dashed-name:l" );
		new OptionsInfoImpl( "long.option.dotted.name:l" );
		new OptionsInfoImpl( "long_option_underscore_name:l" );
		new OptionsInfoImpl( "long-option_mixed.name:l" );
	}
	
	
		void
	checkCreateFailure( String options )
		throws IllegalOptionException
	{
		try
		{
			new OptionsInfoImpl( options );
			fail( "expected failure from option description \"" + options + "\"" );
		}
		catch( Exception e )
		{
			// good
		}
	}
	
		public void
	testCreateFailures()
		throws IllegalOptionException
	{
		final String	illegals	= "~`!@#$%^&*()+={}[]|\\:;\"'<>?,/";
		
		for( int i = 0; i < illegals.length(); ++i )
		{
			checkCreateFailure( "" + illegals.charAt( i ) );
		}
	}
	
	
		public void
	testIllegals()
		throws IllegalOptionException
	{
		final OptionsInfo	info	= new OptionsInfoImpl( "hello:h" );
		
		assertEquals( false, info.isLegalOption( "hello" ) );
		assertEquals( false, info.isLegalOption( "h" ) );
		assertEquals( false, info.isLegalOption( "-hello" ) );
		assertEquals( false, info.isLegalOption( "--h" ) );
		assertEquals( false, info.isLegalOption( "-" ) );
		assertEquals( false, info.isLegalOption( "--" ) );
		assertEquals( false, info.isLegalOption( "" ) );
	}
	
		public void
	testGroupedBoolean()
		throws IllegalOptionException
	{
		final String	options	= "test:t version:V doit:d help:h";
		
		final OptionsInfo	info	= new OptionsInfoImpl( options );
		
		assertEquals( true, info.isLegalOption( "--test" ) );
		assertEquals( true, info.isLegalOption( "-t" ) );
		assertEquals( true, info.isLegalOption( "--version" ) );
		assertEquals( true, info.isLegalOption( "-V" ) );
		assertEquals( true, info.isLegalOption( "--doit" ) );
		assertEquals( true, info.isLegalOption( "-d" ) );
		assertEquals( true, info.isLegalOption( "--help" ) );
		assertEquals( true, info.isLegalOption( "-h" ) );
		
		// grouped booleans by themselves are not options
		assertEquals( false, info.isLegalOption( "-tVdh" ) );
		
	}
	
		public void
	testLegals()
		throws IllegalOptionException
	{
		final OptionsInfo	info	= new OptionsInfoImpl( "hello:h --there:t,3 --x:x,1 yy:-y,1" );
		
		assertEquals( true, info.isLegalOption( "--hello" ) );
		assertEquals( true, info.isLegalOption( "--there" ) );
		assertEquals( true, info.isLegalOption( "-x" ) );
		assertEquals( true, info.isLegalOption( "-y" ) );
	}
	
	
		void
	checkRedundantFailure( String args )
	{
		try
		{
			new OptionsInfoImpl( args );
			fail( "expected rejection of redundant option" );
		}
		catch( Exception e )
		{
		}
	}

		public void
	testRedundant()
		throws IllegalOptionException
	{
		checkRedundantFailure( ":a :a" );
		checkRedundantFailure( "aa:a aa:a" );
		checkRedundantFailure( "--aa:a --aa:-a" );
		checkRedundantFailure( ":-a :-a" );
	}

		public void
	testMixedArgs()
		throws IllegalOptionException
	{
		final OptionsInfo	info	= new OptionsInfoImpl( "aa:a bb:b,b-value cc:c,c-value1,c-value2 dd:d,d1,d2,d3 --xyz:x,v1,v2,v3,v4" );
		
		assertEquals( true, info.isLegalOption( "-a" ) );
		assertEquals( false, info.isLegalOption( "--a" ) );
		assertEquals( 1, info.getNumValues( "-a" ) );
		assertEquals( true, info.isBoolean( "-a" ) );
		
		assertEquals( 1, info.getNumValues( "-b" ) );
		assertEquals( true, info.isLegalOption( "-c" ) );
		assertEquals( 2, info.getNumValues( "-c" ) );
		assertEquals( 3, info.getNumValues( "-d" ) );
		assertEquals( 4, info.getNumValues( "--xyz" ) );
		
	}
	
		public void
	testSynonyms()
		throws IllegalOptionException
	{
		final OptionsInfo	info	= new OptionsInfoImpl(
			"aaa:a:bbb:ccc:ddd" );
		
		assertEquals( 5, ((OptionInfo)(info.getOptionInfos().iterator().next())).getSynonyms().size() );
	}

		public
	OptionsInfoTest()
	{
	}

		public void
	setUp()
	{
	}
	
		public void
	tearDown()
	{
	}

}

