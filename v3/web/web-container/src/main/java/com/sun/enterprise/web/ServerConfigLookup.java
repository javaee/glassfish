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
 * ServerConfigLookup.java
 *
 * Created on June 7, 2002, 11:47 AM
 */

package com.sun.enterprise.web;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.web.session.PersistenceType;

import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.AvailabilityService;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.config.serverbeans.ManagerProperties;
import com.sun.enterprise.config.serverbeans.RequestProcessing;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.SessionConfig;
import com.sun.enterprise.config.serverbeans.SessionManager;
import com.sun.enterprise.config.serverbeans.SessionProperties;
import com.sun.enterprise.config.serverbeans.StoreProperties;
import com.sun.enterprise.config.serverbeans.WebContainerAvailability;

import com.sun.enterprise.util.uuid.UuidGenerator;
import com.sun.enterprise.util.uuid.UuidGeneratorImpl;


public class ServerConfigLookup {
        
    
    /**
    * The prefix for the HADB connection url 
    */  
    protected static final String HADB_CONNECTION_URL_PREFIX = "jdbc:sun:hadb:"; 
  
    /**
    * The property name into domain.xml to obtain
    * the cluster-id string
    * Note: all instances in a cluster must have the same
    * cluster-id defined at this location in server.xml 
    * 7.0 xpath was "/server/availability-service/persistence-store/property[@name='cluster-id']";
    */  
    protected static final String CLUSTER_ID_PROPERTY_NAME = 
        "cluster-id"; 
    
    /**
    * The property name into domain.xml to obtain
    * the stale-session-checking-enabled string
    */  
    protected static final String STALE_SESSION_CHECKING_ENABLED_PROPERTY_NAME = 
        "stale-session-checking-enabled";    
    
    /**
    * The default cluster-id if none is defined in domain.xml
    * Note: the default is ok if only one cluster is being serviced
    * by an instance of HADB; if not then each cluster needs its
    * own unique cluster-id and each instance must have it correctly
    * defined in its server.xml
    */  
    protected static final String DEFAULT_CLUSTER_ID = "cluster1";
    
    /**
    * The xpath expression into domain.xml to obtain
    * the hadb-store-pool-jndi-name string 
    * 7.0 xpath was "/server/availability-service/persistence-store/property[@name='store-pool-jndi-name']";
    */  
    protected static final String HADB_STORE_POOL_JNDI_NAME_PROPERTY_NAME = 
    "store-pool-jndi-name";
    
    /**
    * The default store-pool-jndi-name (used by Web ContractProvider for
    * HTTP Persistence to HADB
    */  
    protected static final String DEFAULT_STORE_POOL_JNDI_NAME = "jdbc/hastore";           
    
    /**
    * The xpath expression into server.xml to obtain
    * the hadb-mgmt-env-path string
    * 7.0 xpath was "/server/availability-service/persistence-store/property[@name='hadb-mgmt-env-path']" 
    */  
    protected static final String HADB_MGMT_ENV_PATH_PROPERTY_NAME = 
        "hadb-mgmt-env-path";          
    
    /**
    * The property name in domain.xml to obtain
    * the hadb-database-name string
    * 7.0 xpath was "/server/availability-service/persistence-store/property[@name='hadb-database-name']";  
    */  
    protected static final String HADB_DATABASE_NAME_PROPERTY_NAME = 
        "hadb-database-name";              
    
    /**
    * The property name into domain.xml to obtain
    * the UuidGenerator impl class name 
    */  
    protected static final String UUID_GENERATOR_CLASS_PROPERTY_NAME =
        "uuid-impl-class";    

    /**
    * The default UuidGenerator class (fully qualified name) 
    */ 
    protected static final String DEFAULT_UUID_GENERATOR_CLASS = "com.sun.enterprise.util.uuid.UuidGeneratorImpl";  
    
    /**
    * The property name in domain.xml to obtain
    * the EE builder path - this property is not expected
    * now to change and if it ever did, then the directory
    * and package structure for the builder classes would
    * have to change also
    * 7.0 xpath was "/server/availability-service/persistence-store/property[@name='ee-builder-path']"
    */  
    protected static final String EE_BUILDER_PATH_PROPERTY_NAME =
        "ee-builder-path";      
  

    /**
    * The default path to the EE persistence strategy builders 
    */ 
    protected static final String DEFAULT_EE_BUILDER_PATH = "com.sun.enterprise.ee.web.initialization";

    /**
    * The property name in domain.xml to obtain
    * the hadb-health-check string
    * 7.0 xpath was "/server/availability-service/persistence-store/property[@name='hadb-health-check-enabled']";  
    */  
    protected static final String HADB_HEALTH_CHECK_ENABLED_PROPERTY_NAME = 
        "hadb-health-check-enabled";     
    
    /**
    * The default value of hadb-healthcheck-interval-in-seconds  
    */  
    protected static final int DEFAULT_HA_STORE_HEALTHCHECK_INTERVAL_IN_SECONDS = 5;    

    /**
    * The property name in domain.xml to obtain
    * the user name string in hadb jdbc-connection pool (for http session
    * persistence)
    */     
    protected static final String USER_NAME = "User";
    
    /**
    * The property name in domain.xml to obtain
    * the password string in hadb jdbc-connection pool (for http session
    * persistence)
    */     
    protected static final String PASSWORD = "Password";
    
    /**
    * The property name in domain.xml to obtain
    * the HADB agent password (used for connecting to HADB agent)
    */    
    protected static final String HADB_AGENT_PASSWORD = "ha-agent-password"; 
    
    private static final String NATIVE_REPLICATION_ENABLED = "native_replication_enabled";
    private static final String CLUSTER_MEMBERS = "cluster_members";
    private static final String NUMBER_OF_PIPES = "number_of_pipes";
    private static final String LATENCY_COUNT = "replication_ack_enabled";
    private static final String MAX_SESSION_UNLOAD_TIME_IN_SECONDS = "max_session_unload_time_in_seconds";
    private static final String REPLICATION_MEASUREMENT_ENABLED = "replication_measurement_enabled";
    private static final String REPLICATION_MEASUREMENT_INTERVAL = "replication_measurement_interval";
    private static final String WAIT_FOR_ACK_PROPERTY = "wait_for_ack_property";
    private static final String WAIT_FOR_FAST_ACK_PROPERTY = "wait_for_fast_ack_property";
    
    /**
    * The default value of thread-count  
    */  
    protected static final int DEFAULT_REQUEST_PROCESSING_THREAD_COUNT = 20;    
    
    /**
    * The config context passed in via constructor
    * used when a dynamic config context is needed
    * rather than usual run-time config context e.g. deployment
    */    
    protected ConfigContext _configContext = null;    
    
    /** Creates a new instance of ServerConfigLookup */
    public ServerConfigLookup() {
    }

    /**
    * Creates a new instance of ServerConfigLookup
    * @param configContext
    */         
    public ServerConfigLookup(ConfigContext configContext) {
        this();        
        _configContext = configContext;        
    }    
    
    //+++++++++++++++++++++++++START NEW METHODS++++++++++++++++++++++

    /**
    * Get the ServerContext for this server
    * return null if not found
    */    
    protected ServerContext getServerContext() {
        return Globals.getGlobals().getLookip().lookup(ServerContext.class);
    }
    
    /**
    * Get the ConfigContext for this server
    * return null if not found
    */    
    protected ConfigContext getConfigContextDynamic() {
        if (_configContext != null) {
            return _configContext;
        } else {
            return getConfigContext();
        }
    }    

    /**
    * Get the ConfigContext for this server
    * return null if not found
    */    
    protected ConfigContext getConfigContext() {
        ServerContext serverCtx = this.getServerContext();
        if(serverCtx == null) {
            return null;
        }
        return serverCtx.getConfigContext();
    }

    /**
    * Get the server element from domain.xml.
    * return null if not found
    */    
    private Server getServerBean() {
        return Globals.getGlobals().getLookup().lookup(Server.class);
    }

    /**
    * determine if this instance is the DAS
    */    
    public boolean isDAS() {
        boolean result = true;
        Server serverBean = this.getServerBean();
        ConfigContext configCtx = this.getConfigContext();
        if(serverBean != null && configCtx != null) {
            try {
                result = ServerHelper.isDAS(configCtx, serverBean);
            } catch (ConfigException ex) {}
        }
        return result;
    }    

    /**
    * Get the server name from domain.xml.
    * return null if not found
    */    
    public String getServerName() {
        String result = null;
        Server serverBean = this.getServerBean();
        if(serverBean != null) {
            result = serverBean.getName();
        }
        return result;
    }
    
    /**
    * Get the cluster name from domain.xml for this server.
    * return null if not found
    */ 
    public String getClusterName() {
        String result = null;
        /*
        ServerContext serverCtx = ApplicationServer.getServerContext();
        ConfigContext configCtx = serverCtx.getConfigContext();
         */
        ConfigContext configCtx = this.getConfigContext();
        
        String serverName = this.getServerName();
        if(serverName == null) {
            return result;
        }
        boolean isClustered = false;
        try {
            isClustered = ServerHelper.isServerClustered(configCtx, serverName);
        } catch (ConfigException ex) {}
        if(!isClustered) {
            result = serverName + "_nc";    //non-clustered example: server1_nc            
            return result;
        }
        Cluster cluster = null;
        try {
            cluster = ClusterHelper.getClusterForInstance(configCtx, serverName);
        } catch (ConfigException ex) {}
        if(cluster != null) {
            result = cluster.getName();
        }
        return result;
    }
    
    public ArrayList getServerNamesInCluster() {
        ArrayList result = new ArrayList();
        ConfigContext configCtx = this.getConfigContext();        
        String serverName = this.getServerName();
        //this should not happen
        if(serverName == null) {
            return result;
        }
        boolean isClustered = false;
        try {
            isClustered = ServerHelper.isServerClustered(configCtx, serverName);
        } catch (ConfigException ex) {}
        //if not clustered try to find the property else just return
        //our own name alone
        if(!isClustered) {
            //from property under availability-service for stand-alones
            ArrayList instanceNamesArray = this.getClusterInstanceNamesList();
            if(!instanceNamesArray.isEmpty()) {
                result = instanceNamesArray;
            } else {
                result.add(serverName);
            }
            return result;
        }        
        // if clustered instance then get list of cluster members names
        String clusterName = this.getClusterName();
        Server[] serversArray = null;
        try {            
            serversArray = ServerHelper.getServersInCluster(configCtx, clusterName);
        } catch (ConfigException ex) {}
        for(int i=0; i<serversArray.length; i++) {
            Server nextServer = serversArray[i];
            /* FIXME this appears wrong
            if(i > 0) {
                result.add(nextServer.getName());
            }
             */
            result.add(nextServer.getName().trim());
        }
        //displayArrayList(result);
        return result;
    }
    
    ArrayList getClusterInstanceNamesList() {
        ArrayList instanceNames = new ArrayList();
        ServerConfigLookup lookup = new ServerConfigLookup();
        String instanceNamesString = 
            lookup.getAvailabilityServicePropertyString(CLUSTER_MEMBERS);
        if(instanceNamesString == null) {
            return instanceNames;
        }
        String[] instancesArray = instanceNamesString.split(",");
        List instancesList = Arrays.asList(instancesArray);
        for(int i=0; i<instancesList.size(); i++) {
            instanceNames.add( ((String)instancesList.get(i)).trim() );
        }
        return instanceNames;
    }
    
    private void displayArrayList(ArrayList list) {
        System.out.println("DISPLAY CLUSTER MEMBERS");
        for(int i=0; i<list.size(); i++) {
            System.out.println("clusterMember[" + i + "] = " + list.get(i));
        }
    }
    
    /**
    * If the clusterName is null; return false
    * else return heartbeat enabled for the cluster
    */ 
    public boolean isGMSEnabled() {
        if(getClusterName() == null) {
            return false;
        }
        Cluster cluster = getCluster();
        if(cluster == null) {
            return false;
        }
        return cluster.isHeartbeatEnabled();      
    } 

    /**
    * Get the cluster if the server is in a cluster
    * return null if not
    */    
    public Cluster getCluster() {
        ConfigContext configCtx = this.getConfigContext();
        String serverName = this.getServerName();
        boolean isClustered = false;
        try {
            isClustered = ServerHelper.isServerClustered(configCtx, serverName);
        } catch (ConfigException ex) {}
        if(!isClustered) {
            return null;
        }
        Cluster cluster = null;
        try {
            cluster = ClusterHelper.getClusterForInstance(configCtx, serverName);
        } catch (ConfigException ex) {}
        return cluster;        
    }
    

            
    /**
    * returns the number of steady-state pipes in pool 
    * will set equal to number of request processing threads plus one
    * in the http-service
    * or if the NUMBER_OF_PIPES property is set that will take precedence
    */ 
    public int getNumberOfReplicationPipesFromConfig() {
        //if NUMBER_OF_PIPES property is set it will over-ride
        //else it will track the number of request processing threads
        //up to 40 - must be greater than 1
        int numPipes = getNumberOfReplicationPipesPropertyFromConfig();
        if(numPipes == -1) {
            numPipes = this.getRequestProcessingThreadCountFromConfig();
        }
        numPipes++;
        if(numPipes > 40) {
            numPipes = 41;
        }
        if(numPipes < 2) {
            numPipes = 2;
        }
        return numPipes;
    }            
    
    /**
    * returns the number of steady-state pipes in pool 
    * in <availability-service>
    * if missing (or error) default to -1
    */ 
    public int getNumberOfReplicationPipesPropertyFromConfig() {
        int candidateReturnValue = -1;
        int returnValue = -1;
        String returnValueString = 
            this.getAvailabilityServicePropertyString(NUMBER_OF_PIPES);        
        if(returnValueString != null) {
            try
            {
                candidateReturnValue = (Integer.valueOf(returnValueString)).intValue();
            } catch (NumberFormatException ex) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Using Default Value = 5");
                }
            }
        }
        //may return negative or zero value
        if(candidateReturnValue > 0) {
            returnValue = candidateReturnValue;
        }
        return returnValue;        
    } 
    
    /**
    * returns the latency count value 
    * in <availability-service>
    * if missing (or error) default to 0
    */ 
    public int getLatencyCountPropertyFromConfig() {
        int candidateReturnValue = 0;
        int returnValue = 0;
        String returnValueString = 
            this.getAvailabilityServicePropertyString(LATENCY_COUNT);        
        if(returnValueString != null) {
            try
            {
                candidateReturnValue = (Integer.valueOf(returnValueString)).intValue();
            } catch (NumberFormatException ex) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Using Default Value = 0");
                }
            }
        }
        //for now only allow 1 or 0
        if(candidateReturnValue == 1) {
            returnValue = candidateReturnValue;
        }
        return returnValue;        
    } 
    
    /**
    * returns the max_session_unload_time_in_seconds value 
    * in <availability-service>
    * if missing (or error) default to 7 minutes (7*60 seconds)
    */ 
    public int getMaxSessionUnloadTimeInSecondsPropertyFromConfig() {
        int candidateReturnValue = 7 * 60;
        int returnValue = 7 * 60;
        String returnValueString = 
            this.getAvailabilityServicePropertyString(MAX_SESSION_UNLOAD_TIME_IN_SECONDS);        
        if(returnValueString != null) {
            try
            {
                candidateReturnValue = (Integer.valueOf(returnValueString)).intValue();
            } catch (NumberFormatException ex) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Using Default Value = 7 * 60");
                }
            }
        }
        //only allow positive integers
        if(candidateReturnValue >= 0) {
            returnValue = candidateReturnValue;
        }
        return returnValue;        
    }      
    
    /**
    * returns the replication_measurement_interval property
    * used to control frequency of measurement output 
    * in <availability-service>
    * if missing (or error) default to 1000
    */ 
    public int getReplicationMeasurementIntervalFromConfig() {
        int candidateReturnValue = 1000;
        int returnValue = 1000;
        String returnValueString = 
            this.getAvailabilityServicePropertyString(REPLICATION_MEASUREMENT_INTERVAL);        
        if(returnValueString != null) {
            try
            {
                candidateReturnValue = (Integer.valueOf(returnValueString)).intValue();
            } catch (NumberFormatException ex) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Using Default Value = 1000");
                }
            }
        }
        //may return negative value
        if(candidateReturnValue > 0) {
            returnValue = candidateReturnValue;
        }
        return returnValue;        
    } 
    
    /**
    * returns the replication_measurement_enabled property
    * in <availability-service>
    * if missing (or error) default to false
    */
    public boolean getReplicationMeasurementEnabledFromConfig() {
        boolean returnValue = false;
        String returnValueString = 
            this.getAvailabilityServicePropertyString(REPLICATION_MEASUREMENT_ENABLED);        
        if(returnValueString != null) {
            returnValue = (Boolean.valueOf(returnValueString));
        }
        return returnValue;        
    }              
    
    /**
    * returns the wait_for_ack_property
    * in <availability-service>
    * if missing (or error) default to false
    */
    public boolean getWaitForAckPropertyFromConfig() {
        boolean returnValue = false;
        String returnValueString = 
            this.getAvailabilityServicePropertyString(WAIT_FOR_ACK_PROPERTY);        
        if(returnValueString != null) {
            returnValue = (Boolean.valueOf(returnValueString));
        }
        return returnValue;        
    }  
    
    /**
    * returns the wait_for_fast_ack_property
    * in <availability-service>
    * if missing (or error) default to true
    */
    public boolean getWaitForFastAckPropertyFromConfig() {
        boolean returnValue = true;
        String returnValueString = 
            this.getAvailabilityServicePropertyString(WAIT_FOR_FAST_ACK_PROPERTY);        
        if(returnValueString != null) {
            returnValue = (Boolean.valueOf(returnValueString));
        }
        return returnValue;        
    }     
    
    /**
    * Get the native_replication_enabled from domain.xml.
    * return true if not found
    */
    public boolean isNativeReplicationEnabledFromConfig() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in ServerConfigLookup>>getNativeReplicationEnabledFromConfig");
        }
        String nativeReplicationEnabledString =
            this.getAvailabilityServicePropertyString(NATIVE_REPLICATION_ENABLED);
        Boolean bool = this.toBoolean(nativeReplicationEnabledString);
        if(bool == null) {
            return true;
        } else {
            return bool.booleanValue();
        }        
    }    
    
    /**
    * Get the resources element from domain.xml.
    * return null if not found
    */    
    private Resources getResourcesBean() {
        Resources resourcesBean = null;
        Domain domainBean = null;
        /*
        ServerContext serverCtx = ApplicationServer.getServerContext();
        ConfigContext configCtx = serverCtx.getConfigContext();
         */
        ConfigContext configCtx = this.getConfigContext();
        try {
            domainBean = ServerBeansFactory.getDomainBean(configCtx);
            if(domainBean != null) {
                resourcesBean = domainBean.getResources();
            }
        } catch (ConfigException ex) {}

        return resourcesBean;
    } 

    /**
    * Get the applications element from domain.xml.
    * return null if not found
    */     
    private Applications getApplicationsBean() {
        Applications applicationsBean = null;
        Domain domainBean = null;
        /*
        ServerContext serverCtx = ApplicationServer.getServerContext();
        ConfigContext configCtx = serverCtx.getConfigContext();
         */
        ConfigContext configCtx = this.getConfigContext();
        try {
            domainBean = ServerBeansFactory.getDomainBean(configCtx);
            if(domainBean != null) {
                applicationsBean = domainBean.getApplications();
            }
        } catch (ConfigException ex) {}

        return applicationsBean;
    }
    
    /**
    * Get the applications element from domain.xml.
    * return null if not found
    */     
    private Applications getApplicationsBeanDynamic() {
        Applications applicationsBean = null;
        Domain domainBean = null;
        /*
        ServerContext serverCtx = ApplicationServer.getServerContext();
        ConfigContext configCtx = serverCtx.getConfigContext();
         */
        ConfigContext configCtx = this.getConfigContextDynamic();
        try {
            domainBean = ServerBeansFactory.getDomainBean(configCtx);
            if(domainBean != null) {
                applicationsBean = domainBean.getApplications();
            }
        } catch (ConfigException ex) {}
        return applicationsBean;
    }    

    /**
    * Get the config element from domain.xml.
    * return null if not found
    */     
    private Config getConfigBean() {
        Config configBean = null;
        /*
        ServerContext serverCtx = ApplicationServer.getServerContext();
        ConfigContext configCtx = serverCtx.getConfigContext(); 
         */
        ConfigContext configCtx = this.getConfigContext();
        try {
            configBean = ServerBeansFactory.getConfigBean(configCtx);
        } catch (ConfigException ex) {}

        return configBean;
    }
    
    /**
    * Get the config element from domain.xml.
    * return null if not found
    */     
    private Config getConfigBeanDynamic() {
        Config configBean = null;
        ConfigContext configCtx = this.getConfigContextDynamic();
        try {
            configBean = ServerBeansFactory.getConfigBean(configCtx);
        } catch (ConfigException ex) {}

        return configBean;
    }
    
    /**
    * Get the http-service element from domain.xml.
    * return null if not found
    */     
    protected HttpService getHttpService() {
        Config configBean = this.getConfigBean();
        if(configBean == null) {
            return null;
        }
        return configBean.getHttpService();
    }
    
    /**
    * Get the request-processing element from domain.xml.
    * return null if not found
    */     
    private RequestProcessing getRequestProcessing() {
        HttpService httpServiceBean = this.getHttpService();
        if(httpServiceBean == null) {
            return null;
        }
        return httpServiceBean.getRequestProcessing();
    }
    
    /**
    * Get the request-processing thread-count attribute from domain.xml.
    * return null if not found
    */     
    private String getRequestProcessingThreadCountStringFromConfig() {
        RequestProcessing requestProcessingBean = this.getRequestProcessing();
        if(requestProcessingBean == null) {
            return null;
        }
        return requestProcessingBean.getThreadCount();
    } 
    
    /**
    * Get the thread-count attribute in request-processing 
    * element from domain.xml.
    * returns the attribute thread-count 
    * in <request-processing>
    * if missing (or error) default to DEFAULT_REQUEST_PROCESSING_THREAD_COUNT
    */ 
    public int getRequestProcessingThreadCountFromConfig() {
        int candidateReturnValue = -1;
        int returnValue 
            = DEFAULT_REQUEST_PROCESSING_THREAD_COUNT;        
        String returnValueString = 
            this.getRequestProcessingThreadCountStringFromConfig();
        if(returnValueString != null) {
            try
            {
                candidateReturnValue = (Integer.valueOf(returnValueString)).intValue();
            } catch (NumberFormatException ex) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Using Default Value = " + DEFAULT_REQUEST_PROCESSING_THREAD_COUNT);
                }
            }
        }
        //insure we have at least 5 pipes
        if(candidateReturnValue > 4) {
            returnValue = candidateReturnValue;
        }
        return returnValue;        
    }    

    /**
    * Get the availability-service element from domain.xml.
    * return null if not found
    */     
    protected AvailabilityService getAvailabilityService() {
        Config configBean = this.getConfigBean();
        if(configBean == null) {
            return null;
        }
        return configBean.getAvailabilityService();
    }
    
    /**
    * Get the availability-service element from domain.xml.
    * return null if not found
    */     
    protected AvailabilityService getAvailabilityServiceDynamic() {
        Config configBean = this.getConfigBeanDynamic();
        if(configBean == null) {
            return null;
        }
        return configBean.getAvailabilityService();
    }    

    /**
    * Get the web-container-availability element from domain.xml.
    * return null if not found
    */     
    private WebContainerAvailability getWebContainerAvailability() {
        AvailabilityService availabilityServiceBean = this.getAvailabilityService();
        if(availabilityServiceBean == null) {
            return null;
        }
        return availabilityServiceBean.getWebContainerAvailability();
    } 

    /**
    * Get the properties under the web-container-availability element 
    * from domain.xml.
    * return array of single empty ElementProperty if not found
    */     
    private ElementProperty[] getWebContainerAvailabilityProperties() {
        WebContainerAvailability webContainerAvailabilityBean = 
            this.getWebContainerAvailability();
        if(webContainerAvailabilityBean == null) {
            return new ElementProperty[0];
        }
        return webContainerAvailabilityBean.getElementProperty();
    }    

    /**
    * Get the String value of the property under web-container-availability 
    * element from domain.xml whose name matches propName
    * return null if not found
    * @param propName
    */     
    protected String getWebContainerAvailabilityPropertyString(String propName) {
        String result = null;
        WebContainerAvailability wcAvailabilityBean = this.getWebContainerAvailability();
        if( (wcAvailabilityBean != null) && (wcAvailabilityBean.sizeElementProperty() > 0) ) {
            ElementProperty[] props = wcAvailabilityBean.getElementProperty();
            for (int i = 0; i < props.length; i++) {
                String name = props[i].getAttributeValue("name");
                String value = props[i].getAttributeValue("value");
                if (name.equalsIgnoreCase(propName)) {
                    result = value;
                }
            }
        }
        return result;
    } 

    /**
    * Get the String value of the property under web-container-availability 
    * element from domain.xml whose name matches propName
    * return defaultValue if not found
    * @param propName
    */    
    protected String getWebContainerAvailabilityPropertyString(String propName, String defaultValue) {
        String result = null;
        WebContainerAvailability wcAvailabilityBean = this.getWebContainerAvailability();
        if( (wcAvailabilityBean != null) && (wcAvailabilityBean.sizeElementProperty() > 0) ) {
            ElementProperty[] props = wcAvailabilityBean.getElementProperty();
            for (int i = 0; i < props.length; i++) {
                String name = props[i].getAttributeValue("name");
                String value = props[i].getAttributeValue("value");
                if (name.equalsIgnoreCase(propName)) {
                    result = value;
                }
            }
        }
        if(result == null) {
            result = defaultValue;
        }
        return result;
    }

    /**
    * Get the int value of the property under web-container-availability 
    * element from domain.xml whose name matches propName
    * return defaultValue if not found
    * @param propName
    */     
    protected int getWebContainerAvailabilityPropertyInt(String propName, int defaultValue) {
        int returnValue = defaultValue;
        String returnValueString = null;
        WebContainerAvailability wcAvailabilityBean = this.getWebContainerAvailability();
        if( (wcAvailabilityBean != null) && (wcAvailabilityBean.sizeElementProperty() > 0) ) {
            ElementProperty[] props = wcAvailabilityBean.getElementProperty();
            for (int i = 0; i < props.length; i++) {
                String name = props[i].getAttributeValue("name");
                String value = props[i].getAttributeValue("value");
                if (name.equalsIgnoreCase(propName)) {
                    returnValueString = value;
                }
            }
        }
        if(returnValueString != null) {
            try {
                returnValue = (Integer.valueOf(returnValueString)).intValue();
            } catch (NumberFormatException ex) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Using Default Value = "
                        + defaultValue);
                }
            }
        }
        return returnValue;
    }
    
    private com.sun.enterprise.config.serverbeans.WebModule getWebModuleByContextRoot(String contextRoot) {
        com.sun.enterprise.config.serverbeans.WebModule result = null;
        Applications applicationsBean = this.getApplicationsBeanDynamic();
        if(applicationsBean == null) {
            return null;
        }
        com.sun.enterprise.config.serverbeans.WebModule[] webModules =
            applicationsBean.getWebModule();
        for(int i=0; i<webModules.length; i++) {
            com.sun.enterprise.config.serverbeans.WebModule nextWebModule =
                webModules[i];
            String nextContextRoot = nextWebModule.getContextRoot();
            if(nextContextRoot != null && nextContextRoot.equalsIgnoreCase(contextRoot)) {
                result = nextWebModule;
            }
        }
        return result;
    }

    private boolean getWebModuleAvailability(String contextRoot, boolean inheritedValue) {
        com.sun.enterprise.config.serverbeans.WebModule webModule =
            this.getWebModuleByContextRoot(contextRoot);
        if(webModule == null) {
            //FIXME remove after testing
            return false;
            //return inheritedValue;
        }
        /*
        String wmsString = webModule.getAvailabilityEnabled();
        Boolean bool = this.toBoolean(wmsString);
        
        if(bool == null) {
            return inheritedValue;
        } else {
            return bool.booleanValue();
        } 
         */
        return webModule.isAvailabilityEnabled();
    }
    
    private J2eeApplication getJ2eeApplicationByName(String appName) {
        J2eeApplication result = null;
        Applications applicationsBean = this.getApplicationsBeanDynamic();
        if(applicationsBean == null) {
            return null;
        }
        return applicationsBean.getJ2eeApplicationByName(appName);
    }
    
    private boolean getJ2eeApplicationAvailability(String appName, boolean inheritedValue) {
        J2eeApplication j2eeApp =
            this.getJ2eeApplicationByName(appName);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ServerConfigLookup>>getJ2eeApplicationAvailability j2eeApp = " + j2eeApp);
        }

        if(j2eeApp == null) {
            //FIXME remove after testing - protection if called in web module case
            //return inheritedValue;
            return false;
        }
        /*
        String appString = j2eeApp.getAvailabilityEnabled();
        Boolean bool = this.toBoolean(appString);
        if(bool == null) {
            return inheritedValue;
        } else {
            return bool.booleanValue();
        } 
         */
        return j2eeApp.isAvailabilityEnabled();
    }    
    
    //+++++++++++++++++++++++++END NEW METHODS++++++++++++++++++++++ 
    
    /**
    * Get the persistenceType from domain.xml.
    * return null if not found
    */
    public PersistenceType getPersistenceTypeFromConfig() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in ServerConfigLookup>>getPersistenceTypeFromConfig");
        }
        String persistenceTypeString = null;      
        PersistenceType persistenceType = null;

        WebContainerAvailability webContainerAvailabilityBean =
        this.getWebContainerAvailability();
        if(webContainerAvailabilityBean == null) {
            return null;
        }
        persistenceTypeString = webContainerAvailabilityBean.getPersistenceType();

        if(persistenceTypeString != null) {
            persistenceType = PersistenceType.parseType(persistenceTypeString);
        }
        if(persistenceType != null) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("SERVER.XML persistenceType= " + persistenceType.getType());
            }
        } else {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("SERVER.XML persistenceType missing");
            }
        }
        return persistenceType;
    }     
    
    /**
    * Get the persistenceFrequency from domain.xml.
    * return null if not found
    * 7.0 xpath was "/server/web-container/session-config/session-manager/manager-properties/property[@name='persistenceFrequency']";
    */
    public String getPersistenceFrequencyFromConfig() { 
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in ServerConfigLookup>>getPersistenceFrequencyFromConfig");
        }
      
        WebContainerAvailability webContainerAvailabilityBean =
        this.getWebContainerAvailability();
        if(webContainerAvailabilityBean == null) {
            return null;
        }
        return webContainerAvailabilityBean.getPersistenceFrequency();      
    }
    

    /**
    * Get the persistenceScope from domain.xml.
    * return null if not found
    * 7.0 xpath was "/server/web-container/session-config/session-manager/store-properties/property[@name='persistenceScope']";
    */
    public String getPersistenceScopeFromConfig() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in ServerConfigLookup>>getPersistenceScopeFromConfig");
        }
        WebContainerAvailability webContainerAvailabilityBean =
            this.getWebContainerAvailability();
        if(webContainerAvailabilityBean == null) {
            return null;
        }
        return webContainerAvailabilityBean.getPersistenceScope(); 
    }     
  
    /**
    * Get the stale-session-checking-enabled from domain.xml.
    * return false if not found
    */
    public boolean getStaleSessionCheckingFromConfig() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in ServerConfigLookup>>getStaleSessionCheckingFromConfig");
        }
        String staleSessionCheckingEnabledString =
            this.getWebContainerAvailabilityPropertyString(STALE_SESSION_CHECKING_ENABLED_PROPERTY_NAME, "false");
        Boolean bool = this.toBoolean(staleSessionCheckingEnabledString);
        if(bool == null) {
            return false;
        } else {
            return bool.booleanValue();
        }        
    }  
    
    /**
    * Get the cluster-id from domain.xml.
    * return "cluster1" if not found
    */
    public String getClusterIdFromConfig() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in ServerConfigLookup>>getClusterIdFromConfig");
        }
        return this.getWebContainerAvailabilityPropertyString(CLUSTER_ID_PROPERTY_NAME, DEFAULT_CLUSTER_ID);
    }    
    
    /**
    * Get the availability-enabled from domain.xml.
    * return false if not found
    */   
    public boolean getAvailabilityEnabledFromConfig() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in ServerConfigLookup>>getAvailabilityEnabledFromConfig");
        }
        AvailabilityService as = this.getAvailabilityService();
        if(as == null) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("AvailabilityService was not defined - check domain.xml");
            }
            return false;
        }        
        return as.isAvailabilityEnabled();
    }
    
    /**
    * Get the availability-enabled from domain.xml.
    * This takes into account:
    * global
    * web-container-availability
    * web-module (if stand-alone)
    * return false if not found
    */   
    public boolean calculateWebAvailabilityEnabledFromConfig(String contextRoot, String j2eeAppName) { 
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in ServerConfigLookup>>calculateWebAvailabilityEnabledFromConfig");
        }

        //global availability from <availability-service> element
        boolean globalAvailability = 
            this.getAvailabilityEnabledFromConfig();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("globalAvailability = " + globalAvailability);
        }

        //web container availability from <web-container-availability> sub-element
        boolean webContainerAvailability = 
            this.getWebContainerAvailabilityEnabledFromConfig(globalAvailability);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("webContainerAvailability = " + webContainerAvailability);
        }        

        if(j2eeAppName == null || "null".equals(j2eeAppName)) {
            //the stand-alone web module case
            boolean webModuleAvailability =
                this.getWebModuleAvailability(contextRoot, webContainerAvailability);
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("webModuleAvailability = " + webModuleAvailability);
            }

            return globalAvailability 
                    && webContainerAvailability 
                    && webModuleAvailability;
        } else {
            //the j2ee application case
            boolean j2eeApplicationAvailability =
                this.getJ2eeApplicationAvailability(j2eeAppName, webContainerAvailability);
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("j2eeApplicationAvailability = " + j2eeApplicationAvailability);            
            }

            return globalAvailability 
                    && webContainerAvailability 
                    && j2eeApplicationAvailability;
        }
    }    
    
    /**
    * Get the availability-enabled for the web container from domain.xml.
    * return inherited global availability-enabled if not found
    */   
    public boolean getWebContainerAvailabilityEnabledFromConfig() {
        boolean globalAvailabilityEnabled = this.getAvailabilityEnabledFromConfig();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in ServerConfigLookup>>getWebContainerAvailabilityEnabledFromConfig");
        }
        WebContainerAvailability was = this.getWebContainerAvailability();
        if(was == null) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("WebContainerAvailability was not defined - check domain.xml");
            }
            return globalAvailabilityEnabled;
        }
        
        String wasString = was.getAvailabilityEnabled();
        Boolean bool = this.toBoolean(wasString);
        if(bool == null) {
            return globalAvailabilityEnabled;
        } else {
            return bool.booleanValue();
        }       
    } 
    
    /**
    * Get the availability-enabled for the web container from domain.xml.
    * return inherited global availability-enabled if not found
    */   
    public boolean getWebContainerAvailabilityEnabledFromConfig(boolean inheritedValue) {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in ServerConfigLookup>>getWebContainerAvailabilityEnabledFromConfig");
        }
        WebContainerAvailability was = this.getWebContainerAvailability();
        if(was == null) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("WebContainerAvailability was not defined - check domain.xml");
            }
            return inheritedValue;
        }
        
        String wasString = was.getAvailabilityEnabled();
        Boolean bool = this.toBoolean(wasString);
        if(bool == null) {
            return inheritedValue;
        } else {
            return bool.booleanValue();
        }       
    }    

    /**
    * convert the input value to the appropriate Boolean value
    * if input value is null, return null
    */     
    protected Boolean toBoolean(String value){
        if(value == null) return null;
        
        if (value.equalsIgnoreCase("true"))
            return Boolean.TRUE;
        if (value.equalsIgnoreCase("yes"))
            return Boolean.TRUE;
        if (value.equalsIgnoreCase("on") )
            return Boolean.TRUE;
        if (value.equalsIgnoreCase("1"))
            return Boolean.TRUE;
    
        return Boolean.FALSE;
    }       

    /**
    * Get the store-pool-jndi-name from domain.xml.
    * This is the store-pool-name in <availability-service> element
    * it represents the default for both web & ejb container
    * return DEFAULT_STORE_POOL_JNDI_NAME if not found
    */    
    public String getStorePoolJndiNameFromConfig() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in ServerConfigLookup>>getStorePoolJndiNameFromConfig");
        }
        String result = DEFAULT_STORE_POOL_JNDI_NAME;
        AvailabilityService as = this.getAvailabilityService();
        if(as == null) {
            return result;
        }
        String storePoolJndiName = as.getStorePoolName();
        if(storePoolJndiName != null) {
            result = storePoolJndiName;
        }
        return result;
    }
    
    /**
    * Get the store-pool-jndi-name from domain.xml.
    * return value returned from getStorePoolJndiNameFromConfig if not found
    */
    public String getHaStorePoolJndiNameFromConfig() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in ServerConfigLookup>>getHaStorePoolJndiNameFromConfig");
        }
        String result = this.getStorePoolJndiNameFromConfig();
        WebContainerAvailability webContainerAvailabilityBean =
            this.getWebContainerAvailability();
        if(webContainerAvailabilityBean == null) {
            return result;
        }
        String result2 = webContainerAvailabilityBean.getHttpSessionStorePoolName();
        if(result2 != null) {
            result = result2;
        }
        return result; 
    }        
    
    /**
    * Get the hadb health check string from domain.xml.
    *7.0 xpath was "/server/availability-service/persistence-store/property[@name='hadb-health-check-enabled']"
    * returns the attribute ha-store-healthcheck-enabled in <availability-service>
    */
    public boolean getHadbHealthCheckFromConfig() {
        AvailabilityService as = this.getAvailabilityService();
        if(as == null) {
            return false;
        }
        return as.isHaStoreHealthcheckEnabled();
    } 
    
    /**
    * Get the hadb health check string from domain.xml.
    *7.0 xpath was "/server/availability-service/persistence-store/property[@name='hadb-health-check-enabled']"
    * returns the attribute ha-store-healthcheck-enabled in <availability-service>
    */
    public boolean getHadbHealthCheckFromConfigDynamic() {
        AvailabilityService as = this.getAvailabilityServiceDynamic();
        if(as == null) {
            return false;
        }
        return as.isHaStoreHealthcheckEnabled();
    }    
    
    /**
    * Get the sso-failover-enabled boolean from domain.xml.
    */
    public boolean getSsoFailoverEnabledFromConfig() {
        WebContainerAvailability webContainerAvailabilityBean =
        this.getWebContainerAvailability();
        if(webContainerAvailabilityBean == null) {
            return false;
        }
        return webContainerAvailabilityBean.isSsoFailoverEnabled(); 
    }     
  
    /**
    * Get the hadb management environment path from domain.xml.
    * this is the location of cladm
    * 7.0 xpath was "/server/availability-service/persistence-store/property[@name='hadb-mgmt-env-path']" 
    */
    public String getHadbMgmtEnvPathFromConfig() {
        return this.getWebContainerAvailabilityPropertyString(HADB_MGMT_ENV_PATH_PROPERTY_NAME, null);
        //return this.getAvailServicePersistenceStorePropertyString(HADB_MGMT_ENV_PATH_PROPERTY_NAME, null); 
    }     
    
    ///begin HADB Health Check Stuff
    /**
    * Get the String value of the property under availability-service 
    * element from domain.xml whose name matches propName
    * return null if not found
    * @param propName
    */     
    public String getAvailabilityServicePropertyString(String propName) {
        String result = null;
        AvailabilityService availabilityServiceBean = this.getAvailabilityService();
        if( (availabilityServiceBean != null) && (availabilityServiceBean.sizeElementProperty() > 0) ) {
            ElementProperty[] props = availabilityServiceBean.getElementProperty();
            for (int i = 0; i < props.length; i++) {
                String name = props[i].getAttributeValue("name");
                String value = props[i].getAttributeValue("value");
                if (name.equalsIgnoreCase(propName)) {
                    result = value;
                }
            }
        }
        return result;
    }   

    /**
    * Get the String value of the ha-store-name attribute under availability-service 
    * element from domain.xml 
    * return null if not found
    */    
    public String getHadbDatabaseNameFromConfig() {
        AvailabilityService as = this.getAvailabilityService();
        if(as == null) {
            return null;
        }
        return as.getHaStoreName();
    } 
    
    /**
    * Get the String value of the ha-store-name attribute under availability-service 
    * element from domain.xml 
    * return null if not found
    */    
    public String getHadbDatabaseNameFromConfigDynamic() {
        AvailabilityService as = this.getAvailabilityServiceDynamic();
        if(as == null) {
            return null;
        }
        return as.getHaStoreName();
    }    

    /**
    * Get the String value of the ha-agent-password attribute under availability-service 
    * element from domain.xml 
    * return null if not found
    */
    public String getHadbAgentPasswordFromConfig() {
        AvailabilityService as = this.getAvailabilityService();
        if(as == null) {
            return null;
        }
        return as.getHaAgentPassword();
    }    
    
    /**
    * Get the String value of the ha-agent-password property under availability-service 
    * element from domain.xml 
    * return null if not found
    */ 
    /* Old version based on property - remove after testing
    public String getHadbAgentPasswordFromConfig() {
        return this.getAvailabilityServicePropertyString(HADB_AGENT_PASSWORD);
    }
     */ 

    /**
    * Get the String value of the ha-store-healthcheck-interval-in-seconds attribute under availability-service 
    * element from domain.xml 
    * return null if not found
    */ 
    public String getHaStoreHealthcheckIntervalInSecondsStringFromConfig() { 
        AvailabilityService as = this.getAvailabilityService();
        if(as == null) {
            return null;
        }
        return as.getHaStoreHealthcheckIntervalInSeconds();        
    }    
    
    /**
    * Get the ha-store-healthcheck-interval-in-seconds from domain.xml.
    * returns the attribute ha-store-healthcheck-interval-in-seconds 
    * in <availability-service>
    * if missing (or error) default to DEFAULT_HA_STORE_HEALTHCHECK_INTERVAL_IN_SECONDS
    */ 
    public int getHaStoreHealthcheckIntervalInSecondsFromConfig() {
        int candidateReturnValue = -1;
        int returnValue 
            = DEFAULT_HA_STORE_HEALTHCHECK_INTERVAL_IN_SECONDS;        
        String returnValueString = 
            this.getHaStoreHealthcheckIntervalInSecondsStringFromConfig();
        if(returnValueString != null) {
            try
            {
                candidateReturnValue = (Integer.valueOf(returnValueString)).intValue();
            } catch (NumberFormatException ex) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Using Default Value = " + DEFAULT_HA_STORE_HEALTHCHECK_INTERVAL_IN_SECONDS);
                }
            }
        }
        //avoid returning negative or zero values
        if(candidateReturnValue > 0) {
            returnValue = candidateReturnValue;
        }
        return returnValue;        
    }
    
    /**
    * Get the String value of the ha-store-healthcheck-interval-in-seconds attribute under availability-service 
    * element from domain.xml 
    * return null if not found
    */ 
    public String getHaStoreHealthcheckIntervalInSecondsStringFromConfigDynamic() { 
        AvailabilityService as = this.getAvailabilityServiceDynamic();
        if(as == null) {
            return null;
        }
        return as.getHaStoreHealthcheckIntervalInSeconds();        
    }    
    
    /**
    * Get the ha-store-healthcheck-interval-in-seconds from domain.xml.
    * returns the attribute ha-store-healthcheck-interval-in-seconds 
    * in <availability-service>
    * if missing (or error) default to DEFAULT_HA_STORE_HEALTHCHECK_INTERVAL_IN_SECONDS
    */ 
    public int getHaStoreHealthcheckIntervalInSecondsFromConfigDynamic() {
        int candidateReturnValue = -1;
        int returnValue 
            = DEFAULT_HA_STORE_HEALTHCHECK_INTERVAL_IN_SECONDS;        
        String returnValueString = 
            this.getHaStoreHealthcheckIntervalInSecondsStringFromConfigDynamic();
        if(returnValueString != null) {
            try
            {
                candidateReturnValue = (Integer.valueOf(returnValueString)).intValue();
            } catch (NumberFormatException ex) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Using Default Value = "
                        + DEFAULT_HA_STORE_HEALTHCHECK_INTERVAL_IN_SECONDS);
                }
            }
        }
        //avoid returning negative or zero values
        if(candidateReturnValue > 0) {
            returnValue = candidateReturnValue;
        }
        return returnValue;        
    }     
 
    /**
    * Get the String value of the ha-agent-port attribute under availability-service 
    * element from domain.xml 
    * return null if not found
    */    
    public String getHadbAgentPortFromConfig() {
        AvailabilityService as = this.getAvailabilityService();
        if(as == null) {
            return null;
        }
        return as.getHaAgentPort();
    }
    
    /**
    * Get the String value of the ha-agent-port attribute under availability-service 
    * element from domain.xml 
    * return null if not found
    */    
    public String getHadbAgentPortFromConfigDynamic() {
        AvailabilityService as = this.getAvailabilityServiceDynamic();
        if(as == null) {
            return null;
        }
        return as.getHaAgentPort();
    }    
    
    /**
     * Get the connectionURL for hadb agent(s) from domain.xml.
     */
    public String getHadbAgentConnectionURLFromConfig() {
        String url = null;
        StringBuffer sb = new StringBuffer();
        String hostsString = this.getHadbAgentHostsFromConfig();
        String portString = this.getHadbAgentPortFromConfig();
        if(hostsString != null && portString != null) { 
            sb.append(hostsString);
            sb.append(":");
            sb.append(portString);
            url = sb.toString();
        } else {
            url = null;
        }
        return url;
    }
    
    /**
     * Get the connectionURL for hadb agent(s) from domain.xml.
     */
    public String getHadbAgentConnectionURLFromConfigDynamic() {
        String url = null;
        StringBuffer sb = new StringBuffer();
        String hostsString = this.getHadbAgentHostsFromConfigDynamic();
        String portString = this.getHadbAgentPortFromConfigDynamic();
        if(hostsString != null && portString != null) { 
            sb.append(hostsString);
            sb.append(":");
            sb.append(portString);
            url = sb.toString();
        } else {
            url = null;
        }
        return url;
    }    

    /**
    * Get the String value of the ha-agent-hosts attribute under availability-service 
    * element from domain.xml 
    * return null if not found
    */    
    public String getHadbAgentHostsFromConfig() {
        AvailabilityService as = this.getAvailabilityService();
        if(as == null) {
            return null;
        }
        return as.getHaAgentHosts();
    } 
    
    /**
    * Get the String value of the ha-agent-hosts attribute under availability-service 
    * element from domain.xml 
    * return null if not found
    */    
    public String getHadbAgentHostsFromConfigDynamic() {
        AvailabilityService as = this.getAvailabilityServiceDynamic();
        if(as == null) {
            return null;
        }
        return as.getHaAgentHosts();
    }    
    
    ///end HADB Health Check Stuff
  
    /**
    * Get the EE_BUILDER_PATH from server.xml.
    * this defaults to EE_BUILDER_PATH but can be modified
    * this is the fully qualified path to the EE builders
    * 7.0 xpath was "/server/availability-service/persistence-store/property[@name='ee-builder-path']"
    */
    public String getEEBuilderPathFromConfig() {
        return this.getWebContainerAvailabilityPropertyString(EE_BUILDER_PATH_PROPERTY_NAME, DEFAULT_EE_BUILDER_PATH);
        //return this.getAvailServicePersistenceStorePropertyString(EE_BUILDER_PATH_PROPERTY_NAME, DEFAULT_EE_BUILDER_PATH);         
    }     
    
    /**
    * Get the UuidGenerator implementation class name from domain.xml.
    * 7.0 xpath was "/server/availability-service/persistence-store/property[@name='uuid-impl-class']", if specified.
    * Use value of session-id-generator-classname attribute of
    * "/server/web-container/session-config/session-manager/manager-properties"
    * as fallback, or DEFAULT_UUID_GENERATOR_CLASS if no fallback specified.
    */
    public String getUuidGeneratorImplClassFromConfig() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest(
                "in ServerConfigLookup>>getUuidGeneratorImplClassFromConfig");
        }
        String defaultUuidGeneratorClass = DEFAULT_UUID_GENERATOR_CLASS;
        ManagerProperties mp = getInstanceSessionManagerManagerProperties();
        if (mp != null) {
            String cls = mp.getSessionIdGeneratorClassname();
            if (cls != null) {
                defaultUuidGeneratorClass = cls;
            }
        }
        return this.getWebContainerAvailabilityPropertyString(UUID_GENERATOR_CLASS_PROPERTY_NAME, defaultUuidGeneratorClass);
    }          
  
    /**
    * Get the UuidGenerator implementation class from server.xml.
    * 7.0 xpath was "/server/availability-service/persistence-store/property[@name='uuid-impl-class']";
    */
    public UuidGenerator getUuidGeneratorFromConfig() {
      UuidGenerator generator = new UuidGeneratorImpl();
      String generatorImplClassName = 
        this.getUuidGeneratorImplClassFromConfig();  
      try {
          generator = 
                (UuidGenerator) (Class.forName(generatorImplClassName)).newInstance();
      } catch (Exception ex) {            
      } 
      return generator;
    }
  
    /**
    * Get the value from server.xml for xpath
    * return defaultValue if not defined or other problem
    */
    public String getServerConfigValue(String xpath, String defaultValue ) {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in ServerConfigLookup>>getServerConfigValue:xpath=" + xpath
                + " defaultValue= " + defaultValue);
        }

      ServerContext serverCtx = ApplicationServer.getServerContext();
      //this condition occurs during some unit tests
      if(serverCtx == null)
          return defaultValue;
      ConfigContext configCtx = serverCtx.getConfigContext();    
      ConfigBean configBean = null;
      String returnValueString = null;

      String returnValue = defaultValue;
      try {
          configBean =
              configCtx.exactLookup(xpath);
      } catch (ConfigException ex) {
      }      
      if(configBean != null) {
          returnValueString = configBean.getAttributeValue("value");
      }
      if(returnValueString != null) {
          returnValue = returnValueString;
      }
      if(_logger.isLoggable(Level.FINEST)) {
          _logger.finest("RETURNED CONFIG VALUE FOR XPATH:" + xpath +
            " = " + returnValue);
      }

      return returnValue;
    }
  
    /**
    * Get the value from server.xml for xpath
    * return defaultValue if not defined or other problem
    */
    public int getServerConfigValue(String xpath, int defaultValue ) {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in getServerConfigValue:xpath=" + xpath
                + " defaultValue= " + defaultValue);
        }
        /*
        ServerContext serverCtx = ApplicationServer.getServerContext();
        ConfigContext configCtx = serverCtx.getConfigContext();
        */
        ConfigContext configCtx = this.getConfigContext();
        ConfigBean configBean = null;
        String returnValueString = null;

        int returnValue = defaultValue;
        try {
            configBean =
                configCtx.exactLookup(xpath);
        } catch (ConfigException ex) {
        }      
        if(configBean != null) {
            returnValueString = configBean.getAttributeValue("value");
        }
        if(returnValueString != null) {
            try {
                returnValue = (Integer.valueOf(returnValueString)).intValue();
            } catch (NumberFormatException ex) {
                _logger.finest("Using Default Value = " + defaultValue);
            }
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("RETURNED CONFIG VALUE FOR XPATH:" + xpath +
                " = " + returnValue);
        }

        return returnValue;
    }   

    /**
    * Get the session manager bean from domain.xml
    * return null if not defined or other problem
    * 7.0 xpath was "/server/web-container/session-config/session-manager";
    */  
    public SessionManager getInstanceSessionManager() { 
        Config configBean = this.getConfigBean();
        if(configBean == null) {
            return null;
        }
        
        com.sun.enterprise.config.serverbeans.WebContainer webContainerBean 
            = configBean.getWebContainer();
        if(webContainerBean == null) {
            return null;
        }
        
        SessionConfig sessionConfigBean = webContainerBean.getSessionConfig();
        if(sessionConfigBean == null) {
            return null;
        }
        
        com.sun.enterprise.config.serverbeans.SessionManager smBean =
            sessionConfigBean.getSessionManager();
        return smBean;
    }    
    
    /**
    * Get the manager properties bean from domain.xml
    * return null if not defined or other problem
    * 7.0 xpath was "/server/web-container/session-config/session-manager/manager-properties";
    */  
    public ManagerProperties getInstanceSessionManagerManagerProperties() {
        
        SessionManager smBean = this.getInstanceSessionManager();
        if(smBean == null) {
            return null;
        }
        return smBean.getManagerProperties();
    } 
    
    /**
    * Get the store properties bean from domain.xml
    * return null if not defined or other problem
    * 7.0 xpath was "/server/web-container/session-config/session-manager/store-properties";
    */  
    public StoreProperties getInstanceSessionManagerStoreProperties() {
        
        SessionManager smBean = this.getInstanceSessionManager();
        if(smBean == null) {
            return null;
        }
        return smBean.getStoreProperties();
    } 

    /**
    * Get the session properties bean from server.xml
    * return null if not defined or other problem
    * 7.0 xpath was "/server/web-container/session-config/session-properties";
    */      
    public SessionProperties getInstanceSessionProperties() { 
        Config configBean = this.getConfigBean();
        if(configBean == null) {
            return null;
        }
        
        com.sun.enterprise.config.serverbeans.WebContainer webContainerBean 
            = configBean.getWebContainer();
        if(webContainerBean == null) {
            return null;
        }
        
        SessionConfig sessionConfigBean = webContainerBean.getSessionConfig();
        if(sessionConfigBean == null) {
            return null;
        }
        
        com.sun.enterprise.config.serverbeans.SessionProperties spBean =
            sessionConfigBean.getSessionProperties();
        return spBean;
    }        
     
    /**
    * Get the connectionUser from domain.xml.
    */ 
    public String getConnectionUserFromConfig() {

        String user = null;
        JdbcConnectionPool pool = this.getHadbJdbcConnectionPoolFromConfig();
        if(pool == null) {
            return null;
        }
        if (pool.sizeElementProperty() > 0) {
            ElementProperty[] props = pool.getElementProperty();
            for (int i = 0; i < props.length; i++) {
                String name = props[i].getAttributeValue("name");
                String value = props[i].getAttributeValue("value");
                if (name.equalsIgnoreCase(USER_NAME)) {
                    user = value; 
                }
            }
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN NEW getConnectionUserFromConfig: user=" + user);
        }
        return user;
    }
    
    /**
    * Get the connectionPassword from domain.xml.
    */ 
    public String getConnectionPasswordFromConfig() {

        String password = null;
        JdbcConnectionPool pool = this.getHadbJdbcConnectionPoolFromConfig();
        if(pool == null)
            return null;
        if (pool.sizeElementProperty() > 0) {
            ElementProperty[] props = pool.getElementProperty();
            for (int i = 0; i < props.length; i++) {
                String name = props[i].getAttributeValue("name");
                String value = props[i].getAttributeValue("value");
                if (name.equalsIgnoreCase(PASSWORD)) {
                    password = value; 
                }
            }
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN NEW getConnectionPasswordFromConfig: password="
                           + password);        
        }
        return password;
    }
    
    /**
     * Get the connectionURL from domain.xml.
     */
    public String getConnectionURLFromConfig() {
        String url = null;
        StringBuffer sb = new StringBuffer();
        JdbcConnectionPool pool = this.getHadbJdbcConnectionPoolFromConfig();
        if(pool == null)
            return null;
        if (pool.sizeElementProperty() > 0) {
            ElementProperty[] props = pool.getElementProperty();
            for (int i = 0; i < props.length; i++) {
                String name = props[i].getAttributeValue("name");
                String value = props[i].getAttributeValue("value");
                if (name.equalsIgnoreCase("serverList")) {
                    sb.append(HADB_CONNECTION_URL_PREFIX);
                    sb.append(value);
                    url = sb.toString(); 
                }
            }
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN NEW getConnectionURLFromConfig: url=" + url); 
        }
        return url;
    } 
   
    /**
    * Get the JdbcConnectionPool for HADB from domain.xml.
    * return null if not found
    */ 
    public JdbcConnectionPool getHadbJdbcConnectionPoolFromConfig() {
        String storePoolJndiName = this.getHaStorePoolJndiNameFromConfig();
        if(storePoolJndiName == null)
            return null;

        Resources resources = this.getResourcesBean();
        JdbcResource jdbcResource = 
            resources.getJdbcResourceByJndiName(storePoolJndiName);
        if(jdbcResource == null) {
            return null;
        }
                
        String poolName = jdbcResource.getPoolName();
        JdbcConnectionPool pool = 
            resources.getJdbcConnectionPoolByName(poolName);
        return pool;
    }
    
    
    /**
    * Get the JdbcConnectionPool name for HADB from domain.xml.
    */ 
    public String getHadbJdbcConnectionPoolNameFromConfig() {

        String storePoolJndiName = this.getHaStorePoolJndiNameFromConfig();
        if(storePoolJndiName == null)
            return null;

        Resources resources = this.getResourcesBean();
        JdbcResource jdbcResource = 
            resources.getJdbcResourceByJndiName(storePoolJndiName);
        if(jdbcResource == null)
            return null;
        String poolName = jdbcResource.getPoolName();
        return poolName;
    }    
    
    /**
    * determine if HADB is installed
    * check if the value of the system property HADB_ROOT_PROPERTY
    * is a directory 
    */     
    public static boolean isHADBInstalled() {
        String hadbRootDirString = System.getProperty(SystemPropertyConstants.HADB_ROOT_PROPERTY);
        if(hadbRootDirString == null) {
            return false;
        }

        File hadbRootDir = new File(hadbRootDirString);
        return (hadbRootDir.exists() 
            && (hadbRootDir.isDirectory()) );
    }
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static final Logger _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);    
    
}
