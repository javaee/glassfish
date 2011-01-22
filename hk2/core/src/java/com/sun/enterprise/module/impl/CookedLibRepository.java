/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.module.impl;

import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.Repository;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dochez
 */
public abstract class CookedLibRepository implements Repository {
    
    protected final File rootLocation;
    protected String[] fileNames = {} ;
    
    /** Creates a new instance of CookedLibRepository */
    public CookedLibRepository(String installRoot) {
        rootLocation = new File(installRoot);        
    }
    
    
    /**
     * Returns the repository name
     * @return repository name
     */
    public String getName() {
        return "cooked lib";
    }
    
    /**
     * Returns the repository location
     * @return the URL for the repository location
     */
    public URI getLocation() {
        return rootLocation.toURI();

    }
    
    /**
     * Finds and returns a <code>DefaultModuleDefinition</code> instance
     * for a module given the name and version constraints.
     * @param name the requested module name
     * @param version the requestion module version
     * @return a <code>DefaultModuleDefinition</code> or null if not found
     * in this repository.
     */
    public ModuleDefinition find(String name, String version) {
        for (ModuleDefinition moduleDef : findAll()) {
            // so far we ignore the module version...
            if (moduleDef.getName().equals(name)) {
                return moduleDef;
            }
        }
        return null;
    }
    
    /**
     * Returns a list of all modules available in this repository 
     * @return a list of available modules
     */
    public abstract List<ModuleDefinition> findAll();
    
    /**
     * Finds and returns a list of all the available versions of a 
     * module given its name.
     * @param name the requested module name
     */
    public List<ModuleDefinition> findAll(String name) {
        List<ModuleDefinition> matchingDefs = new ArrayList<ModuleDefinition>();
        for (ModuleDefinition moduleDef : findAll()) {
            if (moduleDef.getName().equals(name)) {
                matchingDefs.add(moduleDef);
            }
        }
        return matchingDefs;
    }
    
    /**
     * Initialize the repository for use. This need to be called at least
     * once before any find methods is invoked.  
     * @throws IOException if an error occur accessing the repository
     */
    public abstract void initialize() throws IOException;

    /**
     * Adds a new module
     */
    public void add(ModuleDefinition definition) throws IOException {
        throw new UnsupportedOperationException("add not suppored for cooked lib");
    }
    
    /**
     * Shutdown the repository. After this call return, the find methods cannot 
     * be used until initialize() is called again.
     * @throws IOException if an error occur accessing the repository
     */
    public void shutdown() throws IOException {
        
    }
    
    public String toString() {
        StringBuffer s = new StringBuffer();
        for (ModuleDefinition moduleDef : findAll()) {
            s.append(moduleDef.getName()).append(":");
        }
        return s.toString();
    }

}
