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

import junit.framework.*;
import junit.textui.TestRunner;
import com.sun.enterprise.ManagementObjectManager;
import com.sun.enterprise.Switch;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.management.util.J2EEManagementObjectManager;
import com.sun.enterprise.repository.IASJ2EEResourceFactoryImpl;
import com.sun.enterprise.repository.J2EEResource;
import java.util.logging.Logger;

/**
 * Unit test for ConnectorConnectionPoolDeployer.
 *
 * @author Rob Ruyak
 */
public class ConnectorConnectionPoolDeployerTest extends TestCase {

    ConnectorConnectionPoolDeployer deployer;
    ConnectorConnectionPool resource;

    /** Creates a new instance of MailResourceDeployerTest */
    public ConnectorConnectionPoolDeployerTest(String name) {
         super(name);
    }
    
    //TODO: Should ther be a unit test for null resources passed to the 
    //deploy method???
    
    /**
     * Tests the deployResource method.
     *
    public void testDeployResourceWithNull() {
        try {
            deployer.deployResource(null);
        } catch (Throwable e) {
            e.printStackTrace();
            fail("Exception caught -> " + e.getMessage());
        }
    }
     **/

    /**
     * Tests the deployResource method.
     */
    public void testDeployResource() {
        try {
            deployer.deployResource(resource);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception caught -> " + e.getMessage());
        }
    }
    
    /**
     * Tests the deployResource method.
     */
    public void testDeployResourceWithNull() {
        try {
            //This should print the following log:
            //INFO: CORE5005: Error in resource deploy.
            deployer.deployResource(null);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception caught -> " + e.getMessage());
        }
    }
    
    /**
     * Tests the deployResource method.
     */
    public void testDeployResourceNotEnabled() {
        try {
            //This should print the following log:
            //INFO: CORE5039: Resource named [jndi-name]
            //[Type: mail-resource] is disabled. It was not loaded.
            resource.setEnabled(false);
            deployer.deployResource(resource);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception caught -> " + e.getMessage());
        }
    }

    protected void setUp() {
        //initialize the dummy deployer object
        deployer = new ConnectorConnectionPoolDeployer () {
            Switch getAppServerSwitchObject() {
                return new Switch() {
                    public ManagementObjectManager getManagementObjectManager() {
                        return new J2EEManagementObjectManager() {
                            public void registerJMSResource(String name, 
                                    String resType, String username, 
                                    String password, String[] propNames, 
                                    String[] propValues) {
                                System.out.println("Jsr77 Registration Complete...");
                            }
                        };
                    }
                };
            }
        };
        
        //initialize the dummy resource object
        resource = new ConnectorConnectionPool();
        resource.setName("jms/Tester");
        resource.setEnabled(true);
    }

    protected void tearDown() {
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite(ConnectorConnectionPoolDeployerTest.class);
        return suite;
    }
    
     public static void main(String args[]) throws Exception {
        final TestRunner runner= new TestRunner();
        final TestResult result =
                runner.doRun(ConnectorConnectionPoolDeployerTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}
