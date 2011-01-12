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
                                             String instanceName, String domainRoot, String logFileName) throws IOException {

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
        if (logFileDirectoryOnServer.exists())
            logFileDirectoryOnServer.delete();

        logFileDirectoryOnServer.mkdirs();

        instanceLogFile = new File(logFileDirectoryOnServer.getAbsolutePath() + File.separator + logFileName);

        InputStream inputStream = sftpClient.read(node.getInstallDir() +
                File.separator + "glassfish" + File.separator + "nodes" +
                File.separator + sNode + File.separator + instanceName +
                File.separator + "logs" + File.separator + logFileName);

        BufferedInputStream in = new BufferedInputStream(inputStream);
        FileOutputStream file = new FileOutputStream(instanceLogFile);
        BufferedOutputStream out = new BufferedOutputStream(file);
        int i;
        while ((i = in.read()) != -1) {
            out.write(i);
        }
        out.flush();

        return instanceLogFile;

    }

    public void downloadAllInstanceLogFiles(Habitat habitat, Server targetServer, Domain domain, Logger logger,
                                            String instanceName, String tempDirectoryOnServer) throws IOException {

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

        Vector allInstanceLogFileName = getInstanceLogFileNames(habitat, targetServer, domain, logger, instanceName);

        File logFileDirectoryOnServer = new File(tempDirectoryOnServer + File.separator + "logs"
                + File.separator + instanceName);
        if (logFileDirectoryOnServer.exists())
            logFileDirectoryOnServer.delete();

        logFileDirectoryOnServer.mkdirs();

        String[] remoteFileNames = new String[allInstanceLogFileName.size()];
        for (int i = 0; i < allInstanceLogFileName.size(); i++) {
            remoteFileNames[i] = node.getInstallDir() + File.separator +
                    "glassfish" + File.separator + "nodes" + File.separator +
                    sNode + File.separator + instanceName + File.separator +
                    "logs" + File.separator + allInstanceLogFileName.get(i);
        }

        scpClient.get(remoteFileNames, logFileDirectoryOnServer.getAbsolutePath());


    }

    public Vector getInstanceLogFileNames(Habitat habitat, Server targetServer, Domain domain, Logger logger,
                                          String instanceName) throws IOException {

        // helper method to get all log file names for given instance
        String sNode = targetServer.getNodeRef();
        Node node = domain.getNodes().getNode(sNode);
        Vector instanceLogFileNames = new Vector();
        Vector instanceLogFileNamesAsString = new Vector();

        if (node.isLocal()) {
            String sourceDir = System.getProperty("com.sun.aas.instanceRoot") + File.separator + ".." + File.separator + ".."
                    + File.separator + "nodes" + File.separator + sNode
                    + File.separator + instanceName + File.separator + "logs";

            File logsDir = new File(sourceDir);
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

            SSHLauncher sshL = getSSHL(habitat);
            sshL.init(node, logger);

            SFTPClient sftpClient = sshL.getSFTPClient();

            instanceLogFileNames = sftpClient.ls(node.getInstallDir() +
                    File.separator + "glassfish" + File.separator + "nodes" +
                    File.separator + sNode + File.separator + instanceName +
                    File.separator + "logs");

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
