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


