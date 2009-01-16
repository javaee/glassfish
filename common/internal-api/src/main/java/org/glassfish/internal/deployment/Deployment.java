package org.glassfish.internal.deployment;

import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.ActionReport;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.EngineInfo;
import org.glassfish.internal.data.ModuleInfo;
import org.glassfish.internal.data.ProgressTracker;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.config.TransactionFailure;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Collection;

/**
 * Deployment facility
 *
 * @author Jerome Dochez
 */
@Contract
public interface Deployment {

    public ArchiveHandler getArchiveHandler(ReadableArchive archive) throws IOException;
    public ModuleInfo prepareModule(
        LinkedList<EngineInfo> sortedEngineInfos, String moduleName,
        DeploymentContext context, ActionReport report,
        ProgressTracker tracker) throws Exception;    
    public ApplicationInfo deploy(final ExtendedDeploymentContext context, final ActionReport report);
    public ApplicationInfo deploy(final Collection<Sniffer> sniffers, final ExtendedDeploymentContext context, final ActionReport report);
    public void undeploy(String appName, ExtendedDeploymentContext context, ActionReport report);

    public void registerAppInDomainXML(final ApplicationInfo
        applicationInfo, final DeploymentContext context)
        throws TransactionFailure;

    public void unregisterAppFromDomainXML(final String appName)
        throws TransactionFailure;
    

    public LinkedList<EngineInfo> setupContainerInfos(
            Iterable<Sniffer> sniffers, DeploymentContext context,
            ActionReport report) throws Exception;

    public boolean isRegistered(String appName);

    public ApplicationInfo get(String appName);
    

}
