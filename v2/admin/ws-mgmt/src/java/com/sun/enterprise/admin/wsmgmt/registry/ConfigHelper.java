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
 * ConfigHelper.java
 * @author Harpreet Singh
 * Created on June 9, 2005, 11:04 AM
 */

package com.sun.enterprise.admin.wsmgmt.registry;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;

import com.sun.logging.LogDomains;
import com.sun.appserv.management.ext.wsmgmt.WebServiceMgr;
import com.sun.appserv.management.ext.wsmgmt.WebServiceEndpointInfo;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.appserv.management.client.ProxyFactory;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.DomainRoot;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.config.WebServiceEndpointConfig;
import com.sun.appserv.management.config.J2EEApplicationConfig;
import com.sun.appserv.management.config.EJBModuleConfig;
import com.sun.appserv.management.config.WebModuleConfig;
import com.sun.appserv.management.config.ConnectorResourceConfig;
import com.sun.appserv.management.config.ConnectorConnectionPoolConfig;
import com.sun.appserv.management.config.ResourceAdapterConfig;
import com.sun.appserv.management.config.ResourceRefConfig;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.config.RegistryLocationConfig;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * Utility Class to update the domain xml
 * @todo convert log messages from fine to appropriate levels
 * @author Harpreet Singh
 */
public class ConfigHelper {
    
    private static final Logger _logger =
            Logger.getLogger(LogDomains.ADMIN_LOGGER);
    
    // Added as a suffix to the connector-resource jndiname to generate a pool name
    // e.g. if a connector resource jndi name is eis/foo, the generated pool name
    // will be eis_foo__pool
    private static final String POOL_SUFFIX = "__pool";
    private static final String DEFAULT_JAXR_RAR = "jaxr-ra";
    private static final String ignore_ra = "jmsra";
    /**
     * Property to be set by Resource Adapter to signify it is a UDDI specific Resource Adapter
     */
    public static final String UDDI_PROPERTY = "com.sun.appserv.registry.uddi";
    /**
     * Property to be set by Resource Adapter to signify it is a EBXML specific Resource Adapter
     */
    public static final String EBXML_PROPERTY = "com.sun.appserv.registry.ebxml";
    
    // properties that a appserver specific rar may have.
    public static final String APPSERVER_UDDI = "AppserverUDDI";
    
    public static final String APPSERVER_EBXML = "AppserverEBXML";
    
    /**
     * A RegistryLocation is determined by the type of its connector connection
     * definition.
     * The connector-connection-definition-name is of the type
     * <pre>
     *    javax.xml.registry.ConnectionFactory
     * </pre>
     * to be qualified as a valid RegistryLocation
     */
    public static final String JAXR_REGISTRY_TYPE =
            "javax.xml.registry.ConnectionFactory";
    
    private static final String UDDI_JAXR_REGISTRY_TYPE
            = "com.sun.connector.jaxr.JaxrConnectionFactory";
    
    private static final String LifeCycleManagerURL = "LifeCycleManagerURL";
    private static final String QueryManagerURL = "QueryManagerURL";
    private Map webServiceInfoMap = null;
    private static final String INSTANCE_PORT_MBEAN_NAME =
            "com.sun.appserv:type=http-listener,id=http-listener-1,config=server-config,category=config";
    /*
     * Creates a new instance of ConfigHelper.
     * This is used to create instances that update the domain.xml
     */
    private ConfigHelper(Map webServiceInfo) {
        this.webServiceInfoMap = webServiceInfo;
    }
    
    /**
     * Creates a instance of ConfigHelper that is used to query domain.xml for
     * RegistryLocations
     */
    private ConfigHelper(){
    }
    
    
    /**
     * Creates an Instance of ConfigHelper that is used to update the domain.xml
     * @param Map a map representation of WebServiceInfo object
     * @return ConfigHelper
     */
    public static ConfigHelper getInstanceToUpdateConfig(Map webServiceInfo){
        return new ConfigHelper(webServiceInfo);
    }
    
    public static ConfigHelper getInstanceToDeleteRegistryResources() {
        return new ConfigHelper();
    }
    /**
     * Creates an instance of ConfigHelper that is used to query domain.xml to
     * get to RegistryLocations. RegistryLocations are determined by the
     * JAXR_REGISTRY_TYPE
     */
    public static ConfigHelper getInstanceToQueryRegistryLocations(){
        return new ConfigHelper();
    }
    
    /**
     * Adds the list of RegistryLocations to the domain xml under
     * webservice-endpoint element, sub element registry-location element
     * @param String the name of the webservice
     * @param String[] the list of the registry-location. Registry location
     * is the jndi name of connection pool pointing to the registry
     * @exception com.sun.enterprise.ConfigException if config information for
     * this web service is not found in the domain xml
     */
    void addToConfig(String webServiceName, Map<String, String> published){//String[] registryLocations) {
        if(webServiceInfoMap == null){
            _logger.fine("ConfigHelper.addToConfig : Incorrect webServiceName ");
            return;
        }
        final DomainConfig dc = this.getDomainConfig();
        String appId = (String)webServiceInfoMap.get(
                WebServiceEndpointInfo.APP_ID_KEY);
        
        Boolean isStandAlone = (Boolean)webServiceInfoMap.get(
                WebServiceEndpointInfo.IS_STAND_ALONE_MODULE_KEY);
        
        String appName = appId;
        String pureWebServiceName = dropAppNameFromWebServiceName(webServiceName);
        String underScoredWebServiceName =
                convertHashesToUnderScores(webServiceName);
        
        WebServiceEndpointConfig wsec = null;
        
        if(!isStandAlone) {
            Map <String, J2EEApplicationConfig> map =
                    dc.getJ2EEApplicationConfigMap();
            J2EEApplicationConfig appConfig = map.get(appName);
            if (appConfig == null) {
                // this will never happen as this created by deployment
                _logger.log(Level.FINE, "Could not find an application with " +
                        " name = "+appName);
            } else  {
                Map <String, WebServiceEndpointConfig> wsmap =
                        appConfig.getWebServiceEndpointConfigMap();
                wsec = wsmap.get(pureWebServiceName);
                if (wsec == null){
                    wsec = appConfig.createWebServiceEndpointConfig(
                            pureWebServiceName, null);
                    wsec.setJBIEnabled(false);
                }
                for (String jndiname  : published.keySet()){
                    appConfig.createProperty(jndiname+"__"+
                            underScoredWebServiceName,
                            published.get(jndiname));
                }
            }
        } else {
            String type = (String)webServiceInfoMap.
                    get(WebServiceEndpointInfo.SERVICE_IMPL_TYPE_KEY);
            if(type.equals(WebServiceEndpointInfo.EJB_IMPL)){
                Map <String, EJBModuleConfig> map
                        = dc.getEJBModuleConfigMap();
                EJBModuleConfig ejbConfig = map.get(appName);
                if (ejbConfig == null) {
                    // should never happen
                    _logger.log(Level.FINE, "Could not find a ejb module" +
                            " with  name = "+appName);
                } else{
                    Map <String, WebServiceEndpointConfig> ejbmap =
                            ejbConfig.getWebServiceEndpointConfigMap();
                    wsec = ejbmap.get(pureWebServiceName);
                    if (wsec == null){
                        wsec = ejbConfig.createWebServiceEndpointConfig(
                                pureWebServiceName, null);
                        wsec.setJBIEnabled(false);
                    }
                    for (String jndiname  : published.keySet()){
                        ejbConfig.createProperty(jndiname+"__"+
                                underScoredWebServiceName,
                                published.get(jndiname));
                    }
                }
            } else if(type.equals(WebServiceEndpointInfo.SERVLET_IMPL)){
                Map <String, WebModuleConfig> map =
                        dc.getWebModuleConfigMap();
                WebModuleConfig webConfig = map.get(appName);
                if (webConfig == null){
                    // should never happen
                    _logger.log(Level.FINE, "Could not find a web module" +
                            " with  name = "+appName);
                } else{
                    Map <String, WebServiceEndpointConfig> webmap =
                            webConfig.getWebServiceEndpointConfigMap();
                    wsec = webmap.get(pureWebServiceName);
                    if (wsec == null){
                        wsec = webConfig.createWebServiceEndpointConfig(
                                pureWebServiceName, null);
                        wsec.setJBIEnabled(false);
                    }
                    for (String jndiname  : published.keySet()){
                        webConfig.createProperty(jndiname+"__"+
                                underScoredWebServiceName,
                                published.get(jndiname));
                    }
                }
            }
        }
        if(wsec != null){
            String[] registryLocations = new String[published.size()];
            registryLocations = published.keySet().toArray(registryLocations);
            populateWebServiceEndpointConfig(wsec, registryLocations);
        }
        return;
    }
    
    void populateWebServiceEndpointConfig(WebServiceEndpointConfig wsec,
            String[] registryLocations){
        for (String jndi : registryLocations){
            wsec.createRegistryLocationConfig(jndi);
        }
    }
    /**
     * Deletes the mapping of the RegistryLocation under the
     * web-service-endpoint element. This is called when the user has
     * unpublished a web service from the registries.
     * @param String name of the web service
     * @param String[] the list of the RegistryLocations. RegistryLocation is
     * the jndi name of the connection pool pointing to the registry.
     * @exception com.sun.enterprise.ConfigException if config information for
     * this web service is not found in the domain xml
     */
    void deleteFromConfig(String webServiceName, String[] registryLocations) {
        if(webServiceInfoMap == null){
            _logger.fine("ConfigHelper.deletFromConfig : Incorrect " +
                    "webServiceName. Exiting! ");
            return;
        }
        final DomainConfig dc = getDomainConfig();
        String appId = (String)webServiceInfoMap.get(
                WebServiceEndpointInfo.APP_ID_KEY);
        
        Boolean isStandAlone = (Boolean)webServiceInfoMap.get(
                WebServiceEndpointInfo.IS_STAND_ALONE_MODULE_KEY);
        
        String pureWebServiceName = dropAppNameFromWebServiceName(webServiceName);
        String underScoredWebServiceName =
                convertHashesToUnderScores(webServiceName);
        
        WebServiceEndpointConfig wsec = null;
        if (!isStandAlone) {
            Map<String, J2EEApplicationConfig> appMap =
                    dc.getJ2EEApplicationConfigMap();
            J2EEApplicationConfig config = appMap.get(appId);
            Map<String, WebServiceEndpointConfig> wsecMap =
                    config.getWebServiceEndpointConfigMap();
            wsec = wsecMap.get(pureWebServiceName);
            for (int i=0; i<registryLocations.length; i++){
                String jndiname = registryLocations[i];
                config.removeProperty(jndiname+"__"+
                        underScoredWebServiceName);
            }
        } else {
            String type = (String)webServiceInfoMap.get(
                    WebServiceEndpointInfo.SERVICE_IMPL_TYPE_KEY);
            if (type.equals(WebServiceEndpointInfo.EJB_IMPL)){
                Map<String, EJBModuleConfig> ejbMap =
                        dc.getEJBModuleConfigMap();
                EJBModuleConfig config = ejbMap.get(appId);
                Map<String, WebServiceEndpointConfig> wsecMap =
                        config.getWebServiceEndpointConfigMap();
                wsec = wsecMap.get(pureWebServiceName);
                for (int i=0; i<registryLocations.length; i++){
                    String jndiname = registryLocations[i];
                    config.removeProperty(jndiname+"__"+
                            underScoredWebServiceName);
                }
                
            } else if (type.equals(WebServiceEndpointInfo.SERVLET_IMPL)){
                Map<String, WebModuleConfig> webMap =
                        dc.getWebModuleConfigMap();
                WebModuleConfig config = webMap.get(appId);
                Map<String, WebServiceEndpointConfig> wsecMap =
                        config.getWebServiceEndpointConfigMap();
                wsec = wsecMap.get(pureWebServiceName);
                for (int i=0; i<registryLocations.length; i++){
                    String jndiname = registryLocations[i];
                    config.removeProperty(jndiname+"__"+
                            underScoredWebServiceName);
                }
                
            }
        }
        if (wsec != null){
            for (String jndiName : registryLocations )
                wsec.removeRegistryLocationConfig(jndiName);
        }
    }
    
    private String dropAppNameFromWebServiceName(String webServiceName){
        String[] split = webServiceName.split("#");
        StringBuffer buf = new StringBuffer();
        for (int i=1; i<split.length; i++){
            buf.append(split[i]);
            if(i<(split.length-1)){
                buf.append("#");
            }
        }
        return buf.toString();
    }
    
    /**
     * List the RegistryLocations. A registry location is the jndi name of a
     * connection pool that points to a registry determined by the
     * connector connection definition of the type JAXR_REGISTRY_TYPE
     * @return String[] list of registry-location
     * @exception com.sun.enterprise.ConfigException if config information for
     * this web service is not found in the domain xml
     */
    String[] listRegistryLocations(){
        final DomainConfig dc = this.getDomainConfig();
        List<String> jndinames = new ArrayList<String>();
        /**
         * Get the names of resource-adapters for connection-pool of the type
         * JAXR_REGISTRY_TYPE. Peek into the ResourceAdapterConfig to get the
         * jndi name.
         */
        Map<String, ConnectorConnectionPoolConfig> ccpcMap =
                dc.getConnectorConnectionPoolConfigMap();
        
        Map<String, ConnectorResourceConfig> crcMap =
                dc.getConnectorResourceConfigMap();
        
        for (String poolName : ccpcMap.keySet() ){
            ConnectorConnectionPoolConfig ccpc = ccpcMap.get(poolName);
            String connectionDefnName = ccpc.getConnectionDefinitionName();
            if (JAXR_REGISTRY_TYPE.equals(connectionDefnName) ||
                    UDDI_JAXR_REGISTRY_TYPE.equals(connectionDefnName)){
                
                for (String resourceName:crcMap.keySet()){
                    ConnectorResourceConfig crc = crcMap.get(resourceName);
                    if (poolName.equals(crc.getPoolName()))
                        jndinames.add(crc.getJNDIName());
                }
            }
        }
        String[] retValue = new String[jndinames.size()];
        retValue = jndinames.toArray(retValue);
        return retValue;
    }
    
    /**
     * Removes the registry specific resources  from the domain.
     * Peeks at the connector-resource element to obtain the
     * connector-connection-pool name. Using this pool name, removes the
     * connector-connection-pool, proceeds further to remove the
     * connector-resource
     * @param jndiNameOfRegistry whose resources are to be removed from the domain
     */
    public void removeRegistryConnectionResources(String jndiNameOfRegistry) {
        final DomainConfig dc = getDomainConfig();
        Map<String, ConnectorResourceConfig> crcMap =
                dc.getConnectorResourceConfigMap();
        ConnectorResourceConfig crc = crcMap.get(jndiNameOfRegistry);
        String poolName = (crc != null)? crc.getPoolName(): null;
        dc.removeConnectorResourceConfig(jndiNameOfRegistry);
        if (poolName != null){
            dc.removeConnectorConnectionPoolConfig(poolName);
        }
    }
    
    /**
     * Adds registry specific resources to the domain.
     * Adds a connector connection pool and then proceeds to add a connector
     * resource
     *
     * @param jndiName of the connector-resource that points to the registry
     *
     * @param description of the connector-resource and the connector-connection
     * -pool name
     *
     * @param type type of the registry
     * {@link com.sun.appserv.management.WebServiceMgr#UDDI_KEY}
     * {@link com.sun.appserv.management.WebServiceMgr#EBXML_KEY}
     *
     * @param properties a map of key, value pair that encapsulate the properties
     * of the connection pool that connects to the registry.  Properties are
     *
     * {@link com.sun.appserv.management.WebServiceMgr#PUBLISH_URL_KEY}
     * {@link com.sun.appserv.management.WebServiceMgr#QUERY_URL_KEY}
     * {@link com.sun.appserv.management.WebServiceMgr#USERNAME_KEY}
     * {@link com.sun.appserv.management.WebServiceMgr#PASSWORD_KEY}
     */
    public void addRegistryConnectionResources(String jndiName,
            String description, String type, Map<String, String> properties){
        
        final DomainConfig dc = getDomainConfig();
        String registryType = null;
        if (type == WebServiceMgr.UDDI_KEY) {
            registryType = this.UDDI_PROPERTY;
        } else if (type == WebServiceMgr.EBXML_KEY ){
            registryType = this.EBXML_PROPERTY;
        }
        if (registryType == null) {
            Object[] params = {UDDI_PROPERTY, EBXML_PROPERTY};
            _logger.log(Level.WARNING, "registry.specify_registry_type", params);
            throw new RuntimeException("Registry Type has to be "+
                    UDDI_PROPERTY + " or "+ EBXML_PROPERTY);
        }
        // 1. get resource adapter config
        Map <String, ResourceAdapterConfig> raConfMap = dc.getResourceAdapterConfigMap();
        // 2. get the resourceAdapterName
        String resourceAdapterName = null;
        for (String ra : raConfMap.keySet()){
            ResourceAdapterConfig rac = raConfMap.get(ra);
            if (rac.existsProperty(registryType)){
                resourceAdapterName = rac.getResourceAdapterName();
                break;
            }
        }
        if (resourceAdapterName == null){
            // Now look in the system resource adapter.
            resourceAdapterName = getSystemConnectorResources(registryType);
            
            if (resourceAdapterName == null){
                // could not find any resource adapter that is a jaxr resource adapter type
                // log it
                String msg =
                        "Cannot locate JAXR Resource Adapter to add Connection pool" +
                        " and Connector Resource. Please add connector resource " +
                        "of type " + registryType;
                _logger.log(Level.SEVERE,
                        "registry.deploy_registry_connector_resource", registryType);
                throw new RuntimeException(msg);
            }
        }
        
        // 3. generate a unique pool name
        String poolName = FileUtils.makeFriendlyFileName(jndiName) + POOL_SUFFIX;
        // 4. connectorDefinitionName
        String connectorDefinitionName = JAXR_REGISTRY_TYPE;
        if (registryType.equals(UDDI_PROPERTY)){
            // jaxr-ri works only with a "/" appended to the URL
            Properties props = new Properties();
            for (String property : properties.keySet()){
                String propValue = (String)properties.get(property);
                if (LifeCycleManagerURL.equals(property) ||
                        QueryManagerURL.equals(property)){
                    //jaxr-ri needs a backslash at the end of the URL
                    if (!propValue.endsWith("/")) {
                        propValue = propValue + "/";
                    }
                    props.put(property, propValue);
                } else if (property.equalsIgnoreCase("username")){
                    props.put("UserName", propValue);
                } else if (property.equalsIgnoreCase("password")){
                    props.put("UserPassword", propValue);
                } else
                    props.put(property, propValue);
            }
            
            connectorDefinitionName = UDDI_JAXR_REGISTRY_TYPE;
            createConnectorConnectionPoolMBean(resourceAdapterName,
                    connectorDefinitionName, poolName, props);
        } else {
            Map<String, String> optional = new HashMap <String, String> ();
            for (String property : properties.keySet()){
                optional.put(PropertiesAccess.PROPERTY_PREFIX + property,
                        properties.get(property));
            }
            
            dc.createConnectorConnectionPoolConfig( poolName,
                    resourceAdapterName, connectorDefinitionName, optional );
        }
        dc.createConnectorResourceConfig(jndiName, poolName, null);
    }
    
    private void createConnectorConnectionPoolMBean
            (final String resourceAdapterName,
            final String connectorDefinitionName,
            final String poolName, final Properties props) {
        try{
            // system rars cannot used via AMX :-(
            final MBeanServer server = MBeanServerFactory.getMBeanServer();
            ObjectName obj = new ObjectName
                    ("com.sun.appserv:type=resources,category=config");
            AttributeList attrlist = new AttributeList();
            attrlist.add(new Attribute("name", poolName));
            attrlist.add(new Attribute("resource-adapter-name", resourceAdapterName));
            attrlist.add(new Attribute("connection-definition-name", connectorDefinitionName));
            Object[] params = new Object[3];
            params[0] = attrlist;
            params[1] = props;
            params[2] = "server";
            String[] signature = new String[3];
            signature[0] = "javax.management.AttributeList";
            signature[1] = "java.util.Properties";
            signature[2] = "java.lang.String";
            server.invoke(obj, "createConnectorConnectionPool",
                    params, signature);
        } catch (Exception e) {
            _logger.log(Level.WARNING, "cannot create connection pool for " +
                    "resource adapter ="+resourceAdapterName, e);
        }
    }
    // This is not entirely baked as admin backend does not provide a clean
    // way to handle system resource adapters. Look @ bug id: 6364653
    // Commenting this out for now.
    private String getSystemConnectorResources(String type){
        String[] systemResources = null;
        
        final MBeanServer server = MBeanServerFactory.getMBeanServer();
        try {
            ObjectName objName =
                    new ObjectName("com.sun.appserv:type=resources,category=config");
            systemResources = (String[])server.invoke( objName,
                    "getSystemConnectorsAllowingPoolCreation", null, null);
            for (int i =0; i< systemResources.length; i++){
                if (ignore_ra.equals(systemResources[i]))// dont look at jms ra
                    continue;
                
                AttributeList attrList = new AttributeList();
                attrList.add(new Attribute("resource-adapter-name",
                        systemResources[i]));
                if (UDDI_PROPERTY.equals(type))
                    attrList.add(new Attribute("connection-definition-name",
                            UDDI_JAXR_REGISTRY_TYPE));
                else
                    attrList.add(new Attribute("connection-definition-name",
                            JAXR_REGISTRY_TYPE));
                
                Object[] params = new Object[]{attrList};
                String[] types = new String[]{"javax.management.AttributeList"};
                Properties properties =
                        (Properties)server.invoke(new ObjectName
                        ("com.sun.appserv:type=resources,category=config"),
                        "getMCFConfigProps",
                        params, types );
                
                Enumeration propertyNames = properties.propertyNames();
                while(propertyNames.hasMoreElements()){
                    String name = (String)propertyNames.nextElement();
                    if (UDDI_PROPERTY.equals(type)){
                        if (name.equals(APPSERVER_UDDI))
                            return systemResources[i];
                    } else if (EBXML_PROPERTY.equals(type)) { // we do not have this yet.
                        // Kept incase soar-rar goes as a system adapter in the
                        // futurre
                        if (name.equals(APPSERVER_EBXML))
                            return systemResources[i];
                    }
                }
            }
        } catch (Exception e) {
            _logger.log(Level.FINE, "Cannot locate system connector resources");
        }
        
        return null;
    }
    private DomainConfig getDomainConfig(){
        final MBeanServer server = MBeanServerFactory.getMBeanServer();
        final DomainRoot domainRoot =
                ProxyFactory.getInstance(server).getDomainRoot();
        final DomainConfig  domainConfig = domainRoot.getDomainConfig();
        return domainConfig;
    }
    
    public String getInstancePort(){
        final MBeanServer server = MBeanServerFactory.getMBeanServer();
        String port = null;
        try {
            
            port = (String)server.getAttribute(
                    new javax.management.ObjectName(INSTANCE_PORT_MBEAN_NAME), "port");
        } catch (MalformedObjectNameException ex) {
            _logger.log(Level.WARNING, "Incorrect Object name to extract port from" +
                    " domain xml", ex);
        } catch (javax.management.MBeanException mbe){
            _logger.log(Level.WARNING, "MBeanServer returned an exception while" +
                    "being queried for port on http-listener ", mbe);
        } catch (javax.management.AttributeNotFoundException anfe){
            _logger.log(Level.WARNING, "Cannot find attribute on MBean "+
                    INSTANCE_PORT_MBEAN_NAME, anfe);
        } catch (javax.management.InstanceNotFoundException infe){
            _logger.log(Level.WARNING, " Cannot find MBean instance "+
                    INSTANCE_PORT_MBEAN_NAME, infe);
        } catch (javax.management.ReflectionException re){
            _logger.log(Level.WARNING, " Cannot invoke MBean instance "+
                    INSTANCE_PORT_MBEAN_NAME, re);
        }
        return port;
    }
    
    
    public String[] listAlreadyPublishedRegistryLocations(String webServiceName,
            String[] registryLocations) {
        
        String[] prePublished = null;
        if(webServiceInfoMap == null){
            _logger.fine("ConfigHelper.addToConfig : Incorrect webServiceName ");
            return prePublished;
        }
        final DomainConfig dc = this.getDomainConfig();
        String appId = (String)webServiceInfoMap.get(
                WebServiceEndpointInfo.APP_ID_KEY);
        
        Boolean isStandAlone = (Boolean)webServiceInfoMap.get(
                WebServiceEndpointInfo.IS_STAND_ALONE_MODULE_KEY);
        
        String appName = appId;
        String pureWebServiceName = dropAppNameFromWebServiceName(webServiceName);
        WebServiceEndpointConfig wsec = null;
        
        if(!isStandAlone) {
            Map <String, J2EEApplicationConfig> map =
                    dc.getJ2EEApplicationConfigMap();
            J2EEApplicationConfig appConfig = map.get(appName);
            if (appConfig == null) {
                // this will never happen as this created by deployment
                _logger.log(Level.FINE, "Could not find an application with " +
                        " name = "+appName);
            } else  {
                Map <String, WebServiceEndpointConfig> wsmap =
                        appConfig.getWebServiceEndpointConfigMap();
                wsec = wsmap.get(pureWebServiceName);
            }
        } else {
            String type = (String)webServiceInfoMap.
                    get(WebServiceEndpointInfo.SERVICE_IMPL_TYPE_KEY);
            if(type.equals(WebServiceEndpointInfo.EJB_IMPL)){
                Map <String, EJBModuleConfig> map
                        = dc.getEJBModuleConfigMap();
                EJBModuleConfig ejbConfig = map.get(appName);
                if (ejbConfig == null) {
                    // should never happen
                    _logger.log(Level.FINE, "Could not find a ejb module" +
                            " with  name = "+appName);
                } else{
                    Map <String, WebServiceEndpointConfig> ejbmap =
                            ejbConfig.getWebServiceEndpointConfigMap();
                    wsec = ejbmap.get(pureWebServiceName);
                }
            } else if(type.equals(WebServiceEndpointInfo.SERVLET_IMPL)){
                Map <String, WebModuleConfig> map =
                        dc.getWebModuleConfigMap();
                WebModuleConfig webConfig = map.get(appName);
                if (webConfig == null){
                    // should never happen
                    _logger.log(Level.FINE, "Could not find a web module" +
                            " with  name = "+appName);
                } else{
                    Map <String, WebServiceEndpointConfig> webmap =
                            webConfig.getWebServiceEndpointConfigMap();
                    wsec = webmap.get(pureWebServiceName);
                }
            }
        }
        if(wsec != null){
            Map <String, RegistryLocationConfig> regMap =
                    wsec.getRegistryLocationConfigMap();
            prePublished = new String[regMap.size()];
            int i = 0;
            for (String regLoc : regMap.keySet()){
                RegistryLocationConfig rlc = regMap.get(regLoc);
                prePublished [i++] =
                        new String(rlc.getConnectorResourceJNDIName());
            }
        }
        return prePublished;
    }
    
    String getOrganizationName(String webServiceName, String registryLocation){
        String organization = null;
        if(webServiceInfoMap == null){
            _logger.fine("ConfigHelper.addToConfig : Incorrect webServiceName ");
            return null;
        }
        final DomainConfig dc = this.getDomainConfig();
        String appId = (String)webServiceInfoMap.get(
                WebServiceEndpointInfo.APP_ID_KEY);
        
        Boolean isStandAlone = (Boolean)webServiceInfoMap.get(
                WebServiceEndpointInfo.IS_STAND_ALONE_MODULE_KEY);
        
        String appName = appId;
        String underScoredWebServiceName =
                convertHashesToUnderScores(webServiceName);
        
        if(!isStandAlone) {
            Map <String, J2EEApplicationConfig> map =
                    dc.getJ2EEApplicationConfigMap();
            J2EEApplicationConfig appConfig = map.get(appName);
            if (appConfig == null) {
                // this will never happen as this created by deployment
                _logger.log(Level.FINE, "Could not find an application with " +
                        " name = "+appName);
            } else  {
                organization =
                        (String)appConfig.getPropertyValue(registryLocation+"__"+
                        underScoredWebServiceName);
            }
        } else {
            String type = (String)webServiceInfoMap.
                    get(WebServiceEndpointInfo.SERVICE_IMPL_TYPE_KEY);
            if(type.equals(WebServiceEndpointInfo.EJB_IMPL)){
                Map <String, EJBModuleConfig> map
                        = dc.getEJBModuleConfigMap();
                EJBModuleConfig ejbConfig = map.get(appName);
                if (ejbConfig == null) {
                    // should never happen
                    _logger.log(Level.FINE, "Could not find a ejb module" +
                            " with  name = "+appName);
                } else{
                    organization =
                            (String)ejbConfig.getPropertyValue(registryLocation+
                            "__"+ underScoredWebServiceName);
                }
            } else if(type.equals(WebServiceEndpointInfo.SERVLET_IMPL)){
                Map <String, WebModuleConfig> map =
                        dc.getWebModuleConfigMap();
                WebModuleConfig webConfig = map.get(appName);
                if (webConfig == null){
                    // should never happen
                    _logger.log(Level.FINE, "Could not find a web module" +
                            " with  name = "+appName);
                } else{
                    organization =
                            (String)webConfig.getPropertyValue(registryLocation+
                            "__"+ underScoredWebServiceName);
                }
            }
        }
        return organization;
    }
    /*
     * Used for the key in domain.xml. The key, value is of the type
     * registryjndiname__webservicename, Organization
     * webservice name , is the FQN with # converted to __
     */
    
    private String convertHashesToUnderScores(String hashed){
        String returnValue = hashed.replaceAll("#", "__");
        return returnValue;
    }
    
    public String[] checkForDuplicateRegistries(String[] jndi) {
        if (jndi.length <=1)
            return null;
        // plain comparision to see if the jndi names are duplicates
        String[] duplicate = null;
        for (int i = 0; i<jndi.length; i++){
            for (int j=i+1; j<jndi.length; j++){
                if (jndi[i].equals(jndi[j])){
                    duplicate = new String[1];
                    duplicate[0] = jndi[i];
                    this._logger.log(Level.SEVERE,
                            "Duplicate Registry Jndi Names. Will not" +
                            " publish the same " +
                            "web service twice to the same registry. " +
                            "Remove one jndi name ("+jndi[i]+")");
                    return duplicate;
                }
            }
        }
        if (duplicate == null){
            final DomainConfig dc = this.getDomainConfig();
            Map<String, ConnectorResourceConfig> crcMap =
                    dc.getConnectorResourceConfigMap();
            for (int i = 0; i<jndi.length; i++){
                ConnectorResourceConfig crc = crcMap.get(jndi[i]);
                String pool = crc.getPoolName();
                for (int j=i+1; j<jndi.length; j++){
                    ConnectorResourceConfig crcNew = crcMap.get(jndi[j]);
                    if(pool.equals(crcNew.getPoolName())){
                        duplicate = new String[2];
                        duplicate[0] = jndi[i];
                        duplicate[1] = jndi[j];
                        this._logger.log(Level.SEVERE,
                                "Registry Jndi Names point to the same registry. Will not" +
                                " publish the same " +
                                "web service twice to the same registry. " +
                                "Remove one duplicate of  ("+jndi[i]+")" +
                                " or ("+jndi[j]+")");
                        return duplicate;
                    }
                }
            }
        }
        return null;
    }
}
