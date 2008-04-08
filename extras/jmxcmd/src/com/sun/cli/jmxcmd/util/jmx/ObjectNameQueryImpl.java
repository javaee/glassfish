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
 
/*
 * $Header: /m/jws/jmxcmd/src/com/sun/cli/jmxcmd/util/jmx/ObjectNameQueryImpl.java,v 1.3 2005/11/15 20:21:48 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2005/11/15 20:21:48 $
 */
package com.sun.cli.jmxcmd.util.jmx;

import java.util.Set;
import java.util.HashSet;
import java.util.regex.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;


import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import com.sun.cli.jcmd.util.misc.EnumerationIterator;

public class ObjectNameQueryImpl implements ObjectNameQuery
{
		public
	ObjectNameQueryImpl()
	{
	}
	
	
	/**
		Return true if one (or more) of the properties match the regular expressions
		for both name and value.   Return false if no property/value combinations match.
		
		A null pattern matches anything.
	 */
		boolean
	match( Hashtable properties, Pattern propertyPattern, Pattern valuePattern )
	{
		final Iterator	keys	= new EnumerationIterator( properties.keys() );
		boolean	matches	= false;
		
		while ( keys.hasNext() )
		{
			final String	key	= (String)keys.next();
			
			if ( propertyPattern == null || propertyPattern.matcher( key ).matches() )
			{
				if ( valuePattern == null )
				{
					matches	= true;
					break;
				}

				// see if value matches
				final String	value	= (String)properties.get( key );
				
				if ( valuePattern.matcher( value ).matches() )
				{
					matches	= true;
					break;
				}
			}
		}
		
		return( matches );
	}
	
	/**
		Match all property/value expressions against the ObjectName.
		
		Return true if for each property/value regular expression pair there is at least one
		property within the ObjectName whose property name and value match their respective
		patterns.
		
		A null regex indicates "match anything".
	 */
		boolean
	matchAll(	ObjectName	name,
				Pattern []	propertyPatterns,
				Pattern []	valuePatterns )
	{
		boolean	matches	= true;
		
		final Hashtable	properties	= name.getKeyPropertyList();
		
		for( int i = 0; i < propertyPatterns.length; ++i )
		{
			if ( ! match( properties, propertyPatterns[ i ], valuePatterns[ i ] ) )
			{
				matches	= false;
				break;
			}
		}
		
		return( matches );
	}
	
	
	/**
		Match all property/value expressions against the ObjectName.
		
		Return true if there is at least one property/value regular expression pair that
		matches a property/value pair within the ObjectName.
		
		A null regex indicates "match anything".
	 */
		boolean
	matchAny(	ObjectName	name,
				Pattern []	propertyPatterns,
				Pattern []	valuePatterns )
	{
		boolean	matches	= false;
		
		final Hashtable	properties	= name.getKeyPropertyList();
		
		for( int i = 0; i < propertyPatterns.length; ++i )
		{
			if ( match( properties, propertyPatterns[ i ], valuePatterns[ i ] ) )
			{
				matches	= true;
				break;
			}
		}
		
		return( matches );
	}
	
	
	
		Pattern []
	createPatterns( final String [] patternStrings, int numItems )
	{
		final Pattern []	patterns	= new Pattern [ numItems ];
		
		if ( patternStrings == null )
		{
			for( int i = 0; i < numItems; ++i )
			{
				patterns[ i ]	= null;
			}
			
			return( patterns );
		}
			
		
		for( int i = 0; i < numItems; ++i )
		{
			// consider null to match anything
			
			if ( patternStrings[ i ] == null )
			{
				patterns[ i ]	= null;
			}
			else
			{
				patterns[ i ]	= Pattern.compile( patternStrings[ i ] );
			}
		}
		
		return( patterns );
	}
	
	private interface Matcher
	{
		boolean		match( ObjectName name, Pattern [] names, Pattern [] values );
	}
	
	private class MatchAnyMatcher implements Matcher
	{
		public MatchAnyMatcher() {}
		
			public boolean	
		match( ObjectName name, Pattern [] names, Pattern [] values )
		{
			return( matchAny( name, names, values ) );
		}
	}
	
	private class MatchAllMatcher implements Matcher
	{
		public MatchAllMatcher() {}
		
			public boolean	
		match( ObjectName name, Pattern [] names, Pattern [] values )
		{
			return( matchAll( name, names, values ) );
		}
	}
	

		Set
	matchEither( Matcher matcher, Set startingSet, String [] regexNames, String [] regexValues )
	{
		if ( regexNames == null && regexValues == null )
		{
			// both null => matches entire original set
			return( startingSet );
		}
		
		final Iterator	iter	= startingSet.iterator();
		final Set		results	= new HashSet();
		
		int	numMatches	= 0;
		if ( regexNames != null )
		{
			numMatches	= regexNames.length;
		}
		else
		{
			numMatches	= regexValues.length;
		}

		final Pattern []	namePatterns	= createPatterns( regexNames, numMatches );
		final Pattern []	valuePatterns	= createPatterns( regexValues, numMatches );
		
		while ( iter.hasNext() )
		{
			final ObjectName	name	= (ObjectName)iter.next();
			
			if ( matcher.match( name, namePatterns, valuePatterns ) )
			{
				results.add( name );
			}
		}

		return( results );
	}

		public Set<ObjectName>
	matchAll( Set<ObjectName> startingSet, String [] regexNames, String [] regexValues )
	{
		return( matchEither( new MatchAllMatcher(), startingSet, regexNames, regexValues ) );
	}
	

				
		public Set<ObjectName>
	matchAny( Set<ObjectName> startingSet, String [] regexNames, String [] regexValues )
	{
		return( matchEither( new MatchAnyMatcher(), startingSet, regexNames, regexValues ) );
	}
}






