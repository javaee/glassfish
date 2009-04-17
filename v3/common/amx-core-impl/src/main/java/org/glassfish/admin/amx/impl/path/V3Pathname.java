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
package org.glassfish.admin.amx.impl.path;

import org.glassfish.admin.amx.impl.util.Tokenizer;
import org.glassfish.admin.amx.impl.util.TokenizerException;
import org.glassfish.admin.amx.impl.util.TokenizerImpl;
import org.glassfish.admin.amx.impl.util.TokenizerParams;
import static org.glassfish.admin.amx.impl.path.DottedNameSpecialChars.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
	Represents a GlassFish V3 dotted name.
    <p>
    V3DottedName := [<scope-part>][<name-part>]+[<attr-part>]
    (A full formal grammar should be defined)
    <p>
    Examples:<br>
    <pre>
    /domain/servers[server1]@name
    /domain@locale
    </pre>
	<p>
    Initial implementation is "brain dead": no formal parsing, only a simple splitting
    of the string textually.  Once a formal grammar is defined, real parsing should
    be done.
 */
public final class V3Pathname
{
	final String			mSourceString;
	final ParsedV3Pathname	mParsed;
    
		public
	V3Pathname( final String sourceString )
	{
		mSourceString	= sourceString;
		try
		{
			mParsed	= ParsedV3Pathname.parse( sourceString );
		}
		catch( final Exception e )
		{
			final String	msg	= DottedNameStrings.getString(
					DottedNameStrings.MALFORMED_DOTTED_NAME_KEY,
					sourceString );
					
			throw new IllegalArgumentException( msg );
		}
		
		//checkWellFormed( sourceString, toStringFromParsed( mParsed ) );
	}
	
	/*
		Certain malformed escape constructs will parse OK, but are not acceptable
		as dotted names.  Example: "Foo\Bar" is acceptable to the Tokenizer, but
		must be written as "Foo\\Bar" to be a valid dotted name.
	 *
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
    */
    
    /*
	
		public static String
	toString( DottedName dn, int numParts )
	{
		return( toString( dn.getDomain(), dn.getScope(), dn.getParts(), numParts ));
	}
	
		public static String
	toString( String domain, String scope, List<String> parts )
	{
		return( toString( domain, scope, parts, parts.size() ));
	}
	
		public static String
	toString( String domain, String scope, List<String> parts, int numParts )
	{
		final StringBuffer	buf	= new StringBuffer();
		
		if ( domain.length() != 0 )
		{
			buf.append( domain + ":" );
		}
		
		buf.append( escapePart( scope ) );
		
		for( int i = 0; i < numParts; ++ i )
		{
			final String	unescapedPart	= parts.get( i );
			
			buf.append( "." + escapePart( unescapedPart ) );
		}
		
		return( buf.toString() );
	}
    */
	
		public String
	toString()
	{
		return( mSourceString );
	}

/*
        public static String
    toString(final String domain,
             final String scope,
             final List<String> parts,
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
*/

		static boolean
	needsEscaping( String part )
	{
		boolean		needsEscaping	= false;
		
		final int	numChars	= part.length();
		for( int i = 0; i < numChars; ++i )
		{
			final char theChar	= part.charAt( i );
			
			if ( ESCAPEABLE_CHARS_STR.indexOf( (int)theChar ) >= 0 )
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
				
				if ( ESCAPEABLE_CHARS_STR.indexOf( (int)theChar ) >= 0 )
				{
					buf.append( ESCAPE_CHAR );
				}
				buf.append( theChar );
			}

			return( buf.toString() );
		}
		
		return( result );
	}
	
      /**
        Insert a "-" characer between every lower/upper transition and/or every
        acronym transition, and lowercase the entire result.
        Examples:<br>
        FooBarBee => foo-bar-bee<br>
        fooBarBEE => foo-bar-bee<br>
        HTTPService => httpservice
     */
        public static String
    hyphenate( final String name )
    {
        final int length = name.length();
        final StringBuffer b = new StringBuffer();
        
        for ( int i = 0; i < length; ++i)
        {
            final char    c = name.charAt(i);
            final boolean isUpper = Character.isUpperCase(c);
            final char    lowerc = isUpper ? Character.toLowerCase(c) : c;
            
            // transition from run of uppercase to lowercase?
            // eg the 'S' in HTTPService
            if ( isUpper && i != 0 && Character.isUpperCase(name.charAt(i-1)) )
            {
                b.append( "-" );
                b.append( lowerc );
            }
            // transition from lower to upper case?
            else if ( (!isUpper) && i < length-1 && Character.isUpperCase(name.charAt(i+1)) )
            {
                b.append( lowerc );
                b.append( "-" );
            }
            else
            {
                b.append( lowerc );
            }
        }
        //cdebug( "dashify: " + name + " => " + b.toString() );
        return b.toString();
    }

	
	
	/*
		Return a list of the parts.  Each part is the unescaped part as
		parsed and unescaped from the original name.
		
		@returns	List of Strings, each representing the name part.
	 */
		public List<PathPart>
	getParts()
	{
		return( Collections.unmodifiableList( mParsed.getParts() ) );
	}
    
    public ParsedV3Pathname getParsed() { return mParsed; }
	
	/*
		Return the part given by its index
		
		@returns	the part at the specified index
	 */
		public PathPart
	getPart( int i )
	{
		return mParsed.getParts().get( i );
	}
	
		public static boolean
	isWildcardName( final String	name )
	{
		boolean			isWild		= false;
		final int		numWilds	= WILDCARDS.length();
		
		for( int i = 0; i < numWilds; ++i )
		{
			final int	wildChar	= WILDCARDS.charAt( i );
			
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
		
		if ( ! (other instanceof V3Pathname) )
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





