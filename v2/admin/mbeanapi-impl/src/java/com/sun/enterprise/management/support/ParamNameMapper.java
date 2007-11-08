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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
 
/*
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/support/ParamNameMapper.java,v 1.3 2006/03/09 20:30:48 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2006/03/09 20:30:48 $
 */

package com.sun.enterprise.management.support;

import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Collections;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import com.sun.appserv.management.util.jmx.AttributeNameMangler;

/**
	Translates parameters from the new Attribute names to the old
	ones.  When instantiated, optional override mappings may be supplied;
	these will be used in lieu of the algorithmic translation.
 */
public final class ParamNameMapper implements AttributeNameMangler
{
	final Map<String, String>		mMappings;
	final String	mWordDelim;
	
	/** 
		The default delimiter used between words.
	 */
	public static final String	DEFAULT_WORD_DELIM	= "_";
	
	
	/**
		Create a new instance.
		
		The 'overrides' map should map a new name to an old one.
		
		@param overrides any special mappings that override the algorithmic mapping (may be null)
	 */
		public
	ParamNameMapper( final Map<String,String> overrides )
	{
		this( overrides, DEFAULT_WORD_DELIM );
	}
	
	/**
		Create a new instance
		
		@param overrides any special mappings that override the algorithmic mapping (may be null)
	 */
		public
	ParamNameMapper(
		final Map<String,String> overrides,
		final String	wordDelim )
	{
		mMappings	= new HashMap<String,String>();
		if ( overrides != null )
		{
			mMappings.putAll( overrides );
		}
		
		mWordDelim	= wordDelim;
	}
	
	/**
		Create a new instance with no overrides.
	 */
		public
	ParamNameMapper( final String wordDelim )
	{
		this( null, wordDelim );
	}
	
	/**
		Create a new instance with no overrides.
	 */
		public
	ParamNameMapper( )
	{
		this( null, DEFAULT_WORD_DELIM );
	}

		
		private String
	getUpperCaseSequence( final CharacterIterator iter )
	{
		final StringBuffer	buf = new StringBuffer();
		
		char	c;
		
		while ( (c=iter.current()) != CharacterIterator.DONE )
		{
			if ( Character.isLowerCase( c ) )
			{
				break;
			}

			iter.next();
			buf.append( c );
		}
		
		return( buf.toString() );
	}
		private String
	getLowerCaseSequence( final StringCharacterIterator iter )
	{
		final StringBuffer	buf = new StringBuffer();
		
		char	c;
		
		while ( (c=iter.current()) != CharacterIterator.DONE )
		{
			if ( Character.isUpperCase( c ) )
			{
				break;
			}

			iter.next();
			buf.append( c );
		}
		
		return( buf.toString() );
	}
	
		private String
	newToOld( final String newName )
	{
		final StringCharacterIterator	iter	= new StringCharacterIterator( newName );
		final StringBuffer	buf = new StringBuffer();

		while ( iter.current() != CharacterIterator.DONE )
		{
			final String	uppercase	= getUpperCaseSequence( iter );
			if (	uppercase.length() <= 1 ||
					iter.current() == CharacterIterator.DONE )
			{
				buf.append( uppercase.toLowerCase() );
			}
			else
			{
				// a run of an uppercase acronym of length N.  The first N-1 characters
				// are the acronym and the last character is actually the first letter of
				// the next word.
				final int	acronymLength	= uppercase.length() - 1;
				final String	acronym		= uppercase.substring( 0, acronymLength );
				final char		firstOfNext	= uppercase.charAt( acronymLength );
				buf.append( acronym.toLowerCase() );
				buf.append( mWordDelim );
				buf.append( Character.toLowerCase( firstOfNext ) );
			}
			
			buf.append( getLowerCaseSequence( iter ) );
			if ( iter.current() != CharacterIterator.DONE )
			{
				buf.append( mWordDelim );
			}
		}
		
		return( buf.toString() );
	}
	

	
		public String
	toString()
	{
		final StringBuffer	buf = new StringBuffer();
		final Iterator 		iter	= mMappings.keySet().iterator();
		
		while ( iter.hasNext() )
		{
			final String	newName	= (String)iter.next();
			
			buf.append( newName + "=" + mMappings.get( newName ) + "\n");
		}
		return( buf.toString() );
	}

		
	/**
		Convert an Attribute name from the new one to the old one.
		
		Any special casing should override this.
	 */
		public String
	mangleAttributeName( final String newName )
	{
		String	result	= (String)mMappings.get( newName );
		
		if ( result == null )
		{
			// use algorithmic conversion
			result	= newToOld( newName );
			
			// cache for later
			mMappings.put( newName, result );
	    }

		return( result );
	}
}











