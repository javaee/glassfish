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
 * JndiMBeanImplTest.java
 * JUnit based test
 *
 * Created on April 1, 2004, 5:04 PM
 */

package com.sun.enterprise.admin.monitor.jndi;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import javax.naming.NameClassPair;
import javax.naming.NamingException;
import junit.framework.*;

/**
 *
 * @author Rob
 */
public class JndiMBeanImplTest extends TestCase {
    
    public JndiMBeanImplTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(JndiMBeanImplTest.class);
        return suite;
    }
    
    /**
     * Test of initialize method, of class com.sun.enterprise.admin.monitor.jndi.JndiMBeanImpl.
     */
    public void testInitialize() {
        System.out.println("testInitialize");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getAttribute method, of class com.sun.enterprise.admin.monitor.jndi.JndiMBeanImpl.
     */
    public void testGetAttribute() {
        System.out.println("testGetAttribute");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getAttributes method, of class com.sun.enterprise.admin.monitor.jndi.JndiMBeanImpl.
     */
    public void testGetAttributes() {
        System.out.println("testGetAttributes");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getMBeanInfo method, of class com.sun.enterprise.admin.monitor.jndi.JndiMBeanImpl.
     */
    public void testGetMBeanInfo() {
        System.out.println("testGetMBeanInfo");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getOperationInfo method, of class com.sun.enterprise.admin.monitor.jndi.JndiMBeanImpl.
     */
    public void testGetOperationInfo() {
        System.out.println("testGetOperationInfo");
        fail("The test case is empty.");
    }
    
    /**
     * Test of createOperationInfo method, of class com.sun.enterprise.admin.monitor.jndi.JndiMBeanImpl.
     */
    public void testCreateOperationInfo() {
        System.out.println("testCreateOperationInfo");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getParameterInfo method, of class com.sun.enterprise.admin.monitor.jndi.JndiMBeanImpl.
     */
    public void testGetParameterInfo() {
        System.out.println("testGetParameterInfo");
        fail("The test case is empty.");
    }
    
    /**
     * Test of isAttrGetterOrSetter method, of class com.sun.enterprise.admin.monitor.jndi.JndiMBeanImpl.
     */
    public void testIsAttrGetterOrSetter() {
        System.out.println("testIsAttrGetterOrSetter");
        fail("The test case is empty.");
    }
    
    /**
     * Test of invoke method, of class com.sun.enterprise.admin.monitor.jndi.JndiMBeanImpl.
     */
    public void testInvoke() {
        System.out.println("testInvoke");
        fail("The test case is empty.");
    }
    
    /**
     * Test of setAttribute method, of class com.sun.enterprise.admin.monitor.jndi.JndiMBeanImpl.
     */
    public void testSetAttribute() {
        System.out.println("testSetAttribute");
        fail("The test case is empty.");
    }
    
    /**
     * Test of setAttributes method, of class com.sun.enterprise.admin.monitor.jndi.JndiMBeanImpl.
     */
    public void testSetAttributes() {
        System.out.println("testSetAttributes");
        fail("The test case is empty.");
    }
    
    /**
     * Test of getNames method, of class com.sun.enterprise.admin.monitor.jndi.JndiMBeanImpl.
     */
    public void testGetNames() {
        System.out.println("testGetNames");
        fail("The test case is empty.");
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
