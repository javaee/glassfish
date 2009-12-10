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

import org.glassfish.api.deployment.DeploymentContext;

import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.MetaData;

/**
 * Convenient superclass for Deployers which only do prepare and 
 * clean up and do not actually load/unload application
 *
 */
public abstract class SimpleDeployer <T extends Container, 
        U extends ApplicationContainer> implements Deployer<T, U> {

    /**
     * Loads the meta date associated with the application.
     *
     * @parameters type type of metadata that this deployer has declared providing.
     */
    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    /**
     * Prepares the application bits for running in the application server.
     * For certain cases, this is generating non portable
     * artifacts and other application specific tasks.
     * Failure to prepare should throw an exception which will cause the overall
     * deployment to fail.
     *
     * @param dc deployment context
     * @return true if the prepare phase was successful
     *
     */
    public boolean prepare(DeploymentContext dc) {
        try {
            generateArtifacts(dc);
            return true;
        } catch (Exception ex) {
            // re-throw all the exceptions as runtime exceptions
            RuntimeException re = new RuntimeException(ex.getMessage());
            re.initCause(ex);
            throw re;
        }
    }

    /**
     * No-op 
     */
    public U load(T container, DeploymentContext context) {
        return null;
    }

    /**
     * No-op
     */
    public void unload(U appContainer, DeploymentContext context) {
    }

    /**
     * Clean any files and artifacts that were created during the execution
     * of the prepare method.
     *
     * @param context deployment context
     */
    public void clean(DeploymentContext context) {
        try {
            cleanArtifacts(context);
        } catch (Exception ex) {
            // re-throw all the exceptions as runtime exceptions
            RuntimeException re = new RuntimeException(ex.getMessage());
            re.initCause(ex);
            throw re;
        }
    }

    /**
     * Returns the meta data assocated with this Deployer
     *
     * @return the meta data for this Deployer
     */
    public MetaData getMetaData() {
        return new MetaData(false, null, null);
    }

    // methods for implementation clsses to override
    abstract protected void generateArtifacts(DeploymentContext dc) 
        throws DeploymentException;
    abstract protected void cleanArtifacts(DeploymentContext dc)
        throws DeploymentException;

}
