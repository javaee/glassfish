/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.server.logging.commands;

import com.sun.common.util.logging.LoggingConfigImpl;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.server.logging.logviewer.backend.LogFilterForInstance;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: naman
 * Date: 6 Jul, 2010
 * Time: 3:27:24 PM
 * To change this template use File | Settings | File Templates.
 */
@ExecuteOn({RuntimeType.DAS})
@Service(name = "collect-log-files")
@Scoped(PerLookup.class)
@I18n("collect.log.files")
public class CollectLogFiles implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CollectLogFiles.class);

    private static final Logger logger =
            LogDomains.getLogger(CollectLogFiles.class, LogDomains.CORE_LOGGER);

    @Param(optional = true)
    String target = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME;

    @Param
    private String outputFilePath;

    @Inject
    ServerEnvironment env;

    @Inject
    Domain domain;

    @Inject
    private Habitat habitat;

    @Inject
    LoggingConfigImpl loggingConfig;

    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        Properties props = initFileXferProps();

        Server targetServer = domain.getServerNamed(target);

        File outputFile = new File(outputFilePath);
        if (!outputFile.exists()) {
            boolean created = outputFile.mkdir();
            if (!created) {
                final String errorMsg = localStrings.getLocalString(
                        "collectlogfiles.outputPath.notexist", "Output File Path does not exist. Please enter correct value for Outputfilepath.");
                report.setMessage(errorMsg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        if (targetServer != null && targetServer.isDas()) {

            // This loop if target instance is DAS

            String zipFile = "";
            File tempDirectory = null;

            try {
                tempDirectory = File.createTempFile("downloaded", "log");
                tempDirectory.delete();
                tempDirectory.mkdirs();
            } catch (Exception ex) {
                final String errorMsg = localStrings.getLocalString(
                        "collectlogfiles.creatingTempDirectory", "Error while creating temp directory on server for downloading log files.");
                logger.log(Level.SEVERE, errorMsg, ex);
                report.setMessage(errorMsg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            String targetDirPath = tempDirectory.getAbsolutePath() + File.separator + "logs";
            File targetDir = new File(targetDirPath);
            if (!targetDir.exists())
                targetDir.mkdir();
            targetDirPath = tempDirectory.getAbsolutePath() + File.separator + "logs" + File.separator + targetServer.getName();
            targetDir = new File(targetDirPath);
            targetDir.mkdir();

            try {
                copyLogFilesForLocalhost(env.getDomainRoot() + File.separator + "logs",
                        targetDir.getAbsolutePath(), report, targetServer.getName());
            } catch (Exception ex) {
                final String errorMsg = localStrings.getLocalString(
                        "collectlogfiles.errInstanceDownloading", "Error while downloading log files from " + target + ".");
                logger.log(Level.SEVERE, errorMsg, ex);
            }


            try {
                zipFile = loggingConfig.createZipFile(tempDirectory.getAbsolutePath());
                if (zipFile == null || new File(zipFile) == null) {
                    // Failure during zip
                    final String errorMsg = localStrings.getLocalString(
                            "collectlogfiles.creatingZip", "Error while creating zip file " + zipFile + ".");
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    report.setMessage(errorMsg);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }

            } catch (Exception e) {
                // Catching Exception if any
                final String errorMsg = localStrings.getLocalString(
                        "collectlogfiles.creatingZip", "Error while creating zip file " + zipFile + ".");
                logger.log(Level.SEVERE, errorMsg, e);
                report.setMessage(errorMsg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            // Playing with outbound payload to attach zip file..
            Payload.Outbound outboundPayload = context.getOutboundPayload();

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "About to download artifact " + zipFile);
            }

            //code to attach zip file to output directory
            try {
                File moveZipFile = new File(zipFile);
                outboundPayload.attachFile(
                        "application/octet-stream",
                        tempDirectory.toURI().relativize(moveZipFile.toURI()),
                        "files",
                        props,
                        moveZipFile);
            }
            catch (Exception ex) {
                final String errorMsg = localStrings.getLocalString(
                        "collectlogfiles.copyingZip", "Error while copying zip file to " + outputFilePath + ".");
                logger.log(Level.SEVERE, errorMsg, ex);
                report.setMessage(errorMsg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            tempDirectory.delete();

        } else if (targetServer != null && targetServer.isInstance()) {

            // This loop if target standalone instance

            String instanceName = targetServer.getName();
            String serverNode = targetServer.getNode();
            File tempDirectory = null;
            String zipFile = "";

            try {
                tempDirectory = File.createTempFile("downloaded", "log");
                tempDirectory.delete();
                tempDirectory.mkdirs();
            } catch (Exception ex) {
                final String errorMsg = localStrings.getLocalString(
                        "collectlogfiles.creatingTempDirectory", "Error while creating temp directory on server for downloading log files.");
                logger.log(Level.SEVERE, errorMsg, ex);
                report.setMessage(errorMsg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            try {
                if (serverNode.equals("localhost") || serverNode.equals("127.0.0.1")) {
                    String sourceDir = env.getInstanceRoot().getAbsolutePath() + File.separator + ".." + File.separator + ".."
                            + File.separator + "nodes" + File.separator + serverNode
                            + File.separator + instanceName + File.separator + "logs";
                    String targetDirPath = tempDirectory.getAbsolutePath() + File.separator + "logs";
                    File targetDir = new File(targetDirPath);
                    if (!targetDir.exists())
                        targetDir.mkdir();
                    targetDirPath = tempDirectory.getAbsolutePath() + File.separator + "logs" + File.separator + instanceName;
                    targetDir = new File(targetDirPath);
                    targetDir.mkdir();

                    copyLogFilesForLocalhost(sourceDir, targetDir.getAbsolutePath(), report, instanceName);
                } else {
                    new LogFilterForInstance().downloadAllInstanceLogFiles(habitat, targetServer,
                            domain, logger, instanceName, tempDirectory.getAbsolutePath());
                }
            }
            catch (Exception ex) {
                final String errorMsg = localStrings.getLocalString(
                        "collectlogfiles.errInstanceDownloading", "Error while downloading log files from " + instanceName + ".");
                logger.log(Level.SEVERE, errorMsg, ex);
                report.setMessage(errorMsg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            try {
                // Creating zip file and returning zip file absolute path.
                zipFile = loggingConfig.createZipFile(tempDirectory.getAbsolutePath());
                if (zipFile == null || new File(zipFile) == null) {
                    // Failure during zip
                    final String errorMsg = localStrings.getLocalString(
                            "collectlogfiles.creatingZip", "Error while creating zip file " + zipFile + ".");
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    report.setMessage(errorMsg);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
            catch (Exception ex) {
                final String errorMsg = localStrings.getLocalString(
                        "collectlogfiles.creatingZip", "Error while creating zip file " + zipFile + ".");
                logger.log(Level.SEVERE, errorMsg, ex);
                report.setMessage(errorMsg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            // Playing with outbound payload to attach zip file..
            Payload.Outbound outboundPayload = context.getOutboundPayload();

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "About to download artifact " + zipFile);
            }

            //code to attach zip file to output directory
            try {
                File moveZipFile = new File(zipFile);
                outboundPayload.attachFile(
                        "application/octet-stream",
                        tempDirectory.toURI().relativize(moveZipFile.toURI()),
                        "files",
                        props,
                        moveZipFile);
            }
            catch (Exception ex) {
                final String errorMsg = localStrings.getLocalString(
                        "collectlogfiles.copyingZip", "Error while copying zip file to " + outputFilePath + ".");
                logger.log(Level.SEVERE, errorMsg, ex);
                report.setMessage(errorMsg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            tempDirectory.delete();

        } else {
            // This loop if target is cluster

            String finalMessage = "";
            File tempDirectory = null;
            String zipFileName = "";

            try {
                tempDirectory = File.createTempFile("downloaded", "log");
                tempDirectory.delete();
                tempDirectory.mkdirs();
            } catch (Exception ex) {
                final String errorMsg = localStrings.getLocalString(
                        "collectlogfiles.creatingTempDirectory", "Error while creating temp directory on server for downloading log files.");
                logger.log(Level.SEVERE, errorMsg, ex);
                report.setMessage(errorMsg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            com.sun.enterprise.config.serverbeans.Cluster cluster = domain.getClusterNamed(target);
            List<Server> instances = cluster.getInstances();

            for (Server instance : instances) {
                // downloading log files for all instances which is part of cluster under temp directory.
                String instanceName = instance.getName();
                String serverNode = instance.getNode();
                boolean errorOccur = false;
                try {

                    if (serverNode.equals("localhost") || serverNode.equals("127.0.0.1")) {
                        String sourceDir = env.getInstanceRoot().getAbsolutePath() + File.separator + ".." + File.separator
                                + ".." + File.separator + "nodes" + File.separator + serverNode
                                + File.separator + instanceName + File.separator + "logs";
                        String targetDirPath = tempDirectory.getAbsolutePath() + File.separator + "logs";
                        File targetDir = new File(targetDirPath);
                        if (!targetDir.exists())
                            targetDir.mkdir();
                        targetDirPath = tempDirectory.getAbsolutePath() + File.separator + "logs" + File.separator + instanceName;
                        targetDir = new File(targetDirPath);
                        targetDir.mkdir();

                        copyLogFilesForLocalhost(sourceDir, targetDir.getAbsolutePath(), report, instanceName);
                    } else {
                        new LogFilterForInstance().downloadAllInstanceLogFiles(habitat, instance,
                                domain, logger, instanceName, tempDirectory.getAbsolutePath());
                    }
                }
                catch (Exception ex) {
                    final String errorMsg = localStrings.getLocalString(
                            "collectlogfiles.errInstanceDownloading", "Error while downloading log files from " + instanceName + ".");
                    logger.log(Level.SEVERE, errorMsg, ex);
                    errorOccur = true;
                    finalMessage += errorMsg + "\n";
                }
                if (!errorOccur) {
                    final String successMsg = localStrings.getLocalString(
                            "collectlogfiles.successInstanceDownloading", "Log files are downloaded for " + instanceName + ".");
                    finalMessage += successMsg + "\n";
                }
            }
            report.setMessage(finalMessage);

            try {
                // Creating zip file and returning zip file absolute path.                zipFileName = loggingConfig.createZipFile(tempDirectory.getAbsolutePath());
                if (zipFileName == null || new File(zipFileName) == null) {
                    // Failure during zip
                    final String errorMsg = localStrings.getLocalString(
                            "collectlogfiles.creatingZip", "Error while creating zip file " + zipFileName + ".");
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    report.setMessage(errorMsg);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }
            catch (Exception ex) {
                final String errorMsg = localStrings.getLocalString(
                        "collectlogfiles.creatingZip", "Error while creating zip file " + zipFileName + ".");
                logger.log(Level.SEVERE, errorMsg, ex);
                report.setMessage(errorMsg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }


            // Playing with outbound payload to attach zip file..
            Payload.Outbound outboundPayload = context.getOutboundPayload();

            //code to attach zip file to output directory
            try {
                File zipFile = new File(zipFileName);
                outboundPayload.attachFile(
                        "application/octet-stream",
                        tempDirectory.toURI().relativize(zipFile.toURI()),
                        "files",
                        props,
                        zipFile);
            }
            catch (Exception ex) {
                final String errorMsg = localStrings.getLocalString(
                        "collectlogfiles.copyingZip", "Error while copying zip file to " + outputFilePath + ".");
                logger.log(Level.SEVERE, errorMsg, ex);
                report.setMessage(errorMsg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            tempDirectory.delete();
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private void copyLogFilesForLocalhost(String sourceDir, String targetDir, ActionReport report, String instanceName) throws IOException {
        // Getting all Log Files
        File logsDir = new File(sourceDir);
        File allLogFileNames[] = logsDir.listFiles();
        if (allLogFileNames == null) {
            throw new IOException("");
        }
        for (File logFile : allLogFileNames) {
            // File to copy in output file path.
            File toFile = new File(targetDir, logFile.getName());

            FileInputStream from = null;
            FileOutputStream to = null;

            // Copying File
            try {
                from = new FileInputStream(logFile);
                to = new FileOutputStream(toFile);
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = from.read(buffer)) != -1)
                    to.write(buffer, 0, bytesRead); // write
            }
            catch (Exception ex) {
                final String errorMsg = localStrings.getLocalString(
                        "collectlogfiles.errInstanceDownloading", "Error while downloading log file from " + instanceName + ".");
                logger.log(Level.SEVERE, errorMsg, ex);
                report.setMessage(errorMsg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }


            if (!toFile.exists()) {
                final String errorMsg = localStrings.getLocalString(
                        "collectlogfiles.errInstanceDownloading", "Error while downloading log file from " + instanceName + ".");
                report.setMessage(errorMsg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

        }
    }

    private Properties initFileXferProps() {
        final Properties props = new Properties();
        props.setProperty("file-xfer-root", outputFilePath.replace("\\", "/"));
        return props;
    }
}
