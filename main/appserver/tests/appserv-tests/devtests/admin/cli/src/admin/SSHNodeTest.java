/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
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

package admin;

import java.net.InetAddress;
import java.io.IOException;
import java.util.ArrayList;


/**
 * This class adds devtests for creating an SSH node for a remote
 * system, and then using that node for basic instance and cluster
 * lifecycle management.
 *
 * You can run this test two ways:
 *
 * A. Remote system already has glassfish installed and SSH key is already set up
 *
 * In this case you already have glassfish installed on the remote system,
 * and you are able to access the remote system via SSH as the user you are
 * running this test as, and do so via key authentication. In other words
 * running "ssh hostname" works without it prompting you for anything.
 *
 * In this case run the test like this:
 * #
 * ant -Dteststorun=sshnode -Dssh.host=adc2101159.us.oracle.com
 *    "-Dssh.installdir=/export/glassfish5" all
 *
 * If you want to use a different user for SSH login you can set ssh.user:
 *     -Dssh.user=hudson
 *
 * If you want to use an alternate node-dir you can set ssh.nodedir:
 *     -Dssh.nodedir=/var/tmp/nodes
 *
 * B. Remote system does not have glassfish installed and you want the test
 *    to set up key authentication for you.
 *
 * In this case you want the test to install glassfish on the remote system
 * and set up SSH key authentication. Run it like this:
 *
 * ant -Dteststorun=sshnode -Dssh.host=adc2101159.us.oracle.com
 *      -Dssh.doinstall=true all
 *
 * If you want to use a different ssh user do:
 *
 * ant -Dteststorun=sshnode -Dssh.host=adc2101159.us.oracle.com
 *      -Dssh.doinstall=true -Dssh.user=hudson -Dssh.password=hudson all
 *
 * If you want to control the directory that the test installs GF into use
 * the ssh.installPrefix property (which will be used as the parent of
 * installation directory):
 *
 * ant -Dteststorun=sshnode -Dssh.host=adc2101159.us.oracle.com
 *      -Dssh.doinstall=true -Dssh.user=hudson -Dssh.password=hudson
 *      -Dssh.installprefix=/export/home/hudson/devtest all
 *
 *
 * @author Joe Di Pol
 */
public class SSHNodeTest extends SshBaseDevTest {

    private static final String NL = System.getProperty("line.separator");
    private static final String LOCALHOST = "localhost";
    private static final String LOCALHOST_NODE = "localhost-domain1";

    private String thisHost = null;
    private String remoteHost = null;
    private String remoteInstallDir = null;
    private String remoteInstallPrefix = null;
    private String remoteNodeDir = null;
    private String thisUser = null;
    private String sshUser = null;
    private String sshPassword = null;
    private Boolean sshDoInstall = null;

    SSHNodeTest () {

        try {
            thisHost = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            thisHost = "localhost";
        }

        thisUser = System.getProperty("user.name");
        remoteHost       = TestUtils.getExpandedSystemProperty(SSH_HOST_PROP);
        remoteInstallDir = TestUtils.getExpandedSystemProperty(SSH_INSTALLDIR_PROP);
        remoteInstallPrefix = TestUtils.getExpandedSystemProperty(SSH_INSTALLPREFIX_PROP);
        remoteNodeDir = TestUtils.getExpandedSystemProperty(SSH_NODEDIR_PROP);
        sshUser = TestUtils.getExpandedSystemProperty(SSH_USER_PROP);
        sshPassword = TestUtils.getExpandedSystemProperty(SSH_PASSWORD_PROP);
        sshDoInstall = Boolean.valueOf(TestUtils.getExpandedSystemProperty(SSH_DOINSTALL_PROP));
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

        if (!ok(remoteHost)) {
            System.out.printf("%s requires you set the %s property\n",
                this.getClass().getName(), SSH_HOST_PROP);
            remoteHost = null;
            runTest = false;
        }

        if (!ok(remoteInstallDir)) {
            if (sshDoInstall) {
                remoteInstallDir = generateInstallPath(remoteInstallPrefix);
            } else {
                System.out.printf(
                    "%s: You must either set the %s property to point to\n" +
                    "an existing GF installation or set the %s property to true\n" +
                    "to have this test install GF for you",
                    this.getClass().getName() , SSH_INSTALLDIR_PROP,
                    SSH_DOINSTALL_PROP);
                runTest = false;
            }
        } else {
            if (ok(remoteInstallPrefix)) {
                System.out.printf(
                    "%s is set (%s). Ignoring %s\n",
                    SSH_INSTALLDIR_PROP, remoteInstallDir, SSH_INSTALLPREFIX_PROP);
            }
        }

        if (!ok(sshUser)) {
            sshUser = thisUser;
        }

        if (!ok(sshPassword)) {
            sshPassword = null;
        }

        if (! runTest) {
            System.out.printf("Failing %s because required system properties are missing\n",
                    this.getClass().getName());
            report("ssh-node-*", false);
            return;
        }

        System.out.printf("Configuration:\n");
        System.out.printf("%s=%s\n", SSH_HOST_PROP, remoteHost);
        System.out.printf("%s=%s\n", SSH_INSTALLDIR_PROP, remoteInstallDir);
        System.out.printf("%s=%s\n", SSH_INSTALLPREFIX_PROP, remoteInstallPrefix);
        System.out.printf("%s=%s\n", SSH_NODEDIR_PROP,
                (ok(remoteNodeDir) ? remoteNodeDir : "<default>"));
        System.out.printf("%s=%s\n", SSH_USER_PROP,
                (ok(sshUser) ? sshUser : "<default>" ));
        System.out.printf("%s=%s\n", SSH_PASSWORD_PROP,
                (ok(sshPassword) ? "<concealed>" : "<none>" ));
        System.out.printf("%s=%s\n", SSH_DOINSTALL_PROP, sshDoInstall);

        startDomain();
        if (sshDoInstall) {
            doInstall();
        }
        testBasicSSHCluster();
        if (sshDoInstall) {
            doUnInstall();
        }
        stopDomain();
        stat.printSummary();
    }

    private void testBasicSSHCluster() {
        final String CNAME = "ssh_c1";
        final String NNAME = "ssh_node1";
        final String NNAME2 = "ssh_node2_with_nodedir";
        final String INAME1 = "ssh_remote_i1";
        final String INAME2 = "ssh_local_i2";
        final String INAME3 = "ssh_remote_i3";

        System.out.printf("Creating cluster, nodes, and instances\n");

        // Do a basic cluster test. Create a cluster with three
        // instances: one local, two remote. Start the cluster, stop it,
        // then remove everything.
        report("ssh-node-create-cluster", asadmin("create-cluster", CNAME));

        ArrayList<String> args = new ArrayList<String>();
        args.add("create-node-ssh");
        args.add("--nodehost");
        args.add(remoteHost);
        args.add("--installdir");
        args.add(remoteInstallDir);
        if (ok(sshUser)) {
            args.add("--sshuser");
            args.add(sshUser);
        }
        args.add(NNAME);
        String[] argsArray = new String[args.size()];
        report("ssh-node-create-node-ssh", asadmin(args.toArray(argsArray)));

        report("ssh-node-ping-node", asadmin("ping-node-ssh",
                        "--validate",
                        NNAME));

        // Create a second node, but this time with a nodedir
        args.remove(NNAME);
        args.add("--nodedir");

        // If user provided a node dir use it, else use a default
        if (ok(remoteNodeDir)) {
            args.add(remoteNodeDir);
        } else {
            args.add("mynodes");
        }
        args.add(NNAME2);
        argsArray = new String[args.size()];
        report("ssh-node-create-node-ssh", asadmin(args.toArray(argsArray)));

        report("ssh-node-create-instance1", asadmin("create-instance",
                        "--node", NNAME,
                        "--cluster", CNAME,
                        INAME1));

        report("ssh-node-create-instance2", asadmin("create-instance",
                        "--node", LOCALHOST_NODE,
                        "--cluster", CNAME,
                        INAME2));

        report("ssh-node-create-instance3", asadmin("create-instance",
                        "--node", NNAME2,
                        "--cluster", CNAME,
                        INAME3));

        System.out.printf("Starting cluster %s\n", CNAME);
        report("ssh-node-start-cluster1", asadmin("start-cluster", CNAME));

        AsadminReturn ret = asadminWithOutput("list-instances", "--long");
        System.out.printf("After start-cluster list-instances returned:\n%s\n",
                ret.out);

        report("ssh-node-check-instance1", isInstanceRunning(INAME1));
        report("ssh-node-check-instance2", isInstanceRunning(INAME2));
        report("ssh-node-check-instance3", isInstanceRunning(INAME3));
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
        report("ssh-node-check-stopped-instance3", ! isInstanceRunning(INAME3));
        report("ssh-node-check-stopped-cluster1", ! isClusterRunning(CNAME));

        System.out.printf("Deleting instances, cluster, and nodes\n");
        report("ssh-node-delete-instance1", asadmin("delete-instance", INAME1));
        report("ssh-node-delete-instance2", asadmin("delete-instance", INAME2));
        report("ssh-node-delete-instance3", asadmin("delete-instance", INAME3));
        report("ssh-node-delete-cluster1", asadmin("delete-cluster", CNAME));
        report("ssh-node-delete-node-ssh", asadmin("delete-node-ssh",
                NNAME));
        report("ssh-node-delete-node-ssh2", asadmin("delete-node-ssh",
                NNAME2));
    }

    private void doInstall() {
        System.out.printf("Setting up key access for %s@%s\n", sshUser, remoteHost);

        if (ok(sshPassword)) {
            addPassword(sshPassword, PasswordType.SSH_PASS);
        }
        report("ssh-node-setupssh", asadmin("setup-ssh",
                        "--sshuser", sshUser,
                        remoteHost));
        System.out.printf("Installing GlassFish onto %s:%s\n",
                remoteHost, remoteInstallDir);
        report("ssh-node-installnode", asadmin("install-node",
                        "--installdir", remoteInstallDir,
                        "--sshuser", sshUser,
                        remoteHost));
        return;
    }

    private void doUnInstall() {
        System.out.printf("Uninstalling GlassFish from %s:%s\n",
                remoteHost, remoteInstallDir);
        report("ssh-node-uninstallnode", asadmin("uninstall-node",
                        "--installdir", remoteInstallDir,
                        "--sshuser", sshUser,
                        "--force",
                        remoteHost));
        System.out.printf("You may need to manually remove traces of GlassFish from %s:%s\n",
                remoteHost, remoteInstallDir);
        return;
    }

    /**
     * Generate a unique path to use as a remote install location
     * @param prefix
     * @return
     */
    public String generateInstallPath(String prefix) {
        if (!ok(prefix)) {
            prefix = "/var/tmp/devTests-" + thisHost;
        }
        String randomSuffix = Long.toHexString(Double.doubleToLongBits(Math.random()));
        return prefix + "/" + "glassfish5-" + randomSuffix;
    }
}
