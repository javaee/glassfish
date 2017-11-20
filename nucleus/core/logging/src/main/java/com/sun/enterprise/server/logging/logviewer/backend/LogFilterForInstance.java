/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.glassfish.hk2.api.ServiceLocator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public File downloadGivenInstanceLogFile(ServiceLocator habitat, Server targetServer, Domain domain, Logger logger,
                                             String instanceName, String domainRoot, String logFileName, String instanceLogFileName)
            throws IOException {

        File instanceLogFile = null;

        // method is used from logviewer back end code logfilter.
        // for Instance it's going through this loop. This will use ssh utility to get file from instance machine(remote machine) and
        // store in domains/domain1/logs/<instance name> which is used to get LogFile object.
        // Right now user needs to go through this URL to setup and configure ssh http://wikis.sun.com/display/GlassFish/3.1SSHSetup
        SSHLauncher sshL = getSSHL(habitat);
        String sNode = targetServer.getNodeRef();
        Nodes nodes = domain.getNodes();
        Node node = nodes.getNode(sNode);

        if (node.getType().equals("SSH")) {

            try {
                sshL.init(node, logger);

                SFTPClient sftpClient = sshL.getSFTPClient();

                File logFileDirectoryOnServer = makingDirectory(domainRoot + File.separator + "logs"
                        + File.separator + instanceName);

                boolean noFileFound = true;

                String loggingDir = getLoggingDirectoryForNode(instanceLogFileName, node, sNode, instanceName);

                try {
                    Vector instanceLogFileNames = sftpClient.getSftpChannel().ls(loggingDir);

                    for (int i = 0; i < instanceLogFileNames.size(); i++) {
                        LsEntry file = (LsEntry) instanceLogFileNames.get(i);
                        String fileName = file.getFilename();
                        // code to remove . and .. file which is return from sftpclient ls method
                        if (!file.getAttrs().isDir() && !fileName.equals(".") && !fileName.equals("..")
                                && fileName.contains(".log") && !fileName.contains(".log.")) {
                            noFileFound = false;
                            break;
                        }
                    }
                } catch (Exception e) {
                    // if directory doesn't present or missing on remote machine. It happens due to bug 16451
                    noFileFound = true;
                }

                if (noFileFound) {
                    // this loop is used when user has changed value for server.log but not restarted the server.
                    loggingDir = getLoggingDirectoryForNodeWhenNoFilesFound(instanceLogFileName, node, sNode, instanceName);
                }

                String loggingFile = loggingDir + File.separator + logFileName;
                if (!sftpClient.exists(loggingFile)) {
                    loggingFile = loggingDir + File.separator + "server.log";
                } else if (!sftpClient.exists(loggingFile)) {
                    loggingFile = instanceLogFileName;
                }

                // creating local file name on DAS
                long instanceLogFileSize = 0;
                instanceLogFile = new File(logFileDirectoryOnServer.getAbsolutePath() + File.separator
                        + loggingFile.substring(loggingFile.lastIndexOf(File.separator), loggingFile.length()));

                // getting size of the file on DAS
                if (instanceLogFile.exists())
                    instanceLogFileSize = instanceLogFile.length();

                SftpATTRS sftpFileAttributes = sftpClient._stat(loggingFile);

                // getting size of the file on instance machine
                long fileSizeOnNode = sftpFileAttributes.getSize();

                // if differ both size then downloading
                if (instanceLogFileSize != fileSizeOnNode) {
                    BufferedInputStream in = null;
                    FileOutputStream file = null;
                    BufferedOutputStream out = null;
                    try {
                        InputStream inputStream = sftpClient.getSftpChannel().get(loggingFile);
                        in = new BufferedInputStream(inputStream);
                        file = new FileOutputStream(instanceLogFile);
                        out = new BufferedOutputStream(file);
                        int i;
                        while ((i = in.read()) != -1) {
                            out.write(i);
                        }
                        out.flush();
                    } finally {
                        if (out != null) try {
                            out.close();
                        } catch (IOException ex) {
                        }
                        if (in != null) try {
                            in.close();
                        } catch (IOException ex) {
                        }
                    }
                }

                sftpClient.close();
            } catch (JSchException ex) {
                throw new IOException("Unable to download instance log file from SSH Node", ex);
            } catch (SftpException ex) {
                throw new IOException("Unable to download instance log file from SSH Node", ex);
            }
        }

        return instanceLogFile;

    }

    public void downloadAllInstanceLogFiles(ServiceLocator habitat, Server targetServer, Domain domain, Logger logger,
                                            String instanceName, String tempDirectoryOnServer, String instanceLogFileDirectory)
            throws IOException {

        // method is used from collect-log-files command
        // for Instance it's going through this loop. This will use ssh utility to get file from instance machine(remote machine) and
        // store in  tempDirectoryOnServer which is used to create zip file.
        // Right now user needs to go through this URL to setup and configure ssh http://wikis.sun.com/display/GlassFish/3.1SSHSetup
        SSHLauncher sshL = getSSHL(habitat);
        String sNode = targetServer.getNodeRef();
        Nodes nodes = domain.getNodes();
        Node node = nodes.getNode(sNode);

        if (node.getType().equals("SSH")) {
            try {
                sshL.init(node, logger);

                Vector allInstanceLogFileName = getInstanceLogFileNames(habitat, targetServer, domain, logger, instanceName, instanceLogFileDirectory);

                boolean noFileFound = true;
                String sourceDir = getLoggingDirectoryForNode(instanceLogFileDirectory, node, sNode, instanceName);
                SFTPClient sftpClient = sshL.getSFTPClient();

                try {
                    Vector instanceLogFileNames = sftpClient.getSftpChannel().ls(sourceDir);

                    for (int i = 0; i < instanceLogFileNames.size(); i++) {
                        LsEntry file = (LsEntry) instanceLogFileNames.get(i);
                        String fileName = file.getFilename();
                        // code to remove . and .. file which is return from sftpclient ls method
                        if (!file.getAttrs().isDir() && !fileName.equals(".") && !fileName.equals("..")
                                && fileName.contains(".log") && !fileName.contains(".log.")) {
                            noFileFound = false;
                            break;
                        }
                    }
                } catch (Exception e) {
                    // if directory doesn't present or missing on remote machine. It happens due to bug 16451
                    noFileFound = true;
                }

                if (noFileFound) {
                    // this loop is used when user has changed value for server.log but not restarted the server.
                    sourceDir = getLoggingDirectoryForNodeWhenNoFilesFound(instanceLogFileDirectory, node, sNode, instanceName);
                }

                for (int i = 0; i < allInstanceLogFileName.size(); i++) {
                    String remoteFileName = sourceDir + File.separator + allInstanceLogFileName.get(i);
                    InputStream inputStream = sftpClient.getSftpChannel().get(remoteFileName);
                    Files.copy(inputStream, Paths.get(tempDirectoryOnServer));
                }
                sftpClient.close();
            } catch (JSchException ex) {
                throw new IOException("Unable to download instance log file from SSH Node", ex);
            } catch (SftpException ex) {
                throw new IOException("Unable to download instance log file from SSH Node", ex);
            }
        }
    }

    public Vector getInstanceLogFileNames(ServiceLocator habitat, Server targetServer, Domain domain, Logger logger,
                                          String instanceName, String instanceLogFileDetails) throws IOException {

        // helper method to get all log file names for given instance
        String sNode = targetServer.getNodeRef();
        Node node = domain.getNodes().getNode(sNode);
        Vector instanceLogFileNames = null;
        Vector instanceLogFileNamesAsString = new Vector();

        // this code is used when DAS and instances are running on the same machine
        if (node.isLocal()) {
            String loggingDir = getLoggingDirectoryForNode(instanceLogFileDetails, node, sNode, instanceName);

            File logsDir = new File(loggingDir);
            File allLogFileNames[] = logsDir.listFiles();

            boolean noFileFound = true;

            if (allLogFileNames != null) { // This check for,  if directory doesn't present or missing on machine. It happens due to bug 16451
                for (int i = 0; i < allLogFileNames.length; i++) {
                    File file = allLogFileNames[i];
                    String fileName = file.getName();
                    // code to remove . and .. file which is return
                    if (file.isFile() && !fileName.equals(".") && !fileName.equals("..") && fileName.contains(".log")
                            && !fileName.contains(".log.")) {
                        instanceLogFileNamesAsString.add(fileName);
                        noFileFound = false;
                    }
                }
            }

            if (noFileFound) {
                // this loop is used when user has changed value for server.log but not restarted the server.
                loggingDir = getLoggingDirectoryForNodeWhenNoFilesFound(instanceLogFileDetails, node, sNode, instanceName);
                logsDir = new File(loggingDir);
                allLogFileNames = logsDir.listFiles();

                for (int i = 0; i < allLogFileNames.length; i++) {
                    File file = allLogFileNames[i];
                    String fileName = file.getName();
                    // code to remove . and .. file which is return
                    if (file.isFile() && !fileName.equals(".") && !fileName.equals("..") && fileName.contains(".log")
                            && !fileName.contains(".log.")) {
                        instanceLogFileNamesAsString.add(fileName);
                    }
                }
            }
        } else if (node.getType().equals("SSH")) {
            try {
                // this code is used if DAS and instance are running on different machine
                SSHLauncher sshL = getSSHL(habitat);
                sshL.init(node, logger);
                SFTPClient sftpClient = sshL.getSFTPClient();

                boolean noFileFound = true;

                String loggingDir = getLoggingDirectoryForNode(instanceLogFileDetails, node, sNode, instanceName);

                try {
                    instanceLogFileNames = sftpClient.getSftpChannel().ls(loggingDir);
                    for (int i = 0; i < instanceLogFileNames.size(); i++) {
                        LsEntry file = (LsEntry) instanceLogFileNames.get(i);
                        String fileName = file.getFilename();
                        // code to remove . and .. file which is return from sftpclient ls method
                        if (!file.getAttrs().isDir() && !fileName.equals(".") && !fileName.equals("..")
                                && fileName.contains(".log") && !fileName.contains(".log.")) {
                            instanceLogFileNamesAsString.add(fileName);
                            noFileFound = false;
                        }
                    }
                } catch (Exception ex) {
                    // if directory doesn't present or missing on remote machine. It happens due to bug 16451
                    noFileFound = true;
                }

                if (noFileFound) {
                    // this loop is used when user has changed value for server.log but not restarted the server.
                    loggingDir = getLoggingDirectoryForNodeWhenNoFilesFound(instanceLogFileDetails, node, sNode, instanceName);
                    instanceLogFileNames = sftpClient.getSftpChannel().ls(loggingDir);


                    for (int i = 0; i < instanceLogFileNames.size(); i++) {
                        LsEntry file = (LsEntry) instanceLogFileNames.get(i);
                        String fileName = file.getFilename();
                        // code to remove . and .. file which is return from sftpclient ls method
                        if (!file.getAttrs().isDir() && !fileName.equals(".") && !fileName.equals("..")
                                && fileName.contains(".log") && !fileName.contains(".log.")) {
                            instanceLogFileNamesAsString.add(fileName);
                        }
                    }
                }

                sftpClient.close();
            } catch (JSchException ex) {
                throw new IOException("Unable to download instance log file from SSH Node", ex);
            } catch (SftpException ex) {
                throw new IOException("Unable to download instance log file from SSH Node", ex);
            }
        }
        return instanceLogFileNamesAsString;
    }

    private SSHLauncher getSSHL(ServiceLocator habitat) {
        return habitat.getService(SSHLauncher.class);
    }

    private File makingDirectory(String path) {
        File targetDir = new File(path);
        boolean created = false;
        boolean deleted = false;
        if (targetDir.exists()) {
            deleted = targetDir.delete();
            if (!deleted) {
                return targetDir;
            }

        }
        created = targetDir.mkdir();
        if (!created) {
            return null;
        } else {
            return targetDir;
        }

    }

    public String getLoggingDirectoryForNode(String instanceLogFileDirectory, Node node, String sNode, String instanceName) {
        String loggingDir = "";

        if (instanceLogFileDirectory.contains("${com.sun.aas.instanceRoot}/logs") && node.getNodeDir() != null) {
            // this code is used if no changes made in logging.properties file
            loggingDir = node.getNodeDir() + File.separator + sNode
                    + File.separator + instanceName + File.separator + "logs";
        } else if (instanceLogFileDirectory.contains("${com.sun.aas.instanceRoot}/logs") && node.getInstallDir() != null) {
            loggingDir = node.getInstallDir() + File.separator + "glassfish" + File.separator + "nodes"
                    + File.separator + sNode + File.separator + instanceName + File.separator + "logs";
        } else {
            // this code is used when user changes the attributes value(com.sun.enterprise.server.logging.GFFileHandler.file) in
            // logging.properties file to something else.
            loggingDir = instanceLogFileDirectory.substring(0, instanceLogFileDirectory.lastIndexOf(File.separator));
        }

        return loggingDir;
    }

    public String getLoggingDirectoryForNodeWhenNoFilesFound(String instanceLogFileDirectory, Node node, String sNode, String instanceName) {
        String loggingDir = "";

        if (node.getNodeDir() != null) {
            // this code is used if no changes made in logging.properties file
            loggingDir = node.getNodeDir() + File.separator + sNode
                    + File.separator + instanceName + File.separator + "logs";
        } else if (node.getInstallDir() != null) {
            loggingDir = node.getInstallDir() + File.separator + "glassfish" + File.separator + "nodes"
                    + File.separator + sNode + File.separator + instanceName + File.separator + "logs";
        } else {
            // this code is used when user changes the attributes value(com.sun.enterprise.server.logging.GFFileHandler.file) in
            // logging.properties file to something else.
            loggingDir = instanceLogFileDirectory.substring(0, instanceLogFileDirectory.lastIndexOf(File.separator));
        }

        return loggingDir;

    }


}
