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
 * $Id: ShifterTest.java,v 1.3 2005/12/25 04:26:45 tcfujii Exp $
 * $Date: 2005/12/25 04:26:45 $
 * $Revision: 1.3 $
 */
package com.sun.enterprise.admin.jmx.remote.internal;

import junit.framework.*;

/**
 * @author <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since $Revision: 1.3 $
 */
public class ShifterTest extends TestCase {

	public void testRightLeftShiftFromEmpty() {
		final Object[] s = new Object[]{};
		final Shifter ss = new Shifter(s);
		ss.shiftRight(new Object());
		ss.shiftLeft();
		assertEquals(ss.state().length, 0);
	}
	public void testImpossibleLeftShift() {
		try {
			final String[] s = new String[]{};
			final Shifter sh = new Shifter(s);
			final Object r = sh.shiftLeft();
			fail("Should have thrown IllegalStateException, but didn't, hence test fails");
		}
		catch(Exception e){}
	}
	public void testShiftLeftOne() {
		final String one = "one";
		final String[] s = new String[]{one};
		final Shifter sh = new Shifter(s);
		final Object r = sh.shiftLeft();
		assertEquals(one, r);
	}
	public void testShiftRightFromTwo() {
		final Object[] s = new Object[]{new Object(), new Object()};
		final Shifter sh = new Shifter(s);
		final String add = "8";
		sh.shiftRight(add);
		assertEquals(sh.state().length, 3);
	}
	public void testShiftRightOneFromEmpty() {
		final String[] s = new String[]{};
		final Shifter sh = new Shifter(s);
		final String add = "8";
		sh.shiftRight(add);
		assertEquals(sh.state().length, 1);
	}
	public void testShiftRightOne() {
		final String[] s = new String[]{"one"};
		final Shifter sh = new Shifter(s);
		final String add = "8";
		sh.shiftRight(add);
		assertEquals(sh.state().length, 2);
	}
	public void testCreate() {
		final Shifter s = new Shifter(new String[]{"element1"});
	}
	public ShifterTest(java.lang.String testName) {
		super(testName);
	}
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private void nyi() {
		fail("Not yet implemented");
	}
	
	public static Test suite(){
		TestSuite suite = new TestSuite(ShifterTest.class);
		return suite;
	}
	
	public static void main(String args[]){
		junit.textui.TestRunner.run(suite());
		//junit.swingui.TestRunner.run(suite());
	}
	
}
