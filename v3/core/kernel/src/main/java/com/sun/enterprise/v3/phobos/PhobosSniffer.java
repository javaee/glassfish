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

import com.sun.enterprise.v3.deployment.GenericSniffer;
import org.glassfish.api.container.Sniffer;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;
import org.glassfish.api.deployment.Deployer;
import org.jvnet.hk2.annotations.Service;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Phobos sniffer to set up phobos container correctly
 *
 * @author Jerome Dochez
 */
@Service
public class PhobosSniffer extends GenericSniffer implements Sniffer {

    private Logger logger=null;
    
    public PhobosSniffer() {
        super("phobos", "application/startup.js", null);
    }
    
   /**
     * Sets up the container libraries so that any imported bundle from the
     * connector jar file will now be known to the module subsystem
     * @param containerHome is where the container implementation resides
     * @param logger the logger to use
     * @throws java.io.IOException exception if something goes sour
     */
    @Override
    public Module setup(String containerHome, Logger logger) throws IOException {
        // In most cases, the location of the jar files for a
        // particular container is in <containerHome>/lib.
        if (!(new File(containerHome).exists())) {
            throw new FileNotFoundException(getModuleType() + " container not found at " + containerHome);
        }
        this.logger = logger;
        try {
            File libDirectory = new File(containerHome, "lib");
            if (!libDirectory.exists()) {
                logger.warning(getModuleType() + " container does not have a lib directory");
                return null;
            }
            PhobosRepository containerRepo = new PhobosRepository(libDirectory.getAbsolutePath());
            containerRepo.initialize();
            modulesRegistry.addRepository(containerRepo);

            Module phobosConnector = modulesRegistry.makeModuleFor("com.sun.enterprise.glassfish:gf-phobos-connector", null);
            phobosConnector.addImport(modulesRegistry.makeModuleFor("phobos-runtime.jar",null));
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Cannot set up the repository for the container", e);
            throw e;
        }
       return null;
    }

    final String[] containers = { "com.sun.enterprise.phobos.PhobosContainer" };
        
    public String[] getContainersNames() {
        return containers;
    }
}
