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
import org.glassfish.deployment.common.DeploymentProperties;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import org.glassfish.server.ServerEnvironmentImpl;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ParameterNames;
import org.glassfish.api.admin.config.Named;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;
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
@Scoped(PerLookup.class)
public class UndeployCommand extends UndeployCommandParameters implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(UndeployCommand.class);
    
    @Inject
    ServerEnvironmentImpl env;

    @Inject
    Deployment deployment;

    @Inject
    ArchiveFactory archiveFactory;

    public void execute(AdminCommandContext context) {
        
        ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();
        /**
         * A little bit of dancing around has to be done, in case the
         * user passed the path to the original directory.
         */
        name = (new File(name)).getName();

        ApplicationInfo info = deployment.get(name);

        Named module = ConfigBeansUtilities.getModule(name);
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

        DeploymentContextImpl deploymentContext = new DeploymentContextImpl(logger, source, this, env, false);
        if (properties!=null) {
            deploymentContext.setProps(properties);
        }

        if (info!=null) {
            deployment.undeploy(name, deploymentContext, report);
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
                deployment.unregisterAppFromDomainXML(name);
            } catch(TransactionFailure e) {
                logger.warning("Module " + name + " not found in configuration");
            }

            //remove context from generated
            deploymentContext.clean();

            //if directory deployment then do no remove the directory
            if (source!=null) {
                if ( (! keepreposdir) && !isDirectoryDeployed && source.exists()) {
                    FileUtils.whack(new File(info.getSource().getURI()));
                }
            }

            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } // else a message should have been provided.

    }
}
