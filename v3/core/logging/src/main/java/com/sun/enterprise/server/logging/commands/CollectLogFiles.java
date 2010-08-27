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
import com.sun.enterprise.server.logging.GFFileHandler;
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
import java.util.ArrayList;
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
    Domain domain;

    @Inject
    private Habitat habitat;

    @Inject
    GFFileHandler gf;

    @Inject
    LoggingConfigImpl loggingConfig;

    boolean zipDone = false;

    public void execute(AdminCommandContext context) {

        try {

            final ActionReport report = context.getActionReport();

            Properties props = initFileXferProps();

            Server targetServer = domain.getServerNamed(target);

            List<String> instancesForReplication = new ArrayList<String>();

            File outputFile = new File(outputFilePath);
            if (!outputFile.exists()) {
                boolean created = outputFile.mkdir();
                if (!created) {
                    final String errorMsg = localStrings.getLocalString(
                            "outputPath.notexist", "Outputfilepath Doen not exists. Please enter correct value for Outputfilepath.");
                    report.setMessage(errorMsg);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                }
            }

            if (targetServer != null && targetServer.isDas()) {

                // This loop if target instance is DAS
                File file = new File(outputFilePath + File.separator + "server");
                file.mkdir();

                // Getting currenet Log File
                File logFile = gf.getCurrentLogFile();

                // File to copy in output file path.
                File toFile = new File(file, logFile.getName());

                FileInputStream from = null;
                FileOutputStream to = null;

                // Copying File
                from = new FileInputStream(logFile);
                to = new FileOutputStream(toFile);
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = from.read(buffer)) != -1)
                    to.write(buffer, 0, bytesRead); // write

                if (!toFile.exists()) {
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                }

                try {
                    String zipFile = loggingConfig.createZipFile(outputFile.getAbsolutePath());
                    if (zipFile == null || new File(zipFile) == null) {
                        // Failure during zip
                        final String errorMsg = localStrings.getLocalString(
                                "download.errDownloading", "Error while creating zip file.");
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        report.setMessage(errorMsg);
                    }

                } catch (Exception e) {
                    // Catching Exception if any
                    final String errorMsg = localStrings.getLocalString(
                            "download.errDownloading", "Error while creating zip file.");
                    logger.log(Level.SEVERE, errorMsg, e);
                    report.setMessage(errorMsg);
                    report.setFailureCause(e);
                }

            } else {
                // This loop if target is not DAS

                File tempDirectory = File.createTempFile("downloaded", "log");
                tempDirectory.delete();
                tempDirectory.mkdirs();

                com.sun.enterprise.config.serverbeans.Cluster cluster = domain.getClusterNamed(target);
                List<Server> instances = cluster.getInstances();

                for (Server instance : instances) {
                    // downloading log files for all instances which is part of cluster under temp directory.
                    String instanceName = instance.getName();
                    new LogFilterForInstance().getInstanceLogFile(habitat, instance,
                            domain, logger, instanceName, tempDirectory.getAbsolutePath());
                }

                // Creating zip file and returning zip file absolute path.
                String zipFileName = loggingConfig.createZipFile(tempDirectory.getAbsolutePath());


                // Playing with outbound payload to attach zip file..
                Payload.Outbound outboundPayload = context.getOutboundPayload();


                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "About to download artifact " + zipFileName);
                }

                //code to attach zip file to output directory
                File zipFile = new File(zipFileName);
                outboundPayload.attachFile(
                        "application/octet-stream",
                        tempDirectory.toURI().relativize(zipFile.toURI()),
                        "files",
                        props,
                        zipFile);

                tempDirectory.delete();
            }

            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        }
        catch (Exception e) {
            e.printStackTrace();
            // Catching Exception if any
            final String errorMsg = localStrings.getLocalString(
                    "download.errDownloading", "Error while downloading generated files from one of the Instance.");
            logger.log(Level.SEVERE, errorMsg, e);
            ActionReport report = context.getActionReport();
            boolean reportErrorsInTopReport = false;
            if (!reportErrorsInTopReport) {
                report = report.addSubActionsReport();
                report.setActionExitCode(ActionReport.ExitCode.WARNING);
            } else {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            }
            report.setMessage(errorMsg);
            report.setFailureCause(e);
        }
    }

    private Properties initFileXferProps() {
        final Properties props = new Properties();
        props.setProperty("file-xfer-root", outputFilePath.replace("\\", "/"));
        return props;
    }
}
