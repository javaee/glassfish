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
 * ConnectionShutdownUtil.java
 *
 * Created on February 27, 2003, 4:45 PM
 */

package com.sun.enterprise.web;

import java.util.logging.*;
import com.sun.logging.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.ArrayList;
import org.apache.catalina.Context;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Manager;
import org.apache.catalina.Valve;
import org.apache.catalina.core.ContainerBase;
import java.sql.Connection;

import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.Switch;

/**
 *
 * @author  lwhite
 */
public class ConnectionShutdownUtil {
    
    /**
     * The embedded Catalina object.
     */
    protected final EmbeddedWebContainer _embedded;
    private static final Logger _logger;
    private WebContainer webContainer = null;
    static
    {
            _logger=LogDomains.getLogger(LogDomains.WEB_LOGGER);
    }     
    
    /** Creates a new instance of ConnectionShutdownUtil */
    public ConnectionShutdownUtil() {
        _embedded= null;
    }
    
    /** Creates a new instance of ConnectionShutdownUtil */
    /*
    public ConnectionShutdownUtil(Hashtable instances)  {
        _instances = instances;
    }
     */
    
    /** Creates a new instance of ConnectionShutdownUtil */
    public ConnectionShutdownUtil(EmbeddedWebContainer embedded)  {
        _embedded = embedded;
    }     
    
    public String getApplicationId(Context ctx) {
        com.sun.enterprise.web.WebModule wm = 
            (com.sun.enterprise.web.WebModule)ctx;
        return wm.getID();
    }
     
    public String getApplicationName(Context ctx) {
        return ctx.getName();
    }      
        
    public ArrayList runGetShutdownCapables() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN ConnectionShutdownUtil:runGetShutdownCapables");
        }

        //each element of this list will contain
        //a list of connections
        ArrayList shutdownCapablesList = new ArrayList();

        try {
            Engine[] engines = _embedded.getEngines();
            
            for(int h=0; h<engines.length; h++) {
                Container engine = (Container) engines[h];            
                Container[] hosts = engine.findChildren();
                for(int i=0; i<hosts.length; i++) {
                    Container nextHost = hosts[i];
                    Container [] webModules = nextHost.findChildren();
                    for (int j=0; j<webModules.length; j++) {
                        Container nextWebModule = webModules[j];
                        Context ctx = (Context)nextWebModule;
                        //this code gets managers
                        String webAppName = this.getApplicationId(ctx);
                        Manager nextManager = nextWebModule.getManager();
                        if(nextManager instanceof ShutdownCleanupCapable) {
                            if(_logger.isLoggable(Level.FINEST)) {
                                _logger.finest(
                                    "found a shutdown capable manager:"
                                    + nextManager.getClass().getName());
                            }
                            shutdownCapablesList.add(nextManager);
                        }
                    }

                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("1) shutdownCapablesList Size = "
                                       + shutdownCapablesList.size());
                    }

                    //now get any shutdown capable valves installed in virtual hosts
                    Valve[] valves = ((ContainerBase)nextHost).getValves();                    
                    for(int k=0; k<valves.length; k++) {
                        Valve nextValve = valves[k];
                        if(_logger.isLoggable(Level.FINEST)) {
                            _logger.finest("VALVE = "
                                           + nextValve.getClass().getName());
                        }
                        if(nextValve instanceof ShutdownCleanupCapable) {
                            if(_logger.isLoggable(Level.FINEST)) {
                                _logger.finest("found a shutdown capable valve");
                            }
                            shutdownCapablesList.add(nextValve);
                        }                       
                    }
                }                 
            }
        } catch (Throwable th) {
            _logger.log(Level.SEVERE, "Exception thrown", th);
        }

        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("1) shutdownCapablesList Size = "
                           + shutdownCapablesList.size());
        }

        return shutdownCapablesList;
                
    }
    
    public ArrayList runGetShutdownCapablesForAppName(String appName) {
        //FIXME this is not returning valves
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN ConnectionShutdownUtil:runGetShutdownCapablesForAppName");
        }

        //each element of this list will contain
        //a list of connections
        ArrayList shutdownCapablesList = new ArrayList();
        try {
            Engine[] engines = _embedded.getEngines();
            
            for(int h=0; h<engines.length; h++) {
                Container engine = (Container) engines[h];             
                Container[] hosts = engine.findChildren();
                for(int i=0; i<hosts.length; i++) {
                    Container nextHost = hosts[i];
                    Container [] webModules = nextHost.findChildren();
                    for (int j=0; j<webModules.length; j++) {
                        Container nextWebModule = webModules[j];
                        Context ctx = (Context)nextWebModule;
                        String webAppName = this.getApplicationName(ctx);
                        //this code gets managers
                        Manager nextManager = nextWebModule.getManager();
                        if(nextManager instanceof ShutdownCleanupCapable) {
                            if(webAppName.equals(appName)) {
                                if(_logger.isLoggable(Level.FINEST)) {
                                    _logger.finest(
                                        "found a shutdown capable manager:"
                                        + nextManager.getClass().getName());
                                }
                                shutdownCapablesList.add(nextManager);
                            }
                        }
                    }

                }                 
            }
        } catch (Throwable th) {
            _logger.log(Level.SEVERE, "Exception thrown", th);
        }

        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("1) shutdownCapablesList Size = "
                           + shutdownCapablesList.size());
        }

        return shutdownCapablesList;
                
    }    
    
    public void runShutdownCleanupFromShutdownCleanupCapableList(ArrayList shutdownCapablesList) {
        for(int i=0; i<shutdownCapablesList.size(); i++) {
            ShutdownCleanupCapable nextOne = (ShutdownCleanupCapable)shutdownCapablesList.get(i);
            nextOne.doShutdownCleanup();
        }
    }
    
    public void runCloseAllConnections() {
        ArrayList list = this.runGetShutdownCapables();
        this.runShutdownCleanupFromShutdownCleanupCapableList(list);
    }
    
    public void runCloseCachedConnectionForApp(String appName) {
        ArrayList list = this.runGetShutdownCapablesForAppName(appName);
        this.runCloseCachedConnectionFromShutdownCleanupCapableList(list, appName);
    } 
    
    public void runCloseCachedConnectionFromShutdownCleanupCapableList(ArrayList shutdownCapablesList, String appName) {
        for(int i=0; i<shutdownCapablesList.size(); i++) {
            ShutdownCleanupCapable nextOne = (ShutdownCleanupCapable)shutdownCapablesList.get(i);
            nextOne.doCloseCachedConnection();
        }
    }    

    public void clearoutJDBCPool() {
        //this will clear out the HADB connection pool
        String hadbPoolname = this.getHadbJdbcConnectionPoolNameFromConfig();
        if(hadbPoolname == null) {
            return;
        }
        ResourceSpec resourceSpec = new ResourceSpec(hadbPoolname,
            ResourceSpec.JNDI_NAME);
        resourceSpec.setConnectionPoolName(hadbPoolname);
        if(resourceSpec == null) {
            return;
        }
        Switch.getSwitch().getPoolManager().emptyResourcePool(resourceSpec);
        resourceSpec = new ResourceSpec(hadbPoolname, ResourceSpec.JDBC_URL);
        resourceSpec.setConnectionPoolName(hadbPoolname);
        if(resourceSpec == null) {
            return;
        }        
        Switch.getSwitch().getPoolManager().emptyResourcePool(resourceSpec);
    }
    
    private String getHadbJdbcConnectionPoolNameFromConfig() {
        ServerConfigLookup configLookup = new ServerConfigLookup();
        return configLookup.getHadbJdbcConnectionPoolNameFromConfig();
    }     
    
}


