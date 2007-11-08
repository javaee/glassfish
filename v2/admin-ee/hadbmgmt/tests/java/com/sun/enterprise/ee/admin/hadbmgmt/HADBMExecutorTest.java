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
 * HADBMExecutorTest.java
 * JUnit based test
 *
 * Created on April 10, 2004, 11:18 AM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import java.io.*;
import junit.framework.*;

/**
 *
 * @author bnevins
 */
public class HADBMExecutorTest extends TestCase
{
	
	public HADBMExecutorTest(java.lang.String testName)
	{
		super(testName);
	}
	
	public static Test suite()
	{
		TestSuite suite = new TestSuite(HADBMExecutorTest.class);
		return suite;
	}
	
	/** Test of exec method, of class hadb.HADBMExecutor. */
	public void testExecPass()
	{
		System.out.println("testExecPass");
		
		
		try
		{
			HADBMExecutor exe = new HADBMExecutor(phonyExePass);
			int exitValue = exe.exec("foo goo hoo");
			assertEquals(exitValue, 0);
			System.out.println("*****  stdout ****\n" + exe.getStdout());
			System.out.println("*****  stderr ****\n" + exe.getStderr());
			
		}
		catch(HADBSetupException hse)
		{
			fail("Unexpected Exception: " + hse);
		}
	}
	public void testExecFail()
	{
		System.out.println("testExecFail");
		
		try
		{
			HADBMExecutor exe = new HADBMExecutor(phonyExeFail);
			int exitValue = exe.exec("foo goo hoo");
			System.out.println("*****  stdout ****\n" + exe.getStdout());
			System.out.println("*****  stderr ****\n" + exe.getStderr());
			assertEquals(1, exitValue);
		}
		catch(HADBSetupException hse)
		{
			fail("Unexpected Exception: " + hse);
		}
	}

	public static void main(java.lang.String[] args)
	{
		junit.textui.TestRunner.run(suite());
	}
	
	final static String phonyExePass = "D:/hadb/src/cpp/hadbm/Debug/hadbm.exe";
	final static String phonyExeFail = "D:/hadb/src/cpp/hadbm/Debug/hadbm_fail.exe";
}
