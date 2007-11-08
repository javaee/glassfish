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

package com.sun.enterprise.admin.server.core.mbean.config;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.FactoryConfigurationError;  
import javax.xml.parsers.ParserConfigurationException;
 
import org.xml.sax.SAXException;  
import org.xml.sax.SAXParseException;  
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import java.util.Vector;
import java.util.Properties;
import java.util.Iterator;
import java.util.Enumeration;

import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.admin.common.constant.AdminConstants;

import java.util.logging.Level;
import java.util.logging.Logger;

//i18n import
import com.sun.enterprise.util.i18n.StringManager;

/**
 *This Class reads the Properties (resources) from the xml file supplied 
 *to constructor
 */
public class ResourcesXMLParser 
{

    private File resourceFile = null;
    private Document document;
    private Vector resources;
    
    // node name constants
    private static final String CUSTOM_RESOURCE = "custom-resource";
    private static final String EXT_JNDI_RESOURCE = "external-jndi-resource";
    private static final String JDBC_RESOURCE = "jdbc-resource";
    private static final String JDBC_CONN_POOL = "jdbc-connection-pool";
    private static final String MAIL_RESOURCE = "mail-resource";
    private static final String PERSISTENCE_RESOURCE = 
                                        "persistence-manager-factory-resource";
    private static final String JMS_RESOURCE = "jms-resource";

	// i18n StringManager
	private static StringManager localStrings =
		StringManager.getManager( ResourcesXMLParser.class );
    
    //Attribute names constants
    // JDBC Resource
    public static final String JNDI_NAME = "jndi-name";
    public static final String POOL_NAME = "pool-name";
    // JMS Resource                                        
    public static final String RES_TYPE = "res-type";
    public static final String FACTORY_CLASS = "factory-class";
    public static final String ENABLED = "enabled";

    // External JNDI Resource
    public static final String JNDI_LOOKUP = "jndi-lookup-name";
    
    // JDBC Connection pool
    //public static final String ID = "id";
    public static final String CONNECTION_POOL_NAME = "name";
    public static final String STEADY_POOL_SIZE = "steady-pool-size";
    public static final String MAX_POOL_SIZE = "max-pool-size";
    public static final String MAX_WAIT_TIME_IN_MILLIS = "max-wait-time-in-millis";
    public static final String POOL_SIZE_QUANTITY = "pool-resize-quantity";
    public static final String IDLE_TIME_OUT_IN_SECONDS = "idle-timeout-in-seconds";
    public static final String IS_CONNECTION_VALIDATION_REQUIRED = "is-connection-validation-required";
    public static final String CONNECTION_VALIDATION_METHOD = "connection-validation-method";
    public static final String FAIL_ALL_CONNECTIONS = "fail-all-connections";
    public static final String VALIDATION_TABLE_NAME = "validation-table-name";
    public static final String DATASOURCE_CLASS = "datasource-classname";
    public static final String TRANS_ISOLATION_LEVEL = "transaction-isolation-level";
    public static final String IS_ISOLATION_LEVEL_GUARANTEED = "is-isolation-level-guaranteed";

    //Mail resource
    public static final String MAIL_HOST = "host";
    public static final String MAIL_USER = "user";
    public static final String MAIL_FROM_ADDRESS = "from";
    public static final String MAIL_STORE_PROTO = "store-protocol";
    public static final String MAIL_STORE_PROTO_CLASS = "store-protocol-class";
    public static final String MAIL_TRANS_PROTO = "transport-protocol";
    public static final String MAIL_TRANS_PROTO_CLASS = "transport-protocol-class";
    public static final String MAIL_DEBUG = "debug";

    //Persistence Manager Factory resource
    public static final String JDBC_RESOURCE_JNDI_NAME = "jdbc-resource-jndi-name";
  
    /** Creates new ResourcesXMLParser */
    public ResourcesXMLParser(String resourceFileName) throws Exception
    {
        resourceFile = new File(resourceFileName);
        initProperties();
        resources = new Vector();
        generateResourceObjects();
    }

    /**
     *Parse the XML Properties file and populate it into document object
     */
    public void initProperties() throws Exception
    {
        DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        try 
        {
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            if (resourceFile == null)
            {
				String msg = localStrings.getString( "admin.server.core.mbean.config.no_resource_file" );
                throw new Exception( msg );
            }
            InputSource is = new InputSource(resourceFile.toString());
            document = builder.parse(is);

        } 
        catch (SAXException sxe) 
        {
            Exception  x = sxe;
            if (sxe.getException() != null)
               x = sxe.getException();
            //x.printStackTrace();
            throw new Exception(x.getLocalizedMessage());

        }
        catch (ParserConfigurationException pce) 
        {
            // Parser with specified options can't be built
            throw new Exception(pce.getLocalizedMessage());
        }
        catch (IOException ioe) 
        {
            // I/O error
            throw new Exception(ioe.getLocalizedMessage());
        }
    }
    
    /**
     * Get All the resources from the document object.
     *
     */
    private void generateResourceObjects() throws Exception
    {
        if (document != null) 
        {
            for (Node nextKid = document.getDocumentElement().getFirstChild();
                    nextKid != null; nextKid = nextKid.getNextSibling()) 
            {
                String nodeName = nextKid.getNodeName();
                if (nodeName.equalsIgnoreCase(CUSTOM_RESOURCE)) 
                {
                    generateCustomResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(EXT_JNDI_RESOURCE)) 
                {
                    generateJNDIResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(JDBC_RESOURCE)) 
                {
                    generateJDBCResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(JDBC_CONN_POOL)) 
                {
                    generateJDBCConnectionPoolResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(MAIL_RESOURCE)) 
                {
                    generateMailResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(PERSISTENCE_RESOURCE)) 
                {
                    generatePersistenceResource(nextKid);
                }
                else if (nodeName.equalsIgnoreCase(JMS_RESOURCE)) 
                {
                    generateJMSResource(nextKid);
                }
                
            }
        }
    }
    
    /*
     * Generate the Custom resource
     */
    private void generateCustomResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;
        
        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = jndiNameNode.getNodeValue();
        Node resTypeNode = attributes.getNamedItem(RES_TYPE);
        String resType = resTypeNode.getNodeValue();
        Node factoryClassNode = 
                attributes.getNamedItem(FACTORY_CLASS);
        String factoryClass = factoryClassNode.getNodeValue();
        Node enabledNode = attributes.getNamedItem(ENABLED);
        
        Resource customResource = new Resource(Resource.CUSTOM_RESOURCE);
        customResource.setAttribute(JNDI_NAME, jndiName);
        customResource.setAttribute(RES_TYPE, resType);
        customResource.setAttribute(FACTORY_CLASS, factoryClass);
        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           customResource.setAttribute(ENABLED, sEnabled);
        }
        
        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(customResource, children);
        resources.add(customResource);
        
        //debug strings
        printResourceElements(customResource);
    }
    
    /*
     * Generate the JNDI resource
     */
    private void generateJNDIResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;
        
        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = jndiNameNode.getNodeValue();
        Node jndiLookupNode = attributes.getNamedItem(JNDI_LOOKUP);
        String jndiLookup = jndiLookupNode.getNodeValue();
        Node resTypeNode = attributes.getNamedItem(RES_TYPE);
        String resType = resTypeNode.getNodeValue();
        Node factoryClassNode = attributes.getNamedItem(FACTORY_CLASS);
        String factoryClass = factoryClassNode.getNodeValue();
        Node enabledNode = attributes.getNamedItem(ENABLED);
        
        Resource jndiResource = new Resource(Resource.EXT_JNDI_RESOURCE);
        jndiResource.setAttribute(JNDI_NAME, jndiName);
        jndiResource.setAttribute(JNDI_LOOKUP, jndiLookup);
        jndiResource.setAttribute(RES_TYPE, resType);
        jndiResource.setAttribute(FACTORY_CLASS, factoryClass);
        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           jndiResource.setAttribute(ENABLED, sEnabled);
        }
        
        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(jndiResource, children);
        resources.add(jndiResource);
        
        //debug strings
        printResourceElements(jndiResource);
    }
    
    /*
     * Generate the JDBC resource
     */
    private void generateJDBCResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;
        
        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = jndiNameNode.getNodeValue();
        Node poolNameNode = attributes.getNamedItem(POOL_NAME);
        String poolName = poolNameNode.getNodeValue();
        Node enabledNode = attributes.getNamedItem(ENABLED);

        Resource jdbcResource = new Resource(Resource.JDBC_RESOURCE);
        jdbcResource.setAttribute(JNDI_NAME, jndiName);
        jdbcResource.setAttribute(POOL_NAME, poolName);
        if (enabledNode != null) {
           String enabledName = enabledNode.getNodeValue();
           jdbcResource.setAttribute(ENABLED, enabledName);
        }
        
        NodeList children = nextKid.getChildNodes();
        //get description
        if (children != null) 
        {
            for (int ii=0; ii<children.getLength(); ii++) 
            {
                if (children.item(ii).getNodeName().equals("description")) 
                    jdbcResource.setDescription(
                    children.item(ii).getFirstChild().getNodeValue());
            }
        }

        resources.add(jdbcResource);
        
        //debug strings
        printResourceElements(jdbcResource);
    }
    
    /*
     * Generate the JDBC Connection pool Resource
     */
    private void generateJDBCConnectionPoolResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;
        
        Node nameNode = attributes.getNamedItem(CONNECTION_POOL_NAME);
        String name = nameNode.getNodeValue();
        Node nSteadyPoolSizeNode = attributes.getNamedItem(STEADY_POOL_SIZE);
        Node nMaxPoolSizeNode = attributes.getNamedItem(MAX_POOL_SIZE);
        Node nMaxWaitTimeInMillisNode  = 
             attributes.getNamedItem(MAX_WAIT_TIME_IN_MILLIS);
        Node nPoolSizeQuantityNode  = 
             attributes.getNamedItem(POOL_SIZE_QUANTITY);
        Node nIdleTimeoutInSecNode  = 
             attributes.getNamedItem(IDLE_TIME_OUT_IN_SECONDS);
        Node nIsConnectionValidationRequiredNode  = 
             attributes.getNamedItem(IS_CONNECTION_VALIDATION_REQUIRED);
        Node nConnectionValidationMethodNode  = 
             attributes.getNamedItem(CONNECTION_VALIDATION_METHOD);
        Node nFailAllConnectionsNode  = 
             attributes.getNamedItem(FAIL_ALL_CONNECTIONS);
        Node nValidationTableNameNode  = 
             attributes.getNamedItem(VALIDATION_TABLE_NAME);
        Node nResType  = attributes.getNamedItem(RES_TYPE);
        Node nTransIsolationLevel  = 
             attributes.getNamedItem(TRANS_ISOLATION_LEVEL);
        Node nIsIsolationLevelQuaranteed  = 
             attributes.getNamedItem(IS_ISOLATION_LEVEL_GUARANTEED);
        Node datasourceNode = attributes.getNamedItem(DATASOURCE_CLASS);
        String datasource = datasourceNode.getNodeValue();
        
        Resource jdbcResource = new Resource(Resource.JDBC_CONN_POOL);
        jdbcResource.setAttribute(CONNECTION_POOL_NAME, name);
        jdbcResource.setAttribute(DATASOURCE_CLASS, datasource);
        if (nSteadyPoolSizeNode != null) {
           String sSteadyPoolSize = nSteadyPoolSizeNode.getNodeValue();
           jdbcResource.setAttribute(STEADY_POOL_SIZE, sSteadyPoolSize);
        }
        if (nMaxPoolSizeNode != null) {
           String sMaxPoolSize = nMaxPoolSizeNode.getNodeValue();
           jdbcResource.setAttribute(MAX_POOL_SIZE, sMaxPoolSize);
        }
        if (nMaxWaitTimeInMillisNode != null) {
           String sMaxWaitTimeInMillis = nMaxWaitTimeInMillisNode.getNodeValue();
           jdbcResource.setAttribute(MAX_WAIT_TIME_IN_MILLIS, sMaxWaitTimeInMillis);
        }
        if (nPoolSizeQuantityNode != null) {
           String sPoolSizeQuantity = nPoolSizeQuantityNode.getNodeValue();
           jdbcResource.setAttribute(POOL_SIZE_QUANTITY, sPoolSizeQuantity);
        }
        if (nIdleTimeoutInSecNode != null) {
           String sIdleTimeoutInSec = nIdleTimeoutInSecNode.getNodeValue();
           jdbcResource.setAttribute(IDLE_TIME_OUT_IN_SECONDS, sIdleTimeoutInSec);
        }
        if (nIsConnectionValidationRequiredNode != null) {
           String sIsConnectionValidationRequired = nIsConnectionValidationRequiredNode.getNodeValue();
           jdbcResource.setAttribute(IS_CONNECTION_VALIDATION_REQUIRED, sIsConnectionValidationRequired);
        }
        if (nConnectionValidationMethodNode != null) {
           String sConnectionValidationMethod = nConnectionValidationMethodNode.getNodeValue();
           jdbcResource.setAttribute(CONNECTION_VALIDATION_METHOD, sConnectionValidationMethod);
        }
        if (nFailAllConnectionsNode != null) {
           String sFailAllConnection = nFailAllConnectionsNode.getNodeValue();
           jdbcResource.setAttribute(FAIL_ALL_CONNECTIONS, sFailAllConnection);
        }
        if (nValidationTableNameNode != null) {
           String sValidationTableName = nValidationTableNameNode.getNodeValue();
           jdbcResource.setAttribute(VALIDATION_TABLE_NAME, sValidationTableName);
        }
        if (nResType != null) {
           String sResType = nResType.getNodeValue();
           jdbcResource.setAttribute(RES_TYPE, sResType);
        }
        if (nTransIsolationLevel != null) {
           String sTransIsolationLevel = nTransIsolationLevel.getNodeValue();
           jdbcResource.setAttribute(TRANS_ISOLATION_LEVEL, sTransIsolationLevel);
        }
        if (nIsIsolationLevelQuaranteed != null) {
           String sIsIsolationLevelQuaranteed = 
                  nIsIsolationLevelQuaranteed.getNodeValue();
           jdbcResource.setAttribute(IS_ISOLATION_LEVEL_GUARANTEED, 
                                     sIsIsolationLevelQuaranteed);
        }
        
        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(jdbcResource, children);
        resources.add(jdbcResource);
        
        //debug strings
        printResourceElements(jdbcResource);
    }
    
    /*
     * Generate the Mail resource
     */
    private void generateMailResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;

        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        Node hostNode   = attributes.getNamedItem(MAIL_HOST);
        Node userNode   = attributes.getNamedItem(MAIL_USER);
        Node fromAddressNode   = attributes.getNamedItem(MAIL_FROM_ADDRESS);
        Node storeProtoNode   = attributes.getNamedItem(MAIL_STORE_PROTO);
        Node storeProtoClassNode   = attributes.getNamedItem(MAIL_STORE_PROTO_CLASS);
        Node transProtoNode   = attributes.getNamedItem(MAIL_TRANS_PROTO);
        Node transProtoClassNode   = attributes.getNamedItem(MAIL_TRANS_PROTO_CLASS);
        Node debugNode   = attributes.getNamedItem(MAIL_DEBUG);
        Node enabledNode   = attributes.getNamedItem(ENABLED);

        String jndiName = jndiNameNode.getNodeValue();
        String host     = hostNode.getNodeValue();
        String user     = userNode.getNodeValue();
        String fromAddress = fromAddressNode.getNodeValue();
        
        Resource mailResource = new Resource(Resource.MAIL_RESOURCE);

        mailResource.setAttribute(JNDI_NAME, jndiName);
        mailResource.setAttribute(MAIL_HOST, host);
        mailResource.setAttribute(MAIL_USER, user);
        mailResource.setAttribute(MAIL_FROM_ADDRESS, fromAddress);
        if (storeProtoNode != null) {
           String sStoreProto = storeProtoNode.getNodeValue();
           mailResource.setAttribute(MAIL_STORE_PROTO, sStoreProto);
        }
        if (storeProtoClassNode != null) {
           String sStoreProtoClass = storeProtoClassNode.getNodeValue();
           mailResource.setAttribute(MAIL_STORE_PROTO_CLASS, sStoreProtoClass);
        }
        if (transProtoNode != null) {
           String sTransProto = transProtoNode.getNodeValue();
           mailResource.setAttribute(MAIL_TRANS_PROTO, sTransProto);
        }
        if (transProtoClassNode != null) {
           String sTransProtoClass = transProtoClassNode.getNodeValue();
           mailResource.setAttribute(MAIL_TRANS_PROTO_CLASS, sTransProtoClass);
        }
        if (debugNode != null) {
           String sDebug = debugNode.getNodeValue();
           mailResource.setAttribute(MAIL_DEBUG, sDebug);
        }
        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           mailResource.setAttribute(ENABLED, sEnabled);
        }

        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(mailResource, children);
        resources.add(mailResource);
        
        //debug strings
        printResourceElements(mailResource);
    }
    
    /*
     * Generate the Persistence Factory Manager resource
     */
    private void generatePersistenceResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;
        
        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = jndiNameNode.getNodeValue();
        Node factoryClassNode = attributes.getNamedItem(FACTORY_CLASS);
        Node poolNameNode = attributes.getNamedItem(JDBC_RESOURCE_JNDI_NAME);
        Node enabledNode = attributes.getNamedItem(ENABLED);

        Resource persistenceResource = 
                    new Resource(Resource.PERSISTENCE_RESOURCE);
        persistenceResource.setAttribute(JNDI_NAME, jndiName);
        if (factoryClassNode != null) {
           String factoryClass = factoryClassNode.getNodeValue();
           persistenceResource.setAttribute(FACTORY_CLASS, factoryClass);
        }
        if (poolNameNode != null) {
           String poolName = poolNameNode.getNodeValue();
           persistenceResource.setAttribute(JDBC_RESOURCE_JNDI_NAME, poolName);
        }
        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           persistenceResource.setAttribute(ENABLED, sEnabled);
        }
        
        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(persistenceResource, children);
        resources.add(persistenceResource);
        
        //debug strings
        printResourceElements(persistenceResource);
    }
    
    /*
     * Generate the JMS resource
     */
    private void generateJMSResource(Node nextKid) throws Exception
    {
        NamedNodeMap attributes = nextKid.getAttributes();
        if (attributes == null)
            return;
        
        Node jndiNameNode = attributes.getNamedItem(JNDI_NAME);
        String jndiName = jndiNameNode.getNodeValue();
        Node resTypeNode = attributes.getNamedItem(RES_TYPE);
        String resType = resTypeNode.getNodeValue();
        Node enabledNode = attributes.getNamedItem(ENABLED);
        
        Resource jmsResource = new Resource(Resource.JMS_RESOURCE);
        jmsResource.setAttribute(JNDI_NAME, jndiName);
        jmsResource.setAttribute(RES_TYPE, resType);
        if (enabledNode != null) {
           String sEnabled = enabledNode.getNodeValue();
           jmsResource.setAttribute(ENABLED, sEnabled);
        }  
        
        NodeList children = nextKid.getChildNodes();
        generatePropertyElement(jmsResource, children);
        resources.add(jmsResource);

        //debug strings
        printResourceElements(jmsResource);
    }

    private void generatePropertyElement(Resource rs, NodeList children) throws Exception 
    {
       if (children != null) {
           for (int ii=0; ii<children.getLength(); ii++) {
              if (children.item(ii).getNodeName().equals("property")) {
                 NamedNodeMap attNodeMap = children.item(ii).getAttributes();
                 Node nameNode = attNodeMap.getNamedItem("name");
                 Node valueNode = attNodeMap.getNamedItem("value");
                 if (nameNode != null && valueNode != null) {
                    boolean bDescFound = false;
                    String sName = nameNode.getNodeValue();
                    String sValue = valueNode.getNodeValue();
                    //get property description
                    Node descNode = children.item(ii).getFirstChild();
                    while (descNode != null && !bDescFound) {
                       if (descNode.getNodeName().equalsIgnoreCase("description")) {
                          try {
                             rs.setElementProperty(sName, sValue, descNode.getFirstChild().getNodeValue());
                             bDescFound = true;
                          }
                          catch (DOMException dome) {
                             // DOM Error
                             throw new Exception(dome.getLocalizedMessage());
                          }
                       }
                       descNode = descNode.getNextSibling();
                    }
                    if (!bDescFound) {
                       rs.setElementProperty(sName, sValue);
                    }
                 }
              }
              if (children.item(ii).getNodeName().equals("description")) {
                 rs.setDescription(children.item(ii).getFirstChild().getNodeValue());
              }
           }
        }
    }
    
    public Iterator getResources()
    {
        return resources.iterator();
    }
    
    /*
     * Print(Debug) the resource
     */
    private void printResourceElements(Resource resource)
    {
        Properties attributes = resource.getAttributes();
        Enumeration properties = attributes.propertyNames();
        while (properties.hasMoreElements())
        {
            String name = (String) properties.nextElement();
            Logger logger = Logger.getLogger(AdminConstants.kLoggerName);
            logger.log(Level.FINE, "general.print_attr_name", name);
        }
    }
}

