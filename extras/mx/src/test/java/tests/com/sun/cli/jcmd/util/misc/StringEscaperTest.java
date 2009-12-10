/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jcmd/util/misc/StringEscaperTest.java,v 1.1 2003/11/12 02:25:26 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2003/11/12 02:25:26 $
 */
 
package com.sun.cli.jcmd.util.misc;


import junit.framework.TestCase;


public class StringEscaperTest extends TestCase
{
		public
	StringEscaperTest( )
	{
	}

		public void
	testEmpty()
	{
		final StringEscaper	escaper	= new StringEscaper();
		
		assertEquals( "", escaper.unescape( escaper.escape( "" ) ) );
	}
	
		public void
	testSingleNormalChar()
	{
		final StringEscaper	escaper	= new StringEscaper();
		final String	test	= "X";
		
		assertEquals( test, escaper.unescape( escaper.escape( test ) ) );
	}
	
		public void
	testSingleEscapeChar()
	{
		final StringEscaper	escaper	= new StringEscaper();
		assertEquals( "\\", escaper.unescape( escaper.escape( "\\" ) ) );
	}
	
		public void
	testNewlineEtc()
	{
		final StringEscaper	escaper	= new StringEscaper();
		final String	test	= "\\\n\r\t";
		
		assertEquals( test, escaper.unescape( escaper.escape( test ) ) );
	}
	
	
		public void
	testUnicode()
	{
		final String		test	= "\u2345\u0191\uFEAD";
		final StringEscaper	escaper	= new StringEscaper( test );
		
		assertEquals( test, escaper.unescape( escaper.escape( test ) ) );
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



