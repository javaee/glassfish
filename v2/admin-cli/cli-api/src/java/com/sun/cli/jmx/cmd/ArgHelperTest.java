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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/ArgHelperTest.java,v 1.4 2006/11/10 21:14:44 dpatil Exp $
 * $Revision: 1.4 $
 * $Date: 2006/11/10 21:14:44 $
 */
 
package com.sun.cli.jmx.cmd;

import java.util.Arrays;
import java.util.ListIterator;

import junit.framework.TestCase;
import com.sun.cli.util.stringifier.SmartStringifier;
import com.sun.cli.util.stringifier.ArrayStringifier;

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
		final ArgHelperOptionsInfo optionsInfo	= new ArgHelperOptionsInfo();
		
		if ( options != null )
		{
			optionsInfo.addOptions( options );
		}
		
		final String []	tokens	= (input.length() == 0) ?
							new String [0] : input.split( "" + ArgHelperOptionsInfo.MULTIPLE_DELIM );
		
		final ListIterator	iter	= Arrays.asList( tokens ).listIterator( startOffset );
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
	testBooleanWithoutArg()
		throws Exception
	{
		final String	booleanOptions	= "testbool";
		final String	input			= "--testbool";
		
		final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
		
		final Boolean	value	= argHelper.getBoolean( booleanOptions, null );
		assertEquals( "expected true from " + input, Boolean.TRUE, value );
	}
	

		public void
	testBooleanWithInlineArgFalse()
		throws Exception
	{
		final String	booleanOptions	= "testbool";
		final String	input			= "--testbool=false";
		
		final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
		
		final Boolean	value	= argHelper.getBoolean( booleanOptions, null );
		assertEquals( "expected FALSE from " + input, Boolean.FALSE, value );
	}
	
		public void
	testBooleanWithInlineArgTrue()
		throws Exception
	{
		final String	booleanOptions	= "testbool";
		final String	input			= "--testbool=true";
		
		final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
		
		final Boolean	value	= argHelper.getBoolean( booleanOptions, null );
		assertEquals( "expected TRUE from " + input, Boolean.TRUE, value );
	}
	
	
		public void
	testBooleanFailureWithSeparateArgTrue()
		throws Exception
	{
		final String	booleanOptions	= "testbool";
		final String	input			= "--testbool true";// 'true' is an operand
		
		final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
		
		final Boolean	value	= argHelper.getBoolean( booleanOptions, null );
		assertEquals( "expected 1 operand from: " + input, 1, argHelper.getOperands().length );
		assertEquals( "expected 1 option from: " + input, 1, argHelper.countOptions() );
	}
	
		public void
	testBooleanFailureWithSeparateArg()
		throws Exception
	{
		final String	booleanOptions	= "testbool";
		final String	input			= "--testbool false";// 'false' is an operand
		
		final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
		
		final Boolean	value	= argHelper.getBoolean( booleanOptions, null );
		assertEquals( "expected 1 operand from: " + input, 1, argHelper.getOperands().length );
		assertEquals( "expected 1 option from: " + input, 1, argHelper.countOptions() );
	}

	
		public void
	testMultipleBooleans()
		throws Exception
	{
		final String	booleanOptions	= "b1 b2 b3 b4";
		final String	input			= "--b1 --b2=false --b3 --b4=false";
		
		final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
		
		assertEquals( "expected b1 to be true from: " + input,
			Boolean.TRUE, argHelper.getBoolean( "b1", null ) );
			
		assertEquals( "expected b2 to be false from: " + input,
			Boolean.FALSE, argHelper.getBoolean( "b2", null ) );
			
		assertEquals( "expected b3 to be true from: " + input,
			Boolean.TRUE, argHelper.getBoolean( "b3", null ) );
			
		assertEquals( "expected b4 to be false from: " + input,
			Boolean.FALSE, argHelper.getBoolean( "b4", null ) );
	}
	

	
	// test single boolean option
		public void
	testBooleanFailureWithEquals(  )
		throws Exception
	{
		final String	booleanOptions	= "b1";
		final String	input			= "--b1=hello";
		
		try
		{
			ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
			argHelper.getBoolean( "b1", null );
			fail( "expecte this input to fail for boolean: " + input );
		}
		catch( Exception e )
		{
		}
	}

	
	
	// test single boolean option
		public void
	testInteger0()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( "i1,1", "--i1=0" );
		
		final Integer	value	= argHelper.getInteger( "i1" );
		assertEquals(Integer.valueOf(0), value);
	}
	
	// test single boolean option
		public void
	testIntegerNegative()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( "i1,1", "--i1=-9999" );
		
		final Integer	value	= argHelper.getInteger( "i1" );
		assertEquals(Integer.valueOf(-9999), value);
	}
	
	// test single boolean option
		public void
	testIntegerPositive()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( "i1,1", "--i1=9999" );
		
		final Integer	value	= argHelper.getInteger( "i1" );
		assertEquals(Integer.valueOf(9999), value);
	}
	
		public void
	testNoOptionsNoOperands()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( "s1,1", "--s1=" );
		
		final String	value	= argHelper.getString( "s1", null);
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
		final ArgHelper	argHelper	= setupArgHelper( "b1 b2 s1,1", "--b1 --b2 --s1=hello" );
		
		assertEquals( "expecting 0 operands", 0, argHelper.getOperands().length );
		assertEquals( "expecting 3 options", 3, argHelper.countOptions() );
		
		final String	value	= argHelper.getString( "s1", null);
		assertEquals( "hello", value );
	}
	
	
		public void
	testMultipleArgsWithSpaces()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( "a1,4", "--a1 1 2 hello there op1 op2" );
		
		final String []	values	= argHelper.getArgValues( "a1" );
		assertEquals( values.length, 4 );
		assertEquals( "expected 2 operands", 2, argHelper.getOperands().length );
	}
	
	
		public void
	testMultipleArgsWithEquals()
		throws Exception
	{
		final ArgHelper	argHelper	= setupArgHelper( "a1,4", "--a1=1,2,hello,there op1 op2" );
		
		final String []	values	= argHelper.getArgValues( "a1" );
		assertEquals( "expected 4 options", values.length, 4 );
		assertEquals( "expected 2 operands", 2, argHelper.getOperands().length );
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
	testSingleCharBoolean()
		throws Exception
	{
		final String	booleanOptions	= "b";
		final String	input	= "-b";
			
		try
		{
			final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
			
			assertEquals( "expected TRUE for: " + input,
				Boolean.TRUE, argHelper.getBoolean( booleanOptions, null) );
		}
		catch( Exception e )
		{
			// we expected to get here
		}
	}
	
		public void
	testSingleCharBooleanWithValue()
		throws Exception
	{
		final String	booleanOptions	= "b";
		final String	input	= "-b=false";
			
		try
		{
			final ArgHelper	argHelper	= setupArgHelper( booleanOptions, input );
			
			assertEquals( "expected FALSE for: " + input,
				Boolean.FALSE, argHelper.getBoolean( booleanOptions, null) );
		}
		catch( Exception e )
		{
			// we expected to get here
		}
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



