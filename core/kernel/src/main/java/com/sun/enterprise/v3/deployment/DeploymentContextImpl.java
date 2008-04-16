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

import java.lang.instrument.ClassFileTransformer;
import org.glassfish.api.deployment.DeploymentContext;

import org.glassfish.api.deployment.archive.ReadableArchive;

import java.util.*;
import java.util.logging.Logger;
import java.io.File;

import com.sun.enterprise.v3.server.ServerEnvironment;
import com.sun.enterprise.module.ModuleDefinition;

/**
 *
 * @author dochez
 */
public class DeploymentContextImpl implements DeploymentContext {

    final ReadableArchive source;
    final Properties parameters;
    final Logger logger;
    final ServerEnvironment env;
    ClassLoader cloader;
    Properties props;
    Map<String, Object> modulesMetaData = new HashMap<String, Object>();
    List<ClassFileTransformer> transformers = new ArrayList<ClassFileTransformer>();
    List<ModuleDefinition> publicAPIs = new ArrayList<ModuleDefinition>();

    /** Creates a new instance of DeploymentContext */
    public DeploymentContextImpl(Logger logger, ReadableArchive source, Properties params, ServerEnvironment env) {
        this.source = source;
        this.logger = logger;
        this.parameters = params;
        this.env = env;
    }

    public ReadableArchive getSource() {
        return source;
    }

    public Properties getCommandParameters() {
        return parameters;
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
    public ClassLoader getClassLoader() {
        return cloader;
    }

    /**
     * Resets the application class loader associated with this deployment request.
     *
     * @param cloader
     */
    public void setClassLoader(ClassLoader cloader) {
        this.cloader = cloader;
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
        final String appName = parameters.getProperty(DeployCommand.NAME);
        File rootScratchDir;
        if (subDirName == null ) {
            rootScratchDir = new File(env.getApplicationStubPath());
        } else {
            rootScratchDir = new File(env.getApplicationStubPath(),
                subDirName);
        }
        return new File(rootScratchDir, appName);
    }

    /**
     * Returns the directory where the original applications bits should be
     * stored. This is useful when users deploy an archive file that need to
     * be unzipped somewhere for the container to work with.
     *
     * @return the source directory for this application
     */
    public File getSourceDir() {

        return new File(source.getURI());
    }

    public void addModuleMetaData(Object metaData) {
        modulesMetaData.put(metaData.getClass().getName(), metaData);
    }

    public <T> T getModuleMetaData(Class<T> metadataType) {
        Object moduleMetaData = modulesMetaData.get(metadataType.getName());
        if (moduleMetaData != null) {
            return metadataType.cast(moduleMetaData);
        } else {
            return null;
        }
    }

    /**
     * Returns the properties that will be persisted as a key value pair at
     * then end of deployment. That allows individual Deployers implementation
     * to store some information that should be available upon server restart.
     *
     * @return the application's properties.
     */
    public synchronized Properties getProps() {
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

    /**
     * Add a new ModuleDefinition to the public APIs of this application. This can be done before
     * the load phase or it will generate an UnsupportedOpertationException
     *
     * @param def module definition to be added to the list of imports for that application
     * @throws UnsupportedOperationException when it is too late to add a new public API
     */
    public void addPublicAPI(ModuleDefinition def) throws UnsupportedOperationException {
        publicAPIs.add(def);
    }

    /**
     * Returns the list of public APIs to be added to the application class loader
     *
     * @return list of public APIs
     */
    public List<ModuleDefinition> getPublicAPIs() {
        return publicAPIs;
    }
}
