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
import com.sun.enterprise.management.util.J2EEManagementObjectManager;
import com.sun.enterprise.config.serverbeans.MailResource;
import com.sun.enterprise.repository.IASJ2EEResourceFactoryImpl;
import com.sun.enterprise.repository.J2EEResource;
import java.util.logging.Logger;

/**
 * Unit test for MailResourceDeployer.
 *
 * @author Rob Ruyak
 */
public class MailResourceDeployerTest extends TestCase {

    MailResourceDeployer deployer;
    MailResource resource;

    /** Creates a new instance of MailResourceDeployerTest */
    public MailResourceDeployerTest(String name) {
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
    
    /**
     * Tests the method for installing a mail resource.
     */
    public void testInstallResource() {
        try {
            deployer.installResource(resource);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception caught -> " + e.getMessage());
        }
        
    }

    protected void setUp() {
        //initialize the dummy deployer object
        deployer = new MailResourceDeployer () {
            Switch getAppServerSwitchObject() {
                return new Switch() {
                    public ManagementObjectManager getManagementObjectManager() {
                        return new J2EEManagementObjectManager() {
                            public void registerJavaMailResource(String name) {
                                System.out.println("Jsr77 Registration Complete...");
                            }
                        };
                    }
                    public ResourceInstaller getResourceInstaller() {
                        return new ResourceInstaller() {
                            public void installMailResource(
                                com.sun.enterprise.repository.MailResource 
                                        mailRes) {
                                System.out.println("Installed into Runtime...");
                            }
                        };
                    }
                };
            }
        };
        
        //initialize the dummy resource object
        resource = new MailResource();
        resource.setJndiName("javamail/Tester");
        resource.setEnabled(true);
    }

    protected void tearDown() {
    }

    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite(MailResourceDeployerTest.class);
        return suite;
    }
    
     public static void main(String args[]) throws Exception {
        final TestRunner runner= new TestRunner();
        final TestResult result =
                runner.doRun(MailResourceDeployerTest.suite(), false);
        System.exit(result.errorCount() + result.failureCount());
    }
}
