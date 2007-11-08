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
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */
package com.sun.enterprise.ee.selfmanagement.actions;

import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.Notification;
import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.j2ee.statistics.Stats;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.LbConfig;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Mbean;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.config.serverbeans.ClusterRef;
import com.sun.enterprise.config.serverbeans.HealthChecker;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.mbeans.custom.loading.CustomMBeanRegistrationImpl;

import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

    
/**
  * Wrapper to maintain server/cluster relationship
  */
class Instance {
        Server instance;
        String clusterName;
        int timeout;
        boolean isHealthy;
        
        Instance(Server val) {
            instance = val;
        }
        
        Instance(Server val, String name) {
            instance = val;
            clusterName = name;
        }
        
        Server getServer() {
            return instance;
        }
        
        String getClusterName() {
            return clusterName;
        }
        
        void setHealthy(boolean val) {
            isHealthy = val;
        }
        
        boolean isHealthy() {
            return isHealthy;
        }
        
        void setTimeout(int val) {
            timeout = val;
        }
        
        int getTimeout() {
            return timeout;
        }
} 

/**
 * Class InstanceHangAction
 * InstanceHangAction Description
 * @author Pankaj Jairath
 */
public class InstanceHangAction implements InstanceHangActionMBean, 
                                         MBeanRegistration,
                                         NotificationListener { 
    /** Config context */
    static final ConfigContext configCtx =
        ApplicationServer.getServerContext().getConfigContext();
    
    /** Logger for self management service */
    private static final Logger _logger =
        LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
    
    /** String manager for local strings */
    private static final StringManager localStrings =
        StringManager.getManager(com.sun.enterprise.ee.selfmanagement.actions.InstanceHangAction.class);
    
    /** Messgae prefix */
    private static String msgPrefix = null;

    /** List of names of instances,cluster */
    public String checkList = null;
    
    /** Attribute : Max ping wait time, upon elapse of which instance
     * is marked unresponsive.
     */
    public int timeoutInSeconds = 0;
    
    /** MBeanserver */
    private MBeanServer mbeanServer = null;
    
    /** Default response time out for LB in seconds */
    private final long DEFAULT_HC_TIMEOUT_ = 10;
    
    /** Stop thread */
    public boolean isRestart = false;
    
    /** Mail alert ref */
    public String alertRef = null;
    
    /** Complete list of instances to check for non-responsiveness */
    private ArrayList<Instance> instanceList = null;
    
    /** Association of name to server set */
    private HashMap<String,Vector<Instance>> instMap = null;
    
    
   /* Creates a new instance of InstanceHangAction */
    public InstanceHangAction(){     
        AdminService as = AdminService.getAdminService();
        if (!as.isDas()) {
            // this rule needs to configured only on DAS
            String errMsg = localStrings.getString("instancehang.notDAS");
            _logger.log(Level.SEVERE,"sgmt.instancehangaction_notDAS");
            throw new RuntimeException(errMsg);
        }

        instanceList = new ArrayList<Instance>(); 
        instMap = new HashMap<String,Vector<Instance>>();
    }

   /**
    * Adds the given name to list of server instances to check
    */
    void addToCheck(String name) {
        try {
            boolean isServer = ServerHelper.isAServer(configCtx, name);
            if (isServer) {
                Server instance = ServerHelper.getServerByName(configCtx, name);
                Instance inst = new Instance(instance);
                instanceList.add(inst);
                Vector<Instance> instanceCol = new Vector<Instance>();
            
                //add the standalone instance 
                instanceCol.add(inst);
                instMap.put(name,instanceCol);
            } else if (ClusterHelper.isACluster(configCtx,name)) {
                Server[] servers = ServerHelper.getServersInCluster(configCtx, name);
                Vector<Instance> instanceCol = new Vector<Instance>(servers.length);
                for (Server server : servers) {
                    Instance inst = new Instance(server, name);
                    instanceList.add(inst);
                    instanceCol.add(inst);
                }
            
                //add the instance set for this cluster
                instMap.put(name,instanceCol);
            } else {
                //log invalid name
                _logger.log(Level.WARNING,"sgmt.instancehang_invalidname",name);
            }
        } catch (ConfigException ex) {
            //nop
        }
    }
    
   /**
    * Set the instances and or clusters that need to be checked for
    * non-responsiveness
    */
   public void setChecklist(String val) {
       checkList = val;
       String[] list = checkList.split(",");
       for (String name : list) {
           addToCheck(name);
       }
   }
    
    /**
     * The list of instances and or clusters had are to be checked for
     * non-responsiveness
     *
     * @return names of instances and or clusters
     */
    public String getChecklist() {
        return checkList;
    }
    
   /**
    * Get Highest processing time attached with a incoming HTTP request
    */
    public int getTimeoutInSeconds(){
        return timeoutInSeconds;
    }

   /**
    * Set Highest processing time attached with a incoming HTTP request
    */
    public synchronized void setTimeoutInSeconds(int value) {
        timeoutInSeconds = value;
    }
    
    /**
     * Checks for the monitoring support for http requests
     */
    public ObjectName preRegister(MBeanServer mbs, ObjectName objName) 
                                                              throws Exception {
        mbeanServer = mbs;    
        return objName;
    }
    
    /**
     * Initializes the mbean
     */
    public void postRegister(Boolean isRegistrationDone) {
        //nop, place holder
    }
    
    /**
     * Sanity check to ensure the value provided is not less than
     * the health checker timeout value, if there is one asscoiated
     */
    void validateTimeout() {
        try {
            Set<String> names = instMap.keySet();
            Set<LbConfig> lbs = ServerBeansFactory.getLbConfigs(configCtx);
           
            for (String name : names) {
                if (ServerHelper.isAServer(configCtx, name)) {
                    ServerRef ref = null;
                    //standalone server
                    Vector<Instance> col = instMap.get(name);
                    Instance instance = col.firstElement();
                               
                    for ( LbConfig lb : lbs) {
                        ref = lb.getServerRefByRef(name);
                        if (ref != null) {
                            break;
                        }
                    }
                
                    if (ref != null) {
                        //check server ref associated with a health checker
                        HealthChecker hc = ref.getHealthChecker();
                        if (hc != null) {
                            String val = hc.getTimeoutInSeconds();
                            int hcTimeout = Integer.parseInt(val);
                            if (timeoutInSeconds >= hcTimeout) {
                                //use the value provided
                                instance.setTimeout(timeoutInSeconds);
                            } else {
                                //raise it to hc timeout
                                instance.setTimeout(hcTimeout);
                                _logger.log(Level.INFO,"sgmt.instancehang_hctimeout",
                                      new Object[]{name,timeoutInSeconds,val});
                            } 
                        } else {
                            instance.setTimeout(timeoutInSeconds);
                        }
                    } else {
                        // no lb / health checker, use the value specified
                        instance.setTimeout(timeoutInSeconds);
                    }
            //} else if (ClusterHelper.isACluster(configCtx,name)) {
                } else {                
                    //find related LB. should anyways be cluster as processed earlier
                    Vector<Instance> col = instMap.get(name);
                    ClusterRef ref = null;
                               
                    for ( LbConfig lb : lbs) {
                        ref = lb.getClusterRefByRef(name);
                        if (ref != null) {
                            break;
                        }
                    }
                
                    if (ref != null) {
                        //check cluster associated with a health checker
                        HealthChecker hc = ref.getHealthChecker();
                        if (hc != null) {
                            String val = hc.getTimeoutInSeconds();
                            int hcTimeout = Integer.parseInt(val);
                            if (timeoutInSeconds >= hcTimeout) {
                                //use the value provided
                                for (Instance instance : col) {
                                    instance.setTimeout(timeoutInSeconds);
                                }
                            } else {
                                //raise it to hc timeout
                                for (Instance instance : col) {
                                    instance.setTimeout(hcTimeout);
                                }   
                                _logger.log(Level.INFO,"sgmt.instancehang_hccltimeout",
                                            new Object[]{name,timeoutInSeconds,
                                                         val});
                            } 
                        } else {
                            for (Instance instance : col) {
                                instance.setTimeout(timeoutInSeconds);
                            }
                        }
                    } else {
                        // no lb / health checker, use the value specified
                        for (Instance instance : col) {
                            instance.setTimeout(timeoutInSeconds);
                        }
                    }
                }
            }
        } catch (ConfigException ex) {
            //nop
        }
    }
                
    /**
     * Notification handler
     */
    public synchronized void handleNotification(Notification notif,
                                                Object notifObj) {
        int potentialCount = 0;
        ArrayList<Future<?>> tasks = new ArrayList<Future<?>>();
        try {
            if (instanceList.size() != 0) {
                validateTimeout();

                ExecutorService exSrv = 
                                    Executors.newFixedThreadPool(instanceList.size());
                for (Instance checkServer : instanceList) {        
                    CheckServerHealth server = new CheckServerHealth(checkServer);
                    Future<?> task = exSrv.submit(server);
                    tasks.add(task);
                    if (checkServer.getClusterName()!=null) {
                        _logger.log(Level.INFO,"sgmt.instancehang_clinstcheckstart",
                                    new Object[] {checkServer.getServer().getName(),
                                                  checkServer.getClusterName()});
                    } else {
                        _logger.log(Level.INFO,"sgmt.instancehang_instcheckstart",
                                    checkServer.getServer().getName());
                        
                    }
                }
                
                //now wait for the cycle to complete
                for (Future<?> checkServerTask : tasks) {
                    checkServerTask.get();
                }
                
                if (isRestart) {
                    tasks.clear();
                    for (Instance server : instanceList) {
                        if (!server.isHealthy()) {
                            Future<?> task = exSrv.submit(new RestartInstance(server,mbeanServer));
                            tasks.add(task);
                        }
                    }
                    
                    for (Future<?> restartTask : tasks) {
                        restartTask.get();
                    }
                } 

                _logger.log(Level.INFO,"sgmt.instancehang_complete");

            } else {
                    // log no instances to check
                    _logger.log(Level.INFO,"sgmt.instancehang_noinstance");
            }    
        } catch (Exception ex) {
            //
        }
    } //end of notification  
          
            
    /** Set check for restarting instance upon being unresponsive */
    public synchronized void setRestart(boolean restart) {
        isRestart = restart;
    }
    
    /** Check for restarting of instance */
    public boolean getRestart() {
        return isRestart;
    }
    
    /**
     * Handle pre De-Registration
     */
    public void preDeregister() {
        //nop, place holder
    }
    
    
    /**
     * Handle post De-Registration
     */
    public void postDeregister() {
        //nop, place holder
    }

}
