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

package com.sun.enterprise.module.impl;

import com.sun.enterprise.module.ModuleDependency;
import com.sun.enterprise.module.common_impl.DefaultModuleDefinition;
import com.sun.enterprise.module.common_impl.DefaultModuleDefinition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.net.URI;

/**
 *
 * @author dochez
 */
public class CookedModuleDefinition extends DefaultModuleDefinition {
    
    List<String> publicPkgs = new ArrayList<String>();
    List<ModuleDependency> dependencies = new ArrayList<ModuleDependency>();
    Attributes attr;

    /** Creates a new instance of CookedModuleDefinitionefinition */
    public CookedModuleDefinition(File file, Attributes attr) throws IOException {
        super(file, attr);
    }
           
    public void addPublicInterface(String exported) {
        publicPkgs.add(exported);
    }
    
    public String[] getPublicInterfaces() {
        return publicPkgs.toArray(new String[publicPkgs.size()]);
    }
    
    public void addDependency(ModuleDependency dependent) {
        dependencies.add(dependent);
    }
    
    public ModuleDependency[] getDependencies() {
        return dependencies.toArray(new ModuleDependency[dependencies.size()]);
    }

    public void add(List<URI> extraClassPath) {
        classPath.addAll(extraClassPath);
    }
    
}
