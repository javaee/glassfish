/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * MailResourceDeployerTest.java
 *
 * Created on December 10, 2003, 11:55 AM
 */

package com.sun.enterprise.resource;

import com.sun.enterprise.config.serverbeans.ElementProperty;
import java.util.Properties;
import junit.framework.*;
import junit.textui.TestRunner;

/**
 * Unit test for ConnectorConnectionPoolDeployer.
 *
 * @author Rob Ruyak
 */
public class GlobalResourceDeployerTest extends TestCase {

    MailResourceDeployer deployer;
    ElementProperty [] testProps;
    String [] testNames;
    String [] testValues;
    
    /** Creates a new instance of ConnectorConnectionPoolDeployerTest */
    public GlobalResourceDeployerTest(String name) {
         super(name);
    }

    /**
     * Tests the getPropNamesAsStrArr method.
     *
     */
    public void testGetPropNamesAsStrArr() {
        String [] result = deployer.getPropNamesAsStrArr(testProps);
        assertNotNull(result);
        assertEquals(result[0], testNames[0]);
        assertEquals(result[1], testNames[1]);
        assertEquals(result[2], testNames[2]);
    }
    
    /**
     * Tests the getPropNamesAsStrArr method with null param.
     *
     */
    public void testGetPropNamesAsStrArrWithNull() {
        String [] result = deployer.getPropNamesAsStrArr(null);
        assertNull(result);
    }
    
    /**
     * Tests the getPropValuesAsStrArr method.
     *
     */
    public void testGetPropValuesAsStrArr() {
        String [] result = deployer.getPropValuesAsStrArr(testProps);
        assertNotNull(result);
        assertEquals(result[0], testValues[0]);
        assertEquals(result[1], testValues[1]);
        assertEquals(result[2], testValues[2]);   
    }
    
    /**
     * Tests the getPropValuesAsStrArr method with null param.
     */
    public void testGetPropValuesAsStrArrWithNull() {
        String [] result = deployer.getPropValuesAsStrArr(null);
        assertNull(result);
    }
    
    protected void setUp() {
       deployer = new MailResourceDeployer();
       ElementProperty prop1 = new ElementProperty();
       prop1.setName("user");
       prop1.setValue("admin");
       ElementProperty prop2 = new ElementProperty();
       prop2.setName("password");
       prop2.setValue("adminadmin");
       ElementProperty prop3 = new ElementProperty();
       prop3.setName("status");
       prop3.setValue("enabled");
       testProps = new ElementProperty[] {prop1, prop2, prop3};
       testNames = new String[] {"user","password","status"};
       testValues = new String[] {"admin","adminadmin","enabled"};
    }

    protected void tearDown() {
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite(GlobalResourceDeployerTest.class);
        return suite;
    }
    
     public static void main(String args[]) throws Exception {
        final TestRunner runner= new TestRunner();
        final TestResult result =
                runner.doRun(GlobalResourceDeployerTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}
