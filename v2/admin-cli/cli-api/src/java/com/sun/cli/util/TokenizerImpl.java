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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/util/TokenizerImpl.java,v 1.3 2005/12/25 03:46:00 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:46:00 $
 */

package com.sun.cli.util;

import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;


public final class TokenizerImpl implements Tokenizer
{
	final String []			mTokens;
	
		public
	TokenizerImpl(
		String		input,
		String		delimiters,
		char		escapeChar,
		String		escapableChars)
	{
		this( input, delimiters, true, escapeChar, escapableChars );
	}
	
		public
	TokenizerImpl(
		String		input,
		String		delimiters,
		boolean		multipleDelimsCountAsOne,
		char		escapeChar,
		String		escapableChars)
	{
		final TokenizerInternal	worker = new TokenizerInternal( input, delimiters,
			multipleDelimsCountAsOne, escapeChar, escapableChars );
		
		mTokens	= worker.parseTokens();
	}
	
		public String []
	getTokens()
	{
		return( mTokens );
	}
	
		public Iterator
	iterator()
	{
		return( Arrays.asList( getTokens() ).listIterator() );
	}
}



final class TokenizerInternal
{
	final String			mInput;
	final String			mDelimiters;
	final boolean			mMultipleDelimsCountAsOne;
	final char				mEscapeChar;
	final String			mEscapableChars;
	final StringCharacterIterator	mIter;
	
	
	
		private static void
	dm(	Object msg	)
	{
		System.out.println( msg.toString() );
	}
	
		public
	TokenizerInternal(
		String		input,
		String		delimiters,
		char		escapeChar,
		String		escapableChars)
	{
		this( input, delimiters, true, escapeChar, escapableChars );
	}
	
		public
	TokenizerInternal(
		String		input,
		String		delimiters,
		boolean		multipleDelimsCountAsOne,
		char		escapeChar,
		String		escapableChars)
	{
		checkEscapableChars( escapableChars );
		
		mInput			= input;
		mDelimiters		= delimiters;
		mMultipleDelimsCountAsOne	= multipleDelimsCountAsOne;
		mEscapeChar		= escapeChar;
		mEscapableChars	= escapableChars;
		mIter		= new StringCharacterIterator( input );
	}
	
	/*
		Some characters may not be escaped, hex digits in particular
		because they can also be used as character codes.
	 */
		private static void
	checkEscapableChars( final String escapableChars )
	{
		final int	length	= escapableChars.length();
		
		for( int i = 0; i < length; ++i )
		{
			if ( isHexDigit( escapableChars.charAt( i ) ) )
			{
				throw new IllegalArgumentException();
			}
		}
	}
	
		String []
	parseTokens()
	{
		final ArrayList	list	= new ArrayList();
		
		while ( hasMoreChars() )
		{
			list.add( nextToken() );
		}
		
		// if we had a trailing delimiter, there is an empty token following it
		// that normal parsing will not produce, since the end of the input
		// has been reached.
		final int	inputLength	= mInput.length();
		if ( inputLength != 0 &&
			isDelim( mDelimiters, mInput.charAt( inputLength -1 ) ) )
		{
			list.add( "" );
		}
		
		final String []	tokens	= new String[ list.size() ];
		
		return( (String []) list.toArray( tokens ) );
	}
	
		boolean
	isSpecialEscapeChar( char theChar )
	{
		// carriage return or newline
		return( theChar == 'n' || theChar == 'r' );
	}
	
		boolean
	isCallerProvidedEscapeChar( char theChar )
	{
		return( mEscapableChars.indexOf( theChar ) >= 0 || theChar == mEscapeChar );
	}
	
		boolean
	isEscapableChar( char theChar )
	{
		return( isCallerProvidedEscapeChar( theChar ) || isSpecialEscapeChar( theChar ) );
	}
	
		boolean
	isDelim( String delims, char theChar )
	{
		return( delims != null &&
			delims.indexOf( theChar ) >= 0 || theChar == mIter.DONE );
	}
	
		static boolean
	isDigit( char theChar )
	{
		return( (theChar >= '0' && theChar <= '9') );
	}
	

		static boolean
	isHexDigit( char theChar )
	{
		return( isDigit( theChar ) || (theChar >= 'a' && theChar <= 'f') || 
			(theChar >= 'A' && theChar <= 'F') );
	}
	
		boolean
	hasMoreChars()
	{
		return( mIter.current() != mIter.DONE );
	}
	
		char
	peekNextChar()
	{
		return( mIter.current() );
	}

		char
	nextChar()
	{
		final char	theChar	= mIter.current();
		mIter.next();
		
		return( theChar );
	}
	
		void
	skipDelim( String delimiters )
	{
		while ( hasMoreChars() && isDelim( delimiters, peekNextChar() ) )
		{
			nextChar();	// skip it
		}
	}
	
		char
	getEscapedChar( final char inputChar )
	{
		char	outChar	= 0;
		
		if ( isCallerProvidedEscapeChar( inputChar ) )
		{
			outChar	= inputChar;
		}
		else
		{
			if ( inputChar == 'n' )
			{
				outChar	= '\n';
			}
			else if ( inputChar == 'r' )
			{
				outChar	= '\r';
			}
			else
			{
				assert( false );
			}
		}
		
		return( outChar );
	}
	
		char
	handleEscapeChar()
	{
		char	resultChar	= mEscapeChar;
		
		// retain starting position in case it's not a real escaped char
		final int		curIndex	= mIter.getIndex();
		boolean			valid		= false;
		
		final char	nextChar	= nextChar();
		if ( isEscapableChar( nextChar )  )
		{
			resultChar	= getEscapedChar( nextChar );
			valid	= true;
		}
		else
		{
			// if valid hexadecimal, convert two hex digits to a number
			if ( isHexDigit( nextChar ) )
			{
				final char nextNextChar	= nextChar();
				if ( isHexDigit( nextNextChar ) )
				{
					final int	newChar	= (((int)nextChar) << 4) + (int)nextNextChar;
					resultChar	= (char)newChar;
					valid	= true;
				}
			}
		}
		
		if ( ! valid )
		{
			assert( resultChar == mEscapeChar );
			mIter.setIndex( curIndex );
		}
		
		return( resultChar );
	}
	
		String
	parseLiteralString( String delimiters )
	{
		// must start with the string delimiter
		assert( peekNextChar() == Tokenizer.LITERAL_STRING_DELIM );
		nextChar();	// skip it
		
		// did we find a trailing end-of-string delimiter?
		boolean	foundEndDelim	= false;
		
		// escaping still in force, but delimiters are defeated until the string delim
		// is reached.
		StringBuffer tok	= new StringBuffer();
	
		while ( hasMoreChars()  )
		{
			final char	theChar	= nextChar();
			
			if ( theChar == mEscapeChar )
			{
				final char escapedChar	= handleEscapeChar();
				
				tok.append( escapedChar );
			}
			else if ( theChar == Tokenizer.LITERAL_STRING_DELIM )
			{
				// end of the literal string if there are no more chars or the next char
				// is a delimter
				if ( ! hasMoreChars() )
				{
					foundEndDelim	= true;
				}
				else if ( isDelim( delimiters, peekNextChar() ) )
				{
					foundEndDelim	= true;
					nextChar();
				}
				break;
			}
			else
			{
				tok.append( theChar );
			}
		}
		
		if ( ! foundEndDelim )
		{
			// if we didn't find an ending delimter, treat the start one as a literal
			return( '\"' + tok.toString() );
		}
		
		return( tok.toString() );
	}

		String
	parseToken( String delimiters )
	{
		final char	escapeChar	= mEscapeChar;
		StringBuffer tok	= new StringBuffer();
	
		while ( hasMoreChars()  )
		{
			final char	theChar	= nextChar();
			
			if ( isDelim( delimiters, theChar ) )
			{
				break;
			}
			
			if ( theChar == escapeChar )
			{
				final char escapedChar	= handleEscapeChar();
				
				tok.append( escapedChar );
			}
			else
			{
				tok.append( theChar );
			}
		}
		
		return( tok.toString() );
	}
	
		String
	nextToken( )
	{
		if ( ! hasMoreChars() )
		{
			throw new IllegalArgumentException( "no more tokens available" );
		}
		
		String	tok	= null;
		
		if ( peekNextChar() == Tokenizer.LITERAL_STRING_DELIM )
		{
			tok	= parseLiteralString( mDelimiters );
		}
		else
		{
			tok	= parseToken( mDelimiters );
		}
		// a single delimiter following the token as been consumed
		
		if ( mMultipleDelimsCountAsOne )
		{
			skipDelim( mDelimiters );
		}
		
		return( tok );
	}
}

