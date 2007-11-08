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

/*
 * ManagerFactory.java
 *
 * Created on March 28, 2003, 2:20 PM
 */

package com.sun.enterprise.server;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.instance.InstanceFactory;
import com.sun.enterprise.instance.InstanceEnvironment;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.EjbModulesManager;
import com.sun.enterprise.instance.WebModulesManager;
import com.sun.enterprise.instance.ConnectorModulesManager;
import com.sun.enterprise.instance.AppclientModulesManager;
import com.sun.appserv.server.util.PreprocessorUtil;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;



/**
 *
 * @author  Sandhya E
 */
public final class ManagerFactory {
    private ManagerFactory()    { /* preclude instantiation */ }
    
    /** J2EE Application Manager*/
    private static ApplicationManager applicationManager;
    /** Stand alone Connector module manager*/
    private static StandAloneConnectorModulesManager saConnectorManager;
    /** Stand alone Web module manager*/
    private static DummyWebModuleManager saWebManager;
    /** Stand alone Ejb module manager*/
    private static StandAloneEJBModulesManager saEJBManager;
    /** Stand alone Application Client module manager*/
    private static StandAloneAppClientModulesManager saACManager;
    /** Instance environment of current server context */
    private static InstanceEnvironment iEnv;
    /** shared class loader of current server context */
    private static ClassLoader sharedCL;
    
    /** logger to log core messages */
    private static final Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
    
    /**
     * This method gets the Application Manager for this server context
     * @return ApplicationManager
     */
    public static synchronized ApplicationManager getApplicationManager() throws ConfigException{
        if(applicationManager == null) {    
            AppsManager appsManager =
            InstanceFactory.createAppsManager(getInstanceEnvironment(), false);
            try {
                if (appsManager.isByteCodePreprocessingEnabled()){
                    // Initialize the preprocessor.  If for some reason there's a
                    // problem, the preprocessor will be disabled before we attempt
                    // to use it.
                    PreprocessorUtil.init
                    (appsManager.getBytecodeProcessorClassNames());
                }
            } catch (ConfigException confEx) {
                _logger.log(Level.WARNING,
                            "bytecodepreprocessor.config_ex",
                            confEx);
                _logger.log(Level.WARNING,
                            "bytecodepreprocessor.disabled");
            }
            
            // manager for the j2ee applications
            applicationManager = new ApplicationManager(appsManager,getSharedClassLoader());
        }
        return applicationManager;    
    }
    
    /**
     * This method returns the stand alone connector module manager
     * @return StandAloneConnectorModulesManager
     */
    public static synchronized StandAloneConnectorModulesManager getSAConnectorModulesManager() throws ConfigException {
        if(saConnectorManager == null) {
            // config manager for stand alone connector modules
            ConnectorModulesManager connModuleManager =
            InstanceFactory.createConnectorModuleManager(getInstanceEnvironment(), false);
            
            // manager for stand alone connector modules
            saConnectorManager = new StandAloneConnectorModulesManager(
                                               connModuleManager, getSharedClassLoader());
        }
        return saConnectorManager;
    }
    
    /**
     * This method returns the *dummy* stand alone web modules manager. The returned manager
     * does not anything but sending an event to ondemand initialization framework.
     * @return StandAloneWebModulesManager
     **/
    public static synchronized DummyWebModuleManager getSAWebModulesManager() throws ConfigException {
        if(saWebManager == null) {
            // config manager for stand alone web modules
            WebModulesManager webModuleManager = new WebModulesManager(getInstanceEnvironment());
            saWebManager = new WebModuleDeployEventListener(webModuleManager, getSharedClassLoader());           
        }

        return saWebManager;
    }
    
    /**
     * This method returns the stand alone web modules manager
     * @return StandAloneEjbModulesManager
     */
    public static synchronized StandAloneEJBModulesManager getSAEJBModulesManager() throws ConfigException {
        if(saEJBManager == null) {
            // config manager for stand alone ejb modules
            EjbModulesManager ejbModuleManager =
            InstanceFactory.createEjbModuleManager(getInstanceEnvironment(), false);
            
            // manager for stand alone ejb modules
            saEJBManager = new StandAloneEJBModulesManager(ejbModuleManager, getSharedClassLoader());
        }
        return saEJBManager;
    }

    /**
     * This method returns the stand alone application client module manager
     * @return StandAloneAppClientModulesManager
     */
    public static synchronized StandAloneAppClientModulesManager getSAACModulesManager() 
	throws ConfigException {

        if(saACManager == null) {

            // config manager for stand alone application client modules
	    AppclientModulesManager acModuleManager =
		InstanceFactory.createAppclientModulesManager(getInstanceEnvironment());
            
            // manager for stand alone application client modules
            saACManager = new StandAloneAppClientModulesManager(
					acModuleManager, getSharedClassLoader());
        }
        return saACManager;
    }
    
    /**
     * Returns the instance environment of this server's runtime
     * @return InstanceEnvironment
     */
    private static synchronized InstanceEnvironment getInstanceEnvironment() {
        if(iEnv == null) {
            ServerContext sc = ApplicationServer.getServerContext();
            iEnv = sc.getInstanceEnvironment();
        }
        return iEnv;
    }
    
    /**
     * Returns the shared class loader
     * @return ClassLoader
     **/
    private static synchronized ClassLoader getSharedClassLoader() {
        if(sharedCL == null) {
            ServerContext sc = ApplicationServer.getServerContext();
            sharedCL = sc.getSharedClassLoader();            
        }
        return sharedCL;
    }
    
        
}
