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
import com.sun.enterprise.config.serverbeans.AdminObjectResource;
import com.sun.enterprise.management.util.J2EEManagementObjectManager;
import com.sun.enterprise.config.serverbeans.MailResource;
import com.sun.enterprise.repository.IASJ2EEResourceFactoryImpl;
import com.sun.enterprise.repository.J2EEResource;
import java.util.logging.Logger;

/**
 * Unit test for ConnectorConnectionPoolDeployer.
 *
 * @author Rob Ruyak
 */
public class AdminObjectResourceDeployerTest extends TestCase {

    AdminObjectResourceDeployer deployer;
    AdminObjectResource resource;

    /** Creates a new instance of ConnectorConnectionPoolDeployerTest */
    public AdminObjectResourceDeployerTest(String name) {
         super(name);
    }

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
        deployer = new AdminObjectResourceDeployer() {
            Switch getAppServerSwitchObject() {
                return new Switch() {
                    public ManagementObjectManager getManagementObjectManager() {
                        return new J2EEManagementObjectManager() {
                            public void registerAdminObjectResource(String name, 
                                    String raName, String type, String [] names, 
                                    String [] values) {
                                System.out.println("Jsr77 Registration Complete...");
                            }
                        };
                    }
                };
            }
        };
        
        //initialize the dummy resource object
        resource = new AdminObjectResource();
        resource.setJndiName("javamail/Tester");
        resource.setEnabled(true);
    }

    protected void tearDown() {
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite(AdminObjectResourceDeployerTest.class);
        return suite;
    }
    
     public static void main(String args[]) throws Exception {
        final TestRunner runner= new TestRunner();
        final TestResult result =
                runner.doRun(AdminObjectResourceDeployerTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}
