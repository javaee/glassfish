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

import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.FileListerRelative;
import com.sun.enterprise.util.zip.ZipFileException;
import com.sun.enterprise.util.zip.ZipWriter;
import com.trilead.ssh2.SCPClient;
import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.util.SSHUtil;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.internal.api.Globals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Rajiv Mordani
 */


@Service(name = "install-node")
@Scoped(PerLookup.class)
public class InstallNodeCommand extends SSHCommandsBase {
    @Param(name="archivedir", optional = true)
    private String archiveDir;

    @Param(name="installdir", optional = true)
    private String installDir;

    @Param(optional = true)
    private boolean recreate;

    @Param(optional = true)
    private boolean force;

    @Inject
    private Habitat habitat;
    
    @Inject
    SSHLauncher sshLauncher;

    @Override
    protected void validate() throws CommandException {
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
        Globals.setDefaultHabitat(habitat);
        try {

            String baseRootValue = getSystemProperty(SystemPropertyConstants.PRODUCT_ROOT_PROPERTY) ; 
            ArrayList<String>  binDirFiles = new ArrayList<String>();
            File zipFile = createZipFile(baseRootValue, binDirFiles);
            copyToHosts(zipFile, baseRootValue, binDirFiles);
        } catch (IOException ioe) {
            throw new CommandException(ioe);
        } catch (ZipFileException ze) {
            throw new CommandException(ze);
        } catch (InterruptedException e) {
            throw new CommandException(e);
        }

        return SUCCESS;
    }

    private void copyToHosts(File zipFile, String baseRootValue, ArrayList<String> binDirFiles) throws IOException, InterruptedException, CommandException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
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
                
            //String remoteDir = installLocation + "/glassfish3/glassfish";

            SFTPClient sftpClient = sshLauncher.getSFTPClient();
            SCPClient scpClient = sshLauncher.getSCPClient();
            try {
                if (!sftpClient.exists(installDir)) {
                    sftpClient.mkdirs(installDir, 0755);
                }
            } catch (IOException ioe){
                logger.info(Strings.get("mkdir.failed",installDir, host));
                throw new IOException(ioe);
            }

            String zip = zipFile.getCanonicalPath();
            try {
                logger.info("Copying " + zip + " (" + zipFile.length() +" bytes)" + " to " + host + ":" + installDir);
                scpClient.put(zipFile.getAbsolutePath(), installDir);
                logger.finer("Copied " + zip + " to " + host + ":" + installDir);
            } catch (IOException ex){
                logger.info (Strings.get("cannot.copy.zip.file", zip, host));
                throw new IOException (ex);
            }

            try {
                logger.info("Installing " + zip + " into " + host + ":" + installDir);
                String unzipCommand = "cd " + installDir + "; jar -xvf glassfish.zip";
                int status = sshLauncher.runCommand(unzipCommand, outStream);
                if (status != 0){
                    logger.info (Strings.get("jar.failed", host, outStream.toString()));
                    throw new CommandException ("Remote command output: " +outStream.toString());
                }
                logger.finer("Installed " + zip + " into " + host + ":" + installDir);
            } catch (IOException ioe){
                logger.info (Strings.get("jar.failed", host, outStream.toString()));
                throw new IOException (ioe);
            }

            try {
                logger.info("Removing " + host + ":" + installDir + "/glassfish.zip");
                sftpClient.rm(installDir + "/glassfish.zip");
                logger.finer("Removed " + host + ":" + installDir + "/glassfish.zip");
            } catch (IOException ioe){
                logger.info(Strings.get("remove.glassfish.failed",host, installDir));
                throw new IOException(ioe);
            }


            logger.info("Fixing file permissions of all files under " + host + ":" + installDir + "/bin");
            try {
                for (String binDirFile: binDirFiles) {
                    sftpClient.chmod((installDir + "/" + binDirFile), 0755);
                }
                logger.finer("Fixed file permissions of all files under " + host + ":" + installDir + "/bin");
            } catch (IOException ioe){
                logger.info(Strings.get("fix.permissions.failed", host, installDir));
                throw new IOException(ioe);
            }
        }
    }

    private File createZipFile(String baseRootValue, ArrayList<String> binDirFiles) throws IOException, ZipFileException {

        File installRoot = new File(baseRootValue); 

        File zipFileLocation = null;
        File glassFishZipFile = null;

        if (archiveDir != null) {
            zipFileLocation = new File(archiveDir);
            glassFishZipFile = new File(zipFileLocation, "glassfish.zip");
            if (glassFishZipFile.exists() && !recreate) {
                logger.finer("Found " + glassFishZipFile.getCanonicalPath());
                return glassFishZipFile;
            } else if (!zipFileLocation.canWrite()) {
                throw new IOException ("Cannot write to " + archiveDir);
            }
        } else if (installRoot.canWrite()) {
            zipFileLocation = installRoot;
        } else {
            zipFileLocation = new File(System.getProperty("java.io.tmpdir"));
        }

        glassFishZipFile = new File(zipFileLocation, "glassfish.zip");
        if (glassFishZipFile.exists() && !recreate) {
            logger.finer("Found " + glassFishZipFile.getCanonicalPath());
            return glassFishZipFile;
        }


        FileListerRelative lister = new FileListerRelative(installRoot);
        lister.keepEmptyDirectories();
        String[] files = lister.getFiles();

        List<String> resultFiles1 = Arrays.asList(files);
        ArrayList<String> resultFiles = new ArrayList<String>(resultFiles1);

        Iterator<String> iter = resultFiles.iterator();
        while(iter.hasNext()) {
            String fileName = iter.next();
            if (fileName.contains("domains") || fileName.contains("nodes")) {
                iter.remove();
            } else if (fileName.startsWith("bin") || fileName.startsWith("glassfish/bin")) {
                binDirFiles.add(fileName);
            }
        }

        String [] filesToZip = new String[resultFiles.size()];
        filesToZip = resultFiles.toArray(filesToZip);

        ZipWriter writer = new ZipWriter(glassFishZipFile.getCanonicalPath(), installRoot.toString(), filesToZip);
        writer.safeWrite();
        logger.info("Created installation zip " + glassFishZipFile.getCanonicalPath());

        return glassFishZipFile;
    }
    
    private void validateKeyFile(String file) throws CommandException {
        File f = new File(file);
        if (!f.exists()) {
            throw new CommandException(Strings.get("KeyDoesNotExist", file));
        }
    }
}
