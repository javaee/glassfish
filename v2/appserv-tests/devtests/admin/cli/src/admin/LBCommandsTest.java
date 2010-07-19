/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
package admin;

import java.io.*;
import java.net.*;

/*
 * Dev test for load balancer administration commands.
 * @author Yamini K B
 */
public class LBCommandsTest extends AdminBaseDevTest {

    public LBCommandsTest() {
        String host0 = null;

        try {
            host0 = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception e) {
            host0 = "localhost";
        }
        host = host0;
        System.out.println("Host= " + host);
        glassFishHome = getGlassFishHome();
        System.out.println("GF HOME = " + glassFishHome);
    }

    public static void main(String[] args) {
        new LBCommandsTest().run();
    }

    @Override
    public String getTestName() {
        return "LB commands";
    }

    @Override
    protected String getTestDescription() {
        return "Developer tests for load balancer administration";
    }

    public void run() {
        startDomain();
        createInstances();

        //create,list LB config
        report("Test-create-http-lb-config", asadmin("create-http-lb-config", LB_CONFIG));
        AsadminReturn ret = asadminWithOutput("list-http-lb-configs");
        boolean success = ret.out.indexOf(LB_CONFIG) >= 0;
        report("Test-list-http-lb-configs", success);

        //create/delete cluster-ref for LB
        report("Test-create-http-lb-cluster-ref", asadmin("create-http-lb-ref", CONFIG_OPTION, LB_CONFIG, CLUSTER));
        report("Test-delete-http-lb-cluster-ref", asadmin("delete-http-lb-ref", CONFIG_OPTION, LB_CONFIG, CLUSTER));

        //create server-ref for LB
        report("Test-create-http-lb-server-ref-1", asadmin("create-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE2));
        report("Test-create-http-lb-server-ref-2", asadmin("create-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE1));

                //enable/disable servers for LB
        report("Test-enable-http-lb-server", !asadmin("enable-http-lb-server", STANDALONE_INSTANCE2));
        report("Test-disable-http-lb-server", asadmin("disable-http-lb-server", STANDALONE_INSTANCE2));
        report("Test-enable-http-lb-server-1", asadmin("enable-http-lb-server", STANDALONE_INSTANCE2));

        //delete/create health checker
        report("Test-delete-http-health-checker", asadmin("delete-http-health-checker", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE2));
        report("Test-create-http-health-checker", asadmin("create-http-health-checker", CONFIG_OPTION, LB_CONFIG, "--timeout", "30", "--interval", "5", STANDALONE_INSTANCE2));

        //delete server-ref for LB
        report("Test-delete-http-lb-server-ref-1", asadmin("delete-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE2));
        report("Test-delete-http-lb-server-ref-2", asadmin("delete-http-lb-ref", CONFIG_OPTION, LB_CONFIG, STANDALONE_INSTANCE1));

        //configure weights
        report("Test-configure-lb-weight", asadmin("configure-lb-weight", "--cluster", CLUSTER, "cl1-ins1=2:cl1-ins2=3:cl1-ins3=5"));

        //configure weight for non-existing instance
        report("Test-configure-lb-weight-1", !asadmin("configure-lb-weight", "--cluster", CLUSTER, "foo=10"));

        //configure weight for standalone instance
        report("Test-configure-lb-weight-2", !asadmin("configure-lb-weight", "--cluster", CLUSTER, "ins1=10"));

        // deploy an application to the cluster
        File webapp = new File("resources", "helloworld.war");
        asadmin("deploy", "--target", CLUSTER, webapp.getAbsolutePath());

        //disable/enable application for LB
        report("Test-enable-http-lb-application", !asadmin("enable-http-lb-application", "--name" , "helloworld", CLUSTER));
        report("Test-disable-http-lb-application", asadmin("disable-http-lb-application", "--name" , "helloworld", CLUSTER));
        report("Test-enable-http-lb-application-1", asadmin("enable-http-lb-application", "--name" , "helloworld", CLUSTER));

        //undeploy the app
        asadmin("undeploy", "--target", CLUSTER, "helloworld");

        //delete the Lb config
        report("Test-delete-http-lb-config", asadmin("delete-http-lb-config", LB_CONFIG));

        //create the load balancer
        report("Test-create-http-lb", asadmin("create-http-lb", "--devicehost", "localhost", "--deviceport", "9000", "lb1"));
        ret = asadminWithOutput("list-http-lbs");
        success = ret.out.indexOf("lb1") >= 0;
        report("Test-list-http-lbs", success);

        //delete the load balancer
        report("Tests-delete-http-lb", asadmin("delete-http-lb", "lb1"));
        
        deleteInstances();
        stopDomain();
	stat.printSummary();
    }


    private void createInstances() {
        asadmin("create-cluster", CLUSTER);

        asadmin("create-instance", "--cluster", CLUSTER, "--node",
                "localhost", INSTANCE1);
        asadmin("create-instance", "--cluster", CLUSTER, "--node",
                "localhost", INSTANCE2);
        asadmin("create-instance", "--cluster", CLUSTER, "--node",
                "localhost", INSTANCE3);

        asadmin("create-instance", "--node", "localhost", STANDALONE_INSTANCE1);
        asadmin("create-instance", "--node", "localhost", STANDALONE_INSTANCE2);
    }

    private void deleteInstances() {
        asadmin("delete-instance", STANDALONE_INSTANCE2);
        asadmin("delete-instance", STANDALONE_INSTANCE1);

        asadmin("delete-instance", INSTANCE3);
        asadmin("delete-instance", INSTANCE2);
        asadmin("delete-instance", INSTANCE1);

        asadmin("delete-cluster", CLUSTER);
    }
    private final String host;
    private final File glassFishHome;
    private static final String CLUSTER = "cl1";
    private static final String INSTANCE1 = "cl1-ins1";
    private static final String INSTANCE2 = "cl1-ins2";
    private static final String INSTANCE3 = "cl1-ins3";
    private static final String STANDALONE_INSTANCE1 = "ins1";
    private static final String STANDALONE_INSTANCE2 = "ins2";
    private static final String LB_CONFIG = "lb-config1";
    private static final String CONFIG_OPTION="--config";
}
