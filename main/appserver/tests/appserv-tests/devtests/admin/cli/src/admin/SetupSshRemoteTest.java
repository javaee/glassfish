/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
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
 * Dev tests for SSH public key access setup remote command i.e _setup-ssh
 * The tests can be run against localhost or remote host. If ssh.host property
 * is not defined, the tests will use localhost as the target host.
 * To run the tests, do:
 *    ant -Dteststorun=setup-ssh -Dssh.configure=false -Dssh.host=onyx
 *        -Dssh.user=hudson -Dssh.password=hudson all
 *
 * @author Yamini K B
 */
public class SetupSshRemoteTest extends SshBaseDevTest {
    private static final String SSH_USER_OPTION = "--sshuser";

    private static final String backup = System.getProperty("user.home")
                                + File.separator + ".ssh.bak";

    private static final String SSH_KEY = "resources/ssh/id_dsa";
    private static final String SSH_KEY_PASSPHRASE = "Hello World!";
    private static final String SSH_ALIAS_PASS = "ssh-pass";

    private static enum PasswordValue { EMPTY, RIGHT, WRONG };

    private final String host;
    private final File glassFishHome;
    private String remoteHost = null;
    private String sshPass = null;
    private String sshUser = null;
    private Boolean sshConfigure = false;

    public SetupSshRemoteTest() {
        String host0 = null;

        try {
            host0 = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception e) {
            host0 = "localhost";
        }
        host = host0;
        glassFishHome = getGlassFishHome();

        sshUser = TestUtils.getExpandedSystemProperty(SSH_USER_PROP);
        remoteHost = TestUtils.getExpandedSystemProperty(SSH_HOST_PROP);
        sshPass = TestUtils.getExpandedSystemProperty(SSH_PASSWORD_PROP);
    }

    public static void main(String[] args) {
        new SetupSshRemoteTest().run();
    }

    @Override
    public String getTestName() {
        return "SSH public key access test";
    }

    @Override
    protected String getTestDescription() {
        return "Developer tests for _setup-ssh";
    }

    @Override
    public void subrun() {

        boolean failed = false;
        boolean runTest = true;

        if (!ok(remoteHost)) {
            remoteHost=host;
        }

        if (!ok(sshUser)) {
            sshUser = System.getProperty("user.name");
        }

        if (!ok(sshPass)) {
            System.out.printf("%s requires you set the %s property\n",
                this.getClass().getName(), SSH_PASSWORD_PROP);
            runTest = false;
        }

        if (!runTest) {
            report("_setup-ssh-*", false);
            return;
        }

        System.out.printf("%s=%s\n", "Host", host);
        System.out.printf("%s=%s\n", "GlassFish Home", glassFishHome);
        System.out.printf("%s=%s\n", SSH_HOST_PROP, remoteHost);
        System.out.printf("%s=%s\n", SSH_USER_PROP,
                (ok(sshUser) ? sshUser : "<default>" ));
        System.out.printf("%s=%s\n", SSH_PASSWORD_PROP,
                (ok(sshPass) ? "<concealed>" : "<none>" ));
        System.out.println("Password file = " +  Constants.pFile);

        addPassword(PasswordValue.RIGHT, PasswordType.SSH_PASS);

        //to add --interactive=false;
        disableInteractiveMode();

        startDomain();

        if (doesSSHDirectoryExist()) {
            if (getExistingKeyFile() != null) {
                if(!testKeyDistributionWithoutKeyGeneration()) {
                    System.out.println("FAILURE: Please remove the public key manually on " + remoteHost + " and re-run the tests");
                    //clean up the password file
                    removePasswords("SSH");
                    return;
                }
            }

            //backup existing .ssh directory
            if(!renameDirectory(SSH_DIRECTORY, backup)) {
                System.out.println("Unable to rename .ssh directory, backing out from running the remaining tests.");
                failed = true;
            }
        }

        if(!failed) {
            testOptions();
            testPasswordAlias();
            testKeyGeneration();
            testEncryptedKey();

            //restore .ssh directory
            if(!renameDirectory(backup, SSH_DIRECTORY)) {
                System.out.println("Unable to restore .ssh.bak directory, please rename manually.");
            }
        }

        //clean up the password file
        removePasswords("SSH");
        stopDomain();
	stat.printSummary();
    }

    private boolean testKeyDistributionWithoutKeyGeneration() {
        boolean res = true;

        //ssh already setup
        AsadminReturn ret = asadminWithOutput("_setup-ssh", SSH_USER_OPTION, sshUser, remoteHost);
        boolean success = ret.outAndErr.indexOf("SSH public key authentication is already configured") >= 0;

        if (success) {
            //no point in running the tests if ssh already works, so bail out..
            return false;
        } else {
            report("_setup-ssh-key-with-existing-key", ret.returnValue);
        }

        return res;
    }

    private void testOptions() {
        //public key file does not exist
        report("_setup-ssh-no-public-key", !asadmin("_setup-ssh", SSH_USER_OPTION, sshUser, "--sshkeyfile", getFilePath("resources/ssh/emptykey"), remoteHost));

        //key contents invalid
        report("_setup-ssh-invalid-key", !asadmin("_setup-ssh", SSH_USER_OPTION, sshUser, "--sshkeyfile", getFilePath("resources/ssh/identity"), remoteHost));

        //key file not an absolute path
        report("_setup-ssh-invalid-keyfile", !asadmin("_setup-ssh", "--sshkeyfile", "foo", remoteHost));

        //invalid key file
        report("_setup-ssh-invalid-keyfile-1", !asadmin("_setup-ssh", "--sshkeyfile", "/tmp/foo", remoteHost));

        //public key file not an absolute path
        report("_setup-ssh-invalid-public-keyfile", !asadmin("_setup-ssh", "--sshpublickeyfile", "foo", "--generatekey", remoteHost));

        //invalid public key file
        report("_setup-ssh-invalid-public-keyfile-1", !asadmin("_setup-ssh", "--sshpublickeyfile", "/tmp/foo", "--generatekey", remoteHost));

    }

    private void testPasswordAlias() {
        addPassword(sshPass, PasswordType.ALIAS_PASS);
        asadmin("create-password-alias", SSH_ALIAS_PASS);
        removePasswords("ALIAS");

        addPassword("${ALIAS=foo}", PasswordType.SSH_PASS);
        report("_setup-ssh-invalid-alias", !asadmin("_setup-ssh", "--generatekey", remoteHost));

        addPassword("${ALIAS=" + SSH_ALIAS_PASS + "}", PasswordType.SSH_PASS);
        report("_setup-ssh-with-alias", asadmin("_setup-ssh", "--generatekey", remoteHost));
        deleteDirectory(new File(SSH_DIRECTORY));
        removePasswords("SSH");

        asadmin("delete-password-alias", SSH_ALIAS_PASS);
    }

    private void testKeyGeneration() {

        //will fail since default value of generatekey=false
        report("_setup-ssh-with-missing-key-pair", !asadmin("_setup-ssh", SSH_USER_OPTION, sshUser, remoteHost));

        removePasswords("SSH");
        report("_setup-ssh-without-password", !asadmin("_setup-ssh", SSH_USER_OPTION, sshUser, "--generatekey", remoteHost));

        addPassword(PasswordValue.EMPTY, PasswordType.SSH_PASS);
        report("_setup-ssh-with-empty-password", !asadmin("_setup-ssh", SSH_USER_OPTION, sshUser, "--generatekey", remoteHost));
        deleteDirectory(new File(SSH_DIRECTORY));
        removePasswords("SSH");

        addPassword(PasswordValue.WRONG, PasswordType.SSH_PASS);
        report("_setup-ssh-with-wrong-password", !asadmin("_setup-ssh", SSH_USER_OPTION, sshUser, "--generatekey", remoteHost));
        deleteDirectory(new File(SSH_DIRECTORY));
        removePasswords("SSH");

        //restore correct password
        addPassword(PasswordValue.RIGHT, PasswordType.SSH_PASS);

        report("_setup-ssh-with-key-generation", asadmin("_setup-ssh", SSH_USER_OPTION, sshUser, "--generatekey", remoteHost));

        //invalid host name
        report("_setup-ssh-password-failure", !asadmin("_setup-ssh", SSH_USER_OPTION, "foo", remoteHost));

        //should succeed second time as well
        report("_setup-ssh-with-password", !asadmin("_setup-ssh", SSH_USER_OPTION, sshUser, remoteHost));

    }

    private void testEncryptedKey() {
        //pass correct ssh password for each test
        addPassword(PasswordValue.RIGHT, PasswordType.SSH_PASS);
        addPassword(PasswordValue.EMPTY, PasswordType.KEY_PASS);
        report("_setup-ssh-encrypted-key-with-empty-password", !asadmin("_setup-ssh", SSH_USER_OPTION, sshUser, "--generatekey", "--sshkeyfile", getFilePath(SSH_KEY), remoteHost));
        deleteDirectory(new File(SSH_DIRECTORY));
        removePasswords("SSH");

        addPassword(PasswordValue.RIGHT, PasswordType.SSH_PASS);
        addPassword(PasswordValue.WRONG, PasswordType.KEY_PASS);
        report("_setup-ssh-encrypted-key-with-wrong-password", !asadmin("_setup-ssh", SSH_USER_OPTION, sshUser, "--generatekey", "--sshkeyfile", getFilePath(SSH_KEY), remoteHost));
        deleteDirectory(new File(SSH_DIRECTORY));
        removePasswords("SSH");

        addPassword(PasswordValue.RIGHT, PasswordType.SSH_PASS);
        addPassword(PasswordValue.RIGHT, PasswordType.KEY_PASS);
        report("_setup-ssh-encrypted-key-with-key-generation", asadmin("_setup-ssh", SSH_USER_OPTION, sshUser, "--generatekey", "--sshkeyfile", getFilePath(SSH_KEY), remoteHost));
        removePasswords("SSH");
    }

    /**
     * Make an entry in the password file.
     * @param pass actual value
     * @param passType SSH_PASS|KEY_PASS
     */
    private void addPassword(PasswordValue pass, PasswordType passType) {
        switch (pass) {
            case EMPTY:
                addPassword("", passType);
                break;
            case RIGHT:
                if (passType.equals(PasswordType.SSH_PASS))
                    addPassword(sshPass, passType);
                else if (passType.equals(PasswordType.KEY_PASS))
                    addPassword(SSH_KEY_PASSPHRASE, passType);
                break;
            case WRONG:
                addPassword("xxx", passType);
                break;
            default:
                //do nothing
        }
        return;
    }



    private boolean renameDirectory(String from, String to) {
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

    private String getFilePath(String file) {
        File f = new File (file);
        return f.getAbsolutePath();
    }
}
