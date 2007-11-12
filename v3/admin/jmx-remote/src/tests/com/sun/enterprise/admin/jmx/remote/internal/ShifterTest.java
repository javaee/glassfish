/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
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
