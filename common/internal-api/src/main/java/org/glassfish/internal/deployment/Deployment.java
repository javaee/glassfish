package org.glassfish.internal.deployment;

import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.ActionReport;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.EngineInfo;
import org.jvnet.hk2.annotations.Contract;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Deployment facility
 *
 * @author Jerome Dochez
 */
@Contract
public interface Deployment {

    public ArchiveHandler getArchiveHandler(ReadableArchive archive) throws IOException;
    public ApplicationInfo deploy(Iterable<Sniffer> sniffers, final DeploymentContext context, final ActionReport report);

    public LinkedList<EngineInfo> setupContainerInfos(
            Iterable<Sniffer> sniffers, DeploymentContext context,
            ActionReport report) throws Exception;

}
