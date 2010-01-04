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

package com.sun.cli.jcmd.util.cmd;

import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import org.glassfish.admin.amx.util.ArrayUtil;



		
/**
	Internal class used to keep information about the options.
 */
public class OptionInfoImpl implements OptionInfo
{
	private final String				mLongName;	// includes "-" or "--" prefix
	private final String				mShortName;	// includes "-" or "--" prefix
	private final Set<String>			mSynonyms;	// includes "-" or "--" prefix
	private final String[]				mValueNames;
	private final Set<OptionDependency>	mDependencies;
	private final boolean				mIsRequired;
	
	public String	getLongName()	{ return( mLongName ); }
	public String	getShortName()	{ return( mShortName ); }
	public String[]	getValueNames()	{ return( mValueNames ); }
	public int		getNumValues()	{ return( mValueNames == null ? 1 : mValueNames.length ); }
	public boolean	isBoolean()		{ return( mValueNames == null ); }
	public boolean	isRequired()	{ return( mIsRequired ); }
	
		public boolean
	equals(	Object rhs )
	{
		if ( this == rhs )
		{
			return( true );
		}
		
		boolean	equals	= false;
		if ( rhs != null && (rhs instanceof OptionInfo) )
		{
			final OptionInfo	other	= (OptionInfo)rhs;
			
			if ( mIsRequired == other.isRequired() &&
				isBoolean() == other.isBoolean() &&
				ArrayUtil.arraysEqual( getValueNames(), other.getValueNames() ) &&
				mSynonyms.equals( other.getSynonyms() ) )
			{
				equals	= true;
			}
		}
		return( equals );
	}
	
	/**
		Create a new description of an option.  Either the long or short
		names must be non-null (preferably both).
		
		Boolean options must specify numValues of 0.
		
		For best compliance the short name should always be specified.
		
		@param longName		the long name (may be null)
		@param shortName	the short name (may be null)
		@param valueNames	names to use for the values in showing the syntax
		@param synonyms		alternative equivalent names, if any
		@param required		true if required option, false if not
	 */
		public
	OptionInfoImpl(
		final String	longName,
		final String	shortName,
		final String[]	valueNames,
		String []		synonyms,
		boolean			required )
	{
		mLongName		= ensurePrefix( longName );
		mShortName		= ensurePrefix( shortName );
		mIsRequired		= required;
		
		mValueNames	= valueNames;
		
		mSynonyms	= formSynonyms( mLongName, mShortName, synonyms );
		
		mDependencies	= new HashSet<OptionDependency>();
	}
	
		private static Set<String>
	formSynonyms( String longName, String shortName, String[] synonyms )
	{
		final Set<String>	s	= new HashSet<String>();
		
		// ensure that it's <long-name>, then <short-name> as the order is
		// important elsewhere
		if ( longName != null )
		{
			s.add( longName );
		}
		if ( shortName != null )
		{
			s.add( shortName );
		}
		
		if ( synonyms != null )
		{
			for( int i = 0; i < synonyms.length; ++i )
			{
				s.add( ensurePrefix( synonyms[ i ] ) );
			}
		}
		
		return( s );
	}
	
	/**
		Create a new description of a non-Boolean option, required state
		
		Calls this( longName, shortName, numValues, null)
	 */
		public
	OptionInfoImpl( 
		final String	longName,
		final String	shortName,
		final String[]	valueNames,
		final boolean	required )
	{
		this( longName, shortName, valueNames, null, required );
	}
	
	/**
		Create a new description of a non-Boolean option, required state
		
		Calls this( longName, shortName, numValues, null)
	 */
		public
	OptionInfoImpl( 
		final String	longName,
		final String	shortName,
		final String	valueName,
		final boolean	required )
	{
		this( longName, shortName, new String[] { valueName }, null, required );
	}
	
	/**
		Create a new description of a non-Boolean option.
		
		Calls this( longName, shortName, numValues, null)
	 */
		public
	OptionInfoImpl( 
		final String	longName,
		final String	shortName,
		final String[]	valueNames )
	{
		this( longName, shortName, valueNames, null, false );
	}
	
	/**
		Create a new description of a non-Boolean option.
		
		Calls this( longName, shortName, numValues, null)
	 */
		public
	OptionInfoImpl( 
		final String	longName,
		final String	shortName,
		final String	valueName )
	{
		this( longName, shortName, new String[] { valueName }, null, false );
	}
	
	
	/**
		Create a new description of a Boolean option with no synonyms.
		
		Calls this( longName, shortName, 0, null)
	 */
		public
	OptionInfoImpl( 
		final String	longName,
		final String	shortName)
	{
		this( longName, shortName, null, null, false );
	}
	
	/**
		Create a new description of a Boolean option with no synonyms.
		
		Calls this( longName, shortName, 0, null)
	 */
		public
	OptionInfoImpl( 
		final String	longName,
		final String	shortName,
		final boolean	required )
	{
		this( longName, shortName, null, null, required );
	}
	
	/**
		Return the synonyms for this option, including the long and short name and any 
		additional synonyms.
	 */
		public Set<String>
	getSynonyms()
	{
		return( Collections.unmodifiableSet( mSynonyms ) );
	}
	
		public void
	addDependency( OptionDependency 	dependency )
	{
		if ( ! mDependencies.contains( dependency ) )
		{
			mDependencies.add( dependency );
		}
	}
	
		public Set<OptionDependency>
	getDependencies(  )
	{
		return( Collections.unmodifiableSet( mDependencies ) );
	}
	
	/**
		Return true if the name matches either the short or long names or a synonym.
		The name must have the appropriate prefix already in place.
		
		All option names are case-sensitive.
		
		@param name		an option name beginning with "-" or "--"
		@return			true if a match, false otherwise
	 */
		public boolean
	matches( String name )
	{
		if ( ! name.startsWith( OPTION_PREFIX ) )
		{
			throw new IllegalArgumentException( "option name must begin with '-'" );
		}
		
		final boolean	isShortName	= name.length() == 2;
	
		boolean	isAMatch	= false;
		
		isAMatch	= mSynonyms.contains( name );
		
		return( isAMatch );
	}
	
	/**
		Ensure that the name has the appropriate "-" or "--" prefix
	 */
		static public String
	ensurePrefix( String name )
	{
		String	mappedName	= name;
		if ( name != null )
		{
			if ( ! name.startsWith( SHORT_OPTION_PREFIX ) )
			{
				final String	prefix	= (name.length() == 1) ?
								SHORT_OPTION_PREFIX : LONG_OPTION_PREFIX;
				
				mappedName	= prefix + name;
			}
		}
		
		return( mappedName );
	}
	
		public String
	toString( )
	{
		String	s	= ensurePrefix( getLongName() ) + "|" + ensurePrefix( getShortName() );
		
		if ( mValueNames != null  )
		{
			for( int i = 0; i < mValueNames.length; ++i )
			{
				s	= s + " " + "<" + mValueNames[ i ] + ">";
			}
		}
		
		if ( ! isRequired() )
		{
			s	= "[" + s + "]";
		}
		return( s );
	}
	
		public String
	toDisplayString()
	{
		return( toString() );
	}
}


