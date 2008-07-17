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

package com.sun.enterprise.v3.services.impl;

import org.glassfish.internal.api.Init;
import com.sun.enterprise.v3.server.ServerEnvironmentImpl;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.common_impl.DefaultModuleDefinition;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Startup service responsible for 
 */
@Service
public class CmdLineParamProcessor implements Init, PostConstruct {

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    ModulesRegistry registry;

    @Inject
    Logger logger;    

    public void postConstruct() {

        String lib = env.getStartupContext().getArguments().get("-gflib");
        if (lib==null) {
            return;
        }
        File f = new File(lib);
        if (!f.isAbsolute()) {
            f = new File(System.getProperty("user.dir"), lib);
        }
        if (!f.exists()) {
            logger.severe("File not found : " + f.getAbsolutePath());

        }
        try {
            ModuleDefinition moduleDef = new DefaultModuleDefinition(f);
            registry.add(moduleDef);
            logger.fine("Succesfully added library to module subsystem " + moduleDef.getName());
        } catch(IOException ioe) {
            logger.log(Level.SEVERE, "Exception with provided library : " + f.getName(), ioe);
        }        
    }
}
