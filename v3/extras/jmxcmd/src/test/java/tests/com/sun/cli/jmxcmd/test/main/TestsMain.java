/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
/*
 * $Header: /m/jws/jmxcmd/tests/com/sun/cli/jmxcmd/test/main/TestsMain.java,v 1.3 2003/12/05 01:18:08 llc Exp $
 * $Revision: 1.3 $
 * $Date: 2003/12/05 01:18:08 $
 */
 
package com.sun.cli.jmxcmd.test.main;

import java.util.List;

import junit.extensions.ActiveTestSuite;
import junit.framework.TestCase;

import com.sun.cli.jcmd.util.cmd.ArgHelperTest;
import com.sun.cli.jcmd.util.cmd.OptionsInfoTest;
import com.sun.cli.jcmd.util.misc.TokenizerTest;
import com.sun.cli.jcmd.util.misc.CompareUtilTest;
import com.sun.cli.jcmd.util.misc.StringEscaperTest;
import com.sun.cli.jmxcmd.support.ArgParserTest;
import com.sun.cli.jmxcmd.support.AliasMgrTest;
import com.sun.cli.jmxcmd.support.CLISupportMBeanImplTest;
import org.glassfish.admin.amx.util.ListUtil;
import org.glassfish.admin.amx.util.TypeCast;
import org.glassfish.admin.amx.util.jmx.ObjectNameQueryImplTest;
import org.glassfish.admin.amx.util.stringifier.IteratorStringifierTest;



@org.junit.Ignore
public class TestsMain
{
    @SuppressWarnings("unchecked")
	final static List<Class<? extends TestCase>>  TEST_CLASSES = TypeCast.asList(ListUtil.newList( new Class[] {
		StringEscaperTest.class,
		CompareUtilTest.class,
		
		TokenizerTest.class,
		
		IteratorStringifierTest.class,
		
		OptionsInfoTest.class,
		ArgHelperTest.class,
		
		ArgParserTest.class,
		
		AliasMgrTest.class,
		ObjectNameQueryImplTest.class,
		CLISupportMBeanImplTest.class}
	));
	
		static void
	testClass( Class<? extends TestCase> theClass )
	{
		System.out.println( "*** testing " + theClass.getName() + " ***");
		// use 'ActiveTestSuite' to thread the tests
		final ActiveTestSuite	suite	= new ActiveTestSuite( theClass );
		junit.textui.TestRunner.run( suite );
	}
	
		public static void
	main( String [] args )
	{
        for( final Class<? extends TestCase> clazz : TEST_CLASSES )
		{
			testClass( clazz );
		}
	}
}


