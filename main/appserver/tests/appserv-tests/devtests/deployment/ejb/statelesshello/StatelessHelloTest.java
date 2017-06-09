/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2017 Oracle and/or its affiliates. All rights reserved.
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

package devtests.deployment.ejb.statelesshello.testng;

import devtests.deployment.DeploymentTest;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.Property;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

/**
 * Illustrates an example test relying some of the inherited logic
 * in DeploymentTest.
 *
 * The general flow of this test is:
 *
 *   assemble the jars
 *   deploy the app and run the client
 *   redeploy the app and run the client
 *   undeploy the app
 *   deploy the app using JSR-88 and run the client
 *   redeploy the app using JSR-88 and run the client
 *   undeploy the app using JSR-88
 *
 * @author: tjquinn
 * 
 */

public class StatelessHelloTest extends DeploymentTest {
    
    private int nextLogID = 0;
    
    /** Creates a new instance of ExampleTest */
    public StatelessHelloTest() {
    }
    
    /**
     *Deploy the app using asadmin and run it, expecting a positive result.
     */
    @Test
    public void deployWithAsadminAndRun() {
        deploy();
        runPositive("ejb/statelessejb Test asadmin deploy");
    }
    
    /**
     *Redeploy and run after first test. 
     */
    @Test(dependsOnMethods={"deployWithAsadminAndRun"})
    public void redeployWithAsadminAndRun() {
        redeploy();
        runPositive("ejb/statelessejb Test asadmin redeploy");
    }
    
    /**
     *Undeploy using asadmin after first deployment and redeployment.
     */
    @Test(alwaysRun=true,dependsOnMethods={"redeployWithAsadminAndRun"})
    public void undeployAfterAsadminRuns() {
        undeploy();
    }
    
    /**
     *Deploy with JSR-88 and run.
     */
    @Test(dependsOnMethods={"undeployAfterAsadminRuns"})
    public void deployWithJSR88AndRun() {
        deployWithJSR88();
        runPositive("ejb/statelessejb Test jsr88 deploy");
    }
    
    /**
     *Stop using JSR-88 and attempt to run.
     */
    @Test(dependsOnMethods={"deployWithJSR88AndRun"})
    public void stopAndRetry() {
        stopWithJSR88();
        runNegative("ejb/statelessejb Test jsr88 stopped state");
    }
    
    /**
     *Start with JSR-88 and attempt to run.
     */
    @Test(dependsOnMethods={"stopAndRetry"})
    public void startAndRetry() {
        startWithJSR88();
        runPositive("ejb/statelessejb Test jsr88 started state");
    }
    
    /**
     *Stop, redeploy, and attempt to run (should fail).
     */
    @Test(dependsOnMethods={"startAndRetry"})
    public void stopRedeployAndRetry() {
        stopWithJSR88();
        redeployWithJSR88();
        runNegative("ejb/statelessejb Test jsr88 redeploy stop");
    }

    @Configuration(afterTestClass=true)
    public void unsetup() {
        undeployAtEnd();
    }
    
    public void undeployAtEnd() {
        undeployWithJSR88();
    }
    
    protected void deployWithJSR88() {
        project.executeTarget("deploy.jsr88");
    }
    
    protected void startWithJSR88() {
        project.executeTarget("start.jsr88");
    }
    
    protected void stopWithJSR88() {
        project.executeTarget("stop.jsr88");
    }
    
    protected void redeployWithJSR88() {
        project.executeTarget("redeploy.jsr88");
    }
    
    protected void undeployWithJSR88() {
        project.executeTarget("undeploy.jsr88");
    }
    
    protected void runPositive(String testTitle) {
        run(testTitle, "run.positive");
    }
    
    protected void runNegative(String testTitle) {
        run(testTitle, "run.negative");
    }
    
    protected void run(String testTitle, String runTarget) {
        CallTarget target = new CallTarget();
        target.setProject(project);
        target.setTarget(runTarget);

        Property logID = target.createParam();
        Property description = target.createParam();
        logID.setName("log.id");
        logID.setValue(String.valueOf(nextLogID++));
        description.setName("description");
        description.setValue(testTitle);
        
        target.execute();
    }
}
