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
package com.sun.cli.jmxcmd.util;

import java.util.Map;


/**
	Implements the mapping by removing illegal characters and
	attempting to camel-case a following alphabetic character.
	Optionally capitalizes the first letter
 */
public class AttributeNameManglerImpl implements AttributeNameMangler
{
	private final Map<String,String>		mOverrides;
	private final boolean	mCapitalizeFirstLetter;
	
	/**
		@param capitalizeFirstLetter	true if first letter should be capitalized
		@param overrides explicit mappings from the original to a result
	 */
		public
	AttributeNameManglerImpl(
		final boolean	capitalizeFirstLetter,
		final Map<String,String>		overrides )
	{
		mCapitalizeFirstLetter	= capitalizeFirstLetter;
		mOverrides				= overrides;
	}
	
	
		private String
	convertOverride( final String name )
	{
		String	result	= name;
		
		if ( mOverrides != null )
		{
			if ( mOverrides.containsKey( name ) )
			{
				result	= (String)mOverrides.get( name );
			}
			/*
			else
			{
				// have to do case-insensitive search
				final Iterator	iter	= mOverrides.keySet().iterator();
				while ( iter.hasNext() )
				{
					final String	override	= (String)iter.next();
					
					if ( override.equalsIgnoreCase( name ) )
					{
						result	= (String)mOverrides.get( name );
						break;
					}
				}
			}
			*/
		}
		
		return( result );
	}

		public String
	mangleAttributeName( final String attributeName )
	{
		String	result	= mangleIt( attributeName );
		
		return( result );
	}
	
		private String
	toUpperCase( final char c )
	{
		return( ("" + c).toUpperCase() );
	}
	
	/**
		Note that because we expect a "get" or "set" to be placed in front
		of the Attribute name, the first character of the Attribute name
		need only be a valid Java identifier part; it need not be a valid
		first character.
	 */
		private String
	mangleIt( final String attributeName )
	{
		final char[]		chars	= attributeName.toCharArray();
		final StringBuffer	buf	= new StringBuffer();
		
		// capitalize the first letter
		final char	firstChar	= chars[ 0 ];
		if ( Character.isJavaIdentifierPart( firstChar ) )
		{
			buf.append( mCapitalizeFirstLetter ? toUpperCase( firstChar ) : "" + firstChar );
		}
		// else  { omit it }
		
		for( int i = 1; i < chars.length; ++i )
		{
			final char	c	= chars[ i ];
			
			if ( ! Character.isJavaIdentifierPart( c ) )
			{
				++i;	// skip it
				
				final char	nextChar	= (i < chars.length) ? chars[ i ] : 0;
					
				if (  nextChar >= 'a' && nextChar <= 'z' )
				{
					buf.append( toUpperCase( chars[ i ] ) );
				}
				else if (  nextChar >= 'A' && nextChar <= 'Z' )
				{
					buf.append( "" + chars[ i ] );
				}
				else
				{
					// emit nothing and go onto next character
					--i;
				}
			}
			else
			{
				buf.append( c );
			}
		}
		
		final String	before	= buf.toString();
		final String	result	= convertOverride( before );
		
		return( result );
	}
}
