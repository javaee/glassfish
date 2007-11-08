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
 * $Header: /cvs/glassfish/admin/mbeans/src/java/com/sun/enterprise/admin/dottedname/DottedName.java,v 1.5 2007/02/14 04:11:30 bnevins Exp $
 * $Revision: 1.5 $
 * $Date: 2007/02/14 04:11:30 $
 */
package com.sun.enterprise.admin.dottedname;

import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import com.sun.enterprise.admin.util.Tokenizer;
import com.sun.enterprise.admin.util.TokenizerImpl;


class SpecialChars
{
	public final static String WILDCARDS			= "*";
	public final static char BACKSLASH				= '\\';
	public final static String SPECIALS				= "(){}[];<>@$#";
	public final static String LEGAL_CHARS			= "abcdefghijklmnopqrstuvwxyz" +
													  "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
													  "0123456789" +
													  "-_./" +
													  BACKSLASH +
													  WILDCARDS + 
													  SPECIALS;
};

/*
 */
final class ParsedDottedName implements java.io.Serializable
{
	public final String		mDomain;
	public final String		mScope;
	public final List		mParts;
	
		public
	ParsedDottedName( String domain, String scope, List parts )
	{
		if ( domain.length() != 0 )
		{
			checkLegalNamePart( domain );
		}

		checkLegalNamePart( scope );
	
		for( int i = 0; i < parts.size(); ++i )
		{
			checkLegalNamePart( (String)parts.get( i ) );
		}
		
		mDomain		= domain;
		mScope		= scope;
		mParts		= Collections.unmodifiableList( parts );
	}
	
	
		boolean
	isLegalChar( final char theChar )
	{
		return( SpecialChars.LEGAL_CHARS.indexOf( theChar ) >= 0 );
	}
	
		void
	checkLegalChar( final char theChar )
	{
		if ( ! isLegalChar( theChar ) )
		{
			final String	msg	= DottedNameStrings.getString(
					DottedNameStrings.ILLEGAL_CHARACTER_KEY,
					"'" + theChar + "'" );
					
			throw new IllegalArgumentException( msg );
		}
	}
	
		void
	checkLegalNamePart( String part )
	{
		final int length	= part.length();
		
		if ( length == 0 )
		{
			final String	msg	= DottedNameStrings.getString(
					DottedNameStrings.MISSING_EXPECTED_NAME_PART_KEY );
					
			throw new IllegalArgumentException( msg );
		}
	
		for( int i = 0; i < length; ++i )
		{
			final char	theChar	= part.charAt( i );
			
			checkLegalChar( theChar );
		}
	}
	
		public int
	hashCode()
	{
        // trying to come up with a good hash code
        // see Effective Java, pp 36-41

        int hashcode = 17;
        hashcode = 37 * hashcode + mDomain.hashCode();
        hashcode = 37 * hashcode + mScope.hashCode();
        hashcode = 37 * hashcode + mParts.hashCode();
        
        return hashcode;
        
    }
        
		public boolean
	equals( Object other )
	{
		boolean	equals	= false;
		
		if ( ! (other instanceof ParsedDottedName) )
		{
			equals	= false;
		}
		else if ( other == this )
		{
			equals	= true;
		}
		else
		{
			equals	= this.toString().equals( other.toString() );
		}
		
		return( equals );
	}
	
		public String
	toString()
	{
		return( DottedName.toString( mDomain, mScope, mParts ) );
	}
}

/*
	Represents a dotted name.  The dotted name consists of a domain (usually empty),
	a scope (server name, config name, "domain") and parts.
	
	Parts is parts--it is up to the user to decide if the last part is the name of a value
	or if the dotted name is a prefix to which value names may be appended.
 */
public final class DottedName implements java.io.Serializable
{
	final String			mSourceString;
	final ParsedDottedName	mParsed;
	
	public final static String WILDCARDS			= SpecialChars.WILDCARDS;
	public final static char BACKSLASH				= SpecialChars.BACKSLASH;
	public final static String SPECIALS				= SpecialChars.SPECIALS;
	public final static String LEGAL_CHARS			= SpecialChars.LEGAL_CHARS;
													  
		public
	DottedName( String sourceString )
	{
		mSourceString	= sourceString;
		try
		{
			mParsed	= parse( sourceString );
		}
		catch( com.sun.enterprise.admin.util.TokenizerException e )
		{
			final String	msg	= DottedNameStrings.getString(
					DottedNameStrings.MALFORMED_DOTTED_NAME_KEY,
					sourceString );
					
			throw new IllegalArgumentException( msg );
		}
		
		checkWellFormed( sourceString, toStringFromParsed( mParsed ) );
	}
	
	/*
		Certain malformed escape constructs will parse OK, but are not acceptable
		as dotted names.  Example: "Foo\Bar" is acceptable to the Tokenizer, but
		must be written as "Foo\\Bar" to be a valid dotted name.
	 */
		private void
	checkWellFormed( String sourceString, String correctValue )
	{
		if ( ! sourceString.equals( correctValue ) )
		{
			final String	msg	= DottedNameStrings.getString(
					DottedNameStrings.MALFORMED_DOTTED_NAME_KEY,
					sourceString );
					
			throw new IllegalArgumentException( msg );
		}
	}
	
		public static String
	toString( DottedName dn, int numParts )
	{
		return( toString( dn.getDomain(), dn.getScope(), dn.getParts(), numParts ));
	}
	
		public static String
	toString( String domain, String scope, List parts )
	{
		return( toString( domain, scope, parts, parts.size() ));
	}
	
		public static String
	toString( String domain, String scope, List parts, int numParts )
	{
		final StringBuffer	buf	= new StringBuffer();
		
		if ( domain.length() != 0 )
		{
			buf.append( domain + ":" );
		}
		
		buf.append( escapePart( scope ) );
		
		for( int i = 0; i < numParts; ++ i )
		{
			final String	unescapedPart	= (String)parts.get( i );
			
			buf.append( "." + escapePart( unescapedPart ) );
		}
		
		return( buf.toString() );
	}
	
		public String
	toString()
	{
		return( mSourceString );
	}

        public static String
    toString(final String domain,
             final String scope,
             final List parts,
             final boolean needsEscaping )
    {
        if(needsEscaping){
            return toString(domain,scope,parts);
        }

        final StringBuffer	buf	= new StringBuffer();

        if ( domain.length() != 0 )
        {
            buf.append( domain + ":" );
        }

        buf.append( scope );

        for( int i = 0; i < parts.size(); ++ i )
        {
            buf.append( "." +  parts.get( i ) );
        }

        return( buf.toString() );
    }

		static boolean
	needsEscaping( String part )
	{
		boolean		needsEscaping	= false;
		
		final int	numChars	= part.length();
		for( int i = 0; i < numChars; ++i )
		{
			final char theChar	= part.charAt( i );
			
			if ( ESCAPEABLE_CHARS.indexOf( theChar ) >= 0 )
			{
				needsEscaping	= true;
				break;
			}
		}

		return( needsEscaping );
	}
	
		public static String
	escapePart( String part )
	{
		String	result	= part;
		
		// it is rare that escaping is needed (probably 99.9% of the cases it is not).
		// Don't needlessly generate 2 new objects
		if ( needsEscaping( part ) )
		{
			final int			numChars	= part.length();
			final StringBuffer	buf	= new StringBuffer();
			
			for( int i = 0; i < numChars; ++i )
			{
				final char theChar	= part.charAt( i );
				
				if ( ESCAPEABLE_CHARS.indexOf( theChar ) >= 0 )
				{
					buf.append( ESCAPE_CHAR );
				}
				buf.append( theChar );
			}

			return( buf.toString() );
		}
		
		return( result );
	}
	
	
	
		static String
	toStringFromParsed( ParsedDottedName pn )
	{
		return( toString( pn.mDomain, pn.mScope, pn.mParts, pn.mParts.size() ) );
	}
	
	
	private final static String	NO_DOMAIN	= "";
	private final static char	DOMAIN_DELIM	= ':';
	private final static char	SEPARATOR			= '.';
	private final static char	ESCAPE_CHAR			= SpecialChars.BACKSLASH;
	private final static String	ESCAPEABLE_CHARS	= "" + SEPARATOR + ESCAPE_CHAR;
	
	
	
		static ParsedDottedName
	parse( String sourceString )
		throws com.sun.enterprise.admin.util.TokenizerException
	{
		final Tokenizer	tk	= new TokenizerImpl( sourceString, "" + SEPARATOR,
				false, ESCAPE_CHAR, ESCAPEABLE_CHARS );
		
		final String []	tokens	= tk.getTokens();
		
		if ( tokens.length == 0 )
		{
			final String	msg	= DottedNameStrings.getString(
					DottedNameStrings.DOTTED_NAME_MUST_HAVE_ONE_PART_KEY,
					sourceString );
					
			throw new IllegalArgumentException( msg );
		}
		
		// first token is the scope, last one is the value name, 2nd-to-last can
		// be special case of "property" qualifier, but is still considered
		// a name-part, not a value-name.
		
		// if the scope contains a ':' then it's preceeded by a domain name.
		final int	domainDelimIndex	= tokens[ 0 ].indexOf( DOMAIN_DELIM );
		String	scope	= null;
		String	domain	= NO_DOMAIN;
		if ( domainDelimIndex >= 0 )
		{
			domain	= tokens[ 0 ].substring( 0, domainDelimIndex );
			scope	= tokens[ 0 ].substring( domainDelimIndex + 1, tokens[ 0 ].length() );
		}
		else
		{
			scope		= tokens[ 0 ];
		}
		
		final ArrayList	parts	= new ArrayList();
		for( int i = 1; i < tokens.length; ++i )
		{
			parts.add( tokens[ i ] );
		}
		
		final ParsedDottedName	parsedName	= new ParsedDottedName( domain,
												scope, parts );
		
		return( parsedName );
	}
	
		public String
	getDomain()
	{
		return( mParsed.mDomain );
	}
	
		public String
	getScope()
	{
		return( mParsed.mScope );
	}
	
	
	/*
		Return a list of the parts.  Each part is the unescaped part as
		parsed and unescaped from the original name.
		
		@returns	List of Strings, each representing the name part.
	 */
		public List
	getParts()
	{
		return( Collections.unmodifiableList( mParsed.mParts ) );
	}
	
	/*
		Return the part given by its index
		
		@returns	the part at the specified index
	 */
		public String
	getPart( int i )
	{
		return( (String)mParsed.mParts.get( i ) );
	}
	
		public static boolean
	isWildcardName( final String	name )
	{
		boolean			isWild		= false;
		final int		numWilds	= SpecialChars.WILDCARDS.length();
		
		for( int i = 0; i < numWilds; ++i )
		{
			final int	wildChar	= SpecialChars.WILDCARDS.charAt( i );
			
			if ( name.indexOf( wildChar ) >= 0 )
			{
				isWild	= true;
				break;
			}

		}
		return( isWild );
	}
	
		public boolean
	isWildcardName()
	{
		return( isWildcardName( toString() ) );
	}
	
	
		public int
	hashCode()
	{
        // trying to come up with a good hash code
        // see Effective Java, pp 36-41
        int hashcode = 17;
        hashcode = 37 * hashcode + mSourceString.hashCode();
        hashcode = 37 * hashcode + mParsed.hashCode();
        
        return hashcode;
    }
        
        public boolean
	equals( Object other )
	{
		boolean	equals	= false;
		
		if ( ! (other instanceof DottedName) )
		{
			equals	= false;
		}
		else if ( other == this )
		{
			equals	= true;
		}
		else
		{
			equals	= this.toString().equals( other.toString() );
		}
		
		return( equals );
	}
}





