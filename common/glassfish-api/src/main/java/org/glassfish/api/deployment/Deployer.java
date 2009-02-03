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

import org.glassfish.api.container.Container;

/**
 * A deployer is capable of deploying one type of applications.
 *
 * Deployers shoud use the ArchiveHandler to get a ClassLoader capable of loading classes
 * and resources from the archive type that his being deployed.
 *
 * In all cases the ApplicationContainer subclass must return the class loader associated
 * with the application. In case the application is deployed to more than one container
 * the class loader can be shared and therefore should be retrieved from the ArchiveHandler 
 *
 * @param T is the container type associated with this deployer
 * @param U is the ApplicationContainer implementation for this deployer
 * @author Jerome Dochez
 */
public interface Deployer<T extends Container, U extends ApplicationContainer> {

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    public MetaData getMetaData();    

    /**
     * Loads the meta date associated with the application.
     *
     * @parameters type type of metadata that this deployer has declared providing.
     */
    public <V> V loadMetaData(Class<V> type, DeploymentContext context);

    /**
     * Prepares the application bits for running in the application server. 
     * For certain cases, this is generating non portable artifacts and
     * other application specific tasks. 
     * Failure to prepare should throw an exception which will cause the overall
     * deployment to fail.
     *
     * @param context of the deployment
     * @return true if the prepare phase executed successfully
     */
    public boolean prepare(DeploymentContext context);
    
    /**
     * Loads a previously prepared application in its execution environment and 
     * return a ContractProvider instance that will identify this environment in
     * future communications with the application's container runtime.
     * @param container in which the application will reside
     * @param context of the deployment
     * @return an ApplicationContainer instance identifying the running application
     */
    public U load(T container, DeploymentContext context);
    
    /** 
     * Unload or stop a previously running application identified with the 
     * ContractProvider instance. The container will be stop upon return from this
     * method. 
     * @param appContainer instance to be stopped
     * @param context of the undeployment
     */
    public void unload(U appContainer, DeploymentContext context);
    
    /**
     * Clean any files and artifacts that were created during the execution 
     * of the prepare method. 
     * @param context deployment context
     */
    public void clean(DeploymentContext context);
}
