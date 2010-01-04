/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.glassfish.admin.amx.util.Output;

/**
	Maps Attribute names to legal Java identifiers, so that they can
	be exposed in a proxy with get/set routines
 */
public class AttributeNameMapperImpl implements AttributeNameMapper
{
	final Map<String,String>	mDerivedToOriginal;
	final Map<String,String>	mOriginalToDerived;
	final Set<String>			mAllNames;
	final AttributeNameMangler	mMangler;
	
	private Output                mDebug;
	
	/**
		Create a new instance which will map nothing by default.
	 */
		public
	AttributeNameMapperImpl()
	{
		this( null, null);
	}
	
	    public void
	setDebugOutput( final Output debugOutput )
	{
	    mDebug  = debugOutput;
	}
	
	 	protected final void
	debug(final Object o)
	{
	    if ( mDebug != null )
	    {
            mDebug.println( o );
	    }
	}
	
        public String
	matchName(
	    final String   derivedName,
	    final String[] candidates )
	{
	    throw new UnsupportedOperationException( "matchName" );
	}
	
	/**
		Create a new instance which will map (as necessary) the specified
		Attribute names.
		Same as AttributeNameMapperImpl( originalNames, new AttributeNameManglerImpl() )
	 */
		public
	AttributeNameMapperImpl( final String[] originalNames )
	{
		this( originalNames, new AttributeNameManglerImpl( true, null ) );
	}
	
	/**
		Create a new instance which will map (as necessary) the specified
		Attribute names
	 */
		public
	AttributeNameMapperImpl( final AttributeNameMangler mangler )
	{
		this( null, mangler );
	}
	
	/**
		Create a new instance which will map (as necessary) the specified
		Attribute names
	 */
		public
	AttributeNameMapperImpl(
		final String[] 			originalNames,
		AttributeNameMangler	mangler )
	{
		mDebug  = null;
		mDerivedToOriginal	= new HashMap<String,String>();
		mOriginalToDerived	= new HashMap<String,String>();
		mAllNames		= new HashSet<String>();
		mMangler		= mangler == null ? new AttributeNameManglerImpl( true, null ) : mangler ;
		
		if ( originalNames != null )
		{
			deriveAll( originalNames );
		}
	}
	
	
		public void
	deriveAll( final String[] originalNames )
	{
		final Set<String>	notRequired	= new HashSet<String>();
		final Set<String>	required	= new HashSet<String>();
		
		// first determine all names we (a) need to map and (b) don't need to map.
		// we must not generate any mapped names that conflict with names
		// that don't require mapping
		for( final String originalName : originalNames )
		{
			if ( mOriginalToDerived.containsKey( originalName ) )
			{
			    // continue; already present (explicit mapping)
			}
			else if ( ! requiresMapping( originalName ) )
			{
				notRequired.add( originalName );
			}
			else
			{
				required.add( originalName );
			}
		}
		
		// add all ones that don't require mapping to map to themselves
		for ( final String name : notRequired )
		{
			addMapping( name, name );
		}
		
		// now create a mapped name for each one that requires it.
		for( final String originalName : required )
		{
			// our preferred derivation
			final String	preferredDerivation	= originalToDerived( originalName );
			
			// ensure that the derived name is not already used
			String	derivedName	= preferredDerivation;
			int id	= 0;
			while ( mAllNames.contains( derivedName ) ||
					required.contains( derivedName ) )
			{
				derivedName	= preferredDerivation + "_" + id;
				++id;
			}
			addMapping( originalName, derivedName );
		}
	}
	
	
		public boolean
	derivedSameAsOriginal( String derivedName )
	{
		final String	original	= derivedToOriginal( derivedName );
		final boolean	theSame		= original != null && derivedName.equals( original );
			
		return( theSame );
	}
	
		public void
	addMapping(
		final String originalName,
		final String derivedName )
	{
		mDerivedToOriginal.put( derivedName, originalName );
		mOriginalToDerived.put( originalName, derivedName );
		
		mAllNames.add( derivedName );
	}
	
		public void
	dontMap( final String originalName )
	{
	    debug( "dontMap: " + originalName );
		addMapping( originalName, originalName );
	}
	
		public boolean
	requiresMapping( final String originalName )
	{
		boolean	requiresMapping	= true ;
		
		// must start with upper-case first-letter
		final char	firstChar	= originalName.charAt( 0 );
		if ( Character.isUpperCase( firstChar ) )
		{
			if ( Character.isJavaIdentifierStart( firstChar ) )
			{
				final int	length	= originalName.length();
				
				requiresMapping	= false;
				for( int i = 1; i < length; ++i )
				{
					if ( ! Character.isJavaIdentifierPart( originalName.charAt( i ) ) )
					{
						requiresMapping	= true;
						break;
					}
				}
			}
		}
		
		return( requiresMapping );
	}
	
	
		public String
	originalToDerived( String originalName )
	{
		String	derivedName	= (String)mOriginalToDerived.get( originalName );
		
		if ( derivedName == null )
		{
			derivedName	= mMangler.mangleAttributeName( originalName );
		}
		
		return( derivedName );
	}
	
		public String
	derivedToOriginal( String derivedName )
	{
		String	original	= (String)mDerivedToOriginal.get( derivedName );
		
		if ( original == null )
		{
			original	= derivedName;
		}
		
		return( original );
	}
	
	
		public Set<String>
	getAttributeNames( )
	{
		return( mAllNames );
	}
	
		public String
	toString()
	{
		final StringBuffer	buf	= new StringBuffer();
		
		buf.append( "AttributeNameMapperImpl mappings:\n" );
        for( final String name : getAttributeNames() )
		{
			buf.append( name );
			buf.append( " => " );
			buf.append( derivedToOriginal( name ) + "\n" );
		}
		
		return( buf.toString() );
	}
}
