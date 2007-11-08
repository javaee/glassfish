/*
 * TestOpenJarFileWorkaround.java
 *
 * Created on February 8, 2004, 3:36 PM
 */

import devtests.deployment.util.JSR88Deployer;

import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.ModuleType;

import java.io.File;

/**
 *
 * @author  tjquinn
 */
public class TestOpenJarFileWorkaround {
    
    private static final String here = "devtests/deployment/jsr88/misc";
    
    /**
     Values taken from command-line arguments
     */
    private String host = null;
    private String port = null;
    private String user = null;
    private String password = null;
    private String warFileToDeploy = null;
    
    private JSR88Deployer depl = null;
    
    /** Creates a new instance of TestOpenJarFileWorkaround */
    public TestOpenJarFileWorkaround() {
    }
    
    private void initDeployer(String host, String port, String user, String password) throws Exception {
        log("Getting access to JSR88 deployer");
        depl = new JSR88Deployer(host, port, user, password);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestOpenJarFileWorkaround test = new TestOpenJarFileWorkaround();
        try {
            test.run(args);
            test.pass();
        } catch (Throwable th) {
            th.printStackTrace(System.out);
            test.fail();
        }
    }
    
    private void log(String message) {
        System.out.println("[TestProgressObjectImpl]:: " + message);
    }

    private void pass() {
        log("PASSED: " + here);
        System.exit(0);
    }

    private void fail() {
        log("FAILED: " + here);
        System.exit(-1);
    }
    
    private void prepareArgs(String[] args) {
        if (args.length < 5) {
            log("Expected 5 arguments (host, port, user, password, war) but found " + args.length);
            fail();
        }
        
        this.host = args[0];
        this.port = args[1];
        this.user = args[2];
        this.password = args[3];
        this.warFileToDeploy = args[4];
    }
    
    public void run(String[] args) throws Throwable {
        
        /*
         *Prepare instance variables from the command-line arguments for convenience.
         */
        prepareArgs(args);
        
        /*
         *Locate the web archive to deploy.
         */
        File warFile = new File(this.warFileToDeploy);
        if ( ! warFile.exists()) {
            log("Could not find war file " + warFile.getAbsolutePath());
            fail();
        }

        /*
         *Initialize the deployer for use during the test.
         */
        initDeployer(host, port, user, password);
        
        /*
         *Start by deploying the web app.
         */
        int firstDeployResult = depl.deploy(warFile, /*deploymentPlan */ null, /* startByDefault */ true);
        
        if (firstDeployResult != 0) {
            log("Failed to deploy war file " + warFile.getAbsolutePath() + " the first time.");
            fail();
        }
        
        /*
         *Next, undeploy the same web app.
         *Get the list of modules acted upon so we can specify the correct module ID to undeploy. 
         */
        TargetModuleID [] firstListOfApps = depl.getMostRecentTargetModuleIDs();
        
        if (firstListOfApps.length != 1) {
            log("Expected exactly one result module from the deployer but found " + firstListOfApps.length);
            for (int i = 0; i < firstListOfApps.length; i++) {
                log("    " + firstListOfApps[i].getModuleID());
            }
            fail();
        }
            
        int firstUndeployResult = depl.undeploy(firstListOfApps[0].getModuleID());
        
        if (firstUndeployResult != 0) {
            log("Error undeploying the web application the first time.");
            fail();
        }
        
        /*
         *Now, try to deploy the app again.
         */
        int secondDeployResult = depl.deploy(warFile, /*deploymentPlan */ null, /* startByDefault */ true);
        
        if (secondDeployResult != 0) {
            log("Failed to deploy war file " + warFile.getAbsolutePath() + " the second time.");
            fail();
        }
        
        /*
         *Undeploy the web app one last time.
         */
        TargetModuleID [] secondListOfApps = depl.getMostRecentTargetModuleIDs();
        
        if (secondListOfApps.length != 1) {
            log("Expected exactly one application from the deployer but found " + secondListOfApps.length);
            for (int i = 0; i < secondListOfApps.length; i++) {
                log("    " + secondListOfApps[i].getModuleID());
            }
            fail();
        }
            
        int secondUndeployResult = depl.undeploy(secondListOfApps[0].getModuleID());
        
        if (secondUndeployResult != 0) {
            log("Error undeploying the web application the second time.");
            fail();
        }
    }
}
