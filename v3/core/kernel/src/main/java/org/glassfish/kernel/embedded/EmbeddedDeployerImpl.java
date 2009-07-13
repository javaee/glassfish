package org.glassfish.kernel.embedded;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.config.TransactionFailure;
import org.glassfish.api.embedded.EmbeddedDeployer;
import org.glassfish.api.embedded.Server;
import org.glassfish.api.embedded.EmbeddedContainer;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.internal.deployment.SnifferManager;
import org.glassfish.internal.data.ApplicationInfo;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URI;
import java.net.URISyntaxException;

import com.sun.enterprise.v3.server.ApplicationLifecycle;
import com.sun.enterprise.v3.common.PlainTextActionReporter;
import com.sun.enterprise.v3.admin.CommandRunnerImpl;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.logging.LogDomains;

/**
 * @author Jerome Dochez
 */
@Service
public class EmbeddedDeployerImpl implements EmbeddedDeployer {

    @Inject
    Deployment deployment;

    @Inject
    Server server;

    @Inject
    CommandRunner commandRunner;

    @Inject
    Habitat habitat;

    @Inject
    ArchiveFactory factory;

    @Inject
    SnifferManager snifferMgr;

    Map<String, ApplicationInfo> deployedApps = new HashMap<String, ApplicationInfo>();

    final static Logger logger = LogDomains.getLogger(EmbeddedDeployerImpl.class, LogDomains.CORE_LOGGER);

    public File getApplicationsDir() {
        return null;
    }

    public void enableAutoDeploy() {

    }

    public void disableAutoDeploy() {

    }

    public String deploy(File archive, DeployCommandParameters params) {
        try {
            ReadableArchive r = factory.openArchive(archive);
            return deploy(r, params);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }

        return null;
    }

    public String deploy(ReadableArchive archive, DeployCommandParameters params) {

        ActionReport report = new PlainTextActionReporter();
        ExtendedDeploymentContext context = null;
        try {
            context = deployment.getContext(logger, archive, params, report);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final ClassLoader cl = context.getClassLoader();
        Collection<Sniffer> sniffers = snifferMgr.getSniffers(archive, cl);
        List<Sniffer> finalSniffers = new ArrayList<Sniffer>();

        // nowe we intersect with the conficgured sniffers.
        for (EmbeddedContainer container : server.getContainers()) {
            for (Sniffer sniffer : container.getSniffers()) {
                if (sniffers.contains(sniffer)) {
                    finalSniffers.add(sniffer);            
                }
            }
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = deployment.deploy(finalSniffers, context);
        } catch(Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        if (appInfo!=null) {
            deployedApps.put(appInfo.getName(), appInfo);
            return appInfo.getName();
        }
        return null;
    }

    public void undeploy(String name) {

        ActionReport report = habitat.getComponent(ActionReport.class, "plain");
        ApplicationInfo info = deployedApps.get(name);
        if (info==null) {
            info = deployment.get(name);
        }
        if (info == null) {
            report.setMessage(
                "Cannot find deployed application of name " + name);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;            
        }

        ReadableArchive source = info.getSource();
        if (source == null) {
            report.setMessage(
                "Cannot get source archive for undeployment");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        UndeployCommandParameters params = new UndeployCommandParameters(name);
        
        ExtendedDeploymentContext deploymentContext = null;
        try {
            deploymentContext = deployment.getContext(logger, source, params, report);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot create context for undeployment ", e);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }


        if (info!=null) {
            deployment.undeploy(name, deploymentContext);
        }


        if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {

            //remove context from generated
            deploymentContext.clean();

        }
        
    }

    public void undeployAll() {
        for (String appName : deployedApps.keySet()) {
            undeploy(appName);
        }

    }
}
