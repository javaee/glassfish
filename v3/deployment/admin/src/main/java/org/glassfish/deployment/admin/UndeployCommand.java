/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
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
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URI;
import java.net.URISyntaxException;

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
    Deployment deployment;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    Applications apps;

    public UndeployCommand() {
        origin = Origin.undeploy;
    }

    public void execute(AdminCommandContext context) {
        
        ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();
        /**
         * A little bit of dancing around has to be done, in case the
         * user passed the path to the original directory.
         */
        name = (new File(name)).getName();

        ApplicationInfo info = deployment.get(name);

        Application application = apps.getModule(Application.class, name);

        if (application==null) {
            report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", name));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE); 
            return;
 
        }
        ReadableArchive source = null;
        if (info==null) {
            // disabled application or application failed to be
            // loaded for some reason
            if (application!=null) {
                URI uri = null;
                try {
                    uri = new URI(application.getLocation());
                } catch (URISyntaxException e) {
                    logger.severe("Cannot determine original location for application : " + e.getMessage());
                }
                if (uri != null) {
                    File location = new File(uri);
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
            }
        } else {
            source = info.getSource();
        }

        if (source == null) {
            logger.fine("Cannot get source archive for undeployment"); 
            // remove the application from the domain.xml so at least server is 
            // in a consistent state
            try {
                deployment.unregisterAppFromDomainXML(name);
            } catch(TransactionFailure e) {
                logger.warning("Module " + name + " not found in configuration");
            }
            return;
        }

        File sourceFile = new File(source.getURI());
        if (!source.exists()) {
            logger.log(Level.WARNING, "Cannot find application bits at " + 
                sourceFile.getPath());
            // remove the application from the domain.xml so at least server is 
            // in a consistent state
            try {
                deployment.unregisterAppFromDomainXML(name);
            } catch(TransactionFailure e) {
                logger.warning("Module " + name + " not found in configuration");
            }
            return;
        }

        ExtendedDeploymentContext deploymentContext = null;
        try {
            deploymentContext = deployment.getBuilder(logger, this, report).source(source).build();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot create context for undeployment ", e);
            report.setMessage(localStrings.getLocalString("undeploy.contextcreation.failed","Cannot create context for undeployment : {0} ", e.getMessage()));            
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        deploymentContext.getAppProps().putAll(application.getDeployProperties());

        if (properties!=null) {
            deploymentContext.getAppProps().putAll(properties);
        }

        deploymentContext.setModulePropsMap(
            application.getModulePropertiesMap());

        if (info!=null) {
            deployment.undeploy(name, deploymentContext);
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
                    FileUtils.whack(new File(source.getURI()));
                }
            }

            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } // else a message should have been provided.

    }
}
