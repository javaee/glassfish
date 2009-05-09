/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.deployment.client;

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim
 */
public class ListAppRefsTest {

    public ListAppRefsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Ignore
    @Test
    public void testListAppRefsTest() {
        System.out.println("testListAppRefsTest");
        DeploymentFacility df = DeploymentFacilityFactory.getDeploymentFacility();
        ServerConnectionIdentifier sci = new ServerConnectionIdentifier();
        sci.setHostName("localhost");
        sci.setHostPort(4848); // 8080 for the REST client
        sci.setUserName("admin");
        sci.setPassword("adminadmin");

        df.connect(sci);

        try {
            TargetModuleID[] results =
                    df.listAppRefs(new String[] {"server"});
            System.out.println("TargetModuleIDs returned for default:");
            for (TargetModuleID tmid : results) {
                System.out.println(tmid.getTarget().getName() + ":" +
                        tmid.getModuleID());
            }

            TargetModuleID[] resultsAll =
                    df.listAppRefs(new String[] {"server"}, "all");
            System.out.println("TargetModuleIDs returned for all:");
            for (TargetModuleID tmidAll : resultsAll) {
                System.out.println(tmidAll.getTarget().getName() + ":" +
                        tmidAll.getModuleID());
            }

            TargetModuleID[] resultsRunning =
                    df.listAppRefs(new String[] {"server"}, "running");
            System.out.println("TargetModuleIDs returned for running:");
            for (TargetModuleID tmidRunning : resultsRunning) {
                System.out.println(tmidRunning.getTarget().getName() + ":" +
                        tmidRunning.getModuleID());
            }

            TargetModuleID[] resultsNonRunning =
                    df.listAppRefs(new String[] {"server"}, "non-running");
            System.out.println("TargetModuleIDs returned for nonrunning:");
            for (TargetModuleID tmidNonRunning : resultsNonRunning) {
                System.out.println(tmidNonRunning.getTarget().getName() + ":" +
                        tmidNonRunning.getModuleID());
            }

            TargetModuleID[] resultsAllWithType =
                    df.listAppRefs(new String[] {"server"}, "all", "web");
            System.out.println("TargetModuleIDs returned for all web:");
            for (TargetModuleID tmidAllWithType : resultsAllWithType) {
                System.out.println(tmidAllWithType.getTarget().getName() + ":" +
                        tmidAllWithType.getModuleID());
            }

            TargetModuleID[] resultsRunningWithType =
                    df.listAppRefs(new String[] {"server"}, "running", "ear");
            System.out.println("TargetModuleIDs returned for running ear:");
            for (TargetModuleID tmidRunningWithType : resultsRunningWithType) {
                System.out.println(tmidRunningWithType.getTarget().getName() + ":" +
                        tmidRunningWithType.getModuleID());
            }

            TargetModuleID[] resultsNonRunningWithType =
                    df.listAppRefs(new String[] {"server"}, "non-running", "ear");
            System.out.println("TargetModuleIDs returned for nonrunning ear:");
            for (TargetModuleID tmidNonRunningWithType : resultsNonRunningWithType) {
                System.out.println(tmidNonRunningWithType.getTarget().getName() + ":" +
                        tmidNonRunningWithType.getModuleID());
            }



        } catch (Exception e) {
            fail("Failed due to exception " + e.getMessage());
        }

    }

}
