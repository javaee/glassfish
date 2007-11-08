/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
/*
 * NodeAgentHelper.java
 *
 * Created on October 23, 2003, 11:27 AM
 */

package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.NodeAgents;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.JmxConnector;

import com.sun.enterprise.security.store.IdentityManager;

import com.sun.enterprise.admin.util.JMXConnectorConfig;
import com.sun.enterprise.admin.util.IAdminConstants;

import java.util.ArrayList;

/**
 *
 * @author  kebbs
 */
public class NodeAgentHelper extends ConfigAPIHelper {
        
    public static NodeAgent[] getNodeAgentsInDomain(ConfigContext configContext) 
        throws ConfigException
    {
        final Domain domain = getDomainConfigBean(configContext);  
        NodeAgent[] nas = new NodeAgent[0];
        if (domain.getNodeAgents() != null) 
            nas = domain.getNodeAgents().getNodeAgent();
        return nas;
    }
       
    
    public static boolean isANodeAgent(ConfigContext configContext, String agentName) 
        throws ConfigException
    {
        final Domain domain = getDomainConfigBean(configContext);    
        final NodeAgents controllers = domain.getNodeAgents();
        if (controllers == null) {
            return false;
        }
        final NodeAgent controller = controllers.getNodeAgentByName(agentName);
        return (controller != null ? true : false);
    }
       
    public static boolean hasNodeAgentRendezvousd(ConfigContext configContext, 
        NodeAgent agent) throws ConfigException
    {        
        ElementProperty rendezvousProperty = agent.getElementPropertyByName(
            IAdminConstants.RENDEZVOUS_PROPERTY_NAME);
        String rendezvous=rendezvousProperty.getValue();                
        if (rendezvous != null && rendezvousProperty.getValue().equals(Boolean.TRUE.toString())) {
            return true;
        }             
        return false;
    }
        
    public static NodeAgent getNodeAgentByName(ConfigContext configContext, String agentName)
        throws ConfigException
    {
        final Domain domain = getDomainConfigBean(configContext);               
        final NodeAgent controller = domain.getNodeAgents().getNodeAgentByName(agentName);
        if (controller == null) {
            throw new ConfigException(_strMgr.getString("noSuchAgent", 
                agentName));
        }
        return controller;
    }    
        
    public static NodeAgent getNodeAgentForServer(ConfigContext configContext, String instanceName) 
        throws ConfigException
    {        
        final Server server = ServerHelper.getServerByName(configContext, instanceName);
        final Domain domain = getDomainConfigBean(configContext);          
        final NodeAgent controller = domain.getNodeAgents().getNodeAgentByName(
            server.getNodeAgentRef());
        if (controller == null) {
            throw new ConfigException(_strMgr.getString("noSuchAgentForInstance", 
                instanceName, server.getNodeAgentRef()));
        }
        return controller;
    }
        
    
    public static NodeAgent[] getNodeAgentsForCluster(ConfigContext configContext, String clusterName) 
        throws ConfigException
    {     
        final ArrayList result = new ArrayList();
        final Server[] servers = ServerHelper.getServersInCluster(configContext, clusterName);
        for (int i = 0; i < servers.length; i++) {
            NodeAgent controller = getNodeAgentForServer(configContext, 
                servers[i].getName());
            if (!result.contains(controller)) {
                result.add(controller);
            }
        }
        return (NodeAgent[])result.toArray(new NodeAgent[result.size()]);
    }
   
    public static JmxConnector getNodeAgentSystemConnector(ConfigContext configContext, String agentName) 
        throws ConfigException 
     {
        final NodeAgent controller = getNodeAgentByName(configContext, agentName);        
        final String systemConnectorName = controller.getSystemJmxConnectorName();
        final JmxConnector connector = controller.getJmxConnector();
        if (connector == null) {
            throw new ConfigException(_strMgr.getString("noAgentSystemConnector", agentName, 
                systemConnectorName));
        }
        return connector;
    }
    
    /**
     * Returns mbean server connection info for the named node agent.
     */
    public static JMXConnectorConfig getJMXConnectorInfo(ConfigContext configContext, String nodeAgentName) 
        throws ConfigException
    {        
        JmxConnector connector = NodeAgentHelper.getNodeAgentSystemConnector(
            configContext, nodeAgentName);        
        String adminUser = IdentityManager.getUser();
        String adminPassword = IdentityManager.getPassword();     
        ElementProperty hostProp = connector.getElementPropertyByName(HOST_PROPERTY_NAME);    

        if (adminUser == null || adminPassword == null || hostProp == null) {
            throw new ConfigException(_strMgr.getString("missingAgentConnectorAuth", nodeAgentName));
        }
        return new JMXConnectorConfig(hostProp.getValue(), connector.getPort(),                 
            adminUser, adminPassword, connector.getProtocol());
    }

     /** Returns Node Agents as comma-separated list with each element being name of NA.
       * Returns an empty String if there are no node-agents in the array passed or
       * the parameter is null.
       */
     public static String getNodeAgentsAsString(final NodeAgent[] nas) {
         String nasas = "";
         if (nas != null) {
             for (int i = 0 ; i < nas.length ; i++) {
                 nasas += nas[i].getName();
                 if (i < nas.length - 1)
                     nasas += ",";
             }
         }
         return nasas;
     }
    
}
