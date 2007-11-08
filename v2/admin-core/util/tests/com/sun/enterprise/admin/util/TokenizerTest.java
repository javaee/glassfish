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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

 
/*
 * $Header: /cvs/glassfish/admin-core/util/tests/com/sun/enterprise/admin/util/TokenizerTest.java,v 1.2 2005/12/25 03:53:29 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2005/12/25 03:53:29 $
 */
 
package com.sun.enterprise.admin.util;

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
		return( new TokenizerImpl( input,
			WHITE_SPACE, multipleDelimsCountAsOne, ESCAPE_CHAR, ESCAPABLE_CHARS) );
	}
	
		Tokenizer
	create( String input )
		throws TokenizerException
	{
		return( new TokenizerImpl( input,
			WHITE_SPACE, false, ESCAPE_CHAR, ESCAPABLE_CHARS) );
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
		final String[]	tokens	= new TokenizerImpl( input,
									".", false, BACKSLASH, BACKSLASH + "." ).getTokens();
									
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
			create( input2 ).getTokens();
			fail( "expected to fail: " + input2);
			create( input3 ).getTokens();
			fail( "expected to fail: " + input3);
		}
		catch( TokenizerException e )
		{
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



