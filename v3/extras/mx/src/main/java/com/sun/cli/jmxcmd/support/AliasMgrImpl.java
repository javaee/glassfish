/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2009 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/support/AliasMgrImpl.java,v 1.3 2004/04/25 07:14:10 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2004/04/25 07:14:10 $
 */
 

package com.sun.cli.jmxcmd.support;

import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;
import java.util.HashSet;

import javax.management.NotCompliantMBeanException;


/**
	This MBean must be modified to store its aliases within domain.xml.  For now, it uses
	an internal implementation.
 */
public final class AliasMgrImpl implements AliasMgrMBean
{
	final AliasMgrSPI	mImpl;
	
		public
	AliasMgrImpl( AliasMgrSPI	impl )
	{
		mImpl	= impl;
	}
	
		private static void
	checkLegalAlias( String aliasName )
	{
		if ( ! isValidAlias( aliasName ) )
		{
			throw new IllegalArgumentException( "illegal alias name: \"" + aliasName + "\"");
		}
	}
	
		public void
	createAlias( String aliasName, String objectName ) throws Exception
	{
		checkLegalAlias( aliasName );
		
		if ( getAliasValue( aliasName ) != null )
		{
			// illegal to create an already-existing alias
			throw new IllegalArgumentException( "alias already exists: " + aliasName );
		}
		
		mImpl.create( aliasName, objectName );
	}
	
		public String
	getAliasValue( String aliasName )
	{
		// we don't care if the name is legal or not, just try to get it
		final String	result	= (String)mImpl.get( aliasName );
		
		return( result );
	}
	
		public void
	deleteAlias( String aliasName ) throws Exception
	{
		checkLegalAlias( aliasName );
		
		mImpl.delete( aliasName );
	}
	
		public String []
	getAliases() throws Exception
	{
		return( listAliases( ) );
	}
	
		public String []
	listAliases( ) throws Exception
	{
		return( listAliases( false ) );
	}
	
		public String []
	listAliases( boolean showValues ) throws Exception
	{
		final Set		keys	= mImpl.getNames();
		final int		numKeys	= keys.size();
		
		final String []	aliases	= new String[ numKeys ];
		final Iterator	iter	= keys.iterator();
		
		for( int i = 0; i < numKeys; ++i )
		{
			final String	key	= (String)iter.next();
			
			if ( showValues )
			{
				aliases[ i ]	= key + "=" + getAliasValue( key );
			}
			else
			{
				aliases[ i ]	= key;
			}
		}
		
		Arrays.sort( aliases );
		
		return( aliases );
	}

	 
		public static boolean
	isValidAlias( final String str )
	{
		final int strLength	= str.length();

		boolean	isValid	= strLength != 0;
		if ( isValid )
		{
			// this can be done more efficiently with a BitMap or Set, but who cares
			for( int i = 0; i < strLength; ++i )
			{
				final char theChar = str.charAt( i );
				
				if ( ! LegalAliasChars.isLegalAliasChar( theChar ) )
				{
					isValid	= false;
					break;
				}
				
			}
		}
		
		return( isValid );
	}
}



final class LegalAliasChars
{
		private
	LegalAliasChars()
	{
		// disallow instantiation
	}

	/**
	 	Aliases must not allow delimiters used by ObjectNames.
	 */
	 private final static String	LEGAL_ALIAS_CHARS	=
	 	"abcdefghijklmnopqrstuvwxyz" +	// lower-case letters
	 	"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +	// upper-case letters
	 	"0123456789" +					// digits
	 	"-_."; 							// useful separators
	 
	 private final static boolean []	LEGAL_ALIAS_CHARS_BITMAP	= initLegalAliasChars();

		private static boolean []
	initLegalAliasChars()
	{
		final boolean []	legals	= new boolean [ 128 ];
		
		for( int i = 0; i < LEGAL_ALIAS_CHARS.length(); ++i )
		{
			legals[ i ]	= false;
		}
		
		for( int i = 0; i < LEGAL_ALIAS_CHARS.length(); ++i )
		{
			final char	theChar	= LEGAL_ALIAS_CHARS.charAt( i );
			
			legals[ (int)theChar ]	= true;
		}
		return( legals );
	}
	
		public static boolean
	isLegalAliasChar( char theChar )
	{
		final int	intValue	= (int)theChar;
		
		return( intValue < LEGAL_ALIAS_CHARS_BITMAP.length &&
				LEGAL_ALIAS_CHARS_BITMAP[ intValue ] );
	}
}

