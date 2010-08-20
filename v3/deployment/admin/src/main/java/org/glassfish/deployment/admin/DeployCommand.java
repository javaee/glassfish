/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import java.net.URISyntaxException;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.Cluster;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.ApplicationConfigInfo;
import org.glassfish.deployment.common.Artifacts;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.internal.deployment.SnifferManager;
import org.glassfish.config.support.TargetType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.deployment.common.DeploymentUtils;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.Transaction;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.deployment.common.VersioningDeploymentSyntaxException;
import org.glassfish.deployment.common.VersioningDeploymentUtil;

import org.glassfish.deployment.versioning.VersioningService;


/**
 * Deploy command
 *
 * @author Jerome Dochez
 */
@Service(name="deploy")
@I18n("deploy.command")
@Scoped(PerLookup.class)
@Cluster(value={RuntimeType.DAS})
@TargetType(value={CommandTarget.DOMAIN, CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
public class DeployCommand extends DeployCommandParameters implements AdminCommand, EventListener {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeployCommand.class);
    final private static String COPY_IN_PLACE_ARCHIVE_PROP_NAME = "copy.inplace.archive";
    
    @Inject
    Applications apps;

    @Inject
    ServerEnvironment env;

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
    Events events;

    @Inject
    VersioningService versioningService;

    private File safeCopyOfApp = null;
    private File safeCopyOfDeploymentPlan = null;
    private File originalPathValue;
    private boolean isRedeploy = false;
    private List<String> previousTargets = new ArrayList<String>();

    public DeployCommand() {
        origin = Origin.deploy;
    }

    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    @Override
    public void execute(AdminCommandContext context) {

      events.register(this);

      final DeployCommandSupplementalInfo suppInfo =
              new DeployCommandSupplementalInfo();
      context.getActionReport().
              setResultType(DeployCommandSupplementalInfo.class, suppInfo);

      try {
        long operationStartTime = Calendar.getInstance().getTimeInMillis();

        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        originalPathValue = path;
        if (!path.exists()) {
            report.setMessage(localStrings.getLocalString("fnf","File not found", path.getAbsolutePath()));
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
            archive = archiveFactory.openArchive(path, this);
        } catch (IOException e) {
            final String msg = localStrings.getLocalString("deploy.errOpeningArtifact",
                    "deploy.errOpeningArtifact", path.getAbsolutePath());
            if (logReportedErrors) {
                report.failure(logger, msg, e);
            } else {
                report.setMessage(msg + path.getAbsolutePath() + e.toString());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            }
            return;
        }
        File expansionDir=null;
        try {

            ArchiveHandler archiveHandler = deployment.getArchiveHandler(archive);
            if (archiveHandler==null) {
                report.failure(logger,localStrings.getLocalString("deploy.unknownarchivetype","Archive type of {0} was not recognized",path.getName()));
                return;
            }

            // create an initial  context
            ExtendedDeploymentContext initialContext = new DeploymentContextImpl(report, logger, archive, this, env);

            if (name==null) {
                name = archiveHandler.getDefaultApplicationName(archive, initialContext);
            } else {
                DeploymentUtils.validateApplicationName(name);
            }

            boolean isUntagged = VersioningDeploymentUtil.isUntagged(name);
            // no GlassFish versioning support for OSGi budles
            if ( name != null && !isUntagged && type != null && type.equals("osgi") ) {
                ActionReport.MessagePart msgPart = context.getActionReport().getTopMessagePart();
                msgPart.setChildrenType("WARNING");
                ActionReport.MessagePart childPart = msgPart.addChild();
                childPart.setMessage(VersioningDeploymentUtil.LOCALSTRINGS.getLocalString(
                        "versioning.deployment.osgi.warning",
                        "OSGi bundles will not use the GlassFish versioning, any version information embedded as part of the name option will be ignored"));
                name = VersioningDeploymentUtil.getUntaggedName(name);
            }

            // if no version information embedded as part of application name
            // we try to retrieve the version-identifier element's value from DD
            if ( isUntagged ){
                String versionIdentifier = archiveHandler.getVersionIdentifier(archive);

                if ( versionIdentifier != null && !versionIdentifier.isEmpty() ) {
                  StringBuilder sb = new StringBuilder(name).
                          append(VersioningDeploymentUtil.EXPRESSION_SEPARATOR).
                          append(versionIdentifier);
                  name = sb.toString();
                }
            }
            
            if (!DeploymentUtils.isDomainTarget(target) && enabled) {
                // try to disable the enabled version, if exist
                try {
                    versioningService.handleDisable(name,target, report);
                } catch (VersioningDeploymentSyntaxException e) {
                    report.failure(logger, e.getMessage());
                    return;
                }
            } 

            boolean isRegistered = deployment.isRegistered(name);
            isRedeploy = isRegistered && force;
            deployment.validateDeploymentTarget(target, name, isRedeploy);

            ActionReport.MessagePart part = report.getTopMessagePart();
            part.addProperty(DeploymentProperties.NAME, name);

            ApplicationConfigInfo savedAppConfig = 
                    new ApplicationConfigInfo(apps.getModule(Application.class, name));
            Properties undeployProps = handleRedeploy(name, report);

            // clean up any left over repository files
            if ( ! keepreposdir.booleanValue()) {
                FileUtils.whack(new File(env.getApplicationRepositoryPath(), VersioningDeploymentUtil.getRepositoryName(name)));
            }

            File source = new File(archive.getURI().getSchemeSpecificPart());
            boolean isDirectoryDeployed = true;
            if (!source.isDirectory()) {
                isDirectoryDeployed = false;
                expansionDir = new File(domain.getApplicationRoot(), name);
                path = expansionDir;
            } else {
                // test if a version is already directory deployed from this dir
                String versionFromSameDir =
                        versioningService.getVersionFromSameDir(source);
                if (!force && versionFromSameDir != null) {
                    report.failure(logger,
                            VersioningDeploymentUtil.LOCALSTRINGS.getLocalString(
                                "versioning.deployment.dual.inplace",
                                "GlassFish do not support versioning for directory deployment when using the same directory. The directory {0} is already assigned to the version {1}.",
                                source.getPath(),
                                versionFromSameDir));
                    return;
                }
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
            String appLocation = DeploymentUtils.relativizeWithinDomainIfPossible( deploymentContext.getSource().getURI());
            
            appProps.setProperty(ServerTags.LOCATION, appLocation);
            // set to default "user", deployers can override it
            // during processing
            appProps.setProperty(ServerTags.OBJECT_TYPE, "user");
            if (contextroot!=null) {
                appProps.setProperty(ServerTags.CONTEXT_ROOT, contextroot);
            }
            appProps.setProperty(ServerTags.DIRECTORY_DEPLOYED, String.valueOf(isDirectoryDeployed));

            savedAppConfig.store(appProps);

            deploymentContext.addTransientAppMetaData("previousTargets", previousTargets);

            Transaction t = deployment.prepareAppConfigChanges(deploymentContext);

            ApplicationInfo appInfo;
            if (type==null) {
                appInfo = deployment.deploy(deploymentContext);
            } else {
                appInfo = deployment.deploy(deployment.prepareSniffersForOSGiDeployment(type, deploymentContext), deploymentContext);
            }

            /*
             * Various deployers might have added to the downloadable or
             * generated artifacts.  Extract them and, if the command succeeded,
             * persist both into the app properties (which will be recorded
             * in domain.xml).
             */
            final Artifacts downloadableArtifacts =
                    DeploymentUtils.downloadableArtifacts(deploymentContext);
            final Artifacts generatedArtifacts =
                    DeploymentUtils.generatedArtifacts(deploymentContext);
            
            if (report.getActionExitCode()==ActionReport.ExitCode.SUCCESS) {
                try {
                    moveAppFilesToPermanentLocation(
                            deploymentContext, logger);
                    recordFileLocations(appProps);

                    downloadableArtifacts.record(appProps);
                    generatedArtifacts.record(appProps);

                    // register application information in domain.xml
                    deployment.registerAppInDomainXML(appInfo, deploymentContext, t);
                    suppInfo.setDeploymentContext(deploymentContext);
                } catch (Exception e) {
                    // roll back the deployment and re-throw the exception
                    deployment.undeploy(name, deploymentContext);
                    deploymentContext.clean();
                    throw e;
                }

            }
            if(retrieve != null) {
                retrieveArtifacts(context, downloadableArtifacts.getArtifacts(),
                        retrieve, 
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
                        path.getAbsolutePath()), e);
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
      }
    }

    /**
     * Makes safe copies of the archive and deployment plan for later use
     * during instance sync activity.
     * <p>
     * We rename any uploaded files from the temp directory to the permanent
     * place, and we copy any archive files that were not uploaded.  This
     * prevents any confusion that could result if the developer modified the
     * archive file - changing its lastModified value - before redeploying it.
     *
     * @param deploymentContext
     * @param logger logger
     * @throws IOException
     */
    private void moveAppFilesToPermanentLocation(
            final ExtendedDeploymentContext deploymentContext,
            final Logger logger) throws IOException {
        final File finalUploadDir = deploymentContext.getAppInternalDir();
        finalUploadDir.mkdirs();
        safeCopyOfApp = renameUploadedFileOrCopyInPlaceFile(
                finalUploadDir, originalPathValue, logger);
        safeCopyOfDeploymentPlan = renameUploadedFileOrCopyInPlaceFile(
                finalUploadDir, deploymentplan, logger);
    }

    private File renameUploadedFileOrCopyInPlaceFile(
            final File finalUploadDir,
            final File fileParam,
            final Logger logger) throws IOException {
        if (fileParam == null) {
            return null;
        }
        /*
         * If the fileParam resides within the applications directory then
         * it has been uploaded.  In that case, rename it.
         */
        final File appsDir = env.getApplicationRepositoryPath();

        /*
         * The default answer is the in-place file, to handle the
         * directory-deployment case or the in-place archive case if we ae
         * not copying the in-place archive.
         */
        File result = fileParam;
        
        if ( ! appsDir.toURI().relativize(fileParam.toURI()).isAbsolute()) {
            /*
             * The file lies within the apps directory, so it was 
             * uploaded.
             */
            result = new File(finalUploadDir, fileParam.getName());
            FileUtils.renameFile(fileParam, result);
            result.setLastModified(fileParam.lastModified());
        } else {
            final boolean copyInPlaceArchive = Boolean.valueOf(
                    System.getProperty(COPY_IN_PLACE_ARCHIVE_PROP_NAME, "true"));
            if ( ! fileParam.isDirectory() && copyInPlaceArchive) {
                /*
                 * The file was not uploaded and the in-place file is not a directory,
                 * so copy the archive to the permanent location.
                 */
                final long startTime = System.currentTimeMillis();
                result = new File(finalUploadDir, fileParam.getName());
                FileUtils.copy(fileParam, result);
                result.setLastModified(fileParam.lastModified());
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "*** In-place archive copy of {0} took {1} ms",
                            new Object[]{
                                fileParam.getAbsolutePath(),
                                System.currentTimeMillis() - startTime});
                }
            }
        }
        return result;
    }

    private void recordFileLocations(
            final Properties appProps) throws URISyntaxException {
        /*
         * Setting the properties in the appProps now will cause them to be
         * stored in the domain.xml elements along with other properties when
         * the entire config is saved.
         */
        if (safeCopyOfApp != null) {
            appProps.setProperty(Application.APP_LOCATION_PROP_NAME,
                    DeploymentUtils.relativizeWithinDomainIfPossible(
                    safeCopyOfApp.toURI()));
        }
        if (safeCopyOfDeploymentPlan != null) {
                appProps.setProperty(Application.DEPLOYMENT_PLAN_LOCATION_PROP_NAME,
                        DeploymentUtils.relativizeWithinDomainIfPossible(
                        safeCopyOfDeploymentPlan.toURI()));
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
     * @return context properties that might have been set by the deployers
     * while undeploying the application
     *
     */
    private Properties handleRedeploy(final String name, final ActionReport report)
        throws Exception {
        if (isRedeploy) 
        {
            //preserve settings first before undeploy
            Application app = apps.getModule(Application.class, name);

            // we save some of the old registration information in our deployment parameters
            settingsFromDomainXML(app);

            //if application is already deployed and force=true,
            //then undeploy the application first.

            // Use ParameterMap till we have a better way
            // to invoke a command on both DAS and instance with the
            // replication framework
            final ParameterMap parameters = new ParameterMap();
            parameters.add("DEFAULT", name);
            parameters.add("target", target);
            parameters.add("keepreposdir", keepreposdir.toString());
            if (dropandcreatetables != null) {
                parameters.add("droptables", dropandcreatetables.toString());
            }
            parameters.add("ignoreCascade", force.toString());
            if (keepstate != null) {
                parameters.add("keepstate", keepstate.toString());
            }

            ActionReport subReport = report.addSubActionsReport();
            subReport.setExtraProperties(new Properties());
            if (properties!=null && properties.containsKey(DeploymentProperties.KEEP_SESSIONS)) {
                Properties undeployProperties = new Properties();
                undeployProperties.put(DeploymentProperties.KEEP_SESSIONS, properties.getProperty(DeploymentProperties.KEEP_SESSIONS));
                parameters.add("properties", DeploymentUtils.propertiesValue(undeployProperties, ':'));
            } else if (property!=null && property.containsKey(DeploymentProperties.KEEP_SESSIONS)) {
                Properties undeployProperties = new Properties();
                undeployProperties.put(DeploymentProperties.KEEP_SESSIONS, property.getProperty(DeploymentProperties.KEEP_SESSIONS));
                parameters.add("properties", DeploymentUtils.propertiesValue(undeployProperties, ':'));
            }

            CommandRunner.CommandInvocation inv = commandRunner.getCommandInvocation("undeploy", subReport);

            inv.parameters(parameters).execute();
            return subReport.getExtraProperties();
        }
        return null;
    }

    /**
     * Places into the outgoing payload the downloadable artifacts for an application.
     * @param context the admin command context for the command requesting the artifacts download
     * @param app the application of interest
     * @param targetLocalDir the client-specified local directory to receive the downloaded files
     */
    public static void retrieveArtifacts(final AdminCommandContext context,
            final Application app,
            final String targetLocalDir) {
        retrieveArtifacts(context, app, targetLocalDir, true);
    }

    /**
     * Places into the outgoing payload the downloadable artifacts for an application.
     * @param context the admin command context for the command currently running
     * @param app the application of interest
     * @param targetLocalDir the client-specified local directory to receive the downloaded files
     * @param reportErrorsInTopReport whether to include error indications in the report's top-level
     */
    public static void retrieveArtifacts(final AdminCommandContext context,
            final Application app,
            final String targetLocalDir,
            final boolean reportErrorsInTopReport) {
        retrieveArtifacts(context,
                DeploymentUtils.downloadableArtifacts(app).getArtifacts(),
                targetLocalDir,
                reportErrorsInTopReport);
    }

    private static void retrieveArtifacts(final AdminCommandContext context,
            final Collection<Artifacts.FullAndPartURIs> artifactInfo,
            final String targetLocalDir,
            final boolean reportErrorsInTopReport) {

        Logger logger = context.getLogger();
        try {
            Payload.Outbound outboundPayload = context.getOutboundPayload();
            Properties props = new Properties();
            /*
             * file-xfer-root is used as a URI, so convert backslashes.
             */
            props.setProperty("file-xfer-root", targetLocalDir.replace('\\', '/'));
            for (Artifacts.FullAndPartURIs uriPair : artifactInfo) {
                if(logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "About to download artifact {0}", uriPair.getFull());
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

            previousTargets = domain.getAllReferencedTargetsForApplication(name);
        }
    }

    @Override
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
            context.getLogger().log(
                Level.WARNING, "Can not generate verifier report: {0}", ioe.getMessage());
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
