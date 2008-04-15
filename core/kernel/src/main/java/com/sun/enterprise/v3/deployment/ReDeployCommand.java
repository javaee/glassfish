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
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.deployment;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.v3.admin.CommandRunner;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.serverbeans.Application;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.annotations.Scoped;
import java.util.Properties;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * ReDeploy command
 *
 * @author Jerome Dochez
 * 
 */
@Service(name="redeploy")
@Scoped(PerLookup.class)
@I18n("redeploy.command")

public class ReDeployCommand implements AdminCommand {

    @Inject
    CommandRunner commandRunner;
    
    @Inject
    ApplicationHelper appHelper;

    @Param(optional=false)
    String name;

    @Param(primary=true, optional=true)
    String path = null;

        //define this variable to skip parameter valadation.
        //Param validation will be done when referening deploy command.
    boolean skipParamValidation = true;
    
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ReDeployCommand.class);
    
    /**
     * Executes the command.
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final Application app = appHelper.findApplicationByName(name);        
        if (!validateParameters(app, report)) {
            return;
        }
        Properties deployParam = new Properties(context.getCommandParameters());
        deployParam.put("force", Boolean.TRUE.toString());
        deployParam.put("path", path);
        commandRunner.doCommand("deploy", deployParam, report);
    }

        /**
         * Validate the parameters, name and path.
         * Check if name is registered.  For redeployment, the
         * application must be previously deployed.
         * Verify that path is valid and not null.
         *
         * @param app - Application
         * @param report - ActionReport.
         *
         * @returns true if validation successfully else return false.
         */
    boolean validateParameters(final Application app, final ActionReport report) {
        if (app == null) {
            report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", name));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        else if (path == null) {
            /**
             * If path is not specified on the command line but the application
             * is not directory deployed then throw an exception since we don't
             * want to undeploy and then deploy from the domain_root.
             */
            if (!Boolean.parseBoolean(app.getDirectoryDeployed())) {
                report.setMessage(localStrings.getLocalString("redeploy.command.cannot.redeploy","Cannot redeploy this app {0} without specify the operand.", name));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return false;
            }
        }

        //if path not specified on the command line then get it from domain.xml
        path = (path==null)?getPathFromDomainXML(app):path;
        if (path == null) {
                //if unable to get path from domain.xml then return error.
            report.setMessage(localStrings.getLocalString("redeploy.command.invalid.path", "Cannot determine the path of application."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        return true;
    }
    

        /**
         * Retrieve application Path from domain.xml
         * Search from the application element in domain.xml
         * and return the location attribute.
         *
         * @param app - Application
         *
         * @returns the location.
         */
    String getPathFromDomainXML(final Application app) {
        if (app != null) {
            URI uri = null;
            try {
                uri = new URI(app.getLocation());
            }
            catch (URISyntaxException e) {
                return null;
            }
            return uri.getPath();
        }
        return null;
    }

}
