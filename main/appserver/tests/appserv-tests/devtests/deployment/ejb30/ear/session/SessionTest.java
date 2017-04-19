/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package devtests.deployment.ejb30.ear.session.testng;

import devtests.deployment.DeploymentTest;
import org.testng.annotations.Test;

/**
 * Illustrates an example test relying some of the inherited logic
 * in DeploymentTest.
 *
 * @author: tjquinn
 * 
 */

public class SessionTest extends DeploymentTest {
    
    /** Creates a new instance of ExampleTest */
    public SessionTest() {
    }
    
    /**
     *Runs the first step of the test: deploying and running the client.
     */
    @Test
    public void deployAndRun() {
        deploy();
        runClient();
    }
    
    /**
     *Runs the second step of the test, only after the first has run and
     *succeeded: redeploy the app and run the client again.
     */
    @Test(dependsOnMethods={"deployAndRun"})
    public void redeployAndRun() {
        redeploy();
        runClient();
    }
}
