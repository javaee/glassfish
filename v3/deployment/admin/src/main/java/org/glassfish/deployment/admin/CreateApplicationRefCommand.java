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

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.Cluster;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.OpsParams.Origin;
import org.glassfish.config.support.TargetType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.deployment.common.DeploymentUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ServerTags;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.Transaction;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.glassfish.deployment.common.ApplicationConfigInfo;

/**
 * Create application ref command
 */
@Service(name="create-application-ref")
@I18n("create.application.ref.command")
@Cluster(value={RuntimeType.DAS})
@Scoped(PerLookup.class)
@TargetType(value={CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
public class CreateApplicationRefCommand implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateApplicationRefCommand.class);

    @Param(primary=true)
    public String name = null;

    @Param(optional=true)
    String target = "server";

    @Param(optional=true)
    public String virtualservers = null;

    @Param(optional=true, defaultValue="true")
    public Boolean enabled = true;

    @Param(optional=true, defaultValue="true")
    public Boolean lbenabled = true;

    @Inject
    Deployment deployment;

    @Inject
    Domain domain;

    @Inject
    ServerEnvironment env;

    @Inject
    Applications applications;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject(name= ServerEnvironment.DEFAULT_INSTANCE_NAME)
    protected Server server;

    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        if (!deployment.isRegistered(name)) {
            report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", name));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        final DeployCommandSupplementalInfo suppInfo =
              new DeployCommandSupplementalInfo();
        context.getActionReport().
              setResultType(DeployCommandSupplementalInfo.class, suppInfo);

        ApplicationRef applicationRef = domain.getApplicationRefInTarget(name, target);
        if (applicationRef != null) {
            suppInfo.setAppRefExists(true);
            return;
        }

        Transaction t = new Transaction();

        ReadableArchive archive;
        File file = null;
        ApplicationRef appRef = null;
        DeployCommandParameters commandParams=null;
        Properties contextProps = new Properties();
        Map<String, Properties> modulePropsMap = null;
        ApplicationConfigInfo savedAppConfig = null;
        Application app = applications.getApplication(name); 
        try {
            commandParams = app.getDeployParameters(null);
            commandParams.origin = Origin.load;
            commandParams.target = target;
            commandParams.virtualservers = virtualservers;
            commandParams.enabled = enabled;
            commandParams.lbenabled = lbenabled;
            contextProps = app.getDeployProperties();
            modulePropsMap = app.getModulePropertiesMap();
            savedAppConfig = new ApplicationConfigInfo(app);

            URI uri = new URI(app.getLocation());
            file = new File(uri);

            if (!file.exists()) {
                report.setMessage(localStrings.getLocalString("fnf",
                    "File not found", file.getAbsolutePath()));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            archive = archiveFactory.openArchive(file);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error opening deployable artifact : " + file.getAbsolutePath(), e);
            report.setMessage(localStrings.getLocalString("unknownarchiveformat", "Archive format not recognized"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }


        try {
            final ExtendedDeploymentContext deploymentContext = 
                    deployment.getBuilder(logger, commandParams, report).source(archive).build();

            Properties appProps = deploymentContext.getAppProps();
            appProps.putAll(contextProps);

            // relativize the location so it could be set properly in 
            // domain.xml 
            String location = DeploymentUtils.relativizeWithinDomainIfPossible(new URI(app.getLocation()));
            appProps.setProperty(ServerTags.LOCATION, location);

            // relativize the URI properties so they could store in the 
            // domain.xml properly on the instances
            String appLocation = appProps.getProperty(Application.APP_LOCATION_PROP_NAME);
            appProps.setProperty(Application.APP_LOCATION_PROP_NAME, DeploymentUtils.relativizeWithinDomainIfPossible(new URI(appLocation)));
            String planLocation = appProps.getProperty(Application.DEPLOYMENT_PLAN_LOCATION_PROP_NAME);
            if (planLocation != null) {
                appProps.setProperty(Application.DEPLOYMENT_PLAN_LOCATION_PROP_NAME, DeploymentUtils.relativizeWithinDomainIfPossible(new URI(planLocation)));
            }
            savedAppConfig.store(appProps);

            if (modulePropsMap != null) {
                deploymentContext.setModulePropsMap(modulePropsMap);
            }

            if (domain.isCurrentInstanceMatchingTarget(target, name, server.getName(), null)) {
                deployment.deploy(deploymentContext);
            }

            if (report.getActionExitCode().equals(
                ActionReport.ExitCode.SUCCESS)) {
                try {
                    deployment.registerAppInDomainXML(null, deploymentContext, t, true);
                    suppInfo.setDeploymentContext(deploymentContext);
                } catch(TransactionFailure e) {
                    logger.warning("failed to create application ref for " + name);
                }
            }

        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error during creating application ref ", e);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(e.getMessage());
        } finally {
            try {
                archive.close();
            } catch(IOException e) {
                logger.log(Level.INFO, "Error while closing deployable artifact : " + file.getAbsolutePath(), e);
            }
        }
    }        
}
