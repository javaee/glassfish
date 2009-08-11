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

import java.util.Set;

import org.glassfish.admin.amx.util.stringifier.SmartStringifier;


/**
	Escapes/unescapes strings
 */
public final class StringUtil
{
	private	StringUtil()	{}
	
	public final static char	QUOTE_CHAR	= '\"';
	public final static String	QUOTE		= "" + QUOTE_CHAR;
	
	/**
		Line separator as returned by System.getProperty()
	 */
	public final static String	LS		= System.getProperty( "line.separator", "\n" );
	
		public static String
	quote( Object o )
	{
		return( quote( o, QUOTE_CHAR ) );
	}
	
		public static String
	quote( Object o,  char leftHandChar )
	{
		final String	s	= o == null ? "null" : SmartStringifier.toString( o );
		
		char	leftChar	= leftHandChar;
		char	rightChar	= leftHandChar;
		
		if ( leftHandChar == '(' )
		{
			rightChar	= ')';
		}
		else if ( leftHandChar == '{' )
		{
			rightChar	= '}';
		}
		else if ( leftHandChar == '[' )
		{
			rightChar	= ']';
		}
		else if ( leftHandChar == '<' )
		{
			rightChar	= '>';
		}
		else
		{
			// same char on both left and right
		}
		
		final String	out	= leftChar + s + rightChar;

		return( out );
	}
	
	
		public static String
	toHexString( byte theByte )
	{
		String result	= Integer.toHexString( ((int)theByte) & 0x000000FF );
		if ( result.length() == 1 )
		{
			result	= "0" + result;
		}
		return( result );
	}
	
		public static String
	toHexString( byte[] bytes )
	{
		return( toHexString( bytes, null ) );
	}
	
		public static String
	toHexString( byte[] bytes, String delim )
	{
		final StringBuffer	buf	= new StringBuffer();
		
		if ( bytes.length == 0 )
		{
			// nothing
		}
		else if ( delim == null || delim.length() == 0 )
		{
			for( int i = 0; i < bytes.length; ++i )
			{
				buf.append( toHexString( bytes[ i ] ) );
			}
		}
		else
		{
			for( int i = 0; i < bytes.length; ++i )
			{
				buf.append( toHexString( bytes[ i ] ) + delim );
			}
		
			// remove trailing delim
			buf.setLength( buf.length() - 1);
		}
		
		return( buf.toString() );
	}
	
		public static String
	stripSuffix(
		final String s,
		final String suffix )
	{
		String	result	= s;
		
		if ( s.endsWith( suffix ) )
		{
			result	= s.substring( 0, s.length() - suffix.length() );
		}
		
		return( result );
	}
	
	
		public static String
	replaceSuffix(
		final String s,
		final String fromSuffix,
		final String toSuffix )
	{
		if ( ! s.endsWith( fromSuffix ) )
		{
			throw new IllegalArgumentException( fromSuffix );
		}

		return( stripSuffix( s, fromSuffix ) + toSuffix );
	}
	
		public static String
	stripPrefix(
		final String s,
		final String prefix )
	{
		String	result	= s;
		
		if ( s.startsWith( prefix ) )
		{
			result	= s.substring( prefix.length(), s.length() );
		}
		
		return( result );
	}
	
	
		public static String
	stripPrefixAndSuffix(
		final String s,
		final String prefix,
		final String suffix )
	{
		return stripPrefix( stripSuffix( s, suffix ), prefix );
	}
	

		public static String
	upperCaseFirstLetter( final String s)
	{
		String	result	= s;
		
		if ( s.length() >= 1 )
		{
			result	= s.substring( 0, 1 ).toUpperCase() + s.substring( 1, s.length() );
		}

		return( result );
	}
	
	
        private static String
    toString( final Object o)
    {
        String  result  = null;
        
        if ( o == null )
        {
            result  = "null";
        }
        else if ( o instanceof byte[] )
        {
            final byte[]    b   = byte[].class.cast( o );
            result  = "byte[] of length " + b.length;
        }
        else if ( o instanceof String[] )
        {
            result  = toString( ", ", (Object[])o);
        }
        else
        {
            result  = o.toString();
        }
        
        final int   MAX_LENGTH  = 256;
        if ( result.length() > MAX_LENGTH )
        {
            result  = result.substring( 0, MAX_LENGTH - 1 );
        }
        
        return result;
    }
    
        public static String
    toString( final String[] args )
    {
        return toString( ", ", args );
    }
    
        public static String
    toString( final String delim, final String... args )
    {
        return toString( delim, (Object[])args );
    }
    
	/**
	    Turn an array (or varargs) set of Objects into a String
	    using the specified delimiter.
	 */
	    public static String
    toString( final String delim, final Object... args )
    {
        String  result  = null;
        
        if ( args == null )
        {
            result  = "" + null;
        }
        else if ( args.length == 0 )
        {
            result  = "";
        }
        else if ( args.length == 1 )
        {
            result  = toString( args[ 0 ] );
        }
        else
        {
            final StringBuilder builder = new StringBuilder();
            
            for( int i = 0; i < args.length - 1; ++i )
            {
                builder.append( toString( args[ i ] ) );
                builder.append( delim );
            }
            builder.append( toString( args[ args.length - 1 ] ) );
            
            result  = builder.toString();
        }
        
        
        return result;
     }
     
    /**
        @return the prefix found, or null if not found
     */
        public static String
    getPrefix(
        final Set<String>   prefixes,
        final String        s )
    {
        String  result  = null;
        for( final String prefix : prefixes )
        {
            if ( s.startsWith( prefix ) )
            {
                result  = prefix;
                break;
            }
        }
        return result;
    }
    
    /**
        @return the String after stripping the prefix
        @throws IllegalArgumentException if no prefix found
     */
        public static String
    findAndStripPrefix(
        final Set<String>   prefixes,
        final String        s )
    {
        final String  prefix    = getPrefix( prefixes, s );
        if ( prefix == null )
        {
            throw new IllegalArgumentException( s );
        }
        
        return stripPrefix( s, prefix );
    }
    
	private static String	NEWLINE_STR		= null;
	
	    public static String
	NEWLINE()
	{
	    if ( NEWLINE_STR == null )
	    {
	        NEWLINE_STR = System.getProperty( "line.separator" );
	    }
	    return NEWLINE_STR;
	}
   
}
















