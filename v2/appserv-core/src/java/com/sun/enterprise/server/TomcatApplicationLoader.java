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
 
package com.sun.enterprise.server;

import com.sun.logging.LogDomains;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.deployment.Application; 
import com.sun.enterprise.deployment.WebBundleDescriptor; 
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.web.PEWebContainer;
import com.sun.enterprise.Switch;
 
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Deployer;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardHost;

/**
 * This class extends ApplicationLoader to handle 
 * per application loading/unloading of web module.  
 *
 * @author  Amy Roh
 */
public class TomcatApplicationLoader extends ApplicationLoader {
 
 
    /** logger to log loader messages */
    static Logger _logger = LogDomains.getLogger(LogDomains.LOADER_LOGGER);
    
    
    // ------------------------------------------------------------ Constructor
 
    
    /**
     * TomcatApplicationLoader loads one application.
     *
     * @param appID              the name of the application 
     * @param parentClassLoader  parent class loader for this application
     * @param appsManager        the AppsManager for this VS
     */
    public TomcatApplicationLoader(String appID, ClassLoader parentClassLoader,
            AppsManager appsManager) {

        super(appID, parentClassLoader, appsManager);
        _logger.log(Level.FINEST, "[TomcatApplicationLoader] " + appID);
        this.appsManager = appsManager;
        // get the instance of WebContainer
        webContainer = PEWebContainer.getPEWebContainer();
        _logger.log(Level.FINEST, "PEWebContainer " + webContainer);

    }
    
    
    // ----------------------------------------------------- Instance Variables


    /** 
     *  The AppsManager for this VS
     *  save for unload() since super.unload() calls done()
     *  and clears this.configManager
     */
    private AppsManager appsManager = null;


    /**
     * The WebContainer instance.
     */
    private PEWebContainer webContainer = null;
    
    
    
    /**
     * Called from ApplicationManager. Called to load an application. 
     * This loads the web modules of this application on top of
     * its super loader creating the EJB and MDB container.
     *
     * @return    true if all modules were loaded successfully
     */
    protected boolean doLoad(boolean jsr77) {

        _logger.log(Level.FINEST, "[TomcatApplicationLoader] load " + jsr77);
        boolean deployed = super.doLoad(jsr77);
        if (loadUnloadAction == Constants.LOAD_RAR || ! deployed) {
            return deployed;
        }
        _logger.log(Level.FINEST, "deployed "+deployed);
        if (webContainer == null) {
            webContainer = PEWebContainer.getPEWebContainer();
        }
        if (deployed) {
            J2eeApplication[] j2eeAppBeans = appsManager.getAllApps();
            if (j2eeAppBeans != null) {
                for (int i = 0; i < j2eeAppBeans.length; i++) {
                    if (j2eeAppBeans[i].getName().equals(id) && webContainer != null) {
                        _logger.log(Level.FINEST, 
                                    "[TomcatApplicationLoader] loadJ2EEAppWebModule with "+j2eeAppBeans[i]);
                        webContainer.loadJ2EEApplicationWebModules(j2eeAppBeans[i]);
                       
                    }
                }
            }
        }
        return deployed;
        
    }

        
    /**
     * Unloads this application. 
     *
     * @return    true if all modules were removed successfully
     */
    protected boolean unload(boolean jsr77) {

        if (loadUnloadAction == Constants.UNLOAD_RAR) {
            return super.unload(jsr77);
        }

        super.unloadWebserviceEndpoints(jsr77);

        Set wbds = null; 
        J2eeApplication[] j2eeAppBeans = appsManager.getAllApps();        
        if (j2eeAppBeans != null) {
            for (int i = 0; i < j2eeAppBeans.length; i++) {
                if (j2eeAppBeans[i].getName().equals(id)) {

                    String virtualServers = null;
                    try {
                        virtualServers = appsManager.
                            getVirtualServersByAppName(j2eeAppBeans[i].getName());
                    } catch(ConfigException ce) {
                        _logger.log(Level.FINEST, "[TomcatApplicationLoader] unload " 
                            + id + ". error getting virtualServers", ce);
                    }
                    
                    _logger.log(Level.FINEST, "[TomcatApplicationLoader] unload "
                                               +id);
                    wbds = application.getWebBundleDescriptors();
                    WebBundleDescriptor wbd = null;
                    if ( wbds == null) continue;

                    Iterator itr = wbds.iterator();
                    
                    while (itr.hasNext()){
                        wbd = (WebBundleDescriptor) itr.next();
                        String appName = wbd.getApplication().getRegistrationName();
                        try {
			    webContainer.unloadWebModule(wbd.getContextRoot(),
                                                         appName,
                                                         virtualServers,
                                                         wbd);
                        
			} finally {
			    try {
				Switch.getSwitch().
				    getNamingManager().unbindObjects(wbd);
			    } catch (javax.naming.NamingException nameEx) {
				_logger.log(Level.FINEST, "[TomcatApplicationLoader] "
					+ " Exception during namingManager.unbindObject",
					nameEx);
			    }
			}
                    }

                }
            }    
        }

        return super.unload(jsr77);
    }
}
