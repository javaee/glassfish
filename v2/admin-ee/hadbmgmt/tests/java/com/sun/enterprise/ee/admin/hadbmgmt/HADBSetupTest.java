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
 * HADBSetupTest.java
 * JUnit based test
 *
 * Created on April 10, 2004, 12:12 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import junit.framework.*;

/**
 *
 * @author bnevins
 */
public class HADBSetupTest extends TestCase
{
	
	public HADBSetupTest(java.lang.String testName)
	{
		super(testName);
	}
	
	public static Test suite()
	{
		TestSuite suite = new TestSuite(HADBSetupTest.class);
		return suite;
	}
	
	/** Test of setup method, of class hadb.HADBSetup. */
	public void testSetup()
	{
		System.out.println("Test HADB Setup -- Please give me some info first:\n");
		
		String installPath = "/81ee/SUNWhadb/4.3-0.16";
		String configPath = "/81ee/config/SUNWhadb/dbdef";
		String host1 = "bulldozer.red.iplanet.com";
		String host2 = "bulldozer.red.iplanet.com";

		
		String s = Console.readLine("HADB Install Path [" + installPath + "]: ");
		if(s.length() > 0)
			installPath = s;

		s = Console.readLine("HADB Config Path [" + configPath + "]: ");
		if(s.length() > 0)
			configPath = s;
		
		s = Console.readLine("First Host [" + host1 + "]: ");
		if(s.length() > 0)
			host1 = s;
		
		s = Console.readLine("Second Host [" + host2 + "]: ");
		if(s.length() > 0)
			host2 = s;
		
		String clusterName = Console.readLine("Cluster Name: ");

		
		try
		{
			HADBInfo info = new HADBInfo(installPath, configPath, new String[] {host1, host2 }, clusterName, null);
			HADBSetup hs = new HADBSetup(info);
			hs.setup();
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			fail(" Got an unexpected Exception: " + e);
		}
	}
	
	public static void main(java.lang.String[] args)
	{
		junit.textui.TestRunner.run(suite());
	}
	final static String phonyExePass = "D:/hadb/src/cpp/hadbm/Debug/hadbm.exe";
	final static String phonyExeFail = "D:/hadb/src/cpp/hadbm/Debug/hadbm_fail.exe";
}
