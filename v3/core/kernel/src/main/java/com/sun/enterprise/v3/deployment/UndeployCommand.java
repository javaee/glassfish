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

package com.sun.enterprise.v3.deployment;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.contract.ApplicationMetaDataPersistence;
import com.sun.enterprise.v3.data.ApplicationInfo;
import com.sun.enterprise.v3.data.ApplicationRegistry;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import com.sun.enterprise.v3.server.ServerEnvironment;
import com.sun.enterprise.v3.services.impl.GrizzlyService;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.TransactionFailure;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Undeploys applications.
 *
 * @author dochez
 */
@Service(name="undeploy")
@I18n("undeploy.command")
public class UndeployCommand extends ApplicationLifecycle implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(UndeployCommand.class);
    
    @Inject
    ServerEnvironment env;

    @Inject
    ApplicationRegistry appRegistry;

    @Inject
    GrizzlyService adapter;

    @Param(primary = true, name=DeployCommand.NAME)
    String name=null;

    @Param(optional=true)
    String target = "server";

    @Inject
    Applications applications;

    @Inject
    ArchiveFactory archiveFactory;

    Logger logger = LogDomains.getLogger(LogDomains.DPL_LOGGER);

    public void execute(AdminCommandContext context) {
        
        Properties parameters = context.getCommandParameters();
        ActionReport report = context.getActionReport();
        /**
         * A little bit of dancing around has to be done, in case the
         * user passed the path to the original directory.
         */
        name = (new File(name)).getName();
        parameters.setProperty(DeployCommand.NAME, name);

        ApplicationInfo info = appRegistry.get(name);

        Module module = getModule(name);
        Application application = null;
        if (module instanceof Application) {
            application = (Application) module;
        }

        if (module==null) {
            report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", name));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE); 
            return;
 
        }
        ReadableArchive source = null;
        if (info==null) {
            // probably disabled application
            if (application!=null) {
                File location = new File(application.getLocation());
                if (location.exists()) {
                    try {
                        source = archiveFactory.openArchive(location);
                    } catch (IOException e) {
                        logger.log(Level.INFO, e.getMessage(),e );
                    }
                } else {
                    logger.warning("Originally deployed application at "+ location + " not found");
                }
            }
        } else {
            source = info.getSource();
        }

        DeploymentContextImpl deploymentContext = new DeploymentContextImpl(logger, source, parameters, env);

        if (info!=null) {
            undeploy(name, deploymentContext, report);
        }

        // check if it's directory deployment
        boolean isDirectoryDeployed = false;
        if (application!=null) {
            isDirectoryDeployed = Boolean.valueOf(application.getDirectoryDeployed());
        }

        if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
            // so far I am doing this after the unload, maybe this should be moved before...
            try {
                // remove the "application" element
                unregisterAppFromDomainXML(name);
            } catch(TransactionFailure e) {
                logger.warning("Module " + name + " not found in configuration");
            }

            //remove context from generated
            deleteContainerMetaInfo(deploymentContext);

            //if directory deployment then do no remove the directory
            if (source!=null) {
                if (!isDirectoryDeployed && source.exists()) {
                    FileUtils.whack(new File(info.getSource().getURI()));
                }
            }
            report.setMessage(localStrings.getLocalString("undeploy.command.sucess",
                    "{0} undeployed successfully", name));
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } // else a message should have been provided.

    }
}
