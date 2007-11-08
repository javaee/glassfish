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

/*
 * MutableBoundedRangeStatisticImplTest.java
 * JUnit based test
 *
 * Created on April 1, 2004, 4:35 PM
 */

package com.sun.enterprise.admin.monitor.stats;

import javax.management.j2ee.statistics.BoundedRangeStatistic;
import com.sun.enterprise.admin.monitor.stats.BoundedRangeStatisticImpl;
import javax.management.j2ee.statistics.Statistic;
import junit.framework.*;

/**
 *
 * @author Rob
 */
public class MutableBoundedRangeStatisticImplTest extends TestCase {
    
    public MutableBoundedRangeStatisticImplTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(MutableBoundedRangeStatisticImplTest.class);
        return suite;
    }
    
    /**
     * Test of reset method, of class com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl.
     */
    public void testReset() {
        System.out.println("testReset");
        fail("The test case is empty.");
    }
    
    /**
     * Test of setCount method, of class com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl.
     */
    public void testSetCount() {
        System.out.println("testSetCount");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unmodifiableView method, of class com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl.
     */
    public void testUnmodifiableView() {
        System.out.println("testUnmodifiableView");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getDescription method, of class com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl.
     */
    public void testGetDescription() {
        System.out.println("testGetDescription");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getLastSampleTime method, of class com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl.
     */
    public void testGetLastSampleTime() {
        System.out.println("testGetLastSampleTime");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getName method, of class com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl.
     */
    public void testGetName() {
        System.out.println("testGetName");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getStartTime method, of class com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl.
     */
    public void testGetStartTime() {
        System.out.println("testGetStartTime");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getUnit method, of class com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl.
     */
    public void testGetUnit() {
        System.out.println("testGetUnit");
        fail("The test case is empty.");
    }
    
    /**
     * Test of modifiableView method, of class com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl.
     */
    public void testModifiableView() {
        System.out.println("testModifiableView");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getCurrent method, of class com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl.
     */
    public void testGetCurrent() {
        System.out.println("testGetCurrent");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getHighWaterMark method, of class com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl.
     */
    public void testGetHighWaterMark() {
        System.out.println("testGetHighWaterMark");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getLowWaterMark method, of class com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl.
     */
    public void testGetLowWaterMark() {
        System.out.println("testGetLowWaterMark");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getLowerBound method, of class com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl.
     */
    public void testGetLowerBound() {
        System.out.println("testGetLowerBound");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getUpperBound method, of class com.sun.enterprise.admin.monitor.stats.MutableBoundedRangeStatisticImpl.
     */
    public void testGetUpperBound() {
        System.out.println("testGetUpperBound");
        fail("The test case is empty.");
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
