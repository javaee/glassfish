/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2010 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.Application;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.Cluster;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.StateCommandParameters;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.config.support.TargetType;
import org.glassfish.config.support.CommandTarget;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.Utility;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.admin.util.ClusterOperationUtil;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.common.util.admin.ParameterMapExtractor;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.beans.PropertyVetoException;
import org.glassfish.deployment.common.ApplicationConfigInfo;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.deployment.common.VersioningDeploymentSyntaxException;

import org.glassfish.deployment.versioning.VersioningService;

/**
 * Enable command
 */
@Service(name="enable")
@I18n("enable.command")
@Cluster(value={RuntimeType.DAS, RuntimeType.INSTANCE})
@Scoped(PerLookup.class)
@TargetType(value={CommandTarget.DOMAIN, CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CLUSTERED_INSTANCE})
public class EnableCommand extends StateCommandParameters implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(EnableCommand.class);

    @Inject
    Deployment deployment;

    @Inject
    Habitat habitat;

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

    @Inject
    VersioningService versioningService;

    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        if (!deployment.isRegistered(name())) {
            report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", name()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (!DeploymentUtils.isDomainTarget(target)) {
            ApplicationRef applicationRef = domain.getApplicationRefInTarget(name(), target);
            if (applicationRef == null) {
                report.setMessage(localStrings.getLocalString("ref.not.referenced.target","Application {0} is not referenced by target {1}", name(), target));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        // return if the application is already in enabled state
        if (domain.isAppEnabledInTarget(name(), target)) {
            logger.fine("The application is already enabled");
            return;
        }

        if (env.isDas()) {
            if (!DeploymentUtils.isDomainTarget(target)) {
                // try to disable the enabled version, if exist
                try {
                    versioningService.handleDisable(name(),target, report);
                } catch (VersioningDeploymentSyntaxException e) {
                    report.failure(logger, e.getMessage());
                    return;
                }
            } else {
                List<String> targets = domain.getAllReferencedTargetsForApplication(name());
                // replicate command to all referenced targets
                try {
                    ParameterMapExtractor extractor = new ParameterMapExtractor(this);
                    ParameterMap paramMap = extractor.extract(Collections.EMPTY_LIST);
                    paramMap.set("DEFAULT", name());

                    ClusterOperationUtil.replicateCommand("enable", FailurePolicy.Error, FailurePolicy.Warn, targets, context, paramMap, habitat);
                } catch (Exception e) {
                    report.failure(logger, e.getMessage());
                    return;
                }
            }
        }

        // update the domain.xml
        try {
            deployment.updateAppEnabledAttributeInDomainXML(name(), target, true);
        } catch(TransactionFailure e) {
            logger.warning("failed to set enable attribute for " + name());
        }

        if (!domain.isCurrentInstanceMatchingTarget(target, name(), server.getName(), null)) {
            return;  
        }

        ReadableArchive archive;
        File file = null;
        DeployCommandParameters commandParams=null;
        Properties contextProps = new Properties();
        Map<String, Properties> modulePropsMap = null;
        ApplicationConfigInfo savedAppConfig = null;
        try {
            Application app = applications.getApplication(name()); 
            ApplicationRef appRef = domain.getApplicationRefInServer(server.getName(), name());
            if (app!=null && appRef != null) {
                commandParams = app.getDeployParameters(appRef);
                commandParams.origin = Origin.load;
                commandParams.target = target;
                contextProps = app.getDeployProperties();
                modulePropsMap = app.getModulePropertiesMap();
                savedAppConfig = new ApplicationConfigInfo(app);
            }
            if (commandParams==null) {
                report.setMessage(localStrings.getLocalString("bug",
                    "invalid domain.xml entries, please file a bug"));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;                
            }

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
            savedAppConfig.store(appProps);

            if (modulePropsMap != null) {
                deploymentContext.setModulePropsMap(modulePropsMap);
            }

            deployment.deploy(deploymentContext);
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error during enabling: ", e);
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
