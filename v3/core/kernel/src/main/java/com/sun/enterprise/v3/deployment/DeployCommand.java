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
import com.sun.enterprise.v3.admin.CommandRunner;
import com.sun.enterprise.v3.contract.ApplicationMetaDataPersistence;
import com.sun.enterprise.v3.data.ApplicationInfo;
import com.sun.enterprise.v3.server.ApplicationLifecycle;
import com.sun.enterprise.v3.server.V3Environment;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.impl.ClassLoaderProxy;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.ServerTags;
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
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.beans.PropertyVetoException;


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
    public static final String CONTEXT_ROOT = "contextroot";
    public static final String LIBRARIES = "libraries";
    public static final String DIRECTORY_DEPLOYED = "DirectoryDeployed";
    public static final String LOCATION = "LOCATION";
    public static final String ENABLED = "ENABLED";
    
    @Inject
    V3Environment env;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    CommandRunner commandRunner;

    String path;

    @Param(name = NAME, optional=true)
    String name = null;

    @Param(name = CONTEXT_ROOT, optional=true)
    String contextRoot = null;

    @Param(optional=true)
    @I18n("virtualservers")
    String virtualservers = null;

    @Param(name=LIBRARIES, optional=true)
    String libraries = null;

    @Param(optional=true)
    String force = Boolean.FALSE.toString();

    @Param(optional=true)
    String precompilejsp = Boolean.FALSE.toString();

    @Param(optional=true)
    String verify = Boolean.FALSE.toString();
    
    @Param(optional=true)
    String retrieve = null;
    
    @Param(optional=true)
    String dbvendorname = null;

    //mutually exclusive with dropandcreatetables
    @Param(optional=true)
    String createtables = null;

    //mutually exclusive with createtables
    @Param(optional=true)
    String dropandcreatetables = null;

    @Param(optional=true)
    String uniquetablenames = null;

    @Param(optional=true)
    String deploymentplan = null;

    @Param(optional=true)
    String enabled = Boolean.TRUE.toString();
    
    @Param(optional=true)
    String generatermistubs = Boolean.FALSE.toString();
    
    @Param(optional=true)
    String availabilityenabled = Boolean.FALSE.toString();
    
    @Param(optional=true)
    String target = "server";

    @Inject
    Domain domain;

    @Inject
    Applications applications;

    @Param(primary=true, shortName = "p")
    public void setPath(String path) {
        this.path = path;
    }
    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    public void execute(AdminCommandContext context) {

        long operationStartTime = Calendar.getInstance().getTimeInMillis();

        final Properties parameters = context.getCommandParameters();
        final ActionReport report = context.getActionReport();

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

        ReadableArchive archive;
        try {
            archive = archiveFactory.openArchive(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error opening deployable artifact : " + file.getAbsolutePath(), e);
            report.setMessage(localStrings.getLocalString("deploy.unknownarchiveformat", "Archive format not recognized"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        File expansionDir=null;
        try {

            ArchiveHandler archiveHandler = getArchiveHandler(archive);
            if (archiveHandler==null) {
                report.setMessage(localStrings.getLocalString("deploy.unknownarchivetype","Archive type of {0} was not recognized",file.getName()));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
            // get an application name
            if (name==null) {
                // Archive handlers know how to construct default app names.
                name = archiveHandler.getDefaultApplicationName(archive);
                // For the autodeployer in particular the name must be set in the
                // command context parameters for later use.
                parameters.put(NAME, name);
            }
            
            if (checkIfAppIsRegistered(appRegistry.get(name), report)) {
                return;
            }

            File source = new File(archive.getURI().getSchemeSpecificPart());
            boolean isDirectoryDeployed = true;
            if (!source.isDirectory()) {
                isDirectoryDeployed = false;
                expansionDir = new File(domain.getApplicationRoot(), name);
                if (!expansionDir.mkdirs()) {
                    report.setMessage(localStrings.getLocalString("deploy.cannotcreateexpansiondir", "Error while creating directory for jar expansion: {0}",expansionDir));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
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
                        report.setMessage(localStrings.getLocalString("deploy.errorclosingarchive","Error while closing deployable artifact {0}", file.getAbsolutePath()));
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        return;
                    }
                    // Proceed using the expanded directory.
                    file = expansionDir;
                    archive = archiveFactory.openArchive(expansionDir);
                } catch(IOException e) {
                    report.setMessage(localStrings.getLocalString("deploy.errorexpandingjar","Error while expanding archive file"));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;

                }
            }

            // create the parent class loader
            ClassLoader parentCL = createSnifferParentCL(null, Arrays.asList(sniffers));
            // now the archive class loader, this will only be used for the sniffers.handles() method
            final ClassLoader cloader = archiveHandler.getClassLoader(parentCL, archive);

            final Collection<Sniffer> appSniffers = getSniffers(archive, cloader);
            if (appSniffers.size()==0) {
                report.setMessage(localStrings.getLocalString("deploy.unknownmoduletpe","Module type not recognized"));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            final String docBase = archive.getURI().getSchemeSpecificPart();
            final ReadableArchive sourceArchive = archive; 
            final DeploymentContextImpl deploymentContext = new DeploymentContextImpl(logger,
                    sourceArchive, parameters, env);
            deploymentContext.setClassLoader(cloader);

            Properties moduleProps = deploymentContext.getProps();
            moduleProps.setProperty(ServerTags.NAME, name);
            moduleProps.setProperty(ServerTags.LOCATION, deploymentContext.getSource().getURI().toString());
            if (contextRoot!=null) {
                moduleProps.setProperty(CONTEXT_ROOT, contextRoot);
            }

            moduleProps.setProperty(ServerTags.ENABLED, enabled);
            moduleProps.setProperty(ServerTags.DIRECTORY_DEPLOYED, String.valueOf(isDirectoryDeployed));


            // let's add our configuration data. so far it's an horrible hack where I always create
            // a WebModule instance, soon we should have a generic config object
            final List<ApplicationInfo> appInfos = 
                new ArrayList<ApplicationInfo>();
            com.sun.enterprise.config.serverbeans.WebModule wm = (com.sun.enterprise.config.serverbeans.WebModule)
                ConfigSupport.apply(new SingleConfigCode<Applications>() {
                    /**
                     * Runs the following command passing the configration object. The code will be run
                     * within a transaction, returning true will commit the transaction, false will abort
                     * it.
                     *
                     * @param param is the configuration object protected by the transaction
                     * @return true if the changes on param should be commited or false for abort.
                     * @throws java.beans.PropertyVetoException
                     *          if the changes cannot be applied
                     *          to the configuration
                     */
                    public Object run(Applications param) throws PropertyVetoException, TransactionFailure {
                        com.sun.enterprise.config.serverbeans.WebModule wm = ConfigSupport.createChildOf(param, com.sun.enterprise.config.serverbeans.WebModule.class);

                        applications.getModules().add(wm);
                        wm.setName(name);
                        wm.setLocation(docBase);


                        // another horrible hack because Webcontainer wants a WebModule before the deployment code runs,
                        // this will need tobe changed asap.                        
                        deploymentContext.setConfig(wm);

                        ApplicationInfo appInfo = load(appSniffers, deploymentContext, report);
                        appInfos.add(appInfo);
                        return wm;
                    }
                }, applications);
            

            if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
                report.setMessage(localStrings.getLocalString("deploy.command.success",
                        "Application {0} deployed successfully", name));
                
                /*
                 * The caller may want to know the resulting module ID as
                 * assigned by the server - if the caller did not specify it
                 * on the deploy command in the first place.
                 */
                ActionReport.MessagePart msgPart = report.getTopMessagePart();
                msgPart.addProperty(NAME, name);
                
                // register application information in domain.xml
                registerAppInDomainXML(appInfos.get(0), deploymentContext);
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Error during deployment : ", e);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(e.getMessage());
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
     *  @param appInfo ApplicationInfo, contains information of a
     *                 running application.  If this object is null,
     *                 then the application is not deployed.
     *  @param report ActionReport, report object to send back to client.
     *
     *  @return true if application is deployed else return false.
     */
    boolean checkIfAppIsRegistered(final ApplicationInfo appInfo,
                                   final ActionReport report)
    {
        try {
            boolean isForce = Boolean.parseBoolean(force);
            if (appInfo!=null && !isForce) {
                report.setMessage(localStrings.getLocalString("application.alreadyreg",
                                  "Application {0} already registered", name));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return true;
            }
            else if (appInfo != null && isForce) 
            {
                //if applicaiton is already deployed and force=true,
                //then undeploy the application first.
                Properties undeployParam = new Properties();
                undeployParam.put(NAME, name);
                commandRunner.doCommand("undeploy", undeployParam, report);
            }
        } catch(ComponentException e) {
            report.setMessage(e.getMessage());
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return true;
        }
        return false;
    }
    
}
