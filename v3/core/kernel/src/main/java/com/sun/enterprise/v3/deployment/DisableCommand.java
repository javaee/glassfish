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

import com.sun.enterprise.v3.server.ServerEnvironmentImpl;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

import java.util.logging.Level;

/**
 * Disable command
 */
@Service(name="disable")
@I18n("disable.command")
@Scoped(PerLookup.class)
    
public class DisableCommand extends ApplicationLifecycle implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DisableCommand.class);    

    @Inject
    ServerEnvironmentImpl env;

    @Param(primary=true, name="component")
    String component = null;

    @Param(optional=true)    
    String target = "server";

    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        
        if (!isRegistered(component)) {
            report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", component));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        // return if the application is already in disabled state
        if (!Boolean.valueOf(ConfigBeansUtilities.getEnabled(target,
            component))) {
            logger.fine("The application is already disabled");
            return;
        }

        try {
            disable(component, report);

            if (report.getActionExitCode().equals(
                ActionReport.ExitCode.SUCCESS)) {
                setEnableAttributeInDomainXML(component, false);
            }

        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error during disabling: ", e);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(e.getMessage());
        }
    }        
}
