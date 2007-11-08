/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/cmd/ArgHelperOptionsInfoTest.java,v 1.3 2005/12/25 03:45:28 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:28 $
 */
package com.sun.cli.jmx.cmd;

import java.util.ArrayList;
import java.util.Iterator;
 


public class ArgHelperOptionsInfoTest extends junit.framework.TestCase
{

		public void
	testCreate()
		throws ArgHelper.IllegalOptionException
	{
		new ArgHelperOptionsInfo();
		new ArgHelperOptionsInfo( "a b c" );
		new ArgHelperOptionsInfo( "aa bb,2 cc" );
		new ArgHelperOptionsInfo( "long-option-dashed-name" );
		new ArgHelperOptionsInfo( "long.option.dotted.name" );
		new ArgHelperOptionsInfo( "long_option_underscore_name" );
		new ArgHelperOptionsInfo( "long-option_mixed.name" );
	}
	
	
		void
	checkCreateFailure( String options )
		throws ArgHelper.IllegalOptionException
	{
		try
		{
			new ArgHelperOptionsInfo( options );
			fail( "expected failure from option description \"" + options + "\"" );
		}
		catch( Exception e )
		{
			// good
		}
	}
	
		public void
	testCreateFailures()
		throws ArgHelper.IllegalOptionException
	{
		final String	illegals	= "~`!@#$%^&*()+={}[]|\\:;\"'<>?,/";
		
		for( int i = 0; i < illegals.length(); ++i )
		{
			checkCreateFailure( "" + illegals.charAt( i ) );
		}
	}
	
	
		public void
	testIllegals()
		throws ArgHelper.IllegalOptionException
	{
		final ArgHelperOptionsInfo	info	= new ArgHelperOptionsInfo( "hello h" );
		
		assertEquals( false, info.isLegalOption( "hello" ) );
		assertEquals( false, info.isLegalOption( "-hello" ) );
		assertEquals( false, info.isLegalOption( "h" ) );
		assertEquals( false, info.isLegalOption( "--h" ) );
		assertEquals( false, info.isLegalOption( "-" ) );
		assertEquals( false, info.isLegalOption( "--" ) );
		assertEquals( false, info.isLegalOption( "" ) );
	}
	
		public void
	testLegals()
		throws ArgHelper.IllegalOptionException
	{
		final ArgHelperOptionsInfo	info	= new ArgHelperOptionsInfo( "hello --there,3 x,1 -y,1" );
		
		assertEquals( true, info.isLegalOption( "--hello" ) );
		assertEquals( true, info.isLegalOption( "--there" ) );
		assertEquals( true, info.isLegalOption( "-x" ) );
		assertEquals( true, info.isLegalOption( "-y" ) );
	}
	
	
		void
	checkRedundantFailure( String args )
	{
		try
		{
			new ArgHelperOptionsInfo( args );
			fail( "expected rejection of redundant option" );
		}
		catch( Exception e )
		{
		}
	}

		public void
	testRedundant()
		throws ArgHelper.IllegalOptionException
	{
		checkRedundantFailure( "a a" );
		checkRedundantFailure( "aa aa" );
		checkRedundantFailure( "--aa --aa" );
		checkRedundantFailure( "-a -a" );
	}

		public void
	testMixedArgs()
		throws ArgHelper.IllegalOptionException
	{
		final ArgHelperOptionsInfo	info	= new ArgHelperOptionsInfo( "a b,1 c,2 d,3 --xyz,4" );
		
		assertEquals( true, info.isLegalOption( "-a" ) );
		assertEquals( false, info.isLegalOption( "--a" ) );
		assertEquals( 1, info.getNumValues( "-a" ) );
		assertEquals( true, info.isBoolean( "-a" ) );
		
		assertEquals( 1, info.getNumValues( "-b" ) );
		assertEquals( 2, info.getNumValues( "-c" ) );
		assertEquals( 3, info.getNumValues( "-d" ) );
		assertEquals( 4, info.getNumValues( "--xyz" ) );
		
	}



		public
	ArgHelperOptionsInfoTest()
	{
	}

		public void
	setUp()
	{
	}
	
		public void
	tearDown()
	{
	}

}

