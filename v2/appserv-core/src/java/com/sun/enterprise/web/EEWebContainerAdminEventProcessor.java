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
 * EEWebContainerAdminEventProcessor.java
 *
 * Created on September 26, 2003, 4:51 PM
 */

package com.sun.enterprise.web;

import java.util.Hashtable;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ResourceBundle;

import com.sun.logging.LogDomains;

import com.sun.enterprise.admin.event.ApplicationDeployEvent;
import com.sun.enterprise.admin.event.ModuleDeployEvent;

/**
 *
 * @author  lwhite
 */
public class EEWebContainerAdminEventProcessor implements WebContainerAdminEventProcessor {
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    protected static Logger _logger = null;
    
    /**
     * The resource bundle containing the message strings for _logger.
     */
    protected static ResourceBundle _rb = null;    
    
    /**
     * The embedded Catalina object.
     */
    protected EmbeddedWebContainer _embedded = null; 
    
    /**
     * Hashtable to keep track of deploy / undeploy events timing
     * HERCULES:add
     */    
    private static Hashtable _deployHistory = new Hashtable();
    
    /** Creates a new instance of EEWebContainerAdminEventProcessor */
    public EEWebContainerAdminEventProcessor() {
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
            _rb = _logger.getResourceBundle();
        }        
    }    
    
    /** Creates a new instance of EEWebContainerAdminEventProcessor */
    public EEWebContainerAdminEventProcessor(EmbeddedWebContainer embedded) {
        _embedded = embedded;
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
            _rb = _logger.getResourceBundle();
        }        
    }
    
    public void init(EmbeddedWebContainer embedded) {
        _embedded = embedded;
    }    
    
    public void applicationDeployed(ApplicationDeployEvent deployEvent) {
        String deployedAppName = deployEvent.getApplicationName();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("applicationDeployed:" + deployedAppName);
        }

        String key = "App_" + deployedAppName;        
        boolean deployedRecently = checkDeployHistoryEntry(key, "deployed");
        setDeployHistoryEntry(key, "deployed", System.currentTimeMillis());
        if (deployedRecently) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Returning from MIDDLE of applicationDeployed");
            }
            return;
        }        
    }
    
    public void applicationDisabled(ApplicationDeployEvent deployEvent) {
    }
    
    public void applicationEnabled(ApplicationDeployEvent deployEvent) {
    }
    
    public void applicationRedeployed(ApplicationDeployEvent deployEvent) {
        String deployedAppName = deployEvent.getApplicationName();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("applicationRedeployed:" + deployedAppName);
        }

        String key = "App_" + deployedAppName;
        boolean redeployedRecently = checkDeployHistoryEntry(key, "redeployed");
        setDeployHistoryEntry(key, "redeployed", System.currentTimeMillis());
        if (redeployedRecently) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Returning from MIDDLE of applicationRedeployed");
            }
            return;
        }
    }
    
    public void applicationUndeployed(ApplicationDeployEvent deployEvent) {
        String deployedAppName = deployEvent.getApplicationName();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("applicationUndeployed:" + deployedAppName);
        }

        String key = "App_" + deployedAppName;
        boolean undeployedRecently = checkDeployHistoryEntry(key, "undeployed");
        setDeployHistoryEntry(key, "undeployed", System.currentTimeMillis());
        if (undeployedRecently) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Returning from MIDDLE of applicationUndeployed");
            }
            return;
        }
    }
    
    public void moduleDeployed(ModuleDeployEvent deployEvent) {
        String deployedModuleName = deployEvent.getModuleName();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("moduleDeployed:" + deployedModuleName);
        }

        String key = "Mod_" + deployedModuleName;
        boolean deployedRecently = checkDeployHistoryEntry(key, "deployed");
        setDeployHistoryEntry(key, "deployed", System.currentTimeMillis());
        if (deployedRecently) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Returning from MIDDLE of moduleDeployed");
            }
            return;
        }
                
        ConnectionShutdownUtil shutdownUtil = new ConnectionShutdownUtil(_embedded);
        shutdownUtil.runCloseAllConnections();
        System.gc();
    }
    
    public void moduleDisabled(ModuleDeployEvent deployEvent) {
    }
    
    public void moduleEnabled(ModuleDeployEvent deployEvent) {
    }
    
    public void moduleRedeployed(ModuleDeployEvent deployEvent) {
        String deployedModuleName = deployEvent.getModuleName();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("moduleRedeployed:" + deployedModuleName);
        }

        String key = "Mod_" + deployedModuleName;
        boolean redeployedRecently = checkDeployHistoryEntry(key, "redeployed");
        setDeployHistoryEntry(key, "redeployed", System.currentTimeMillis());
        if (redeployedRecently) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Returning from MIDDLE of moduleRedeployed");
            }
            return;
        }
                
        ConnectionShutdownUtil shutdownUtil = new ConnectionShutdownUtil(_embedded);
        shutdownUtil.runCloseAllConnections();
        System.gc();
    }
    
    public void moduleUndeployed(ModuleDeployEvent deployEvent) {
        String deployedModuleName = deployEvent.getModuleName();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("moduleUndeployed:" + deployedModuleName);
        }

        String key = "Mod_" + deployedModuleName;
        boolean undeployedRecently = checkDeployHistoryEntry(key, "undeployed");
        setDeployHistoryEntry(key, "undeployed", System.currentTimeMillis());
        if (undeployedRecently) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Returning from MIDDLE of moduleUndeployed");
            }
            return;
        }

        ConnectionShutdownUtil shutdownUtil = new ConnectionShutdownUtil(_embedded);
        shutdownUtil.runCloseAllConnections();
        System.gc();
    }
    
    /**
     * set a deploy history entry
     * @param key identifies an application or module
     * @param value may have one of
     *                  the following values : 'deployed', 'undeployed', 'redeployed'.
     * @param lat the latency
     * HERCULES:add
     */
    private void setDeployHistoryEntry(String key, String value, long lat) {
        DeployHistoryEntry entry = (DeployHistoryEntry) _deployHistory.get(key);		if (entry == null)
            _deployHistory.put(key, new DeployHistoryEntry(value, lat));
        else {
            entry.value = value;
            entry.lat = lat;
        }
    } 
    
    /**
     * This method returns true if the application/module is attempted to be  
     * undeployed even though it was already undeployed, or is attempted to be 
     * (re)deployed when it was (re)deployed within the last 1 minute (60000 ms)
     * @param key identifies an application or module
     * @param deployStatus may have one of
     *                  the following values : 'deployed', 'undeployed', 'redeployed'.
     * HERCULES:add
     */    
    private boolean checkDeployHistoryEntry(String key, String deployStatus) {
        DeployHistoryEntry entry = (DeployHistoryEntry) _deployHistory.get(key);
        if (entry == null) {
            return false;
        }
        String val = entry.value;
        if (val.equals(deployStatus)) {
            /*if (deployStatus.equals("undeployed")) {
                    return true; // no need to check timestamp
            }*/
            if (( System.currentTimeMillis() - entry.lat ) < 60000) {
                    return true;
            }
        }
        return false;
    }    
    
}

/**
 * class used by WebContainer to properly handle timing of admin events
 * HERCULES:add
 */
class DeployHistoryEntry {
    String value;
    long lat;

    DeployHistoryEntry(String value, long lat) {
        this.value = value;
        this.lat = lat;
    }
}
