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
 * JdbcPoolMgmtAction.java
 *
 *
 */

package com.sun.enterprise.ee.selfmanagement.actions;

import javax.management.MBeanRegistration;
import javax.management.NotificationListener;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.ObjectName;
import javax.management.NotificationFilter;
import javax.management.MBeanServerConnection;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ResourceHelper;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.Switch;
import com.sun.enterprise.resource.ResourcePool;
import com.sun.enterprise.resource.MonitorableResourcePool;
import com.sun.enterprise.resource.PoolLifeCycle;
import com.sun.enterprise.resource.PoolManagerImpl;
import com.sun.enterprise.admin.selfmanagement.event.*;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;


/**
 *
 * @author Sun Micro Systems, Inc
 */
public class JdbcPoolMgmtAction implements JdbcPoolMgmtActionMBean, MBeanRegistration, NotificationListener, PoolLifeCycle {
    
    
    static StringManager sm = StringManager.getManager(JdbcPoolMgmtAction.class);
    static Logger _logger = LogDomains.getLogger(LogDomains.SELF_MANAGEMENT_LOGGER);
    
    
    /** Creates a new instance of JdbcPoolMgmtAction */
    public JdbcPoolMgmtAction() {
    }
    
    public void postRegister(Boolean registrationDone) {
        if (registrationDone) {
            /**
            // Support to dynamic detection of server starts and stops
             ((PoolManagerImpl)Switch.getSwitch().getPoolManager()).registerPoolLifeCycleListner(this);
             ElementProperty properties[] = new ElementProperty[0];
             Event e = EventBuilder.getInstance().getEvent("cluster", properties, "null");
             ObjectName objName = e.getObjectName();
             NotificationFilter filter = e.getNotificationFilter();
             try {
                 MBeanServerFactory.getMBeanServer().addNotificationListener(objName,myObjectName,filter,null);
             } catch (Exception ex) {
               _logger.log(Level.WARNING, sm.getString("action.internal_error"),ex);
             }
             **/
        }
    }
    
    public void handleNotification(javax.management.Notification notification, Object handback) {
        if (notification.getType().startsWith("cluster")) {
            handleClusterNotification(notification,handback);
        } else if (notification.getType().startsWith("lifecycle")) {
            parse();
            Iterator iter = Switch.getSwitch().getPoolManager().getPoolTable().keySet().iterator();
            String poolName = null;
            while (iter.hasNext()){
                poolName = (String)iter.next();
                poolCreated(poolName);
            }
            continueWorkerThread = true;
            new JdbcWorkerThread(sampleInterval,this, sampleSize).start();
            if(_logger.isLoggable(Level.INFO))
                _logger.log(Level.INFO, sm.getString("actions.activated", "Connection Pool Management", notification));
            
        }
    }
    
    private void handleClusterNotification(javax.management.Notification notification, Object handback) {
        // for every pool which is self managed, see if it effects the max pool size
        ConfigContext ctx = AdminService.getAdminService().getAdminContext().getAdminConfigContext();
        String poolName = null;
        Iterator<String> iter = (Iterator<String>)createdPools.keys();
        while (iter.hasNext()){
            poolName = iter.next();
            try {
                String serverName = null;
                if (notification.getUserData() != null)
                    serverName = (String)((Map)notification.getUserData()).get("com.sun.enterprise.ee.selfmanagement.events.clusterevent.servername");
                if (ResourceHelper.isJdbcPoolReferenced(ctx, poolName, serverName)) {
                    int maxPoolSize = calcMaxPoolSize(poolName);
                    ResourcePool pool = Switch.getSwitch().getPoolManager().getPool(poolName);
                    pool.setMaxPoolSize(maxPoolSize);
                    if(_logger.isLoggable(Level.INFO))
                        _logger.log(Level.INFO, sm.getString("connectionpool.setmaxpoolsize", poolName, maxPoolSize));
                }
            } catch (ConfigException cex) {
                _logger.log(Level.WARNING, sm.getString("action.internal_error"),cex);
            }
        }
    }
    
    public javax.management.ObjectName preRegister(javax.management.MBeanServer server, javax.management.ObjectName name) throws Exception {
        myObjectName = name;
        return name;
    }
    
    public void preDeregister() throws Exception {
        continueWorkerThread = false;
    }
    
    public void postDeregister() {
    }
    
    boolean continueAction() {
        return continueWorkerThread;
    }
    
    void gatherStats() {
        Iterator<String> iter = (Iterator<String>)createdPools.keys();
        ResourcePool pool = null;
        String poolName = null;
        long connsInUse = 0;
        while (iter.hasNext()){
            poolName = iter.next();
            pool = Switch.getSwitch().getPoolManager().getPool(poolName);
            connsInUse = ((MonitorableResourcePool)pool).getNumConnInUse();
            createdPools.get(poolName).add(connsInUse);
        }
    }
    
    void tuneSteadyPoolSize() {
        Iterator<String> iter = (Iterator<String>)createdPools.keys();
        ResourcePool pool = null;
        String poolName = null;
        int steadyPoolSize = 0;
        while (iter.hasNext()){
            poolName = iter.next();
            pool = Switch.getSwitch().getPoolManager().getPool(poolName);
            steadyPoolSize = (int)createdPools.get(poolName).getApprox90thPercentile();
            if (pool.getSteadyPoolSize() != steadyPoolSize) {
                if(_logger.isLoggable(Level.INFO))
                    _logger.log(Level.INFO, sm.getString("connectionpool.setsteadypoolsize", poolName, pool.getSteadyPoolSize() , steadyPoolSize));
                pool.setSteadyPoolSize(steadyPoolSize);
            }
        }
    }
    
    private boolean allPools = false;
    private String poolNames = null;
    private  ConcurrentHashMap<String,Integer>
            poolNameMap = new ConcurrentHashMap<String,Integer>(10, 0.75f, 2);
    private ObjectName myObjectName = null;
    private int sampleSize = 5;
    private int sampleInterval = 60; // seconds
    
    private int defaultMaxConnections = 0;
    // private ArrayList<String> createdPools = new ArrayList<String>();
    private ConcurrentHashMap<String,BoundedCircularQueue>
            createdPools = new ConcurrentHashMap<String,BoundedCircularQueue>(10, 0.75f, 2);
    private boolean continueWorkerThread = false;
    
    public void setDefaultMaxConnections(int maxConnections) {
        defaultMaxConnections = maxConnections;
    }
    
    public String getPoolNames() {
        return poolNames;
    }
    
    // format of the poolNames is poolName=noofconnections,poolName=numberofconnections
    public void setPoolNames(String poolNames) {
        if (poolNames == null)
            return;
        this.poolNames = poolNames;
    }
    
    public void setSampleSize(int size) {
        sampleSize = size;
    }
    
    public void setSampleInterval(int seconds) {
        sampleInterval = seconds;
    }
    
    private void parse() {
        StringTokenizer tokenizer = new StringTokenizer(poolNames, ",");
        StringTokenizer subTokenizer = null;
        String poolName = null;
        Integer maxConnections = null;
        while( tokenizer.hasMoreTokens()) {
            poolName = tokenizer.nextToken();
            if ("*".equals(poolName)) {
                allPools = true;
                return;
            }
            subTokenizer = new StringTokenizer(poolName,"=");
            if (subTokenizer.hasMoreTokens()) {
                poolName =  subTokenizer.nextToken();
                maxConnections = Integer.parseInt(subTokenizer.nextToken());
                poolNameMap.put(poolName, maxConnections);
            } else {
                poolNameMap.put(poolName, defaultMaxConnections);
            }
        }
    }
    
    private int calcMaxPoolSize(String poolName) {
        int maxConnections = poolNameMap.get(poolName);
        ConfigContext ctx = AdminService.getAdminService().getAdminContext().getAdminConfigContext();
        Server servers[] = null;
        try {
            servers = ServerHelper.getServersReferencingJdbcPool(ctx, poolName);
        } catch (ConfigException ex) {
            // log the exception
        }
        String currentServer = ApplicationServer.getServerContext().getInstanceName();
        int totalWeights = 0;
        int myWeight = 0;
        for (int i = 0; i < servers.length; i++) {
            if (servers[i].getName().equals(currentServer))  {
                myWeight = Integer.parseInt(servers[i].getLbWeight());  // new code
            }
            totalWeights += Integer.parseInt(servers[i].getLbWeight());
        }
        if (totalWeights == 0 || myWeight == 0)
            return maxConnections;
        return ( (maxConnections * myWeight) / totalWeights);
    }
    
    
    public void poolDestroyed(String poolName) {
        createdPools.remove(poolName);
    }
    
    public void poolCreated(String poolName) {
        if (poolNameMap.get(poolName) == null) {
            return;
        }
        int maxPoolSize = calcMaxPoolSize(poolName);
        ResourcePool pool = Switch.getSwitch().getPoolManager().getPool(poolName);
        pool.setSelfManaged(true);
        pool.setMaxPoolSize(maxPoolSize);
        if(_logger.isLoggable(Level.INFO))
            _logger.log(Level.INFO, sm.getString("connectionpool.setmaxpoolsize", poolName, maxPoolSize));
        if (!pool.isMonitoringEnabled()) {
            pool.setMonitoringEnabledLow();
        }
        createdPools.put(poolName, new BoundedCircularQueue(sampleSize));
    }
    
    
}

class JdbcWorkerThread extends Thread {
    private int interval;
    private int statsCounter;
    private JdbcPoolMgmtAction action = null;
    JdbcWorkerThread(int sampleInterval, JdbcPoolMgmtAction action, int statsCounter) {
        interval = sampleInterval;
        this.action = action;
        this.statsCounter = statsCounter;
    }
    
    public void run() {
        try {
            boolean loop = true;
            int counter = 0;
            while(loop) {
                Thread.sleep(interval*1000);
                action.gatherStats();
                counter++;
                if (counter == statsCounter) {
                    action.tuneSteadyPoolSize();
                    counter = 0;
                }
                if(!action.continueAction())
                    loop = false;
            }
        }catch (InterruptedException exc) {
            JdbcPoolMgmtAction._logger.log(Level.WARNING, JdbcPoolMgmtAction.sm.getString("action.internal_error"),exc);
        }
    }
    
}
