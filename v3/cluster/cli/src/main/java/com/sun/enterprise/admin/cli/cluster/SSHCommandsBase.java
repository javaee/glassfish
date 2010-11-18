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
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import com.sun.enterprise.admin.cli.CLICommand;
import org.glassfish.cluster.ssh.util.SSHUtil;
import org.glassfish.cluster.ssh.sftp.SFTPClient;

import com.sun.enterprise.universal.glassfish.TokenResolver;

import com.trilead.ssh2.SFTPv3DirectoryEntry;

/**
 *  Base class for SSH provisioning commands.
 *
 */
public abstract class SSHCommandsBase extends CLICommand {

    @Param(optional = true, defaultValue="${user.name}")
    protected String sshuser;

    @Param(optional=true, defaultValue="22")
    protected int sshport;

    @Param(optional = true)
    protected String sshkeyfile;

    @Param(optional = false, primary = true, multiple = true)
    protected String[] hosts;

    protected String sshpassword;
    protected String sshkeypassphrase=null;

    protected boolean promptPass=false;

    protected TokenResolver resolver = null;

    public SSHCommandsBase() {
        // Create a resolver that can replace system properties in strings
        Map<String, String> systemPropsMap =
                new HashMap<String, String>((Map)(System.getProperties()));
        resolver = new TokenResolver(systemPropsMap);
    }

    /**
     * Get SSH password from password file or user.
     */
    protected String getSSHPassword(String node) throws CommandException {
        String password = getFromPasswordFile("AS_ADMIN_SSHPASSWORD");

        //get password from user if not found in password file
        if (password == null) {
            if (programOpts.isInteractive()) {
                password=readSSHPassword(Strings.get("SSHPasswordPrompt", sshuser, node));
            } else {
                throw new CommandException(Strings.get("SSHPasswordNotFound"));
            }
        }
        return password;
    }

    /**
     * Get SSH key passphrase from password file or user.
     */
    protected String getSSHPassphrase() throws CommandException {
        String passphrase = getFromPasswordFile("AS_ADMIN_SSHKEYPASSPHRASE");

        //get password from user if not found in password file
        if (passphrase == null) {
            if (programOpts.isInteractive()) {
                //i18n
                passphrase=readSSHPassword(Strings.get("SSHPassphrasePrompt", sshkeyfile));
            } else {
                passphrase=""; //empty passphrase
            }
        }
        return passphrase;
    }

    private String getFromPasswordFile(String name) {
        return passwords.get(name);
    }

    protected boolean isValidAnswer(String val) {
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

    protected boolean isEncryptedKey() throws CommandException {
        boolean res = false;
        try {
            res = SSHUtil.isEncryptedKey(sshkeyfile);
        } catch (IOException ioe) {
            throw new CommandException(Strings.get("ErrorParsingKey", sshkeyfile, ioe.getMessage()));
        }
        return res;
    }
    
    protected void deleteRemoteFiles(SFTPClient sftpClient, String dir)
    throws IOException {
        for (SFTPv3DirectoryEntry directoryEntry: (List<SFTPv3DirectoryEntry>)sftpClient.ls(dir)) {
            if (directoryEntry.filename.equals(".") || directoryEntry.filename.equals("..")
                    || directoryEntry.filename.equals("nodes")) {
                continue;
            } else if (directoryEntry.attributes.isDirectory()) {
                deleteRemoteFiles(sftpClient, dir+"/"+directoryEntry.filename);
                sftpClient.rmdir(dir  +"/"+directoryEntry.filename);
            } else {
                sftpClient.rm(dir+"/"+directoryEntry.filename);
            }
        }
    }
}
