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

package com.sun.enterprise.config.serverbeans;

import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.logging.Logger;
import java.util.logging.Level;
//import com.sun.logging.LogDomains;

import com.sun.enterprise.config.util.ConfigXPathHelper;


/** A helper class to store valid xpaths for ias7.0 SE.
 * Helper methods are provided to generate valid xpaths
 * that are needed by configContext.
 */
public final class ServerXPathHelper {

    //"static" names for milestone 1 only
//    public static final String DEFAULT_SERVER_MODEL_NAME = "server"; //the same for both instances
    public static final String DEFAULT_CONFIG_MODEL_NAME = "server-config";
    public static final String REGEX_DEFAULT_CONFIG_MODEL_NAME = "server\\-config";
    public static final String DEFAULT_HOST_MODEL_NAME   = "host1"; //do we need it?
    
    
    
    public static final String REGEX_ONE_PLUS = "{1,}";
    
    public static final String XPATH_SEPARATOR = "/";
    
    public static final String XPATH_DOMAIN = XPATH_SEPARATOR + ServerTags.DOMAIN;
    public static final String REGEX_XPATH_DOMAIN = XPATH_SEPARATOR
            + REGEX_ONE_PLUS + ServerTags.DOMAIN;

//    public static final String XPATH_SERVER = XPATH_DOMAIN + XPATH_SEPARATOR + 
//                                ServerTags.SERVERS + XPATH_SEPARATOR + 
//                                ServerTags.SERVER +
//                                "[@" + ServerTags.NAME + "='" + DEFAULT_SERVER_MODEL_NAME + "']";
    
    public static final String XPATH_CONFIG = XPATH_DOMAIN + XPATH_SEPARATOR + 
                                ServerTags.CONFIGS + XPATH_SEPARATOR + 
                                ServerTags.CONFIG +
                                "[@" + ServerTags.NAME + "='" + DEFAULT_CONFIG_MODEL_NAME + "']";
    
    public static final String REGEX_XPATH_CONFIG = REGEX_XPATH_DOMAIN
            + XPATH_SEPARATOR + REGEX_ONE_PLUS + ServerTags.CONFIGS
            + XPATH_SEPARATOR + REGEX_ONE_PLUS + ServerTags.CONFIG
            + "[@" + ServerTags.NAME + "='" + REGEX_DEFAULT_CONFIG_MODEL_NAME
            + "']";

    public static final String XPATH_APPLICATIONS = XPATH_DOMAIN + XPATH_SEPARATOR +
                                ServerTags.APPLICATIONS;
    
    public static final String XPATH_J2EE_APPLICATION = XPATH_APPLICATIONS + XPATH_SEPARATOR +
                                ServerTags.J2EE_APPLICATION;
    
    public static final String XPATH_EJB_MODULE = XPATH_APPLICATIONS + XPATH_SEPARATOR +
                                ServerTags.EJB_MODULE;
    
    public static final String XPATH_WEB_MODULE = XPATH_APPLICATIONS + XPATH_SEPARATOR +
                                ServerTags.WEB_MODULE;
    
    public static final String XPATH_CONNECTOR_MODULE = XPATH_APPLICATIONS + XPATH_SEPARATOR +
                                ServerTags.CONNECTOR_MODULE;

    public static final String XPATH_APPCLIENT_MODULE = XPATH_APPLICATIONS + XPATH_SEPARATOR +
                                ServerTags.APPCLIENT_MODULE;

    public static final String XPATH_LIFECYCLE_MODULE = XPATH_APPLICATIONS + XPATH_SEPARATOR +
                                ServerTags.LIFECYCLE_MODULE;

/*    public static final String XPATH_CONNECTION_GROUP = XPATH_SEPARATOR + ServerTags.SERVER + XPATH_SEPARATOR +
                                ServerTags.HTTP_SERVICE + XPATH_SEPARATOR + 
                                ServerTags.HTTP_LISTENER + XPATH_SEPARATOR + 
                                ServerTags.CONNECTION_GROUP;
 */ //FIXME removed
                                
    public static final String XPATH_LOG_SERVICE       = XPATH_CONFIG + XPATH_SEPARATOR + ServerTags.LOG_SERVICE;
    public static final String XPATH_SECURITY_SERVICE  = XPATH_CONFIG + XPATH_SEPARATOR + ServerTags.SECURITY_SERVICE;
    public static final String XPATH_SECURITY_AUTH_REALM = XPATH_SECURITY_SERVICE + XPATH_SEPARATOR + ServerTags.AUTH_REALM;

    //RESOURCES XPATHes
    public static final String XPATH_RESOURCES       = XPATH_DOMAIN + XPATH_SEPARATOR + ServerTags.RESOURCES;
    public static final String XPATH_JDBC_RESOURCE   = XPATH_RESOURCES + XPATH_SEPARATOR + ServerTags.JDBC_RESOURCE;
   // public static final String XPATH_JMS_RESOURCE    = XPATH_RESOURCES + XPATH_SEPARATOR + ServerTags.JMS_RESOURCE;
    public static final String XPATH_JNDI_RESOURCE   = XPATH_RESOURCES + XPATH_SEPARATOR + ServerTags.EXTERNAL_JNDI_RESOURCE;
    public static final String XPATH_CUSTOM_RESOURCE = XPATH_RESOURCES + XPATH_SEPARATOR + ServerTags.CUSTOM_RESOURCE;
    public static final String XPATH_JDBC_CONNECTION_POOL = XPATH_RESOURCES + XPATH_SEPARATOR + ServerTags.JDBC_CONNECTION_POOL;
    public static final String XPATH_PM_FACTORY_RESOURCE = XPATH_RESOURCES + XPATH_SEPARATOR + ServerTags.PERSISTENCE_MANAGER_FACTORY_RESOURCE;
    public static final String XPATH_MAIL_RESOURCE   = XPATH_RESOURCES + XPATH_SEPARATOR + ServerTags.MAIL_RESOURCE;
    public static final String XPATH_ADMIN_OBJECT_RESOURCE   = XPATH_RESOURCES + XPATH_SEPARATOR + ServerTags.ADMIN_OBJECT_RESOURCE;
    public static final String XPATH_CONNECTOR_RESOURCE   = XPATH_RESOURCES + XPATH_SEPARATOR + ServerTags.CONNECTOR_RESOURCE;    
    public static final String XPATH_CONNECTOR_CONNECTION_POOL   = XPATH_RESOURCES + XPATH_SEPARATOR + ServerTags.CONNECTOR_CONNECTION_POOL;        
    public static final String XPATH_RESOURCE_ADAPTER_CONFIG   = XPATH_RESOURCES + XPATH_SEPARATOR + ServerTags.RESOURCE_ADAPTER_CONFIG;


    //HTTP_SERVICE XPATHes
    public static final String XPATH_HTTP_SERVICE  = XPATH_CONFIG + XPATH_SEPARATOR + ServerTags.HTTP_SERVICE;
    public static final String XPATH_HTTP_LISTENER = XPATH_HTTP_SERVICE + XPATH_SEPARATOR + ServerTags.HTTP_LISTENER;
    //public static final String XPATH_HTTP_ACL      = XPATH_HTTP_SERVICE + XPATH_SEPARATOR + ServerTags.ACL;
    //public static final String XPATH_HTTP_MIME     = XPATH_HTTP_SERVICE + XPATH_SEPARATOR + ServerTags.MIME;

    //JTS
    public static final String XPATH_TRANSACTION_SERVICE  = XPATH_CONFIG + XPATH_SEPARATOR + ServerTags.TRANSACTION_SERVICE;
    
    //JMS
    public static final String XPATH_JMS_SERVICE  = XPATH_CONFIG + XPATH_SEPARATOR + ServerTags.JMS_SERVICE;

    //VIRTUAL SERVER
    // public static final String XPATH_VIRTUAL_SERVER_CLASS = XPATH_HTTP_SERVICE + XPATH_SEPARATOR + ServerTags.VIRTUAL_SERVER_CLASS;
    //IIOP XPATHes
    public static final String XPATH_IIOP_SERVICE     = XPATH_CONFIG + XPATH_SEPARATOR + ServerTags.IIOP_SERVICE;
    public static final String XPATH_ORB              = XPATH_IIOP_SERVICE + XPATH_SEPARATOR + ServerTags.ORB;
    public static final String XPATH_IIOP_LISTENER    = XPATH_IIOP_SERVICE + XPATH_SEPARATOR + ServerTags.IIOP_LISTENER;

    //CONTAINERS
    public static final String XPATH_MDB_CONTAINER = XPATH_CONFIG + XPATH_SEPARATOR + ServerTags.MDB_CONTAINER;
    public static final String XPATH_WEB_CONTAINER = XPATH_CONFIG + XPATH_SEPARATOR + ServerTags.WEB_CONTAINER;
    public static final String XPATH_EJB_CONTAINER = XPATH_CONFIG + XPATH_SEPARATOR + ServerTags.EJB_CONTAINER;
    
    public static final String XPATH_JAVACONFIG = XPATH_CONFIG + XPATH_SEPARATOR + ServerTags.JAVA_CONFIG;
    public static final String XPATH_PROFILER   = XPATH_JAVACONFIG + XPATH_SEPARATOR + ServerTags.PROFILER;

	//private static final Logger _logger = LogDomains.getLogger(LogDomains.CONFIG_LOGGER);


    public static String getServerIdXpath(String serverId) {
        return XPATH_DOMAIN + XPATH_SEPARATOR + 
               ServerTags.SERVERS + XPATH_SEPARATOR + 
               ServerTags.SERVER +
               "[@" + ServerTags.NAME + "='" + serverId + "']";
    }
    
    public static String getConfigIdXpath(String configId) {
        return XPATH_DOMAIN + XPATH_SEPARATOR + 
               ServerTags.CONFIGS + XPATH_SEPARATOR + 
               ServerTags.CONFIG +
               "[@" + ServerTags.NAME + "='" + configId + "']";
    }

   /**
    * 
    * @return  */   
    public static String getAppIdXpathExpression(String appId) {
        return XPATH_J2EE_APPLICATION +"[@" + ServerTags.NAME + "='" + appId + "']";
    }

    public static String getEjbModuleIdXpathExpression(String moduleId) {
        return getAbsoluteIdXpathExpression(XPATH_EJB_MODULE, 
                                            ServerTags.NAME, 
                                            moduleId);
    }

    public static String getWebModuleIdXpathExpression(String moduleId) {
        return getAbsoluteIdXpathExpression(XPATH_WEB_MODULE, 
                                            ServerTags.NAME, 
                                            moduleId);
    }
    
    public static String getConnectorModuleIdXpathExpression(String moduleId) {
        /** IASRI 4666602
        return getAbsoluteIdXpathExpression(XPATH_CONNECTOR_MODULE, 
                                            ServerTags.NAME, 
                                            moduleId);
         **/
        // START OF IASRI 4666602
        return XPATH_CONNECTOR_MODULE + "[@" + ServerTags.NAME + "='" + moduleId + "']";
        // END OF IASRI 4666602
    }

    public static String getAppClientModuleIdXpathExpression(String moduleId) {
        return getAbsoluteIdXpathExpression(XPATH_APPCLIENT_MODULE, 
                                            ServerTags.NAME, 
                                            moduleId);
    }
    
    public static String getLifecycleModuleIdXpath(String moduleId) {
        return getAbsoluteIdXpathExpression(XPATH_LIFECYCLE_MODULE, 
                                            ServerTags.NAME, 
                                            moduleId);
    }
    //RESOURCES
    public static String getJDBCResourceIdXpath(String jndiId) {
        return getAbsoluteIdXpathExpression(XPATH_JDBC_RESOURCE, ServerTags.JNDI_NAME, jndiId);
    }
   /* public static String getJMSResourceIdXpath(String jndiId) {
        return getAbsoluteIdXpathExpression(XPATH_JMS_RESOURCE, ServerTags.JNDI_NAME, jndiId);
    }
   */
    public static String getJNDIResourceIdXpath(String jndiId) {
        return getAbsoluteIdXpathExpression(XPATH_JNDI_RESOURCE, ServerTags.JNDI_NAME, jndiId);
    }
    public static String getMailResourceIdXpath(String jndiId) {
        return getAbsoluteIdXpathExpression(XPATH_MAIL_RESOURCE, ServerTags.JNDI_NAME, jndiId);
    }
    public static String getCustomResourceIdXpath(String jndiId) {
        return getAbsoluteIdXpathExpression(XPATH_CUSTOM_RESOURCE, ServerTags.JNDI_NAME, jndiId);
    }
    public static String getJDBCConnectionPoolIdXpath(String poolId) {
        return getAbsoluteIdXpathExpression(XPATH_JDBC_CONNECTION_POOL, ServerTags.NAME, poolId);
    }
    public static String getPMFactoryResourceIdXpath(String jndiId) {
        return getAbsoluteIdXpathExpression(XPATH_PM_FACTORY_RESOURCE, ServerTags.JNDI_NAME, jndiId);
    }

    //CONTAINERS
    public static String getMDBContainerXpath()
    {
        return XPATH_MDB_CONTAINER;
    }
    public static String getWEBContainerXpath()
    {
        return XPATH_WEB_CONTAINER;
    }
    public static String getEJBContainerXpath() 
    {
        return XPATH_EJB_CONTAINER;
    }

    //TRANSACTION_SERVICE
    public static String getTransactionServiceXpath() {
        return XPATH_TRANSACTION_SERVICE;
    }

    //JMS_SERVICE
    public static String getJmsServiceXpath() {
        return XPATH_JMS_SERVICE;
    }

    //HTTP_SERVICE
    public static String getHTTPServiceXpath() {
        return XPATH_HTTP_SERVICE;
    }
    public static String getHTTPListenerIdXpath(String id) {
        return getAbsoluteIdXpathExpression(XPATH_HTTP_LISTENER, ServerTags.ID, id);
    }
/*
    public static String getHTTPMimeIdXpath(String id) {
        return getAbsoluteIdXpathExpression(XPATH_HTTP_MIME, ServerTags.ID, id);
    }
    public static String getHTTPAclIdXpath(String id) {
        return getAbsoluteIdXpathExpression(XPATH_HTTP_ACL, ServerTags.ID, id);
    }
*/
    /*
    public static String getHTTPConnectionGroupIdXpath(String listenerId, String id) {
        return getAbsoluteIdXpathExpression(
             getHTTPListenerIdXpath(listenerId)+XPATH_SEPARATOR+ ServerTags.CONNECTION_GROUP,
                                    ServerTags.ID, id);
    }
     */ //FIXME: removed
    

    //VIRTUAL SERVER (CLASS)
   /* public static String getVirtualServerClassIdXpath(String id) {
        return getAbsoluteIdXpathExpression(XPATH_VIRTUAL_SERVER_CLASS, ServerTags.ID, id);
    }
   */
    public static String getVirtualServerIdXpath(String classId, String id) {
        return getAbsoluteIdXpathExpression(
                            XPATH_HTTP_SERVICE + XPATH_SEPARATOR + ServerTags.VIRTUAL_SERVER,
                            ServerTags.ID, id);
    }
  
/* 
    public static String getAuthDbIdXpath(String serverClassId, String serverId, String id) {
        return getAbsoluteIdXpathExpression(
                            getVirtualServerIdXpath(serverClassId, serverId) + XPATH_SEPARATOR + ServerTags.AUTH_DB,
                            ServerTags.ID, id);
    }
*/

    //IIOP_SERVICE
    public static String getIIOPServiceXpath() {
        return XPATH_IIOP_SERVICE;
    }
    public static String getOrbXpath() {
        return XPATH_ORB;
    }
    public static String getIIOPListenerIdXpath(String id) {
        return getAbsoluteIdXpathExpression(XPATH_IIOP_LISTENER, ServerTags.ID, id);
    }
    
    //LOG_SERVICE
    public static String getLogServiceXpath() {
        return XPATH_LOG_SERVICE;
    }

    //SECURITY_SERVICE
    public static String getSecurityServiceXpath() {
        return XPATH_SECURITY_SERVICE;
    }
    public static String getSecurityAuthRealmIdXpath(String name) {
        return getAbsoluteIdXpathExpression(XPATH_SECURITY_AUTH_REALM, ServerTags.NAME, name);
    }

    //JAVA_CONFIG
    public static String getJavaConfigXpath() {
        return XPATH_JAVACONFIG;
    }
    public static String getProfilerXpath() {
        return XPATH_PROFILER;
    }
    /**
     * @param childTagName
     * @param nameId
     * @param valueId
     * 
     */    
    public static String getAbsoluteIdXpathExpression(
                                String childTagName, 
                                String nameId, 
                                String valueId) {
                                    
        return ConfigXPathHelper.getAbsoluteIdXpathExpression(
                            childTagName, 
                            nameId, 
                            valueId);
    }

    /* 
     *  returns element's tag name extracting from its xpath
     */
    public static String getLastNodeName(String xPath) {
        return ConfigXPathHelper.getLastNodeName(xPath);
    }
    
    /* 
     *  returns parent XPath for  givend child's xpath
     *  correcly bypasses bracketed values with possible escaping inside of quoted values
     */
    public static String getParentXPath(String xPath) {
        return ConfigXPathHelper.getParentXPath(xPath);
    }
}
