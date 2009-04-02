package org.glassfish.javaee.full.deployment;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.internal.deployment.GenericCompositeSniffer;
import org.glassfish.deployment.common.DeploymentUtils;

/**
 * Ear sniffers snifs ear files.
 *
 * @author Jerome Dochez
 */
@Service(name="ear")
public class EarSniffer extends GenericCompositeSniffer {


    public EarSniffer() {
        super("ear", "META-INF/application.xml", null);
    }

    public String[] getContainersNames() {
        return new String[] { "org.glassfish.javaee.full.deployment.EarContainer"};
    }                                                                              
    /**
     * Returns true if the passed file or directory is recognized by this
     * instance.
     *
     * @param location the file or directory to explore
     * @param loader class loader for this application
     * @return true if this sniffer handles this application type
     */
    public boolean handles(ReadableArchive location, ClassLoader loader) {
        return DeploymentUtils.isEAR(location);
    }

}

