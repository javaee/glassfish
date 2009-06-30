package org.glassfish.javaee.full.deployment;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.internal.deployment.GenericCompositeSniffer;
import org.glassfish.deployment.common.DeploymentUtils;

import java.util.List;
import java.util.ArrayList;

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

    /**
     * @return whether this sniffer should be visible to user
     *
     */
    public boolean isUserVisible() {
        return true;
    }

    private static final List<String> deploymentConfigurationPaths =
            initDeploymentConfigurationPaths();

    private static List<String> initDeploymentConfigurationPaths() {
        final List<String> result = new ArrayList<String>();
        result.add("META-INF/application.xml");
        result.add("META-INF/sun-application.xml");
        return result;
    }

    /**
     * Returns the descriptor paths that might exist at the root of the 
     * ear.
     *
     * @return list of the deployment descriptor paths
     */
    @Override
    protected List<String> getDeploymentConfigurationPaths() {
        return deploymentConfigurationPaths;
    }

}

