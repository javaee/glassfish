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

import com.sun.enterprise.ee.admin.PortInUseException;
import com.sun.enterprise.ee.admin.PortInUse;
import com.sun.enterprise.ee.admin.PortReplacedException;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.NodeAgents;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.ConnectorModule;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.ServerRef;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.serverbeans.NodeAgentHelper;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;

import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.util.net.NetUtils;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.server.Constants;
import com.sun.enterprise.ee.admin.clientreg.InstanceRegistry;
import com.sun.enterprise.ee.admin.clientreg.NodeAgentRegistry;
import com.sun.enterprise.ee.admin.clientreg.MBeanServerConnectionInfo;

import com.sun.logging.ee.EELogDomains;

import com.sun.enterprise.ee.admin.servermgmt.EEInstancesManager;

import com.sun.enterprise.ee.admin.proxy.NodeAgentProxy;
import com.sun.enterprise.ee.admin.mbeanapi.NodeAgentMBean;

import com.sun.enterprise.ee.admin.proxy.InstanceProxy;
import com.sun.enterprise.ee.admin.mbeanapi.ServerRuntimeMBean;  

import com.sun.enterprise.ee.admin.concurrent.Task;
import com.sun.enterprise.ee.admin.concurrent.Executor;    

import com.sun.enterprise.addons.AddonFacade;
import java.rmi.RemoteException;

import java.util.logging.Logger;
import java.util.logging.Level;        
import java.util.Properties;
import java.util.Enumeration;
import java.util.ArrayList;
import java.io.File;
import javax.management.MBeanException;

//ISSUE: Do we really want to throws an InstanceException here as this will clients 
//using this mbean to have our runtime; however we seem to be throwing our own
//exceptions everywhere else in the mbeans. The problem with MBeanException 
//currently is that it masks the real exception (due to the fact that MBeanHelper
//does some bogus formatting on the exception.

public class ServersConfigBean extends ServersAndClustersBaseBean implements IAdminConstants
{    
    
    class GetRuntimeStatusTask extends Task {
        private static final long TIMEOUT_IN_MILLIS = 30000;
        
        private String _serverName;
        private RuntimeStatus _status;
        
        public GetRuntimeStatusTask(String serverName) {
            super(TIMEOUT_IN_MILLIS);
            _serverName = serverName;
            _status = new RuntimeStatus(_serverName);
        }
        
        public RuntimeStatus getStatus() {
            return _status;
        }
        
        public void run() {            
            try {
                _status = getRuntimeStatus(_serverName);
            } catch (InstanceException ex) {
                StringManagerBase sm = StringManagerBase.getStringManager(
                    getLogger().getResourceBundleName());         
                getLogger().log(Level.WARNING, 
                    sm.getString("eeadmin.listInstances.Exception", _serverName), ex);
                _status = new RuntimeStatus(_serverName);
            }            
        }
    }         
    
    private static final StringManager _strMgr = 
        StringManager.getManager(ServersConfigBean.class);
    
    private static final TargetType[] VALID_LIST_TYPES = new TargetType[] {
        TargetType.DOMAIN, TargetType.CLUSTER, TargetType.SERVER, TargetType.NODE_AGENT};
        
    private static final TargetType[] VALID_TYPES = new TargetType[] {
        TargetType.SERVER};        
        
    public ServersConfigBean(ConfigContext configContext) {
        super(configContext);        
    }
    
    public void clearRuntimeStatus(String serverName) throws InstanceException
    {        
        try {                         
            ServerRuntimeMBean serverMBean = InstanceProxy.getInstanceProxy(serverName);
            serverMBean.clearRuntimeStatus();
        } catch (Exception ex) {
            //Ignore any error indicating that the server is unreachable.
            //FIXTHIS: We could expect the proxy to do this for us as it 
            //seems to be a common case.
            if (InstanceProxy.isUnreachable(ex)) {
                // do nothing
            } else {
                throw new InstanceException(ex);                        
            }
        }        
    }
      
    public RuntimeStatus getRuntimeStatus(String serverName) throws InstanceException
    {        
        try {                         
            ServerRuntimeMBean serverMBean = InstanceProxy.getInstanceProxy(serverName);
            return serverMBean.getRuntimeStatus();
        } catch (Exception ex) {
            //Ignore any error indicating that the server is unreachable.
            //FIXTHIS: We could expect the proxy to do this for us as it 
            //seems to be a common case.
            if (InstanceProxy.isUnreachable(ex)) {
                return new RuntimeStatus(serverName);
            } else {
                throw new InstanceException(ex);                        
            }
        }
    }       
    
    public boolean isRunning(String serverName) throws InstanceException
    {
        return getRuntimeStatus(serverName).isRunning();
    }
    
    /**
     * Fetches the runtime status of the given server instances in parallel.
     */
    public RuntimeStatusList getRuntimeStatus(Server[] servers)
    {     
        return getRuntimeStatus(toStringArray(servers));
    }           
        
    public RuntimeStatusList getRuntimeStatus(String[] servers)
    {
        GetRuntimeStatusTask[] tasks = new GetRuntimeStatusTask[servers.length];
        for (int i = 0; i < servers.length; i++) {
            tasks[i] = new GetRuntimeStatusTask(servers[i]);
        }
        Executor exec = new Executor(tasks);
        exec.run();
        RuntimeStatusList result = new RuntimeStatusList(servers.length);
        for (int i = 0; i < servers.length; i++) {
            result.add(tasks[i].getStatus());
        }        
        return result;
    }    
    
    public RuntimeStatusList getServerInstanceRuntimeStatus(String targetName) 
        throws InstanceException
    {
        final ConfigContext configContext = getConfigContext();
        final StringManager stringMgr = StringManager.getManager(EEInstancesManager.class);        
        try {                               
            final Target target = TargetBuilder.INSTANCE.createTarget(
                DOMAIN_TARGET, VALID_LIST_TYPES, targetName, configContext);                    
            final Server[] servers = target.getServers();                          
            return getRuntimeStatus(servers);                            
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.listInstances.Exception", targetName);
        }         
    }
    
    /**
     * Lists server instances.
     */
    public String[] listServerInstancesAsString(
        String targetName, boolean andStatus) throws InstanceException
   {
        final ConfigContext configContext = getConfigContext();
        final StringManager stringMgr = StringManager.getManager(EEInstancesManager.class);        
        try {                               
            final Target target = TargetBuilder.INSTANCE.createTarget(
                DOMAIN_TARGET, VALID_LIST_TYPES, targetName, configContext);                    
            final Server[] servers = target.getServers();          
            final int numServers = servers.length;            
            ArrayList result = new ArrayList();
            String serverName = null;
            int status;            
            RuntimeStatusList statusList = null;
            if (andStatus) {                
                //Fetch the status of all the server instances in parallel
                statusList = getRuntimeStatus(servers);                
            }
            for (int i = 0; i < numServers; i++) {                
                serverName = servers[i].getName();
                if (andStatus) {
                    //Do not display the DAS as a server instance
                    if (!ServerHelper.isDAS(configContext, serverName)) {                                               
                        result.add(stringMgr.getString("listInstanceElement", serverName,
                            statusList.getStatus(i).toShortString()));                        
                    }
                } else {
                    result.add(serverName);
                }
            }            
            return (String[])result.toArray(new String[result.size()]);
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.listInstances.Exception", targetName);
        }         
    }
    
    public String listDASServerInstanceAsString(boolean andStatus) throws InstanceException
    {
        final ConfigContext configContext = getConfigContext();
        String dasName = null;
        try {
            final Server das = ServerHelper.getDAS(configContext);
            dasName = das.getName();
            return listServerInstancesAsString(dasName, andStatus)[0];
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.listInstances.Exception", dasName);
        }    
    }
   
        
    public String[] listUnclusteredServerInstancesAsString(
        boolean andStatus, boolean excludeDASInstance)
        throws InstanceException
    {
        final ConfigContext configContext = getConfigContext();
        final StringManager stringMgr = StringManager.getManager(EEInstancesManager.class);
        try {            
            final Server[] servers = ServerHelper.getUnclusteredServers(
                configContext, excludeDASInstance);            
            ArrayList result = new ArrayList();
            String serverName;
            RuntimeStatusList statusList = null;
            if (andStatus) {                
                //Fetch the status of all the server instances in parallel
                statusList = getRuntimeStatus(servers);                
            }
            for (int i = 0; i < servers.length; i++) {                
                serverName = servers[i].getName();
                if (andStatus) {                     
                    result.add(stringMgr.getString("listInstanceElement", serverName,
                        statusList.getStatus(i).toShortString()));                    
                } else {
                    result.add(serverName);
                }                
            }
            return (String[])result.toArray(new String[result.size()]);
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.listInstances.Exception", "unclustered instances");
        }        
    }
        
   
    /**
     * Starts the specified server instance. This operation is invoked by the asadmin start-instance
     * command.
     */
    public RuntimeStatus startServerInstance(
        String serverName) throws InstanceException
    {
        try {
            final ConfigContext configContext = getConfigContext();
            //validate that the server exists and return its node agent
            Target target = null;
            try {
                target = TargetBuilder.INSTANCE.createTarget(
                    VALID_TYPES, serverName, configContext);   
            } catch (Exception e) {
                throw new InstanceException(
                    _strMgr.getString("noSuchInstance", serverName));
            }
            NodeAgent controller = target.getNodeAgents()[0];
            String agentName = controller.getName();
                                    
            // Call the Node Agent to start the server instance remotely
            NodeAgentsConfigBean ncb = getNodeAgentsConfigBean();
            if (ncb.isRunning(agentName)) {
                if (isRunning(serverName)) {
                    // WBN January 2007 Issue 726
                    // This is now OFFICIALLY not an error.  Log it and return
                    StringManagerBase sm = StringManagerBase.getStringManager(
                        getLogger().getResourceBundleName());         
                    getLogger().log(Level.WARNING, 
                        sm.getString("eeadmin.already.running", serverName));
                    return null;
                } else {
                    String installRoot = System.getProperty(
                    SystemPropertyConstants.INSTALL_ROOT_PROPERTY);
                    String instanceRoot = System.getProperty(
                    SystemPropertyConstants.INSTANCE_ROOT_PROPERTY);
                    NodeAgentMBean agentMBean = NodeAgentProxy.getNodeAgentProxy(agentName);                        
                    agentMBean.startInstance(serverName);        
                    return getRuntimeStatus(serverName);
                }
            } else {
                throw new InstanceException(_strMgr.getString("agentNotRunning", 
                    agentName, serverName));
            }
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.startServerInstance.Exception", serverName);
        }
    }
            
    
    /**
     * Stops the specified server instance. This operation is invoked by the asadmin stop-instance
     * command.
     */
    public void stopServerInstance(
        String serverName, int timeout) throws InstanceException
    {
        stopServerInstance(serverName, true, timeout);
    }
    
    /**
     * Stops the specified server instance. This operation is invoked by the asadmin stop-instance
     * command.
     */
    public void stopServerInstance(
        String serverName) throws InstanceException
    {
        stopServerInstance(serverName, false, -1);
    }
    
    /**
     * Stops the specified server instance. This operation is invoked by the asadmin stop-instance
     * command.
     */
    private void stopServerInstance(
        String serverName, boolean forcekill, int timeout) throws InstanceException
    {
        try {
            final ConfigContext configContext = getConfigContext();
            //validate that the server exists and return its node agent
            final Target target = TargetBuilder.INSTANCE.createTarget(
                VALID_TYPES, serverName, configContext);   
            NodeAgent controller = target.getNodeAgents()[0];
            String agentName = controller.getName();
                       
            // Call the Node Agent to stop the server instance remotely
            NodeAgentsConfigBean ncb = getNodeAgentsConfigBean();
            if (ncb.isRunning(agentName)) {
                    NodeAgentMBean agentMBean = 
                        NodeAgentProxy.getNodeAgentProxy(agentName);
                    if (forcekill) {
                        agentMBean.stopInstance(serverName, timeout);
                    } else {
                        agentMBean.stopInstance(serverName);
                    }
            } else throw new InstanceException(
                   _strMgr.getString("agentNotRunning", agentName, serverName));            
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.stopServerInstance.Exception", serverName);            
        }
    }
    
    /**
     * Restart server instance
     */
    public void restartServerInstance(
        String serverName) throws InstanceException
    {
	boolean restarted = false;

	try {

	    // stop the instance only if it is running
	    if (isRunning(serverName)) {
		stopServerInstance(serverName);
	    }

	    // start the instance only if it is stopped
	    if (! isRunning(serverName)) {
		startServerInstance(serverName);
		restarted = true;
	    }

	    // if none of the above are true then the restart is a failure
	    // throw appropriate exception
	    if (! restarted) {
                throw new InstanceException(_strMgr.getString("serverCannotRestart", 
			serverName));
	    }

        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.restartServerInstance.Exception", serverName);
        }
    }

    
    private void deleteClusterReferences(String serverName) 
        throws ConfigException, InstanceException
    {
        //If the instance is clustered, the we must remove the server refs from 
        //the cluster.
        final ConfigContext configContext = getConfigContext();
        final Cluster cluster = ClusterHelper.getClusterForInstance(configContext, serverName);
        final ServerRef ref = cluster.getServerRefByRef(serverName);
        if (ref == null) {
            throw new InstanceException(_strMgr.getString("clusterMissingServerRef", 
                cluster.getName(), serverName));
        } else {
            cluster.removeServerRef(ref, OVERWRITE);
        }
    }
    
    /**
     * Deletes the specified server instance. This operation is invoked by the asadmin delete-instance
     * command.
     */    
    public void deleteServerInstance(String serverName) throws InstanceException
    {
        try {
            final ConfigContext configContext = getConfigContext();
            //validate that the server exists and return its node agent
            final Target target = TargetBuilder.INSTANCE.createTarget(
                VALID_TYPES, serverName, configContext);   
            final NodeAgent controller = target.getNodeAgents()[0];
            
            //Remove the server instance
            final Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);            
            final Servers servers = domain.getServers();
            final Server server = target.getServers()[0];
            boolean isReachable = true;

            try {                
                ServerRuntimeMBean serverMBean = InstanceProxy.getInstanceProxy(serverName);
                Status status = serverMBean.getRuntimeStatus().getStatus(); 
            } catch (Exception ex) {
                if (InstanceProxy.isUnreachable(ex)) {
                    isReachable = false;
                }
            }

            if(isReachable)
                throw new InstanceException(_strMgr.getString("serverIsNotStopped", 
                    serverName));
            
            if (ServerHelper.isServerStandAlone(configContext, serverName)) {
                //If the server instance is stand-alone, we must remove its stand alone 
                //configuration after removing the server instance (since a configuration
                //can only be deleted if it is unreferenced). Unfortunately, if 
                //this fails, we leave an unreferenced standalone configurtion.                
                String configName = server.getConfigRef();
                // remove the server
                servers.removeServer(server, OVERWRITE);            
                //remove the standalone configuration                
                //FIXTHIS: One issue is that the call below will result in a call to flushAll
                //which is also called below. This must be taken into account when we 
                //figure out the notification story.
                getConfigsConfigBean().deleteConfiguration(configName);
            } else {
                if (ServerHelper.isServerClustered(configContext, server)) {
                    //If the instance is clustered, the we must remove the server refs from 
                    //the cluster.
                    deleteClusterReferences(serverName);
                }                
                //Remove the server
                servers.removeServer(server, OVERWRITE);     
            }
            
            //Remove our connction to the server instance
            InstanceRegistry.removeInstanceConnection(serverName);
            
            //Notify the node agent that a new server has been deleted to it so it can 
            //resynchronize. 
            //FIXTHIS: We force persistence, clear any notifications, and update the 
            //Application server's config context explicitely. Until this is modelled 
            //as an event notification (TBD) we need this to happen before notifying or
            //the Node Agent will not synchronize the correct data.
            //QUESTION: What happens if an exception is thrown above (e.g. in addNodeAgent). How do
            //we restore the admin config context to its previous (and unpersisted value)???
            flushAll();
            NodeAgentMBean agentMBean = NodeAgentProxy.getNodeAgentProxy(controller.getName());                        
            agentMBean.synchronizeWithDAS();
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.deleteServerInstance.Exception", serverName);
        }        
    }
           
    private void addServerProperties(Server server, Properties props) throws ConfigException
    {
        //Set the server instances properties.
        //FIXTHIS: Should we be setting properties of standalone instances, since
        //the properties are already set in the stand alone configuration?
        if (props != null) {
            for (Enumeration e = props.propertyNames() ; e.hasMoreElements() ;) {
                String name = (String)e.nextElement();
                String value = (String)props.getProperty(name);                
                //Add the new property
                if (value != null) {
                    //Remove the property if it already exists
                    SystemProperty sysProp = server.getSystemPropertyByName(name);
                    if (sysProp != null) {                        
                        server.removeSystemProperty(sysProp, OVERWRITE);
                    }
                    SystemProperty ep = new SystemProperty();
                    ep.setName(name);
                    ep.setValue(value);
                    server.addSystemProperty(ep, OVERWRITE);                    
                }
            }
        }
    }           
            
    protected void addApplicationReference(Object server, boolean enabled, String name, 
        String virtualServers) throws ConfigException
    {        
        ApplicationRef ref = new ApplicationRef();
        ref.setEnabled(enabled);
        ref.setRef(name);
        if (virtualServers != null) {
            ref.setVirtualServers(virtualServers);
        }
        ((Server)server).addApplicationRef(ref, OVERWRITE);        
    }
        
    protected void addResourceReference(Object server, boolean enabled, String name) 
        throws ConfigException
    {
        ResourceRef ref = new ResourceRef();
        ref.setEnabled(enabled);
        ref.setRef(name);
        ((Server)server).addResourceRef(ref, OVERWRITE);        
    }
            
    private void createStandAloneServerInstance(
        Server server, Properties props) throws ConfigException, InstanceException
    {                
        //Create the standalone configuration to be referenced by the server.
        //FIXTHIS: One issue is that the call below will result in a call to flushAll
        //which is also called below. This must be taken into account when we 
        //figure out the notification story.
        String standaloneConfigName = getConfigsConfigBean().createStandAloneConfiguration(
            server.getName(), props);
        
        //Create the server instance referencing this new standalone configuration
        createSharedServerInstance(standaloneConfigName, server, null);
    }
    
    private void createClusteredServerInstance(String clusterName, 
        Server server, Properties props) throws ConfigException
    {
        final ConfigContext configContext = getConfigContext();
        //Add properties
        addServerProperties(server, props);
        
        //Get the configuration specified by configName and ensure that it exists
        Cluster cluster = ClusterHelper.getClusterByName(configContext, 
            clusterName);
        
        
        //The clustered server inherits its configuration from the cluster
        server.setConfigRef(cluster.getConfigRef());

        //Now add the new server instance
        // to avoid Validator's complains
        Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);
        domain.getServers().addServer(server, OVERWRITE);                        
        
        //Add the server to the cluster
        ServerRef serverRef = new ServerRef();
        serverRef.setRef(server.getName());
        cluster.addServerRef(serverRef, OVERWRITE);
        
        
        //The clustered server inherits its applications from the cluster
        ApplicationRef[] appRefs = cluster.getApplicationRef();        
        for (int i = 0; i < appRefs.length; i++) {
            addApplicationReference(server, appRefs[i].isEnabled(),  
                appRefs[i].getRef(), appRefs[i].getVirtualServers());
        }
        
        //The clustered server inherits its resources from the cluster
        ResourceRef[] resRefs = cluster.getResourceRef();
        ResourceRef resRef = null;
        for (int i = 0; i < resRefs.length; i++) {
            addResourceReference(server, resRefs[i].isEnabled(),
                resRefs[i].getRef());
        }
    }
    
    private void createSharedServerInstance(String configName, 
        Server server, Properties props) throws ConfigException, InstanceException
    {        
        final ConfigContext configContext = getConfigContext();
        
        //Get the configuration specified by configName and ensure that it exists
        //and is valid
        Config config = validateSharedConfiguration(configContext, configName);
        
        server.setConfigRef(configName);
        
        //Add properties
        addServerProperties(server, props);
        
        //Add system applications and resources
        addSystemApplications(server);
        addSystemResources(server);
        //Finally add the new server instance
        Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);
        domain.getServers().addServer(server, OVERWRITE);                        
    }
    
    /**
     * Choose non-conflicting port numbers. Warning the incoming portsInUse is side 
     * affected, so that the PortInUse elements are modified to contain the new port.
     */
    private ArrayList pickNonConflictingPorts(Server server, ArrayList portsInUse) 
        throws ConfigException
    {        
        Properties props = new Properties();
        PortInUse portInUse;
        int port;
        
        //We only want to check for port conflicts if the node agent specified has rendezvous'd;
        //otherwise, the host name on which it is residing is unknown (defaulting to "localhost").
        //Before we can indicate whether a port is in use, we must know the valid host name.
        final boolean isBoundToHost = getPortConflictCheckerConfigBean().isBoundToHost(server);
        
        //Iterate through all the conflicting ports and pick new values.
        for (int i = 0; i < portsInUse.size(); i++) {
            portInUse = (PortInUse)portsInUse.get(i);
            port = portInUse.getPort();
            int newPort = 0;
            if (isBoundToHost) {
                newPort = NetUtils.getNextFreePort(portInUse.getHostName(), port);            
            } else {
                newPort = port + 1;
            }
            //Keep track of the newly assigned and non-conflicting port. WARNING: we are 
            //side affecting the incoming ArrayList.
            portInUse.setNewPort(newPort);
            //Build the list of system properties to add to the server instance
            props.put(portInUse.getPropertyName(), new Integer(newPort).toString());
        }
        addServerProperties(server, props);
        return portsInUse;
    }
         
    /**
     * Check to see if any of the ports defined as system properties are in use or conflict with
     * other ports in domain.xml (i.e. with other server instances on the same machine -- with
     * the same node agent). If there are conflicts, then we pick new port numbers.
     */
    private ArrayList resolvePortConflicts(Server server, Properties newProps) throws Exception
    {
        PortConflictCheckerConfigBean portChecker = getPortConflictCheckerConfigBean();
        String serverName = server.getName();
        ArrayList portsInUse = null;        
        try {
            //First check to see whether any of the user specified ports are in use, if
            //so an exception must be thrown
            if (newProps != null) {
                portChecker.checkForPortConflicts(server, newProps, newProps, false);
            }
            
            int i = 0;            
            //We loop trying trying to incrementally assign new ports.
            while (true) {
                //Perform some sort of sanity check to ensure 
                //that the port numbers do not conflict for other servers on the same
                //node agent. This is accomplished by comparing properties whose
                //name contains "port" and whose value is numeric. NOTE: port validation
                //is done after adding the server. For this reason, we must rollback the
                //addition of the server if the port check fails.
                try {
                    portChecker.checkForPortConflicts(server, null, false);
                    //stop when there are no port conflicts
                    break;
                } catch (PortInUseException ex) {                           
                    if (i++ > 25) {
                        //We do not want to throw the PortInUseException since it is an internal 
                        //class and protected.
                        throw new InstanceException(ex.getMessage());
                    }
                    //If there were port conflicts, then we pick unused ports.
                    ArrayList newPorts = pickNonConflictingPorts(server, ex.getConflictingPorts());
                    
                    //Keep track of the initial list of conflicting ports. This is tricky
                    //since the list in the PortInUseException will shrink as conflicts 
                    //are resolved.
                    if (portsInUse == null) {
                        portsInUse = newPorts;
                    } else {
                        //Now take all the newly resolved ports and apply them to the original list, updating
                        //only the new port.
                        for (int j = 0; j < newPorts.size(); j++) {
                            PortInUse newPort = (PortInUse)newPorts.get(j);                        
                            for (int k = 0; k < portsInUse.size(); k++) {
                                PortInUse port = (PortInUse)portsInUse.get(k);
                                if (port.getPropertyName().equals(newPort.getPropertyName())) {
                                    port.setNewPort(newPort.getNewPort());
                                    break;
                                } 
                            }
                        }
                    }
                }                        
            }
        } catch (Exception ex) {                
            try {
                deleteServerInstance(serverName);
            } catch (Exception ex2) {
                //Log          
                StringManagerBase sm = StringManagerBase.getStringManager(getLogger().getResourceBundleName());            
                getLogger().log(Level.WARNING, 
                   sm.getString("eeadmin.createServerInstance.Exception", serverName), ex2);
            }
            throw (ex);
        }
        return portsInUse;
    }
    
    /**
     * Creates a new server instance. This operation is invoked by the asadmin create-instance
     * command.
     */
    public void createServerInstance(
        String nodeAgentName, String serverName, 
        String configName, String clusterName, Properties props) 
        throws InstanceException, PortReplacedException
    {
        ArrayList conflictingPorts = null;
        try {                        
            final ConfigContext configContext = getConfigContext();
            //validate name uniqueness
            if (!ConfigAPIHelper.isNameUnique(configContext, serverName)) {
                 throw new InstanceException(_strMgr.getString("serverNameNotUnique", 
                    serverName));
            }
            
			ConfigAPIHelper.checkLegalName(serverName);
			
            // see if nodeagent exists, if not create an empty reference
            Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);            
            NodeAgents agents=domain.getNodeAgents();
            NodeAgent agent=agents.getNodeAgentByName(nodeAgentName);
            if (agent == null) {
                // create an implnodeagent reference
                NodeAgentsConfigBean naMBean = getNodeAgentsConfigBean();
                naMBean.createNodeAgentConfig(nodeAgentName);
            }                    
            
            //a configuration and cluster cannot both be specified
            if (configName != null && clusterName != null) {
                throw new InstanceException(_strMgr.getString("configAndClusterMutuallyExclusive"));
            }            
            
            //Ensure that server specified by serverName does not already exist. 
            //Given that we've already checked for uniqueness earlier, this should never
            //be the case, but we'll be extra safe here.
            Servers servers = domain.getServers();
            Server server = servers.getServerByName(serverName);
            if (server != null) {
                throw new InstanceException(_strMgr.getString("serverAlreadyExists", 
                    serverName));
            }                            
            
            //Create the new server instance
            server = new Server();            
            server.setNodeAgentRef(nodeAgentName);
            server.setName(serverName);                                
            
            if (configName != null) {
                createSharedServerInstance(configName, server, props);
            } else if (clusterName != null) {
                createClusteredServerInstance(clusterName, server, props);
            } else {
                //FIXTHIS: One issue is that the call below will result in a call to flushAll
                //which is also called below. This must be taken into account when we 
                //figure out the notification story.
                createStandAloneServerInstance(server, props);
            }                                                   
            
            
            //Check for and resolve port conflicts. The list of port conflicts is maintained. 
            //This is called after creating the server. This is due to the fact that port
            //conflicts are resolved and a marker PortReplacedException is thrown; however
            //the server is still created.
            conflictingPorts = resolvePortConflicts(server, props);
            
            //Notify the node agent that a new server has been added to it so it can 
            //resynchronize. 
            //FIXTHIS: We force persistence, clear any notifications, and update the 
            //Application server's config context explicitely. Until this is modelled 
            //as an event notification (TBD) we need this to happen before notifying or
            //the Node Agent will not synchronize the correct data.
            //QUESTION: What happens if an exception is thrown above (e.g. in addNodeAgent). How do
            //we restore the admin config context to its previous (and unpersisted value)???
            flushAll();
            NodeAgentMBean agentMBean = NodeAgentProxy.getNodeAgentProxy(nodeAgentName);                        
            agentMBean.synchronizeWithDAS();                           
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.createServerInstance.Exception", serverName);
        }
        //Finally after all is said and done, we throw an exception if there were port 
        //conflicts.
        if (conflictingPorts != null) {
            throw new PortReplacedException(conflictingPorts);
        }
    }      
    
    private String[] toStringArray(Server[] sa)
    {
        int numServers = sa.length;
        final String[] result = new String[numServers];
        for (int i = 0; i < numServers; i++)
        {
            result[i] = sa[i].getName();
        }
        return result;
    }
    
    private PropertyConfigBean getPropertyConfigBean() {
        return new PropertyConfigBean(getConfigContext());
    }
    
    private NodeAgentsConfigBean getNodeAgentsConfigBean() 
    {
        return new NodeAgentsConfigBean(getConfigContext());
    }
    
    private ConfigsConfigBean getConfigsConfigBean() 
    {
        return new ConfigsConfigBean(getConfigContext());
    }    
    
    private PortConflictCheckerConfigBean getPortConflictCheckerConfigBean() 
    {
        return new PortConflictCheckerConfigBean(getConfigContext());
    }
}
