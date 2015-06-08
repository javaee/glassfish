/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
     * @return
     *      Fully qualified class name that's assignable to {@link ImportPolicy},
     *      or null if no import policy exists.
     */
    String getImportPolicyClassName();

    /**
     * Returns the lifecycle policy class name. Although the implementation of
     * this policy does not necessary have to implement the LifecyclePolicy, but
     * could use another interface, it is the responsibility of the associated
     * Repository to invoke that interface when the module is started.
     *
     * @return
     *      Fully qualified class name that's assignable to {@link LifecyclePolicy},
     *      or null if no import policy exists.
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
