/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.deployment.client;

import java.io.File;
import java.util.Properties;
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

    private static final String APP_NAME = "servletonly";

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
        sci.setHostPort(4848); // 8080 for the REST client
        sci.setUserName("admin");
        sci.setPassword("adminadmin");
        
        df.connect(sci);
        
//        File archive = new File("C:\\tim\\asgroup\\dev-9p-fcs\\glassfish\\appserv-tests\\devtests\\deployment\\build\\servletonly.war");
        File archive = new File("/Users/Tim/asgroup/v3/warWithPlan/servletonly-portable.war");
        File plan = new File("/Users/Tim/asgroup/v3/warWithPlan/servletonly-deployplan.jar");
        DFDeploymentProperties options = new DFDeploymentProperties();
        options.setForce(true);
        options.setUpload(true);
        options.setName(APP_NAME);
        Properties props = new Properties();
        props.setProperty("keepSessions", "true");
        props.setProperty("foo", "bar");
        options.setProperties(props);
        DFProgressObject prog = df.deploy(
                new Target[0] /* ==> deploy to the default target */, 
                archive.toURI(), 
                plan.toURI(),
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
        sci.setHostPort(4848); 
        sci.setUserName("admin");
        sci.setPassword("adminadmin");
        
        df.connect(sci);
        
        DFProgressObject prog = df.undeploy(
                new Target[0] /* ==> deploy to the default target */, 
                APP_NAME);
        DFDeploymentStatus ds = prog.waitFor();
        
        if (ds.getStatus() == DFDeploymentStatus.Status.FAILURE) {
            fail(ds.getAllStageMessages());
        }
        
    }
}
