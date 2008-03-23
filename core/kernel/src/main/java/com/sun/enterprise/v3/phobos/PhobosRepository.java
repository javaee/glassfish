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

package com.sun.enterprise.v3.phobos;

import com.sun.enterprise.module.ManifestConstants;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModuleDependency;
import com.sun.enterprise.module.RepositoryChangeListener;
import com.sun.enterprise.module.impl.CookedLibRepository;
import com.sun.enterprise.module.impl.CookedModuleDefinition;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.net.URI;

/**
 * Until phobos becomes modular, I have no choice than cooking the module 
 * definition
 *
 * @author dochez
 */
public class PhobosRepository extends CookedLibRepository {
    
    List<ModuleDefinition> moduleDefs = new ArrayList<ModuleDefinition>();
    
    /** Creates a new instance of WebServicesRepository */
    public PhobosRepository(String installRoot) {
        super(installRoot);
        System.out.println("Phobos installation at " + installRoot);
    }
    
    /**
     * Initialize the repository for use. This need to be called at least
     * once before any find methods is invoked.  
     * @throws IOException if an error occur accessing the repository
     */
    public void initialize() throws IOException {
                
        Attributes phobosAttr = new Attributes();
        phobosAttr.putValue(Attributes.Name.CLASS_PATH.toString(), "rome.jar, xmlbeans.jar,  phobos-debugger-api.jar");

        CookedModuleDefinition phobos = new CookedModuleDefinition(
                new File(rootLocation, "phobos-runtime.jar"), phobosAttr);
        phobos.addDependency(new ModuleDependency("com.sun.enterprise.glassfish:webtier", null));     
        //phobos.addDependency(new ModuleDependency("derby.jar", null));      
        phobos.addDependency(new ModuleDependency("phobos-rhino.jar", null));
        phobos.addDependency(new ModuleDependency("freemarker-2.3.6.jar", null));     
        phobos.addDependency(new ModuleDependency("logging-api-1.0.4.jar", null));        
        phobos.addDependency(new ModuleDependency("jdom-1.0.jar", null));                
        moduleDefs.add(phobos);
        

        CookedModuleDefinition freemarker = new CookedModuleDefinition(
                new File(rootLocation, "freemarker-2.3.6.jar"), null);
        freemarker.addDependency(new ModuleDependency("phobos-rhino.jar", null));
        moduleDefs.add(freemarker);
        
        CookedModuleDefinition jsr223 = new CookedModuleDefinition(
        new File(rootLocation, "jsr223-api.jar"), null);
        moduleDefs.add(jsr223);     
        
        CookedModuleDefinition rhino = new CookedModuleDefinition(
        new File(rootLocation, "phobos-rhino.jar"), null);
        rhino.addDependency(new ModuleDependency("jsr223-api.jar", null));            
        moduleDefs.add(rhino);        
        
        CookedModuleDefinition jdom = new CookedModuleDefinition(
        new File(rootLocation, "jdom-1.0.jar"), null);
        moduleDefs.add(jdom);
        
        Attributes commonsAttr = new Attributes();
        commonsAttr.putValue(Attributes.Name.CLASS_PATH.toString(), "logging-api-1.0.4.jar");
        CookedModuleDefinition commons = new CookedModuleDefinition(
        new File(rootLocation, "logging-api-1.0.4.jar"), commonsAttr);
        moduleDefs.add(commons);
    }

    public List<URI> getJarLocations() {
        return null;
    }

    public boolean addListener(RepositoryChangeListener repositoryChangeListener) {
        return false;
    }

    public boolean removeListener(RepositoryChangeListener repositoryChangeListener) {
        return false;
    }

    public List<ModuleDefinition> findAll() {
        return moduleDefs;
    }  
    
    /**
     * Returns the repository name
     * @return repository name
     */
    public String getName() {
        return "phobos";
    }    
    
    
}
