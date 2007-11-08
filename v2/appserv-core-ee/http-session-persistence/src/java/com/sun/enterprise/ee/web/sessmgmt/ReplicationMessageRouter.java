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
 * ReplicationMessageRouter.java
 *
 * Created on October 12, 2006, 12:03 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import org.apache.catalina.Context;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Manager;
import org.apache.catalina.Valve;
import org.apache.catalina.core.ContainerBase;

import com.sun.enterprise.ee.web.sessmgmt.RepairAgent;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationState;

import com.sun.enterprise.web.EmbeddedWebContainer;

/**
 *
 * @author Larry White
 */
public class ReplicationMessageRouter implements RepairAgent {
    
    public final static String LOGGER_MEM_REP 
        = ReplicationState.LOGGER_MEM_REP;    
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    //protected static final Logger _logger 
    //    = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    private static final Logger _logger 
        = Logger.getLogger(LOGGER_MEM_REP);    
        
    /** 
     * applicationId vs. logical ReplicationManager
     * for web: web appid vs. Manager
     * for sso: virtual server id vs. SingleSignOn valve
     * for ejb: containerid vs. container 
     */
    protected static final ConcurrentHashMap _appId2Container 
        = new ConcurrentHashMap();
    
    private Set replicationManagerSet = new HashSet();
    
    /**
     * The singleton instance of ReplicationMessageRouter
     */    
    private static ReplicationMessageRouter _soleInstance = null;
    
    /**
     * The embedded Catalina object.
     */
    protected EmbeddedWebContainer _embedded = null;     
    
    /**
     * a monitor obj for synchronization
     */    
    private static final Object _monitor = new Object();    
    
    /** Return the singleton instance
     *  assume it already exists
     */
    public static ReplicationMessageRouter createInstance() {
        synchronized (_monitor) {
            if (_soleInstance == null) {
                _soleInstance = new ReplicationMessageRouter();
            }
        }        
        return _soleInstance;
    }    
    
    /** Return the singleton instance
     *  lazily creates a new instance of ReplicationMessageRouter if not created yet
     * @param embedded the embedded web container
     */
    public static ReplicationMessageRouter createInstance(EmbeddedWebContainer embedded) {
        synchronized (_monitor) {
            if (_soleInstance == null) {
                _soleInstance = new ReplicationMessageRouter(embedded);
            } else {
                _soleInstance._embedded = embedded;
            }
        }
        return _soleInstance;
    } 
    
    /** Creates a new instance of ReplicationMessageRouter 
     * @param the embedded web container
     */
    public ReplicationMessageRouter(EmbeddedWebContainer embedded) {
        _embedded = embedded;        
    } 
    
    /** Creates a new instance of ReplicationMessageRouter 
     */
    public ReplicationMessageRouter() {       
    }     
    
    public void addReplicationManager(String appid, ReplicationManager container) {
        _appId2Container.put(appid, container);
        replicationManagerSet.add(container);
    }
    
    public ReplicationManager removeReplicationManager(String appid) {
        ReplicationManager removedMgr = (ReplicationManager) _appId2Container.remove(appid);
        replicationManagerSet.remove(removedMgr);
        return removedMgr;        
    }
    
    private ReplicationManager getReplicationManager(String appid) {
        return (ReplicationManager) _appId2Container.get(appid);
    } 
    
    public ReplicationManager[] getReplicationManagerArray() {
        List replicationManagerList 
            = new ArrayList(replicationManagerSet.size());
        Iterator it = replicationManagerSet.iterator();
        while(it.hasNext()) {
            ReplicationManager nextReplicationManager = (ReplicationManager)it.next();
            replicationManagerList.add(nextReplicationManager);
        }
        ReplicationManager results[] = null;
        results = new ReplicationManager[replicationManagerSet.size()];
        results = (ReplicationManager[]) replicationManagerList.toArray(results);
        return (results);
    }   
    
    /**
     * Returns all the applicationIds available in this registry.
     *
     * @return   a collection of applicationIds
     */
   public static Enumeration getReplicationAppIds(boolean printIt) {
        Enumeration applicationIds = null;
        if (_appId2Container != null) {
            synchronized(_appId2Container) {
                applicationIds = _appId2Container.keys();
            }
        }
        //for testing
        if(printIt) {
            int i=0;
            while(applicationIds.hasMoreElements()) {
                String nextAppId = (String)applicationIds.nextElement();
                System.out.println("appid-key[" + i + "]= " + nextAppId);
                System.out.println("appid-key-length[" + i + "]= " + nextAppId.length());
                i++;
            }
        }
        //end for testing        
        return applicationIds;
    }
    
    public ReplicationManager findApp(String appName) /*throws IOException*/ { 
        //System.out.println("ReplicationMessageRouter>>findApp:appName = " + appName);
        //System.out.println("ReplicationMessageRouter>>findApp:appName length = " + appName.length());
        //getReplicationAppIds(true);
        if (appName == null)
            return (null);
        ReplicationManager mgr = getReplicationManager(appName);
        //System.out.println("ReplicationMessageRouter>>findApp:mgr = " + mgr);
        return (mgr);
    }
    
    public void repairApps(long repairStartTime) {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicationMessageRouter:repairApps");
        }
        eagerlyLoadRoutingMap();
        //System.out.println("ReplicationMessageRouter:repairApps");
        ReplicationManager[] apps = this.getReplicationManagerArray();
        for(int i=0; i<apps.length; i++) {
            if(ReplicationHealthChecker.isStopping()) {
                break;
            }
            ReplicationManager nextMgr = apps[i];
            nextMgr.repair(repairStartTime);
        }
    }
    
    public void repairApps(long repairStartTime, boolean checkForStopping) {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicationMessageRouter:repairApps: checkForStopping: " + checkForStopping);
        }
        if(!checkForStopping) {
            eagerlyLoadRoutingMap();
        }
        //System.out.println("ReplicationMessageRouter:repairApps: checkForStopping: " + checkForStopping);
        ReplicationManager[] apps = this.getReplicationManagerArray();
        //System.out.println("ReplicationMessageRouter:apps length: " + apps.length);
        for(int i=0; i<apps.length; i++) {
            if(checkForStopping && ReplicationHealthChecker.isStopping()) {
                break;
            }
            ReplicationManager nextMgr = apps[i];
            //System.out.println("ReplicationMessageRouter:repairApps: nextMgr: " + nextMgr);
            nextMgr.repair(repairStartTime, checkForStopping);
        }
    }    
    
    public void processMessage(ReplicationState state) {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicationMessageRouter:processMessage:state=" + state);
        }        
        //System.out.println("ReplicationMessageRouter:processMessage:state=" + state);
        //send ack if not a return msg and is a void return
        if(!state.isReturnMessage() && state.isVoidMethodReturnState()) {
            //FIXME: can send acknowledgement back immediately
        }
        //this is used only for health check message
        if(state.isHCReturnMessage()) {            
            //bypass normal routing in this case that is
            //why we return here
            processResponse(state);
            return;
        }
        //otherwise do normal routing to app
        this.routeMessageForApp(state.getAppId(), state);
    }
    
    public void processResponse(ReplicationState message) {
        //complete processing response - not sending response to a response
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN" + this.getClass().getName() + ">>processResponse");            
        }        
        ReplicationResponseRepository.putEntry(message);
    }    
    
    public void routeMessageForApp(String appName, ReplicationState message) {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicationMessageRouter:routeMessageForApp" + appName);
        }        
        //System.out.println("ReplicationMessageRouter:routeMessageForApp: " + appName);
        ReplicationManager mgr = null;
        if((mgr = this.findApp(appName)) != null) {
            mgr.processMessage(message);
            return;
        }
        //if _embedded not (yet) initialized - just return
        if (_embedded == null) {
            return;
        }
        boolean continueProcessing = true;
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
                        Manager nextManager = nextWebModule.getManager();                       
                        if(nextManager instanceof ReplicationManager) {
                            //let this manager process the message 
                            //if it is a ReplicationManager
                            //and app names match
                            String nextAppName = ((ReplicationManagerBase)nextManager).getApplicationId();
                            //System.out.println("ReplicationMessageRouter:nextAppName = " + nextAppName + ", appName = " + appName);
                            if (_logger.isLoggable(Level.FINEST)) {
                                _logger.finest("ReplicationMessageRouter:routeMessageForApp:nextAppName = " + nextAppName + ", appName = " + appName);
                            }                            
                            if(nextAppName.equals(appName)) {
                                if (_logger.isLoggable(Level.FINEST)) {
                                    _logger.finest("ReplicationMessageRouter:routeMessageForApp:found our manager:" + nextManager.getClass().getName());
                                }                                                                
                                this.addReplicationManager(appName, (ReplicationManager)nextManager);
                                ((ReplicationManager)nextManager).processMessage(message);
                                continueProcessing = false;
                                break;
                            }
                        }
                    }
                    if(!continueProcessing) {
                        break;
                    }
                    //now get ReplicationManager valves installed in virtual hosts
                    Valve[] valves = ((ContainerBase)nextHost).getValves();                   
                    for(int k=0; k<valves.length; k++) {
                        Valve nextValve = valves[k];
                        if (_logger.isLoggable(Level.FINEST)) {
                            _logger.finest("ReplicationMessageRouter:routeMessageForApp:VALVE = " + nextValve.getClass().getName());
                        }                                            
                        //System.out.println("ReplicationMessageRouter:routeMessageForApp:VALVE = " + nextValve.getClass().getName());                         
                        if(nextValve instanceof ReplicationManager) {
                            //let this manager process the message 
                            //if it is a ReplicationManager
                            //and app names match
                            String nextAppName = ((ReplicationManager)nextValve).getApplicationId();
                            if (_logger.isLoggable(Level.FINEST)) {
                                _logger.finest("ReplicationMessageRouter:routeMessageForApp:nextAppName = " + nextAppName + ", appName = " + appName); 
                            }                             
                            //System.out.println("ReplicationMessageRouter:routeMessageForApp:nextAppName = " + nextAppName + ", appName = " + appName);                            
                            if(nextAppName.equals(appName)) {
                                if (_logger.isLoggable(Level.FINEST)) {
                                    _logger.finest("ReplicationMessageRouter:routeMessageForApp:found our manager:" + nextValve.getClass().getName());
                                }                                                                 
                                //System.out.println("ReplicationMessageRouter:routeMessageForApp:found our manager valve:" + nextValve.getClass().getName());
                                this.addReplicationManager(appName, (ReplicationManager)nextValve);
                                ((ReplicationManager)nextValve).processMessage(message);
                                continueProcessing = false;
                                break;                                
                            }                            
                        }                       
                    }                    
                    
                } 
                if(!continueProcessing) {
                    break;
                }                
            }
        } catch (Throwable th) {
            _logger.log(Level.SEVERE, "Exception thrown", th);
        }       
                
    }
    
    public void eagerlyLoadRoutingMap() {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicationMessageRouter>>eagerlyLoadRoutingMap");
        }        
        //System.out.println("ReplicationMessageRouter>>eagerlyLoadRoutingMap");
        ReplicationManager mgr = null;

        boolean continueProcessing = true;
        try {
            Engine[] engines = _embedded.getEngines();
            //System.out.println("engines length" + engines.length);
            
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
                        Manager nextManager = nextWebModule.getManager();                       
                        if(nextManager instanceof ReplicationManager) {
                            //let this manager process the message 
                            //if it is a ReplicationManager
                            //and app names match
                            String nextAppName = ((ReplicationManagerBase)nextManager).getApplicationId();
                            //System.out.println("ReplicationMessageRouter:nextAppName = " + nextAppName);
                            if (_logger.isLoggable(Level.FINEST)) {
                                _logger.finest("ReplicationMessageRouter:eagerlyLoadRoutingMap:nextAppName = " + nextAppName);
                            }                            
                            //System.out.println("ReplicationMessageRouter:eagerlyLoadRoutingMap:nextAppName = " + nextAppName);
                            if (_logger.isLoggable(Level.FINEST)) {
                                _logger.finest("ReplicationMessageRouter:eagerlyLoadRoutingMap:found our manager:" + nextManager.getClass().getName());
                            } 
                            //System.out.println("ReplicationMessageRouter:eagerlyLoadRoutingMap:found our manager:" + nextManager.getClass().getName());
                            this.addReplicationManager(nextAppName, (ReplicationManager)nextManager);

                        }
                    }
                    if(!continueProcessing) {
                        break;
                    }
                    //now get ReplicationManager valves installed in virtual hosts
                    Valve[] valves = ((ContainerBase)nextHost).getValves();                   
                    for(int k=0; k<valves.length; k++) {
                        Valve nextValve = valves[k];
                        if (_logger.isLoggable(Level.FINEST)) {
                            _logger.finest("ReplicationMessageRouter:eagerlyLoadRoutingMap:VALVE = " + nextValve.getClass().getName());
                        }                                            
                        //System.out.println("ReplicationMessageRouter:eagerlyLoadRoutingMap:VALVE = " + nextValve.getClass().getName());                         
                        if(nextValve instanceof ReplicationManager) {
                            //let this manager process the message 
                            //if it is a ReplicationManager
                            //and app names match
                            String nextAppName = ((ReplicationManager)nextValve).getApplicationId();                            
                            if (_logger.isLoggable(Level.FINEST)) {
                                _logger.finest("ReplicationMessageRouter:eagerlyLoadRoutingMap:found our manager:" + nextValve.getClass().getName());
                            }                                                                 
                            //System.out.println("ReplicationMessageRouter:eagerlyLoadRoutingMap:found our manager valve:" + nextValve.getClass().getName());
                            this.addReplicationManager(nextAppName, (ReplicationManager)nextValve);                                                         
                        }                       
                    }                    
                    
                } 
                if(!continueProcessing) {
                    break;
                }                
            }
        } catch (Throwable th) {
            _logger.log(Level.SEVERE, "Exception thrown", th);
        }       
                
    }    
    
    public void processQueryMessage(ReplicationState message, String returnInstance) {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicationMessageRouter:processQueryMessage"); 
        }        
        //System.out.println("ReplicationMessageRouter:processQueryMessage");
        this.routeQueryMessageForApp(message.getAppId(), message, returnInstance);
    }    
    
    public void routeQueryMessageForApp(String appName, ReplicationState message, String returnInstance) {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicationMessageRouter:routeQueryMessageForApp" + appName); 
        }
        //if _embedded == null startup is not complete - do nothing
        if(_embedded == null) {
            return;
        }
        //System.out.println("ReplicationMessageRouter:routeQueryMessageForApp: " + appName);        
        ReplicationManager mgr = null;
        if((mgr = this.findApp(appName)) != null) {
            mgr.processQueryMessage(message, returnInstance);
            return;
        }
        boolean continueProcessing = true;
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
                        Manager nextManager = nextWebModule.getManager();                       
                        if(nextManager instanceof ReplicationManager) {
                            //let this manager process the message 
                            //if it is a ReplicationManager
                            //and app names match
                            String nextAppName = ((ReplicationManagerBase)nextManager).getApplicationId();
                            if (_logger.isLoggable(Level.FINEST)) {
                                _logger.finest("ReplicationMessageRouter:routeQueryMessageForApp:nextAppName = " + nextAppName + ", appName = " + appName);
                            }                            
                            //System.out.println("ReplicationMessageRouter:routeQueryMessageForApp:nextAppName = " + nextAppName + ", appName = " + appName);                            
                            if(nextAppName.equals(appName)) {
                                if (_logger.isLoggable(Level.FINEST)) {
                                    _logger.finest("ReplicationMessageRouter:routeQueryMessageForApp:found our manager:" + nextManager.getClass().getName());
                                }                                                         
                                this.addReplicationManager(appName, (ReplicationManager)nextManager);
                                ((ReplicationManager)nextManager).processQueryMessage(message, returnInstance);
                                continueProcessing = false;
                                break;                                
                            }
                        }
                    }
                    
                    if(!continueProcessing) {
                        break;
                    }
                    //now get ReplicationManager valves installed in virtual hosts
                    Valve[] valves = ((ContainerBase)nextHost).getValves();                   
                    for(int k=0; k<valves.length; k++) {
                        Valve nextValve = valves[k];
                        if (_logger.isLoggable(Level.FINEST)) {
                            _logger.finest("ReplicationMessageRouter:routeQueryMessageForApp:VALVE = " + nextValve.getClass().getName());
                        }                                                
                        //System.out.println("ReplicationMessageRouter:routeQueryMessageForApp:VALVE = " + nextValve.getClass().getName());                         
                        if(nextValve instanceof ReplicationManager) {
                            //let this manager process the message 
                            //if it is a ReplicationManager
                            //and app names match
                            String nextAppName = ((ReplicationManager)nextValve).getApplicationId();
                            if (_logger.isLoggable(Level.FINEST)) {
                                _logger.finest("ReplicationMessageRouter:routeQueryMessageForApp:nextAppName = " + nextAppName + ", appName = " + appName); 
                            }                            
                            //System.out.println("ReplicationMessageRouter:routeQueryMessageForApp:nextAppName = " + nextAppName + ", appName = " + appName);                            
                            if(nextAppName.equals(appName)) {
                                if (_logger.isLoggable(Level.FINEST)) {
                                    _logger.finest("ReplicationMessageRouter:routeQueryMessageForApp:found our manager:" + nextValve.getClass().getName()); 
                                }                                                                 
                                //System.out.println("ReplicationMessageRouter:routeQueryMessageForApp:found our manager valve:" + nextValve.getClass().getName());
                                this.addReplicationManager(appName, (ReplicationManager)nextValve);
                                ((ReplicationManager)nextValve).processQueryMessage(message, returnInstance);
                                continueProcessing = false;
                                break;                                
                            }                            
                        }                       
                    }                     
                    
                }
                if(!continueProcessing) {
                    break;
                }                
            }
        } catch (Throwable th) {
            _logger.log(Level.SEVERE, "Exception thrown", th);
        }       
                
    }    
    
}
