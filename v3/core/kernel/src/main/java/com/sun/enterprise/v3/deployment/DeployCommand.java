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

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.contract.ApplicationMetaDataPersistence;
import com.sun.enterprise.v3.data.ApplicationInfo;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import com.sun.enterprise.v3.server.V3Environment;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.PerLookup;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;


/**
 * Deploy command
 *
 * @author Jerome Dochez
 */
@Service(name="deploy")
@I18n("deploy.command")
@Scoped(PerLookup.class)
public class DeployCommand extends ApplicationLifecycle implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeployCommand.class);    
    public static final String NAME = "name";
    public static final String VIRTUAL_SERVERS = "virtualservers";

    @Inject
    V3Environment env;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    ApplicationMetaDataPersistence metaData;

    String path;

    @Param(name = NAME, optional=true)
    String name = null;

    @Param(optional=true)
    @I18n("virtualservers")
    String virtualservers = null;


    @Param(primary=true)
    public void setPath(String path) {
        this.path = path;
    }
    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    public void execute(AdminCommandContext context) {

        
        Properties parameters = context.getCommandParameters();
        ActionReport report = context.getActionReport();

        File file = new File(path);
        if (!file.exists()) {
            report.setMessage(localStrings.getLocalString("fnf","File not found", file.getAbsolutePath()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        
        if (sniffers==null) {
            String msg = localStrings.getLocalString("deploy.nocontainer", "No container services registered, done...");
            logger.severe(msg);
            report.setMessage(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }        

        ReadableArchive archive = null;
        try {
            archive = archiveFactory.openArchive(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error opening deployable artifact : " + file.getAbsolutePath(), e);
            report.setMessage(localStrings.getLocalString("deploy.unknownarchiveformat", "Archive format not recognized"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        // get an application name
        if (name==null) {
            if (file.getName().lastIndexOf(".")!=-1) {
                name = file.getName().substring(0, file.getName().lastIndexOf("."));
            } else {
                name = file.getName();
            }
            parameters.put(NAME, name);
        }

        // check this application is not already registered.
        try {
            ApplicationInfo appInfo = habitat.getComponent(ApplicationInfo.class, name);
            if (appInfo!=null) {
                report.setMessage(localStrings.getLocalString("application.alreadyreg",
                    "Application {0} already registered", name));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

        } catch(ComponentException e) {
            report.setMessage(e.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;

        }

        File expansionDir=null;
        try {

            ArchiveHandler archiveHandler = getArchiveHandler(archive);
            if (archiveHandler==null) {
                report.setMessage(localStrings.getLocalString("deploy.unknownarchivetype","Archive type not recognized"));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            File source = new File(archive.getURI().getSchemeSpecificPart());
            if (!source.isDirectory()) {
                expansionDir = new File(env.getApplicationRepositoryPath(), name);
                if (!expansionDir.mkdirs()) {
                    report.setMessage(localStrings.getLocalString("deploy.cannotcreateexpansiondir", "Error while creating directory for jar expansion: {0}",expansionDir));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    // we don't own it, we don't delete it.
                    expansionDir=null;
                    return;
                }
                try {
                    archiveHandler.expand(archive, archiveFactory.createArchive(expansionDir));
                    archive = archiveFactory.openArchive(expansionDir);
                } catch(IOException e) {
                    report.setMessage(localStrings.getLocalString("deploy.errorexpandingjar","Error while expanding archive file"));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                    
                }
            }

            ClassLoader cloader = archiveHandler.getClassLoader(null ,archive);

            Collection<Sniffer> appSniffers = getSniffers(archive, cloader);
            if (appSniffers.size()==0) {
                report.setMessage(localStrings.getLocalString("deploy.unknownmoduletpe","Module type not recognized"));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            DeploymentContextImpl deploymentContext = new DeploymentContextImpl(logger,
                    archive, parameters, env);

            deploymentContext.setClassLoader(cloader);

            ApplicationInfo appInfo = load(appSniffers, deploymentContext, report);
            if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
                report.setMessage(localStrings.getLocalString("deploy.command.success",
                        "Application {0} deployed successfully", name));

                Properties moduleProps = deploymentContext.getProps();
                moduleProps.setProperty("Name", name);
                StringBuffer sb = new StringBuffer();
                for (Sniffer sniffer : appSniffers) {
                    sb.append(sniffer.getModuleType());
                    sb.append(" ");
                }
                moduleProps.setProperty("Type", sb.toString());
                moduleProps.setProperty("Source", deploymentContext.getSource().getURI().toString());

                metaData.save(name, moduleProps);
            }
        } catch(Exception e) {
            if (expansionDir!=null) {
               FileUtils.whack(expansionDir);
            }            
        } finally {
            try {
                archive.close();
            } catch(IOException e) {
                logger.log(Level.INFO, "Error while closing deployable artifact : " + file.getAbsolutePath(), e);
            }
        }

    }        
}
