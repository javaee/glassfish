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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/*
 * Base class for SSH Dev tests
 * @author Yamini K B
 * @author Joe Di Pol
 */
public abstract class SshBaseDevTest extends AdminBaseDevTest {
    static final String SSH_DIRECTORY = System.getProperty("user.home")
                                + File.separator + ".ssh" + File.separator;

    // Properties that may be passed into test

    // Host to create remote instances on
    static final String SSH_HOST_PROP = "ssh.host";

    // SSH user to use when connecting to ssh.host. Defaults to user running test
    static final String SSH_USER_PROP = "ssh.user";

    // GlassFish installdir on ssh.host.
    static final String SSH_INSTALLDIR_PROP = "ssh.installdir";

    // Alternative to SSH_INSTALLDIR_PROP. If you provide this prefix
    // then SSHNodeTest will generate a unique install directory using this
    // prefix. Typically used with ssh.doinstall=true.
    static final String SSH_INSTALLPREFIX_PROP = "ssh.installprefix";

    // Location of nodedir to use. If not set use default location.
    static final String SSH_NODEDIR_PROP = "ssh.nodedir";

    // SSH password to use when connecting to ssh.host. Not needed if
    // public key authentication is already set up.
    static final String SSH_PASSWORD_PROP = "ssh.password";

    // Controlls whether or not the SSHNodeTest does an install for you
    // onto ssh.host.
    static final String SSH_DOINSTALL_PROP = "ssh.doinstall";

    // Used by SetupSshTest
    static final String SSH_CONFIGURE_PROP = "ssh.configure";

    static enum PasswordType { SSH_PASS, KEY_PASS };

    static final String pFile = System.getenv("APS_HOME") +
             File.separator + "config" + File.separator + "adminpassword.txt";

    static final String tmpFile = System.getenv("APS_HOME") +
            File.separator + "config" + File.separator + "adminpassword.tmp";

    /**
     * Add a password to the asadmin password file used by the test.
     *
     * @param value     Password to use
     * @param passType  SSH_PASS if you are setting ssh password.
     *                  KEY_PASS if you are setting encryption key
     */
    void addPassword(String value, PasswordType passType) {
        BufferedWriter out = null;

        if (value == null)
            value="";
        try {
            final File f = new File(pFile);
            out = new BufferedWriter(new FileWriter(f, true));
            out.newLine();
            switch (passType) {
                case SSH_PASS:
                    out.write("AS_ADMIN_SSHPASSWORD=" + value);
                    break;
                case KEY_PASS:
                    out.write("AS_ADMIN_SSHKEYPASSPHRASE=" + value);
                    break;
                default:
                    //do nothing
            }
        } catch (IOException ioe) {
            //ignore
        }
        finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch(final Exception ignore){}
        }

        return;
    }

    /**
     * Remove SSH related passwords from the asadmin password file
     */
    void removePasswords() {
        final File f = new File(pFile);
        final File tempFile = new File(tmpFile);

        BufferedReader reader = null;
        BufferedWriter writer = null;

        try {
            writer=new BufferedWriter(new FileWriter(tempFile));
            reader = new BufferedReader(new FileReader(f));

            String currentLine;

            while((currentLine = reader.readLine()) != null && !currentLine.trim().isEmpty()) {
                if(currentLine.trim().startsWith("AS_ADMIN_SSH"))
                    continue;
                writer.write(currentLine);
                writer.newLine();
            }

            reader.close();
            writer.close();

            //On Windows, rename will fail if destination file already exists
            if(!f.delete()) {
                System.out.println("Failed to delete original file");
            }

            if(!tempFile.renameTo(f)) {
                System.out.println("Failed to restore password file.");
            }
        } catch (IOException ioe) {
            //ignore
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch(final Exception ignore){}
        }

        return;
    }

    /**
     * Modify asadmin common options to include --interactive=false
     */
    void disableInteractiveMode() {
        String s = antProp("as.props");

        String newProps = s + " --interactive=false";
        System.setProperty("as.props", newProps);
        System.out.println("Updated common options = " + antProp("as.props"));
    }
}
