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
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jcmd/util/misc/StringEscaper.java,v 1.3 2005/11/08 22:39:23 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/08 22:39:23 $
 */
 
package com.sun.cli.jcmd.util.misc;

import java.text.StringCharacterIterator;


/**
	Escapes/unescapes strings
 */
public final class StringEscaper
{
	static final public char	BACKSLASH	= '\\';
	static final public char	NEWLINE		= '\n';
	static final public char	RETURN		= '\r';
	static final public char	TAB			= '\t';
	static final public char	SPACE		= ' ';
	
	static final public char	ESCAPE_CHAR		= BACKSLASH;
	static final public char	UNICODE_START	= 'u';
	final char					mEscapeChar;
	final char[]				mCharsToEscape;
	StringCharacterIterator		mCharIter;
	
		public
	StringEscaper()
	{
		this( "\n\r\t" );
	}
	
		public
	StringEscaper( String charsToEscape )
	{
		this( ESCAPE_CHAR, charsToEscape );
	}
	
	
		public
	StringEscaper( char escapeChar, String charsToEscape )
	{
		mCharIter	= null;
		
		mEscapeChar	= escapeChar;
		
		mCharsToEscape	= new char[ 1 + charsToEscape.length() ];
		
		mCharsToEscape[ 0 ]	= ESCAPE_CHAR;
		final int	length	= charsToEscape.length();
		for( int i = 0; i < length; ++i )
		{
			mCharsToEscape[ i + 1 ]	= charsToEscape.charAt( i );
		}
	}
	
		boolean
	shouldEscape( final char c )
	{
		boolean	shouldEscape	= false;
		
		for( int i = 0; i < mCharsToEscape.length; ++i )
		{
			if ( c == mCharsToEscape[ i ] )
			{
				shouldEscape	= true;
				break;
			}
		}
		
		return( shouldEscape );
	}
	
	/*
		Get an escape sequence for the character.
	 */
		String
	getEscapeSequence( char c )
	{
		String	sequence	= null;
		
		if ( c == mEscapeChar )
		{
			sequence	= "" + mEscapeChar + mEscapeChar;
		}
		else if ( c == NEWLINE )
		{
			sequence	= mEscapeChar + "n";
		}
		else if ( c == RETURN )
		{
			sequence	= mEscapeChar + "r";
		}
		else if ( c == TAB )
		{
			sequence	= mEscapeChar + "t";
		}
		else if ( c == SPACE )
		{
			sequence	= mEscapeChar + "s";
		}
		else
		{
			final int	numericValue	= (int)c;
			
			String	valueString	= "" + Integer.toHexString( numericValue );
			// make sure it's 4 digits by prepending leading zeroes
			while ( valueString.length() != 4 )
			{
				valueString	= "0" + valueString;
			}
			
			// careful not to append char to char
			sequence	= mEscapeChar + (UNICODE_START + valueString);
		}
		
		return( sequence );
	}
	
		public String
	escape( String s)
	{
		final StringBuffer	buf	= new StringBuffer();
		
		final int length	= s.length();
		for( int i = 0; i < length; ++i )
		{
			final char	c	= s.charAt( i );
			
			if ( shouldEscape( c ) )
			{
				buf.append( getEscapeSequence( c ) );
			}
			else
			{
				buf.append( c );
			}
		}
		
		return( buf.toString() );
	}
	
		boolean
	hasMoreChars()
	{
		return( mCharIter.current() != mCharIter.DONE );
	}
	
		char
	peekNextChar()
	{
		return( mCharIter.current() );
	}

		char
	nextChar()
	{
		final char	theChar	= mCharIter.current();
		mCharIter.next();
		
		if ( theChar == mCharIter.DONE )
		{
			throw new ArrayIndexOutOfBoundsException();
		}
		
		return( theChar );
	}
	
		char
	escapeSequenceToChar()
	{
		final char	c	= nextChar();
		char	result	= 0;
		
		if ( c == mEscapeChar )
		{
			result	= mEscapeChar;
		}
		else if ( c == 'n' )
		{
			result	= NEWLINE;
		}
		else if ( c == 'r' )
		{
			result	= RETURN;
		}
		else if ( c == 't' )
		{
			result	= TAB;
		}
		else if ( c == 's' )
		{
			result	= SPACE;
		}
		else if ( c == UNICODE_START )
		{
			final String	unicodeSequence	= "" + nextChar() + nextChar() + nextChar() + nextChar();
			final int		intValue	= Integer.parseInt( unicodeSequence, 16 );
			
			result	= (char)intValue;
		}
		else
		{
			throw new IllegalArgumentException( "Illegal escape sequence" );
		}
		
		return( result );
	}
	
	
		public String
	unescape( String s )
	{
		final StringBuffer	buf	= new StringBuffer();
		
		mCharIter	= new StringCharacterIterator( s );
		
		while ( hasMoreChars() )
		{
			final char	c	= (char)nextChar();
			assert ( c != mCharIter.DONE );
			
			if ( c == mEscapeChar )
			{
				final char	newChar	= escapeSequenceToChar();
				buf.append( newChar );
			}
			else
			{
				buf.append( c );
			}
		}
		
		mCharIter	= null;
		
		return( buf.toString() );
	}
}

