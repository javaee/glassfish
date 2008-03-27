/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jcmd/util/misc/CompareUtilTest.java,v 1.1 2003/11/12 02:25:26 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/12 02:25:26 $
 */
 
package com.sun.cli.jcmd.util.misc;


import junit.framework.TestCase;



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



