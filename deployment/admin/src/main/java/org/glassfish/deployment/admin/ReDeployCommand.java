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

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.internal.deployment.Deployment;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.annotations.Scoped;
import java.util.Properties;
import java.io.File;

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

public class ReDeployCommand extends DeployCommandParameters implements AdminCommand {

    @Inject
    CommandRunner commandRunner;

    @Inject
    Deployment deployment;
    
    @Param(optional=false)
    String name;

    @Param(primary=true, optional=true)
    File path = null;

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
        if (!validateParameters(name, report)) {
            return;
        }
        force = true;

        CommandRunner.CommandInvocation inv = commandRunner.getCommandInvocation("deploy", report);
        inv.parameters(this).inbound(context.getInboundPayload()).outbound(context.getOutboundPayload()).execute();
    }

        /**
         * Validate the parameters, name and path.
         * Check if name is registered.  For redeployment, the
         * application must be previously deployed.
         * Verify that path is valid and not null.
         *
         * @param name - Application name
         * @param report - ActionReport.
         *
         * @returns true if validation successfully else return false.
         */
    boolean validateParameters(final String name, final ActionReport report) {
        if (!deployment.isRegistered(name)) {
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
            if (!Boolean.parseBoolean(ConfigBeansUtilities.getDirectoryDeployed(name))) {
                report.setMessage(localStrings.getLocalString("redeploy.command.cannot.redeploy","Cannot redeploy this app {0} without specify the operand.", name));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return false;
            }
        }

        //if path not specified on the command line then get it from domain.xml
        super.path = (path==null)?new File(ConfigBeansUtilities.getLocation(name)):path;
        if (super.path == null) {
                //if unable to get path from domain.xml then return error.
            report.setMessage(localStrings.getLocalString("redeploy.command.invalid.path", "Cannot determine the path of application."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        return true;
    }
}
