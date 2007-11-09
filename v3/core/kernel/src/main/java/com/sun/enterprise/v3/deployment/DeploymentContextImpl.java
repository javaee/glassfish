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
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.io.File;

import com.sun.enterprise.v3.server.V3Environment;

/**
 *
 * @author dochez
 */
public class DeploymentContextImpl implements DeploymentContext {
    
    final ReadableArchive source;
    final Properties parameters;
    final Logger logger;
    final V3Environment env;
    ClassLoader cloader;
    Properties props;
    Map<String, Object> modulesMetaData = new HashMap<String, Object>();


    /** Creates a new instance of DeploymentContext */
    public DeploymentContextImpl(Logger logger, ReadableArchive source, Properties params, V3Environment env) {
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
        return null; 
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
     * The scratch directory will be persisted accross server restart but not accross
     * redeployment of the same application
     *
     * @return the scratch directory for this application.
     */
    public File getScratchDir() {
        final String appName = parameters.getProperty(DeployCommand.NAME);
        return new File(env.getApplicationStubPath(), appName);
    }

    /**
     * Returns the directory where the original applications bits should be
     * stored. This is useful when users deploy an archive file that need to
     * be unzipped somewhere for the container to work with.
     *
     * @return the source directory for this application
     */
    public File getSourceDir() {

        String moduleName = parameters.getProperty(DeployCommand.NAME);
        return new File(env.getApplicationRepositoryPath(), moduleName);

    }

    public void addModuleMetaData(String moduleType, Object metaData) {
        modulesMetaData.put(moduleType, metaData);
    }

    public <T> T getModuleMetaData(String moduleType, Class<T> metadataType) {
        Object moduleMetaData = modulesMetaData.get(moduleType);
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

    public void addClassFileTransformer(ClassFileTransformer arg0) 
            throws UnsupportedOperationException {
        
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
