/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.deployment.client;

import java.io.File;
import javax.enterprise.deploy.spi.Target;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tjquinn
 */
public class TestDeploy {

    public TestDeploy() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /*
     * Note that the two tests below are here as examples of how to use the
     * DeploymentFacility.  In their current form they should not be used
     * as tests, because they would require the server to be up.
     */
    
    @Ignore
    @Test
    public void testDeploy() {
        DeploymentFacility df = DeploymentFacilityFactory.getDeploymentFacility();
        ServerConnectionIdentifier sci = new ServerConnectionIdentifier();
        sci.setHostName("localhost");
        sci.setHostPort(8080); // 8080 for the REST client
        sci.setUserName("admin");
        sci.setPassword("adminadmin");
        
        df.connect(sci);
        
        File archive = new File("C:\\tim\\asgroup\\dev-9p-fcs\\glassfish\\appserv-tests\\devtests\\deployment\\build\\servletonly.war");
        DFDeploymentProperties options = new DFDeploymentProperties();
        options.setForce(true);
        options.setUpload(false);
        DFProgressObject prog = df.deploy(
                new Target[0] /* ==> deploy to the default target */, 
                archive.toURI(), 
                null, 
                options);
        DFDeploymentStatus ds = prog.waitFor();
        
        if (ds.getStatus() == DFDeploymentStatus.Status.FAILURE) {
            fail(ds.getAllStageMessages());
        }
        
    }
    
    @Ignore
    @Test
    public void testUndeploy() {
        DeploymentFacility df = DeploymentFacilityFactory.getDeploymentFacility();
        ServerConnectionIdentifier sci = new ServerConnectionIdentifier();
        sci.setHostName("localhost");
        sci.setHostPort(8080); // 8080 for the REST client
        sci.setUserName("admin");
        sci.setPassword("adminadmin");
        
        df.connect(sci);
        
        DFProgressObject prog = df.undeploy(
                new Target[0] /* ==> deploy to the default target */, 
                "servletonly");
        DFDeploymentStatus ds = prog.waitFor();
        
        if (ds.getStatus() == DFDeploymentStatus.Status.FAILURE) {
            fail(ds.getAllStageMessages());
        }
        
    }
}