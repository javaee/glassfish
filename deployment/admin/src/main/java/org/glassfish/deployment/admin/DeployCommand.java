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

import org.glassfish.admin.payload.PayloadFilesManager;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.ApplicationConfigInfo;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.internal.deployment.SnifferManager;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.Habitat;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.deployment.common.DownloadableArtifacts;


/**
 * Deploy command
 *
 * @author Jerome Dochez
 */
@Service(name="deploy")
@I18n("deploy.command")
@Scoped(PerLookup.class)
public class DeployCommand extends DeployCommandParameters implements AdminCommand, EventListener {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeployCommand.class);

    private static final String INSTANCE_ROOT_URI_PROPERTY_NAME = "com.sun.aas.instanceRootURI";

    @Inject
    Applications apps;

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    Habitat habitat;

    @Inject
    CommandRunner commandRunner;

    @Inject
    Deployment deployment;

    @Inject
    SnifferManager snifferManager;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    Domain domain;

    @Inject
    DownloadableArtifacts downloadableArtifacts;

    @Inject
    Events events;

    private PayloadFilesManager.Temp payloadFilesMgr = null;
    private List<File> payloadFiles = null;

    public DeployCommand() {
        origin = Origin.deploy;
    }

    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    public void execute(AdminCommandContext context) {

      events.register(this);

      try {
        long operationStartTime = Calendar.getInstance().getTimeInMillis();

        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        try {
            payloadFilesMgr = new PayloadFilesManager.Temp(
                    context.getActionReport(),
                    logger);
            payloadFiles = payloadFilesMgr.extractFiles(context.getInboundPayload());
        } catch (Exception e) {
            report.setFailureCause(e);
            report.failure(logger, localStrings.getLocalString(
                    "adapter.command.errorPrepUploadedFiles", 
                    "Error preparing uploaded files"), e);
            return;
        }

        File file = choosePathFile(context);
        if (!file.exists()) {
            report.setMessage(localStrings.getLocalString("fnf","File not found", file.getAbsolutePath()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        deploymentplan = chooseDeploymentPlanFile(context);
        
        if (snifferManager.hasNoSniffers()) {
            String msg = localStrings.getLocalString("nocontainer", "No container services registered, done...");
            report.failure(logger,msg);
            return;
        }

        ReadableArchive archive;
        try {
            archive = archiveFactory.openArchive(file, this);
        } catch (IOException e) {
            final String msg = localStrings.getLocalString("deploy.errOpeningArtifact",
                    "deploy.errOpeningArtifact", file.getAbsolutePath());
            if (logReportedErrors) {
                report.failure(logger, msg, e);
            } else {
                report.setMessage(msg + file.getAbsolutePath() + e.toString());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            }
            return;
        }
        File expansionDir=null;
        try {

            ArchiveHandler archiveHandler = deployment.getArchiveHandler(archive);
            if (archiveHandler==null) {
                report.failure(logger,localStrings.getLocalString("deploy.unknownarchivetype","Archive type of {0} was not recognized",file.getName()));
                return;
            }

            // create an initial  context
            ExtendedDeploymentContext initialContext = new DeploymentContextImpl(report, logger, archive, this, env);

            if (name==null) {
                name = archiveHandler.getDefaultApplicationName(archive, initialContext);
            }

            ActionReport.MessagePart part = report.getTopMessagePart();
            part.addProperty("name", name);

            ApplicationConfigInfo savedAppConfig = 
                    new ApplicationConfigInfo(apps.getModule(Application.class, name));
            Properties undeployProps = handleRedeploy(name, report);

            // clean up any left over repository files
            if ( ! keepreposdir.booleanValue()) {
                FileUtils.whack(new File(env.getApplicationRepositoryPath(), name));
            }

            File source = new File(archive.getURI().getSchemeSpecificPart());
            boolean isDirectoryDeployed = true;
            if (!source.isDirectory()) {
                isDirectoryDeployed = false;
                expansionDir = new File(domain.getApplicationRoot(), name);
                file = expansionDir;
            }

            // create the parent class loader
            final ExtendedDeploymentContext deploymentContext =
                    deployment.getBuilder(logger, this, report).
                            source(archive).archiveHandler(archiveHandler).build(initialContext);

            // reset the properties (might be null) set by the deployers when undeploying.
            if (undeployProps!=null) {
                deploymentContext.getAppProps().putAll(undeployProps);
            }

            if (properties!=null) {
                deploymentContext.getAppProps().putAll(properties);
            }

            if (property!=null) {
                deploymentContext.getAppProps().putAll(property);
            }

            // clean up any generated files
            deploymentContext.clean();

            Properties appProps = deploymentContext.getAppProps();
            /*
             * If the app's location is within the domain's directory then
             * express it in the config as ${com.sun.aas.instanceRootURI}/rest-of-path
             * so users can relocate the entire installation without having
             * to modify the app locations.  Leave the location alone if
             * it does not fall within the domain directory.
             */
            URI instanceRootURI = new URI(System.getProperty(INSTANCE_ROOT_URI_PROPERTY_NAME));
            URI appURI = instanceRootURI.relativize(deploymentContext.getSource().getURI());
            String appLocation = (appURI.isAbsolute()) ?
                appURI.toString() :
                "${" + INSTANCE_ROOT_URI_PROPERTY_NAME + "}/" + appURI.toString();
            appProps.setProperty(ServerTags.LOCATION, appLocation);
            // set to default "user", deployers can override it
            // during processing
            appProps.setProperty(ServerTags.OBJECT_TYPE, "user");
            if (contextroot!=null) {
                appProps.setProperty(ServerTags.CONTEXT_ROOT, contextroot);
            }
            appProps.setProperty(ServerTags.DIRECTORY_DEPLOYED, String.valueOf(isDirectoryDeployed));

            savedAppConfig.store(appProps);

            ApplicationInfo appInfo;
            if (type==null) {
                appInfo = deployment.deploy(deploymentContext);
            } else {
                StringTokenizer st = new StringTokenizer(type);
                List<Sniffer> sniffers = new ArrayList<Sniffer>();
                while (st.hasMoreTokens()) {
                    String aType = st.nextToken();
                    Sniffer sniffer = snifferManager.getSniffer(aType);
                    if (sniffer==null) {
                        report.failure(logger, localStrings.getLocalString("deploy.unknowncontainer",
                                "{0} is not a recognized container ", new String[] { aType }));
                        return;
                    }
                    if (!snifferManager.canBeIsolated(sniffer)) {
                        report.failure(logger, localStrings.getLocalString("deploy.isolationerror",
                                 "container {0} does not support other components containers to be turned off, --type {0} is forbidden",
                                new String[] { aType }));
                        return;
                    }
                    sniffers.add(sniffer);
                }
                appInfo = deployment.deploy(sniffers, deploymentContext);
            }
            
            if (report.getActionExitCode()==ActionReport.ExitCode.SUCCESS) {
                // register application information in domain.xml
                deployment.registerAppInDomainXML(appInfo, deploymentContext);

            }
            if(retrieve != null) {
                retrieveArtifacts(context, name, retrieve, downloadableArtifacts,
                        false);
            }
        } catch(Throwable e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            report.failure(logger,localStrings.getLocalString(
                    "errDuringDepl", 
                    "Error during deployment : ") + e.getMessage(),null);
        } finally {
            try {
                archive.close();
            } catch(IOException e) {
                logger.log(Level.INFO, localStrings.getLocalString(
                        "errClosingArtifact", 
                        "Error while closing deployable artifact : ",
                        file.getAbsolutePath()), e);
            }
            if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
                if (report.hasWarnings()) {
                    report.setMessage(localStrings.getLocalString("deploy.command.successwithwarning","Application deployed successfully with name {0} and with the following warning(s):", name));
                } else {
                    report.setMessage(localStrings.getLocalString("deploy.command.success","Application deployed successfully with name {0}", name));
                }

                logger.info(localStrings.getLocalString(
                        "deploy.done", 
                        "Deployment of {0} done is {1} ms",
                        name,
                        (Calendar.getInstance().getTimeInMillis() - operationStartTime)));
            } else {
                if (expansionDir!=null) {
                   FileUtils.whack(expansionDir);
                }
            }
        }
      } finally {
          events.unregister(this);

          if (payloadFilesMgr != null) {
              payloadFilesMgr.cleanup();
          }
      }
    }

    private File choosePathFile(AdminCommandContext context) {
        if (payloadFiles.size() >= 1) {
            /*
             * Use the uploaded file rather than the one specified by --path.
             */
            return payloadFiles.get(0);
        }
        return path;
    }

    private File chooseDeploymentPlanFile(AdminCommandContext context) {
        if (payloadFiles.size() >= 2) {
            /*
             * Use the uploaded file rather than the one specified by
             * --deploymentplan.
             */
            return payloadFiles.get(1);
        }
        return deploymentplan;
    }

    /**
     *  Check if the application is deployed or not.
     *  If force option is true and appInfo is not null, then undeploy
     *  the application and return false.  This will force deployment
     *  if there's already a running application deployed.
     *
     *  @param name application name
     *  @param report ActionReport, report object to send back to client.
     * @return context properties that might have been set by the deployers
     * while undeploying the application
     *
     */
    private Properties handleRedeploy(final String name, final ActionReport report)
        throws Exception {
        boolean isRegistered = deployment.isRegistered(name);
        if (isRegistered && !force) {
            String msg = localStrings.getLocalString(
                "application.alreadyreg.redeploy",
                "Application with name {0} is already registered. Either specify that redeployment must be forced, or redeploy the application. Or if this is a new deployment, pick a different name.", name);
            throw new Exception(msg);
        }
        else if (isRegistered && force) 
        {
            //preserve settings first before undeploy
            Application app = apps.getModule(Application.class, name);

            // we save some of the old registration information in our deployment parameters
            settingsFromDomainXML(app);

            //if application is already deployed and force=true,
            //then undeploy the application first.
            UndeployCommandParameters undeployParams = new UndeployCommandParameters(name);
            undeployParams.keepreposdir = keepreposdir;
            undeployParams.droptables = dropandcreatetables;
            undeployParams.ignoreCascade = force;

            ActionReport subReport = report.addSubActionsReport();
            if (properties!=null && properties.containsKey(DeploymentProperties.KEEP_SESSIONS)) {
                undeployParams.properties = new Properties();
                undeployParams.properties.put(DeploymentProperties.KEEP_SESSIONS, properties.getProperty(DeploymentProperties.KEEP_SESSIONS));
                subReport.setExtraProperties(new Properties());
            } else if (property!=null && property.containsKey(DeploymentProperties.KEEP_SESSIONS)) {
                undeployParams.properties = new Properties();
                undeployParams.properties.put(DeploymentProperties.KEEP_SESSIONS, property.getProperty(DeploymentProperties.KEEP_SESSIONS));
                subReport.setExtraProperties(new Properties());
            }

            CommandRunner.CommandInvocation inv = commandRunner.getCommandInvocation("undeploy", subReport);
            inv.parameters(undeployParams).execute();
            return subReport.getExtraProperties();
        }
        return null;
    }

    public static void retrieveArtifacts(final AdminCommandContext context,
            final String appName,
            final String targetLocalDir,
            final DownloadableArtifacts downloadableArtifacts) {
        retrieveArtifacts(context, appName, targetLocalDir, downloadableArtifacts, true);
    }

    public static void retrieveArtifacts(final AdminCommandContext context,
            final String appName,
            final String targetLocalDir,
            final DownloadableArtifacts downloadableArtifacts,
            final boolean reportErrorsInTopReport) {
        Logger logger = context.getLogger();
        try {
            Payload.Outbound outboundPayload = context.getOutboundPayload();
            Properties props = new Properties();
            props.setProperty("file-xfer-root", targetLocalDir);
            for (DownloadableArtifacts.FullAndPartURIs uriPair : downloadableArtifacts.getArtifacts(appName)) {
                if(logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "About to download artifact " + uriPair.getFull());
                }
                outboundPayload.attachFile("application/octet-stream",
                        uriPair.getPart(),"files",props,
                        new File(uriPair.getFull().getSchemeSpecificPart()));
            }
        } catch (Exception e) {
            final String errorMsg = localStrings.getLocalString(
                    "download.errDownloading", "Error while downloading generated files");
            logger.log(Level.SEVERE, errorMsg, e);
            ActionReport report = context.getActionReport();
            if ( ! reportErrorsInTopReport) {
                report = report.addSubActionsReport();
                report.setActionExitCode(ExitCode.WARNING);
            } else {
                report.setActionExitCode(ExitCode.FAILURE);
            }
            report.setMessage(errorMsg);
            report.setFailureCause(e);
        }
    }

    
    /**
     *  Get settings from domain.xml and preserve the values.
     *  This is a private api and its invoked when --force=true and if the app is registered.
     *
     *  @param app is the registration information about the previously deployed application
     *
     */
    private void settingsFromDomainXML(Application app) {
            //if name is null then cannot get the application's setting from domain.xml
        if (name != null) {
            if (contextroot == null) {            
                if (app.getContextRoot() != null) {
                    this.previousContextRoot = app.getContextRoot();
                }
            }
            if (libraries == null) {
                libraries = app.getLibraries();
            }
            if (virtualservers == null) {
                virtualservers = ConfigBeansUtilities.getVirtualServers(
                    target, name);
            }
            String compatProp = app.getDeployProperties().getProperty(
                DeploymentProperties.COMPATIBILITY);
            if (compatProp != null) {
                if (properties == null) {
                    properties = new Properties();
                }
                // if user does not specify the compatibility flag 
                // explictly in this deployment, set it to the old value
                if (properties.getProperty(DeploymentProperties.COMPATIBILITY) == null) {
                    properties.setProperty(DeploymentProperties.COMPATIBILITY, compatProp);
                }
            }
        }
    }

    public void event(Event event) {
        if (event.is(Deployment.APPLICATION_PREPARED)) {
            DeploymentContext context = (DeploymentContext)event.hook();
            if (verify) {
                if (!isVerifierInstalled()) {
                    context.getLogger().warning("Verifier is not installed yet. Install verifier module.");
                } else {
                    invokeVerifier(context);
                }
            }
        }
    }

    private void invokeVerifier(DeploymentContext context) 
        throws DeploymentException {
        com.sun.enterprise.tools.verifier.Verifier verifier = habitat.getComponent(com.sun.enterprise.tools.verifier.Verifier.class);
        com.sun.enterprise.tools.verifier.VerifierFrameworkContext verifierFrameworkContext = new com.sun.enterprise.tools.verifier.VerifierFrameworkContext();
        verifierFrameworkContext.setArchive(context.getSource());
        verifierFrameworkContext.setApplication(context.getModuleMetaData(com.sun.enterprise.deployment.Application.class));
        verifierFrameworkContext.setJarFileName(context.getSourceDir().getAbsolutePath());
        verifierFrameworkContext.setJspOutDir(context.getScratchDir("jsp"));
        //verifierFrameworkContext.setIsBackend(true);
        verifierFrameworkContext.setOutputDirName(env.getDomainRoot().getAbsolutePath()+"/logs/verifier-results");
        com.sun.enterprise.tools.verifier.ResultManager rm = verifierFrameworkContext.getResultManager();

        try { 
            verifier.init(verifierFrameworkContext);
            verifier.verify();
        } catch (Exception e) {
            LogRecord logRecord = new LogRecord(Level.SEVERE,
                                "Could not verify successfully.");
            logRecord.setThrown(e);
            verifierFrameworkContext.getResultManager().log(logRecord);
        }  
       
        try {
            verifier.generateReports();
        } catch (IOException ioe) {
            context.getLogger().warning(
                "Can not generate verifier report: " + ioe.getMessage());
        }
        int failedCount = rm.getFailedCount() + rm.getErrorCount();
        if (failedCount != 0) {
            ((ExtendedDeploymentContext)context).clean();
            throw new DeploymentException(localStrings.getLocalString("deploy.failverifier","Some verifier tests failed. Aborting deployment"));
        }
    }

    private boolean isVerifierInstalled() {
        try {
            Class.forName("com.sun.enterprise.tools.verifier.Verifier");
            return true;
        } catch (ClassNotFoundException cnfe) {
            Logger.getAnonymousLogger().log(Level.FINE, 
                "Verifier class not found: ", cnfe); 
            return false;
        }
    }
}
