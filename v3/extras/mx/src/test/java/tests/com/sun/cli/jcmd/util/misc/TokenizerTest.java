/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jcmd/util/misc/TokenizerTest.java,v 1.3 2003/12/12 20:08:40 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2003/12/12 20:08:40 $
 */
 
package com.sun.cli.jcmd.util.misc;

import java.util.Iterator;

import junit.framework.TestCase;



public class TokenizerTest extends TestCase
{
		public
	TokenizerTest( String name )
	{
		super( name );
	}
	
	public final static char	TAB			= '\t';
	public final static char	BACKSLASH	= '\\';
	public final static char	QUOTE_CHAR	= '\"';
	public final static String	WHITE_SPACE	= " " + TAB;
	public final static char	ESCAPE_CHAR	= BACKSLASH;
	public final static String	ESCAPABLE_CHARS	= "" + ESCAPE_CHAR + WHITE_SPACE + QUOTE_CHAR;
	
		Tokenizer
	create( String input, boolean multipleDelimsCountAsOne )
		throws TokenizerException
	{
		final TokenizerParams	params	= new TokenizerParams();
		params.mDelimiters	= WHITE_SPACE;
		params.mMultipleDelimsCountAsOne	= multipleDelimsCountAsOne;
		params.mEscapeChar		= ESCAPE_CHAR;
		params.ensureDelimitersEscapable();
		
		return( new TokenizerImpl( input, params ) );
	}
	
		Tokenizer
	create( String input )
		throws TokenizerException
	{
		return( create( input, false ) );
	}
	
		public void
	testEmpty()
		throws TokenizerException
	{
		final Tokenizer tk	= create( "" );
		
		assertEquals( "expecting no tokens", 0, tk.getTokens().length );
	}
	
	
		public void
	testLeadingDelim()
		throws TokenizerException
	{
		final String	input	= WHITE_SPACE.charAt( 0 ) + "hello";
		final String []	tokens	= create( input, false ).getTokens();
		
		assertEquals( 2, tokens.length );
		assertEquals( "", tokens[ 0 ] );
		assertEquals( "hello", tokens[ 1 ] );
	}
	
	
		public void
	testSingleDelimOnly()
		throws TokenizerException
	{
		final String	input	= "" + WHITE_SPACE.charAt( 0 );
		final String []	tokens	= create( input, false ).getTokens();
		
		assertEquals( 2, tokens.length );
		assertEquals( "", tokens[ 0 ] );
		assertEquals( "", tokens[ 1 ] );
	}
	
		public void
	testOnlyDelims()
		throws TokenizerException
	{
		final String	input	= WHITE_SPACE + WHITE_SPACE + WHITE_SPACE;
		final String []	tokens	= create( input, false ).getTokens();
		
		assertEquals( (WHITE_SPACE.length() * 3) + 1, tokens.length );
	}
	
		public void
	testSingleToken()
		throws TokenizerException
	{
		final String	input	= "hello";
		final String []	tokens	= create( input, false ).getTokens();
		
		assertEquals( "expecting 1 token", 1, tokens.length );
		assertEquals( "expecting " + input, input, tokens[ 0 ] );
	}

		public void
	testMultipleTokens()
		throws TokenizerException
	{
		final String	input	= "hello there 1 2 3 4 5";
		final String[]	tokens	= create( input ).getTokens();
		
		assertEquals( "expecting 7 tokens", 7, tokens.length );
		assertEquals( tokens[ 0 ], "hello" );
		assertEquals( tokens[ 1 ], "there" );
		assertEquals( tokens[ 2 ], "1" );
		assertEquals( tokens[ 3 ], "2" );
		assertEquals( tokens[ 4 ], "3" );
		assertEquals( tokens[ 5 ], "4" );
		assertEquals( tokens[ 6 ], "5" );
	}
	
		public void
	testWhiteSpaceEquality()
		throws TokenizerException
	{
		final String	input1	= "hello there 1 2 3 4 5";
		final String	input2	= "hello\tthere 1\t2 3 4\t5";
		final String[]	tokens1	= create( input1 ).getTokens();
		final String[]	tokens2	= create( input2 ).getTokens();
		
		
		assertEquals( tokens1.length, tokens2.length );
		for( int i = 0; i < tokens1.length; ++i )
		{
			assertEquals( tokens1[ i ], tokens2[ i ] );
		}
	}
	
		public void
	testEscapedNewlineCR()
		throws TokenizerException
	{
		final String	TEST	= "test" + BACKSLASH + "n" + BACKSLASH + "r" + BACKSLASH + "t";
		final String []	tokens	= create( TEST ).getTokens();
		
		assertEquals( 1, tokens.length );
		assertEquals( "test\n\r\t", tokens[ 0 ] );
	}
	
		public void
	testEscaping()
		throws TokenizerException
	{
		// create a String which each escapable character is represented
		final StringBuffer	b	= new StringBuffer();
		for( int i = 0; i < ESCAPABLE_CHARS.length(); ++i )
		{
			b.append( "\\" + ESCAPABLE_CHARS.charAt( i ) );
		}
		final String []	tokens	= create( b.toString() ).getTokens();
		
		assertEquals( "expecting 1 token", 1, tokens.length );
		assertEquals( "expecting match", ESCAPABLE_CHARS, tokens[ 0 ] );
	}
	
	
		public void
	testTrailingDelimiter1()
		throws TokenizerException
	{
		final String	input	= "hello" + WHITE_SPACE.charAt( 0 );
		final String []	tokens	= create( input, false ).getTokens();
		
		assertEquals( 2, tokens.length );
	}
	
		public void
	testTrailingDelimiter2()
		throws TokenizerException
	{
		final String	input	= "hello" + WHITE_SPACE.charAt( 0 ) +
							"\"there\"" + WHITE_SPACE.charAt( 0 );
		final String []	tokens	= create( input, false ).getTokens();
		
		assertEquals( 3, tokens.length );
	}
	
		public void
	testMultipleDelimsWithNoData()
		throws TokenizerException
	{
		final String	input	= "" + WHITE_SPACE.charAt( 0 );
		final String []	tokens	= create( input, false ).getTokens();
		
		assertEquals( input.length() + 1, tokens.length );
	}
	
	
		public void
	testMultipleDelimsAsOne()
		throws TokenizerException
	{
		final String	input	= "HELLO" + WHITE_SPACE + "THERE" + WHITE_SPACE + WHITE_SPACE;
		final String []	tokens	= create( input, true ).getTokens();
		
		assertEquals( 3, tokens.length );
	}

	static final char	QUOTE	= '\"';
		static String
	quote( String s )
	{
		return( QUOTE + s + QUOTE );
	}
	

		public void
	testEmptyQuotedString()
		throws TokenizerException
	{
		final String	input	= quote( "" );
		final String []	tokens	= create( input ).getTokens();
		
		assertEquals( 1, tokens.length );
		assertEquals( "", tokens[ 0 ] );
	}
		public void
	testEmptyQuotedStrings()
		throws TokenizerException
	{
		final String	input	= quote( "" ) + quote( "" ) + quote( "" ) + quote( "" );
		final String []	tokens	= create( input ).getTokens();
		
		assertEquals( 1, tokens.length );
		assertEquals( "", tokens[ 0 ] );
	}
	
		public void
	testEmptyQuotedStringsSeparatedByDelim()
		throws TokenizerException
	{
		final String	input	= quote( "" ) + WHITE_SPACE.charAt( 0 ) + quote( "" );
		final String []	tokens	= create( input ).getTokens();
		
		assertEquals( 2, tokens.length );
	}
	
		public void
	testAdjacentQuotedStrings()
		throws TokenizerException
	{
		final String	input	= quote( "xxx" ) + quote( "yyy" ) + quote( "" );
		final String []	tokens	= create( input ).getTokens();
		
		assertEquals( 1, tokens.length );
		assertEquals( "xxxyyy", tokens[ 0 ] );
	}
	
		public void
	testQuotedString()
		throws TokenizerException
	{
		final String	input	= quote( "hello there" ) + " " + quote( "another" ) + " " +
			quote( "3" ) + " " + quote( "words" ) + " ";
		final String []	tokens	= create( input ).getTokens();
		
		assertEquals( 5, tokens.length );
		assertEquals( "hello there", tokens[ 0 ] );
		assertEquals( "another", tokens[ 1 ] );
		assertEquals( "3", tokens[ 2 ] );
		assertEquals( "words", tokens[ 3 ] );
		assertEquals( "", tokens[ 4 ] );
	}
	
		public void
	testQuotedDelim()
		throws TokenizerException
	{
		final String	input	= quote( " " ) + " " + quote( " " ) + BACKSLASH + " ";
		final String []	tokens	= create( input ).getTokens();
		
		assertEquals( 2, tokens.length );
		assertEquals( " ", tokens[ 0 ] );
		assertEquals( "  ", tokens[ 1 ] );
	}
	
	
		public void
	testTrailingEscapedDelim()
		throws TokenizerException
	{
		final String	input	= "x." + BACKSLASH + ".";
		final TokenizerParams	params	= new TokenizerParams();
		params.mDelimiters	= ".";
		params.mEscapableChars	= ".";
		final String[]	tokens	= new TokenizerImpl( input, params ).getTokens();
									
		assertEquals( 2, tokens.length );
		assertEquals( "x", tokens[ 0 ] );
		assertEquals( ".", tokens[ 1 ] );
	}
	
		public void
	testLegalUnicodeSequence()
		throws TokenizerException
	{
		final String	input	= BACKSLASH + "u0020";	// unicode for the space char
		
		final String[]	tokens	= create( input ).getTokens();
		assertEquals( 1, tokens.length );
		assertEquals( " ", tokens[ 0 ] );
	}
	
		public void
	testIllegalUnicodeSequence()
		throws TokenizerException
	{
		final String	input1	= BACKSLASH + "u";
		final String	input2	= input1 + "zzzz";
		final String	input3	= input1 + "abcx";
		
		try
		{
			create( input1 ).getTokens();
			fail( "expected to fail: " + input1);
		}
		catch( TokenizerException e )
		{
		}
		
		try
		{
			create( input2 ).getTokens();
			fail( "expected to fail: " + input2);
		}
		catch( TokenizerException e )
		{
		}
		
		
		try
		{
			create( input3 ).getTokens();
			fail( "expected to fail: " + input3);
		}
		catch( TokenizerException e )
		{
		}
		
		// now verify that it doesn't fail if we specify that invalid
		// sequences are to be emitted as literals.
		
		final TokenizerParams	params	= new TokenizerParams();
		params.mEmitInvalidEscapeSequencesLiterally	= true;
		try
		{
			String[]	tokens	= new TokenizerImpl( input1, params ).getTokens();
			assertEquals( 1, tokens.length );
			assertEquals( input1, tokens[ 0 ] );
			
			tokens	= new TokenizerImpl( input2, params ).getTokens();
			assertEquals( 1, tokens.length );
			assertEquals( input2, tokens[ 0 ] );
			
			tokens	= new TokenizerImpl( input3, params ).getTokens();
			assertEquals( 1, tokens.length );
			assertEquals( input3, tokens[ 0 ] );
		}
		catch( TokenizerException e )
		{
			fail( "did not expect an exception: " + e.getClass().getName() );
		}
	}
	
	
		public void
	testIllegalEscapeSequence()
		throws TokenizerException
	{
		final String	input	= BACKSLASH + "x";
		try
		{
			final String []	tokens	= create( input ).getTokens();
			fail( "expected to fail: " + input);
		}
		catch( TokenizerException e )
		{
		}
	}

		public void
	testUnterminatedLiteralString()
		throws TokenizerException
	{
		final String	input1	= "" + QUOTE;
		final String	input2	= QUOTE + "xxx";
		final String	input3	= "xxx" + QUOTE;
		try
		{
			create( input1 ).getTokens();
			fail( "expected to fail: " + input1);
			
			create( input2 ).getTokens();
			fail( "expected to fail: " + input2);
			
			create( input3 ).getTokens();
			fail( "expected to fail: " + input3);
		}
		catch( TokenizerException e )
		{
		}
	}


	//-------------------------------------------------------------------------


	
		protected void
	setUp()
	{
	}
	
		protected void
	tearDown()
	{
	}
	
};



