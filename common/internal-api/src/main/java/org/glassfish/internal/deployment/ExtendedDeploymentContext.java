package org.glassfish.internal.deployment;

import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.data.ApplicationInfo;

import java.lang.instrument.ClassFileTransformer;
import java.util.List;
import java.net.URISyntaxException;
import java.net.MalformedURLException;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 12, 2009
 * Time: 2:49:07 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ExtendedDeploymentContext extends DeploymentContext {
    
    public enum Phase { UNKNOWN, PREPARE, LOAD, START, STOP, UNLOAD, CLEAN };


    /**
     * Sets the phase of the deployment activity.
     * 
     * @param newPhase
     */
    public void setPhase(Phase newPhase);

    /**
     * Returns the final class loader that will be used to load the application
     * bits in their associated runtime container.
     *
     * @return final class loader
     */
    public ClassLoader getFinalClassLoader();

    /**
     * Returns the list of transformers registered to this context.
     *
     * @return the transformers list
     */
    public List<ClassFileTransformer> getTransformers();

    /**
     * Create the class loaders for the application pointed by the getSource()
     *
     * @param clh the hierarchy of class loader for the parent
     * @param handler the archive handler for the source archive
     */
    public void createClassLoaders(ClassLoaderHierarchy clh, ArchiveHandler handler)
            throws URISyntaxException, MalformedURLException;

    public void setApplicationInfo(ApplicationInfo appInfo);
    public ApplicationInfo getApplicationInfo();

    public void clean();
}
