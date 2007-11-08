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
 * StatsHolderImplTest.java
 * JUnit based test
 *
 * Created on April 1, 2004, 5:04 PM
 */

package com.sun.enterprise.admin.monitor.registry.spi;

import com.sun.enterprise.admin.monitor.registry.*;
import javax.management.j2ee.statistics.*;
import javax.management.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Collection;
import java.util.logging.*;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
import junit.framework.*;

/**
 *
 * @author Rob
 */
public class StatsHolderImplTest extends TestCase {
    
    public StatsHolderImplTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(StatsHolderImplTest.class);
        return suite;
    }
    
    /**
     * Test of addChild method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testAddChild() {
        System.out.println("testAddChild");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getAllChildren method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testGetAllChildren() {
        System.out.println("testGetAllChildren");
        fail("The test case is empty.");
    }
    
    /**
     * Test of removeAllChildren method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testRemoveAllChildren() {
        System.out.println("testRemoveAllChildren");
        fail("The test case is empty.");
    }
    
    /**
     * Test of removeChild method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testRemoveChild() {
        System.out.println("testRemoveChild");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getName method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testGetName() {
        System.out.println("testGetName");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getType method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testGetType() {
        System.out.println("testGetType");
        fail("The test case is empty.");
    }
    
    /**
     * Test of setStats method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testSetStats() {
        System.out.println("testSetStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getStats method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testGetStats() {
        System.out.println("testGetStats");
        fail("The test case is empty.");
    }
    
    /**
     * Test of setObjectName method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testSetObjectName() {
        System.out.println("testSetObjectName");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getObjectName method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testGetObjectName() {
        System.out.println("testGetObjectName");
        fail("The test case is empty.");
    }
    
    /**
     * Test of setDottedName method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testSetDottedName() {
        System.out.println("testSetDottedName");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getDottedName method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testGetDottedName() {
        System.out.println("testGetDottedName");
        fail("The test case is empty.");
    }
    
    /**
     * Test of registerMBean method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testRegisterMBean() {
        System.out.println("testRegisterMBean");
        fail("The test case is empty.");
    }
    
    /**
     * Test of unregisterMBean method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testUnregisterMBean() {
        System.out.println("testUnregisterMBean");
        fail("The test case is empty.");
    }
    
    /**
     * Test of setType method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testSetType() {
        System.out.println("testSetType");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getChild method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testGetChild() {
        System.out.println("testGetChild");
        fail("The test case is empty.");
    }
    
    /**
     * Test of write method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testWrite() {
        System.out.println("testWrite");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getStatsClass method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testGetStatsClass() {
        System.out.println("testGetStatsClass");
        fail("The test case is empty.");
    }
    
    /**
     * Test of setStatsClass method, of class com.sun.enterprise.admin.monitor.registry.spi.StatsHolderImpl.
     */
    public void testSetStatsClass() {
        System.out.println("testSetStatsClass");
        fail("The test case is empty.");
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
