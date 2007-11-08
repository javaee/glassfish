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
 * $Id: ValueListMapTest.java,v 1.3 2005/12/25 03:43:53 tcfujii Exp $
 * $Date: 2005/12/25 03:43:53 $
 * $Revision: 1.3 $
 */
package com.sun.enterprise.admin.monitor.registry.spi;

import javax.management.j2ee.statistics.*;
import com.sun.enterprise.admin.monitor.registry.*;
import com.sun.enterprise.admin.monitor.stats.*;
import java.util.*;
import junit.framework.*;

/**
 * @author <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since $Revision: 1.3 $
 */
public class ValueListMapTest extends TestCase {
	
	/* Write your unit tests here as methods ending in "Test". */
	private MonitoringLevelListener[] jvml;
	private MonitoringLevelListener[] ejbl;
	public void testCreateMap() {
		final Map m = new ValueListMap();
	}
	public void testInvalidPut() {
		try {
			final Map m = new ValueListMap(); //value with listener class.
			final String key = "key1";
			final String val = "Invalid Value"; //attempt to put a value with String class.
			m.put(key, val);
			fail ("Should have thrown IllegalArgumentException - Failed Test");
		}
		catch(IllegalArgumentException e) {}
	}
	public void testJvmListenerAdd() {
		final Map m = new ValueListMap();
		m.put(MonitoredObjectType.JVM, jvml[0]);
		m.put(MonitoredObjectType.JVM, jvml[1]);
		final Map n = (Map) m.get(MonitoredObjectType.JVM); //I know it is a map
		assertEquals(n.keySet().size(), 2);
	}
	public void testJvmListenerAddRemove() {
		final Map m = new ValueListMap();
		m.put(MonitoredObjectType.JVM, jvml[0]);
		m.put(MonitoredObjectType.JVM, jvml[1]);
		m.remove(jvml[1]);
		final Map n = (Map) m.get(MonitoredObjectType.JVM); //I know it is a map
		assertEquals(n.keySet().size(), 1);
	}
	
	public void testRemoveJvmType() {
		final Map m = new ValueListMap();
		m.put(MonitoredObjectType.JVM, jvml[0]);
		m.put(MonitoredObjectType.JVM, jvml[1]);
		final Collection removed = (Collection)m.remove(MonitoredObjectType.JVM);
		assertEquals(m.get(MonitoredObjectType.JVM), null);
		assertEquals(removed.size(), 2);
		final Iterator it = removed.iterator();
		while (it.hasNext()) {
			final Object n = it.next();
			assertTrue(n == jvml[0] || n == jvml[1]); //order is not known
		}
	}
	
	public void testAddOneListenerForTwoTypes() {
		final Map m = new ValueListMap();
		m.put(MonitoredObjectType.JVM, jvml[0]);
		m.put(MonitoredObjectType.EJB, jvml[0]);
		//now there should be two keys created
		assertEquals(m.keySet().size(), 2);
		//removed Collection should also contain 2 elements.
		final Collection removed = (Collection) m.remove(jvml[0]); //It is a collection
		assertEquals(removed.size(), 2);
		final Iterator it = removed.iterator();
		while (it.hasNext()) {
			assertSame(jvml[0], it.next());
		}
	}
	public ValueListMapTest(java.lang.String testName) {
		super(testName);
		createListeners();
	}
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private void createListeners() {
		final int nj = 2;
		jvml = new MonitoringLevelListener[nj];
		jvml[0] = new JvmListener();
		jvml[1] = new JvmListener();
		final int ne = 3;
		ejbl = new MonitoringLevelListener[ne];
		ejbl[0] = new EjbListener();
		ejbl[1] = new EjbListener();
		ejbl[2] = new EjbListener();
	}
	
	private static class JvmListener implements MonitoringLevelListener {
		JvmListener() {
		}
		public void changeLevel(MonitoringLevel from, MonitoringLevel to, MonitoredObjectType type) {
			System.out.println("from = " + from + " to = " + to + " type = " + type);
		}
		
		public void changeLevel(MonitoringLevel from, MonitoringLevel to, javax.management.j2ee.statistics.Stats handback) {
			//@deprecated
		}
		public void setLevel(MonitoringLevel level) {
			//@deprecated
		}
	}
	private static class EjbListener implements MonitoringLevelListener {
		EjbListener() {
		}
		public void changeLevel(MonitoringLevel from, MonitoringLevel to, MonitoredObjectType type) {
			System.out.println("from = " + from + " to = " + to + " type = " + type);
		}
		
		public void changeLevel(MonitoringLevel from, MonitoringLevel to, javax.management.j2ee.statistics.Stats handback) {
			//@deprecated
		}
		public void setLevel(MonitoringLevel level) {
			//@deprecated
		}
	}
	private void nyi() {
		fail("Not yet implemented");
	}
	
	public static Test suite(){
		TestSuite suite = new TestSuite(ValueListMapTest.class);
		return suite;
	}
	
	public static void main(String args[]){
		junit.textui.TestRunner.run(suite());
		//junit.swingui.TestRunner.run(suite());
	}
	
}
