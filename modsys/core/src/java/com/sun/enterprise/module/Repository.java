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

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * A Repository instance is an abstraction of a set of accessible
 * modules. Repository can be local or remote and are used to
 * procure modules implementation based on constraints like name or
 * version.
 *
 * @author Jerome Dochez
 */
public interface Repository {
    
    /**
     * Returns the repository name
     * @return repository name
     */
    public String getName();
    
    /**
     * Returns the repository location
     * @return the URI for the repository location
     */
    public URI getLocation();
    
    /**
     * Finds and returns a <code>DefaultModuleDefinition</code> instance
     * for a module given the name and version constraints.
     * @param name the requested module name
     * @param version
     *      the module version. Can be null if the caller doesn't care about the version.
     * @return a <code>DefaultModuleDefinition</code> or null if not found
     * in this repository.
     */
    public ModuleDefinition find(String name, String version);
    
    /**
     * Returns a list of all modules available in this repository 
     * @return a list of available modules
     */
    public List<ModuleDefinition> findAll();
    
    /**
     * Finds and returns a list of all the available versions of a 
     * module given its name.
     * @param name the requested module name
     */
    public List<ModuleDefinition> findAll(String name);
    
    /**
     * Initialize the repository for use. This need to be called at least
     * once before any find methods is invoked.  
     * @throws IOException if an error occur accessing the repository
     */
    public void initialize() throws IOException;
    
    /**
     * Shutdown the repository. After this call return, the find methods cannot 
     * be used until initialize() is called again.
     * @throws IOException if an error occur accessing the repository
     */
    public void shutdown() throws IOException;

    /**
     * Creates a module instance from a Module definition.
     * Different implementation of repositories may need to use different means
     * of initializing a Module instance depending on the module's definition nature.
     * For instance, an Netbeans module or a JSR277/Glassfish module could use different
     * implementation of Module.
     *
     * @param registry is the registry that will own the module
     * @param moduleDef the requested module definition
     * @return the uninitialized Module instance
     */
    public Module newModule(ModulesRegistry registry, ModuleDefinition moduleDef);

    /**
     * This module adds a new Module Definition to the repository.
     *
     * @param definition is the module definition
     * @return true if the addition was successful
     */
    //public void add(ModuleDefinition definition) throws IOException;
}
