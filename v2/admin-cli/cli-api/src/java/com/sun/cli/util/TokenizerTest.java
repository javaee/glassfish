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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/util/TokenizerTest.java,v 1.3 2005/12/25 03:46:00 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:46:00 $
 */
 
package com.sun.cli.util;

import java.util.Iterator;

import junit.framework.TestCase;



public class TokenizerTest extends TestCase
{
		public
	TokenizerTest( String name )
	{
		super( name );
	}
	
	public final static String	WHITE_SPACE	= " \t";
	public final static char	ESCAPE_CHAR	= '\\';
	public final static String	ESCAPABLE_CHARS	= "" + ESCAPE_CHAR + WHITE_SPACE + '\"';
	
		Tokenizer
	create( String input, boolean multipleDelimsCountAsOne )
	{
		return( new TokenizerImpl( input,
			WHITE_SPACE, multipleDelimsCountAsOne, ESCAPE_CHAR, ESCAPABLE_CHARS) );
	}
	
		Tokenizer
	create( String input )
	{
		return( new TokenizerImpl( input,
			WHITE_SPACE, false, ESCAPE_CHAR, ESCAPABLE_CHARS) );
	}
	

		public void
	testEmpty()
	{
		final Tokenizer tk	= create( "" );
		
		assertEquals( "expecting no tokens", 0, tk.getTokens().length );
		
		try
		{	
			tk.iterator().next();
			fail( "expecting exception trying to get token" );
		}
		catch( Exception e )
		{
		}
	}
	
	
		public void
	testLeadingDelim()
	{
		final String	input	= WHITE_SPACE.charAt( 0 ) + "hello";
		final String []	tokens	= create( input, false ).getTokens();
		
		assertEquals( 2, tokens.length );
	}
	
	
		public void
	testSingleDelimOnly()
	{
		final String	input	= "" + WHITE_SPACE.charAt( 0 );
		final String []	tokens	= create( input, false ).getTokens();
		
		assertEquals( 2, tokens.length );
	}
	
		public void
	testSingleToken()
	{
		final String	input	= "hello";
		final String []	tokens	= create( input, false ).getTokens();
		
		assertEquals( "expecting 1 token", 1, tokens.length );
		assertEquals( "expecting " + input, input, tokens[ 0 ] );
	}

		public void
	testMultipleTokens()
	{
		final String	input	= "hello there 1 2 3 4 5";
		final Iterator	iter	= create( input ).iterator();
		
		int		count	= 0;
		String	temp	= "";
		while ( iter.hasNext() )
		{
			++count;
			temp	= temp + " " + iter.next();
		}
		assertEquals( "expecting 7 tokens", 7, count );
		assertEquals( "expecting match", input, temp.substring( 1, temp.length() ));
	}
	
		public void
	testWhiteSpaceEquality()
	{
		final String	input1	= "hello there 1 2 3 4 5";
		final String	input2	= "hello\tthere 1\t2 3 4\t5";
		final Iterator	iter1	= create( input1 ).iterator();
		final Iterator	iter2	= create( input2 ).iterator();
		
		while ( iter1.hasNext() )
		{
			assertEquals( "expecting equal results from different white space",
				iter1.next(), iter2.next() );
		}
	}
	
	private final static char	BACKSLASH	= '\\';
	
	
		public void
	testEscapedNewlineCR()
	{
		final String	TEST	= "test" + BACKSLASH + "n" + BACKSLASH + "r";
		final String []	tokens	= create( TEST ).getTokens();
		
		assertEquals( 1, tokens.length );
		assertEquals( "test\n\r", tokens[ 0 ] );
	}
	
		public void
	testUnescapableChar()
	{
		final String	TEST	= "test" + BACKSLASH + "xyz";
		final String []	tokens	= create( TEST ).getTokens();
		
		assertEquals( 1, tokens.length );
		assertEquals( "test\\xyz", tokens[ 0 ] );
	}
	
		public void
	testEscaping()
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
	{
		final String	input	= "hello" + WHITE_SPACE.charAt( 0 );
		final String []	tokens	= create( input, false ).getTokens();
		
		assertEquals( 2, tokens.length );
	}
	
		public void
	testTrailingDelimiter2()
	{
		final String	input	= "hello" + WHITE_SPACE.charAt( 0 ) +
							"\"there\"" + WHITE_SPACE.charAt( 0 );
		final String []	tokens	= create( input, false ).getTokens();
		
		assertEquals( 3, tokens.length );
	}
	
		public void
	testMultipleDelimsWithNoData()
	{
		final String	input	= "" + WHITE_SPACE.charAt( 0 );
		final String []	tokens	= create( input, false ).getTokens();
		
		assertEquals( input.length() + 1, tokens.length );
	}
	
		public void
	testMultipleDelimsAsOne()
	{
		final String	input	= "hello" + WHITE_SPACE + "there" + WHITE_SPACE;
		final String []	tokens	= create( input, true ).getTokens();
		
		assertEquals( 3, tokens.length );
	}
	
		public void
	testQuotedString()
	{
		final String	input	= "\"hello there\" \"another\" \"3\" \"words\" \"\"";
		final String []	tokens	= create( input ).getTokens();
		
		assertEquals( 5, tokens.length );
		assertEquals( "hello there", tokens[ 0 ] );
		assertEquals( "another", tokens[ 1 ] );
		assertEquals( "3", tokens[ 2 ] );
		assertEquals( "words", tokens[ 3 ] );
		assertEquals( "", tokens[ 4 ] );
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



