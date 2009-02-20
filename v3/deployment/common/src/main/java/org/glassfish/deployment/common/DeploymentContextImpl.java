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

package org.glassfish.deployment.common;

import java.lang.instrument.ClassFileTransformer;

import org.glassfish.api.deployment.InstrumentableClassLoader;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.deployment.DeployCommandParameters;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.loader.util.ASClassLoaderUtil;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;

import org.jvnet.hk2.component.PreDestroy;
import com.sun.enterprise.util.io.FileUtils;

/**
 *
 * @author dochez
 */
public class DeploymentContextImpl implements ExtendedDeploymentContext {


    final ReadableArchive source;
    final OpsParams parameters;
    final Logger logger;
    final ServerEnvironment env;
    ClassLoader cloader;
    ArchiveHandler archiveHandler;
    Properties props;
    Map<String, Object> modulesMetaData = new HashMap<String, Object>();
    List<ClassFileTransformer> transformers = new ArrayList<ClassFileTransformer>();
    Phase phase = Phase.UNKNOWN;
    boolean finalClassLoaderAccessedDuringPrepare = false;
    boolean tempClassLoaderInvalidated = false;
    ClassLoader sharableTemp = null;

    /** Creates a new instance of DeploymentContext */
    public DeploymentContextImpl(Logger logger, ReadableArchive source,
                                 OpsParams params, ServerEnvironment env) {
        this.source = source;
        this.logger = logger;
        this.parameters = params;
        this.env = env;
    }

    public void setPhase(Phase newPhase) {
        this.phase = newPhase;
    }

    public ReadableArchive getSource() {
        return source;
    }

    public <U extends OpsParams> U getCommandParameters(Class<U> commandParametersType) {
        try {
            return commandParametersType.cast(parameters);
        } catch (ClassCastException e) {
            return null;
        }
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * Returns the class loader associated to this deployment request.
     * ClassLoader instances are usually obtained by the getClassLoader API on
     * the associated ArchiveHandler for the archive type being deployed.
     * <p/>
     * This can return null and the container should allocate a ClassLoader
     * while loading the application.
     *
     * @return a class loader capable of loading classes and resources from the
     *         source
     * @link {org.jvnet.glassfish.apu.deployment.archive.ArchiveHandler.getClassLoader()}
     */
    public ClassLoader getFinalClassLoader() {

        // someone got hold of our final class loader, the temp is automatically invalidated.
        tempClassLoaderInvalidated = true;

        // check if we are in prepare phase and the final class loader has been accessed...
        if (phase==Phase.PREPARE) {
            if (finalClassLoaderAccessedDuringPrepare) {
                Boolean force = false;
                if (parameters instanceof DeployCommandParameters) {
                    force = ((DeployCommandParameters) parameters).force;
                }
                if (!force) {
                    throw new RuntimeException("More than one deployer is trying to access the final class loader during prepare phase," +
                            " use --force=true to force deployment");
                }
            } else {
                finalClassLoaderAccessedDuringPrepare=true;
            }
        }
        return cloader;
    }

    /**
     * Returns the class loader associated to this deployment request.
     * ClassLoader instances are usually obtained by the getClassLoader API on
     * the associated ArchiveHandler for the archive type being deployed.
     * <p/>
     * This can return null and the container should allocate a ClassLoader
     * while loading the application.
     *
     * @return a class loader capable of loading classes and resources from the
     *         source
     * @link {org.jvnet.glassfish.apu.deployment.archive.ArchiveHandler.getClassLoader()}
     */
    public ClassLoader getClassLoader() {
        return getClassLoader(true);
    }

    public void createClassLoaders(ClassLoaderHierarchy clh, ArchiveHandler handler)
            throws URISyntaxException, MalformedURLException {

        if (cloader!=null && sharableTemp!=null) {
            return;
        }
        // first we create the appLib class loader, this is non shared libraries class loader
        ClassLoader applibCL = clh.getAppLibClassLoader(parameters.name(), getAppLibs());

        ClassLoader parentCL = clh.createApplicationParentCL(applibCL, this);

        this.sharableTemp = handler.getClassLoader(parentCL, this);
        this.cloader = handler.getClassLoader(parentCL, this);
    }

    public void invalidateTempClassLoader() {
        tempClassLoaderInvalidated=true;

    }
    
    public synchronized ClassLoader getClassLoader(boolean sharable) {
        // if we are in prepare phase, we need to return our sharable temporary class loader
        // otherwise, we return the final one.
        if (phase==Phase.PREPARE) {
            if (sharable) {
                return sharableTemp;
            } else {
                InstrumentableClassLoader cl = InstrumentableClassLoader.class.cast(sharableTemp);
                return cl.copy();
            }
        } else {
            // we are out of the prepare phase, if none of our deployers have invalidated
            // their class loader during the prepare phase, we can continue using the
            // sharableone which will become the final class loader after all.
            if (tempClassLoaderInvalidated) {
                if (sharableTemp!=null) {                                                                 
                    try {
                        PreDestroy.class.cast(sharableTemp).preDestroy();
                    } catch (Exception e) {
                        // ignore, the classloader does not need to be destroyed
                    }
                    sharableTemp=null;
                }
                return cloader;
            } else {
                return sharableTemp;                
            }

        }
    }

    /**
     * Returns a scratch directory that can be used to store things in.
     * The scratch directory will be persisted accross server restart but
     * not accross redeployment of the same application
     *
     * @param subDirName the sub directory name of the scratch dir
     * @return the scratch directory for this application based on
     *         passed in subDirName. Returns the root scratch dir if the
     *         passed in value is null.
     */
    public File getScratchDir(String subDirName) {
        File rootScratchDir = env.getApplicationStubPath();
        if (subDirName != null )
            rootScratchDir = new File(rootScratchDir, subDirName);
        return new File(rootScratchDir, parameters.name());
    }

    /**
     * {@inheritDoc}
     */
    public File getSourceDir() {

        return new File(getSource().getURI());
    }

    public void addModuleMetaData(Object metaData) {
        if (metaData!=null) {
            modulesMetaData.put(metaData.getClass().getName(), metaData);
        }
    }

    public <T> T getModuleMetaData(Class<T> metadataType) {
        Object moduleMetaData = modulesMetaData.get(metadataType.getName());
        if (moduleMetaData != null) {
            return metadataType.cast(moduleMetaData);
        } else {
            for (Object metadata : modulesMetaData.values()) {
                try {
                    return metadataType.cast(metadata);
                } catch (ClassCastException e) {
                }
            }
            return null;
        }
    }

    public Collection<Object> getModuleMetadata() {
        List<Object> copy = new ArrayList<Object>();
        copy.addAll(modulesMetaData.values());
        return copy;
    }

    /**
     * Returns the properties that will be persisted as a key value pair at
     * then end of deployment. That allows individual Deployers implementation
     * to store some information that should be available upon server restart.
     *
     * @return the application's properties.
     */
    public Properties getProps() {
        if (props==null) {
            props = new Properties();
        }
        return props;
    }


    /**
     * Sets the extra properties for this deployment context
     *
     * @param props extra properties bag.
     */
    public void setProps(Properties props) {
        this.props = props;
    }

    /**
     * Add a new ClassFileTransformer to the context. Once all the deployers potentially
     * invalidating the application class loader (as indicated by the
     * @link {MetaData.invalidatesClassLoader()})
     * the deployment backend will recreate the application's class loader registering
     * all the ClassTransformers added by the deployers to this context.
     *
     * @param transformer the new class file transformer to register to the new application
     * class loader
     * @throws UnsupportedOperationException if the class loader we use does not support the
     * registration of a ClassFileTransformer. In such case, the deployer should either fail
     * deployment or revert to a mode without the byteocode enhancement feature.
     */
    public void addTransformer(ClassFileTransformer transformer) {

        transformers.add(transformer);
    }

    /**
     * Returns the list of transformers registered to this context.
     *
     * @return the transformers list
     */
    public List<ClassFileTransformer> getTransformers() {
        return transformers;
    }

    private List<URI> getAppLibs()
            throws URISyntaxException {
        List<URI> libURIs = new ArrayList<URI>();
        if (parameters.libraries() != null) {
            URL[] urls = 
                ASClassLoaderUtil.getLibrariesAsURLs(parameters.libraries(), 
                    env);
            for (URL url : urls) {
                libURIs.add(url.toURI());
            }
        }
        return libURIs;
    }

    public void clean() {
        // need to remove the generated directories...
        // need to remove generated/xml, generated/ejb, generated/jsp

        // remove generated/xml
        File generatedXmlRoot = getScratchDir("xml");
        FileUtils.whack(generatedXmlRoot);

        // remove generated/ejb
        File generatedEjbRoot = getScratchDir("ejb");
        // recursively delete...
        FileUtils.whack(generatedEjbRoot);

        // remove generated/jsp
        File generatedJspRoot = getScratchDir("jsp");
        // recursively delete...
        FileUtils.whack(generatedJspRoot);

    }

    public ArchiveHandler getArchiveHandler() {
        return archiveHandler;
    }

    public void setArchiveHandler(ArchiveHandler archiveHandler) {
        this.archiveHandler = archiveHandler;
    }
}
