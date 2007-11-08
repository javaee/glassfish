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

package com.sun.enterprise.ee.admin.configbeans;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.admin.common.Status;

import com.sun.enterprise.admin.WarningException;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetBuilder;

import com.sun.logging.ee.EELogDomains;

import com.sun.enterprise.ee.admin.servermgmt.EEInstancesManager;
import java.util.*;

import java.util.logging.Logger;
import java.util.logging.Level;        
import java.util.Vector;
import java.util.Properties;
import java.util.Enumeration;
import java.util.ArrayList;

import com.sun.enterprise.ee.admin.concurrent.Task;
import com.sun.enterprise.ee.admin.concurrent.Executor;
import com.sun.enterprise.ee.cms.core.GMSFactory;
import com.sun.enterprise.ee.cms.core.GMSConstants;
import com.sun.enterprise.ee.cms.core.GroupManagementService;


/*
    ISSUE: Do we really want to throws an AgentException here as this will 
    clients using this mbean to have our runtime; however we seem to be 
    throwing our own exceptions everywhere else in the mbeans. The problem with 
    MBeanException currently is that it masks the real exception (due to the 
    fact that MBeanHelper does some bogus formatting on the exception).
 */

public class ClustersConfigBean extends ServersAndClustersBaseBean implements IAdminConstants
{
    private static final int MAX_HB_ADDR_RANGE = 100;
    private static final long MAX_HB_PORT_RANGE = 45556;
    private static final String DEFAULT_HB_ADDRESS = "228.8.7.9";
    private static final int DEFAULT_HB_PORT= 1025;

    class StartServerTask extends Task {
        
        private static final long TIMEOUT_IN_MILLIS = 600000; //5 minutes
        String _serverName;
        ArrayList _msgsList;
        
        public StartServerTask(String serverName, ArrayList msgsList) {
            super(TIMEOUT_IN_MILLIS);
            _serverName = serverName;
            _msgsList   = msgsList;
        }
        
        public void run() {            
            try {
                getServersConfigBean().startServerInstance(_serverName);
            } catch (InstanceException ex) {
                /*
                    Ignore any exceptions, but do not propagate them up. 
                    There are a number. For example the node agent may not 
                    be running, the server may already be started. We do 
                    not log here because logging is done by 
                    startServerInstance itself.
                */
                /* 
                   the best for usability purposes - is to show problem in final Exception 
                 */
                addMsgToList(ex.getMessage());
            }            
        }
        synchronized void addMsgToList(String msg)
        {
            _msgsList.add(msg);
        }
    }
    
    class StopServerTask extends Task {
        private static final long TIMEOUT_IN_MILLIS = 120000; //2 minutes
        
        String _serverName;
        
        public StopServerTask(String serverName) {
            super(TIMEOUT_IN_MILLIS);
            _serverName = serverName;
        }
        
        public void run() {            
            try {
                getServersConfigBean().stopServerInstance(_serverName);
            } catch (InstanceException ex) {
                /*
                    Ignore any exceptions, but do not propagate them up. 
                    There are a number. For example the node agent may not 
                    be running, the server may already be started. We do 
                    not log here because logging is done by 
                    stopServerInstance itself.
                */
            }            
        }
    }        
    
    private static final TargetType[] VALID_TYPES = new TargetType[] {
        TargetType.DOMAIN, TargetType.CLUSTER, TargetType.SERVER, TargetType.NODE_AGENT};
        
    private static final StringManager _strMgr = 
        StringManager.getManager(ClustersConfigBean.class);              
     
    public ClustersConfigBean(ConfigContext configContext) {
        super(configContext);
    }                

    public String[] listClustersAsString(
        String targetName, boolean withStatus) throws InstanceException
    {
        try {            
            final StringManager stringMgr = StringManager.getManager(EEInstancesManager.class);
            final String[] result = toStringArray(getClusters(targetName));            
            int numClusters = result.length;
            for (int i = 0; i < numClusters; i++)
            {
                final String name = result[i];
                if (withStatus) {
                    final RuntimeStatusList statusList = getRuntimeStatus(name);
                    result[i] = stringMgr.getString("listInstanceElement", name,
                        statusList.toString());                                
                } else {
                    result[i] = name;
                }
            }
            return result;
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.listClusters.Exception", targetName);
        }
    }        
    
    public RuntimeStatusList getRuntimeStatus(String clusterName) 
        throws InstanceException
    {
        try {
            final String[] servers = getServersInCluster(clusterName);
            ServersConfigBean cmb = getServersConfigBean(); 
            //Get the status of all the servers in the cluster in parallel.
            return cmb.getRuntimeStatus(servers);
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.getClusterStatus.Exception", clusterName);            
        }
    }       
       
    public void clearRuntimeStatus(String clusterName) 
        throws InstanceException
    {
        try
        {           
            final String[] servers = getServersInCluster(clusterName);
            ServersConfigBean cmb = getServersConfigBean();            
            for (int i = 0; i < servers.length; i++)
            {
                cmb.clearRuntimeStatus(servers[i]);
            }            
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.getClusterStatus.Exception", clusterName);            
        }          
    }
       public RuntimeStatusList startCluster(String clusterName) 
        throws InstanceException
    {
        RuntimeStatusList beforeStatus = getRuntimeStatus(clusterName);
        if (beforeStatus.isEmpty()) {
            //no instances
            throw new InstanceException(_strMgr.getString("clusterIsEmpty", 
                clusterName));
        } else if (beforeStatus.allRunning()) {
            //already started
            for (Object rso : beforeStatus)
            {
                RuntimeStatus rs = (RuntimeStatus)rso;
                rs.resetRecentErrorMessages();
            }
            return  beforeStatus;
        } else {
            ArrayList msgsList = new ArrayList();
            doStartCluster(clusterName, msgsList);
            RuntimeStatusList afterStatus = getRuntimeStatus(clusterName);
            if (!afterStatus.allRunning()) {
                boolean bAny = afterStatus.anyRunning();
                String msg = _strMgr.getString(bAny?"clusterNotFullyStarted":"clusterNotStarted", clusterName);
                for(int i=0; i<msgsList.size(); i++ )
                {
                    msg = msg + "\n" + msgsList.get(i);
                }
                if(bAny)
                    throw new WarningException(msg);
                else
                    throw new InstanceException(msg);
            }

            for(int i = 0; i < afterStatus.size(); i++) {
                // this will get us a special error message from RuntimeStatus.toString()
                afterStatus.getStatus(i).setStartClusterFlag(beforeStatus.getStatus(i));
            }
            
            return afterStatus;
        }
    }
    
    /**
     * Starts the specified cluster. This operation is invoked by the 
     * asadmin start-cluster command.
     */
    private void doStartCluster(String clusterName, ArrayList msgsList) 
        throws InstanceException
    {
        try
        {
            String[] servers = getServersInCluster(clusterName);
            StartServerTask[] tasks = new StartServerTask[servers.length];
            for (int i = 0; i < servers.length; i++) {
                tasks[i] = new StartServerTask(servers[i], msgsList);
            }
            Executor exec = new Executor(tasks);
            exec.run();            

        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.startCluster.Exception", clusterName);
       }
    }

    public RuntimeStatusList stopCluster(String clusterName) 
        throws InstanceException
    {
        RuntimeStatusList beforeStatus = getRuntimeStatus(clusterName);
        if (beforeStatus.isEmpty()) {
            //no instances
            throw new InstanceException(_strMgr.getString("clusterIsEmpty", 
                clusterName));
        } 

        doStopCluster(clusterName);
        
        RuntimeStatusList afterStatus = getRuntimeStatus(clusterName);
        if (afterStatus.anyRunning()) {
            throw new InstanceException(_strMgr.getString("clusterNotFullyStopped", 
                clusterName));
        }

        for(int i = 0; i < afterStatus.size(); i++) {
            // this will get us a special error message from RuntimeStatus.toString()
            afterStatus.getStatus(i).setStopClusterFlag(beforeStatus.getStatus(i));
        }

        return afterStatus;
    }
    
    /**
     * Stops the specified server instance. This operation is invoked by the 
     * asadmin stop-instance command.
     */
    private void doStopCluster(String clusterName) 
        throws InstanceException
    {
        try
        {
            announceShutdownState( clusterName,
                                   GMSConstants.shutdownState.INITIATED );
            String[] servers = getServersInCluster(clusterName);
            StopServerTask[] tasks = new StopServerTask[servers.length];
            for (int i = 0; i < servers.length; i++) {
                tasks[i] = new StopServerTask(servers[i]);
            }
            Executor exec = new Executor(tasks);
            exec.run();            
            announceShutdownState( clusterName,
                                   GMSConstants.shutdownState.COMPLETED);
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.stopCluster.Exception", clusterName);            
        }
    }

    private void announceShutdownState (final String clusterName, 
                            final GMSConstants.shutdownState shutdownState) {

        if(GMSFactory.isGMSEnabled( clusterName )) {
            try {
                final GroupManagementService gms =
                                GMSFactory.getGMSModule( clusterName );
                gms.announceGroupShutdown( clusterName, shutdownState);
            }
            catch ( Exception e ) {
                //harmless at this point, hence ignored.
            }
        }
    }

    /**
     * Deletes the specified server instance. This operation is invoked by the asadmin delete-instance
     * command.
     */   
    public void deleteCluster(String clusterName) 
        throws InstanceException
    {               
        try{                      
            final ConfigContext configContext = getConfigContext();
            //validate that the cluster exists 
            Cluster cluster = ClusterHelper.getClusterByName(configContext, 
                clusterName);
            
            //Ensure that the cluster contains no server instances
            Server[] servers = ServerHelper.getServersInCluster(configContext, 
                clusterName);
            if (servers.length > 0) {
                throw new InstanceException(_strMgr.getString("clusterNotEmpty", 
                    clusterName, ServerHelper.getServersAsString(servers)));
            }                                               
            
            //Remove the the cluster. This is the last thing we want to do.
            Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);
            Clusters clusters = domain.getClusters();                       
                          
            //If the cluster is stand-alone, we must remove its stand alone 
            //configuration. Only non-referenced configurations can be deleted,
            //this is why the configuration is first deleted. Unfortunately, if 
            //this fails, we leave an unreferenced standalone configuration.
            if (ClusterHelper.isClusterStandAlone(configContext, clusterName)) {
                //Remove the cluster first
                String configName = cluster.getConfigRef();
                clusters.removeCluster(cluster, OVERWRITE);
                //Remove the standalone configuration                                             
                getConfigsConfigBean().deleteConfiguration(configName);
            } else {
                clusters.removeCluster(cluster, OVERWRITE);
            }        
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.deleteCluster.Exception", clusterName); 
        }
    }
        
    protected void addApplicationReference(Object cluster, boolean enabled, String name, 
        String virtualServers) throws ConfigException
    {        
        ApplicationRef ref = new ApplicationRef();
        ref.setEnabled(enabled);
        ref.setRef(name);
        if (virtualServers != null) {
            ref.setVirtualServers(virtualServers);
        }
        ((Cluster)cluster).addApplicationRef(ref, OVERWRITE);        
    }
        
    protected void addResourceReference(Object cluster, boolean enabled, String name) 
        throws ConfigException
    {
        ResourceRef ref = new ResourceRef();
        ref.setEnabled(enabled);
        ref.setRef(name);
        ((Cluster)cluster).addResourceRef(ref, OVERWRITE);        
    }

    /**
     * Creates a new server instance. This operation is invoked by the asadmin create-instance
     * command.
     */        
    public void createCluster(String clusterName, 
        String configName, Properties props) throws InstanceException
    {
        try {                       
            final ConfigContext configContext = getConfigContext();
            //validate name uniqueness
            if (!ConfigAPIHelper.isNameUnique(configContext, clusterName)) {
                 throw new InstanceException(_strMgr.getString("clusterNameNotUnique", 
                    clusterName));
            }
                           
            //Ensure that cluster specified by clusterName does not already exist.
            //Given that we've already checked for uniqueness earlier, this should never
            //be the case, but we'll be extra safe here.
            Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);            
            Clusters clusters = domain.getClusters();
            Cluster cluster = clusters.getClusterByName(clusterName);
            if (cluster != null) {
                throw new InstanceException(_strMgr.getString("clusterAlreadyExists", 
                    clusterName));
            }
    
            //Create the new cluster instance
            cluster = new Cluster();            
            cluster.setName(clusterName);
    
            
            if (configName != null) {                     
                //A shared cluster has its configuration pre-created.
                //Get the configuration specified by configName and ensure that it exists
                //is valid.
                Config config = validateSharedConfiguration(configContext, configName);
                cluster.setConfigRef(configName);
                //add system properties into cluster element itself
                if (props != null) {            
                    for (Enumeration e = props.propertyNames(); e.hasMoreElements() ;) {
                        String propName = (String)e.nextElement();
                        String propValue = (String)props.getProperty(propName);
                        if (propValue != null) {
                            SystemProperty ep = new SystemProperty();
                            ep.setName(propName);
                            ep.setValue(propValue);                    
                            cluster.addSystemProperty(ep, OVERWRITE);
                        }
                    }
                }
            } else {
                //For a standalone cluster, we need to create a standalone configuration.
                //Create the standalone configuration to be referenced by the server
                //FIXTHIS: Currently properties are passed when creating a standalone configuration
                //to override defaults in the default configuration. We need to be consistent
                //with properties passed during server instance creation and do one of the following:
                //Currently Aspproach #2 is implemented.
                //1) add property support at the cluster level (domain, configuration, cluster, instance)
                //2) use the properties passed at standalone cluster creation in a manner 
                //similar to standalone cluster creation.                
                //FIXTHIS: One issue is that the call below will result in a call to flushAll
                //which is also called below. This must be taken into account when we 
                //figure out the notification story.
                String stanaloneConfigName = getConfigsConfigBean().createStandAloneConfiguration(
                    clusterName, props);
                cluster.setConfigRef(stanaloneConfigName);
            }
            setHeartbeatAddressAndPort(cluster);
            //Add system applications and resources
            addSystemApplications(cluster);
            addSystemResources(cluster);
        
            //Add the new cluster
            clusters.addCluster(cluster, OVERWRITE);            
            
            //FIXTHIS: We force persistence, clear any notifications, and update the 
            //Application server's config context explicitely. Until this is modelled 
            //as an event notification (TBD) we need this to happen before notifying or
            //the Node Agent will not synchronize the correct data.
            //QUESTION: What happens if an exception is thrown above (e.g. in addNodeAgent). How do
            //we restore the admin config context to its previous (and unpersisted value)???
            //flushAll();            
        } catch (Exception ex) {
           throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.createCluster.Exception", clusterName); 
        }
    }

    private void setHeartbeatAddressAndPort ( final Cluster cluster )
            throws ConfigException {
        String heartbeatAddress = DEFAULT_HB_ADDRESS;
        while(addrAlreadyAssigned( heartbeatAddress )){
            heartbeatAddress = getHeartbeatAddress();
        }
        cluster.setHeartbeatAddress( heartbeatAddress, true);
        // for address we search thru other clusters to get a unique
        // address. Since that is being done we dont try to get a
        // unique port number given that each cluster will be in a unique
        // heartbeat address.
        int port = DEFAULT_HB_PORT;
        while(port <= DEFAULT_HB_PORT){
            port = getHeartbeatPort();
        }

        cluster.setHeartbeatPort(Integer.toString( port));
    }

    private int getHeartbeatPort () {
        return (new Long(
                    Math.round(
                        Math.random()*MAX_HB_PORT_RANGE)))
                .intValue();
    }

    private String getHeartbeatAddress () {
        final StringBuffer heartbeatAddressBfr = new StringBuffer( "228.8.");
        heartbeatAddressBfr.append(Math.round(Math.random()*MAX_HB_ADDR_RANGE))
                            .append('.')
                            .append(Math.round(Math.random()*MAX_HB_ADDR_RANGE));
        return heartbeatAddressBfr.toString();
    }

    private boolean addrAlreadyAssigned ( final String heartbeatAddress ) throws ConfigException {
        boolean exists = false;
        final Domain domain = ConfigAPIHelper.getDomainConfigBean(getConfigContext());
        final Clusters clusters = domain.getClusters();
        String cHbAddress;
        for(Cluster c : clusters.getCluster()){
            cHbAddress = c.getHeartbeatAddress();
            if(cHbAddress != null && heartbeatAddress.equals( cHbAddress)){
                exists = true;
                break;
            }
        }
        return exists;
    }

//Helper methods
    private Cluster[] getClusters(
        String targetName) throws ConfigException
    {
        
        final Target target = TargetBuilder.INSTANCE.createTarget(
            DOMAIN_TARGET, VALID_TYPES, targetName, getConfigContext());
        final Cluster[] clusters = target.getClusters();
        assert clusters != null;
        return clusters;
    }  

    private String[] getServersInCluster(
        String clusterName) throws ConfigException
    {
        String[] sa = new String[0];        
        Server[] servers = ServerHelper.getServersInCluster(getConfigContext(), 
            clusterName);
        if (servers != null)
        {
            sa = new String[servers.length];
            for (int i = 0; i < sa.length; i++)
            {
                sa[i] = servers[i].getName();
            }
        }
        return sa;
    }

    private ConfigsConfigBean getConfigsConfigBean() 
    {
        return new ConfigsConfigBean(getConfigContext());        
    }
    
    private ServersConfigBean getServersConfigBean()
    {      
        return new ServersConfigBean(getConfigContext());        
    }
   

    private String[] toStringArray(Cluster[] ca)
    {
        int numClusters = ca.length;
        final String[] result = new String[numClusters];
        for (int i = 0; i < numClusters; i++)
        {
            result[i] = ca[i].getName();
        }
        return result;
    }      
}
