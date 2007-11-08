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
 * $Id: ResourcesMBean.java,v 1.25 2007/05/15 20:51:37 msreddy Exp $
 * author hamid@sun.com
 */

package com.sun.enterprise.admin.mbeans;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Enumeration;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;
import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;

//JMX imports
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.AttributeNotFoundException;

// JMS util imports
import com.sun.enterprise.jms.IASJmsUtil;

//JMS SPI imports
import com.sun.messaging.jmq.jmsspi.JMSAdmin;
import com.sun.messaging.jmq.jmsspi.JMSAdminFactory;
import com.sun.messaging.jmq.jmsspi.JMSConstants;

//config imports
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.admin.MBeanHelper;
import com.sun.enterprise.admin.config.BaseConfigMBean;
import com.sun.enterprise.admin.config.ConfigMBeanHelper;
import com.sun.enterprise.admin.config.MBeanConfigException;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.admin.common.exception.AFResourceException;

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
import com.sun.enterprise.config.serverbeans.JmsService;
import com.sun.enterprise.config.serverbeans.JmsHost;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ResourceHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;
import com.sun.enterprise.config.ConfigBeansFactory;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Server;

// For 8.1 event
import com.sun.enterprise.admin.event.EventBuilder;
import com.sun.enterprise.admin.event.EventContext;
import com.sun.enterprise.admin.event.ConfigChangeEvent;
import com.sun.enterprise.admin.event.BaseDeployEvent;

// For 8.0 JMS implementation
import javax.resource.ResourceException;
import javax.jms.JMSException;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.ConnectorConstants;
import com.sun.enterprise.connectors.util.JmsRaMapping;
import com.sun.enterprise.connectors.util.ConnectionDefinitionUtils;
import com.sun.enterprise.resource.Resource;
import com.sun.enterprise.resource.ResourcesXMLParser;
import com.sun.enterprise.admin.common.exception.JMSAdminException;
import com.sun.enterprise.admin.common.constant.JMSAdminConstants;
import com.sun.enterprise.admin.common.JMSStatus;
import com.sun.enterprise.admin.common.JMSDestinationInfo;

import com.sun.enterprise.server.ApplicationServer;

import com.sun.enterprise.admin.mbeanapi.IResourcesMBean;

import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.TargetBuilder;

import com.sun.enterprise.admin.configbeans.ResourceReferenceHelper;

public class ResourcesMBean extends BaseConfigMBean
    implements IResourcesMBean
{
    private static final String CUSTOM_RESOURCE          = ServerTags.CUSTOM_RESOURCE;
    private static final String JDBC_CONNECTION_POOL     = ServerTags.JDBC_CONNECTION_POOL;
    private static final String CONNECTOR_RESOURCE       = ServerTags.CONNECTOR_RESOURCE;
    private static final String ADMIN_OBJECT_RESOURCE    = ServerTags.ADMIN_OBJECT_RESOURCE;
    private static final String JDBC_RESOURCE            = ServerTags.JDBC_RESOURCE;
    private static final String RESOURCE_ADAPTER_CONFIG  = ServerTags.RESOURCE_ADAPTER_CONFIG;
    private static final String MAIL_RESOURCE            = ServerTags.MAIL_RESOURCE;
    private static final String EXTERNAL_JNDI_RESOURCE   = ServerTags.EXTERNAL_JNDI_RESOURCE;
    private static final String CONNECTOR_CONNECTION_POOL = ServerTags.CONNECTOR_CONNECTION_POOL;
    private static final String PERSISTENCE_MANAGER_FACTORY_RESOURCE = ServerTags.PERSISTENCE_MANAGER_FACTORY_RESOURCE;
    private static final String CONNECTOR_SECURITY_MAP   = ServerTags.SECURITY_MAP;

    public static final Logger sLogger = Logger.getLogger(AdminConstants.kLoggerName);
    private static final StringManager localStrings = StringManager.getManager( ResourcesMBean.class );

    private static final String GET    = "get";
    private static final String BYNAME = "ByName";
    private static final String BYJNDINAME = "ByJndiName";    
    private static final String PMF_SUFFIX = "PM";

    // For JMS resources
    private static final String QUEUE = "javax.jms.Queue";
    private static final String TOPIC = "javax.jms.Topic";
    private static final String QUEUE_CF = "javax.jms.QueueConnectionFactory";
    private static final String TOPIC_CF = "javax.jms.TopicConnectionFactory";
    private static final String UNIFIED_CF = "javax.jms.ConnectionFactory";

    private static final String RESOURCES      = "resources";
    private static final String POOL_NAME      = "pool-name";
    private static final String POOL_TYPE       = "connector-connection-pool";
    private static final String CONFIG         = "config";

    //operations
    private static final String CREATE_SECURITY_MAP = "createSecurityMap";

    //JMS constants....
    private static final String DEST_NAME = "destName";
    private static final String DEST_TYPE = "destType";

    //JMS destination resource properties
    private static final String NAME = "Name";
    private static final String IMQ_DESTINATION_NAME = "imqDestinationName";

    /**
     * Valid targets for the resource mbeans are 1) domain,
     * 2) a named server instance 3) a named cluster
     *
     * FIXTHIS: This needs to be made pluggable.
     */
    private static final TargetType[] VALID_CREATE_DELETE_TYPES = new TargetType[] {
        TargetType.CONFIG, 
	TargetType.SERVER, 
	TargetType.DOMAIN, 
	TargetType.CLUSTER, 
	TargetType.STANDALONE_SERVER, 
	TargetType.UNCLUSTERED_SERVER, 
	TargetType.STANDALONE_CLUSTER, 
	TargetType.DAS};
        
    private static final TargetType[] VALID_LIST_TYPES = new TargetType[] {
        TargetType.CONFIG, 
	TargetType.SERVER, 
	TargetType.DOMAIN, 
	TargetType.CLUSTER, 
	TargetType.STANDALONE_SERVER, 
	TargetType.UNCLUSTERED_SERVER, 
	TargetType.STANDALONE_CLUSTER, 
	TargetType.DAS};

    /**
     */
    public ResourcesMBean()
    {
        super();
    }
    
    private ResourceReferenceHelper getResourceReferenceHelper()
    {
        return new ResourceReferenceHelper(getConfigContext());
    }
    
    /**
     * Returns an array of all system resource adapters that allow connector
     * connection pool creation.
     * Presently we need this separate method as the system resource adapters are
     * not registered in the config. this method will not be required if the system
     * resource adapters (connector modules) will have entry in domain.xml
     * @return an array of names of system RARs that allow pool creation.
     */
    public String[] getSystemConnectorsAllowingPoolCreation() {
            return ConnectorRuntime.getRuntime().getSystemConnectorsAllowingPoolCreation();
    }
    
    /**
     */
    public ObjectName createCustomResource( AttributeList   attrList,
                                            Properties      props,
                                            String          tgtName )
        throws Exception
    {
        return createResource( CUSTOM_RESOURCE, attrList, props, tgtName );
    }

    public ObjectName createExternalJndiResource( AttributeList   attrList,
                                                  Properties      props,
                                                  String          tgtName)
        throws Exception
    {
        return createResource( EXTERNAL_JNDI_RESOURCE, attrList, props, tgtName );
    }

    public ObjectName createPersistenceManagerFactoryResource( AttributeList   attrList,
                                                               Properties      props,
                                                               String          tgtName)
        throws Exception
    {
        return createResource( PERSISTENCE_MANAGER_FACTORY_RESOURCE, attrList, props, tgtName );
    }

    public ObjectName createPMFResourceWithJDBCResource( AttributeList   attrList,
                                                         Properties      props,
                                                         String          tgtName)
        throws Exception
    {
        // create jdbc-resource with the given jdbc-connection-pool
        String poolName = null;
        String jndiName = null;
        int indx = 0;
        
        for (int i=0; i<attrList.size(); i++)
        {
            Attribute attr = (Attribute)attrList.get(i);
            if (isAttrNameMatch(attr, "pool-name")) {
                poolName = (String)attr.getValue();
                indx = i;
            }
            if (isAttrNameMatch(attr, "jndi-name"))
                jndiName = (String)attr.getValue();
        }
        
        if (poolName != null && !(poolName.equals(""))) {
            jndiName = jndiName + PMF_SUFFIX; // Add PM suffix to jndiName to indicate it has been created with PMF.
            AttributeList jdbcAttrList = new AttributeList();
            jdbcAttrList.add(new Attribute("jndi_name",  (Object)jndiName));
            jdbcAttrList.add(new Attribute("pool_name",  (Object)poolName));

            createResource( JDBC_RESOURCE, jdbcAttrList, null, tgtName );

            // Remove the connection-pool name from the attrib list for pmf as pmf creation doesn't require it.
            attrList.remove(indx);
            // Add the jdbc-resource-jndi-name in the attrib list for pmf resource.
            attrList.add(new Attribute("jdbc_resource_jndi_name",  (Object)jndiName));
        }
        
        return createResource( PERSISTENCE_MANAGER_FACTORY_RESOURCE, attrList, props, tgtName );
    }

    public ObjectName createJmsConnectionFactory( AttributeList   attrList,
                                                  Properties      props,
                                                  String          tgtName)
        throws Exception
    {
        return createJmsResource(attrList, props, tgtName);
    }

    public ObjectName createJmsDestinationResource( AttributeList   attrList,
                                                    Properties      props,
                                                    String          tgtName)
        throws Exception
    {
        return createJmsResource(attrList, props, tgtName);
    }
    
    //this method is added to provide backward compartibility with old JMS prop names (for GUI)
    public String getJmsRaMappedName(String name) {
	return ConnectorRuntime.getRuntime().getJmsRaMapping().getMappedName(name);
    }

    public ObjectName createJmsResource( AttributeList   attrList,
                                         Properties      props,
                                         String          tgtName)
        throws Exception
    {
        sLogger.info("createJmsResource -------------------" );
        ObjectName mbean = null;
        Properties properties = new Properties();

        Target target = getResourceTarget(tgtName);

//        try {
            JmsRaMapping ramap = ConnectorRuntime.getRuntime().getJmsRaMapping();

            /* Map MQ properties to Resource adapter properties */
            if (props != null) {
                Enumeration en = props.keys();
                while (en.hasMoreElements()) {
                    String key = (String) en.nextElement();
                    String raKey = ramap.getMappedName(key);
                    if (raKey == null) raKey = key;
                    properties.put(raKey, (String) props.get(key));
                }
            }

            // Get the default res adapter name from Connector-runtime
            String raName = ConnectorRuntime.DEFAULT_JMS_ADAPTER;

            // Find out the jndiName & resourceType from the attributeList.
            String resourceType = null;
            String jndiName = null;
            String description = null;
            Object enabled = null;
            String steadyPoolSize = null;
            String maxPoolSize = null;
            String poolResizeQuantity = null;
            String idleTimeoutInSecs = null;
            String maxWaitTimeInMillis = null;
	    String failAllConnections = null;
	    String transactionSupport = null;

            for (int i=0; i<attrList.size(); i++)
            {
                Attribute attr = (Attribute)attrList.get(i);
                if (isAttrNameMatch(attr, "res-type"))
                    resourceType = (String)attr.getValue();
                else if (isAttrNameMatch(attr, "jndi-name"))
                    jndiName = (String)attr.getValue();
                else if (isAttrNameMatch(attr, "enabled"))
                    enabled = attr.getValue();
                else if (isAttrNameMatch(attr, "description"))
                    description = (String)attr.getValue();
                else if (isAttrNameMatch(attr, "steady-pool-size"))
                    steadyPoolSize = (String) attr.getValue();
                else if (isAttrNameMatch(attr, "max-pool-size"))
                    maxPoolSize = (String) attr.getValue();
                else if (isAttrNameMatch(attr, "pool-resize-quantity"))
                    poolResizeQuantity = (String) attr.getValue();
                else if (isAttrNameMatch(attr, "idle-timeout-in-seconds"))
                    idleTimeoutInSecs = (String) attr.getValue();
                else if (isAttrNameMatch(attr, "max-wait-time-in-millis"))
                    maxWaitTimeInMillis = (String) attr.getValue();
		else if (isAttrNameMatch(attr, "transaction-support"))
		    transactionSupport = (String) attr.getValue();
		else if(isAttrNameMatch(attr, "fail-all-connections"))
		    failAllConnections = (String) attr.getValue();

                 
            }

            if (resourceType == null)
                throw new Exception(localStrings.getString("admin.mbeans.rmb.null_res_type"));

            ObjectName resObjName = m_registry.getMbeanObjectName(RESOURCES, new String[]{getDomainName()});            
            if (resourceType.equals(TOPIC_CF) || resourceType.equals(QUEUE_CF) || resourceType.equals(UNIFIED_CF))
            {
                // Add a connector-connection-pool & a connector-resource

                String defPoolName = ConnectorRuntime.getRuntime().getDefaultPoolName(jndiName);

                // Check for existence of this connection pool
                ObjectName connPool = null;
                try {
                    connPool = (ObjectName)getMBeanServer().invoke(resObjName, "getConnectorConnectionPoolByName",
                            new Object[] {defPoolName, tgtName},
                            new String[] {"java.lang.String", "java.lang.String"});
                } catch (Exception ee){}

                // If pool is already existing, do not try to create it again
                if (connPool == null) {
                    // Add connector-connection-pool.
                    AttributeList cpAttrList = new AttributeList();
                    cpAttrList.add(new Attribute("name",  (Object)defPoolName));
                    cpAttrList.add(new Attribute("resource_adapter_name",  (Object)raName));
                    cpAttrList.add(new Attribute("connection_definition_name",  (Object)resourceType));
                    cpAttrList.add(new Attribute("max_pool_size",  (maxPoolSize == null) ? (Object)"250" : (Object)maxPoolSize));
                    cpAttrList.add(new Attribute("steady_pool_size",  (steadyPoolSize == null) ? (Object)"1" : (Object)steadyPoolSize));
                    if (poolResizeQuantity != null) {
                        cpAttrList.add(new Attribute("pool_resize_quantity", (Object)poolResizeQuantity));
                    }
                    if (idleTimeoutInSecs != null) {
                        cpAttrList.add(new Attribute("idle_timeout_in_seconds", (Object)idleTimeoutInSecs));
                    }
                    if (maxWaitTimeInMillis != null) {
                        cpAttrList.add(new Attribute("max_wait_time_in_millis", (Object)maxWaitTimeInMillis));
                    }
		    if (failAllConnections != null) {
		        cpAttrList.add(new Attribute("fail-all-connections",(Object)failAllConnections));
		    }
		    if (transactionSupport != null) {
		        cpAttrList.add(new Attribute("transaction-support", (Object)transactionSupport));
		    }


                    getMBeanServer().invoke(resObjName, "createConnectorConnectionPool",
                                new Object[] {cpAttrList, properties, tgtName},
                                new String[] {AttributeList.class.getName(), Properties.class.getName(), "java.lang.String"});
                }

                // Add connector-resource
                AttributeList crAttrList = new AttributeList();
                crAttrList.add(new Attribute("jndi_name",  (Object)jndiName));
                crAttrList.add(new Attribute("pool_name",  (Object)defPoolName));
                if(enabled!=null)
                    crAttrList.add(new Attribute("enabled",  enabled));
                if(description!=null)
                    crAttrList.add(new Attribute("description",  description));
                try {
                    mbean = (ObjectName)getMBeanServer().invoke(resObjName, "createConnectorResource",
                                new Object[] {crAttrList, null, tgtName},
                                new String[] {AttributeList.class.getName(), Properties.class.getName(), "java.lang.String"});
                } catch (MBeanException me) {
                    me.printStackTrace();
                    // Rollback the change of connector-connection-pool creation
                    // delete pool only if it was created in this method
                    if(connPool==null)
                    {
                            getMBeanServer().invoke(resObjName, "deleteConnectorConnectionPool",
                                new Object[] {defPoolName, tgtName},
                                new String[] {"java.lang.String", "java.lang.String"});
                    }
                    throw me;
                }
            }
            else if (resourceType.equals("javax.jms.Topic") ||
                    resourceType.equals("javax.jms.Queue"))
            {
            	//validate the provided properties and modify it if required.
                properties =  validateDestinationResourceProps(properties, jndiName);
                
                // create admin object
                AttributeList aoAttrList = new AttributeList();
                aoAttrList.add(new Attribute("jndi_name",  (Object)jndiName));
                aoAttrList.add(new Attribute("res_type",  (Object)resourceType));
                aoAttrList.add(new Attribute("res_adapter",  (Object)raName));
                if(enabled!=null)
                    aoAttrList.add(new Attribute("enabled",  enabled));
                if(description!=null)
                    aoAttrList.add(new Attribute("description",  description));
                sLogger.info("props = " + properties);

                mbean = (ObjectName)getMBeanServer().invoke(resObjName, "createAdminObjectResource",
                            new Object[] {aoAttrList, properties, tgtName},
                            new String[] {AttributeList.class.getName(), Properties.class.getName(), "java.lang.String"});
            } else {
                throw new Exception(localStrings.getString("admin.mbeans.rmb.invalid_res_type", resourceType));
            }
/*        }
        catch (MBeanException me) {
            throw me;
        }
        catch (Exception e) {
            throw new MBeanException(e, e.getLocalizedMessage());
        }
*/
        return mbean;
    }
    
    /**
     * Validates the properties specified for a Destination Resource
     * and returns a validated Properties list.
     * 
     * NOTE: When "Name" property has not been specified by the user,
     * the properties object is updated with a computed Name.
     */
    private Properties validateDestinationResourceProps(Properties props, 
    		String jndiName) throws Exception {
    	sLogger.fine("ResourcesMBean: validateDest(" +
    			"props=" + props + " jndiName=" + jndiName + ")");
    	
        String providedDestinationName = getProvidedDestinationName(props);
        sLogger.fine("provided destination name =  " 
        		+ providedDestinationName);
        if (providedDestinationName != null) {
        	//check validity of provided JMS destination namei
            if (!isSyntaxValid(providedDestinationName)) {
                throw new Exception(localStrings.getString(
                      "admin.mbeans.rmb.destination_name_invalid",
                      jndiName, providedDestinationName));
            }
	} else {
            //compute a valid destination name from the JNDI name.
            String newDestName = computeDestinationName(jndiName);
            sLogger.log(Level.WARNING, 
        	"admin.mbeans.rmb.destination_name_missing", 
        	new Object[]{jndiName, newDestName});
        	
            props.put(NAME, newDestName);
            sLogger.fine("Computed destination name" + newDestName 
                                            + " and updated props");
        }
    	return props;
    }
    
    /**
     * Get the physical destination name provided by the user. The "Name" 
     * and "imqDestinationName" properties are used to link a JMS destination
     * resource to its physical destination in SJSMQ.
     */
    private String getProvidedDestinationName(Properties props) {
        for (Enumeration e = props.keys() ; e.hasMoreElements() ;) {
            String key = (String)e.nextElement();
            String value = (String)props.get(key);
            if(NAME.equals(key) || IMQ_DESTINATION_NAME.equals(key)){
                if (value != null && value.length() != 0) return value;
            }
        }
        return null;
    }

    private boolean isSyntaxValid(String name) {
        char[] namechars = name.toCharArray();
        if (Character.isJavaIdentifierStart(namechars[0])) {
            for (int i = 1; i<namechars.length; i++) {
                if (!Character.isJavaIdentifierPart(namechars[i])) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
	
    /**
     * Derive a destination name, valid as per MQ destination naming rules,
     * from the JNDI name provided for the JMS destination resource.
     * 
     * Scheme: merely replace all invalid identifiers in the JNDI name with
     * an 'underscore'.
     */
    private String computeDestinationName(String providedJndiName) {
    	char[] jndiName = providedJndiName.toCharArray();
        char[] finalName = new char[jndiName.length];
        finalName[0] = Character.isJavaIdentifierStart(jndiName[0]) ? jndiName[0] : '_';
        for (int i = 1; i < jndiName.length; i++) {
        	finalName[i] = Character.isJavaIdentifierPart(jndiName[i])? jndiName[i] : '_';
        }
        return new String(finalName);
    }
	
    public ObjectName createJdbcConnectionPool( AttributeList   attrList,
                                                Properties      props,
                                                String          tgtName)
        throws Exception
    {
        return createResource( JDBC_CONNECTION_POOL, attrList, props, tgtName );
    }

    public ObjectName createConnectorConnectionPool( AttributeList   attrList,
                                                     Properties      props,
                                                     String          tgtName)
        throws Exception
    {
        //validateCnctorConnPoolAttrList(attrList);
        return createResource( CONNECTOR_CONNECTION_POOL, attrList, props, tgtName );
    }

    public ObjectName createConnectorResource( AttributeList   attrList,
                                               Properties      props,
                                               String          tgtName)
        throws Exception
    {
        return createResource( CONNECTOR_RESOURCE, attrList, props, tgtName );
    }

    public ObjectName createAdminObjectResource( AttributeList   attrList,
                                                 Properties      props,
                                                 String          tgtName)
        throws Exception
    {
        String raName = null;
        String resType = null;
        boolean validResType = false;

        // Find out the value of raname attribute in the attrList
        for (int i=0; i<attrList.size(); i++)
        {
            Attribute attr = (Attribute)attrList.get(i);

            if (isAttrNameMatch(attr, "res-adapter")) {
                raName = (String)attr.getValue();
            }
            if (isAttrNameMatch(attr, "res-type")) {
                resType = (String)attr.getValue();
            }
        }

        if (isValidRAName(raName)) {
            // Check if the restype is valid -
            // To check this, we need to get the list of admin-object-interface 
            // names and then find out if this list contains the restype.
            String[] resTypes = ConnectorRuntime.getRuntime().getAdminObjectInterfaceNames(raName);
            if (resTypes == null || resTypes.length <= 0)
                throw new Exception(localStrings.getString("admin.mbeans.rmb.null_ao_intf", raName));
            
            for (int i=0; i<resTypes.length; i++) {
                if (resTypes[i].equals(resType)) {
                    validResType = true;
                    break;
                }
            }

            if (!validResType) {
                throw new Exception(localStrings.getString("admin.mbeans.rmb.invalid_res_type", resType));
            }

            return createResource( ADMIN_OBJECT_RESOURCE, attrList, props, tgtName );
        }
        else return null;
    }

    public ObjectName createJdbcResource( AttributeList   attrList,
                                          Properties      props,
                                          String          tgtName)
        throws Exception
    {
        // fix for bug# 6531767
        TargetType[] VALID_CREATE_DELETE_TYPES_JDBCRES = new TargetType[] {
                TargetType.CLUSTER, TargetType.UNCLUSTERED_SERVER, 
                TargetType.DAS, TargetType.DOMAIN};
        Target target = TargetBuilder.INSTANCE.createTarget(
            VALID_CREATE_DELETE_TYPES_JDBCRES,  
            tgtName, getConfigContext());

        return createResource( JDBC_RESOURCE, attrList, props, tgtName );
    }

    public ObjectName createMailResource( AttributeList   attrList,
                                          Properties      props,
                                          String          tgtName)
        throws Exception
    {
        return createResource( MAIL_RESOURCE, attrList, props, tgtName );
    }

    public ObjectName createResourceAdapterConfig( AttributeList   attrList,
                                                   Properties      props,
                                                   String          tgtName)
        throws Exception
    {
        final ObjectName on = createResource( RESOURCE_ADAPTER_CONFIG, attrList, props, tgtName );

        return on;
    }

    private ObjectName createResource( String resourceType,
                                       AttributeList attrList,
                                       Properties props,
            String tgtName ) throws Exception {
        return createResource(resourceType, attrList, props, tgtName,true);        
    }
    
    private ObjectName createResource( String resourceType,
                                       AttributeList attrList,
                                       Properties props,
                                       String tgtName,
                                       boolean createResourceRefs )
        throws Exception
    {
        if (resourceType.equals(CONNECTOR_CONNECTION_POOL))
             validateCnctorConnPoolAttrList(attrList);

        
        ObjectName mbean = null;      

        final Target target = TargetBuilder.INSTANCE.createTarget(
            VALID_CREATE_DELETE_TYPES,  
            tgtName, getConfigContext()); // This method call fails as getConfigContext is returning null.          
        boolean hasReferences = isResourceReferenceValid(resourceType, target.getType());

        boolean enabled = true;
        String resName = getResourceName(resourceType, attrList);
        int  idxEnabled = -1;
        if (attrList != null)
        {
            for(int i=0; i<attrList.size(); i++)
            {
                Attribute attr = (Attribute)attrList.get(i);
                if (attr.getName().equals("enabled"))
                {
                    enabled = Boolean.valueOf(attr.getValue().toString()).booleanValue();
                    idxEnabled = i;
                }
            }
        }
        

        Attribute saveEnabledAtttr = null;
        if(!enabled && hasReferences)
        { //change original "enabled" value to true
            saveEnabledAtttr  = (Attribute)attrList.get(idxEnabled);
            attrList.set(idxEnabled, new Attribute("enabled", "true"));
        }
        mbean = (ObjectName)super.invoke( "create" + ConfigMBeanHelper.convertTagName(resourceType),
                    new Object[] {attrList},
                    new String[] {AttributeList.class.getName()});

        if(!enabled && hasReferences)
        { //restore original enabled value
            attrList.set(idxEnabled, saveEnabledAtttr);
        }
                    
        // Add properties
        if (props != null)
        {
            setProperties(mbean, props);
        }

        /*
            No need to create resource-ref if 
            - resource is a jdbc-connection-pool or connector-connection-pool
              or resource-adapter-config
            - target is domain
         */
        if (!hasReferences)
        {
            return mbean;
        }

        // If target is server or cluster, resource-ref should also be
        // created inside server or group/cluster element
        if (target.getType() == TargetType.SERVER || 
            target.getType() == TargetType.CLUSTER ||
            target.getType() == TargetType.DAS)                
        {                
            if ( (resName != null) && !(resName.equals("")) && createResourceRefs )
            {
                //System.out.println("ResourcesMBean - default creation of resource refs "+ target.getName() + " " + resName);
                getResourceReferenceHelper().createResourceReference(
                    target.getName(), enabled, resName);
            }                         
        }
        return mbean;
    }

    /**
     * Checks to see if resourcerefs are valid and can be created for
     * the given <code>resourceType</code> in a <code>TargetType</code>
     * @return true if resource references are valid and can be created for
     * the passed in resourceType and targetType.
     */
    private boolean isResourceReferenceValid(String resourceType, TargetType targetType) {
        boolean hasReferences = (!( resourceType.equals(CONNECTOR_CONNECTION_POOL)  ||
                resourceType.equals(JDBC_CONNECTION_POOL)       ||
                resourceType.equals(RESOURCE_ADAPTER_CONFIG)    ||
                resourceType.equals(CONNECTOR_SECURITY_MAP)     ||
                (targetType == TargetType.DOMAIN)));
        return hasReferences;
    }

    /**
     * Gets the name of the resource from the attribute list associated with the
     * resource.
     */
    private String getResourceName(String resourceType, AttributeList attrList) {
        if (attrList != null) {
            if (resourceType.equals(RESOURCE_ADAPTER_CONFIG)) {
                for(int i=0; i<attrList.size(); i++) {
                    Attribute attr = (Attribute)attrList.get(i);
                    if ( attr.getName().equals("resource-adapter-name") ){
                        return (String)attr.getValue();
                    }
                }
            } else {
                for(int i=0; i<attrList.size(); i++) {
                    Attribute attr = (Attribute)attrList.get(i);
                    //resource-adapter-name is for RA configs.
                    if (attr.getName().equals("name") || attr.getName().equals("jndi_name") ||
                                    attr.getName().equals("jndi-name")) {
                                return (String)attr.getValue();
                    }
                }
            }
        }
        return "";
    }
    
    //FIXME: this method is copied from EE Resources MBean to support AMXBean calls
    // redundancy should be resolved in 8.2
    public void createResourceReference(String targetName, boolean enabled,
        String referenceName) throws ConfigException
    {       
        //System.out.println("ResourcesMBean: createResourceRef" + targetName + " " + referenceName);
        getResourceReferenceHelper().createResourceReference(
                targetName, enabled, referenceName); 
    }
           
    /**
     * Creates a Resource Reference for the resource passed in the targe
     * @param resource <code>Resource</code> object for which the resource-ref
     * needs to be created.
     * @throws ConfigException
     */
    public void createResourceReference(String targetName, boolean enabled,
                    Resource resource) throws ConfigException {
        this.createResourceReference(targetName, enabled, getResourceName(resource.getType(), resource.getAttributes()));
    }
    
    /**
     * Creating resources from sun-resources.xml file. This API
     * is used by the deployment backend to create resource refs
     * @param resources An iterator that returns the resources for which the resource
     *                              refs needs to be created
     * @param tgtName target in which the resource ref needs to be created
     * @param isEnabled enables/disables the resource ref
     */
    public void createResourceReference(List<Resource> res, List<String> tgtNames, 
                                                           Boolean isEnabled)  throws Exception {
        Iterator<Resource> resources = res.iterator();
        //@todo: handle isEnabled. This should override the enabled flag specified 
        //in the individual Resources.
        while (resources.hasNext()) {
            createAResourceReference(resources.next(), tgtNames, isEnabled);
        }
    }
    
    private void createAResourceReference(Resource res, List<String> tgtNames, Boolean isEnabled) 
                                                           throws Exception {
        for (String target : tgtNames) {
            createAResourceReference(res, target, isEnabled);
        }
    }
    
    private void createAResourceReference(Resource res, String target, Boolean isEnabled) 
    throws Exception {
        TargetType targetType = getResourceTarget(target).getType();
        String resourceType = res.getType();
        if (isResourceReferenceValid(resourceType,targetType)) {
            createResourceReference(target, isEnabled.booleanValue(), getResourceName(res.getType(), res.getAttributes()));
        }
    }
    
    /**
     * Deletes the  Resource Reference for the resource passed in the target
     * @param targetName target from which the resource ref needs to be deleted 
     * @param resource <code>Resource</code> object for which the resource-ref
     * needs to be deleted.
     * @throws Exception 
     */
    public void deleteResourceReference(String targetName, Resource resource) throws Exception {
        Target target = getResourceTarget(targetName);
        this.deleteResourceRef(getResourceName(resource.getType(), resource.getAttributes()), target, targetName);
    }
    
    /**
     * Deletes the  Resource Reference for the resource passed in the target
     * @param targetName target from which the resource ref needs to be deleted 
     * @param resource <code>Resource</code> object for which the resource-ref
     * needs to be deleted.
     * @throws Exception 
     */
    public void deleteResourceReference(List<Resource> res, List<String> targetNames) throws Exception {
        //System.out.println("Dlete resource refs in refs");
        int size = res.size();
        //@todo: modify this to simplify the loop logic
        for (int i = (size -1); i >= 0 ; i--) {
            Resource resource = res.get(i);
            try{
                deleteAResourceRef(resource, targetNames);
            }catch(Exception ex){
                String s = localStrings.getString("unable.delete.resource.ref", resource.toString());
                sLogger.log(Level.WARNING, s);
            }
        }
    }
    

    private void deleteAResourceRef(Resource resource, List<String> targetNames) throws Exception {
        //System.out.println("ResourcesMBean deleteAResourceRef " + resource.getType()); 
        for (String tgt : targetNames) {
            Target target = getResourceTarget(tgt);
            if (isResourceReferenceValid(resource.getType(),target.getType())) {
                this.deleteResourceRef(getResourceName(resource.getType(), resource.getAttributes()), target, tgt);
            }
        }
    }
    
    
    private void setProperties(ObjectName objName, Properties props)
        throws Exception
    {
            if ( props != null )
            {
                Enumeration keys = props.keys();
                while (keys.hasMoreElements())
                {
                    final String key = (String)keys.nextElement();
                    final Attribute property =  new Attribute(key, props.get(key));
                    getMBeanServer().invoke(objName, "setProperty", new Object[]{property}, new String[]{Attribute.class.getName()});
                }
            }
    }

    private boolean isValidRAName(String raName) throws Exception
    {
        boolean retVal = false;

//        try {
            if ((raName == null) || (raName.equals("")))
                throw new Exception(localStrings.getString("admin.mbeans.rmb.null_res_adapter"));

            // To check for embedded conenctor module
            if (raName.equals(ConnectorRuntime.DEFAULT_JMS_ADAPTER) || raName.equals(ConnectorRuntime.JAXR_RA_NAME)) {
                // System RA, so don't validate
                retVal = true;
            } else {
                // Check if the raName contains double underscore or hash.
                // If that is the case then this is the case of an embedded rar,
                // hence look for the application which embeds this rar,
                // otherwise look for the webconnector module with this raName.

                ObjectName applnObjName = m_registry.getMbeanObjectName(ServerTags.APPLICATIONS, new String[]{getDomainName()});
                int indx = raName.indexOf(
                       ConnectorConstants.EMBEDDEDRAR_NAME_DELIMITER);
                if (indx != -1) {
                    String appName = raName.substring(0, indx);
                    ObjectName j2eeAppObjName = (ObjectName)getMBeanServer().invoke(applnObjName, "getJ2eeApplicationByName", new Object[]{appName}, new String[]{"java.lang.String"});

                    if (j2eeAppObjName == null) 
                        throw new Exception(localStrings.getString("admin.mbeans.rmb.invalid_ra_app_not_found", appName));
                    else retVal = true;
                } else {
                    ObjectName connectorModuleObjName = (ObjectName)getMBeanServer().invoke(applnObjName, "getConnectorModuleByName", new Object[]{raName}, new String[]{"java.lang.String"});

                    if (connectorModuleObjName == null) 
                        throw new Exception(localStrings.getString("admin.mbeans.rmb.invalid_ra_cm_not_found", raName));
                    else retVal = true;
                }
            }
/*        }
        catch (javax.management.MBeanException me) {
            throw me;
        }
        catch (Exception e) {
            throw new MBeanException(e, e.getLocalizedMessage());
        }
*/
        return retVal;
    }



    /**
     *
     */
    private void validateCnctorConnPoolAttrList(AttributeList list) throws Exception {
        String raName = getAttributeValueFromList("resource_adapter_name",list);
        String connDef = getAttributeValueFromList("connection_definition_name",list);
        if(isValidRAName(raName)) {
            if(!isValidConnectionDefinition(connDef,raName)) {
                throw new Exception(localStrings.getString(
                        "admin.mbeans.rmb.invalid_ra_connectdef_not_found",connDef));
            }
        }
    }

    /**
     *
     */
    private boolean isValidConnectionDefinition(String connectionDef,String raName) throws Exception{
        String [] names =
                ConnectorRuntime.getRuntime().getConnectionDefinitionNames(raName);
        for(int i = 0; i < names.length; i++) {
            if(names[i].equals(connectionDef)) {
                return true;
            }
        }
        return false;
    }


    /**
     *
     */
    private String getAttributeValueFromList(String name, AttributeList list) {
        for (int i=0; i<list.size(); i++) {
            Attribute attr = (Attribute)list.get(i);
            if (isAttrNameMatch(attr, name)) {
                return (String)attr.getValue();
            }
        }
        return "";
    }


    /**
     * GETTER Methods for resources
     */
    public ObjectName getCustomResourceByJndiName( String key, String tgtName )
        throws Exception
    {
        return getResourceByName( CUSTOM_RESOURCE, key, tgtName );
    }

    public ObjectName getJdbcResourceByJndiName( String key, String tgtName )
        throws Exception
    {
        return getResourceByName( JDBC_RESOURCE, key, tgtName );
    }

    public ObjectName getJdbcConnectionPoolByName( String key, String tgtName )
        throws Exception
    {
        return getResourceByName( JDBC_CONNECTION_POOL, key, tgtName );
    }

    public ObjectName getExternalJndiResourceByJndiName( String key, String tgtName )
        throws Exception
    {
        return getResourceByName( EXTERNAL_JNDI_RESOURCE, key, tgtName );
    }

    public ObjectName getMailResourceByJndiName( String key, String tgtName )
        throws Exception
    {
        return getResourceByName( MAIL_RESOURCE, key, tgtName );
    }

    public ObjectName getConnectorResourceByJndiName( String key, String tgtName )
        throws Exception
    {
        return getResourceByName( CONNECTOR_RESOURCE, key, tgtName );
    }

    public ObjectName getResourceAdapterConfigByResourceAdapterName( String key, String tgtName )
        throws Exception
    {
        return getResourceByName( RESOURCE_ADAPTER_CONFIG, key, tgtName );
    }

    public ObjectName getAdminObjectResourceByJndiName( String key, String tgtName )
        throws Exception
    {
        return getResourceByName( ADMIN_OBJECT_RESOURCE, key, tgtName );
    }

    public ObjectName getPersistenceManagerFactoryResourceByJndiName( String key, String tgtName )
        throws Exception
    {
        return getResourceByName( PERSISTENCE_MANAGER_FACTORY_RESOURCE, key, tgtName );
    }

    public ObjectName getConnectorConnectionPoolByName( String key, String tgtName )
        throws Exception
    {
        return getResourceByName( CONNECTOR_CONNECTION_POOL, key, tgtName );
    }

    private ObjectName getResourceByName( String resType, String key, String tgtName )
        throws Exception
    {
        ObjectName mbean = null;        

//        try
        {
            final Target target = TargetBuilder.INSTANCE.createTarget(
                VALID_LIST_TYPES, tgtName, getConfigContext());
            String opName = "";
            if (resType.equals(RESOURCE_ADAPTER_CONFIG)) {
                // opName is getResourceAdapterConfigByResourceAdapterName
                opName = GET + ConfigMBeanHelper.convertTagName(resType) + "By" + ConfigMBeanHelper.convertTagName(ServerTags.RESOURCE_ADAPTER_NAME);
            } else if (resType.equals(JDBC_CONNECTION_POOL) || resType.equals(CONNECTOR_CONNECTION_POOL)) {
                // opName is getJdbcConnectionPoolByName or getConnectorConnectionPoolByName
                opName = GET + ConfigMBeanHelper.convertTagName(resType) + BYNAME;
            } else opName = GET + ConfigMBeanHelper.convertTagName(resType) + BYJNDINAME;

            mbean = (ObjectName)super.invoke(opName, new Object[] {key},
                        new String[] {key.getClass().getName()});
        }
/*        catch (MBeanException me)
        {
            throw me;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new MBeanException(e, e.getLocalizedMessage());
        }
*/
        return mbean;
    }

    /**
     * GETTER Methods for resources
     */
    public ObjectName[] getCustomResource( String tgtName )
        throws Exception
    {
        return getResource( CUSTOM_RESOURCE, tgtName );
    }

    public ObjectName[] getPersistenceManagerFactoryResource( String tgtName )
        throws Exception
    {
        return getResource( PERSISTENCE_MANAGER_FACTORY_RESOURCE, tgtName );
    }

    public ObjectName[] getJmsResource( String tgtName )
        throws Exception
    {
        return getJmsResource( null, tgtName );
    }

    public ObjectName[] getJmsConnectionFactory( String tgtName )
        throws Exception
    {
        ObjectName[] QRes = null;
        ObjectName[] TRes = null;
        ObjectName[] uRes = null;
        ObjectName[] retVal = null;

        QRes = getJmsResource( QUEUE_CF, tgtName );
        TRes = getJmsResource( TOPIC_CF, tgtName );
        uRes = getJmsResource( UNIFIED_CF, tgtName );

        int i = 0;
        if (QRes != null) i = QRes.length;
        if (TRes != null) i = i + TRes.length;
        if (uRes != null) i = i + uRes.length;

        if (i > 0) {
            ArrayList ret = new ArrayList();
            if (QRes != null) ret.addAll(Arrays.asList(QRes));
            if (TRes != null) ret.addAll(Arrays.asList(TRes));
            if (uRes != null) ret.addAll(Arrays.asList(uRes));

            if (ret.size() > 0) {
                    retVal = new ObjectName[ret.size()];
                    ret.toArray(retVal);
            }
        }

        return retVal;
    }

    public ObjectName[] getJmsDestinationResource( String tgtName )
        throws Exception
    {
        ObjectName[] QRes = null;
        ObjectName[] TRes = null;
        ObjectName[] retVal = null;
        QRes = getJmsResource( QUEUE, tgtName );
        TRes = getJmsResource( TOPIC, tgtName );

        int i = 0;
        if (QRes != null) i = QRes.length;
        if (TRes != null) i = i + TRes.length;

        if (i > 0) {
            ArrayList ret = new ArrayList();
            if (QRes != null) ret.addAll(Arrays.asList(QRes));
            if (TRes != null) ret.addAll(Arrays.asList(TRes));

            if (ret.size() > 0) {
                    retVal = new ObjectName[ret.size()];
                    ret.toArray(retVal);
            }
        }

        return retVal;
    }

    public ObjectName[] getJmsResource( String resType, String tgtName )
        throws Exception
    {
        Target target = getResourceTarget(tgtName);
        ObjectName[] QRes = null;
        ObjectName[] TRes = null;
        ObjectName[] QCFRes = null;
        ObjectName[] TCFRes = null;
        ObjectName[] uCFRes = null;
        ObjectName[] retVal = null;

//        try {
        if (resType == null || resType.equals("null")) {
            // All types of JMS resources are needed
            QRes = getJmsResourceForResType(QUEUE);
            TRes = getJmsResourceForResType(TOPIC);
            QCFRes = getJmsResourceForResType(QUEUE_CF);
            TCFRes = getJmsResourceForResType(TOPIC_CF);
            uCFRes = getJmsResourceForResType(UNIFIED_CF);

            int i = 0;
            if (QRes != null) i = QRes.length;
            if (TRes != null) i = i + TRes.length;
            if (QCFRes != null) i = i + QCFRes.length;
            if (TCFRes != null) i = i + TCFRes.length;
            if (uCFRes != null) i = i + uCFRes.length;
            if (i > 0) {
                ArrayList ret = new ArrayList();
                if (QRes != null) ret.addAll(Arrays.asList(QRes));
                if (TRes != null) ret.addAll(Arrays.asList(TRes));
                if (QCFRes != null) ret.addAll(Arrays.asList(QCFRes));
                if (TCFRes != null) ret.addAll(Arrays.asList(TCFRes));
                if (uCFRes != null) ret.addAll(Arrays.asList(uCFRes));

                if (ret.size() > 0) {
                    retVal = new ObjectName[ret.size()];
                    ret.toArray(retVal);
                }
            }
        } else {
            if (!(resType.equals(QUEUE) || resType.equals(TOPIC) 
               || resType.equals(QUEUE_CF) || resType.equals(TOPIC_CF) || resType.equals(UNIFIED_CF))) {
                throw new Exception(localStrings.getString("admin.mbeans.rmb.invalid_res_type", resType));
            } else {
		retVal = getJmsResourceForResType(resType);
            }
        }
/*        } 
        catch (MBeanException me)
        {
            throw me;
        }
        catch (Exception e)
        {
            throw new MBeanException(e, e.getLocalizedMessage());
        }
*/
	if ((tgtName != null) && (tgtName.trim().equals(""))) {
	   /* This basically means that someone wants the jms
 	    * jms resources for the whole domain, this case the 
	    * targetname  is empty "". If target is null then 
	    * it indicates the target to be Default server. 
	    */
	   return retVal;
	} else {
  	    /* This would filter out all the resources based on
	     * on the target. Fix for issue 6413580
	     */	
       	   return filterForTarget(retVal, target);
	}

    }

    private ObjectName[] getJmsResourceForResType( String resType )
        throws MBeanException
    {
        ObjectName[] retVal = null;
        ArrayList al = new ArrayList();
        try {
            if (resType.equals(QUEUE) || resType.equals(TOPIC)) {
                // get all admin-objects whose resType is Queue or Topic and
                // res-adapter is Default-jms-adapter
                ObjectName o = m_registry.getMbeanObjectName(RESOURCES, new String[]{getDomainName()});
                ObjectName[] adminObjNames = (ObjectName[])getMBeanServer().invoke(o, "getAdminObjectResource", null, null);
                if (adminObjNames != null) {
                    for (int i=0; i<adminObjNames.length; i++) {
                        String adminResType = (String)getMBeanServer().getAttribute(adminObjNames[i], "res_type");
                        if ((adminResType != null) && ((adminResType.equals(QUEUE) && resType.equals(QUEUE)) || (adminResType.equals(TOPIC) && resType.equals(TOPIC)))) {
                            //Check for the res-adapter
                            String adminResAdapter = (String)getMBeanServer().getAttribute(adminObjNames[i], "res_adapter");
                            if ((adminResAdapter != null) && (adminResAdapter.equals(ConnectorRuntime.DEFAULT_JMS_ADAPTER)))
                                al.add(adminObjNames[i]);
                        }
                    }
                    if (!al.isEmpty()) {
                        retVal = new ObjectName[al.size()];
                        al.toArray(retVal);
                    }
                }
            } else {
                // get all connector-resources whose connector-connection-pool
                // satisfy the criterion -
                // connection-definition-name is QCF or TCF &
                // resource-adapter-name is DEFAULT_JMS_ADAPTER
                ObjectName o = m_registry.getMbeanObjectName(RESOURCES, new String[]{getDomainName()});
                ObjectName[] CRObjNames = CRObjNames = (ObjectName[])getMBeanServer().invoke(o, "getConnectorResource", null, null);
                if (CRObjNames != null) {
                    for (int i=0; i<CRObjNames.length; i++) {
                        String poolName = (String)getMBeanServer().getAttribute(CRObjNames[i], "pool_name");
                        if (poolName != null) {
                            ObjectName CCPObjName = null;
                            try {
                               CCPObjName = (ObjectName)getMBeanServer().invoke(o, "getConnectorConnectionPoolByName", new Object[]{poolName}, new String[]{"java.lang.String"});
                            } catch (Exception ee){
                                // ignore this Exception
                            }
                            if (CCPObjName != null) {
                                String cdn = (String)getMBeanServer().getAttribute(CCPObjName, "connection_definition_name");
                                if ((cdn != null) && ((cdn.equals(resType)) || ((cdn.equals(resType))))) {
                                    //Check for the res-adapter
                                    String resAdapter = (String)getMBeanServer().getAttribute(CCPObjName, "resource_adapter_name");
                                    if ((resAdapter != null) && (resAdapter.equals(ConnectorRuntime.DEFAULT_JMS_ADAPTER)))
                                        al.add(CRObjNames[i]);
                                }
                            }
                        }
                    }

                    if (!al.isEmpty()) {
                        retVal = new ObjectName[al.size()];
                        al.toArray(retVal);
                    }
                }
            }//end of else block
        }//end of try
        catch (javax.management.InstanceNotFoundException infe) {}
        catch (javax.management.ReflectionException re) {}
        catch (javax.management.AttributeNotFoundException anf) {}

        return retVal;
    }

    /* This method filters out the mbean objects based on the target
      */
    public ObjectName [] filterForTarget(ObjectName[] mbean, Target target) { 
	ArrayList result = new ArrayList();
	try { 
	if (target.getType() == TargetType.SERVER ||
	    target.getType() == TargetType.CLUSTER  || 
	    target.getType() == TargetType.DAS) {
	    //filter out only those resources referenced by the target
            ResourceRef[] refs = target.getResourceRefs();
  	    String objectProps = null;
	    for (int i = 0; i < mbean.length; i++) {
		for (int j = 0; j < refs.length; j++) {
                    objectProps = mbean[i].getKeyPropertyListString();
	   	    if (objectProps.indexOf(refs[j].getRef()) > 0) {
			result.add(mbean[i]);
                        break;
                    }	 
		}
	     }
	     return (ObjectName[])result.toArray(new ObjectName[result.size()]);
     }
    } catch (Exception e) {
	;
     }
	return mbean;
  }
	

    public ObjectName[] getJdbcConnectionPool( String tgtName )
        throws Exception
    {
        return getResource( JDBC_CONNECTION_POOL, tgtName );
    }

    public ObjectName[] getConnectorResource( String tgtName )
        throws Exception
    {
        return getResource( CONNECTOR_RESOURCE, tgtName );
    }

    public ObjectName[] getAdminObjectResource( String tgtName )
        throws Exception
    {
        return getResource( ADMIN_OBJECT_RESOURCE, tgtName );
    }

    public ObjectName[] getConnectorConnectionPool( String tgtName )
        throws Exception
    {
        return getResource( CONNECTOR_CONNECTION_POOL, tgtName );
    }

    public ObjectName[] getJdbcResource( String tgtName )
        throws Exception
    {
        return getResource( JDBC_RESOURCE, tgtName );
    }

    public ObjectName[] getResourceAdapterConfig( String tgtName )
        throws Exception
    {
        return getResource( RESOURCE_ADAPTER_CONFIG, tgtName );
    }

    public String getResourceAdapterConfig( String resAdapterConfig, Boolean verbose, String tgtName )
        throws Exception
    {
        String retVal = null;
        StringBuffer sb = new StringBuffer();

//        try {
            if ((resAdapterConfig != null) && !(resAdapterConfig.equals("")) && !(resAdapterConfig.equals("null"))) {
                ObjectName rac = null;
                try {
                    rac = (ObjectName) super.invoke("getResourceAdapterConfigByResourceAdapterName", new Object[]{resAdapterConfig}, new String[]{"java.lang.String"});
                } catch (Exception ee){
                    // fall through -- the null test below will handle the error
                };
                if (rac == null) {
                    throw new Exception(localStrings.getString("admin.mbeans.rmb.null_rac"));
                }
                else {
                    /**
                        get the properties if verbose=true. Otherwise return the
                        name.
                     */
                    retVal = verbose.booleanValue() ? 
                        getFormattedProperties(rac) : 
                        rac.getKeyProperty("resource-adapter-name");
                }
            }
            else {
                ObjectName[] rac = getResource( RESOURCE_ADAPTER_CONFIG, tgtName );

                if (verbose.booleanValue() && rac != null && rac.length>0) {
                    for (int i=0; i<rac.length; i++) {
                        sb.append(getFormattedProperties(rac[i]));
			sb.append("\n");
                    }
                } else if(!verbose.booleanValue() && rac != null && rac.length>0) {
                    for (int i=0; i<rac.length; i++) {
                        sb.append(rac[i].getKeyProperty("resource-adapter-name"));
                        sb.append("\n");
                    }
                } else sb.append("No resource-adapter-config found.");

                retVal = sb.toString();
            }
/*        }
        catch (MBeanException me) {
            throw me;
        }
        catch (Exception e) {
            throw new MBeanException(e, e.getLocalizedMessage());
        }
*/
        return retVal;
    }

    private String getFormattedProperties(ObjectName objName) throws Exception
    {
        StringBuffer retVal = new StringBuffer();

//        try {
            if (objName == null)
                throw new Exception(localStrings.getString("admin.mbeans.rmb.null_rac"));

            retVal.append(objName.getKeyProperty("resource-adapter-name"));

            AttributeList props = (AttributeList) getMBeanServer().invoke(objName, "getProperties", null, null);

            if (!props.isEmpty()) {
                for (int i=0; i<props.size(); i++) {
                    Attribute attrib = (Attribute)props.get(i);
                    if (i==0) retVal.append("\n");
                    retVal.append("\t");
                    retVal.append(attrib.getName());
                    retVal.append("=");
                    retVal.append(attrib.getValue());
                    retVal.append("\n");
                }
            }
/*        }
        catch (Exception e) {
            e.printStackTrace();
            throw new MBeanException(e, e.getLocalizedMessage());
        }
*/
        return retVal.toString();
    }

    public ObjectName[] getMailResource( String tgtName )
        throws Exception
    {
        return getResource( MAIL_RESOURCE, tgtName );
    }

    public ObjectName[] getExternalJndiResource( String tgtName )
        throws Exception
    {
        return getResource( EXTERNAL_JNDI_RESOURCE, tgtName );
    }

    private ObjectName[] getResource( String resType, String tgtName )
       throws Exception
    {             
        ArrayList result = new ArrayList(); 

        final Target target = TargetBuilder.INSTANCE.createTarget(
            VALID_LIST_TYPES, tgtName, getConfigContext());            
        ObjectName[] mbean = (ObjectName[])super.invoke( GET + ConfigMBeanHelper.convertTagName(resType), null, null);

        /**
            resource-ref business doesnot apply to jdbc-connection-pool, 
            connector-connection-pool & resource-adapter-config. So return 
            the pools as if the target were domain. &

            If the target is domain, return all the resources of the specific
            type.
         */
        if (CONNECTOR_CONNECTION_POOL.equals(resType) || 
            JDBC_CONNECTION_POOL.equals(resType) || 
            RESOURCE_ADAPTER_CONFIG.equals(resType) || 
            (target.getType() == TargetType.DOMAIN))
        {
            return mbean;
        }


        if (target.getType() == TargetType.SERVER || 
            target.getType() == TargetType.CLUSTER ||
            target.getType() == TargetType.DAS) {
            //filter out only those resources referenced by the target            
            ResourceRef[] refs = target.getResourceRefs();                   
            String objectProps = null;
            for (int i = 0; i < mbean.length; i++) {
                for (int j = 0; j < refs.length; j++) {
                    objectProps = mbean[i].getKeyPropertyListString();                    
                    if (objectProps.indexOf(refs[j].getRef()) > 0) {
                        result.add(mbean[i]);
                        break;
                    }                    
                }
            }    
        }

        return (ObjectName[])result.toArray(new ObjectName[result.size()]);
    }

    /**
    * Delete Methods for the resources
    */
    public void deleteCustomResource( String name, String tgtName )
        throws Exception
    {
        this.deleteResource(CUSTOM_RESOURCE, name, tgtName);
    }

    public void deleteJdbcConnectionPool( String name, String tgtName )
        throws Exception
    {
        this.deleteJdbcConnectionPool(name, Boolean.valueOf(false), tgtName);
    }

    public void deleteJdbcConnectionPool( String name, Boolean cascade, String tgtName )
        throws Exception
    {
        // Passing the resType as eleteConnectionPool is common method for both
        // jdbc-connection-pool and connector-connection-pool deletion
        deleteConnectionPool(JDBC_RESOURCE, name, cascade, tgtName);
    }

	void deleteResourceRefFromTargets(String ref) throws Exception
	{
        //System.out.println("del res ref from Targets" + ref);
		final ConfigContext ctx = getConfigContext();
		final String targets = ResourceHelper.getResourceReferenceesAsString(
			ctx, ref);
		final StringTokenizer strTok = new StringTokenizer(targets, ",");
		while (strTok.hasMoreTokens())
		{
			final String target = strTok.nextToken();
			if (ServerHelper.isAServer(ctx, target) && 
				ServerHelper.isServerClustered (ctx, target))
			{
				//Skip the clustered servers.
				continue;
			}
			getResourceReferenceHelper().deleteResourceReference(
				target, ref);
		}
	}

    private void deleteConnectionPool( String resType, String name, Boolean cascade, String tgtName )
        throws Exception
    {
        // Find out if the connection-pool is being accessed by any jdbc-resource/connector-resource
        // If yes, throw MBeanException that it can't be deleted as it is being
        // referenced by jdbc-resource in case cascade is false
        boolean crFound = false;
        boolean pmfFound = false;

        Target target = getResourceTarget(tgtName);

//        try {
            String[] resArr = null;
            String[] pmfArr = null;

            String opName = GET + ConfigMBeanHelper.convertTagName(resType);
            ObjectName[] resObjNames = (ObjectName[])super.invoke(opName, new Object[]{tgtName}, new String[]{"java.lang.String"});

            if (resObjNames != null && resObjNames.length > 0)
            {
                int k=0;
                resArr = new String[resObjNames.length];
                for (int i=0; i<resObjNames.length; i++)
                {
                    String poolName = (String)getMBeanServer().getAttribute(resObjNames[i], "pool_name");
                    if (poolName.equals(name)) {
                        String jndiName = (String)getMBeanServer().getAttribute(resObjNames[i], "jndi_name");
                        resArr[k++] = jndiName;
                        crFound = true;
                    }
                }
            }

            if (resType.equalsIgnoreCase(JDBC_RESOURCE) && crFound)
            {
                // If there exists, pmf with jdbc-resource-jndi-name as the jdbc-resources' jndi-name,
                // we need to delete them too as they are indirectly connected to this conn-pool.
                String pmfOp = GET + ConfigMBeanHelper.convertTagName(PERSISTENCE_MANAGER_FACTORY_RESOURCE);
                ObjectName[] pmfObjNames = (ObjectName[])super.invoke(pmfOp, new Object[]{tgtName}, new String[]{"java.lang.String"});
                
                if (pmfObjNames != null && pmfObjNames.length > 0)
                {
                    int k=0;
                    pmfArr = new String[pmfObjNames.length];
                    for (int i=0; i<pmfObjNames.length; i++)
                    {
                        String jrjnName = (String)getMBeanServer().getAttribute(pmfObjNames[i], "jdbc_resource_jndi_name");
                        for (int j=0; j<resArr.length && resArr[j] != null; j++)
                        {
                            if (jrjnName.equals(resArr[j])) {
                                String pmfJndiName = (String)getMBeanServer().getAttribute(pmfObjNames[i], "jndi_name");
                                pmfArr[k++] = pmfJndiName;
                                pmfFound = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (!cascade.booleanValue() && (crFound || pmfFound))
            {
                if (resType.equalsIgnoreCase(JDBC_RESOURCE))
                    throw new Exception(localStrings.getString("admin.mbeans.rmb.jdbc_res_pmf_ref_exists"));
                else
                    throw new Exception(localStrings.getString("admin.mbeans.rmb.conn_res_ref_exists"));
            } else if(cascade.booleanValue()) {
                if (pmfFound && (pmfArr!=null))
                {
                    // Delete the pmf resources attached with this conn pool also
                    for (int i=0; i<pmfArr.length && pmfArr[i] != null; i++) {
                        deleteResourceRefFromTargets(pmfArr[i]);
                        boolean isDeleted = destroyResource(PERSISTENCE_MANAGER_FACTORY_RESOURCE, pmfArr[i]);
                        if (!isDeleted) {
                            throw new Exception (localStrings.getString("admin.mbeans.rmb.pmf_not_deleted", pmfArr[i]));
                        }                        
                    }
                }
                
                if (resArr != null) {
                    // Delete the jdbc/connector resources attached with this conn pool also
                    for (int i=0; i<resArr.length && resArr[i] != null; i++) {
                        deleteResourceRefFromTargets(resArr[i]);
                        boolean isDeleted = destroyResource(resType, resArr[i]);
                        if (!isDeleted) {
                            if (resType.equalsIgnoreCase(JDBC_RESOURCE))
                                throw new Exception (localStrings.getString("admin.mbeans.rmb.jdbc_res_not_deleted", resArr[i]));
                            else
                                throw new Exception (localStrings.getString("admin.mbeans.rmb.conn_res_not_deleted", resArr[i]));
                        }                        
                    }
                }
            }

            /*
                There wont be any resource-refs for jdbc & connector connection 
                pools.

                deleteResourceRef(name, target, tgtName);
             */
            if (resType.equalsIgnoreCase(JDBC_RESOURCE))
                destroyResource(JDBC_CONNECTION_POOL,name);
            else
                destroyResource(CONNECTOR_CONNECTION_POOL,name);
            
/*        }
        catch (MBeanException me) {
            throw me;
        }
        catch (Exception e) {
            throw new MBeanException(e, e.getLocalizedMessage());
        }
*/
    }

    public void deletePersistenceManagerFactoryResource( String name, String tgtName )
        throws Exception
    {
        //  *** first we remove persistence resource itself as it has references 
        //  *** to jdbc-resources and should be removed first to avoid
        //  *** Validator's reject
        this.deleteResource(PERSISTENCE_MANAGER_FACTORY_RESOURCE, name, tgtName);

        
        // If creation of pmf resource also resulted in creation of jdbc-resource,
        // then that needs to be removed. This will be identified by suffix PM.
        Resources resources = (Resources)this.getBaseConfigBean();
        if (resources.getJdbcResourceByJndiName(name + PMF_SUFFIX)!=null)
        {
           this.deleteResource(JDBC_RESOURCE, name + PMF_SUFFIX, tgtName);
        }
        
    }

    /**
      * Helper method for deleting Jms connection factory
      */
    public void deleteJmsConenctionFactory( String name, String tgtName )
        throws Exception
    {
        deleteJmsResource(name, tgtName);
    }

    /**
      * Helper method for deleting Jms destination resource
      */
    public void deleteJmsDestinationResource( String name, String tgtName )
        throws Exception
    {
        deleteJmsResource(name, tgtName);
    }

    public void deleteJmsResource( String name, String tgtName )
        throws Exception
    {
//        try {
            Target target = getResourceTarget(tgtName);
            ObjectName connResource = null;
            try {
               connResource = (ObjectName)super.invoke("getConnectorResourceByJndiName", new Object[]{name}, new String[]{"java.lang.String"});
            } catch (Exception ee){};

            if (connResource == null)
            {
                // delete any admin objects with this jndi name
                super.invoke("deleteAdminObjectResource", new Object[]{name, tgtName}, new String[]{"java.lang.String", "java.lang.String"});
            } else {
                // Delete the connector resource and connector connection pool
                String defPoolName = ConnectorRuntime.getRuntime().getDefaultPoolName(name);
                String poolName = (String) getMBeanServer().getAttribute(connResource, "pool_name");
                if (poolName != null && poolName.equals(defPoolName))
                    {
                    deleteResourceRef(name, target, tgtName);
                    // Delete both the resource and the poolname
                    destroyResource(CONNECTOR_RESOURCE, name);

                    ObjectName connPool = getObjectNameForResType(CONNECTOR_CONNECTION_POOL, poolName);
                    if (connPool != null) {
                        destroyResource(CONNECTOR_CONNECTION_POOL,poolName);
                    }                    
                }
                else
                {
                    // There is no connector pool with the default poolName.
                    // However, no need to throw exception as the connector
                    // resource might still be there. Try to delete the
                    // connector-resource without touching the ref. as
                    // ref. might have been deleted while deleting connector-connection-pool
                    // as the ref. is the same.
                    ObjectName connResMBean = getObjectNameForResType(CONNECTOR_RESOURCE, name);
                    super.invoke("removeConnectorResourceByJndiName",new Object[]{name},new String[] {"java.lang.String"});
                }
            }
/*        }
        catch (MBeanException me) {
            throw me;
        }
        catch (Exception e) {
            throw new MBeanException(e, e.getLocalizedMessage());
        }
*/
    }

    public void deleteConnectorResource( String name, String tgtName )
        throws Exception
    {
        this.deleteResource(CONNECTOR_RESOURCE, name, tgtName);
    }

    public void deleteAdminObjectResource( String name, String tgtName )
        throws Exception
    {
        this.deleteResource(ADMIN_OBJECT_RESOURCE, name, tgtName);
    }

    public void deleteConnectorConnectionPool( String name, String tgtName )
        throws Exception
    {
        this.deleteConnectorConnectionPool(name, Boolean.valueOf(false), tgtName);
    }

    public void deleteConnectorConnectionPool( String name, Boolean cascade, String tgtName )
        throws Exception
    {
        // Passing the resType as deleteConnectionPool is common method for both
        // jdbc-connection-pool and connector-connection-pool deletion
        deleteConnectionPool( CONNECTOR_RESOURCE, name, cascade, tgtName);
    }

    public void deleteJdbcResource( String name, String tgtName )
        throws Exception
    {
        this.deleteResource(JDBC_RESOURCE, name, tgtName);
    }

    public void deleteResourceAdapterConfig( String name, String tgtName )
        throws Exception
    {
        this.deleteResource(RESOURCE_ADAPTER_CONFIG, name, tgtName);
    }

    public void deleteMailResource( String name, String tgtName )
        throws Exception
    {
        this.deleteResource(MAIL_RESOURCE, name, tgtName);
    }

    public void deleteExternalJndiResource( String name, String tgtName )
        throws Exception
    {
        this.deleteResource(EXTERNAL_JNDI_RESOURCE, name, tgtName);
    }

    private Target getResourceTarget( String tgtName )
        throws Exception
    {        
//        try
//        {
            return TargetBuilder.INSTANCE.createTarget(VALID_CREATE_DELETE_TYPES,
                tgtName, getConfigContext());
/*        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new MBeanException(e, e.getLocalizedMessage());
        }
*/
    }

    private void deleteResource( String resType, String name, String tgtName )
        throws Exception
    {
        String operation = null;
        String suffix = null;

        if (resType.equals(RESOURCE_ADAPTER_CONFIG))
           suffix = "ByResourceAdapterName";
        else if (resType.equals(JDBC_CONNECTION_POOL) || resType.equals(CONNECTOR_CONNECTION_POOL))
           suffix = "ByName";
        else suffix = "ByJndiName";

        operation = "remove" + ConfigMBeanHelper.convertTagName(resType) + suffix;

        if (!(RESOURCE_ADAPTER_CONFIG.equals(resType) ||
              JDBC_CONNECTION_POOL.equals(resType) ||
              CONNECTOR_CONNECTION_POOL.equals(resType))) {
            /*
                delete the resource-refs before calling the delete for this 
                resource.
             */
            Target target = getResourceTarget(tgtName);
            deleteResourceRef(name, target, tgtName);
        }

        // Invoke removeXXXByXXXName() for the appropriate MBean
        super.invoke(operation,new Object[]{name},new String[] {"java.lang.String"});
    }

    /**
     * Used by the deployment backend to destroy resources defined in 
     * sun-resources.xml
     * @param resources Resources to be destroyed
     * @throws Exception
     */
    public void deleteResource(List<Resource> res) throws Exception{
        //Delete Resource and ResourceRefs in the reverse order.
        int size = res.size();
        for (int i = (size -1); i >= 0 ; i--) {
            Resource resource = res.get(i);
            //Security map deletion is not required
            //They will be deleted automatically when connector connection pool is deleted
            if(resource.getType().equals(CONNECTOR_SECURITY_MAP))
                continue;
            try{
                deleteAResource(resource);
            }catch(Exception ex){
                String s = localStrings.getString("unable.delete.resource", resource.toString());
                sLogger.log(Level.WARNING, s);
            }
        }
    }
    
    private void deleteAResource(Resource resource) throws Exception{
        //System.out.println("ResourcesMBean deleteAResource " + resource.getType());
        this.destroyResource(resource.getType(), 
                getResourceName(resource.getType(), resource.getAttributes()));
    }


    private boolean destroyResource(String resType, String name)
        throws Exception
    {
        String operation = null;
        String suffix = null;
        boolean isDeleted = false;

        if (resType.equals(RESOURCE_ADAPTER_CONFIG)){
           suffix = "ByResourceAdapterName";
        }else if (resType.equals(JDBC_CONNECTION_POOL) || resType.equals(CONNECTOR_CONNECTION_POOL)){
           suffix = "ByName";
        }else{
            suffix = "ByJndiName";
        }

        operation = "remove" + ConfigMBeanHelper.convertTagName(resType) + suffix;
        try {
            super.invoke(operation, new Object[]{name}, new String[] {"java.lang.String"});
            isDeleted = true;
        }
        catch (javax.management.ReflectionException re) {}

        return isDeleted;
    }


    private ObjectName getObjectNameForResType(String resType, String name)
        throws Exception
    {
        return m_registry.getMbeanObjectName(resType, new String[]{getDomainName(), name});
    }
   
    private void deleteResourceRef(String resRef, Target target, String tgtName)
        throws Exception
    {        
        //System.out.println("deleteResourceRef " + resRef +  " target " + target + " tgtName " + tgtName );
//        try {
        
            //This call is a sanity check to make sure that the resource exists.
            final String resType = getResourceReferenceHelper().getResourceType(resRef);
            
            // If target is server or cluster, resource-ref should also be
            // deleted from server or cluster element.
            if (target.getType() == TargetType.SERVER ||
                target.getType() == TargetType.DAS) {               

                //Delete the resource reference only if the resource is referenced 
                //by the target server only
                if (!ServerHelper.serverReferencesResource(getConfigContext(),  
                    tgtName, resRef)) {
                    throw new ConfigException(localStrings.getString("serverResourceRefDoesNotExist",
                        tgtName, resRef));
                } else if (ResourceHelper.isResourceReferencedByServerOnly(getConfigContext(), 
                    resRef, tgtName)) {
                    getResourceReferenceHelper().deleteResourceReference(tgtName, resRef); 
                } else {
                    throw new ConfigException(localStrings.getString("resourceHasMultipleRefs", 
                        tgtName, resRef, ResourceHelper.getResourceReferenceesAsString(
                            getConfigContext(), resRef)));
                }
            } else if (target.getType() == TargetType.CLUSTER) {                
                //Delete the resource reference only if the resource is referenced 
                //by the target cluster only
                 if (!ClusterHelper.clusterReferencesResource(getConfigContext(), 
                    tgtName, resRef)) {
                    throw new ConfigException(localStrings.getString("clusterResourceRefDoesNotExist",
                        tgtName, resRef));
                 } else if (ResourceHelper.isResourceReferencedByClusterOnly(getConfigContext(), 
                    resRef, tgtName)) {
                    getResourceReferenceHelper().deleteResourceReference(tgtName, resRef);     
                 } else {
                    throw new ConfigException(localStrings.getString("resourceHasMultipleRefs", 
                        tgtName, resRef, ResourceHelper.getResourceReferenceesAsString(
                            getConfigContext(), resRef)));                     
                 }
            }
            //As a sanity check ensure that the resource has no references; otherwise
            //it cannot be deleted from the domain
            if (ResourceHelper.isResourceReferenced(getConfigContext(), resRef)) {
                throw new ConfigException(localStrings.getString("resourceIsReferenced", 
                    resRef, ResourceHelper.getResourceReferenceesAsString(
                        getConfigContext(), resRef)));
            }
/*        }
        catch (javax.management.MalformedObjectNameException mone) {}
        catch (javax.management.InstanceNotFoundException infe) {}
        catch (javax.management.ReflectionException re) {}
*/
    }

     /**
     * Creating resources from sun-resources.xml file. This method is used by 
     * the admin framework when the add-resources command is used to create
     * resources
     */
    public ArrayList createResource(String resourceXMLFile, String tgtName)
                                                            throws Exception {
        //@todo: how do we handle failures?
        boolean retVal = false;
        ArrayList results = new ArrayList();
        com.sun.enterprise.resource.ResourcesXMLParser resourcesParser =
            new com.sun.enterprise.resource.ResourcesXMLParser(resourceXMLFile);
        List<Resource> resources = resourcesParser.getResourcesList();
        //First add all non connector resources.
        ;
        Iterator<Resource> nonConnectorResources = ResourcesXMLParser.getNonConnectorResourcesList(resources,false).iterator();
        while (nonConnectorResources.hasNext()) {
            Resource resource = (Resource) nonConnectorResources.next();
            String s = "";
            try {
                s = createAResource(resource, tgtName, true);
            } catch (Exception e) {
                s = e.getMessage();
            }
            results.add(s);
        }

        //Now add all connector resources
        Iterator connectorResources = ResourcesXMLParser.getConnectorResourcesList(resources, false).iterator();
        while (connectorResources.hasNext()) {
            Resource resource = (Resource) connectorResources.next();
            String s = "";
            try {
                s = createAResource(resource, tgtName, true);
            } catch (Exception e) {
                s = e.getMessage();
            }
            results.add(s);
        }

        return results;
    }


     /**
     * Creating resources from sun-resources.xml file. This API
     * is used by the deployment backend to create Resources
     * @param isEnabled the enabled flag as passed in via asadmin
     */
    public void createResource(List<Resource> res, 
            Boolean isEnabled)  throws Exception {
        //@todo: handle isEnabled. This should override the enabled flag specified
        //in the individual Resources.
        Iterator<Resource> resources = res.iterator();
        while (resources.hasNext()) {
            Resource resource = (Resource) resources.next();
            createAResource(resource, null, false);
        }
    }
    
    public void createResourceAndResourceReference(
                    List<Resource> resources, List<String> targetNameList, 
                    Boolean enabled) throws Exception {
        Iterator<Resource> resourceList = resources.iterator();
        while (resourceList.hasNext()) {
            Resource resource = (Resource) resourceList.next();
            for (String target : targetNameList) {
                createAResource(resource, target, false);
                createAResourceReference(resource, target, enabled);    
            }
            
        }
    }

    /**
     * Deleting resources from sun-resources.xml file. This API
     * is used by the deployment backend to delete Resources
     */
    public void deleteResourceAndResourceReference (
                    List<Resource> resources, 
                    List<String> targetNameList) throws Exception{
        //Delete Resource and ResourceRefs in the reverse order.
        int size = resources.size();
        for (int i = (size -1); i >= 0 ; i--) {
            Resource resource = (Resource)resources.get(i);
            //Security map deletion is not required
            //They will be deleted automatically when connector connection pool is deleted
            if(resource.getType().equals(CONNECTOR_SECURITY_MAP))
                continue;
            try{
                deleteAResourceRef(resource, targetNameList);
            }catch(Exception ex){
                String s = localStrings.getString("unable.delete.resource.ref", resource.toString());
                sLogger.log(Level.WARNING, s);
                continue;
            }
            try{
                deleteAResource(resource);
            }catch(Exception ex){
                String s = localStrings.getString("unable.delete.resource", resource.toString());
                sLogger.log(Level.WARNING, s);
            }
        }
    }
    
    /**
     * Adds a resource 
     * @param resource Resource to be added
     * @param tgtName Target to which the resource needs to be added
     * @return a string representing the status of the resource-addition
     */
    private String createAResource(Resource resource, String tgtName, boolean createResourceRefs ) throws Exception {
        //System.out.println("ResourcesMBean : createAResource " + resource.getType());
                String resourceType = resource.getType();
                //if the resource element is security-map then invoke connector-
                //connection-pool MBean which has all the operations for
                // security-map and its sub-elemnents....
                // This is a special case since security-map element is a part
                //of connector-connection-pool and not a resource by itself.

                if(resourceType.equals(CONNECTOR_SECURITY_MAP)){
                    AttributeList mapAttributes = resource.getAttributes();
                    // get the pool name.
                    String poolName = null;
                    String username = null ;
                    String password = null;
                    if(mapAttributes != null){
                        int s = mapAttributes.size();
                        for(int i=0;i<s;i++){
                            Attribute attribute =(Attribute)mapAttributes.get(i);
                            String n= attribute.getName();
                            if((attribute.getName()).equalsIgnoreCase("pool-name"))
                                poolName = (String)attribute.getValue();
                            if((attribute.getName()).equalsIgnoreCase("user-name"))
                               username = (String)attribute.getValue();
                            if((attribute.getName()).equalsIgnoreCase("password"))
                                password = (String)attribute.getValue();
                            
                        }
                    }
                    mapAttributes.add(new Attribute(POOL_NAME,poolName));
                    ObjectName poolObj = m_registry.getMbeanObjectName(POOL_TYPE,
                           new String[]{getDomainName(),poolName,CONFIG});

                    try{
                        ObjectName map =(ObjectName)getMBeanServer().invoke(poolObj,
                            CREATE_SECURITY_MAP, new Object[] {mapAttributes,username,
                            password, tgtName},
                            new String[] {AttributeList.class.getName(),"java.lang.String",
                            "java.lang.String","java.lang.String"});
                String s = localStrings.getString("admin.mbeans.rmb.res_type_added", resourceType);
                sLogger.log(Level.INFO, s);
                return s;
                    }catch(Exception e){
                        e.printStackTrace();
                String s = localStrings.getString("admin.mbeans.rmb.res_type_not_added", resourceType, e.getMessage());
                sLogger.log(Level.INFO, s);
                throw new Exception(s, e);
                    }
                }
                else{
                    AttributeList attr = resource.getAttributes();
                    String desc = resource.getDescription();
                    if (desc != null)
                        attr.add(new Attribute("description", desc));

                    Properties props = resource.getProperties();
                    try{
                        ObjectName objName = createResource(resource.getType(),
                                                attr, props, tgtName, createResourceRefs);
                        //results.add("Added Resource Type :"+resourceType);
                String s = localStrings.getString("admin.mbeans.rmb.res_type_added", resourceType);
                sLogger.log(Level.INFO, s);
                return s;
                    }catch(Exception e){
                e.printStackTrace();
                         //results.add("Could not add Resource Type "
                         //+":"+resourceType +" because :"+e.getMessage());
                String s = localStrings.getString("admin.mbeans.rmb.res_type_not_added", resourceType, e.getMessage());
                sLogger.log(Level.INFO, s);
                throw new Exception (s, e);
                    }
                }

    }

    /**
     * Get list of properties and default values for connection with
     * defined data source.
     *
     * returns "null" if no info or error
    */
    public Map getConnectionDefinitionPropertiesAndDefaults(String dataSource)
    {
       try {
         return new HashMap( ConnectionDefinitionUtils.
             getConnectionDefinitionPropertiesAndDefaults(dataSource.trim()));
       } catch(Throwable t) {
           return null;
       }
    }

    /**
     * Get list of properties for resource adapter defined by location 
     *
     * returns "null" if no info or error
    */
    public Map getResourceAdapterBeanProperties(String location)
    {
       try {
            return new HashMap(ConnectorRuntime.getRuntime().
               getResourceAdapterBeanProperties(location));
       } catch(Throwable t) {
           return null;
       }
    }
   
    /**
     * Get the list of connection definition names supported by a specific
     * resource adapter deployed on the server.
     */
    public String[] getConnectionDefinitionNames(String rarName)
        throws Exception
    {
//        try {
            return ConnectorRuntime.getRuntime().
                getConnectionDefinitionNames(rarName);
/*        } catch (Exception e) {
            throw new MBeanException(e, e.getMessage());
        }
 */
    }

    /**
     * Get the resource-adapter-config properties supported by a specific
     * resource adapter deployed on the server.
     */
    public Properties getResourceAdapterConfigProps(AttributeList attrList)
        throws Exception 
    {
        String rarName = null;

//        try {
            if (attrList != null) 
            {
                int s = attrList.size();
                for(int i=0; i<s; i++)
                {
                    Attribute attribute =(Attribute)attrList.get(i);
                    String n= attribute.getName();
                    
                    if(isAttrNameMatch(attribute, "resource-adapter-name"))
                        rarName = (String)attribute.getValue();
                }
            } else 
                throw new Exception (localStrings.getString("admin.mbeans.rmb.null_attrib_list"));
            
            return ConnectorRuntime.getRuntime().
                getResourceAdapterConfigProps(rarName);
/*        } catch (Exception e) {
            throw new MBeanException(e, e.getMessage());
        }
 */
    }

    /**
     * Get the Managed Connection Factory Config Properties supported by a 
     * specific resource adapter for the given connection definition name,
     * deployed on the server.
     */
    public Properties getMCFConfigProps(AttributeList attrList)
        throws Exception 
    {
        String rarName = null;
        String connDefName = null;

//        try {
            if (attrList != null) 
            {
                int s = attrList.size();
                for(int i=0; i<s; i++){
                    Attribute attribute =(Attribute)attrList.get(i);
                    String n= attribute.getName();
                    
                    if(isAttrNameMatch(attribute, "resource-adapter-name"))
                        rarName = (String)attribute.getValue();
                    
                    if(isAttrNameMatch(attribute, "connection-definition-name"))
                        connDefName = (String)attribute.getValue();
                }
            } else 
                throw new Exception (localStrings.getString("admin.mbeans.rmb.null_attrib_list_mcf"));

            return ConnectorRuntime.getRuntime().
                getMCFConfigProps(rarName, connDefName);
/*        } catch (Exception e) {
            throw new MBeanException(e, e.getMessage());
        }
 */
    }

    /**
     * Get the list of Admin Object Config Properties supported by a specific
     * resource adapter for the given admin object interface,
     * deployed on the server.
     */
    public Properties getAdminObjectConfigProps(AttributeList attrList)
        throws Exception 
    {
        String rarName = null;
        String adminObjIntf = null;

//        try {
            if (attrList != null) 
            {
                int s = attrList.size();
                for(int i=0; i<s; i++){
                    Attribute attribute =(Attribute)attrList.get(i);
                    String n= attribute.getName();
                    
                    if(isAttrNameMatch(attribute, "resource-adapter-name"))
                        rarName = (String)attribute.getValue();
                    
                    if(isAttrNameMatch(attribute, "admin-object-interface"))
                        adminObjIntf = (String)attribute.getValue();
                }
            } else 
                throw new Exception (localStrings.getString("admin.mbeans.rmb.null_attrib_list_ao"));

            return ConnectorRuntime.getRuntime().
                getAdminObjectConfigProps(rarName, adminObjIntf);
/*        } catch (Exception e) {
            throw new MBeanException(e, e.getMessage());
        }
 */
    }

    /**
     * Get the list of Admin Object Interface Names supported by a specific
     * resource adapter deployed on the server.
     */
    public String[] getAdminObjectInterfaceNames(String rarName)
        throws Exception 
    {
        if (rarName == null) 
            //throw new Exception ("Could not get Admin Object Interface Names. Resource Adapter Name is null");
            throw new Exception (localStrings.getString("admin.mbeans.rmb.null_raname_ao"));

        return ConnectorRuntime.getRuntime().getAdminObjectInterfaceNames(rarName);
    }

    /*
     * Temporary method for PE. This method should ideally be in Runtime MBean.
     */
    public Boolean pingConnectionPool(String poolName, String tgtName) throws Exception
    {
        boolean retVal = false;
        final Target target;       

//        try
        {
            //FIXTHIS: Not sure why this can only be a server instance. We should be 
            //able to reference a server, domain, or cluster here.
            //Ramakanth: Sure. Why can't they be?
            final TargetType[] validTargets = new TargetType[] 
                {TargetType.CLUSTER, TargetType.DOMAIN, 
                 TargetType.SERVER, TargetType.DAS};

            target = TargetBuilder.INSTANCE.createTarget(validTargets, 
                tgtName, getConfigContext());
            retVal = ConnectorRuntime.getRuntime().testConnectionPool(poolName);
        }
/*        catch (MBeanException me) {
            throw me;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new MBeanException(e, e.getLocalizedMessage());
        }
*/
        return Boolean.valueOf(retVal);
    }

    protected MBeanServer getMBeanServer()
    {
        return com.sun.enterprise.admin.common.MBeanServerFactory.getMBeanServer();
    }

    // JMS Handling Starts
    /**
         Ping the JMS service.

         @return Status of JMS service
    */
    public JMSStatus JMSPing() throws JMSAdminException
    {
        try {
            JMSAdmin jmsAdmin = getJMSAdmin();
            jmsAdmin.pingProvider();
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "JMSPing", e);
            handleException(e);
        }

        JMSStatus	js = new JMSStatus();
        return (js);

    }

    /**
        Ping the JMS service.

        @return Status of JMS service
    */
    public JMSStatus JMSPing(String username, String password, int port)
                        throws JMSAdminException
    {
        try {
            JMSAdmin jmsAdmin = getJMSAdmin();
            jmsAdmin.pingProvider(username, password, port);
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "JMSPing", e);
            handleException(e);
        }

        JMSStatus	js = new JMSStatus();
        return (js);
    }

    /**
	fix for bug# 6157447
        Ping the JMS service.

 	@param target server, cluster, config, etc.
        @return String status of jms ping RUNNING or exception
    */
    public String JMSPing(String targetName)
                        throws JMSAdminException
    {
        try {
            Target target = getResourceTarget(targetName);
			// check and use JMX
			if (JMSDestination.useJMX(target)) {
				JMSDestination jmsd = new JMSDestination();
				return(jmsd.JMSPing(target.getName()));
			} else {
                JMSAdmin jmsAdmin = getJMSAdmin(targetName);
                jmsAdmin.pingProvider();
            }
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "JMSPing", e);
            handleException(e);
        }

        return JMSAdminConstants.JMS_HOST_RUNNING;
    }


    /**
         Create a JMS Destination.

         @param destName	Destination name.
         @param destType	Type of destination to create. See
                                JMSAdminConstants for details.
         @param destProps	Properties of destination to create.
    */
    public void createJMSDestination(String destName, 
                                     String destType, 
                                     Properties destProps, 
                                     String tgtName)
                throws JMSAdminException
    {
        sLogger.entering(getClass().getName(), "createJMSDestination",
        new Object[] {destName, destType, destProps, tgtName});

        validateJMSDestName(destName);
        validateJMSDestType(destType);

        int newDestType = JMSConstants.QUEUE;
        JMSAdmin jmsAdmin = null;
        boolean connected = false;


        try {
	     /* Do not restrict the number of consumers, because in 4.1 MQ
	      * open MQ, there is no restriction on the number of consumers
	      * for a queue. In 4.0 PE there was a restriction of 2.
	      * Fixes issue : 6543199
	      */
             if (destType.equals(JMSAdminConstants.JMS_DEST_TYPE_QUEUE)) {
                if (destProps == null) {
                    destProps = new Properties();
                }
                String maxConsumersProperty = IASJmsUtil.getMaxActiveConsumersProperty();
                String maxConsumersAttribute = IASJmsUtil.getMaxActiveConsumersAttribute();
                String maxConsumersValue = IASJmsUtil.getDefaultMaxActiveConsumers();
                if (!destProps.containsKey(maxConsumersProperty) && 
                    !destProps.containsKey(maxConsumersAttribute) ) {
                    destProps.put(maxConsumersAttribute, maxConsumersValue);
                }
            }

    		// check and use JMX
            Target target = getResourceTarget(tgtName);
    		if (JMSDestination.useJMX(target)) {
    			JMSDestination jmsd = new JMSDestination();
    			jmsd.createJMSDestination(destName, destType, destProps, tgtName);
    			return;
    		} else {
	            jmsAdmin = getJMSAdmin(tgtName);
	            jmsAdmin.connectToProvider();
	            connected = true;
	
	            if (destType.equals(JMSAdminConstants.JMS_DEST_TYPE_TOPIC)) {
	                newDestType = JMSConstants.TOPIC;
	            } else if (destType.equals(JMSAdminConstants.JMS_DEST_TYPE_QUEUE)) {
	                newDestType = JMSConstants.QUEUE;
	            }
	
	            jmsAdmin.createProviderDestination(destName, newDestType, destProps);
    		}
        } catch (Exception e) {
            handleException(e);
        } finally {
            if (connected)  {
                try  {
                    jmsAdmin.disconnectFromProvider();
                } catch (Exception ex)  {
                    handleException(ex);
                }
            }
        }
    }

    /**
         Delete a JMS Destination.

         @param destName	Destination name.
         @param destType	Type of destination to delete. See
                                JMSAdminConstants for details.
    */
    public void deleteJMSDestination(String destName, String destType, String tgtName)
                throws JMSAdminException
    {
        sLogger.entering(getClass().getName(), "deleteJMSDestination",
        new Object[] {destName, destType});

		validateJMSDestName(destName);
		validateJMSDestType(destType);

        int newDestType = JMSConstants.QUEUE;
        JMSAdmin jmsAdmin = null;
        boolean connected = false;

        try {
    		// check and use JMX
            Target target = getResourceTarget(tgtName);
    		if (JMSDestination.useJMX(target)) {
    			JMSDestination jmsd = new JMSDestination();
    			jmsd.deleteJMSDestination(destName, destType, tgtName);
    			return;
    		} else {
	            jmsAdmin = getJMSAdmin(tgtName);
	            jmsAdmin.connectToProvider();
	            connected = true;
	
	            if (destType.equals(JMSAdminConstants.JMS_DEST_TYPE_TOPIC)) {
	                    newDestType = JMSConstants.TOPIC;
	            } else if (destType.equals(JMSAdminConstants.JMS_DEST_TYPE_QUEUE)) {
	                    newDestType = JMSConstants.QUEUE;
	            }
	
	            jmsAdmin.deleteProviderDestination(destName, newDestType);
    		}
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "deleteJMSDestination", e);
            handleException(e);
        } finally {
            if (connected)  {
                try  {
                    jmsAdmin.disconnectFromProvider();
                } catch (Exception ex)  {
                    handleException(ex);
                }
            }
        }
    }


    /**
      * Purge a JMS Destination.
      * @param destName	Destination name.
      * @param destType	Type of destination to purge.
      */
    public void flushJMSDestination(String destName, String destType, String tgtName)
        throws JMSAdminException {
		// check and use JMX
        try {
            Target target = getResourceTarget(tgtName);
            if (JMSDestination.useJMX(target)) {
            	JMSDestination jmsd = new JMSDestination();
	    	if (isClustered(tgtName)) {
			/* The MQ 4.1 JMX Apis do not clean up all 
			 * the destintations in all the instances 
 			 * in a broker cluster, in other words, JMX 
			 * operation purge is not cluster aware
			 * So we have to ensure that we purge each instance
			 * in the cluster one by one.
			 * If one of them fail just log and proceed, we will
			 * flag an error towards the end. Issue 6523135
		         * This works because we resolve the port numbers	
			 * even for standalone instances in MQAddressList.
		  	 */
		  	boolean success = true;	
			Server [] servers = target.getServers();
			for (int server = 0; server < servers.length; server++) {
			    try {
				jmsd.purgeJMSDestination(destName, destType, servers[server].getName());
			    } catch (Exception e) {
				success = false;
			        sLogger.log(Level.SEVERE,localStrings.getString("admin.mbeans.rmb.error_purging_jms_dest") + servers[server].getName());
		            }		
			}
			if (!success) {
				throw new Exception(localStrings.getString("admin.mbeans.rmb.error_purging_jms_dest"));	
			}
				
	    	} else {
            		jmsd.purgeJMSDestination(destName, destType, tgtName);
		}
            } else {
                sLogger.log(Level.WARNING, "Flush JMS destination not supported in the JMS SPI");
            }
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "flushJMSDestination", e);
            handleException(e);
        }
    }

    /**
      *  List JMS Destinations.
      *
          *  @param destType Type of destination to list. See
          *  JMSAdminConstants for details.
          *  @return An array of JMSDestinationInfo objects.
          *  Each JMSDestinationInfo object contains
          *  information such as destination name,
	  *                                  	  *     *     	  *	 type and other attributes.
      */
    public JMSDestinationInfo[] listJMSDestinations(String destType, String tgtName)
                throws JMSAdminException
    {
        JMSDestinationInfo	destInfoArray[] = null;
        JMSAdmin jmsAdmin = null;
        boolean connected = false;

        sLogger.entering(getClass().getName(), "listJMSDestinations", destType);
        
        try {
    		// check and use JMX
            Target target = getResourceTarget(tgtName);
    		if (JMSDestination.useJMX(target)) {
    			JMSDestination jmsd = new JMSDestination();
            	return (jmsd.listJMSDestinations(tgtName, destType));
    		} else {
    	        String s[][] = {null, null};
	            jmsAdmin = getJMSAdmin(tgtName);
	            jmsAdmin.connectToProvider();
	            connected = true;
	
	            s = jmsAdmin.getProviderDestinations();
	
	            if (destType == null) {
	                destInfoArray = listAllDestinations(s);
	            } else if (destType.equals(JMSAdminConstants.JMS_DEST_TYPE_TOPIC) ||
	               destType.equals(JMSAdminConstants.JMS_DEST_TYPE_QUEUE)) {
	               destInfoArray = listDestinationsByType(destType, s);
	            } else {
	               destInfoArray = listAllDestinations(s);
	            }
    	        return (destInfoArray);
    		}
        } catch (Exception e) {
            sLogger.throwing(getClass().getName(), "listJMSDestinations", e);
            handleException(e);
        } finally {
            if (connected)  {
                try  {
                    jmsAdmin.disconnectFromProvider();
                } catch (Exception ex)  {
                    handleException(ex);
                }
            }
        }
        return null;
    }


    private void validateJMSDestName(String destName) {
        if(destName==null || destName.length() <= 0)
            throw new IllegalArgumentException(localStrings.getString("admin.mbeans.rmb.invalid_jms_destname",destName));
    }

    private void validateJMSDestType(String destType) {
        if(destType==null || destType.length() <= 0)
            throw new IllegalArgumentException(localStrings.getString("admin.mbeans.rmb.invalid_jms_desttype",destType));
        if(!destType.equals(JMSAdminConstants.JMS_DEST_TYPE_QUEUE) &&
            !destType.equals(JMSAdminConstants.JMS_DEST_TYPE_TOPIC))
            throw new IllegalArgumentException(localStrings.getString("admin.mbeans.rmb.invalid_jms_desttype",destType));
    }

    /** calls {@link createJMSDestination } after getting the values from attribute list
     * @param attrList attributeList with destName and destType attributes
     * @param props JMS properties
     * @param target ignored
     * @return void
     * @throws JMSAdminException thrown from createJMSDestination call
     * @throws MBeanException thrown from createJMSDestination call
     */    
    public void createPhysicalDestination(AttributeList attrList ,Properties props,
        String target)throws JMSAdminException,MBeanException
    {
        String destType = null;
        String destName = null;
        if(attrList == null)
            throw new IllegalArgumentException();
        
        int size = attrList.size();
        for(int i=0;i<size;i++){
                Attribute attribute =(Attribute)attrList.get(i);
                if((attribute.getName().equalsIgnoreCase(DEST_TYPE)))
                    destType = (String)attribute.getValue();
                if((attribute.getName().equalsIgnoreCase(DEST_NAME)))
                    destName = (String)attribute.getValue();
        }
        //FIXME: Should target be passed in here???
        createJMSDestination(destName,destType,props, target);
    }
    
    /** calls {@link deleteJMSDestination }
     * @param destName destination name
     * @param destType destination type "queue" or "topic"
     * @param target ignored
     * @return void
     * @throws JMSAdminException from deleteJMSDestination
     * @throws MBeanException from deleteJMSDestination
     */    
    public void deletePhysicalDestination(String destName, String destType,
        String target)throws JMSAdminException,MBeanException
    {
        deleteJMSDestination(destName,destType, target);
    }
    
    
    /** returns ObjectName array of all jms destinations
     * @param target ignored
     * @return ObjectName array of all jms destinations with attributes destName and destType
     * @throws JMSAdminException from the list JMSDestination call
     * @throws MBeanException from the listJMSDestination call
     */    
    public ObjectName[] listPhysicalDestinations(String target)
        throws JMSAdminException,MBeanException
    {
        ObjectName[] objectNames = null;
        
        JMSDestinationInfo[] destInfos =  listJMSDestinations(null, target);
        int infoLength=0;
        if (destInfos != null && destInfos.length >0 )
            infoLength = destInfos.length;

        // Instead of null, convey the message that there are no jms destinations.
        if (infoLength ==0  )
            return null;
        
        objectNames = new ObjectName[infoLength];
        for(int i=0;i<infoLength;i++) {
            String destName = destInfos[i].getDestinationName();
            String destType = destInfos[i].getDestinationType();
            try {
                ObjectName objectName =
                new ObjectName(getDomainName()+":"+DEST_NAME+"="+destName+","+DEST_TYPE+"="+destType);
                objectNames[i]=objectName;
            }catch(javax.management.MalformedObjectNameException e){
                sLogger.warning(e.toString());
            }
        }
        
        return objectNames;
    }
    
// <NEW CODE> Ram Jeyaraman

    /**
     * Retrieve the list of message listener types supported by a specific
     * resource adapter deployed on the server.
     */
    public String[] getMessageListenerTypes(String raName)
            throws Exception {
//        try {
            return ConnectorRuntime.getRuntime().
                getMessageListenerTypes(raName);
/*        } catch (Exception e) {
            throw new MBeanException(e, e.getMessage());
        }
 */
    }

    /**
     * Retrieve the list of activation configuration property names
     * for a specific message listener type from the resource adapter deployed
     * on the server.
     */
    public Properties getActivationConfProps(String raName,
                                           String msgLsnrType)
            throws Exception {
//        try {
            return ConnectorRuntime.getRuntime().
                getMessageListenerConfigProps(raName, msgLsnrType);
/*        } catch (Exception e) {
            throw new MBeanException(e, e.getMessage());
        }
 */
    }

    /**
     * Retrieve the list of activation configuration property types
     * for a specific message listener type from the resource adapter deployed
     * on the server.
     *
     * Note, the types need to correspond to the names in the
     * same retrieval order.
     */
    public Properties getActivationConfPropTypes(String raName,
                                           String msgLsnrType)
            throws Exception {
//        try {
            return ConnectorRuntime.getRuntime().
                getMessageListenerConfigPropTypes(raName, msgLsnrType);
/*        } catch (Exception e) {
            throw new MBeanException(e, e.getMessage());
        }
 */
    }

    /**
     * Retrieve the list of required activation configuration property names
     * for a specific message listener type from the resource adapter deployed
     * on the server.
     */
    /* yet to be implemented
    public Properties getRequiredActivationConfProps(String raName,
                                                   String msgLsnrType)
            throws MBeanException {
        try {
            return ConnectorRuntime.getRuntime().
                getRequiredActivationConfProps(raName, msgLsnrType);
            throw new MBeanException("Function Not Supported");
        } catch (Exception e) {
            throw new MBeanException(e, e.getMessage());
        }
    }
    */

// <NEW CODE END> Ram Jeyaraman

    /**
     * Synchronized to serialize access to _jmsAdmin, in case there are
     * multiple admin clients accessing the broker and modifying
     * the admin username/passwd or port.
     */
    private synchronized JMSAdmin getJMSAdmin() throws Exception
    {
        ConfigContext serverContext;
        JmsService jmsService;
        JMSAdminFactory jmsaf;
        JMSAdmin _jmsAdmin = null;
        String instanceName, portStr, username, password, localhost, jmsAdminURL;

        serverContext = getConfigContext();

        jmsaf = IASJmsUtil.getJMSAdminFactory();

        jmsService = (JmsService)ConfigBeansFactory.getConfigBeanByXPath(
            serverContext, ServerXPathHelper.XPATH_JMS_SERVICE);

        localhost = java.net.InetAddress.getLocalHost().getHostName();
        JmsHost hostElement = jmsService.getJmsHost(0);  //ms1 krav
        portStr = hostElement.getPort();
        username = hostElement.getAdminUserName();
        password = hostElement.getAdminPassword();
        jmsAdminURL = localhost + ((portStr == null) ?
                        "" : ":" + portStr);

        _jmsAdmin = jmsaf.getJMSAdmin(jmsAdminURL, username, password);

        return _jmsAdmin;
    }

    private JMSAdmin getJMSAdmin(String targetName) throws Exception
    {
        final TargetType[] vaildTargetTypes = new TargetType[] {
			TargetType.CLUSTER, TargetType.SERVER, TargetType.DAS, TargetType.CONFIG};
        final Target target = TargetBuilder.INSTANCE.createTarget(
                vaildTargetTypes, targetName, getConfigContext());
        assert target != null;

        final Config config = target.getConfigs()[0];
        final JmsService jmsService = config.getJmsService();
        JmsHost host = jmsService.getJmsHostByName(jmsService.getDefaultJmsHost());
        if (host == null)
        {
            host = jmsService.getJmsHost(0);
        }

        final String hostName = host.getHost();
        final String port = host.getPort();
        final String adminUser = host.getAdminUserName();
        final String adminPassword = host.getAdminPassword();
        String url = hostName + (port == null ? "" : ":" + port);
        if (adminUser == null)
        {
           return IASJmsUtil.getJMSAdminFactory().getJMSAdmin(url);
        }
        else
        {
           return IASJmsUtil.getJMSAdminFactory().getJMSAdmin(
                url, adminUser, adminPassword);
        }
    }

    private JMSDestinationInfo[] listAllDestinations(String[][] s)
    {
        JMSDestinationInfo	destInfo;
        Vector	dests = new Vector();
        String	destName, destType;

        // Convert the array[1] from JMSAdminConstants.QUEUE/TOPIC
        // to a JMSConstants.QUEUE/TOPIC
        for (int i = 0; i < s[1].length; i++) {
            destName = s[0][i];
            if (s[1][i].equals(Integer.valueOf(JMSConstants.QUEUE).toString())) {
                destType = JMSAdminConstants.JMS_DEST_TYPE_QUEUE;
            } else if (s[1][i].equals(Integer.valueOf(JMSConstants.TOPIC).toString())) {
                destType = JMSAdminConstants.JMS_DEST_TYPE_TOPIC;
            } else {
                destType = "";
            }
            destInfo = new JMSDestinationInfo(destName, destType);
            dests.addElement(destInfo);
        }

        if (dests.size() > 0)  {
            Object objArray[] = dests.toArray();
            JMSDestinationInfo 	jmsDestArray[];
            int	size = dests.size();

            jmsDestArray = new JMSDestinationInfo [size];

            for (int i = 0; i < size; ++i)  {
                jmsDestArray[i] = (JMSDestinationInfo)objArray[i];
            }

            return (jmsDestArray);
        }

        return (null);
    }

    private JMSDestinationInfo[] listDestinationsByType(String destType, String[][] s)
    {
        JMSDestinationInfo	destInfo;
        Vector			dests = new Vector();
        String 			destName, type;
        String 			lookFor = null;

        if (destType.equals(JMSAdminConstants.JMS_DEST_TYPE_TOPIC))
            lookFor = Integer.valueOf(JMSConstants.TOPIC).toString();
        else
            lookFor = Integer.valueOf(JMSConstants.QUEUE).toString();

        // Convert the array[1] from JMSAdminConstants.QUEUE/TOPIC
        // to a JMSConstants.QUEUE/TOPIC
        int j = 0;
        for (int i = 0; i < s[1].length; i++) {
            if (s[1][i].equals(lookFor)) {
                destName = s[0][i];
                if (s[1][i].equals(Integer.valueOf(JMSConstants.QUEUE).toString())) {
                    type = JMSAdminConstants.JMS_DEST_TYPE_QUEUE;
                } else if (s[1][i].equals(Integer.valueOf(JMSConstants.TOPIC).toString())) {
                    type = JMSAdminConstants.JMS_DEST_TYPE_TOPIC;
                } else {
                    type = "";
                }
                destInfo = new JMSDestinationInfo(destName, type);
                dests.addElement(destInfo);
            }
        }

        if (dests.size() > 0)  {
            Object 		objArray[] = dests.toArray();
            JMSDestinationInfo 	jmsDestArray[];
            int			size = dests.size();

            jmsDestArray = new JMSDestinationInfo [size];

            for (int i = 0; i < size; ++i)  {
                    jmsDestArray[i] = (JMSDestinationInfo)objArray[i];
            }

            return (jmsDestArray);
        }

        return (null);
    }


    private void handleException(Exception e) throws JMSAdminException
    {
        if (e instanceof JMSAdminException)  {
            throw ((JMSAdminException)e);
        }

        String msg = e.getMessage();

        JMSAdminException jae;

        if (msg == null)  {
            jae = new JMSAdminException();
        } else  {
            jae = new JMSAdminException(msg);
        }

        /*
         * Don't do this for now because the CLI does not include jms.jar
         * (at least not yet) in the classpath. Sending over a JMSException
         * will cause a class not found exception to be thrown.
        jae.setLinkedException(e);
         */

        throw jae;
    }
    
    private static boolean isAttrNameMatch(Attribute attr, String name)
    {
        //FIXME: this code should be changed after FCS
        // for now we supporting both "dashed" and "underscored" names
        return attr.getName().replace('_','-').equals(name.replace('_','-'));
    }

    // JMS Handling Ends
    private boolean isClustered(String tgt) throws ConfigException{
        final TargetType[] vaildTargetTypes = new TargetType[] {
                        TargetType.CLUSTER, TargetType.SERVER, TargetType.DAS, TargetType.CONFIG};
        final Target target = TargetBuilder.INSTANCE.createTarget(
                vaildTargetTypes, tgt, getConfigContext());
        assert target != null;
        return target.getType() == TargetType.CLUSTER;
    }

}

