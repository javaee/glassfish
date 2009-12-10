/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package com.sun.cli.jcmd.util.misc;


import junit.framework.TestCase;
import org.glassfish.admin.amx.util.CompareUtil;

public class CompareUtilTest extends TestCase
{
		public
	CompareUtilTest( )
	{
	}
	

		public void
	testBothNull()
	{
		assertEquals( true, CompareUtil.objectsEqual( null, null ) );
	}
	
		public void
	testSingleNull()
	{
		assertEquals( false, CompareUtil.objectsEqual( "hello", null ) );
		assertEquals( false, CompareUtil.objectsEqual( null, "hello" ) );
	}
	
	
		public void
	testArrays()
	{
		assertEquals( true, CompareUtil.objectsEqual( new String[0], new String[0]  ) );
		assertEquals( true,
			CompareUtil.objectsEqual( new String[] { "hello" }, new String[] { "hello" }) );
	}
	
	
	
		public void
	testSameObject()
	{
		final String	x	= "hello";
		
		assertEquals( true, CompareUtil.objectsEqual( x, x ) );
	}
	
	
		public void
	testArrayOfArrays()
	{
		final String[]	a1	= new String[] { "hello", "there" };
		final String[]	a2	= new String[] { "hello", "there" };
		
		final String[][]	o1	= new String[][] { a1 };
		final String[][]	o2	= new String[][] { a2 };
		
		assertEquals( true, CompareUtil.objectsEqual( o1, o2 ) );
	}
	
	
	//-------------------------------------------------------------------------


	
		protected void
	setUp()
	{
	}
	
		protected void
	tearDown()
	{
	}
	
};



