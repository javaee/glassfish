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

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.admin.CommandRunner;
import org.glassfish.internal.data.ApplicationInfo;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.v3.server.ServerEnvironment;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ServerTags;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ParameterNames;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Calendar;
import java.util.Collection;
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
    
    @Inject
    ServerEnvironment env;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    CommandRunner commandRunner;

    @Param(name = ParameterNames.NAME, optional=true)
    String name = null;

    @Param(name = ParameterNames.CONTEXT_ROOT, optional=true)
    String contextRoot = null;

    @Param(name = ParameterNames.VIRTUAL_SERVERS, optional=true)
    @I18n("virtualservers")
    String virtualservers = null;

    @Param(name=ParameterNames.LIBRARIES, optional=true)
    String libraries = null;

    @Param(optional=true, defaultValue="false")
    Boolean force;

    @Param(name=ParameterNames.PRECOMPILE_JSP, optional=true, defaultValue="false")
    Boolean precompilejsp;

    @Param(optional=true, defaultValue="false")
    Boolean verify;
    
    @Param(optional=true)
    String retrieve = null;
    
    @Param(optional=true)
    String dbvendorname = null;

    //mutually exclusive with dropandcreatetables
    @Param(optional=true)
    Boolean createtables;

    //mutually exclusive with createtables
    @Param(optional=true)
    Boolean dropandcreatetables;

    @Param(optional=true)
    Boolean uniquetablenames;

    @Param(optional=true)
    String deploymentplan = null;

    @Param(name=ParameterNames.ENABLED, optional=true, defaultValue="true")
    Boolean enabled;
    
    @Param(optional=true, defaultValue="false")
    Boolean generatermistubs;
    
    @Param(optional=true, defaultValue="false")
    Boolean availabilityenabled;
    
    @Param(optional=true)
    String target = "server";

    @Inject
    Domain domain;

    @Param(primary=true)
    File path;

    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    public void execute(AdminCommandContext context) {

        long operationStartTime = Calendar.getInstance().getTimeInMillis();

        final Properties parameters = context.getCommandParameters();
        final ActionReport report = context.getActionReport();

        File file = path;
        if (!file.exists()) {
            report.setMessage(localStrings.getLocalString("fnf","File not found", file.getAbsolutePath()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (snifferManager.hasNoSniffers()) {
            String msg = localStrings.getLocalString("nocontainer", "No container services registered, done...");
            report.failure(logger,msg);
            return;
        }

        ReadableArchive archive;
        try {
            archive = archiveFactory.openArchive(file);
        } catch (IOException e) {
            report.failure(logger,"Error opening deployable artifact : " + file.getAbsolutePath(),e);
            return;
        }
        File expansionDir=null;
        try {

            ArchiveHandler archiveHandler = getArchiveHandler(archive);
            if (archiveHandler==null) {
                report.failure(logger,localStrings.getLocalString("deploy.unknownarchivetype","Archive type of {0} was not recognized",file.getName()));
                return;
            }
            // get an application name
            if (name==null) {
                // Archive handlers know how to construct default app names.
                name = archiveHandler.getDefaultApplicationName(archive);
                // For the autodeployer in particular the name must be set in the
                // command context parameters for later use.
                parameters.put(ParameterNames.NAME, name);
            }
            
            handleRedeploy(name, report, parameters);

            // clean up any left over repository files
            FileUtils.whack(new File(env.getApplicationRepositoryPath(), name));
            parameters.put(ParameterNames.ENABLED, enabled.toString());

            File source = new File(archive.getURI().getSchemeSpecificPart());
            boolean isDirectoryDeployed = true;
            if (!source.isDirectory()) {
                isDirectoryDeployed = false;
                expansionDir = new File(domain.getApplicationRoot(), name);
                if (!expansionDir.mkdirs()) {
                    report.failure(logger,localStrings.getLocalString("deploy.cannotcreateexpansiondir", "Error while creating directory for jar expansion: {0}",expansionDir));
                    // we don't own it, we don't delete it.
                    expansionDir=null;
                    return;
                }
                try {
                    archiveHandler.expand(archive, archiveFactory.createArchive(expansionDir));
                    // Close the JAR archive before losing the reference to it or else the JAR remains locked.
                    try {
                        archive.close();
                    } catch(IOException e) {
                        report.failure(logger,localStrings.getLocalString("deploy.errorclosingarchive","Error while closing deployable artifact {0}", file.getAbsolutePath()),e);
                        return;
                    }
                    // Proceed using the expanded directory.
                    file = expansionDir;
                    archive = archiveFactory.openArchive(expansionDir);
                } catch(IOException e) {
                    report.failure(logger,localStrings.getLocalString("deploy.errorexpandingjar","Error while expanding archive file"),e);
                    return;

                }
            }

            // create the parent class loader
            ClassLoader parentCL = snifferManager.createSnifferParentCL(null);
            // now the archive class loader, this will only be used for the sniffers.handles() method
            final ClassLoader cloader = archiveHandler.getClassLoader(parentCL, archive);

            final Collection<Sniffer> appSniffers = snifferManager.getSniffers(archive, cloader);
            if (appSniffers.size()==0) {
                report.failure(logger,localStrings.getLocalString("deploy.unknownmoduletpe","Module type not recognized"));
                return;
            }

            final String docBase = archive.getURI().toURL().toString();
            final ReadableArchive sourceArchive = archive; 
            final DeploymentContextImpl deploymentContext = new DeploymentContextImpl(logger,
                    sourceArchive, parameters, env);
            deploymentContext.setClassLoader(cloader);

            // clean up any generated files
            deleteContainerMetaInfo(deploymentContext);

            Properties moduleProps = deploymentContext.getProps();
            moduleProps.setProperty(ServerTags.NAME, name);
            moduleProps.setProperty(ServerTags.LOCATION, deploymentContext.getSource().getURI().toURL().toString());
            // set to default "user", deployers can override it 
            // during processing
            moduleProps.setProperty(ServerTags.OBJECT_TYPE, "user");
            if (contextRoot!=null) {
                moduleProps.setProperty(ServerTags.CONTEXT_ROOT, contextRoot);
            }
            if (libraries!=null) {
                moduleProps.setProperty(ServerTags.LIBRARIES, libraries);
            }
            moduleProps.setProperty(ServerTags.ENABLED, enabled.toString());
            moduleProps.setProperty(ServerTags.DIRECTORY_DEPLOYED, String.valueOf(isDirectoryDeployed));
            if (virtualservers != null) {
                moduleProps.setProperty(ServerTags.VIRTUAL_SERVERS, 
                    virtualservers);
            } 

            ApplicationInfo appInfo = deploy(appSniffers, deploymentContext, report);
            if (report.getActionExitCode()==ActionReport.ExitCode.SUCCESS) {
                // register application information in domain.xml
                registerAppInDomainXML(appInfo, deploymentContext);

            }
        } catch(Exception e) {
            report.failure(logger,"Error during deployment : "+e.getMessage(),e);
        } finally {
            try {
                archive.close();
            } catch(IOException e) {
                logger.log(Level.INFO, "Error while closing deployable artifact : " + file.getAbsolutePath(), e);
            }
            if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
                logger.info("Deployment of " + name + " done is "
                        + (Calendar.getInstance().getTimeInMillis() - operationStartTime) + " ms");
            } else {
                if (expansionDir!=null) {
                   FileUtils.whack(expansionDir);
                }
            }
        }
    }

    /**
     *  Check if the application is deployed or not.
     *  If force option is true and appInfo is not null, then undeploy
     *  the application and return false.  This will force deployment
     *  if there's already a running application deployed.
     *
     *  @param name application name
     *  @param report ActionReport, report object to send back to client.
     *
     */
    private void handleRedeploy(final String name, final ActionReport report,
                                Properties parameters) 
        throws Exception {
        boolean isRegistered = isRegistered(name);
        if (isRegistered && !force) {
            String msg = localStrings.getLocalString(
                "application.alreadyreg.redeploy",
                "Application {0} already registered, please use deploy --force=true to redeploy", name);
            throw new Exception(msg);
        }
        else if (isRegistered && force) 
        {
            //preserve settings first before undeploy
            settingsFromDomainXML(parameters);
            //if applicaiton is already deployed and force=true,
            //then undeploy the application first.
            Properties undeployParam = new Properties();
            undeployParam.put(ParameterNames.NAME, name);
            ActionReport subReport = report.addSubActionsReport();
            commandRunner.doCommand("undeploy", undeployParam, subReport);
        }
    }

    
    /**
     *  Get settings from domain.xml and preserve the values.
     *  This is a private api and its invoked when --force=true and if the app is registered.
     *
     *  @param parameters 
     *
     */
    private void settingsFromDomainXML(Properties parameters) {
            //if name is null then cannot get the application's setting from domain.xml
        if (name != null) {
            if (contextRoot == null) {            
                contextRoot = ConfigBeansUtilities.getContextRoot(name);
                if (contextRoot != null) {
                    parameters.put(ParameterNames.CONTEXT_ROOT, contextRoot);
                }
            }
            if (libraries == null) {
                libraries = ConfigBeansUtilities.getLibraries(name);
                if (libraries != null) {
                    parameters.put(ParameterNames.LIBRARIES, libraries);
                }
            }
            if (virtualservers == null) {
                virtualservers = ConfigBeansUtilities.getVirtualServers(
                    target, name);
                if (virtualservers != null) {
                    parameters.put(ParameterNames.VIRTUAL_SERVERS, virtualservers);
                }
            }
        }
    }
}
