/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package admin;

import java.net.InetAddress;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class adds devtests for creating an SSH node for a remote
 * system, and then using that node for basic instance and cluster
 * lifecycle management.
 * 
 * In order to run this test you must:
 * 
 * Install glassfish on a remote system.
 * 
 * Pass "-Dssh.host=<hostname> -Dssh.installdir=<installdir>" to the test
 * 
 * Where <hostname> is the remote host you have glassfish installed on and
 * installdir is the full path the that glassfish installation.
 * 
 * You must be able to access the remote system via SSH as the user you are
 * running this test as, and do so via key authentication. In other words
 * running "ssh hostname" must work without it prompting you for anything.
 *
 * @author Joe Di Pol
 */
public class SSHNodeTest extends AdminBaseDevTest {

    private static final String NL = System.getProperty("line.separator");
    private static final String LOCALHOST = "localhost";
    private static final String LOCALHOST_NODE = "localhost-domain1";
    private static final String SSH_HOST_PROP = "ssh.host";
    private static final String SSH_INSTALLDIR_PROP = "ssh.installdir";

    private String thisHost = null;
    private String remoteHost = null;
    private String remoteInstallDir = null;
    private String thisUser = null;
    private String glassfishHome = null;

    SSHNodeTest () {

        try {
            thisHost = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception e) {
            thisHost = "localhost";
        }

        try {
            glassfishHome = getGlassFishHome().getCanonicalPath();
        } catch (IOException e) {
            glassfishHome = getGlassFishHome().getAbsolutePath();
        }

        thisUser = System.getProperty("user.name");
    }


    @Override
    protected String getTestDescription() {
        return "Tests Node configuration using create-node-*/delete-node-*";
    }

    public static void main(String[] args) {
        new SSHNodeTest().runTests();
    }

    private void runTests() {

        boolean runTest = true;
        remoteHost       = System.getProperty(SSH_HOST_PROP);
        remoteInstallDir = System.getProperty(SSH_INSTALLDIR_PROP);

        if (remoteHost == null || remoteHost.length() == 0) {
            System.out.printf("%s requires you set the %s property\n",
                this.getClass().getName(), SSH_HOST_PROP);
            runTest = false;
        } else {
            System.out.printf("%s=%s\n", SSH_HOST_PROP, remoteHost);
        }

        if (remoteInstallDir == null || remoteInstallDir.length() == 0) {
            System.out.printf("%s requires you set the %s property\n",
                this.getClass().getName() , SSH_INSTALLDIR_PROP);
            runTest = false;
        } else {
            System.out.printf("%s=%s\n", SSH_INSTALLDIR_PROP, remoteInstallDir);
        }

        if (! runTest) {
            System.out.printf("Failing %s because required system properties are missing\n",
                    this.getClass().getName());
            report("ssh-node-*", false);
            return;
        }

        startDomain();
        testBasicSSHCluster();
        stopDomain();
        stat.printSummary();
    }

    private void testBasicSSHCluster() {
        final String CNAME = "ssh_c1";
        final String NNAME = "ssh_node1";
        final String INAME1 = "ssh_remote_i1";
        final String INAME2 = "ssh_local_i2";

        // Do a basic cluster test. Create a cluster with two
        // local instances: one local, one remote. Start the cluster, stop it,
        // then remove everything.
        report("ssh-node-create-cluster", asadmin("create-cluster", CNAME));

        report("ssh-node-create-node-ssh", asadmin("create-node-ssh",
                "--nodehost", remoteHost,
                "--installdir", remoteInstallDir,
                NNAME));

        report("ssh-node-create-instance1", asadmin("create-instance",
                        "--node", NNAME,
                        "--cluster", CNAME,
                        INAME1));

        report("ssh-node-create-instance2", asadmin("create-instance",
                        "--node", LOCALHOST_NODE,
                        "--cluster", CNAME,
                        INAME2));

        System.out.printf("Starting cluster %s\n", CNAME);
        report("ssh-node-start-cluster1", asadmin("start-cluster", CNAME));

        AsadminReturn ret = asadminWithOutput("list-instances", "--long");
        System.out.printf("After start-cluster list-instances returned:\n%s\n",
                ret.out);

        report("ssh-node-check-instance1", isInstanceRunning(INAME1));
        report("ssh-node-check-instance2", isInstanceRunning(INAME2));
        report("ssh-node-check-cluster1", isClusterRunning(CNAME));

        report("ssh-node-stop-cluster1", asadmin("stop-cluster", CNAME));

        try {
            // Give instances time to come down
            Thread.sleep(4 * 1000);
        } catch (InterruptedException e) {
        }

        ret = asadminWithOutput("list-instances", "--long");
        System.out.printf("After stop-cluster list-instances returned:\n%s\n",
                ret.out);

        report("ssh-node-check-stopped-instance1", ! isInstanceRunning(INAME1));
        report("ssh-node-check-stopped-instance2", ! isInstanceRunning(INAME2));
        report("ssh-node-check-stopped-cluster1", ! isClusterRunning(CNAME));

        report("ssh-node-delete-instance1", asadmin("delete-instance", INAME1));
        report("ssh-node-delete-instance2", asadmin("delete-instance", INAME2));
        report("ssh-node-delete-cluster1", asadmin("delete-cluster", CNAME));
        report("ssh-node-delete-node-ssh", asadmin("delete-node-ssh",
                NNAME));
    }
}
