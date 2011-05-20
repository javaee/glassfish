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

import java.io.*;
import java.net.*;

import java.util.Arrays;

/*
 * Dev tests for SSH public key access setup command i.e setup-ssh
 * @author Yamini K B
 */
public class SetupSshTest extends AdminBaseDevTest {
    private static final String SSH_HOST_PROP = "ssh.host";
    private static final String SSH_USER_PROP = "ssh.user";
    private static final String SSH_PASSWORD = "ssh.password";

    private String remoteHost = null;
    private String sshPass = null;
    private String sshUser = null;

    public SetupSshTest() {
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
        new SetupSshTest().run();
    }

    @Override
    public String getTestName() {
        return "SSH public key access test";
    }

    @Override
    protected String getTestDescription() {
        return "Developer tests for setup-ssh";
    }

    public void run() {

        boolean failed = false;
        remoteHost = System.getProperty(SSH_HOST_PROP);
        sshPass = System.getProperty(SSH_PASSWORD);
        sshUser = System.getProperty(SSH_USER_PROP);

        if (remoteHost == null || remoteHost.length() == 0) {
            System.out.printf("%s requires you set the %s property\n",
                this.getClass().getName(), SSH_HOST_PROP);
            report("setup-ssh-*", false);
            return;
        } else {
            System.out.printf("%s=%s\n", SSH_HOST_PROP, remoteHost);
        }        

        if (sshPass == null || sshPass.length() == 0) {
            System.out.printf("%s requires you set the %s property\n",
                this.getClass().getName(), SSH_PASSWORD);
            report("setup-ssh-*", false);
            return;
        } else {
            System.out.printf("%s=%s\n", SSH_PASSWORD, "<concealed>");
        }

        if (sshUser == null || sshUser.length() == 0) {
            sshUser = System.getProperty("user.name");
        } else {
            System.out.printf("%s=%s\n", SSH_USER_PROP, sshUser);
        }

        addPasswords();

        updateCommonOptions();

        String backup = System.getProperty("user.home")
                                + File.separator + ".ssh.bak";

        if (doesSSHDirectoryExist()) {
            if (getExistingKeyFile() != null) {
                if(!testKeyDistributionWithoutKeyGeneration()) {
                    System.out.println("FAILURE: Please remove the public key manually on " + remoteHost + " and re-run the tests");
                    return;
                }
            }

            //backup existing .ssh directory
            if(!renameSSHDirectory(SSH_DIRECTORY, backup)) {
                System.out.println("Unable to rename .ssh directory, backing out from running the remaining tests.");
                failed = true;
            }
        }        

        if(!failed) {
            testKeyGeneration();

            //restore .ssh directory
            if(!renameSSHDirectory(backup, SSH_DIRECTORY)) {
                System.out.println("Unable to restore .ssh.bak directory, please rename manually.");
            }
        }
	stat.printSummary();
    }
    
    private boolean testKeyDistributionWithoutKeyGeneration() {
        boolean res = true;

        //ssh already setup
        AsadminReturn ret = asadminWithOutput("setup-ssh", SSH_USER_OPTION, sshUser, remoteHost);
        boolean success = ret.outAndErr.indexOf("SSH public key authentication is already configured") >= 0;

        if (success) {
            //no point in running the tests if ssh already works, so bail out..
            return false;
        } else {
            report("setup-ssh-key", ret.returnValue);
        }

        return res;
    }       

    private void testKeyGeneration() {

        //will fail since default value of generatekey=false
        report("setup-ssh-with-missing-key-pair", !asadmin("setup-ssh", SSH_USER_OPTION, sshUser, remoteHost));

        report("setup-ssh-with-key-generation", asadmin("setup-ssh", SSH_USER_OPTION, sshUser, "--generatekey", remoteHost));

        //invalid host name
        report("setup-ssh-password-failure", !asadmin("setup-ssh", SSH_USER_OPTION, "foo", remoteHost));

        //invalid key file
        report("setup-ssh-invalid-keyfile", !asadmin("setup-ssh", "--sshkeyfile", "foo", remoteHost));

        //invalid public key file
        report("setup-ssh-invalid-public-keyfile", !asadmin("setup-ssh", "--sshpublickeyfile", "foo", remoteHost));

        //without ssh password
        //report("setup-ssh-without-password", !asadmin("setup-ssh", remoteHost));

        //should succeed second time as well
        report("setup-ssh-with-password", asadmin("setup-ssh", SSH_USER_OPTION, sshUser, remoteHost));

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

    private boolean renameSSHDirectory(String from, String to) {
        File file = new File(from);
        File file2 = new File(to);
        if (file2.exists()) {
            deleteDirectory(file2);
        }

        if (!file.exists()) {
            return true;
        }
        return file.renameTo(file2);
    }

    private static String getExistingKeyFile() {
        String key = null;
        for (String keyName : Arrays.asList("id_rsa","id_dsa",
                                                "identity"))
        {
            File f = new File(SSH_DIRECTORY+keyName);
            if (f.exists()) {
                key =  SSH_DIRECTORY + keyName;
                break;
            }
        }
        return key;
    }

    private static boolean doesSSHDirectoryExist() {
        boolean result = false;
        File f = new File(SSH_DIRECTORY);
        if (f.exists()) {
            result = true;
        }
        return result;
    }
    private final String host;
    private final File glassFishHome;
    private static final String NON_INTERACTIVE = " --interactive=false";
    private static final String SSH_USER_OPTION = "--sshuser";
    private static final String SSH_DIRECTORY = System.getProperty("user.home")
                                + File.separator + ".ssh" + File.separator;
}
