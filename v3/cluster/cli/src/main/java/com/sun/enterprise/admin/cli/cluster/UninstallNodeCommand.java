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

import com.sun.enterprise.admin.cli.remote.RemoteCommand;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.trilead.ssh2.SFTPv3DirectoryEntry;
import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
//import org.glassfish.api.admin.ExecuteOn;
//import org.glassfish.api.admin.RuntimeType;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.glassfish.cluster.ssh.util.SSHUtil;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.List;

/**
 * @author Rajiv Mordani
 */


@Service(name = "uninstall-node")
@Scoped(PerLookup.class)
//@ExecuteOn({RuntimeType.DAS})
public class UninstallNodeCommand extends SSHCommandsBase {
    @Param(name="installdir", optional = true)
    private String installDir;

    @Inject
    SSHLauncher sshLauncher;

    @Inject
    Node[] nodeList;

    @Override
    protected void validate() throws CommandException {
        for (String host: hosts) {
            for (Node node: nodeList) {
                if (node.getNodeHost().equals(host)) {
                    throw new CommandException("delete-node-ssh needs to be called to delete node for " + host + " before uninstall-node is called");
                }
            }
        }
        
        sshuser = resolver.resolve(sshuser);
        if (sshkeyfile == null) {
            //if user hasn't specified a key file check if key exists in
            //default location
            String existingKey = SSHUtil.getExistingKeyFile();
            if (existingKey == null) {
                promptPass=true;
            } else {
                sshkeyfile = existingKey;
            }
        } else {
            validateKeyFile(sshkeyfile);
        }
        
        //we need the key passphrase if key is encrypted
        if(sshkeyfile != null && isEncryptedKey()){
            sshkeypassphrase=getSSHPassphrase();
        }
        
    }

    @Override
    protected int executeCommand() throws CommandException {
        try {
            //String baseRootValue = executeLocationsCommand();
            String baseRootValue = getSystemProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) + "/../";
            deleteFromHosts(baseRootValue);
        } catch (IOException ioe) {
            throw new CommandException(ioe);
        }  catch (InterruptedException e) {
            throw new CommandException(e);
        }

        return SUCCESS;
    }

    private void deleteFromHosts(String baseRootValue) throws CommandException, IOException, InterruptedException {

        if (installDir == null) {
            installDir = baseRootValue;
        }

        for (String host: hosts) {
            sshLauncher.init(sshuser, host, sshport, sshpassword, sshkeyfile, sshkeypassphrase, logger);

            if (sshkeyfile != null && !sshLauncher.checkConnection()) {
                //key auth failed, so use password auth
                promptPass=true;
            }
            
            if (promptPass) {                
                sshpassword=getSSHPassword(host);
                //re-initialize
                sshLauncher.init(sshuser, host, sshport, sshpassword, sshkeyfile, sshkeypassphrase, logger);
            }
            
            SFTPClient sftpClient = sshLauncher.getSFTPClient();


            if (!sftpClient.exists(installDir)) {
                throw new IOException (installDir + " Directory does not exist");
            }

            //ArrayList<String> remoteDirectories = new ArrayList<String>();
            //deleteRemoteFiles(sftpClient, installDir, remoteDirectories);
            deleteRemoteFiles(sftpClient, installDir);
            sftpClient.rmdir(installDir);
        }
    }

    //private void deleteRemoteFiles(SFTPClient sftpClient, String dir,
    //ArrayList<String> remoteDirectories) {
    private void deleteRemoteFiles(SFTPClient sftpClient, String dir)
    throws IOException {
        for (SFTPv3DirectoryEntry directoryEntry: (List<SFTPv3DirectoryEntry>)sftpClient.ls(dir)) {
            if (directoryEntry.filename.equals(".") || directoryEntry.filename.equals("..")) {
                continue;
            } else if (directoryEntry.attributes.isDirectory()) {
                deleteRemoteFiles(sftpClient, dir+"/"+directoryEntry.filename);
                sftpClient.rmdir(dir  +"/"+directoryEntry.filename);
            } else {
                sftpClient.rm(dir+"/"+directoryEntry.filename);
            }
        }
    }

    private String executeLocationsCommand() throws CommandException {
        RemoteCommand cmd =
                new RemoteCommand("__locations", programOpts, env);
        Map<String, String> attrs =
                cmd.executeAndReturnAttributes(new String[]{"__locations"});
        return attrs.get("Base-Root_value");

    }
    
    private void validateKeyFile(String file) throws CommandException {
        File f = new File(file);
        if (!f.exists()) {
            throw new CommandException(Strings.get("KeyDoesNotExist", file));
        }
    }
}
