/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
