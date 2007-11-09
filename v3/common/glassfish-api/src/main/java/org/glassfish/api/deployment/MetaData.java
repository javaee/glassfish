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

import com.sun.enterprise.module.ModuleDefinition;

/**
 * MetaData associated with a Deployer. This is used by the deployment layers
 * to identify the special requirements of the Deployer.
 *
 * Supported Requirements :
 *      invalidatesClassLoader  Deployer can load classes that need to be reloaded
 *                              for the application to run successfully hence requiring
 *                              the class loader to be flushed and reinitialized between
 *                              the prepare and load phase.
 *      componentAPIs           Components can use APIs that are defined outside of the
 *                              component's bundle. These component's APIs (eg. Java EE
 *                              APIs) must be imported by the application class loader
 *                              before any application code is loaded.
 */
public class MetaData {

    private final boolean invalidatesCL;
    private final ModuleDefinition[] componentAPIs;

    /**
     * Constructor for the Deployer's metadata
     *
     * @param invalidatesClassLoader If true, invalidates the class loader used during
     * the deployment's prepare phase
     * @param componentAPIs is an array of module definition that should be added to the
     * deployable artifact list of imported modules.
     *
     */
    public MetaData(boolean invalidatesClassLoader, ModuleDefinition[] componentAPIs) {
        this.invalidatesCL = invalidatesClassLoader;
        this.componentAPIs = componentAPIs;
    }

    /**
     * Returns whether or not the class loader is invalidated by the Deployer's propare
     * phase.
     * 
     * @return true if the class loader is invalid after the Deployer's prepare phase
     * call.
     */
    public boolean invalidatesClassLoader() {
        return invalidatesCL;
    }

    /**
     * Returns the array of module definition containing the public APIs of applications
     * supported by this deployer.
     *
     * @return the public APIs fo the associated container as an array of module definitions
     */
    public ModuleDefinition[] getPublicAPIs() {
        return componentAPIs;
    }
}
