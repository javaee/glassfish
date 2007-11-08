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

/*/*
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
import com.sun.enterprise.config.serverbeans.ModuleMonitoringLevels;

import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistrationException;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevel;
import com.sun.enterprise.admin.monitor.registry.MonitoringLevelListener;
import com.sun.enterprise.admin.monitor.registry.MonitoredObjectType;
import com.sun.enterprise.admin.mbeans.custom.loading.CustomMBeanRegistrationImpl;

import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Class ThreadHangAction
 * ThreadHangAction Description
 * @author Pankaj Jairath
 */
public class ThreadHangAction implements ThreadHangActionMBean, 
                                         MonitoringLevelListener,
                                         MBeanRegistration,
                                         NotificationListener { 
    /** Logger for self management service */
    private static Logger _logger =  null;
    
    /** String manager for local strings */
    private static StringManager localStrings = null;
    
    /** Messgae prefix */
    private static String msgPrefix = null;
    
    /** Instance name */
    private static String instanceName = null;
    
    /** ClusterName */
    private static String clusterName = null;
 
    static {
        _logger = LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
        localStrings = StringManager.getManager(
            com.sun.enterprise.ee.selfmanagement.actions.ThreadHangAction.class);
        instanceName = System.getProperty("com.sun.aas.instanceName");
        clusterName = System.getProperty("com.sun.aas.clusterName");
        String domainName = System.getProperty("com.sun.aas.domainName");
        
        if ( clusterName != null) {
            msgPrefix = localStrings.getString("threadhang.incluster",
                                               instanceName, clusterName,
                                               domainName);
        } else { 
            msgPrefix = localStrings.getString("threadhang.ininstance", 
                                               instanceName, domainName);
        } 
    }
    
    /** Attribute : MaxProcessingTime */
    private long thresholdWaitWindow = 0;
    
    /** Monitoring flag of http service */
    private boolean isMonitoringEnabled = false;
    
    /** MBeanserver */
    private MBeanServer mbeanServer = null;
    
    /** Default response time out for LB in milliseconds */
    private final long DEFAULT_LB_TIMEOUT = (60 * 1000);
    
    /** Type Key Name */
    private final String TYPE_KEY_NAME = "type";
    
    /** Request Processor type */
    private final String REQ_PROCESSOR_TYPE = "RequestProcessor";
    
    /** URI */
    private final String REQ_URI = "currentUri";
    
    /** Proccessing Time */
    private final String REQ_PROSTIME = "requestProcessingTime";
    
    /** Completion Time */
    private final String REQ_COMPTIME = "requestCompletionTime";        
    
    /** WorkerThread */
    private final String REQ_THREAD = "workerThreadID";
    
    /** Server port */
    private final String REQ_SERVER_PORT = "serverPort";
    
    /** Stop thread */
    private boolean stopThread = false;
    
    /** Mail alert ref */
    private String alertRef = null;
    
    /** Domain */
    private String domainName = null;
    
    /** Object name pattern for registered catalina statistic */
    private ObjectName objPattern = null;
    
    
   /* Creates a new instance of ThreadHangAction */
    public ThreadHangAction(){
        /* Currently tomcat engine is set to use from default domain */
        try {
            String domain = ApplicationServer.getServerContext().getDefaultDomainName();
            ObjectName objName = new ObjectName(domain,TYPE_KEY_NAME, 
                                          REQ_PROCESSOR_TYPE);
            String concat = objName.toString() + ",*";
            objPattern = new ObjectName(concat);
        } catch (MalformedObjectNameException ex) {
            //nop
        }
    }

   /**
    * Get Highest processing time attached with a incoming HTTP request
    */
    public long getThresholdWait(){
        return thresholdWaitWindow;
    }

   /**
    * Set Highest processing time attached with a incoming HTTP request
    */
    public synchronized void setThresholdWait(long value) {
        try {
            long lbTimeout = 0;
            
            ConfigContext cfgCtx = ApplicationServer.getServerContext().getConfigContext();
            Set<LbConfig> lbs = ServerBeansFactory.getLbConfigs(cfgCtx);
            LbConfig selectLb = null;
            for ( LbConfig lb : lbs) {
                if (lb.getClusterRefByRef(clusterName)!= null) {
                    selectLb = lb;
                    break;
                }
                if (lb.getServerRefByRef(instanceName)!= null) {
                    selectLb = lb;                    
                    break;
                }
            }
        
            if (selectLb != null) {
                String val = selectLb.getResponseTimeoutInSeconds();
                lbTimeout = Long.parseLong(val); 
            } 
            
            if (lbTimeout == 0) {
                //standalone
                 if (value < DEFAULT_LB_TIMEOUT) {          
                     _logger.log(Level.WARNING,"sgmt.threadhangaction_deftimeout", 
                                 new Object[]{msgPrefix,Long.toString(value),
                                              Long.toString(DEFAULT_LB_TIMEOUT)});
                     value = DEFAULT_LB_TIMEOUT;
                 }
            } else {
                if (value < lbTimeout) {        
                   _logger.log(Level.WARNING,"sgmt.threadhangaction_lbtimeout", 
                                new Object[]{msgPrefix,Long.toString(value),
                                             Long.toString(lbTimeout)});
                   value = lbTimeout;
                }
            }
            
            thresholdWaitWindow = value;
        } catch(Exception ex) {
            //log
        }
    }
    
    /**
     * Checks for the monitoring support for http requests
     */
    public ObjectName preRegister(MBeanServer mbs, ObjectName objName) 
                                                              throws Exception {
        
        mbeanServer = mbs;        
        isMonitoringEnabled = checkMonitoringEnabled();
       
        if (!isMonitoringEnabled) {
            /* illegal state. log that action would not work, until turned on
             * to collection the statistics
             */
            _logger.log(Level.WARNING,"sgmt.threadhangaction_nostats");
           
       }
       
       return objName;
    }
    
    boolean checkMonitoringEnabled() throws ConfigException {
 
        ServerContext srCtx = ApplicationServer.getServerContext();
        ConfigContext cfgCtx = srCtx.getConfigContext();
        Config instanceCfg = ServerBeansFactory.getConfigBean(cfgCtx);
        
        MonitoringLevel monitoringLevel = MonitoringLevel.OFF;

        if (instanceCfg.getMonitoringService() != null) {
            ModuleMonitoringLevels levels =
                instanceCfg.getMonitoringService().getModuleMonitoringLevels();
            if (levels != null) {
                monitoringLevel = MonitoringLevel.instance(
                                                levels.getHttpService());
            }
        } 

        if(MonitoringLevel.OFF.equals(monitoringLevel)) {
            isMonitoringEnabled = false;
        } else {
            isMonitoringEnabled = true;
        }
     
        return isMonitoringEnabled;
    }
    
    /**
     * Initializes the mbean
     */
    public void postRegister(Boolean isRegistrationDone) {
        registerMonitoringLevelListener();    
    }
    
    /**
     * Register the monitor change event listener
     */
    public void registerMonitoringLevelListener() {
        MonitoringRegistry monitoringRegistry = 
            ApplicationServer.getServerContext().getMonitoringRegistry();
        monitoringRegistry.registerMonitoringLevelListener(
            this, MonitoredObjectType.HTTP_LISTENER);
    }

    /**
     * Set the level for monitoring
     */  
    public void setLevel(MonitoringLevel level) {
        // place holder
    }
    
    /**
     * Listner changeLevel event
     */
    public void changeLevel(MonitoringLevel from, MonitoringLevel to,
                            MonitoredObjectType type) {
        if (MonitoredObjectType.HTTP_LISTENER.equals(type)) {
            if (MonitoringLevel.OFF.equals(to)) {
                if (isMonitoringEnabled) {
                    //log henceworth this action would not work
                    isMonitoringEnabled = false;
                    _logger.log(Level.INFO,"sgmt.threadhangaction_statsdisabled");
                }
            } else {
                if (!isMonitoringEnabled) {
                    //log henceforth action would work
                    isMonitoringEnabled = true;
                    _logger.log(Level.INFO,"sgmt.threadhangaction_statsenabled");
                }
            }    
        }
    }    
    
    public void changeLevel(MonitoringLevel from, MonitoringLevel to, 
			    Stats handback) {
        // place holder
    }
    
    /**
     * Notification handler
     */
    public synchronized void handleNotification(Notification notif,
                                                Object notifObj) {
        int potentialCount = 0;
        try {
            if (isMonitoringEnabled) {
                //query mbs for statistics - requestinfos
                Set<ObjectName> requestObjs = mbeanServer.queryNames(objPattern, null);
                if (requestObjs.size()!=0) {
                    for (ObjectName request : requestObjs) {         
                        Long completionTime = 
                            (Long)mbeanServer.getAttribute(request,REQ_COMPTIME);
                        Long processingTime = 
                            (Long)mbeanServer.getAttribute(request,REQ_PROSTIME);
                        Long workerThreadId = 
                                    (Long)mbeanServer.getAttribute(request,
                                                                    REQ_THREAD);
                    
                        /*
                         *check existence of requests/threads that are nonreponsive
                         *and have the potenial to finally cause server to drop out.
                         *Accumilation of such requests could endanger the server from
                         *quickly runing out of resources - http worker threads.
                         */
                        if ( (completionTime == 0) 
                                && (processingTime > thresholdWaitWindow)
                             ) {
                            potentialCount++;
                            Integer port = (Integer)mbeanServer.getAttribute(request,
                                                               REQ_SERVER_PORT);
                            String uri = (String) mbeanServer.getAttribute(request,
                                                                      REQ_URI);
                            
                
                            _logger.log(Level.WARNING,
                                        "sgmt.threadhangaction_potentialthread",
                                         new Object[]{msgPrefix,port,uri});
                            if (stopThread)  {
                                String domain = ApplicationServer.getServerContext().getDefaultDomainName();
                                ObjectName oname = new ObjectName(domain + ":"
                                                        + "type=Selector,name=http" + port); 
                                Object[] params = new Object[1];
                                String[] sig = new String[]{"long"};
                                params[0] = workerThreadId;
                            
                                Boolean isCancelled = (Boolean) mbeanServer.invoke(oname,
                                                                 "cancelThreadExecution", 
                                                                  params,sig);
                                if (isCancelled) {
                                    _logger.log(Level.SEVERE,"sgmt.threadhangaction_stopd",
                                                new Object[]{msgPrefix,port,uri});
                                 } else {
                                    _logger.log(Level.WARNING, "sgtm_threadhangaction_notstopd",
                                               new Object[]{msgPrefix,port,uri});
                                 }
                            }
                        } 
                    }    
                
                    /*
                     *Also send a alert request count that would potenially
                     *bring the server down - unresponsive
                     */
                    if (potentialCount > 0) {
                        sendMailAlert(potentialCount);                
                    }
                
                    //log the count
                    _logger.log(Level.INFO,"sgmt.threadhangaction_potentialcount",
                                new Object[]{msgPrefix,potentialCount});
               } else {
                    //log no http stats found. This is possible no http request
                    //received
                   _logger.log(Level.INFO,"sgmt.threadhangaction_norequests",
                               msgPrefix);
               }             
            } else {
                /*
                 * log need to have http service monitoring statistics for 
                 *  action to run
                 */
                _logger.log(Level.WARNING,"sgmt.threadhangaction_nostats",
                            msgPrefix);
            }
        } catch (SecurityException ex) {
            //stop, interrupt
            _logger.log(Level.SEVERE,"sgmt.threadhangaction_securityex",ex);
        } catch (Exception ex) {
            //Attribute, Instance not found, Reflection, MBeanException
            _logger.log(Level.SEVERE,"sgmt.threadhangaction_mbeanex",ex);
        }
        
    }
    
    /**
     * Send mail alert on the potenial requests/threads which are going
     * no where
     */
    private void sendMailAlert(int potentialCount) {
        String alertMsg = msgPrefix 
                         + localStrings.getString("threadhang.potentialcount",
                                                 potentialCount);
        if (alertRef != null) {
            try {
                ServerContext srCtx = ApplicationServer.getServerContext();
                ConfigContext cfgCtx = srCtx.getConfigContext();
        
                Domain domain = ServerBeansFactory.getDomainBean(cfgCtx);
                Applications apps = domain.getApplications();
                Mbean definedMBean = apps.getMbeanByName(alertRef);
                ObjectName objName =
                    CustomMBeanRegistrationImpl.getCascadingAwareObjectName(
                                                                  definedMBean);
                Object[] params = new Object[2];
                String[] signature = new String[2];
                params[0] = new Notification("thread.hang", this,0,alertMsg);
                params[1] = null;
                signature[0] = (Notification.class).getName();
                signature[1] = (java.lang.Object.class).getName();
                mbeanServer.invoke(objName,"notification", params, signature);
            } catch (InstanceNotFoundException ex) {
                // invalid appref, anyways simply log the message
                _logger.log(Level.SEVERE,"sgmt.threadhangaction_nosuchobj",
                            new Object[]{msgPrefix, alertRef});
            } catch (Exception ex) {
                //MBeanException, ReflectionException, Runtime anyway log count
                _logger.log(Level.WARNING,"sgmt.threadhangaction_excep",ex);
            }
        } else {
            //log cannot send as no ref set. Anyway log count
            _logger.log(Level.WARNING,"threadhangaction.noalertref");
       }
    }
    
    public String toString() {
        return (getClass().getName());
    }
    
    /**
     * Set mail alert ref
     */
    public synchronized void setMailAlertAppRef(String appRef) {
        alertRef = appRef;
    }
    
    /**
     * Get the Mail Alert Application ref
     */
    public String getMailAlertAppRef() {
        return alertRef;
    }
            
    /** Set check for stop thread */
    public synchronized void setStopThread(boolean stop) {
        stopThread = stop;
    }
    
    /** Set check for stop thread */
    public boolean getStopThread() {
        return stopThread;
    }
    
    /**
     * Handle pre De-Registration
     */
    public void preDeregister() {
        unregisterMonitoringLevelListener();
    }
    
    public void unregisterMonitoringLevelListener() {
        MonitoringRegistry monitoringRegistry =
            ApplicationServer.getServerContext().getMonitoringRegistry();
        monitoringRegistry.unregisterMonitoringLevelListener(this);
    }
    
    /**
     * Handle post De-Registration
     */
    public void postDeregister() {
        //place holder
    }

}
