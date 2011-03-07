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

package com.sun.enterprise.server.logging.logviewer.backend;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.SFTPv3DirectoryEntry;
import com.trilead.ssh2.SFTPv3FileAttributes;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.jvnet.hk2.component.Habitat;

import java.io.*;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: Naman Mehta
 * Date: 6 Aug, 2010
 * Time: 11:20:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class LogFilterForInstance {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(LogFilterForInstance.class);

    public File downloadGivenInstanceLogFile(Habitat habitat, Server targetServer, Domain domain, Logger logger,
                                             String instanceName, String domainRoot, String logFileName, String instanceLogFileName) throws IOException {

        File instanceLogFile = null;

        // method is used from logviewer back end code logfilter.
        // for Instance it's going through this loop. This will use ssh utility to get file from instance machine(remote machine) and
        // store in domains/domain1/logs/<instance name> which is used to get LogFile object.
        // Right now user needs to go through this URL to setup and configure ssh http://wikis.sun.com/display/GlassFish/3.1SSHSetup
        SSHLauncher sshL = getSSHL(habitat);
        String sNode = targetServer.getNodeRef();
        Nodes nodes = domain.getNodes();
        Node node = nodes.getNode(sNode);
        sshL.init(node, logger);

        SFTPClient sftpClient = sshL.getSFTPClient();

        File logFileDirectoryOnServer = new File(domainRoot + File.separator + "logs"
                + File.separator + instanceName);
        if (!logFileDirectoryOnServer.exists())
            logFileDirectoryOnServer.mkdirs();

        String loggingFile = "";

        if (instanceLogFileName.equals("${com.sun.aas.instanceRoot}/logs/server.log")) {
            // this code is used if no changes made to log file name under logging.properties file
            loggingFile = node.getInstallDir() + File.separator + "glassfish" + File.separator + "nodes"
                    + File.separator + sNode + File.separator + instanceName + File.separator + "logs"
                    + File.separator + logFileName;

            // verifying loggingFile presents or not if not then changing logFileName value to server.log. It means wrong name is coming
            // from GUI to back end code.
            if (!sftpClient.exists(loggingFile)) {
                loggingFile = node.getInstallDir() + File.separator + "glassfish" + File.separator + "nodes"
                        + File.separator + sNode + File.separator + instanceName + File.separator + "logs"
                        + File.separator + "server.log";
            }

        } else {
            // this code is used when user changes the attributes value(com.sun.enterprise.server.logging.GFFileHandler.file) in
            // logging.properties file to something else.
            loggingFile = instanceLogFileName.substring(0, instanceLogFileName.lastIndexOf(File.separator))
                    + File.separator + logFileName;
            if (!sftpClient.exists(loggingFile)) {
                loggingFile = instanceLogFileName;
            }
        }

        // creating local file name on DAS
        long instanceLogFileSize = 0;
        instanceLogFile = new File(logFileDirectoryOnServer.getAbsolutePath() + File.separator
                + loggingFile.substring(loggingFile.lastIndexOf(File.separator), loggingFile.length()));

        // getting size of the file on DAS
        if (instanceLogFile.exists())
            instanceLogFileSize = instanceLogFile.length();

        SFTPv3FileAttributes sftPv3FileAttributes = sftpClient._stat(loggingFile);

        // getting size of the file on instance machine
        long fileSizeOnNode = sftPv3FileAttributes.size;

        // if differ both size then downloading
        if (instanceLogFileSize != fileSizeOnNode) {

            InputStream inputStream = sftpClient.read(loggingFile);

            BufferedInputStream in = new BufferedInputStream(inputStream);
            FileOutputStream file = new FileOutputStream(instanceLogFile);
            BufferedOutputStream out = new BufferedOutputStream(file);
            int i;
            while ((i = in.read()) != -1) {
                out.write(i);
            }
            out.flush();
        }

        sftpClient.close();

        return instanceLogFile;

    }

    public void downloadAllInstanceLogFiles(Habitat habitat, Server targetServer, Domain domain, Logger logger,
                                            String instanceName, String tempDirectoryOnServer, String instanceLogFileDirectory) throws IOException {

        // method is used from collect-log-files command
        // for Instance it's going through this loop. This will use ssh utility to get file from instance machine(remote machine) and
        // store in  tempDirectoryOnServer which is used to create zip file.
        // Right now user needs to go through this URL to setup and configure ssh http://wikis.sun.com/display/GlassFish/3.1SSHSetup
        SSHLauncher sshL = getSSHL(habitat);
        String sNode = targetServer.getNodeRef();
        Nodes nodes = domain.getNodes();
        Node node = nodes.getNode(sNode);
        sshL.init(node, logger);

        SCPClient scpClient = sshL.getSCPClient();
        Vector allInstanceLogFileName = getInstanceLogFileNames(habitat, targetServer, domain, logger, instanceName, instanceLogFileDirectory);

        File logFileDirectoryOnServer = new File(tempDirectoryOnServer + File.separator + "logs"
                + File.separator + instanceName);
        if (logFileDirectoryOnServer.exists())
            logFileDirectoryOnServer.delete();

        logFileDirectoryOnServer.mkdirs();

        String sourceDir = "";
        if (instanceLogFileDirectory.contains("${com.sun.aas.instanceRoot}/logs")) {
            sourceDir = node.getInstallDir() + File.separator +
                    "glassfish" + File.separator + "nodes" + File.separator +
                    sNode + File.separator + instanceName + File.separator +
                    "logs" + File.separator;
        } else {
            sourceDir = instanceLogFileDirectory.substring(0, instanceLogFileDirectory.lastIndexOf(File.separator));
        }

        String[] remoteFileNames = new String[allInstanceLogFileName.size()];
        for (int i = 0; i < allInstanceLogFileName.size(); i++) {
            remoteFileNames[i] = sourceDir + File.separator + allInstanceLogFileName.get(i);
        }

        scpClient.get(remoteFileNames, logFileDirectoryOnServer.getAbsolutePath());

    }

    public Vector getInstanceLogFileNames(Habitat habitat, Server targetServer, Domain domain, Logger logger,
                                          String instanceName, String instanceLogFileDirectory) throws IOException {

        // helper method to get all log file names for given instance
        String sNode = targetServer.getNodeRef();
        Node node = domain.getNodes().getNode(sNode);
        Vector instanceLogFileNames = new Vector();
        Vector instanceLogFileNamesAsString = new Vector();

        // this code is used when DAS and instances are running on the same machine
        if (node.isLocal()) {
            String loggingDir = "";

            if (instanceLogFileDirectory.contains("${com.sun.aas.instanceRoot}/logs/")) {
                // this code is used if no changes made in logging.properties file
                loggingDir = System.getProperty("com.sun.aas.instanceRoot") + File.separator + ".." + File.separator + ".."
                        + File.separator + "nodes" + File.separator + sNode
                        + File.separator + instanceName + File.separator + "logs";
            } else {
                // this code is used when user changes the attributes value(com.sun.enterprise.server.logging.GFFileHandler.file) in
                // logging.properties file to something else.
                loggingDir = instanceLogFileDirectory.substring(0, instanceLogFileDirectory.lastIndexOf(File.separator));
            }

            File logsDir = new File(loggingDir);
            File allLogFileNames[] = logsDir.listFiles();
            if (allLogFileNames != null) {
                instanceLogFileNames = new Vector(Arrays.asList(allLogFileNames));
            }

            for (int i = 0; i < instanceLogFileNames.size(); i++) {
                File file = (File) instanceLogFileNames.get(i);
                String fileName = file.getName();
                // code to remove . and .. file which is return
                if (file.isFile() && !fileName.equals(".") && !fileName.equals("..") && fileName.contains(".log")
                        && !fileName.contains(".log.")) {
                    instanceLogFileNamesAsString.add(fileName);
                }
            }
        } else {
            // this code is used if DAS and instance are running on different machine
            SSHLauncher sshL = getSSHL(habitat);
            sshL.init(node, logger);
            SFTPClient sftpClient = sshL.getSFTPClient();

            String loggingDir = "";

            if (instanceLogFileDirectory.contains("${com.sun.aas.instanceRoot}/logs/")) {
                // this code is used if no changes made in logging.properties file
                loggingDir = node.getInstallDir() +
                        File.separator + "glassfish" + File.separator + "nodes" +
                        File.separator + sNode + File.separator + instanceName +
                        File.separator + "logs";
            } else {
                // this code is used when user changes the attributes value(com.sun.enterprise.server.logging.GFFileHandler.file) in
                // logging.properties file to something else.
                loggingDir = instanceLogFileDirectory.substring(0, instanceLogFileDirectory.lastIndexOf(File.separator));
            }

            instanceLogFileNames = sftpClient.ls(loggingDir);

            for (int i = 0; i < instanceLogFileNames.size(); i++) {
                SFTPv3DirectoryEntry file = (SFTPv3DirectoryEntry) instanceLogFileNames.get(i);
                String fileName = file.filename;
                // code to remove . and .. file which is return from sftpclient ls method
                if (!file.attributes.isDirectory() && !fileName.equals(".") && !fileName.equals("..")
                        && fileName.contains(".log") && !fileName.contains(".log.")) {
                    instanceLogFileNamesAsString.add(fileName);
                }
            }
        }

        return instanceLogFileNamesAsString;
    }

    private SSHLauncher getSSHL(Habitat habitat) {
        SSHLauncher sshL = null;
        try {
            sshL = habitat.getComponent(SSHLauncher.class);
        }
        catch (NoClassDefFoundError ex) {
            throw new NoClassDefFoundError(localStrings.getLocalString(
                    "collectlogfiles.missingclusterlibraries", "Missing Cluster libraries in your ClassPath."));
        }
        return sshL;
    }
}
