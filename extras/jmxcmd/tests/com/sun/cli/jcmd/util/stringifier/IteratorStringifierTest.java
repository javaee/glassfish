/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jcmd/util/stringifier/IteratorStringifierTest.java,v 1.1 2003/11/12 02:25:27 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/12 02:25:27 $
 */
 
package com.sun.cli.jcmd.util.stringifier;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;


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
		final Set		s	= new HashSet();
		final Iterator	iter	= s.iterator();
		
		final String	stringified	= IteratorStringifier.stringify( iter, ",");
		
		assertEquals( "", stringified );
	}
	
	
		public void
	testSingle(  ) throws Exception
	{
		final Set		s	= new HashSet();
		s.add( "hello" );
		
		final Iterator	iter	= s.iterator();
		
		final String	stringified	= IteratorStringifier.stringify( iter, ",");
		
		assertEquals( "hello", stringified );
	}
	
	
	
		public void
	testDouble(  ) throws Exception
	{
		final ArrayList		list	= new ArrayList();
		list.add( "hello" );
		list.add( "there" );
		
		final Iterator	iter	= list.iterator();
		
		final String	stringified	= IteratorStringifier.stringify( iter, ",");
		
		assertEquals( "hello,there", stringified );
	}
}

