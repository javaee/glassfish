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

import com.sun.enterprise.admin.cli.CLICommand;
import com.sun.enterprise.admin.cli.remote.RemoteCommand;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.FileListerRelative;
import com.sun.enterprise.util.zip.ZipFileException;
import com.sun.enterprise.util.zip.ZipWriter;
import com.trilead.ssh2.SCPClient;
import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
//import org.glassfish.api.admin.ExecuteOn;
//import org.glassfish.api.admin.RuntimeType;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Rajiv Mordani
 */


@Service(name = "install-node")
@Scoped(PerLookup.class)
//@ExecuteOn({RuntimeType.DAS})
public class InstallNodeCommand extends CLICommand {

    @Param(optional = true)
    private String sshuser;

    @Param(optional=true)
    private int sshport;

    @Param(optional = true)
    private String sshkeyfile;


    @Param(name="archivedir", optional = true)
    private String archiveDir;

    @Param(optional = false, primary = true, multiple = true)
    private String[] hosts;

    @Param(name="installdir", optional = true)
    private String installDir;

    @Param(optional = true)
    private boolean recreate;

    @Param(optional = true)
    private boolean force;

    private String sshpassword;

    private String sshkeypassphrase=null;
    
    private boolean promptPass=false;

    @Inject
    SSHLauncher sshLauncher;

    protected void validate() throws CommandException {

    }

    @Override
    protected int executeCommand() throws CommandException {
        try {
            //String baseRootValue = executeLocationsCommand();
            String baseRootValue = getSystemProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) + "/../"; 
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

    private void copyToHosts(File zipFile, String baseRootValue, ArrayList<String> binDirFiles) throws IOException, InterruptedException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        if (installDir == null) {
            installDir = baseRootValue;
        }
        
        for (String host: hosts) {
            sshLauncher.init(sshuser, host, sshport, sshpassword, sshkeyfile, sshkeypassphrase, logger);
            //String remoteDir = installLocation + "/glassfish3/glassfish";

            SFTPClient sftpClient = sshLauncher.getSFTPClient();
            SCPClient scpClient = sshLauncher.getSCPClient();
            
            if (!sftpClient.exists(installDir)) {
                sftpClient.mkdirs(installDir, 0755);
            }
            

            scpClient.put(zipFile.getAbsolutePath(), installDir);
            //String unzipCommand = "cd " + installDir + "; unzip glassfish.zip; chmod +x bin/*";
            String unzipCommand = "cd " + installDir + "; jar -xvf glassfish.zip";
            sshLauncher.runCommand(unzipCommand, outStream);
            sftpClient.rm(installDir + "/glassfish.zip");
            for (String binDirFile: binDirFiles) {
                sftpClient.chmod((installDir + "/" + binDirFile), 0755);
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

    private File createZipFile(String baseRootValue, ArrayList<String> binDirFiles) throws IOException, ZipFileException {

        File installRoot = new File(baseRootValue); 

        File zipFileLocation = null;
        File glassFishZipFile = null;

        if (archiveDir != null) {
            zipFileLocation = new File(archiveDir);
            glassFishZipFile = new File(zipFileLocation, "glassfish.zip");
            if (glassFishZipFile.exists() && !recreate) {
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

        return glassFishZipFile;

    }
}
