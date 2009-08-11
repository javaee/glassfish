/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jcmd/util/stringifier/IteratorStringifierTest.java,v 1.1 2003/11/12 02:25:27 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/12 02:25:27 $
 */
 
package org.glassfish.admin.amx.util.stringifier;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;


/**
	Stringifies an Iterator, using an optional element Stringifier
 */
 
public final class IteratorStringifierTest extends junit.framework.TestCase
{
		public
	IteratorStringifierTest()
	{
	}
	
		public void
	testEmpty(  ) throws Exception
	{
		final Set<String>		s	= new HashSet<String>();
		final Iterator<String>	iter	= s.iterator();
		
		final String	stringified	= IteratorStringifier.stringify( iter, ",");
		
		assertEquals( "", stringified );
	}
	
	
		public void
	testSingle(  ) throws Exception
	{
		final Set<String>		s	= new HashSet<String>();
		s.add( "hello" );
		
		final Iterator<String>	iter	= s.iterator();
		
		final String	stringified	= IteratorStringifier.stringify( iter, ",");
		
		assertEquals( "hello", stringified );
	}
	
	
	
		public void
	testDouble(  ) throws Exception
	{
		final List<String>		list	= new ArrayList<String>();
		list.add( "hello" );
		list.add( "there" );
		
		final Iterator<String>	iter	= list.iterator();
		
		final String	stringified	= IteratorStringifier.stringify( iter, ",");
		
		assertEquals( "hello,there", stringified );
	}
}

