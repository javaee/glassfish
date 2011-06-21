/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.net.*;

/*
 * Dev tests for remote install/uninstall-node
 * @author Yamini K B
 */
public class InstallNodeRemoteTest extends AdminBaseDevTest {
    private static final String SSH_HOST_PROP = "ssh.host";
    private static final String SSH_USER_PROP = "ssh.user";
    private static final String SSH_PASSWORD = "ssh.password";

    private String remoteHost = null;
    private String sshPass = null;
    private String sshUser = null;

    public InstallNodeRemoteTest() {
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
        new InstallNodeRemoteTest().run();
    }

    @Override
    public String getTestName() {
        return "remote install/uninstall-node test";
    }

    @Override
    protected String getTestDescription() {
        return "Developer tests for GlassFish provisioning";
    }

    public void run() {

        remoteHost = System.getProperty(SSH_HOST_PROP);
        sshPass = System.getProperty(SSH_PASSWORD);
        sshUser = System.getProperty(SSH_USER_PROP);

        if (remoteHost == null || remoteHost.length() == 0) {
            System.out.printf("%s requires you set the %s property\n",
                this.getClass().getName(), SSH_HOST_PROP);
            report("install-test-*", false);
            return;
        } else {
            System.out.printf("%s=%s\n", SSH_HOST_PROP, remoteHost);
        }        

        if (sshPass == null || sshPass.length() == 0) {
            System.out.printf("%s requires you set the %s property\n",
                this.getClass().getName(), SSH_PASSWORD);
            report("install-ssh-*", false);
            return;
        } else {
            System.out.printf("%s=%s\n", SSH_PASSWORD, "<concealed>");
        }

        if (sshUser == null || sshUser.length() == 0) {
            sshUser = System.getProperty("user.name");
        } else {
            System.out.printf("%s=%s\n", SSH_USER_PROP, sshUser);
        }

        //will use password auth for the tests
        addPasswords();

        updateCommonOptions();

        asadmin("start-domain");

        testInstallRemoteNode();
        testUnInstallRemoteNode();        
 
        asadmin("stop-domain");
        stat.printSummary();
    }
    
    private void testInstallRemoteNode() {
        //create a ssh node with --install
        report("create-node-ssh-with-install-remote", asadmin("create-node-ssh", "--nodehost", remoteHost, "--sshuser", sshUser, INSTALL, INSTALL_DIR, "gf-test-1", "n2"));

        //cannot install if node already has an installation
        report("install-again-remote", !asadmin("install-node", "--sshuser", sshUser, INSTALL_DIR, "gf-test-1", remoteHost));

        //installing at different location on same host should work
        report("install-at-different-location-remote", asadmin("install-node", "--sshuser", sshUser, INSTALL_DIR, "gf-test-2", remoteHost));
    }
    
    private void testUnInstallRemoteNode() {
        //should fail since there is an installation
        report("uninstall-node-remote", !asadmin("uninstall-node", "--sshuser", sshUser, INSTALL_DIR, "gf-test-1", remoteHost));

        //simple uninstall
        report("uninstall-node-1-remote", asadmin("uninstall-node", "--sshuser", sshUser, INSTALL_DIR, "gf-test-2", remoteHost));

        //delete node with --uninstall
        report("delete-node-with-uninstall-remote", asadmin("delete-node-ssh", UNINSTALL, "n2"));
    }

    private void addPasswords() {
        BufferedWriter out = null;
        try {
            String pfile = System.getenv("APS_HOME") + File.separator + "config"
                            + File.separator + "adminpassword.txt";
            final File f = new File(pfile);
            System.out.println("f = " + f.toString());
            out = new BufferedWriter(new FileWriter(f, true));
            out.newLine();
            out.write("AS_ADMIN_SSHPASSWORD=" + sshPass + "\n");
            out.write("AS_ADMIN_SSHKEYPASSPHRASE=\n");
        } catch (IOException ioe) {
            //ignore
        }
        finally {
            try {
                if (out != null)
                    out.flush();
                    out.close();
            } catch(final Exception ignore){}
        }

        return;
    }

    private void updateCommonOptions() {
        String s = antProp("as.props");

        String newProps = s + NON_INTERACTIVE;
        System.setProperty("as.props", newProps);
        System.out.println("Updated common options = " + antProp("as.props"));
    }

    private final String host;
    private final File glassFishHome;
    private static final String NON_INTERACTIVE = " --interactive=false";
    private static final String INSTALL_DIR = "--installdir";
    private static final String INSTALL = "--install";
    private static final String UNINSTALL = "--uninstall";
}
