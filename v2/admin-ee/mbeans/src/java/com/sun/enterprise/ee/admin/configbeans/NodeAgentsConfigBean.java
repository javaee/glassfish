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

import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.NodeAgents;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.serverbeans.Ssl;
import com.sun.enterprise.config.serverbeans.AuthRealm;
import com.sun.enterprise.config.serverbeans.LogService;
import com.sun.enterprise.config.serverbeans.ModuleLogLevels;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.ConfigAPIHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.NodeAgentHelper;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.common.Status;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.admin.util.JMXConnectorConfig;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.enterprise.ee.admin.servermgmt.AgentManager;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;
import com.sun.enterprise.ee.admin.servermgmt.AgentConfig;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;
import com.sun.enterprise.admin.servermgmt.KeystoreManager;

import com.sun.enterprise.ee.admin.mbeanapi.NodeAgentMBean;
import com.sun.enterprise.ee.admin.proxy.NodeAgentProxy;

import com.sun.enterprise.ee.admin.clientreg.NodeAgentRegistry;
import com.sun.enterprise.ee.admin.clientreg.InstanceRegistry;

import com.sun.logging.ee.EELogDomains;

import com.sun.enterprise.ee.admin.ExceptionHandler;

import com.sun.enterprise.admin.configbeans.BaseConfigBean;

import com.sun.enterprise.ee.admin.concurrent.Task;
import com.sun.enterprise.ee.admin.concurrent.Executor;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * <p>MBean class that facilitates the configuration of the NodeAgent for CLI and remote clients
 *
 */

//ISSUE: Do we really want to throws an AgentException here as this will clients
//using this mbean to have our runtime; however we seem to be throwing our own
//exceptions everywhere else in the mbeans. The problem with MBeanException
//currently is that it masks the real exception (due to the fact that MBeanHelper
//does some bogus formatting on the exception.

public class NodeAgentsConfigBean extends BaseConfigBean implements IAdminConstants
{

    class GetRuntimeStatusTask extends Task {
        private static final long TIMEOUT_IN_MILLIS = 30000; //30 seconds
        private String _agentName;
        private RuntimeStatus _status;

        public GetRuntimeStatusTask(String agentName) {
            super(TIMEOUT_IN_MILLIS);
            _agentName = agentName;
            _status = new RuntimeStatus();
        }

        public RuntimeStatus getStatus() {
            return _status;
        }

        public void run() {
            try {
                _status = getRuntimeStatus(_agentName);
            } catch (AgentException ex) {
                StringManagerBase sm = StringManagerBase.getStringManager(
                    getLogger().getResourceBundleName());
                getLogger().log(Level.WARNING,
                    sm.getString("nodeagent.listNodeAgents.Exception", _agentName), ex);
                _status = new RuntimeStatus();
            }
        }
    }

    private static final StringManager _strMgr =
        StringManager.getManager(NodeAgentsConfigBean.class);

    private static Logger _logger = null;

    public NodeAgentsConfigBean(ConfigContext configContext) {
        super(configContext);
    }

    private static Logger getLogger()
    {
        if (_logger == null) {
            //We explicitly call EELogDomains.getLogger instead of Logger.getLogger()
            //so that our resource bundle will be properly initialized. This allows this 
            //code to be invoked from something (i.e. in the case the CLI) which does not
            //have a custom log manager.
            _logger = EELogDomains.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }

    private static ExceptionHandler _handler = null;

    //The exception handler is used to parse and log exceptions
    protected static ExceptionHandler getExceptionHandler()
    {
        if (_handler == null) {
            _handler = new ExceptionHandler(getLogger());
        }
        return _handler;
    }

    public void clearRuntimeStatus(String agentName) throws AgentException
    {
        try {
            NodeAgentMBean agentMBean = NodeAgentProxy.getNodeAgentProxy(agentName);
            agentMBean.clearRuntimeStatus();
        } catch (Exception ex) {
            //Ignore any error indicating that the server is unreachable.
            //FIXTHIS: We could expect the proxy to do this for us as it
            //seems to be a common case.
            if (NodeAgentProxy.isUnreachable(ex)) {
                // do nothing
            } else {
                throw new AgentException(ex);
            }
        }
    }

    public RuntimeStatus getRuntimeStatus(String agentName) throws AgentException
    {
        try {
            NodeAgentMBean agentMBean = NodeAgentProxy.getNodeAgentProxy(agentName);
            return agentMBean.getRuntimeStatus();
        } catch (Exception ex) {
            //Ignore any error indicating that the server is unreachable.
            //FIXTHIS: We could expect the proxy to do this for us as it
            //seems to be a common case.
            if (NodeAgentProxy.isUnreachable(ex)) {
                return new RuntimeStatus(agentName);
            } else {
                throw new AgentException(ex);
            }
        }
    }

    public boolean isRunning(String agentName) throws AgentException
    {
        return getRuntimeStatus(agentName).isRunning();
    }
    
    /**
     * Fetches the runtime status of the given server instances in parallel.
     */
    public RuntimeStatusList getRuntimeStatus(NodeAgent[] agents)
    {
        GetRuntimeStatusTask[] tasks = new GetRuntimeStatusTask[agents.length];
        for (int i = 0; i < agents.length; i++) {
            tasks[i] = new GetRuntimeStatusTask(agents[i].getName());
        }
        Executor exec = new Executor(tasks);
        exec.run();
        RuntimeStatusList result = new RuntimeStatusList(agents.length);
        for (int i = 0; i < agents.length; i++) {
            result.add(tasks[i].getStatus());
        }
        return result;
    }

    
    public RuntimeStatusList getNodeAgentRuntimeStatus(String targetName) throws AgentException
    {
        final StringManager stringMgr = StringManager.getManager(AgentManager.class);
        try {
            final TargetType[] validTargets = {TargetType.NODE_AGENT, TargetType.CLUSTER,
                TargetType.DOMAIN, TargetType.SERVER};
            final ConfigContext configContext = getConfigContext();
            Target target = TargetBuilder.INSTANCE.createTarget(
                DOMAIN_TARGET, validTargets, targetName,
                configContext);
            final NodeAgent[] agents = target.getNodeAgents();          
                //Fetch the status of all the node agents in parallel
            return getRuntimeStatus(agents);            
        } catch (Exception ex) {
            throw getExceptionHandler().handleAgentException(
                ex, "nodeagent.listNodeAgents.Exception", targetName);
        }
    }
    
    /**
     * lists node agents and their status
     */
    public String[] listNodeAgentsAsString(String targetName, boolean andStatus) throws AgentException
    {
        final StringManager stringMgr = StringManager.getManager(AgentManager.class);
        try {
            final TargetType[] validTargets = {TargetType.NODE_AGENT, TargetType.CLUSTER,
                TargetType.DOMAIN, TargetType.SERVER};
            final ConfigContext configContext = getConfigContext();
            Target target = TargetBuilder.INSTANCE.createTarget(
                DOMAIN_TARGET, validTargets, targetName,
                configContext);
            final NodeAgent[] agents = target.getNodeAgents();
            final int numAgents = agents.length;
            String[] result = new String[numAgents];
            String nodeAgentName = null;
            RuntimeStatusList statusList = null;
            if (andStatus) {
                //Fetch the status of all the node agents in parallel
                statusList = getRuntimeStatus(agents);
            }
            for (int i = 0; i < numAgents; i++) {
                nodeAgentName = agents[i].getName();
                if (andStatus) {
                    result[i] = stringMgr.getString("listAgentElement", nodeAgentName,
                        (statusList.getStatus(i)).toShortString());
                } else {
                    result[i] = nodeAgentName;
                }
            }
            return result;
        } catch (Exception ex) {
            throw getExceptionHandler().handleAgentException(
                ex, "nodeagent.listNodeAgents.Exception", targetName);
        }
    }

    /**
     * Removes the specified node agent. This operation is triggered by the asadmin
     * delete-nodeagent-config command.
     */
    public void deleteNodeAgentConfig(String nodeAgentName) throws AgentException
    {
        try {
            final ConfigContext configContext = getConfigContext();
            //Get the node agent specified by nodeAgentName and ensure that it exists
            NodeAgent controller = NodeAgentHelper.getNodeAgentByName(configContext,
                nodeAgentName);

            //Ensure that there are no server instances referring to the node agent
            Server[] servers = ServerHelper.getServersOfANodeAgent(configContext,
                nodeAgentName);
            if (servers.length > 0) {
                throw new AgentException(_strMgr.getString("agentHasServerReferrences",
                    nodeAgentName, ServerHelper.getServersAsString(servers)));
            }

            //Get a connection to the agent's mbean server before removing the node controller;
            //otherwise, we may never be able to find it in domain.xml when we want to
            //synchronizeWithDAS below.
            try {
                NodeAgentRegistry.getNodeAgentConnection(nodeAgentName);
            } catch (Exception ex) {
                //The node agent may be unreachable (i.e. not listening) so this exception is expected.
                if (!NodeAgentRegistry.isMBeanServerUnreachable(ex)) {
                    throw new AgentException(ex);
                }
            }

            Domain domain = ConfigAPIHelper.getDomainConfigBean(configContext);
            NodeAgents controllers = domain.getNodeAgents();
            controllers.removeNodeAgent(controller, OVERWRITE);

            //Notify the Node Agent to rendezvous
            //FIXTHIS: We force persistence, clear any notifications, and update the
            //Application server's config context explicitely. Until this is modelled
            //as an event notification (TBD) we need this to happen before notifying or
            //the Node Agent will not synchronize the correct data.
            //QUESTION: What happens if an exception is thrown above (e.g. in addNodeAgent). How do
            //we restore the admin config context to its previous (and unpersisted value)???
            flushAll();

            //FIXTHIS: Currently we hard code the DAS info. The issues are
            //1) we need a system jmx-connector from the DAS from which to obtain host, port
            //and protocol. This is not yet re-integrated
            //2) we need a place to store the DAS authentication information -- new properties
            //in domain.xml

            NodeAgentMBean agentMBean = NodeAgentProxy.getNodeAgentProxy(nodeAgentName);
            agentMBean.synchronizeWithDAS();

            // clear the JMXConnectorRegistry cache, do not want to leave unused connections around
            NodeAgentRegistry.removeNodeAgentConnection(nodeAgentName);
        } catch (Exception ex) {
            throw getExceptionHandler().handleAgentException(
                ex, "nodeagent.unbindNodeAgent.Exception", nodeAgentName);
        }
    }


    /**
     * Adds the specified Node Agent to domain.xml. This operation is invoked by the asadmin
     * create-nodeagent-config command that updates domain.xml.
     */
    public void createNodeAgentConfig(String nodeAgentName) throws AgentException
    {
        // set defaults until domail.xml's dtd has been changed ???
        String host="localhost";
        String port="9999";
        try {
            final ConfigContext configContext = getConfigContext();
            //validate name uniqueness
            if (!ConfigAPIHelper.isNameUnique(configContext, nodeAgentName)) {
                 throw new AgentException(_strMgr.getString("agentNameNotUnique",
                    nodeAgentName));
            }

            //Get the node controller specified by nodeAgentName and ensure that it does not
            //already exist
            Domain domain = ServerBeansFactory.getDomainBean(configContext);
            NodeAgents controllers =domain.getNodeAgents();
            NodeAgent controller = controllers.getNodeAgentByName(nodeAgentName);
            if (controller != null) {
                throw new AgentException(_strMgr.getString("agentAlreadyExists", nodeAgentName));
            }

            // add in "0.0.0.0"  as use host as clientHostName has a place holder until CLI is decided ???
            addNodeAgent(AgentConfig.NODEAGENT_DEFAULT_HOST_ADDRESS, port, nodeAgentName,
                AgentConfig.NODEAGENT_JMX_DEFAULT_PROTOCOL, host, Boolean.FALSE);

        } catch (Exception ex) {
            throw getExceptionHandler().handleAgentException(
                ex, "nodeagent.bindNodeAgent.Exception", nodeAgentName);
        }
    }


    /**
     * Adds the specified Node Agent to the domain. This operation is invoked from
     * the Node Agent when it is initiating the rendezvous.
     */
    public String rendezvousWithDAS(String host, String port,
        String nodeAgentName, String protocol, String clientHostName) throws AgentException
    {
        String sxRet="";
        try {
            final ConfigContext configContext = getConfigContext();

            //Get the node controller specified by nodeAgentName and ensure that it does not
            //already exist
            Domain domain = ServerBeansFactory.getDomainBean(configContext);
            NodeAgents controllers =domain.getNodeAgents();
            NodeAgent controller = controllers.getNodeAgentByName(nodeAgentName);

            if (controller != null) {
               ElementProperty rendezvousProperty = controller.getElementPropertyByName(RENDEZVOUS_PROPERTY_NAME);
               String rendezvous=rendezvousProperty.getValue();
               if (rendezvous != null && rendezvousProperty.getValue().equals(Boolean.TRUE.toString())) {
                    // should only rendezvous once, not the same, throw exception
                    throw new AgentException(_strMgr.getString("agentAlreadyExists", nodeAgentName));
                }

                // alter node agent in domain.xml to the criteria sent
                alterNodeAgent(host, port, nodeAgentName, protocol, clientHostName, Boolean.TRUE);

            } else {
                // need to make sure a unique instance is being added.
                if (!ConfigAPIHelper.isNameUnique(configContext, nodeAgentName)) {
                     throw new AgentException(_strMgr.getString("agentNameNotUnique",
                        nodeAgentName));
                }

                // add node agent to domain.xml
                addNodeAgent(host, port, nodeAgentName, protocol, clientHostName, Boolean.TRUE);
            }

            // after proper nodeagent rendezvous, return port for system jmx connector
            String dasName = System.getProperty(SystemPropertyConstants.SERVER_NAME);
            if (dasName == null) {
                throw new AgentException(_strMgr.getString("noDASServerNameProperty"));
            }
            
            JmxConnector dasConnector=ServerHelper.getServerSystemConnector(configContext, dasName);
            //JMXConnectorConfig dasConnectorConfig = ServerHelper.getJMXConnectorInfo(configContext, dasName);
            getLogger().log(Level.FINE," Nodeagent: " + nodeAgentName + " has Renezvoused -  returning DAS JMXport|Security Enabled - " + 
                dasConnector.getPort() + "|" + dasConnector.isSecurityEnabled());
            sxRet=dasConnector.getPort() + "|" + String.valueOf(dasConnector.isSecurityEnabled());

        } catch (Exception ex) {
            throw getExceptionHandler().handleAgentException(
                ex, "nodeagent.rendezvousWithDAS.Exception", nodeAgentName);
        }

        return sxRet;
    }


    protected void addNodeAgent(String host, String port,
        String nodeAgentName, String protocol, String clientHostName, Boolean rendezvousOccurred)
        throws AgentException
    {
        try {
            final ConfigContext configContext = getConfigContext();
            //Validate port number since it is a string...
            try {
                Integer.parseInt(port);
            } catch (NumberFormatException ex) {
                throw new AgentException(_strMgr.getString("portMustBeNumeric",
                    port));
            }

            NodeAgents controllers = ServerBeansFactory.getDomainBean(configContext).getNodeAgents();

            //Create the new node controller
            NodeAgent controller = new NodeAgent();
            controller.setName(nodeAgentName);

            ElementProperty rendezvousProperty = new ElementProperty();
            rendezvousProperty.setName(RENDEZVOUS_PROPERTY_NAME);
            rendezvousProperty.setValue(rendezvousOccurred.toString());
            controller.addElementProperty(rendezvousProperty);

            JmxConnector connector = new JmxConnector();
            connector.setName(SYSTEM_CONNECTOR_NAME);
            connector.setAddress(host);
            connector.setPort(port);
            connector.setProtocol(protocol);            
            Ssl ssl = new Ssl();
            ssl.setCertNickname(KeystoreManager.CERTIFICATE_ALIAS);
            connector.setSsl(ssl);

            ElementProperty hostnameProperty = new ElementProperty();
            hostnameProperty.setName(HOST_PROPERTY_NAME);
            hostnameProperty.setValue(clientHostName);
            connector.addElementProperty(hostnameProperty);

            //ISSUE: Not sure how to set the realm name here??? The realm name in the jmx-connector
            //element refers to a realm in the security-service which is associated with a configuration.
            //Unfortunately, the node-agent does not reference a configuration!!!!!!
            connector.setAuthRealmName("admin-realm");
            controller.setJmxConnector(connector);

            // TODO: need to reconcile authrealms ???
            AuthRealm[] authRealms = new AuthRealm[1];
            authRealms[0]=new AuthRealm();
            authRealms[0].setName("admin-realm");
            authRealms[0].setClassname("com.sun.enterprise.security.auth.realm.file.FileRealm");
            ElementProperty fileProperty = new ElementProperty();
            fileProperty.setName("file");
            fileProperty.setValue("${com.sun.aas.instanceRoot}/config/admin-keyfile");
            authRealms[0].addElementProperty(fileProperty);
            ElementProperty jaasContextProperty = new ElementProperty();
            jaasContextProperty.setName("jaas-context");
            jaasContextProperty.setValue("fileRealm");
            authRealms[0].addElementProperty(jaasContextProperty);
            controller.setAuthRealm(authRealms[0]);

            controller.setSystemJmxConnectorName(SYSTEM_CONNECTOR_NAME);

            LogService log = new LogService();
            ModuleLogLevels logLevels = new ModuleLogLevels();
            log.setModuleLogLevels(logLevels);
            log.setFile("${com.sun.aas.instanceRoot}/logs/server.log");
            controller.setLogService(log);

            controllers.addNodeAgent(controller, OVERWRITE);

            //Notify the Node Agent to rendezvous
            //FIXTHIS: We force persistence, clear any notifications, and update the
            //Application server's config context explicitely. Until this is modelled
            //as an event notification (TBD) we need this to happen before notifying or
            //the Node Agent will not synchronize the correct data.
            //QUESTION: What happens if an exception is thrown above (e.g. in addNodeAgent). How do
            //we restore the admin config context to its previous (and unpersisted value)???
            flushAll();
        } catch (Exception ex) {
            throw getExceptionHandler().handleAgentException(
                ex, "nodeagent.addNodeAgent.Exception", nodeAgentName);
        }
    }


    protected void alterNodeAgent(String host, String port,
        String nodeAgentName, String protocol, String clientHostName, Boolean rendezvousOccurred)
        throws AgentException
    {
        try {
            final ConfigContext configContext = getConfigContext();
            // Validate port number since it is a string...
            try {
                Integer.parseInt(port);
            } catch (NumberFormatException ex) {
                throw new AgentException(_strMgr.getString("portMustBeNumeric",
                    port));
            }

            // TODO: get nodeagent to alter, ** need to verify code when dtd is finalized ???
            NodeAgents controllers = ServerBeansFactory.getDomainBean(configContext).getNodeAgents();
            NodeAgent controller = controllers.getNodeAgentByName(nodeAgentName);

            // Alter proper rendezvousProperties
            ElementProperty rendezvousProperty=controller.getElementPropertyByName(RENDEZVOUS_PROPERTY_NAME);
            if (rendezvousProperty != null) {
                controller.removeElementProperty(rendezvousProperty);
            }
            
            rendezvousProperty=new ElementProperty();
            rendezvousProperty.setName(RENDEZVOUS_PROPERTY_NAME);
            rendezvousProperty.setValue(rendezvousOccurred.toString());
            controller.addElementProperty(rendezvousProperty);                         

            // add/alter proper JmxConnector
            JmxConnector connector=controller.getJmxConnector();
            // alter/add attribute information
            connector.setAddress(host);
            connector.setPort(port);
            connector.setProtocol(protocol);

            // set host name for jmxconnector
            ElementProperty hostnameProperty=connector.getElementPropertyByName(HOST_PROPERTY_NAME);
            if(hostnameProperty == null) {
                hostnameProperty=new ElementProperty();
                hostnameProperty.setName(HOST_PROPERTY_NAME);
                connector.addElementProperty(hostnameProperty);
            }
            hostnameProperty.setValue(clientHostName);

            //Notify the Node Agent to rendezvous
            //FIXTHIS: We force persistence, clear any notifications, and update the
            //Application server's config context explicitely. Until this is modelled
            //as an event notification (TBD) we need this to happen before notifying or
            //the Node Agent will not synchronize the correct data.
            //QUESTION: What happens if an exception is thrown above (e.g. in addNodeAgent). How do
            //we restore the admin config context to its previous (and unpersisted value)???
            flushAll();
        } catch (Exception ex) {
            throw getExceptionHandler().handleAgentException(
                ex, "nodeagent.addNodeAgent.Exception", nodeAgentName);
        }
    }
}
