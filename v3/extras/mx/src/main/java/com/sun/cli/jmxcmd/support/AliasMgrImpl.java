/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

