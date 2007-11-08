/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.enterprise.jbi.serviceengine.bridge;

import com.sun.appserv.server.ServerLifecycle;
import com.sun.appserv.server.ServerLifecycleException;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.jbi.serviceengine.install.Installer;
import com.sun.logging.LogDomains;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.jbi.serviceengine.install.ServiceEngineObjectFactoryImpl;
import com.sun.enterprise.server.event.ApplicationLoaderEventNotifier;
import com.sun.enterprise.webservice.ServiceEngineUtil;
import java.net.InetAddress;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Manisha Umbarje
 */
public class JavaEEServiceEngineLifeCycle implements ServerLifecycle {
    
    
    private static Logger logger = LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    public String DEFAULT_COMPONENT_NAME ="sun-javaee-engine";
    
    
    /** Creates a new instance of JavaEEServiceEngineLifeCycle */
    public JavaEEServiceEngineLifeCycle() {
    }
    
    /**
     * Server is initializing subsystems and setting up the runtime environment.
     * Prepare for the beginning of active use of the public methods of this
     * subsystem. This method is called before any of the public methods of
     * this subsystem are utilized.
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception IllegalStateException if this subsystem has already been
     *  started
     * @exception ServerLifecycleException if this subsystem detects a fatal
     *  error that prevents this subsystem from being used
     */
    public void onInitialization(ServerContext sc)
    throws ServerLifecycleException {
        logger.log(Level.FINEST, "se_lifecycle_initializing");
        /*ApplicationLoaderEventNotifier.getInstance().
                        addListener(ApplicationLoaderEventListenerImpl.getInstance());*/
        
    }
    
    /**
     * Server is starting up applications
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal
     *  error that prevents this subsystem from being used
     */
    public void onStartup(ServerContext sc)
    throws ServerLifecycleException {
        logger.log(Level.FINEST, "se_lifecycle_starting");
    }
    /**
     * Server has complted loading the applications and is ready to serve requests.
     *
     * @param sc ServerContext the server runtime context.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal
     *  error that prevents this subsystem from being used
     */
    public void onReady(ServerContext sc) throws ServerLifecycleException {
        
        
        
        Installer installer =
                ServiceEngineObjectFactoryImpl.getInstance().
                createInstaller();
        if(installer.isJBIInstalled()) {
            installer.setComponentName(DEFAULT_COMPONENT_NAME);
            try {
                boolean installedFlag = installer.isComponentInstalled();
                logger.log(Level.FINE, "Is Java EE Service Engine installed " + installedFlag);
                
                if(ServiceEngineUtil.isServiceEngineEnabled()) {
                    //Assumption here is if service engine is already installed,
                    // It will be resumed in its state by the JBI framework
                    if(!installedFlag) {
                        installer.install(null);
                        installer.start();
                    }
                }else {
                    logger.log(Level.FINEST, "Java EE Service Engine is disabled");
                    if(installedFlag) {
                        // If service engine is disabled, stop the service engine
                        try {
                            installer.stop();
                        } catch(Exception e) {
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Server is shutting down applications
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal
     *  error that prevents this subsystem from being used
     */
    public void onShutdown()
    throws ServerLifecycleException {
    }
    /**
     * Server is terminating the subsystems and the runtime environment.
     * Gracefully terminate the active use of the public methods of this
     * subsystem.  This method should be the last one called on a given
     * instance of this subsystem.
     *
     * @exception ServerLifecycleException if this subsystem detects a fatal
     *  error that prevents this subsystem from being used
     */
    public void onTermination()
    throws ServerLifecycleException {
        
    }
}
