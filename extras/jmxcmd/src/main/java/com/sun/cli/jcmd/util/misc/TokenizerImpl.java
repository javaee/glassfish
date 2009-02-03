/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
package com.sun.cli.jcmd.util.misc;

import java.text.StringCharacterIterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;


class IllegalEscapeSequenceException extends TokenizerException
{
    static final long serialVersionUID = 0;
    
	public	IllegalEscapeSequenceException( String msg )	{ super( msg ); }
}

final class UnterminatedLiteralStringException extends TokenizerException
{
    static final long serialVersionUID = 0;
	public	UnterminatedLiteralStringException( String msg )	{ super( msg ); }
}

final class MalformedUnicodeSequenceException extends IllegalEscapeSequenceException
{
    static final long serialVersionUID = 0;
	public	MalformedUnicodeSequenceException( String msg )	{ super( msg ); }
}

/**
 */
public final class TokenizerImpl implements Tokenizer
{
	final String[]		mTokens;
	
		public
	TokenizerImpl( String	input )
		throws TokenizerException
	{
		this( input, new TokenizerParams() );
	}
	
	private static final char	QUOTE_CHAR	= '\"';
	
		public
	TokenizerImpl(
		String			input,
		TokenizerParams	params )
		throws TokenizerException
	{
		final TokenizerInternal	worker = new TokenizerInternal( input, params );
	
		List<Object>	allTokens	= worker.parseTokens( );

		if ( params.mMultipleDelimsCountAsOne )
		{
			allTokens	= removeMultipleDelims( allTokens );
		}
		
		mTokens	= interpretTokenList( allTokens );
	}
	
		final static List<Object>
	removeMultipleDelims( List<Object> list )
	{
		final List<Object>		resultList	= new ArrayList<Object>();
		
		boolean	lastWasDelim	= false;
		for( final Object value : list )
		{
			if ( value instanceof String )
			{
				resultList.add( value );
				lastWasDelim	= false;
			}
			else if ( ! lastWasDelim )
			{
				// add the delimiter
				resultList.add( value );
				lastWasDelim	= true;
			}
		}
		
		return( resultList );
	}
	
	/**
		Interpret the parsed token list, which consists of a series of strings
		and tokens.  We need to handle the special cases where the list starts
		with a delimiter and/or ends with a delimiter.  Examples:
		
		""	=> {}
		"."	=> { "", "" }
		"..."	=> { "", "", "", "" }
		"x."	=> { "x", "" }
		".x"	=> { "", "x" }
		"y.x"	=> { "y", "x" }
	 */
		static String[]
	interpretTokenList( final List<Object> list )
	{
		final List<String>		resultList	= new ArrayList<String>();

		boolean	lastWasDelim	= true;
		for( final Object value : list )
		{
			if ( value instanceof String )
			{
				resultList.add( (String)value );
				lastWasDelim	= false;
			}
			else
			{
				if ( lastWasDelim )
				{
					// this one's a delimiter, and so was the last one
					// insert the implicit empty string
					resultList.add( "" );
				}
				else
				{
					lastWasDelim	= true;
				}
			}
		}
		
		// a trailing delimiter implies an empty string after it
		if ( lastWasDelim && list.size() != 0 )
		{
			resultList.add( "" );
		}
		
		return( (String[])resultList.toArray( new String[ resultList.size() ] ) );
	}
	
		public String []
	getTokens()
	{
		return( mTokens );
	}
}



final class TokenizerInternal
{
	final String					mInput;
	final TokenizerParams			mParams;
	final StringCharacterIterator	mIter;
	
	// a distinct object used to denote a delimiter
	private static final class Delim
	{
		private Delim()	{}
		public static Delim	getInstance()	{ return( new Delim() ); }
		public String	toString() { return( "<DELIM>" ); }
	}
	final static Delim	DELIM	= Delim.getInstance();
	
		
	TokenizerInternal(
		String			input,
		TokenizerParams	params )
	{
		mInput			= input;
		mParams			= params;
		mIter		= new StringCharacterIterator( input );
	}
	
		private static boolean
	isSpecialEscapeChar( char theChar )
	{
		// carriage return or newline
		return( theChar == 'n' || theChar == 'r' || theChar == 't' ||theChar == QUOTE_CHAR );
	}
	
		private boolean
	isCallerProvidedEscapableChar( char theChar )
	{
		return( mParams.mEscapableChars.indexOf( theChar ) >= 0 ||
			theChar == mParams.mEscapeChar );
	}
	
		private boolean
	isEscapableChar( char theChar )
	{
		return( isCallerProvidedEscapableChar( theChar ) || isSpecialEscapeChar( theChar ) );
	}
	
		private boolean
	isDelim( String delims, char theChar )
	{
		return( delims.indexOf( theChar ) >= 0 || theChar == mIter.DONE );
	}
	
		private static boolean
	isDigit( char theChar )
	{
		return( (theChar >= '0' && theChar <= '9') );
	}
	

		private static boolean
	isHexDigit( char theChar )
	{
		return( isDigit( theChar ) || (theChar >= 'a' && theChar <= 'f') || isUpper( theChar ) );
	}
	
		private static boolean
	isUpper( char c )
	{
		return( (c >= 'A' && c <= 'F') );
	}
	
		private boolean
	hasMoreChars()
	{
		return( mIter.current() != mIter.DONE );
	}
	
		private int
	getIndex()
	{
		return( mIter.getIndex() );
	}
	
		private char
	setIndex( int index )
	{
		return( mIter.setIndex( index ) );
	}

		private char
	nextChar()
	{
		final char	theChar	= mIter.current();
		mIter.next();
		
		return( theChar );
	}
	
	private static final char	QUOTE_CHAR	= '\"';
	private static final char	TAB_CHAR	= '\t';
	
		private char
	decodeUnicodeSequence()
		throws MalformedUnicodeSequenceException
	{
		int		value	= 0;
		
		try
		{
			for( int i = 0; i < 4; ++i )
			{
				value	= (value << 4 ) | hexValue( nextChar() );
			}
		}
		catch( Exception e )
		{
			throw new MalformedUnicodeSequenceException( "" );
		}
		
		return( (char)value );
	}
	
		private static int
	hexValue( char c )
	{
		if ( ! isHexDigit( c ) )
		{
			throw new IllegalArgumentException();
		}
		
		int	value	= 0;

		if ( isDigit( c ) )
		{
			value	= (int)c - (int)'0';
		}
		else if ( isUpper( c ) )
		{
			value	= (int)c - (int)'A';
		}
		else
		{
			value	= (int)c - (int)'a';
		}
		return( value );
	}
	
		private char
	getEscapedChar( final char inputChar )
		throws MalformedUnicodeSequenceException,IllegalEscapeSequenceException
	{
		char	outChar	= 0;
		
		if ( isCallerProvidedEscapableChar( inputChar ) )
		{
			outChar	= inputChar;
		}
		else
		{
			switch( inputChar )
			{
				default:	throw new IllegalEscapeSequenceException( "" + inputChar );
				case 'n':	outChar	= '\n';		break;
				case 'r':	outChar	= '\r';		break;
				case 't':	outChar	= '\t';		break;
				case QUOTE_CHAR:	outChar	= QUOTE_CHAR;	break;
				case 'u':	outChar	= decodeUnicodeSequence();	break;
			}
		}
		
		return( outChar );
	}
	
	
		private String
	processEscapeSequence()
	{
		// index of the character following the escape character
		String	s	= null;
		
		final char	theChar	= nextChar();
		final int	continuePos	= mIter.getIndex();
		try
		{
			s	= "" + getEscapedChar( theChar );
		}
		catch( TokenizerException e )
		{
			// emit the escape character and the character following it]
			// literally, then proceed.
			s	= mParams.mEscapeChar + "" + theChar;
			mIter.setIndex( continuePos );
		}
		
		return( s );
	}
	
		List<Object>
	parseTokens(   )
		throws UnterminatedLiteralStringException,
			MalformedUnicodeSequenceException, IllegalEscapeSequenceException
	{
		final StringBuffer	tok	= new StringBuffer();
		final List<Object>	tokens	= new ArrayList<Object>();
		boolean				insideStringLiteral	= false;
		
		/**
			Escape sequences are always processed regardless of whether we're inside a
			quoted string or not.  A quote string really only alters whether delimiters
			are treated as literal characters, or not.
		 */
		while ( hasMoreChars()  )
		{
			final char	theChar	= nextChar();
			
			if ( theChar == mParams.mEscapeChar )
			{
				if ( mParams.mEmitInvalidEscapeSequencesLiterally )
				{
					tok.append( processEscapeSequence() );
				}
				else
				{
					tok.append( getEscapedChar( nextChar() ) );
				}
			}
			else if ( theChar == Tokenizer.LITERAL_STRING_DELIM )
			{
				// special cases of "", """", """""", etc require forcing an empty string out
				// these case have no delimiter or regular characters to cause a string to
				// be emitted
				if ( insideStringLiteral && tok.length() == 0 && tokens.size() == 0)
				{
					tokens.add( "" );
				}
				
				insideStringLiteral	= ! insideStringLiteral;
			}
			else if ( insideStringLiteral )
			{
				tok.append( theChar );
			}
			else if ( isDelim( mParams.mDelimiters, theChar ) )
			{
				// we've hit a delimiter...if characters have accumulated, spit them out
				// then spit out the delimiter token.
				if ( tok.length() != 0 )
				{
					tokens.add( tok.toString() );
					tok.setLength( 0 );
				}
				tokens.add( DELIM );
			}
			else
			{
				tok.append( theChar );
			}
		}
		
		if ( tok.length() != 0 )
		{
			tokens.add( tok.toString() );
		}
		
		if ( insideStringLiteral )
		{
			throw new UnterminatedLiteralStringException( tok.toString() );
		}
		
		return( tokens );
	}
}

