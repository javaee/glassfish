/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.deployment.admin;

import org.glassfish.deployment.common.DeploymentContextImpl;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ParameterNames;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.Archive;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import org.glassfish.internal.data.ApplicationInfo;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import org.glassfish.api.ActionReport;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.ParameterNames;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

import java.util.Properties;
import java.util.Collection;
import java.util.logging.Level;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Enable command
 */
@Service(name="enable")
@I18n("enable.command")
@Scoped(PerLookup.class)
public class EnableCommand extends ApplicationLifecycle implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(EnableCommand.class);    

    @Inject
    ServerEnvironmentImpl env;

    @Param(primary=true)
    String component = null;

    @Param(optional=true)
    String target = "server";

    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    public void execute(AdminCommandContext context) {
        final Properties parameters = context.getCommandParameters();
        final ActionReport report = context.getActionReport();
        
        if (!isRegistered(component)) {
            report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", component));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        // return if the application is already in enabled state
        if (Boolean.valueOf(ConfigBeansUtilities.getEnabled(target, 
            component))) {
            logger.fine("The application is already enabled");
            return;
        }


        if (snifferManager.getSniffers().isEmpty()) {
            String msg = localStrings.getLocalString("nocontainer", "No container services registered, done...");
            logger.severe(msg);
            report.setMessage(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        ReadableArchive archive;
        File file = null;
        Properties commandParams;
        Properties contextProps;
        try {
            String path = null;
            Application app = 
                (Application)ConfigBeansUtilities.getModule(component); 
            ApplicationRef appRef = 
                ConfigBeansUtilities.getApplicationRefInServer(target, 
                    component);

            commandParams = populateDeployParamsFromDomainXML(app, appRef);
            contextProps = populateDeployPropsFromDomainXML(app);
 
            parameters.putAll(commandParams);
            URI uri = new URI(parameters.getProperty(
                ParameterNames.LOCATION));
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
            ArchiveHandler archiveHandler = getArchiveHandler(archive);
            if (archiveHandler==null) {
                report.setMessage(localStrings.getLocalString("unknownarchivetype","Archive type of {0} was not recognized",file.getName()));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            final DeploymentContextImpl deploymentContext =
                new DeploymentContextImpl(logger, archive, parameters, env);
            deploymentContext.setPhase(DeploymentContextImpl.Phase.PREPARE);
            deploymentContext.createClassLoaders(clh, archiveHandler);

            deploymentContext.setProps(contextProps);
            enable(component, deploymentContext, report);

            if (report.getActionExitCode().equals(
                ActionReport.ExitCode.SUCCESS)) {
                setEnableAttributeInDomainXML(component, true);
            }

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
