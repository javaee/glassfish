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

package com.sun.enterprise.module;

import java.net.URI;
import java.util.jar.Manifest;

/**
 * A module definition contains all information about a module
 * existence, its dependencies and its exported interfaces.
 *
 * This module meta information can be obtained from different
 * sources and format. For instance OSGi modules use the manifest
 * file and so is the glassfish application server. Others can
 * use api or xml file.
 *
 * @author Jerome Dochez
 */
public interface ModuleDefinition {

    /**
     *  Returns the module name, usually this is the same name as
     *  the jar file name containing the module's implementation.

     * @return module name
     */
    String getName();

    /**
     *  Returns a list of public interfaces for this module.
     *  Public interface can be packages, interfaces, or classes
     *
     * @return a array of public interfaces
     */
    String[] getPublicInterfaces();

    /**
     * Returns the list of this module's dependencies. Each dependency
     * must be satisfied at run time for this module to function
     * properly.
     *
     * @return list of dependencies
     */
    ModuleDependency[] getDependencies();

    /**
     * A Module is implemented by one to many jar files. This method returns
     * the list of jar files implementing the module
     *
     * @return the module's list of implementation jars
     */
    URI[] getLocations();

    /**
     * Returns the version of this module's implementation
     *
     * @return a version number
     */
    String getVersion();

    /**
     * Returns the import policy class name. Although the implementation of
     * this policy does not necessary have to implement the ImportPolicy, but
     * could use another interface, it is the responsibility of the associated
     * Repository to invoke that interface when the module is started.
     *
     * @return the ImportPolicy or equivalent interface implementation
     */
    String getImportPolicyClassName();

    /**
     * Returns the lifecycle policy class name. Although the implementation of
     * this policy does not necessary have to implement the LifecyclePolicy, but
     * could use another interface, it is the responsibility of the associated
     * Repository to invoke that interface when the module is started.
     *
     * @return the LifecyclePolicy or equivalent interface implementation
     */
    String getLifecyclePolicyClassName();

    /**
     * Returns the manifest file for the main module jar file
     * 
     * @return the manifest file
     */
    Manifest getManifest();

    /**
     * Gets the metadata that describes various components and services in this module.
     *
     * @return
     *      Always non-null.
     */
    ModuleMetadata getMetadata();
}
