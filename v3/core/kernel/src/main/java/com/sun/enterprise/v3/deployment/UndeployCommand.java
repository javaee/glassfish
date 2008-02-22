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
import com.sun.enterprise.v3.server.V3Environment;
import com.sun.enterprise.v3.services.impl.GrizzlyService;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.logging.LogDomains;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.deployment.DeploymentContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;
import java.beans.PropertyVetoException;

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
    GrizzlyService adapter;

    @Param(primary = true, name=DeployCommand.NAME)
    String name=null;

    @Inject
    Applications applications;

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

        undeploy(name, deploymentContext, report);

        // check if it's directory deployment
        boolean isDirectoryDeployed = false;
        for (Module module : applications.getModules()) {
            if (module.getName().equals(name) 
                && module instanceof Application) {
                isDirectoryDeployed = Boolean.valueOf(
                    ((Application)module).getDirectoryDeployed());
            }
        }

        if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
            // so far I am doing this after the unload, maybe this should be moved before...
            try {
                ConfigSupport.apply(new SingleConfigCode<Applications>() {
                    public Object run(Applications apps) throws PropertyVetoException, TransactionFailure {
                        for (Module module : applications.getModules()) {
                            if (module.getName().equals(name) 
                                && module instanceof WebModule) {
                                apps.getModules().remove(module);
                                return module;
                            }
                        }
                        throw new TransactionFailure("Module not found in configuration", null);
                    }
                }, applications);

                // remove the "application" element
                unregisterAppFromDomainXML(name);

            } catch(TransactionFailure e) {
                logger.warning("Module " + name + " not found in configuration");
            }
            //remove context from generated
            deleteContainerMetaInfo(info, deploymentContext);
                //if directory deployment then do no remove the directory
            if (!isDirectoryDeployed) {
                FileUtils.whack(new File(info.getSource().getURI()));
            }
            report.setMessage(localStrings.getLocalString("undeploy.command.sucess",
                    "{0} undeployed successfully", name));
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } // else a message should have been provided.

    }
    
    private void deleteContainerMetaInfo(ApplicationInfo info, DeploymentContext context) {
        
        // need to remove the entry in the generated directories...
        // we need to remove generated/xml, generated/ejb, generated/jsp

        // remove generated/xml
        File generatedXmlRoot = new File(env.getApplicationStubPath(), "xml");
        generatedXmlRoot = new File(generatedXmlRoot, 
                context.getCommandParameters().getProperty(DeployCommand.NAME));
        // recursively delete...
        FileUtils.whack(generatedXmlRoot);               

        // remove generated/ejb
        File generatedEjbRoot = new File(env.getApplicationStubPath(), "ejb");
        generatedEjbRoot = new File(generatedEjbRoot, 
                context.getCommandParameters().getProperty(DeployCommand.NAME));
        // recursively delete...
        FileUtils.whack(generatedEjbRoot);               

        // remove generated/jsp
        File generatedJspRoot = new File(env.getApplicationStubPath(), "jsp");
        generatedJspRoot = new File(generatedJspRoot, 
                context.getCommandParameters().getProperty(DeployCommand.NAME));
        // recursively delete...
        FileUtils.whack(generatedJspRoot);               
    }
}
