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

import com.sun.enterprise.v3.server.ApplicationLifecycle;
import com.sun.enterprise.v3.server.V3Environment;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import com.sun.enterprise.v3.admin.CommandRunner;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import java.util.Properties;


/**
 *
 * ReDeploy command
 *
 * @author Jerome Dochez
 * 
 */
@Service(name="redeploy")
@I18n("redeploy.command")

public class ReDeployCommand extends ApplicationLifecycle implements AdminCommand {

    @Inject
    CommandRunner commandRunner;
    @Param(primary=true)
    String name;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ReDeployCommand.class);
    
        
    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        Properties deployParam = new Properties();
        deployParam.put("force", Boolean.TRUE.toString());
        deployParam.put("path", name);        
        commandRunner.doCommand("deploy", deployParam, report);
        if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
            report.setMessage(localStrings.getLocalString("redeploy.command.sucess",
                    "{0} redeployed successfully", name));
        } // else a message should have been provided for the failure. don't overwrite.
    }

}
