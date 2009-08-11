/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/util/jmx/ObjectNameQueryImplTest.java,v 1.2 2003/11/21 22:15:45 llc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/11/21 22:15:45 $
 */
package org.glassfish.admin.amx.util.jmx;

import java.util.Set;
import java.util.HashSet;

import javax.management.ObjectName;

public class ObjectNameQueryImplTest extends junit.framework.TestCase
{
		public void
	testCreation()
	{
		new ObjectNameQueryImpl();
	}
	
	static final Set<ObjectName>		EmptySet	= java.util.Collections.emptySet();
	static final String []	EmptyStrings	= new String [0];
	
		static Set<ObjectName>
	createSet( ObjectName name )
	{
		final HashSet<ObjectName>	s	= new HashSet<ObjectName>();
		
		s.add( name );
		
		return( s );
	}
	
		static Set<ObjectName>
	createSet( ObjectName [] names )
	{
		final HashSet<ObjectName>	s	= new HashSet<ObjectName>();
		
		for( int i = 0; i < names.length; ++i )
		{
			s.add( names[ i ] );
		}
		
		return( s );
	}
	
	
		static ObjectName
	createName( String nameString )
	{
		ObjectName	name	= null;
		
		try
		{
			name	= new ObjectName( nameString );
		}
		catch( Exception e )
		{
			assert( false );
		}
		return( name );
	}
	
		public void
	testEmptySet()
	{
		final ObjectNameQuery	q	= new ObjectNameQueryImpl();
		
		assertEquals( 0, q.matchAny( EmptySet, null, null ).size() );
		assertEquals( 0, q.matchAny( EmptySet, EmptyStrings, EmptyStrings ).size() );
		assertEquals( 0, q.matchAny( EmptySet, null, EmptyStrings ).size() );
		assertEquals( 0, q.matchAny( EmptySet, EmptyStrings, null ).size() );
		
		assertEquals( 0, q.matchAll( EmptySet, null, null ).size() );
		assertEquals( 0, q.matchAll( EmptySet, EmptyStrings, EmptyStrings ).size() );
		assertEquals( 0, q.matchAll( EmptySet, null, EmptyStrings ).size() );
		assertEquals( 0, q.matchAll( EmptySet, EmptyStrings, null ).size() );
	}
	
		public void
	testSingleItem()
	{
		final ObjectName	name1	= createName( ":name=test,type=test" );
		final Set<ObjectName>			testSet	= createSet( name1 );
		
		final ObjectNameQuery	q	= new ObjectNameQueryImpl();
		
		assertEquals( 1, q.matchAny( testSet,
			null,
			null ).size() );
		
		assertEquals( 1, q.matchAny( testSet, 
			new String [] { "type" },
			new String [] { "test" } ).size() );
			
		assertEquals( 1, q.matchAny( testSet, 
			new String [] { "type" },
			null ).size() );
			
		assertEquals( 1, q.matchAny( testSet, 
			null,
			new String [] { "test" } ).size() );
			
			
		assertEquals( 1, q.matchAny( testSet, 
			new String [] { "t.*" },
			new String [] { ".*e.*" } ).size() );
			
			
		assertEquals( 1, q.matchAny( testSet, 
			null,
			new String [] { ".*e.*" } ).size() );
		
		
		assertEquals( 0, q.matchAny( testSet, EmptyStrings, EmptyStrings ).size() );
		assertEquals( 0, q.matchAny( testSet, null, EmptyStrings ).size() );
		assertEquals( 0, q.matchAny( testSet, EmptyStrings, null ).size() );
	}
}






