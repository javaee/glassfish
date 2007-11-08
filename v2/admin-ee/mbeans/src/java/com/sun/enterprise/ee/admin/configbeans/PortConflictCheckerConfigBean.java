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
import com.sun.enterprise.ee.admin.InvalidPortException;

import com.sun.enterprise.admin.configbeans.BaseConfigBean;

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

import java.util.logging.Logger;
import java.util.logging.Level;        
import java.util.Properties;
import java.util.Enumeration;
import java.util.ArrayList;


public class PortConflictCheckerConfigBean extends BaseConfigBean implements IAdminConstants
{          
    private static final String PORT_SUFFIX = "port";
       
    public PortConflictCheckerConfigBean(ConfigContext configContext) {
        super(configContext);        
    }
                   
    public static boolean isLocalHost(String host) 
    {
        if (host.equals("localhost") || host.equals("127.0.0.1") ||
            host.equals(System.getProperty(SystemPropertyConstants.HOST_NAME_PROPERTY))) {
                return true;
        } else {
            return false;
        }
    }

    private boolean isServerRunningOnDASHost(Server s) 
        throws ConfigException, InstanceException
    {
        if (!isBoundToHost(s)) {
            return false;
        } else {
            final ConfigContext configContext = getConfigContext();
            if (ServerHelper.isDAS(configContext, s)) { 
                return true;
            } else {
                final MBeanServerConnectionInfo dasInfo = InstanceRegistry.
                    getDASConnectionInfo(configContext);
                final MBeanServerConnectionInfo serverInfo = InstanceRegistry.
                    getInstanceConnectionInfo(configContext, s.getName());
                /*
                    We determine if the server is on the same host as the DAS by seeing 
                    if the server's mbean server connection info matches that of the 
                    DAS. Also, if the server's host is the localhost (i.e. the same 
                    host that the DAS is running on).
                 */
                return (dasInfo.getHost().equals(serverInfo.getHost()) ||
                        isLocalHost(serverInfo.getHost()));
            }
        }
    }
    
    private NodeAgent[] getOtherNodeAgentsOnSameHost(NodeAgent agent) throws ConfigException,
        AgentException
    {       
        final ConfigContext configContext = getConfigContext();
        ArrayList result = new ArrayList();
        if (NodeAgentHelper.hasNodeAgentRendezvousd(configContext, agent)) {            
            MBeanServerConnectionInfo agentInfo = NodeAgentRegistry.getNodeAgentConnectionInfo(
                configContext, agent.getName());
            String hostName = agentInfo.getHost();
            NodeAgent[] agents = NodeAgentHelper.getNodeAgentsInDomain(configContext);
            for (int i = 0; i < agents.length; i++) {
                if (NodeAgentHelper.hasNodeAgentRendezvousd(configContext, agents[i])) {
                    if (!agents[i].getName().equals(agent.getName())) {
                        agentInfo = NodeAgentRegistry.getNodeAgentConnectionInfo(
                            configContext, agents[i].getName());
                        if (hostName.equals(agentInfo.getHost())) {
                            result.add(agents[i]);
                        }
                    }
                }
            }
        }
        return (NodeAgent[])result.toArray(new NodeAgent[result.size()]);
    }
    
    private Server[] getServersRunningOnDASHost(boolean includeDAS)
        throws ConfigException, InstanceException 
    {
        final ConfigContext configCtx = getConfigContext();
        final ArrayList servers = new ArrayList();
        final Server[] allServers = ServerHelper.getServersInDomain(
            configCtx);
        for (int i = 0; i < allServers.length; i++) {
            final Server s = allServers[i];
            boolean isDAS = ServerHelper.isDAS(configCtx, s);
            if (isDAS && includeDAS) {
                servers.add(s);
            } else if (!isDAS && isServerRunningOnDASHost(s)) {
                servers.add(s);
            }
        }
        return (Server[])servers.toArray(new Server[0]);
    }
    
    /**
     * Return true if the node agent referenced by the server has rendezvous'd with the DAS.
     */
    boolean isBoundToHost(Server server) throws ConfigException
    {
        final ConfigContext configContext = getConfigContext();
        if (ServerHelper.isDAS(configContext, server)) {
            return true;
        } else {
            NodeAgent agent = NodeAgentHelper.getNodeAgentForServer(configContext, server.getName());            
            return NodeAgentHelper.hasNodeAgentRendezvousd(configContext, agent);
        }
    }

    /**
     */
    private void checkForPortPropertyConflicts(String propertyName, 
        int port, Properties serverProps, 
        String serverName, String hostName) throws ConfigException, PortInUseException, InstanceException
    {                                      
        //Iterate through all the properties of the newly created server, looking for 
        //conflicts in domain.xml
        for (Enumeration e = serverProps.propertyNames() ; e.hasMoreElements() ;) {            
            String propName = (String)e.nextElement();            
            if (propName.toLowerCase().indexOf(PORT_SUFFIX) >= 0) {
                String propValue = (String)serverProps.getProperty(propName);
                try {
                    //If the name of the property contains the string "port" and 
                    //the value of the property is numeric then check for a 
                    //port conflict.
                    if (port == Integer.parseInt(propValue)) {
                        throw new PortInUseException(serverName, hostName, port, 
                            propertyName, propName);                   
                    }
                } catch (NumberFormatException ex) {
                    continue;
                }                
            }
        }                
    }
    
    private void checkForInvalidPorts(Properties newServerProps) throws InvalidPortException
    {
        ArrayList invalidPorts = new ArrayList();
        for (Enumeration e = newServerProps.propertyNames() ; e.hasMoreElements() ;) {            
            String name = (String)e.nextElement(); 
            if (name.toLowerCase().indexOf(PORT_SUFFIX) >= 0) {
                String value = (String)newServerProps.getProperty(name);
                if (!NetUtils.isPortStringValid(value)) {
                    invalidPorts.add(new PortInUse(name));
                }
            }
        }
        if (invalidPorts.size() > 0) {
            throw new InvalidPortException(invalidPorts);
        }
    }
    
    private PortInUseException checkForDuplicatePorts(Properties newServerProps, 
        String hostName, String serverName, PortInUseException exception) 
        throws ConfigException, InstanceException
    {        
        final ConfigContext configContext = getConfigContext();           
        PortInUseException result = exception;
        final Properties serverProps = getPropertyConfigBean().getTargetedProperties(
            serverName, true);
                
        //Iterate through all the properties of the newly created server and check to 
        //see if any of the ports are in use.
        for (Enumeration e = newServerProps.propertyNames() ; e.hasMoreElements() ;) {            
            String name = (String)e.nextElement();            
            if (name.toLowerCase().indexOf(PORT_SUFFIX) >= 0) {
                String value = (String)newServerProps.getProperty(name);
                try {
                    int port = Integer.parseInt(value);
                    //If we have already detected a conflict (while checking for DAS port conflicts, 
                    //then there is no need to check the port for conflicts again.
                    if (result == null || !result.portAlreadyConflicts(port)) {    
                        //If the name of the property contains the string "port" and 
                        //the value of the property is numeric then check for a duplicate port 
                        //(i.e. value).
                        for (Enumeration e2 = serverProps.propertyNames(); e2.hasMoreElements() ;) {
                            String name2 = (String)e2.nextElement();            
                            if (name2.toLowerCase().indexOf(PORT_SUFFIX) >= 0) {
                                String value2 = (String)serverProps.getProperty(name2);
                                if (!name2.equals(name) && value.equals(value2)) {
                                    PortInUseException ex2 = new PortInUseException(
                                        serverName, hostName, port, name, name2); 
                                    if (result == null) {
                                        result = ex2;
                                    } else {
                                        result.augmentException(ex2);
                                    }
                                }
                            }
                        }
                    }
                } catch (NumberFormatException ex) {
                    continue;
                }                
            }       
        }
        return result;
    }
    
    /**
     * Iterates through all the properties of the server and sees if any of the ports
     * conflict
     */
    private PortInUseException checkForPortInUseConflicts(Properties serverProps, 
        String hostName, String serverName, PortInUseException exception) 
        throws ConfigException, InstanceException
    {        
        final ConfigContext configContext = getConfigContext();           
        PortInUseException result = exception;
                        
        //Iterate through all the properties of the newly created server and check to 
        //see if any of the ports are in use.
        for (Enumeration e = serverProps.propertyNames() ; e.hasMoreElements() ;) {            
            String name = (String)e.nextElement();            
            if (name.toLowerCase().indexOf(PORT_SUFFIX) >= 0) {
                String value = (String)serverProps.getProperty(name);
                try {
                    int port = Integer.parseInt(value);
                    //If we have already detected a conflict 
                    //then there is no need to check the port for conflicts again.
                    if (result == null || !result.portAlreadyConflicts(port)) {                                            
                        //If the name of the property contains the string "port" and 
                        //the value of the property is numeric then check for a 
                        //port conflict.                         
                        if (!NetUtils.isPortFree(hostName, port)) {     
                            //System.out.println("yes");
                            PortInUseException ex2 = new PortInUseException(
                                serverName, hostName, port, name, null); 
                            if (result == null) {
                                result = ex2;
                            } else {
                                result.augmentException(ex2);
                            }
                        }
                    }
                } catch (NumberFormatException ex) {
                    continue;
                }                
            }                        
        }
        return result;
    }
    
            
    /**
     * Checks for port conflicts against the given list of servers.
     */
    private PortInUseException checkForServerPortPropertyConflicts(Properties serverProps, 
        String hostName, String serverName, Server[] servers, PortInUseException exception) 
        throws ConfigException, InstanceException
    {
        PropertyConfigBean pcb = getPropertyConfigBean();

        PortInUseException result = exception;
        //Iterate through all the properties of the newly created server and check for conflicts
        //with all other servers on the same node agent.

        for (Enumeration e = serverProps.propertyNames() ; e.hasMoreElements() ;) {            
            String name = (String)e.nextElement();            
            if (name.toLowerCase().indexOf(PORT_SUFFIX) >= 0) {
                String value = (String)serverProps.getProperty(name);
                try {
                    int port = Integer.parseInt(value);
                    //If we have already detected a conflict (while checking for DAS port conflicts, 
                    //then there is no need to check the port for conflicts again.
                    if (result == null || !result.portAlreadyConflicts(port)) {                                            
                        //If the name of the property contains the string "port" and 
                        //the value of the property is numeric then check for a 
                        //port conflict against the other servers on the same 
                        //machine (i.e. with the same node agent).
                        String nextServerName = null;
                        for (int i = 0; i < servers.length; i++) {
                            nextServerName = servers[i].getName();
                            //We do not want to check against port conflicts with ourself.
                            if (!nextServerName.equals(serverName)) {
                                Properties props = pcb.getTargetedProperties( 
                                    nextServerName, true);
                                try {
                                    checkForPortPropertyConflicts(name, port, 
                                        props, nextServerName, hostName);
                                } catch (PortInUseException ex) {
                                    //Add on any newly found conflict to the exception
                                    if (result == null) {
                                        result = ex;
                                    } else {
                                        result.augmentException(ex);
                                    }
                                }
                            }
                        }
                    }
                } catch (NumberFormatException ex) {
                    continue;
                }                
            }                        
        }
        return result;
    }
           
    
    /**
     * Check to see if the newly created server has any port conflicts with 
     * the DAS (if it is on the same machine) or with any other server's on 
     * the same machine (i.e. with the same node agent). Keep in mind that
     * the specified server must already been added to the list of servers.
     */
    
    void checkForPortConflicts(Server server, Properties newServerProps, 
        boolean isServerRunning) throws ConfigException, PortInUseException, 
        InstanceException, AgentException
    {
        final String serverName = server.getName();
        final Properties existingServerProps = getPropertyConfigBean().getTargetedProperties(
            serverName, true);
        checkForPortConflicts(server, existingServerProps, newServerProps, 
            isServerRunning);
    }
    
    /**
     * Check for port conflicts
     * @param server -- The serverare to be checked for conflicts
     * @param existingServerProps -- The existing properties of the server (including all those
     * that are inherited from its config.
     * @param newServerProps -- The list of new system properties for an existing server that 
     * we are applying.
     * @param isServerRunning -- is the server up and running
     * @param checkForPortConflicts -- should we check to see if ports are in use (i.e. someone
     * is listening)
     * @param checkForPropertyConflicts -- should we check for port conflicts resulting from 
     * system properties in domain.xml
     * @throws ConfigException
     * @throws PortInUseException
     * @throws InstanceException
     * @throws AgentException
     */    
    void checkForPortConflicts(Server server, 
        Properties existingServerProps, Properties newServerProps, 
        boolean isServerRunning) 
        throws ConfigException, PortInUseException, InstanceException, AgentException
    {
        final ConfigContext configContext = getConfigContext();                            
        final String serverName = server.getName();        
        final MBeanServerConnectionInfo serverInfo = InstanceRegistry.getInstanceConnectionInfo(
            configContext, serverName);
        final String hostName = serverInfo.getHost();
        final boolean isBoundToHost = isBoundToHost(server);
        
        PortInUseException exception = null;                
        
        //check for invalid ports (i.e. non-numeric ports or ports that are outside the valid range).
        checkForInvalidPorts(existingServerProps);
        
        //Check for duplicate ports within the server instance itself        
        exception = checkForDuplicatePorts(existingServerProps, 
            hostName, serverName, exception);
        
        //If the server (including the DAS) is bound to a particular host, then first to see if any of the 
        //system properties containing ports are in use on that host. We only want to check for port 
        //conflicts if the server is not running (otherwise, we will always be conflicting with ourself).
        if (isBoundToHost) {
            if (!isServerRunning) {
                //We only want to check for port conflicts if the server is not running; 
                //otherwise, we will get port conflicts with ourselves.
                exception = checkForPortInUseConflicts(existingServerProps, 
                    hostName, serverName, exception);         
            } else if (newServerProps != null) {
                //If we are running, then we can only check for port conflicts on the newly 
                //specified properties.
                exception = checkForPortInUseConflicts(newServerProps, 
                    hostName, serverName, exception);    
            }
        }             
        
        //Now we want to look for system-property conflicts meaning that we exaime the 
        //system-property elements of other servers on the same host and look for conflicts.
        //This allows us to detect conflicts without the servers running and the port 
        //actually in use.
        if (!ServerHelper.isDAS(configContext, server))  {            
            //The server is not the DAS but is bound to a host and is running on the same host as 
            //the DAS, the we check for port conflicts with the DAS itself;
            if (isServerRunningOnDASHost(server)) {
                Server[] servers = new Server[1];
                servers[0] = ServerHelper.getDAS(configContext);
                exception = checkForServerPortPropertyConflicts(existingServerProps, 
                    hostName, serverName, servers, exception);   
            }
                      

            //Check for port conflicts with the Server instances associated with the server's node
            //agent (i.e. all other servers that share the same node agent.       
            Server[] servers = ServerHelper.getServersOfANodeAgent(configContext,
                server.getNodeAgentRef());        
            exception = checkForServerPortPropertyConflicts(existingServerProps, 
                hostName, serverName, servers, exception);   

            //Look for other node agents on the same machine and repeat the drill. This is overkill, since
            //there really is no use case for having 2 node agents servicing the same domain on the 
            //same machine.
            if (isBoundToHost) {
                NodeAgent agent = NodeAgentHelper.getNodeAgentForServer(configContext, serverName);
                NodeAgent[] agents = getOtherNodeAgentsOnSameHost(agent);
                for (int i = 0; i < agents.length; i++) {
                    servers = ServerHelper.getServersOfANodeAgent(configContext,
                        agents[i].getName());                        
                    exception = checkForServerPortPropertyConflicts(existingServerProps, 
                        hostName, serverName, servers, exception); 
                }  
            }
        } else {
            //If we are checking for port conflicts in the DAS, then we look for conflicts with 
            //all instances running on the same host.
            Server[] servers = getServersRunningOnDASHost(false);        
            exception = checkForServerPortPropertyConflicts(existingServerProps, 
                hostName, serverName, servers, exception);   
        }        

        if (exception != null) {
            throw exception;
        }
    }        
    
    private PropertyConfigBean getPropertyConfigBean() {
        return new PropertyConfigBean(getConfigContext());
    }
    
}
