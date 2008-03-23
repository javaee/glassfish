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

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;
import org.glassfish.api.Startup;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.Singleton;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * that sucks, I have to initialize the derby in a special thread otherwise it
 * will prevent gc of whichever class loader initialized it. In this case, I use
 * the core system class loader which is not supposed to ever be gc'ed.
 *
 * @author Jerome Dochez
 */
//@Service(scope= Singleton.class)
public class DatabaseService implements Startup, PostConstruct {
    

    @Inject
    ModulesRegistry systemRegistry;

    @Inject
    Logger logger;
    
    public void postConstruct() {

        final Module module = systemRegistry.makeModuleFor("shared", null);

        // this should go to a lifecycle but since I cannot modify the derby.jar...
        // it ends up here
            Thread thread = new Thread() {
                public void run() {
                    try {
                        try {
                            Class driverClass = module.getClassLoader().loadClass("org.apache.derby.jdbc.EmbeddedDriver");
                            driverClass.newInstance();
                        } catch(ClassNotFoundException e) {
                            logger.log(Level.SEVERE, "Cannot load Derby Driver ",e);
                        } catch(java.lang.InstantiationException e) {
                            logger.log(Level.SEVERE, "Cannot instantiate Derby Driver", e);
                        } catch(IllegalAccessException e) {
                            logger.log(Level.SEVERE, "Cannot instantiate Derby Driver", e);
                        }
                    }
                    catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
    }
    
    public String toString() {
        return "Forced derby initialization";
    }
    
    public Lifecycle getLifecycle() {
        return Lifecycle.START;
    }
}
