/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
