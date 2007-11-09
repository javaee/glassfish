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
import com.sun.enterprise.v3.data.ApplicationInfo;
import com.sun.enterprise.v3.data.ApplicationRegistry;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import com.sun.enterprise.v3.server.V3Environment;
import com.sun.enterprise.v3.services.impl.GrizzlyAdapter;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.deployment.DeploymentContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;

import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;

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
    V3Environment env;

    @Inject
    ApplicationRegistry appRegistry;

    @Inject
    GrizzlyAdapter adapter;

    @Param(primary = true, name=DeployCommand.NAME)
    String name=null;

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
        if (info==null) {
            report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", name));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE); 
            return;
 
        }

        DeploymentContextImpl deploymentContext = new DeploymentContextImpl(logger, info.getSource(), parameters, env);        

        unload(name, deploymentContext, report);
        if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
            deleteContainerMetaInfo(info, deploymentContext);
            report.setMessage(localStrings.getLocalString("redeploy.command.sucess",
                    "{0} undeployed successfully", name));
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } // else a message should have been provided.

    }
    
    private void deleteContainerMetaInfo(ApplicationInfo info, DeploymentContext context) {
        
        // need to remove the entry in the generated directory...
        File generatedAppRoot = new File(env.getApplicationStubPath());
        generatedAppRoot = new File(generatedAppRoot, 
                context.getCommandParameters().getProperty(DeployCommand.NAME));

        // recursively delete...
        deleteDirectory(generatedAppRoot);
               
    }
    
    /**
     * delete all content of a given directory
     * @param dir the directory to wipe out
     */
    public static void deleteDirectory(File dir) {
	File[] files = dir.listFiles();
	if (files != null) {
        for (File child : files) {
    		if(child.isDirectory()) {
	    	    deleteDirectory(child);
    		}
	    	child.delete();
	    }
	}
	dir.delete();
    }    
}
