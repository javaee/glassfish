/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package org.glassfish.deployment.admin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.Cluster;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.ActionReport;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.internal.deployment.SnifferManager;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.ActionReport.ExitCode;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.Server;


/**
 * Trivial example of deployment-for-instance command
 * @author hzhang_jn
 * @author tjquinn
 */
@Service(name="_deploy")
@Scoped(PerLookup.class)
@Cluster(value={RuntimeType.INSTANCE})
public class InstanceDeployCommand extends InstanceDeployCommandParameters implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(InstanceDeployCommand.class);
    private final static String LS = System.getProperty("line.separator");

    @Inject
    Deployment deployment;

    @Inject
    SnifferManager snifferManager;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    ServerEnvironment env;

    @Inject(name= ServerEnvironment.DEFAULT_INSTANCE_NAME)
    protected Server server;

    @Override
    public void execute(AdminCommandContext ctxt) {

        long operationStartTime = Calendar.getInstance().getTimeInMillis();
        final ActionReport report = ctxt.getActionReport();
        final Logger logger = ctxt.getLogger();
        ReadableArchive archive = null;

        this.origin = Origin.deploy_instance;

        this.previousContextRoot = preservedcontextroot;

        try {
            if (!path.exists()) {
                report.setMessage(localStrings.getLocalString("fnf","File not found", path.getAbsolutePath()));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            if (snifferManager.hasNoSniffers()) {
                String msg = localStrings.getLocalString("nocontainer", "No container services registered, done...");
                report.failure(logger,msg);
                return;
            }

            archive = archiveFactory.openArchive(path, this);
            ArchiveHandler archiveHandler = deployment.getArchiveHandler(archive);
            if (archiveHandler==null) {
                report.failure(logger,localStrings.getLocalString("deploy.unknownarchivetype","Archive type of {0} was not recognized",path.getName()));
                return;
            }

            // clean up any left over repository files
            if ( ! keepreposdir.booleanValue()) {
                FileUtils.whack(new File(env.getApplicationRepositoryPath(), name));
            }

            ExtendedDeploymentContext deploymentContext = deployment.getBuilder(logger, this, report).source(archive).build();

            // clean up any remaining generated files
            deploymentContext.clean();

            deploymentContext.getAppProps().putAll(appprops);

            // move/copy the generated directories to the expected location
            renameOrCopyFileParam(generatedejbdir, deploymentContext.getScratchDir("ejb"), logger);
            renameOrCopyFileParam(generatedjspdir, deploymentContext.getScratchDir("jsp"), logger);
            renameOrCopyFileParam(generatedxmldir, deploymentContext.getScratchDir("xml"), logger);
            if (generatedpolicydir != null) {
                renameOrCopyFileParam(generatedpolicydir, deploymentContext.getScratchDir("policy"), logger);
            }

            ApplicationInfo appInfo;
            if (type==null) {
                appInfo = deployment.deploy(deploymentContext);
            } else {
                appInfo = deployment.deploy(deployment.prepareSniffersForOSGiDeployment(type, deploymentContext), deploymentContext);
            }

            if (report.getActionExitCode()==ActionReport.ExitCode.SUCCESS) {
                try {
                    // register application information in domain.xml
                    deployment.registerAppInDomainXML(appInfo, deploymentContext);
                } catch (Exception e) {
                    // roll back the deployment and re-throw the exception
                    deployment.undeploy(name, deploymentContext);
                    deploymentContext.clean();
                    throw e;
                }
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            report.failure(logger,localStrings.getLocalString(
                    "failToLoadOnInstance",
                    "Failed to load the application on instance {0} : {1}", server.getName(), e.getMessage()),null);
        } finally {
            try {
                if (archive != null)  {
                    archive.close();
                }
            } catch(IOException e) {
                logger.log(Level.INFO, localStrings.getLocalString(
                        "errClosingArtifact",
                        "Error while closing deployable artifact : ",
                        path.getAbsolutePath()), e);
            }
            logger.info(localStrings.getLocalString(
                        "deploy.done",
                        "Deployment of {0} done is {1} ms",
                        name,
                        (Calendar.getInstance().getTimeInMillis() - operationStartTime)));
        }

    }

    private File renameOrCopyFileParam(
            final File fileParam,
            final File newLocation,
            final Logger logger) throws IOException {
        if (fileParam == null) {
            return null;
        }
        File result = null;
        newLocation.mkdirs();
        final boolean renameResult = FileUtils.renameFile(fileParam, newLocation);
        /*
         * If the rename failed then it could be because the new location is
         * on a different device, for example.  In that case, try copying
         * the file.
         */
        if (renameResult) {
            result = newLocation;
        } else {
            FileUtils.copyTree(fileParam, newLocation);
            result = newLocation;
        }
        return result;
    }
}
