/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli.cluster;

import java.io.*;

import org.jvnet.hk2.annotations.*;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.internal.api.Globals;
import com.sun.enterprise.admin.cli.CLICommand;
import com.sun.enterprise.admin.cli.CLIUtil;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.util.SSHUtil;

/**
 *  This is a local command that distributes the SSH public key to remote node(s)
 *
 */
@Service(name = "setup-ssh")
@Scoped(PerLookup.class)
@ExecuteOn({RuntimeType.DAS})
public final class SetupSshKey extends CLICommand {
    
    @Param(optional = true)
    private String sshuser;

    @Param(optional=true, defaultValue="22")
    private int sshport;

    @Param(optional = true)
    private String sshkeyfile;

    @Param(optional = true)
    private String sshpublickeyfile;

    @Param(optional = true, defaultValue="false")
    private boolean generatekey;

    @Param(name="hosts", optional = false, primary = true, multiple = true)
    private String[] nodes;

    @Inject
    private Habitat habitat;

    private String sshpassword;
    private String sshkeypassphrase=null;

    private boolean promptPass=false;

    /**
     */
    @Override
    protected void validate()
            throws CommandException {

        if(sshuser==null) {
            sshuser = System.getProperty("user.name");
        }

        if (sshkeyfile == null) {
            //if user hasn't specified a key file and there is no key file at default
            //location, then generate one
            if (SSHUtil.getExistingKeyFile() == null) {
                sshkeyfile = SSHUtil.getDefaultKeyFile();
                if(promptForKeyGeneration()) {
                    generatekey=true;
                    sshkeypassphrase=getSSHPassphrase();
                }
            } else {
                //there is a key that requires to be distributed, hence need password
                promptPass = true;
            }
        } else {
            validateKeyFile(sshkeyfile);
        }

        if (sshpublickeyfile != null) {
            validateKeyFile(sshpublickeyfile);
        }

    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException {

        SSHLauncher sshL=habitat.getComponent(SSHLauncher.class);
        Globals.setDefaultHabitat(habitat);

        for (String node : nodes) {
            sshL.init(sshuser, node,  sshport, sshpassword, sshkeyfile, sshkeypassphrase, logger);
            if (generatekey || promptPass) {
                //prompt for password iff required
                sshpassword=getSSHPassword();
            }
            try {
                sshL.setupKey(node, sshpublickeyfile, generatekey, sshpassword);
            } catch (IOException ce) {
                //logger.fine("SSH key setup failed: " + ce.getMessage());
                throw new CommandException(Strings.get("KeySetupFailed", ce.getMessage()));
            } catch (Exception e) {
                //handle KeyStoreException
            }
            if (sshL.checkConnection())
                logger.fine("Connection SUCCEEDED!");
        }
        return SUCCESS;
    }

    /**
     * Method that sets the prompt flag only if key file exists
     * @param file the key file
     */
    private void validateKeyFile(String file) throws CommandException {
        //if key exists, set prompt flag
        File f = new File(file);
        if (f.exists()) {
            promptPass=true;
        } else {
            throw new CommandException(Strings.get("KeyDoesNotExist", file));
        }
    }
    /**
     * Get SSH password from password file or user.
     */
    private String getSSHPassword() throws CommandException {
        String password = getFromPasswordFile("AS_ADMIN_SSH_PASSWORD");

        //get password from user if not found in password file
        if (password == null) {
            if (programOpts.isInteractive()) {
                password=readSSHPassword(Strings.get("SSHPasswordPrompt"));
            } else {
                throw new CommandException(Strings.get("SSHPasswordNotFound"));
            }
        }
        return password;
    }

    /**
     * Get SSH key passphrase from password file or user.
     */
    private String getSSHPassphrase() throws CommandException {
        String passphrase = getFromPasswordFile("AS_ADMIN_SSH_KEYPASSPHRASE");

        //get password from user if not found in password file
        if (passphrase == null) {
            if (programOpts.isInteractive()) {
                //i18n
                passphrase=readSSHPassword(Strings.get("SSHPassphrasePrompt"));
            } else {
                passphrase=""; //empty passphrase
            }
        }
        return passphrase;
    }

    private String getFromPasswordFile(String name) throws CommandException {
        String pass = null;
        String pwfile = programOpts.getPasswordFile();
        if (ok(pwfile)) {
            passwords = CLIUtil.readPasswordFileOptions(pwfile, true);
            logger.fine("Passwords from password file " + passwords);
            pass = passwords.get(name);
        }
        return pass;
    }

    /**
     * Prompt for key generation
     */
    private boolean promptForKeyGeneration() {
        if (generatekey)
            return true;
        
        if (!programOpts.isInteractive())
            return false;

        Console cons = System.console();

        if (cons != null) {
            String val = null;
            do {
                cons.printf("%s", Strings.get("GenerateKeyPairPrompt"));
                val = cons.readLine();
                if (val != null && (val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("y"))) {
                    logger.fine("Generate key!");
                    return true;
                } else if ( val != null && (val.equalsIgnoreCase("no") || val.equalsIgnoreCase("n"))) {
                    break;
                }
            } while (val != null && !isValidAnswer(val));
        }
        return false;
    }

    private boolean isValidAnswer(String val) {
        return val.equalsIgnoreCase("yes") || val.equalsIgnoreCase("no")
                || val.equalsIgnoreCase("y") || val.equalsIgnoreCase("n") ;
    }
    /**
     * Display the given prompt and read a password without echoing it.
     * Returns null if no console available.
     */
    protected String readSSHPassword(String prompt) {
        String password = null;
        Console cons = System.console();
        if (cons != null) {
            char[] pc = cons.readPassword("%s", prompt);
            // yes, yes, yes, it would be safer to not keep it in a String
            password = new String(pc);
        }
        return password;
    }
}
