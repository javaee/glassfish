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

import com.sun.enterprise.config.serverbeans.Application;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ApplicationName;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.StateCommandParameters;
import org.glassfish.api.deployment.DeployCommandParameters;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.beans.PropertyVetoException;
import org.glassfish.deployment.common.ApplicationConfigInfo;

/**
 * Enable command
 */
@Service(name="enable")
@I18n("enable.command")
@Scoped(PerLookup.class)
public class EnableCommand extends StateCommandParameters implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(EnableCommand.class);

    @Inject
    Deployment deployment;

    @Inject
    ServerEnvironmentImpl env;

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
        
        if (!deployment.isRegistered(name())) {
            report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", name()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        // return if the application is already in enabled state
        if (Boolean.valueOf(ConfigBeansUtilities.getEnabled(target, 
            name()))) {
            logger.fine("The application is already enabled");
            return;
        }

        ReadableArchive archive;
        File file = null;
        ApplicationRef appRef = null;
        DeployCommandParameters commandParams=null;
        Properties contextProps = new Properties();
        Map<String, Properties> modulePropsMap = null;
        ApplicationConfigInfo savedAppConfig = null;
        try {
            Application app = null; 
            for (ApplicationName module : applications.getModules()) {
                if (module.getName().equals(name())) {  
                    app = (Application)module;
                    break;
                }
            }

            for (ApplicationRef ref : server.getApplicationRef()) {
                if (ref.getRef().equals(name())) {
                    appRef = ref;
                    break;
                }
            }
            if (app!=null) {
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

            if (report.getActionExitCode().equals(
                ActionReport.ExitCode.SUCCESS)) {
                if (appRef!=null) {
                    ConfigSupport.apply(new SingleConfigCode<ApplicationRef>() {
                        public Object run(ApplicationRef param) throws
                            PropertyVetoException, TransactionFailure {
                            param.setEnabled(String.valueOf(true));
                            return null;
                        }
                    }, appRef);
                }
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
