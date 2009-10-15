/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jcmd/util/cmd/ArgHelperTest.java,v 1.2 2003/12/18 19:10:49 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/12/18 19:10:49 $
 */
 
package com.sun.cli.jcmd.util.cmd;

import java.util.Arrays;
import java.util.ListIterator;

import junit.framework.TestCase;
import org.glassfish.admin.amx.util.stringifier.SmartStringifier;
import org.glassfish.admin.amx.util.stringifier.ArrayStringifier;

/**
	Unit tests for ArgHelper.
	
	These acronyms are used in comments:
	<p>
	SUS	-- Open Group Technical Standard Base Specifications, Issue 6 (SUSv3)
	<p>
	CLIP -- Sun Microsystems Command Line Interface Paradigm version 1.0
	<p>A utility that conforms to guidelines 1-14 of CLIP is said to be
	<i>getopt compliant</i>.  A utility that conforms to guidelines 1 through 21
	is said to be <i>CLIP compliant</i>
	
	Not all guidelines are testable here; some of them are external in nature, such
	as the name of the command line program (CLIP 1).
 */
public class ArgHelperTest extends TestCase
{
		private static void
	dm(	Object msg	)
	{
		System.out.println( SmartStringifier.toString( msg ) );
	}
	
		public
	ArgHelperTest( String name )
	{
		super( name );
	}
	
	
	
		ArgHelper
	setupArgHelper( int startOffset, String options, String input)
		throws Exception
	{
		final OptionsInfoImpl optionsInfo	= new OptionsInfoImpl();
		
		if ( options != null )
		{
			optionsInfo.addOptions( options );
		}
		
		final String []	tokens	= (input.length() == 0) ?
							new String [0] : input.split( " " );
		
		final ListIterator<String>	iter	= Arrays.asList( tokens ).listIterator( startOffset );
		final ArgHelper		argHelper = new ArgHelperImpl( iter, optionsInfo );
		
		return( argHelper );
	}
	
		ArgHelper
	setupArgHelper( String options, String input)
		throws Exception
	{
		return( setupArgHelper( 0, options, input ) );
	}

	
	//-------------------------------------------------------------------------

		public void
	testNoOptionsOrOperands()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( null, "" );
		
		assertEquals( "expected no options", 0, argHelper.countOptions() );
		assertEquals( "expected no operands", 0, argHelper.getOperands().length );
	}
	
		public void
	testOperandsWithSameNameAsOptions()
		throws Exception
	{
		final String	options	= "version:V";
		final String	input	= "version";
		
		final ArgHelper	argHelper	= setupArgHelper( options, input );
		
		assertEquals( "expected no options", 0, argHelper.countOptions() );
		assertEquals( "expected 1 operands", 1, argHelper.getOperands().length );
	}



		public void
	testBooleanWithoutArg()
		throws Exception
	{
		final String	booleanOptions	= "testbool:t";
		final String	input			= "--testbool";
		
		final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
		
		final Boolean	value	= argHelper.getBooleanValue( "testbool", null );
		assertEquals( "expected true from " + input, Boolean.TRUE, value );
	}
	

		public void
	testBooleanWithInlineArgFalse()
		throws Exception
	{
		final String	booleanOptions	= "testbool:t";
		final String	input			= "--testbool=false";
		
		final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
		
		final Boolean	value	= argHelper.getBooleanValue( "testbool", null );
		assertEquals( "expected FALSE from " + input, Boolean.FALSE, value );
	}
	
		public void
	testBooleanWithInlineArgTrue()
		throws Exception
	{
		final String	booleanOptions	= "testbool:t";
		final String	input			= "--testbool=true";
		
		final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
		
		final Boolean	value	= argHelper.getBooleanValue( "testbool", null );
		assertEquals( "expected TRUE from " + input, Boolean.TRUE, value );
	}
	
	/**
		Boolean arguments must not allow their true/false value as a separate argument; this
		should be interpreted as the next token instead.
	 */
		public void
	testBooleanFailureWithSeparateArgTrue()
		throws Exception
	{
		final String	booleanOptions	= "testbool:t";
		final String	input			= "--testbool true";// 'true' is an operand
		
		final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
		
		final Boolean	value	= argHelper.getBooleanValue( "testbool", null );
		assertEquals( "expected 1 operand from: " + input, 1, argHelper.getOperands().length );
		assertEquals( "expected 1 option from: " + input, 1, argHelper.countOptions() );
	}
	
	/**
		Boolean arguments must not allow their true/false value as a separate argument; this
		should be interpreted as the next token instead.
	 */
		public void
	testBooleanFailureWithSeparateArgFalse()
		throws Exception
	{
		final String	booleanOptions	= "testbool:t";
		final String	input			= "--testbool false";// 'false' is an operand
		
		final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
		
		final Boolean	value	= argHelper.getBooleanValue( "testbool", null );
		assertEquals( "expected 1 operand from: " + input, 1, argHelper.getOperands().length );
		assertEquals( "expected 1 option from: " + input, 1, argHelper.countOptions() );
	}

	
	/**
		Test all the various forms of boolesn
	 */
		public void
	testMultipleBooleans()
		throws Exception
	{
		final String	booleanOptions	= "b1:1 b2:2 b3:3 b4:4";
		final String	input			= "--b1 --b2=false --b3 --b4=false";
		
		final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
		
		assertEquals( "expected b1 to be true from: " + input,
			Boolean.TRUE, argHelper.getBooleanValue( "b1", null ) );
			
		assertEquals( "expected b2 to be false from: " + input,
			Boolean.FALSE, argHelper.getBooleanValue( "b2", null ) );
			
		assertEquals( "expected b3 to be true from: " + input,
			Boolean.TRUE, argHelper.getBooleanValue( "b3", null ) );
			
		assertEquals( "expected b4 to be false from: " + input,
			Boolean.FALSE, argHelper.getBooleanValue( "b4", null ) );
	}
	

	
	// test single boolean option
		public void
	testBooleanFailureWithInvalidValue(  )
		throws Exception
	{
		final String	booleanOptions	= "b1:b";
		final String	input			= "--b1=hello";
		
		try
		{
			ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
			argHelper.getBooleanValue( "b1", null );
			fail( "expecte this input to fail for boolean: " + input );
		}
		catch( Exception e )
		{
		}
	}

	
	
		public void
	testInteger0()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( "i1:i,1", "--i1=0" );
		
		final Integer	value	= argHelper.getIntegerValue( "i1", null);
		assertEquals( new Integer( 0 ), value );
	}
	
		public void
	testIntegerNegative()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( "i1:i,1", "--i1=-9999" );
		
		final Integer	value	= argHelper.getIntegerValue( "i1", null);
		assertEquals( new Integer( -9999 ), value );
	}
	
		public void
	testIntegerPositive()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( "i1:i,1", "--i1=9999" );
		
		final Integer	value	= argHelper.getIntegerValue( "i1", null);
		assertEquals( new Integer( 9999 ), value );
	}
	
		public void
	testNoOptionsNoOperands()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( "s1:s,1", "--s1=" );
		
		final String	value	= argHelper.getStringValue( "s1", null);
		assertEquals( "", value );
	}
	
		public void
	testNoOptionsNoOperandsWithCmd()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( 1, null, "cmd" );
		
		assertEquals( "expecting no options", 0, argHelper.countOptions() );
		assertEquals( "expecting no operands", 0, argHelper.getOperands().length );
	}
	
		public void
	testOnlyOperands()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( "", "op1 op2" );
		
		assertEquals( "expecting 2 operands", 2, argHelper.getOperands().length );
		assertEquals( "expecting 0 options", 0, argHelper.countOptions() );
	}
	
		public void
	testNoOperands()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( "b1:1 b2:2 s1:s,1", "--b1 --b2 --s1=hello" );
		
		assertEquals( "expecting 0 operands", 0, argHelper.getOperands().length );
		assertEquals( "expecting 3 options", 3, argHelper.countOptions() );
		
		final String	value	= argHelper.getStringValue( "s1", null);
		assertEquals( "hello", value );
	}
	
	
		public void
	testIllegalOptionsFailure()
		throws Exception
	{
		try
		{
			final ArgHelper	argHelper	= setupArgHelper( "", "--option1=1" );
		}
		catch( Exception e )
		{
			// we expected to get here
		}
	}
	
		public void
	testShortBoolean()
		throws Exception
	{
		final String	booleanOptions	= "b";
		final String	input	= "-b";
			
		try
		{
			final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
			
			assertEquals( "expected TRUE for: " + input,
				Boolean.TRUE, argHelper.getBooleanValue( booleanOptions, null) );
		}
		catch( Exception e )
		{
			// we expected to get here
		}
	}
	
		public void
	testShortBooleanWithValue()
		throws Exception
	{
		final String	booleanOptions	= "b";
		final String	input	= "-b=false";
			
		try
		{
			final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
			
			assertEquals( "expected FALSE for: " + input,
				Boolean.FALSE, argHelper.getBooleanValue( booleanOptions, null) );
		}
		catch( Exception e )
		{
			// we expected to get here
		}
	}
	

	/**
		Test that asking for an option by its short name or long name or synonym works
	 */
		public void
	testAnyNameWorks()
		throws Exception
	{
		final String	booleanOptions	= "test:t:test2:xxx";
		final String	input	= "-t";
			
		final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
		
		assertEquals( Boolean.TRUE, argHelper.getBooleanValue( "t", null) );
		assertEquals( Boolean.TRUE, argHelper.getBooleanValue( "test", null) );
		assertEquals( Boolean.TRUE, argHelper.getBooleanValue( "xxx", null) );
		assertEquals( Boolean.TRUE, argHelper.getBooleanValue( "test2", null) );
	}
	
	
	/**
		Test that required options are detected propertly.
	 */
		public void
	testRequired()
		throws Exception
	{
		final String	options	= "+test:t foo:f";
		final String	input	= "";
		
		final ArgHelper	argHelper	= setupArgHelper( options, input );
		
		final OptionInfo[]	missing	= argHelper.getMissingOptions();
		assertEquals( 1, missing.length );
		assertEquals( "-t", missing[ 0 ].getShortName() );
	}
	

	
	/**
		Test that grouped booleans work eg that -x -y -z works the same as -xyz
		
		SUS/CLIP-5
	 */
		public void
	testGroupedBooleans_CLIP5()
		throws Exception
	{
		final String	options	= "verbose:v version:V help:-?:-h terse:t";
		final String	input	= "-vV?t";
		final ArgHelper	argHelper	= setupArgHelper( options, input);
		
		assertEquals( Boolean.TRUE, argHelper.getBooleanValue( "v", null) );
		assertEquals( Boolean.TRUE, argHelper.getBooleanValue( "V", null) );
		assertEquals( Boolean.TRUE, argHelper.getBooleanValue( "?", null) );
		assertEquals( Boolean.TRUE, argHelper.getBooleanValue( "h", null) );
		assertEquals( Boolean.TRUE, argHelper.getBooleanValue( "t", null) );
	}
	
	
	
	/**
		Short option arguments must be separated by a space
		from their name eg "-c 1". The form "-c=1" is not allowed.
		
		SUS/CLIP-6
	 */
		public void
	testShortOptionRequiresSpace_CLIP6()
	{
		final String	options	= "super:s,1";
		final String	input	= "-s=1";
			
		try
		{
			final ArgHelper	argHelper	= setupArgHelper( options, input );
			
			final String 	value	= argHelper.getStringValue( "s", null);
			
			assert( false ) : "expected " + input + " to fail";
		}
		catch( Exception e )
		{
			// good-we expected to get here
		}
	}
	
	
	
	
	/**
	
		When multiple option-arguments are specified to follow a single option, they should
		be presented as a single argument using commas within that argument or blanks within
		that argument to separate them.
		<p>
		<i>NOTE</i>: tested parsed does not support blanks.
	
		SUS/CLIP-7
	 */
		public void
	testMultipleValues_CLIP7()
		throws Exception
	{
		final String	options	= "a1:a,a1,a1,a3,a4";
		final String	input	= "--a1=1,2,hello,there op1 op2";
		
		final ArgHelper	argHelper	= setupArgHelper( options, input );
		
		final String []	values	= argHelper.getOptionValues( "a1" );
		assertEquals( "expected 4 options", values.length, 4 );
		assertEquals( "expected 2 operands", 2, argHelper.getOperands().length );
	}
		
	/**
		Parser supports escaping the delimiter ','.
	 */
		public void
	testMultipleValuesEscaped_CLIP7()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( "attrs:a,a1,a2,a3,a4", "--attrs hello,there,foo\\,bar,end op1 op2" );
		
		final String []	values	= argHelper.getOptionValues( "attrs" );
		assert( values != null );
		assertEquals( values.length, 4 );
		assertEquals( "expected 2 operands", 2, argHelper.getOperands().length );
	}
	
	
	/**
		The delimiter "--" marks the end of any options, even if subsequent arguments
		start with a "-".  The "--" should not be used as an option or operand.
		
		SUS/CLIP-10
	 */
		public void
	testEndOfOptionsDelim_CLIP10()
		throws Exception
	{
		final String	options	= "test:t,1";
		// note that the first '--' is actually an argument to 'test'
		final String	input	= "--test -- -- -xy --foobar abc";
		
		final ArgHelper	argHelper	= setupArgHelper( options, input );
		
		assertEquals( 1, argHelper.countOptions() );
		assertEquals( "--", argHelper.getStringValue( "test", null ) );
		
		final String[]	operands	= argHelper.getOperands();
		assertEquals( 3, operands.length );
		assertEquals( "-xy", operands[ 0 ] );
		assertEquals( "--foobar", operands[ 1 ] );
		assertEquals( "abc", operands[ 2 ] );
	}
	
	/**
		If an option that has option-arguments is repeated, the option and option-argument
		combinations should be interpreted in the order specified on the command line.
		
		SUS/CLIP-11
	 */
		public void
	testDuplicateOptions_CLIP11()
		throws Exception
	{
		final String	optionsDesc	= "test:t:test2:x,1";
		final String	input	= "--test=1 -t 2 -x 3 --test2=4 --test=5";
		
		final ArgHelper	argHelper	= setupArgHelper( optionsDesc, input );
		
		assertEquals( 5, argHelper.countOptions() );
		
		// our API should return the last instance which has value "3"
		assertEquals( "5", argHelper.getStringValue( "test", null) );
		assertEquals( "5", argHelper.getStringValue( "t", null) );
		assertEquals( "5", argHelper.getStringValue( "test2", null) );
		
		// should get them all, in correct order
		final ParsedOption[]	options	= argHelper.getOptionInstances( "t" );
		assertEquals( 5, options.length );
		assertEquals( "1", options[ 0 ].getValue() );
		assertEquals( "2", options[ 1 ].getValue() );
		assertEquals( "3", options[ 2 ].getValue() );
		assertEquals( "4", options[ 3 ].getValue() );
		assertEquals( "5", options[ 4 ].getValue() );

	}
	
	/**
		If a long-option name consists of a single character, it must use the same character
		as the short-option name.
		
		CLIP-20
	 */
		public void
	testSingleCharLongOption_CLIP11()
		throws Exception
	{
		final String	options	= "--t:-x";
		final String	input	= "--t";
		
		try
		{
			final ArgHelper	argHelper	= setupArgHelper( options, input );
		}
		catch( IllegalOptionException e )
		{
			// good-we expected to get her
		}
		
		setupArgHelper( "--t:-t", "" );
	}
	
	
	
		protected void
	setUp()
	{
	}
	
		protected void
	tearDown()
	{
	}
	
};



