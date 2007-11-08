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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.enterprise.admin.monitor.stats;

import javax.management.j2ee.statistics.*;
import com.sun.enterprise.admin.monitor.stats.*;
import java.util.*;
import junit.framework.*;

/** Tests the class GenericStatsImplTescc.
 * No need to import the class being tested, as the package name is the same.
 * @author <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @version $Revision: 1.2 $
 */
public class GenericStatsImplTest extends TestCase {
	
	public void testCorrectClassNameConstructor() {
		try {
			final String c = "javax.management.j2ee.statistics.EJBStats";
			final EJBStats provider = createEjbStatsProvider();
			final Stats stats = new GenericStatsImpl(c, provider);
			assertNotNull("Object is not null", stats);
		}
		catch (Exception e) {
			// It should never get here as this should never be exceptional
		}
	}
	
	public void testInCorrectClassNameConstructor() {
		try {
			final String invalid = "javax.management.j2ee.statistics.UnknownStats";
			final EJBStats provider = createEjbStatsProvider();
			final Stats stats = new GenericStatsImpl(invalid, provider);
			fail("How can javax.management.j2ee.statistics.UnknownStats be impl????");
		}
		catch (Exception e) {
			//We always should get this exception -- so this test should PASS.
		}
	}
	
	public void testInvalidInterface() {
		try {
			final String invalid = "java.io.Serializable";
			final EJBStats provider = createEjbStatsProvider();
			final Stats stats = new GenericStatsImpl(invalid, provider);
			fail("We have not committed to java.io.Serializable as it is not a Stats interface");
		}
		catch (Exception e) {
			//We always should get this exception -- so this test should PASS.
		}
	}
	
	public void testGetStatisticNamesWithEjbStats() {
		try {
			final String c = "javax.management.j2ee.statistics.EJBStats";
			final EJBStats provider = createEjbStatsProvider();
			final Stats stats = new GenericStatsImpl(c, provider);
			final String[] s1 = stats.getStatisticNames();
			final String[] s2 = new String[]{"CreateCount", "RemoveCount"};
			final List ls1 = Arrays.asList(s1);
			final List ls2 = Arrays.asList(s2);
			assertTrue(ls1.containsAll(ls2)); //order is unimportant
			assertEquals(s1.length, s2.length);
		}
		catch (Exception e) {
			// It should never get here as this should never be exceptional
		}
	}
	
	public void testGetAStatisticWithEjbStats() {
		try {
			final String c = "javax.management.j2ee.statistics.EJBStats";
			final EJBStats provider = createEjbStatsProvider();
			final Stats stats = new GenericStatsImpl(c, provider);
			final String[] names = new String[]{"CreateCount", "RemoveCount"};
			final CountStatistic c1 = new CountStatisticImpl(10, names[0], "", "Beans Created", 10, 10);
			final CountStatistic c2 = new CountStatisticImpl(5, names[1], "", "Beans Removed", 10, 10);

			/* tests for equality */
			final CountStatistic cc = (CountStatistic)stats.getStatistic(names[0]);
			final CountStatistic rc = (CountStatistic)stats.getStatistic(names[1]);
			assertEquals(c1.getCount(), cc.getCount());
			assertEquals(c1.getName(), cc.getName());

			final CountStatistic  u = (CountStatistic)stats.getStatistic(names[1]);
			assertEquals(c2.getCount(), rc.getCount());
			assertEquals(c2.getName(), rc.getName());

			/* tests for inequality */
			final CountStatistic c3 = new CountStatisticImpl(1111, names[0], "", "Beans Created", 10, 10);
			final CountStatistic c4 = new CountStatisticImpl(444, names[1], "", "Beans Removed", 10, 10);
			
			assertTrue(c3.getCount() != cc.getCount());			
			assertTrue(c4.getCount() != rc.getCount());
		}
		catch (Exception e) {
			// It should never get here as this should never be exceptional
		}
	}
	public GenericStatsImplTest(java.lang.String testName) {
		super(testName);
	}
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	private EJBStats createEjbStatsProvider() {
		return new EjbStatsImpl();
	}
	
	private static class EjbStatsImpl implements EJBStats, java.io.Serializable {
		final Map m = new HashMap();
		final String[] names = new String[]{"CreateCount", "RemoveCount"};
		final CountStatistic c = new CountStatisticImpl(10, names[0], "", "Beans Created", 10, 10);
		final CountStatistic r = new CountStatisticImpl(5, names[1], "", "Beans Removed", 10, 10);
		EjbStatsImpl() {
			m.put(names[0], c);
			m.put(names[1], r);
		}
		
		public CountStatistic getCreateCount() {
			return ( c );
		}
		
		public CountStatistic getRemoveCount() {
			return ( r );
		}
		
		public Statistic getStatistic(String str) {
			return ( (Statistic) m.get(str) );
		}
		
		public String[] getStatisticNames() {
			return  ( names );
		}
		
		public Statistic[] getStatistics() {
			return ( (Statistic[]) m.values().toArray() );
		}
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(GenericStatsImplTest.class);
		return suite;
	}
	
	public static void main(String args[]){
		junit.textui.TestRunner.run(suite());
		//junicc.swingui.TestRunner.run(suite());
	}
	
}
