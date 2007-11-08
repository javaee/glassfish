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
 * ServerConfigHelper.java
 *
 * Created on October 23, 2003, 11:15 AM
 */

package com.sun.enterprise.config.serverbeans;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.security.store.IdentityManager;

import com.sun.enterprise.admin.util.JMXConnectorConfig;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import com.sun.enterprise.admin.jmx.remote.server.rmi.JmxServiceUrlFactory;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.net.InetAddress;
import java.util.Arrays;

/**
 *
 * @author  kebbs
 * TODO: We may want to hide the DAS (instance "server") completely from any server list, etc. 
 * Currently the DAS is treated like any other server instance; however, the user should 
 * be able to specify "server" as a target, thus allowing the PE like deployment. Obviously
 * this needs some thought.
 */
public class ServerHelper extends ConfigAPIHelper {       

    public static final String ADMIN_HTTP_LISTNER_ID  = "admin-listener";
    public static final String DEFAULT_CERT_NICKNAME = "s1as";    

    /**
     * Return the given server array as a comma separated string
     */
    public static String getServersAsString(Server[] servers)
    {
        String result = "";
        for (int i = 0; i < servers.length; i++) {
            result += servers[i].getName();
            if (i < servers.length - 1) {
                result += ",";
            }
        }
        return result;
    }
    
    /**
     * Return all the servers in the domain. We do not check for null since we assume
     * at least one server instance ("server") in the domain.
     */     
    public static Server[] getServersInDomain(ConfigContext configContext) 
        throws ConfigException
    {
        final Domain domain = getDomainConfigBean(configContext);          
        return domain.getServers().getServer();        
    }
      
    /**
     * Return true if the given server instance is a server instance.
     */
    public static boolean isAServer(ConfigContext configContext, String instanceName) 
        throws ConfigException
    {
        final Domain domain = getDomainConfigBean(configContext);          
        final Server server = domain.getServers().getServerByName(instanceName);
        return (server != null ? true : false);
    }
    
    /**
     * Return the server associated with the given instanceName. An exception is 
     * thrown if the instanceName does not correspond to a server instance, or does
     * not exist.
     */
    public static Server getServerByName(ConfigContext configContext, String instanceName) 
        throws ConfigException
    {
        final Domain domain = getDomainConfigBean(configContext);        
        final Server server = domain.getServers().getServerByName(instanceName);
        if (server == null) {
            throw new ConfigException(_strMgr.getString("noSuchInstance", 
                instanceName));
        }
        return server;
    }
     
    public static Server getDAS(ConfigContext configContext) 
        throws ConfigException
    {
        //Now find all server instances that reference that config.
        Server[] servers = getServersInDomain(configContext);    
        Server das = null;
        for (int i = 0; i <  servers.length; i++) {
            if (isDAS(configContext, servers[i])) {
                if (das == null) {
                    das = servers[i];
                } else {
                    //Throw an exception if we found more than 1 DAS. This is 
                    //more of a sanity check and can be removed if there are 
                    //performance concerns.
                    throw new ConfigException(_strMgr.getString("tooManyDASFound", das.getName(), 
                        servers[i].getName()));
                }
            }
        }        
        if (das == null) {
            //Throw an exception if we could not identify the DAS.
            throw new ConfigException(_strMgr.getString("noDASFound"));
        }
        return das;
    }
    
    /**
     * Return the server instances referencing the given configName or a zero length list.
     */
    public static Server[] getServersReferencingConfig(ConfigContext configContext, String configName) 
        throws ConfigException
    {        
        //First ensure that the config exists
        Config config = getConfigByName(configContext, configName);
        
        //Now find all server instances that reference that config.
        Server[] servers = getServersInDomain(configContext);
        ArrayList result = new ArrayList();
        for (int i = 0; i < servers.length; i++) {
            if (servers[i].getConfigRef().equals(configName)) {
                result.add(servers[i]);
            }            
        }
        return (Server[])result.toArray(new Server[result.size()]);
    }


    /**
     * Return the list of stand-alone instances
     * Based on DTD each stand-alone instances points to a named config.
     * If the server instance is part of a cluster then it will not have a config.
     */
    public static Server[] getStandAloneServers(
        ConfigContext configContext, boolean excludeDASInstance)
        throws ConfigException 
    {        
        Server[] servers = getServersInDomain(configContext);        
        ArrayList result = new ArrayList();        
        for (int i = 0; i < servers.length; i++) {            
            if (isDAS(configContext, servers[i]) && excludeDASInstance) {
                continue;
            }            
            if (isServerStandAlone(configContext, servers[i].getName())) {
                result.add(servers[i]);
            }
        }
        return (Server[])result.toArray(new Server[result.size()]);
    }

    /**
        Returns an array of unclustered servers. Note that the difference 
        between an unclustered server and a standalone server is that an
        unclustered server may share its config with other servers whereas
        a standalone server must not share its config with other servers.
     */
    public static Server[] getUnclusteredServers(
        ConfigContext configContext, boolean excludeDASInstance)
        throws ConfigException 
    {        
        Server[] servers = getServersInDomain(configContext);        
        ArrayList result = new ArrayList();        
        for (int i = 0; i < servers.length; i++) {            
            if (isDAS(configContext, servers[i]) && excludeDASInstance) {
                continue;
            }
            if (!isServerClustered(configContext, servers[i])) {
                result.add(servers[i]);
            }
        }
        return (Server[])result.toArray(new Server[result.size()]);
    }

    /**
     * Return the clusters in the given clusterName or a zero length list. The 
     * cluster (and each of its instances) must exist or an exception is thrown.
     */
    public static Server[] getServersInCluster(ConfigContext configContext, 
        String clusterName) throws ConfigException
    {
        //first ensure that the cluster exists
        Cluster cluster = ClusterHelper.getClusterByName(configContext, clusterName);
        
        //Now fetch the server instances in the cluster.    
        ServerRef[] serverRefs = cluster.getServerRef();
        
        Server[] result = new Server[serverRefs.length];
        for (int i = 0; i <  serverRefs.length; i++) {
            try {
                result[i] = getServerByName(configContext, serverRefs[i].getRef());
            } catch (ConfigException ex) {
                throw new ConfigException(_strMgr.getString("noSuchClusterInstance", 
                    clusterName, serverRefs[i].getRef()));
            }
        }
        return result;
    }      
    
    /**
     * Returns true if the given server is the Domain Administration Server.         
     * There is a large assumption being made here that the DAS is the only server
     * instance with out a node controller ref.
     *
     * NOTE: This method has unused paramenters (configContext) and an unnecessary
     * ConfigException. This is to support a more complete check in the future 
     * (e.g. checking the to see if the server is the 0th element, or checking the 
     * admin-service of the server's configuration) should we determine that 
     * checking for an empty node controller ref is not sufficient.s
     * 
     **/
    public static boolean isDAS(ConfigContext configContext, Server server)
        throws ConfigException
    {
        if (server.getNodeAgentRef() == null) {            
            return true;
        } 
        return false;        
    }
    
    /**
     * Returns true if the given server is the Domain Administration Server.
     */    
    public static boolean isDAS(ConfigContext configContext, String instanceName)
        throws ConfigException
    {
        final Domain domain = getDomainConfigBean(configContext);          
        final Server server = domain.getServers().getServerByName(instanceName);
        return isDAS(configContext, server);
    }
    
    /**
     * Return all the servers associated with the given node agent or a zero length
     * list. The node agent must exist or an exception is thrown.
     */
    public static Server[] getServersOfANodeAgent(ConfigContext configContext, String agentName) 
        throws ConfigException
    {
        //First ensure that the config exists
        NodeAgent controller = NodeAgentHelper.getNodeAgentByName(configContext, agentName);
        
        //Next fetch all the server instances that reference the node agent
        Server[] servers = getServersInDomain(configContext);
        ArrayList result = new ArrayList();
        for (int i = 0; i < servers.length; i++) {
            if (!isDAS(configContext, servers[i]) && 
                servers[i].getNodeAgentRef().equals(agentName)) 
            {
                result.add(servers[i]);
            }            
        }
        return (Server[])result.toArray(new Server[result.size()]);
    }
    
    /**
     * Return the system jmx connector for the specified server instance. The specified server 
     * instance and its connector must exist or an exception is thrown.
     */
    public static JmxConnector getServerSystemConnector(ConfigContext configContext, String instanceName) 
        throws ConfigException 
    {                
        //Find the admin service element of the config
        final AdminService adminService = getAdminServiceForServer(configContext, instanceName);        
        
        final String systemConnectorName = adminService.getSystemJmxConnectorName();
        final JmxConnector connector = adminService.getJmxConnectorByName(
            systemConnectorName);
        if (connector == null) {
            throw new ConfigException(_strMgr.getString("noInstanceSystemConnector", instanceName, 
                systemConnectorName));
        }
        return connector;
    }      
    
    /**
     * Return mbean server connection info for a server instance (including the DAS). This returns
     * the information necessary to connect to the exposed mbean server's connector.
     */
    public static JMXConnectorConfig getJMXConnectorInfo(ConfigContext configContext, String instanceName) 
        throws ConfigException
    {        
        //Fetch the server instances system jmx connector defined in its configuration.       
        JmxConnector connector = ServerHelper.getServerSystemConnector(
            configContext, instanceName);

        //If the port number is not a number, but instead a property reference of the form
        //${port-number}, we must resolve the property reference manually. The issue is that 
        //this code is running in the DAS, and the port-number system property will not 
        //be defined there. It will only be defined in the server instance.

        //FIXTHIS: This is a HACK since connector.getPort() will return a 
        //port with all the system properties resolved. We need the raw attribute value.
        String portAttribute = connector.getRawAttributeValue(ServerTags.PORT);
        String port = new PropertyResolver(configContext, instanceName).resolve(
            portAttribute);


        //Now set up client connection info
        Server server = ServerHelper.getServerByName(configContext, instanceName);        
        ElementProperty hostProp = null;
        AdminService adminService = ServerHelper.getAdminServiceForServer(
            configContext, instanceName);

        //If we are connecting to the DAS, then use connector as is. If we are connecting 
        //to a remote server instance then we have to obtain its user, password, host 
        //information from its Node Agent.
        if (ServerHelper.isDAS(configContext, server)) { //DAS            
            hostProp = connector.getElementPropertyByName(HOST_PROPERTY_NAME);   
        } else {                       
            //Take the client connection properties from the node agent referenced by the 
            //given server instance. In other words, the host, user, password used to connect to the 
            //server instance should be identical to the host, user, password used to connect to the
            //server's node agent.
            final JmxConnector agentConnector = NodeAgentHelper.getNodeAgentSystemConnector(
                configContext, server.getNodeAgentRef());                            
            hostProp = agentConnector.getElementPropertyByName(HOST_PROPERTY_NAME);                   
        }

        String adminUser = IdentityManager.getUser();
        String adminPassword = IdentityManager.getPassword();
        
        if (adminUser == null || adminPassword == null || hostProp == null) {
            throw new ConfigException(_strMgr.getString("missingInstanceConnectorAuth", 
                instanceName));
        }

        return new JMXConnectorConfig(hostProp.getValue(), port,                 
            adminUser, adminPassword, connector.getProtocol());
    }
     
    /**
     * Fetch the AdminService for the given server instance. The server instance must exist or
     * an exception is thrown. 
     */
    public static AdminService getAdminServiceForServer(ConfigContext configContext, String instanceName) 
        throws ConfigException 
    {
        final Config config = getConfigForServer(configContext, instanceName);
        
        //Find the admin service element of the config
        final AdminService adminService = config.getAdminService();
        if (adminService == null) {
            throw new ConfigException(_strMgr.getString("noAdminService", 
                config.getName(), instanceName));
        }
        return adminService;      
    }     
    
    /**
     * Return the configuration of the given server instance. Both the server instance
     * or its configuration must exist or an exception is thrown.
     */
    public static Config getConfigForServer(ConfigContext configContext, String instanceName) 
        throws ConfigException
    {
        final Server server = getServerByName(configContext, instanceName);        
        return getConfigByName(configContext, server.getConfigRef()); 
    }
    
    /**
     * Return true if the given server instance is part of a cluster.
     */
    public static boolean isServerClustered(ConfigContext configContext, Server server)
        throws ConfigException
    {
        //Return the server only if it is part of a cluster (i.e. only if a cluster
        //has a reference to it).
        final String instanceName = server.getName();
        final Cluster[] clusters = ClusterHelper.getClustersInDomain(configContext);
        for (int i = 0; i < clusters.length; i++) {
            final ServerRef[] servers = clusters[i].getServerRef();
            for (int j = 0; j < servers.length; j++) {
                if (servers[j].getRef().equals(instanceName)) {
                    // check to see if the server exists as a sanity check. 
                    // NOTE: we are not checking for duplicate server instances here.
                    return true;                    
                }
            }
        }
        return false;
    }
    
    /**
     * Return true if the given server instance is part of a cluster.
     */
    public static boolean isServerClustered(ConfigContext configContext, String instanceName)
        throws ConfigException 
    {
        return isServerClustered(configContext, 
            getServerByName(configContext, instanceName)); 
    }
    
    /**
     * Return true if the server is standalone. A standalone server has a configuration
     * named serverName>-config and it configuration is referenced by the server 
     * only. No other clusters or servers may refer to its configuration.
     */
    public static boolean isServerStandAlone(ConfigContext configContext, String instanceName) 
        throws ConfigException
    {
       final Server server = getServerByName(configContext, instanceName);
       final String configName = server.getConfigRef();
       if (isConfigurationNameStandAlone(configName, instanceName)) {
            if (!isServerClustered(configContext, server)) {           
                if (isConfigurationReferencedByServerOnly(configContext, configName, instanceName)) {
                   return true;
               }
           }
       }
       return false;
    }
   
    /**
     * Return true if the given server instance references the stated application.
     */
    public static boolean serverReferencesApplication(ConfigContext configContext, 
        String instanceName, String appName) throws ConfigException
    {
        final Server server = getServerByName(configContext, instanceName);         
        return serverReferencesApplication(server, appName);
    }
    
    public static boolean serverReferencesApplication(Server server, String appName) 
        throws ConfigException
    {
        final ApplicationRef ref = server.getApplicationRefByRef(appName);
        return (ref == null) ? false : true;
    }
    
    /**
     * Return the server instances referencing the given configName or a zero length list.
     */
    public static Server[] getServersReferencingApplication(ConfigContext configContext, String appName) 
        throws ConfigException
    {                
        //Now find all server instances that reference the application
        Server[] servers = getServersInDomain(configContext);
        ArrayList result = new ArrayList();
        for (int i = 0; i < servers.length; i++) {
            if (serverReferencesApplication(servers[i], appName)) {
                result.add(servers[i]);
            }            
        }
        return (Server[])result.toArray(new Server[result.size()]);
    }

    public static boolean serverReferencesJdbcConPool(ConfigContext ctx, 
        String instanceName, String poolName) throws ConfigException
    {
        final Server server = getServerByName(ctx, instanceName);         
        return serverReferencesJdbcConPool(server, poolName);
    }
    
    public static boolean serverReferencesJdbcConPool(Server server,
        String poolName) throws ConfigException
    {
        final ResourceRef ref = server.getResourceRefByRef(poolName);
        return (ref == null) ? false : true;
    }
       
    
    /**
     * Return true if the given server instance references the stated resource.
     */
    public static boolean serverReferencesResource(ConfigContext configContext, 
        String instanceName, String resourceName) throws ConfigException
    {
        final Server server = getServerByName(configContext, instanceName);         
        return serverReferencesResource(server, resourceName);
    }
    
    public static boolean serverReferencesResource(Server server,
        String resourceName) throws ConfigException
    {
        final ResourceRef ref = server.getResourceRefByRef(resourceName);
        return (ref == null) ? false : true;
    }
       
    /**
     * Return the server instances referencing the given configName or a zero length list.
     */
    public static Server[] getServersReferencingResource(ConfigContext configContext, String resName) 
        throws ConfigException
    {                
        //Now find all server instances that reference the application
        Server[] servers = getServersInDomain(configContext);
        ArrayList result = new ArrayList();
        for (int i = 0; i < servers.length; i++) {
            if (serverReferencesResource(servers[i], resName)) {
                result.add(servers[i]);
            }            
        }
        return (Server[])result.toArray(new Server[result.size()]);
    }

    /**
     * Returns all servers referencing the given JDBC pool. There is no 
     * explicit reference from a server to a jdbc pool. This implementation
     * examines the jdbc resource refs to return the associated servers.
     *
     * @param   ctx   config context
     * @param   poolName   name of the jdbc pool
     *
     * @return  servers associated to the pool
     * @throws  ConfigException  if an error while parsing domain.xml
     */
    public static Server[] getServersReferencingJdbcPool(ConfigContext ctx, 
            String poolName) throws ConfigException {

        Server[] servers = getServersInDomain(ctx);
        ArrayList result = new ArrayList();
        for (int i = 0; i < servers.length; i++) {
            if (ResourceHelper.isJdbcPoolReferenced(ctx, poolName,
                                            servers[i].getName())) {
                result.add(servers[i]);
            }            
        }
        return (Server[])result.toArray(new Server[result.size()]);
    }

    /**
     * Returns all servers referencing the given connector connection pool. 
     * There is no explicit reference from a server to a connector connection 
     * pool. This implementation examines the connector resource references 
     * to return the associated servers.
     *
     * @param   ctx   config context
     * @param   poolName   name of the connector connection pool
     *
     * @return  servers associated to the connector connection pool
     *
     * @throws  ConfigException  if an error while parsing domain.xml
     */
    public static Server[] getServersReferencingConnectorPool(ConfigContext ctx,
            String poolName) throws ConfigException {

        Server[] servers = getServersInDomain(ctx);
        ArrayList result = new ArrayList();
        for (int i = 0; i < servers.length; i++) {
            if (ResourceHelper.isConnectorPoolReferenced(ctx, poolName,
                                            servers[i].getName())) {
                result.add(servers[i]);
            }            
        }
        return (Server[])result.toArray(new Server[result.size()]);
    }
    
    /**
     * Return all the application refs of the server
     */
    public static ApplicationRef[] getApplicationReferences(ConfigContext configContext, 
        String serverName) throws ConfigException
    {
        final Server server = getServerByName(configContext, serverName);
        if (server.getApplicationRef() == null) {
            return new ApplicationRef[0];
        } else {
            return server.getApplicationRef();
        }
    }
    
    /**
     * Return all the resource refs of the server
     */    
    public static ResourceRef[] getResourceReferences(ConfigContext configContext, 
        String serverName) throws ConfigException
    {
        final Server server = getServerByName(configContext, serverName);
        if (server.getResourceRef() == null) {
            return new ResourceRef[0];
        } else {
            return server.getResourceRef();
        }
    }    

    /**
     * Returns all the associated J2EE Applications bean for this
     * given sever.
     */
    public static J2eeApplication[] getAssociatedJ2eeApplications(
            ConfigContext ctx, String serverName) throws ConfigException {

        ArrayList list = new ArrayList();
        Domain domain = (Domain) ctx.getRootConfigBean();
        Applications applications = domain.getApplications();

        Servers servers = domain.getServers();
        Server s = servers.getServerByName(serverName);
        if (s != null) {
            J2eeApplication[] j2eeApps = applications.getJ2eeApplication();
            for (int i=0; i < j2eeApps.length; i++) {
                if (serverReferencesApplication(ctx, serverName, 
                                                j2eeApps[i].getName()) ) {
                    list.add(j2eeApps[i]);
                }
            }
        }

        J2eeApplication[] associatedApps = new J2eeApplication[list.size()];
        return ((J2eeApplication[]) list.toArray(associatedApps));
    }

    public static J2eeApplication[] getUnAssociatedJ2eeApplications(
            ConfigContext ctx, String serverName) throws ConfigException {

        ArrayList list = new ArrayList();
        Domain domain = (Domain) ctx.getRootConfigBean();
        Applications applications = domain.getApplications();

        Servers servers = domain.getServers();
        Server s = servers.getServerByName(serverName);
        if (s != null) {
            J2eeApplication[] j2eeApps = applications.getJ2eeApplication();
            for (int i=0; i < j2eeApps.length; i++) {
                if (!serverReferencesApplication(ctx, serverName, 
                                                j2eeApps[i].getName()) ) {
                    list.add(j2eeApps[i]);
                }
            }
        }

        J2eeApplication[] unassociatedApps = new J2eeApplication[list.size()];
        return ((J2eeApplication[]) list.toArray(unassociatedApps));
    }

    /**
     * Returns all the associated EJB modules bean for this
     * given sever.
     */
    public static EjbModule[] getAssociatedEjbModules(
            ConfigContext ctx, String serverName) throws ConfigException {

        ArrayList list = new ArrayList();
        Domain domain = (Domain) ctx.getRootConfigBean();
        Applications applications = domain.getApplications();

        Servers servers = domain.getServers();
        Server s = servers.getServerByName(serverName);
        if (s != null) {
            EjbModule[] ejbMods = applications.getEjbModule();
            for (int i=0; i < ejbMods.length; i++) {
                if (serverReferencesApplication(ctx, serverName, 
                                                ejbMods[i].getName()) ) {
                    list.add(ejbMods[i]);
                }
            }
        }

        EjbModule[] associatedApps = new EjbModule[list.size()];
        return ((EjbModule[]) list.toArray(associatedApps));
    }

    public static EjbModule[] getUnAssociatedEjbModules(
            ConfigContext ctx, String serverName) throws ConfigException {

        ArrayList list = new ArrayList();
        Domain domain = (Domain) ctx.getRootConfigBean();
        Applications applications = domain.getApplications();

        Servers servers = domain.getServers();
        Server s = servers.getServerByName(serverName);
        if (s != null) {
            EjbModule[] ejbMods = applications.getEjbModule();
            for (int i=0; i < ejbMods.length; i++) {
                if (!serverReferencesApplication(ctx, serverName, 
                                                ejbMods[i].getName()) ) {
                    list.add(ejbMods[i]);
                }
            }
        }

        EjbModule[] unassociatedApps = new EjbModule[list.size()];
        return ((EjbModule[]) list.toArray(unassociatedApps));
    }

    /**
     * Returns all the associated web modules bean for this
     * given sever.
     */
    public static WebModule[] getAssociatedWebModules(
            ConfigContext ctx, String serverName) throws ConfigException {

        ArrayList list = new ArrayList();
        Domain domain = (Domain) ctx.getRootConfigBean();
        Applications applications = domain.getApplications();

        Servers servers = domain.getServers();
        Server s = servers.getServerByName(serverName);
        if (s != null) {
            WebModule[] webMods = applications.getWebModule();
            for (int i=0; i < webMods.length; i++) {
                if (serverReferencesApplication(ctx, serverName, 
                                                webMods[i].getName()) ) {
                    list.add(webMods[i]);
                }
            }
        }

        WebModule[] associatedApps = new WebModule[list.size()];
        return ((WebModule[]) list.toArray(associatedApps));
    }

    public static WebModule[] getUnAssociatedWebModules(
            ConfigContext ctx, String serverName) throws ConfigException {

        ArrayList list = new ArrayList();
        Domain domain = (Domain) ctx.getRootConfigBean();
        Applications applications = domain.getApplications();

        Servers servers = domain.getServers();
        Server s = servers.getServerByName(serverName);
        if (s != null) {
            WebModule[] webMods = applications.getWebModule();
            for (int i=0; i < webMods.length; i++) {
                if (!serverReferencesApplication(ctx, serverName, 
                                                webMods[i].getName()) ) {
                    list.add(webMods[i]);
                }
            }
        }

        WebModule[] unassociatedApps = new WebModule[list.size()];
        return ((WebModule[]) list.toArray(unassociatedApps));
    }

    public static ConnectorModule[] getUnAssociatedConnectorModules(
            ConfigContext ctx, String serverName) throws ConfigException {

        ArrayList list = new ArrayList();
        Domain domain = (Domain) ctx.getRootConfigBean();
        Applications applications = domain.getApplications();

        Servers servers = domain.getServers();
        Server s = servers.getServerByName(serverName);
        if (s != null) {
            ConnectorModule[] connMods = applications.getConnectorModule();
            for (int i=0; i < connMods.length; i++) {
                if (!serverReferencesApplication(ctx, serverName, 
                                                connMods[i].getName()) ) {
                    list.add(connMods[i]);
                }
            }
        }

        ConnectorModule[] unassociatedApps = new ConnectorModule[list.size()];
        return ((ConnectorModule[]) list.toArray(unassociatedApps));
    }

    /**
     * Returns all the associated connector modules bean for this
     * given sever.
     */
    public static ConnectorModule[] getAssociatedConnectorModules(
            ConfigContext ctx, String serverName) throws ConfigException {

        ArrayList list = new ArrayList();
        Domain domain = (Domain) ctx.getRootConfigBean();
        Applications applications = domain.getApplications();

        Servers servers = domain.getServers();
        Server s = servers.getServerByName(serverName);
        if (s != null) {
            ConnectorModule[] connMods = applications.getConnectorModule();
            for (int i=0; i < connMods.length; i++) {
                if (serverReferencesApplication(ctx, serverName, 
                                                connMods[i].getName()) ) {
                    list.add(connMods[i]);
                }
            }
        }

        ConnectorModule[] associatedApps = new ConnectorModule[list.size()];
        return ((ConnectorModule[]) list.toArray(associatedApps));
    }

    public static LifecycleModule[] getUnAssociatedLifecycleModules(
            ConfigContext ctx, String serverName) throws ConfigException {

        ArrayList list = new ArrayList();
        Domain domain = (Domain) ctx.getRootConfigBean();
        Applications applications = domain.getApplications();

        Servers servers = domain.getServers();
        Server s = servers.getServerByName(serverName);
        if (s != null) {
            LifecycleModule[] lcMods = applications.getLifecycleModule();
            for (int i=0; i < lcMods.length; i++) {
                if (!serverReferencesApplication(ctx, serverName, 
                                                lcMods[i].getName()) ) {
                    list.add(lcMods[i]);
                }
            }
        }

        LifecycleModule[] unassociatedApps = new LifecycleModule[list.size()];
        return ((LifecycleModule[]) list.toArray(unassociatedApps));
    }

    /**
     * Returns all the associated Lifecycle modules bean for this
     * given sever.
     */
    public static LifecycleModule[] getAssociatedLifecycleModules(
            ConfigContext ctx, String serverName) throws ConfigException {

        ArrayList list = new ArrayList();
        Domain domain = (Domain) ctx.getRootConfigBean();
        Applications applications = domain.getApplications();

        Servers servers = domain.getServers();
        Server s = servers.getServerByName(serverName);
        if (s != null) {
            LifecycleModule[] lcMods = applications.getLifecycleModule();
            for (int i=0; i < lcMods.length; i++) {
                if (serverReferencesApplication(ctx, serverName, 
                                                lcMods[i].getName()) ) {
                    list.add(lcMods[i]);
                }
            }
        }

        LifecycleModule[] associatedApps = new LifecycleModule[list.size()];
        return ((LifecycleModule[]) list.toArray(associatedApps));
    }

    public static AppclientModule[] getUnAssociatedAppclientModules(
            ConfigContext ctx, String serverName) throws ConfigException {

        ArrayList list = new ArrayList();
        Domain domain = (Domain) ctx.getRootConfigBean();
        Applications applications = domain.getApplications();

        Servers servers = domain.getServers();
        Server s = servers.getServerByName(serverName);
        if (s != null) {
            AppclientModule[] appclntMods = applications.getAppclientModule();
            for (int i=0; i < appclntMods.length; i++) {
                if (!serverReferencesApplication(ctx, serverName, 
                                                appclntMods[i].getName()) ) {
                    list.add(appclntMods[i]);
                }
            }
        }

        AppclientModule[] unassociatedApps = new AppclientModule[list.size()];
        return ((AppclientModule[]) list.toArray(unassociatedApps));
    }

    /**
     * Returns all the associated app client modules bean for this
     * given sever.
     */
    public static AppclientModule[] getAssociatedAppclientModules(
            ConfigContext ctx, String serverName) throws ConfigException {

        ArrayList list = new ArrayList();
        Domain domain = (Domain) ctx.getRootConfigBean();
        Applications applications = domain.getApplications();

        Servers servers = domain.getServers();
        Server s = servers.getServerByName(serverName);
        if (s != null) {
            AppclientModule[] appclntMods = applications.getAppclientModule();
            for (int i=0; i < appclntMods.length; i++) {
                if (serverReferencesApplication(ctx, serverName, 
                                                appclntMods[i].getName()) ) {
                    list.add(appclntMods[i]);
                }
            }
        }

        AppclientModule[] associatedApps = new AppclientModule[list.size()];
        return ((AppclientModule[]) list.toArray(associatedApps));
    }

    /**
     * Returns the administrative.domain.name property value defined 
     * under domain element. administrative.domain.name is 
     * used to determine the jmx mbean server name for the AMX 
     * mbeans. 
     *
     * @param   ctx   config contex 
     * @param   serverName   name of the server instance
     *
     * @return  administrative.domain.name property value or null if not defined
     *
     * @throws  ConfigException  if an error while parsing domain configuration
     */
    public static String getAdministrativeDomainName(ConfigContext ctx, 
            String serverName) throws ConfigException {

        // name of administrative domain name property
        final String ADMIN_DOMAIN_PROP = "administrative.domain.name";

        // administrative domain name property value
        String aDomainName = null;

        final Domain domain = getDomainConfigBean(ctx);          

        if (domain != null) {
            // administrative domain name
            ElementProperty aDomainNameProp = 
                domain.getElementPropertyByName(ADMIN_DOMAIN_PROP);
        
            if (aDomainNameProp != null) {
                aDomainName = aDomainNameProp.getValue();
            }
        }
        
        return aDomainName;
    }

    /**
     * Get MBeanServerConnection for the given
     * configContext and serverName
     */
    public static MBeanServerConnection connect(
	ConfigContext configContext, String instanceName) 
	throws ConfigException, IOException {

	// fix me
	// move this code to a common place which is being worked on by Kedar

	// get jmx connector
	JMXConnector conn = getJMXConnector(configContext, instanceName);

	return conn.getMBeanServerConnection();
    }


    /**
     * Get JMX Connector for the given
     * configContext and serverName
     */
    public static JMXConnector getJMXConnector(
	ConfigContext configContext, String instanceName) 
	throws ConfigException, IOException {

	// fix me
	// move this code to a common place which is being worked on by Kedar

	// connection info
	JMXConnectorConfig jcc = getJMXConnectorInfo(configContext, instanceName);

	// url
	JMXServiceURL url = null;
	/* Currently this is totally hacked code. - May 2004.
	   This should clearly use a pool of connectors.
	   The pool of connectors should be created early in the life cycle.
	*/
	try {
		url = JmxServiceUrlFactory.forRmiWithJndiInAppserver
		(jcc.getHost(), Integer.parseInt(jcc.getPort()));
	}
	catch(final Exception e) {
		throw new RuntimeException(e);
	}

	final Map env = new HashMap();
	// add user and password based on Abhijit's patch
	env.put(JMXConnector.CREDENTIALS, new String[] {jcc.getUser(), jcc.getPassword()});
	// connection
	JMXConnector conn = JMXConnectorFactory.connect(url, env);

	return conn;
    }

    public static HttpListener getHttpListener(final ConfigContext cc, final String sn, final String ln) throws ConfigException {
        final Config cfg           = getConfigForServer(cc, sn);
        final HttpService hs       = cfg.getHttpService();
        final HttpListener hl      = hs.getHttpListenerById(ln);
        return ( hl );
    }
    
    public static HttpListener[] getHttpListeners(final ConfigContext cc, final String sn) throws ConfigException {
        final Config cfg           = getConfigForServer(cc, sn);
        final HttpService hs       = cfg.getHttpService();
        return ( hs.getHttpListener() );
    }
    public static String getUrlString(final HttpListener ls) {
        String url = ls.isSecurityEnabled() ? "https://" : "http://";
        final String address = SystemPropertyConstants.DEFAULT_SERVER_SOCKET_ADDRESS.equals(ls.getAddress()) ? "localhost" : ls.getAddress();
        url = url + address + ":" + ls.getPort();
        return ( url );
    }
    
    public static JMXServiceURL getJmxServiceUrl(final JmxConnector conn) {
        final String sport = conn.getPort();
        int port;
        //the protocol is assumed here.
        String local = null;
        try {
              port = Integer.parseInt(sport);
              local = InetAddress.getLocalHost().getHostName(); //UHE should have been a RuntimeException
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        final String address = SystemPropertyConstants.DEFAULT_SERVER_SOCKET_ADDRESS.equals(conn.getAddress()) ? local : conn.getAddress();
        final JMXServiceURL url = JmxServiceUrlFactory.forJconsoleOverRmiWithJndiInAppserver(address, port);
        return ( url );
    }

    /** Returns all the server instances (clustered or not) in the domain excluding
     * the DAS itself. Thus if in a domain without servers in it, it guarantees to return
     * an empty array.
     * @return Array of Server objects
     */
     public static Server[] getServersInDomainExcludingDAS(final ConfigContext cc)
     throws ConfigException {
         final Server[] as = getServersInDomain(cc);
         final List list = new ArrayList();
         for(int i = 0 ; i < as.length ; i++) {
             if (as[i] != null) 
                 if (!isDAS(cc, as[i])) list.add(as[i]);
         }
         final Server[] nodas = new Server[list.size()];
         return ( (Server[]) list.toArray(nodas) );
     }
    /** Investigates if the given ${link Server} has a JVM
        Option that determines if runtime uses NSS. Note that
        this is the best case effort because of how the config
        is designed. The idea is to get all the JVM options
        and look for presence of NSS related property and
        absence of JKS related property.
        @param cc ConfigContext represeting the domain.xml
        @param sn String representing the name of the server
        @see con.sun.enterprise.util.SystemPropertyConstants
        @throws NullPointerException if any parameter is null
        @throws ConfigException if there is any error in config processing
    */
    public static boolean serverUsesNss(final ConfigContext cc,
        final String sn) throws ConfigException {
        final Config c = getConfigForServer(cc, sn);
        final String[] opts = c.getJavaConfig().getJvmOptions();
        boolean nss = false, jks = false;
        for (final String opt : opts) {
            if (opt.trim().indexOf(SystemPropertyConstants.NSS_DB_PROPERTY) != -1) {
               nss = true;
            }
            if (opt.trim().indexOf(SystemPropertyConstants.KEYSTORE_PROPERTY) != -1) {
               jks = true;
            }
        }
        return (nss && !jks);
    }
    /** Investigates if the given ${link Server} has a JVM
        Option that determines if DAS runtime is capable of
        handling clusters.
        @param cc ConfigContext represeting the domain.xml
        @see con.sun.enterprise.util.SystemPropertyConstants
        @throws NullPointerException if any parameter is null
        @throws ConfigException if there is any error in config processing
    */
    public static boolean isClusterAdminSupported(final ConfigContext cc) 
        throws ConfigException {
        final String dn = getDAS(cc).getName();
        final Config c = getConfigForServer(cc, dn);
        final String[] opts = c.getJavaConfig().getJvmOptions();
        boolean cas = false;
        for (final String opt : opts) {
            if (opt.trim().indexOf(SystemPropertyConstants.CLUSTER_AWARE_FEATURE_FACTORY_CLASS) != -1) {
               cas = true;
               break;
            }
        }
        return (cas);
    }

    /**
     * Returns the certificate nick name. 
     *
     * @param   ctx          config context
     * @param   serverName   sever instance name
     * 
     * @return  certificate nick name
     * @throws  ConfigException  if system jmx connector is not defined
     */
    public static String getCertNickname(ConfigContext ctx, String serverName) 
        throws ConfigException {

        JmxConnector con = ServerHelper.getServerSystemConnector(ctx, serverName);
        if (con != null) {
            Ssl ssl = con.getSsl();
            if (ssl != null) return ssl.getCertNickname();
        }
        return DEFAULT_CERT_NICKNAME;
    }
}
