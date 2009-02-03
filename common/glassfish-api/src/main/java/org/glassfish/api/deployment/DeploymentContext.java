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

package org.glassfish.api.deployment;

import org.glassfish.api.ExecutionContext;

import org.glassfish.api.deployment.archive.ReadableArchive;
import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.util.Properties;
import java.util.Collection;

/**
 * Useful services for Deployer service implementation
 *
 * @author Jerome Dochez
 */
public interface DeploymentContext extends ApplicationContext, ExecutionContext {

    /**
     * Application bits, at the raw level. Deployer's should avoid
     * using such low level access as it binds the deployer to a particular directory
     * layout. Instead Deployers should use the class loader obtained via the getClassLoader() API
     *
     * @return Abstraction to the application's source archive.
     */
    public ReadableArchive getSource();

    /**
     * During the prepare phase, when a deployer need to have access to the class loader
     * that will be used to load the application in the runtime container, it can call
     * this API during the prepare phase. Otherswise, deployers should use the
     * getClassLoader API.
     *
     * If a deployers needs to have access to the classloader during the prepare phase
     *
     *
     * @return the final class loader
     */
    public ClassLoader getFinalClassLoader();

    /**
     * Depending on the phase of deployment, returns a temporary class loader (prepare
     * phase) or the final class loader used to run the application in the various
     * containers.
     *
     * @return a class loader to load application bits with
     */
    public ClassLoader getClassLoader();

    /**
     * Returns the DeployCommand parameters 
     * @return the command parameters
     */
    public Properties getCommandParameters();

    /**
     * Returns a scratch directory that can be used to store things in.
     * The scratch directory will be persisted accross server restart but not 
     * accross redeployment of the same application
     *
     * @param subDirName the sub directory name of the scratch dir
     * @return the specific scratch subdirectory for this application based on 
     *         passed in subDirName. Returns the root scratch dir if the 
     *         passed in value is null.      
     */
    public File getScratchDir(String subDirName);
    
    /**
     * Returns the directory where the original applications bits should be 
     * stored. This is useful when users deploy an archive file that need to 
     * be unzipped somewhere for the container to work with. 
     * 
     * @return the source directory for this application
     */
    public File getSourceDir();

    /**
     * Stores a descriptor for the module in the context so other deployer's
     * can have access to it. Module meta-data is usual not persistent which
     * mean that any modification to it will not be available at the next
     * server restart and will need to be reset.
     *
     * @param metaData the meta data itself
     */
    public void addModuleMetaData(Object metaData);

    /**
     * Returns the meta data associated with a module type.
     *
     * @param metadataType type of the meta date.
     * @return
     */
    public <T> T getModuleMetaData(Class<T> metadataType);

    /**
     * Returns the properties that will be persisted as a key value pair at
     * then end of deployment. That allows individual Deployers implementation
     * to store some information that should be available upon server restart.
     *
     * @return the application's properties.
     */
    public Properties getProps();

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
    public void addTransformer(ClassFileTransformer transformer);

    /**
     * Returns all the metadata assocated with this deployment
     *
     * @return collection of metadata added to the context
     */
    public Collection<Object> getModuleMetadata();        
}
