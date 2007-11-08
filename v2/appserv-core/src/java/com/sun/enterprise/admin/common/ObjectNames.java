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

package com.sun.enterprise.admin.common;
//JMX imports
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
//Admin imports
import com.sun.enterprise.admin.util.Debug;
import com.sun.enterprise.admin.util.Logger;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.appserv.management.base.Util;

/**
	A convenience class to create the object names of various MBeans
	that are of interest. Note that all the Object Names will be created
	in domain with name "ias", unless specified otherwise.
	Clients of this class need not worry of details of creating valid
	Object Names. 
	<p>
	Note that if any of the methods of this class returns a <code> null </code>
	Object Name, then it implies that the input parameters specified were not
	valid.
	@author  Kedar Mhaswade
	@version 1.0
*/
public class ObjectNames
{
    public static final String kDefaultIASDomainName	= "com.sun.appserv";

    public static final char   kWildcardChar		= '*';
    public static final char   kDomainNameSeparatorChar	= ':';
    public static final char   kPropertySeparatorChar	= ',';
    public static final char   kNameValueSeparatorChar	= '=';
    public static final char   kSingleMatchChar		= '?';

    public static final String kDomainNameInvalidatorString = 
            "" + kWildcardChar + kDomainNameSeparatorChar + kPropertySeparatorChar +
            kNameValueSeparatorChar + kSingleMatchChar;
    //constant names of the property keys
    public static final String kTypeKeyName		= "type";
    public static final String kNameKeyName		= "name";
    public static final String kGroupIdKeyName	= "group";
    public static final String kCategoryKeyName	= "category";
    public static final String kClassIdKeyName	= "class";
    public static final String kServerIdKeyName	= "server";
    public static final String kDeployment      = "deployment-service";
    public static final String kModuleTypeKeyName	= "module-type";
    public static final String kServerInstanceKeyName	= "instance-name";
    public static final String kApplicationNameKeyName	= "app-name";
    public static final String kModuleNameKeyName	= "module-name";
    //constant names of property values
    public static final String kController		= "controller";
    public static final String kConfig			= "config";
    public static final String kServer			= "server";
    public static final String kGenericConfigurator	= "configurator";
    public static final String kSystemServices		= "system-services";
    public static final String kServerInstance		= "server-instance";
    public static final String kApplication		= "application";
    public static final String kModule			= "module";
    public static final String kJMSAdminHandler		= "jms-service";
    public static final String kLogService   		= "log-service";

    public static final String kComponentKeyName        = "component"; //
    public static final String kOrbType                 = "orb";
    public static final String kOrbListenerType         = "orblistener";

    public static final String kHTTPServiceType          = "httpservice";
    public static final String kHTTPListenerType         = "httplistener";
    public static final String kHTTPListenerIdKeyName    = "http-listener-id";
//    public static final String kHTTPConnectionGroupType  = "httpconnectiongroup";
    public static final String kSecurityServiceType      = "securityservice";

    public static final String kVirtualServerClassType   = "virtual-server-class";
    public static final String kVirtualServerClassDefaultName   = "defaultclass"; //only one class for now
    public static final String kVirtualServerClassIdKeyName = "virtual-server-class-id";
    public static final String kVirtualServerType        = "virtual-server";
    public static final String kVirtualServerIdKeyName   = "virtual-server-id";

    public static final String kMimeType                = "mime";
    public static final String kMimeIdKeyName           = "mime-id";
    public static final String kAclType                 = "acl";
    public static final String kAclIdKeyName            = "acl-id";
    public static final String kAuthDbType              = "authdb";
    public static final String kAuthDbIdKeyName         = "authdbId";
    public static final String kAuthRealmType           = "authrealm";
    public static final String kHTTPQosType             = "http-qos";

    public static final String kJdbcConnectionPoolType  = "jdbcpool";
    public static final String kJdbcResourceType        = "jdbc-resource";
    public static final String kMailResourceType        = "mail-resource";
    public static final String kJmsResourceType         = "jms-resource";
    public static final String kJndiResourceType        = "jndi-resource";
    public static final String kCustomResourceType      = "custom-resource";
    public static final String kPersistenceManagerFactoryResourceType   = 
        "persistence-manager-factory-resource";

    public static final String kWebContainer            = "web-container";
    public static final String kEjbContainer            = "ejb-container";
    public static final String kMdbContainer            = "mdb-container";
    
    public static final String kJtsComponent            = "transaction-service";
    public static final String kJvmType                 = "java-config";

    public static final String kEjbModule               = "ejb-module";
    public static final String kWebModule               = "web-module";
    public static final String kStandaloneEjbModule               = "standalone-ejb-module"; //only MBean naming type
    public static final String kStandaloneWebModule               = "standalone-web-module"; //only MBean naming type

    public static final String kConnectorModule         = "connector-module";
    public static final String kLifecycleModule         = "lifecycle-module";

    public static final String kEjbType                 = "ejb";
    public static final String kServletType             = "servlet";

    public static final String kMonitoringType          = "monitor";
    public static final String kMonitoringClassName     = "mclass";
    public static final String kMonitoringRootClass     = "root";

    public static final String kProfiler                = "profiler";

    public static final String kConfigCategory          = "config";
    public static final String kCategoryConfig          = Util.makeProp( kCategoryKeyName, kConfigCategory );
    
    /** 
            Making the only default constructor private.
    */
    private ObjectNames()
    {
    }
    
    // com.sun.appserv:type=hadb-config,category=config
    static private final ObjectName HADB_CONFIG_OBJECT_NAME =
        Util.newObjectName( kDefaultIASDomainName,
            Util.concatenateProps( kCategoryConfig, Util.makeProp( kTypeKeyName, "hadb-config") ) );
            
    public static ObjectName    getHADBConfigObjectName()   { return HADB_CONFIG_OBJECT_NAME; }
	
    /** 
            Returns the ObjectName of MBean representing Server Controller. 
            This is the Object Name	of the entity that makes all Server side 
            entities manageable.

            @return controller's ObjectName or null if something went wrong.
    */
    public static ObjectName getControllerObjectName()
    {
        return ( getObjectName(kDefaultIASDomainName,
                new String[]{kTypeKeyName},
                new String[]{kController}) );
    }
    
	 /** 
		Returns the ObjectName of MBean representing a <server> element. 

		@return <server> element's ObjectName, or null
    */
	public static ObjectName getServerObjectName( String serverName )
	{
		return ( getObjectName(kDefaultIASDomainName,
			new String[]{kTypeKeyName, kNameKeyName, kCategoryKeyName},
			new String[]{kServer, serverName, kConfig}) );
	}
    
    /**
        Returns the ObjectName of Generic Configurator.
    */

    public static ObjectName getGenericConfiguratorObjectName()
    {
        return ( getObjectName(kDefaultIASDomainName,
                new String[]{kTypeKeyName},
                new String[]{kGenericConfigurator}) );
    }
    /** Gets the System Service MBean Object Name in the Admin Server aka DAS.
     * This method has been deprecated. Use #getPerInstanceSystemServicesObjectName
     * instead.
     * @deprecated
     * @return the ObjectName of MBean responsible for System Services in the DAS.
     * The MBean returned has the ObjectName of the form <p>
     * com.sun.appserv:type=system-services,server=server
     * <p>
     * This is because the server-id of admin server (DAS) is "server".
     * @see SystemPropertyConstants
     */
    public static ObjectName getSystemServicesObjectName()
    {
        return ( getPerInstanceSystemServicesObjectName(SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME) );
    }

    /** Returns the ObjectName of the system services MBean that would
     * be available in a given server instance name. 
     * @param instanceId String representing the id of instance - may not be null
     * @return the ObjectName of such an MBean
     */
    public static ObjectName getPerInstanceSystemServicesObjectName(final String instanceId)
    {
         final ObjectName on = getObjectName(kDefaultIASDomainName,
                new String[]{kTypeKeyName, kServerIdKeyName},
                new String[]{kSystemServices, instanceId}) ;
         //System.out.println("[[DELETE]] Getting the System Services MBean ObjectName for instance: " + instanceId + ", and it is: " + on.toString());
         return ( on );
    }
    
    /**
    	<LLC>
    	ObjectNames for the MBeans supporting dotted names--
    	registry	-- associates dotted names with ObjectNames
    	resolver	-- resolves a dotted name to an ObjectName
    	get-set		-- supports the get/set/list commands within CLI
     */
    public static final String	kDottedNameDomainName	= "com.sun.appserv"; //kDefaultIASDomainName;
    public static final String	kDottedNameType					= "dotted-name-support";
    public static final String	kDottedNameRegistryNameKeyName	= "dotted-name-registry";
    public static final String	kDottedNameMonitoringRegistryNameKeyName	= "dotted-name-monitoring-registry";
    public static final String	kDottedNameGetSetNameKeyName	= "dotted-name-get-set";
    
	private static ObjectName getDottedNameObjectName( final String name )
	{
		return( getObjectName( kDottedNameDomainName,
				new String []	{ kNameKeyName, kTypeKeyName },
				new String []	{ name,			kDottedNameType }  )
				);
	}
     
    public static ObjectName getDottedNameRegistryObjectName()
    {
    	return( getDottedNameObjectName( kDottedNameRegistryNameKeyName ) );
    }
     
    public static ObjectName getDottedNameMonitoringRegistryObjectName()
    {
    	return( getDottedNameObjectName( kDottedNameMonitoringRegistryNameKeyName ) );
    }
    
    public static ObjectName getDottedNameGetSetObjectName()
    {
    	return( getDottedNameObjectName( kDottedNameGetSetNameKeyName ) );
    }
    
    /* </LLC> */
     
     
    /**
            Returns the ObjectName of MBean representing JMS Admin handler.
            This is the Object Name	of the entity that does all JMS
            related admin tasks.

            @return JMSAdminHandler's ObjectName or null if something went 
                    wrong.
    */
    public static ObjectName getJMSAdminHandlerObjectName(String instanceName)
    {
        return ( getObjectName(kDefaultIASDomainName,
                new String[]{kTypeKeyName, kServerInstanceKeyName},
                new String[]{kJMSAdminHandler, instanceName}) );
    }

    /**
     */
    public static ObjectName getJMSAdminHandlerObjectName()
    {
        Debug.println("This method is deprecated. Use the overloaded method" + 
                      " instead.", Debug.LOW);
        return ( getObjectName(kDefaultIASDomainName,
                new String[]{kTypeKeyName},
                new String[]{kJMSAdminHandler}) );
    }

    /**
            Returns ObjectName of MBean that represents a managed Server Instance, 
            with given name.
            @param instanceName String representing name of the Server Instance.
            @return ObjectName of Server Instance MBean or null if input is invalid.
    */
    public static ObjectName getServerInstanceObjectName(String instanceName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName},
                new String[]{kServerInstance, instanceName}) );
    }

    /**
     */
    public static ObjectName getServerInstanceObjectNamePattern()
    {
        ObjectName pattern = null;
        ObjectName tmp = getObjectName(kDefaultIASDomainName, 
        new String[] {kTypeKeyName}, new String[] {kServerInstance});
        if (tmp != null)
        {
            String strPattern = tmp.toString() + kPropertySeparatorChar;
            strPattern += kWildcardChar;
            try
            {
                pattern = new ObjectName(strPattern);
            }
            catch (MalformedObjectNameException mfone)
            {
                pattern = null;
                Debug.printStackTrace(mfone);
            }
        }
        return pattern;
    }

    /**
     * System property name used to toggle between DeploymentMBean 
     * and ManagedServerInstance.  Just Temporary till DeplMBean is tested.
     * will be removed and DeploymentMBean will remain
     */
    private static final String USE_DEPLOYMENT_MBEAN ="com.sun.aas.deployment.UseDeploymentMBean";
   
    /**
     * Returns objectName of the mbean representing deployment interface
     * @param instanceName name of the instance to which this mbean belongs to
     * @return objectName of deploymentMBean
     */
    public static ObjectName getDeploymentServiceObjectName(String instanceName)
    {
        if(System.getProperty(USE_DEPLOYMENT_MBEAN,"false").equals("true"))
        { 
             return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kServerInstanceKeyName},
                new String[]{kDeployment, instanceName}));
        }
        else {
            return getServerInstanceObjectName(instanceName);
        }
    }


    /**
            Returns the ObjectName of MBean that represents a managed J2EE
            application, with given name and deployed to given Server Instance.

            @param instanceName String representing  name of holding Server Instance.
            @param appName String representing the name of application.
            @return ObjectName of Application MBean or null if input is invalid.
    */
    public static ObjectName getApplicationObjectName(String instanceName,
            String appName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, 
                             kServerInstanceKeyName},
                new String[]{kApplication, appName, instanceName}) );
    }

    /**
     * Returns a patterned ObjectName that can be used to query the
     * MBeanServer for all the application mbeans that match this
     * pattern. This method uses the default domain to construct the
     * ObjectName.
     * For example given an instance name "ias1" this method should return
     * an ObjectName with pattern - 
     * "ias:type=application,instance-name=ias1,*"
     * @param instanceName The name of the instance for which the 
     * application pattern name will be generated.
     * @throws IllegalArgumentException if the inastanceName is null or
     * of 0 length.
     * @return Returns a valid ObjectName or null if unable to construct an
     * ObjectName.
     */
    public static ObjectName getApplicationObjectNamePattern(
                                                String instanceName)
    {
        ObjectName pattern = null;
        ObjectName tmp = getObjectName(kDefaultIASDomainName,
        new String[] {kTypeKeyName, kServerInstanceKeyName},
        new String[] {kApplication, instanceName});
        if (tmp != null)
        {
            String strPattern = tmp.toString() + kPropertySeparatorChar;
            strPattern += kWildcardChar;
            try
            {
                pattern = new ObjectName(strPattern);
            }
            catch (MalformedObjectNameException mfone)
            {
                Debug.printStackTrace(mfone);
                pattern = null;
            }
        }
        return pattern;
    }

    /**
            Returns the ObjectName of MBean that represents a managed J2EE
            module, with given name and deployed to given Server Instance
            as a part of given application (<strong> 
            not a standalone module)</strong>.

            @param instanceName String representing  name of holding Server Instance.
            @param appName String representing the name of enveloping application.
            @param moduleName name of the module.
            @return ObjectName of Application MBean or null if input is invalid.
    */
    public static ObjectName getModuleObjectName(String instanceName,
            String appName, String moduleName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, kApplicationNameKeyName, 
                             kServerInstanceKeyName},
                new String[]{kModule, moduleName, appName, instanceName}) );
    }

    public static ObjectName getEjbModuleObjectName(String instanceName,
                                                    String moduleName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, 
                             kServerInstanceKeyName, kModuleTypeKeyName},
                new String[]{kModule, moduleName, instanceName, kEjbModule}) );
    }

    public static ObjectName getEjbModuleObjectName(String instanceName,
                                                    String moduleName, 
                                                    String applicationName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, 
                             kServerInstanceKeyName, kModuleTypeKeyName, 
                             kApplicationNameKeyName},
                new String[]{kModule, moduleName, instanceName, kEjbModule, 
                             applicationName}) );
    }

    public static ObjectName getWebModuleObjectName(String instanceName,
                                                    String moduleName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName,  
                             kServerInstanceKeyName, kModuleTypeKeyName},
                new String[]{kModule, moduleName, instanceName, kWebModule}) );
    }

    public static ObjectName getWebModuleObjectName(String instanceName,
                                                    String moduleName, 
                                                    String applicationName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, 
                             kServerInstanceKeyName, kModuleTypeKeyName, 
                             kApplicationNameKeyName},
                new String[]{kModule, moduleName, instanceName, kWebModule, 
                             applicationName}) );
    }

    public static ObjectName getConnectorModuleObjectName(String instanceName,
                                                          String moduleName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName,  
                             kServerInstanceKeyName, kModuleTypeKeyName},
                new String[]{kModule, moduleName, instanceName, 
                             kConnectorModule}) );
    }

    public static ObjectName getConnectorModuleObjectName(
                                                      String instanceName,
                                                      String moduleName, 
                                                      String applicationName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, 
                             kServerInstanceKeyName, kModuleTypeKeyName, 
                             kApplicationNameKeyName},
                new String[]{kModule, moduleName, instanceName, 
                             kConnectorModule, applicationName}) );
    }

    /**
     */
    public static ObjectName getModuleObjectNamePattern(String instanceName,
            String appName)
    {
        ObjectName pattern = null;

        ObjectName tmp = getObjectName(kDefaultIASDomainName,
            new String[] {kTypeKeyName, kServerInstanceKeyName, 
                kApplicationNameKeyName},
            new String[] {kModule, instanceName, appName});

        if (tmp != null)
        {
            String strPattern = tmp.toString() + kPropertySeparatorChar;
            strPattern += kWildcardChar;
            try
            {
                pattern = new ObjectName(strPattern);
            }
            catch (MalformedObjectNameException mfone)
            {
                pattern = null;
            }
        }
        return pattern;
    }

    /**
            Returns the ObjectName of MBean that represents a managed J2EE
            module, with given name and deployed to given Server Instance
            as a <strong> standalone module </strong>. Note that this is an
            overloaded method.

            @param instanceName String representing  name of holding Server Instance.
            @param moduleName name of the module.
            @return ObjectName of Application MBean or null if input is invalid.
    */
    public static ObjectName getModuleObjectName(String instanceName,
            String moduleName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, kServerInstanceKeyName},
                new String[]{kModule, moduleName, instanceName}) );
    }

    /**
     */
    public static ObjectName getModuleObjectNamePattern(String instanceName)
    {
        ObjectName pattern = null;
        ObjectName tmp = getObjectName(kDefaultIASDomainName,
        new String[] {kTypeKeyName, kServerInstanceKeyName},
        new String[] {kModule, instanceName});
        if (tmp != null)
        {
            try
            {
                String strPattern = tmp.toString() + kPropertySeparatorChar;
                strPattern += kWildcardChar;
                pattern = new ObjectName(strPattern);
            }
            catch (MalformedObjectNameException mfone)
            {
                pattern = null;
                Debug.printStackTrace(mfone);
            }
        }
        return pattern;
    }

    public static ObjectName getORBObjectName(String instanceName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kServerInstanceKeyName},
                new String[]{kOrbType, instanceName}));
    }

    public static ObjectName getIiopListenerObjectName(String instanceName, 
                                                       String listenerId)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kServerInstanceKeyName, 
                             kNameKeyName},
                new String[]{kOrbListenerType, instanceName, listenerId}));
    }

        public static ObjectName 
    getJDBCConnectionPoolObjectName(String instanceName, String poolName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kServerInstanceKeyName, 
                             kNameKeyName},
                new String[]{kJdbcConnectionPoolType, instanceName, poolName}));
    }

        public static ObjectName 
    getJDBCResourceObjectName(String instanceName, String jndiName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kServerInstanceKeyName, 
                             kNameKeyName},
                new String[]{kJdbcResourceType, instanceName, jndiName}));
    }

        public static ObjectName 
    getJNDIResourceObjectName(String instanceName, String jndiName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kServerInstanceKeyName, 
                             kNameKeyName},
                new String[]{kJndiResourceType, instanceName, jndiName}));
    }

        public static ObjectName 
    getJMSResourceObjectName(String instanceName, String jndiName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kServerInstanceKeyName, 
                             kNameKeyName},
                new String[]{kJmsResourceType, instanceName, jndiName}));
    }

        public static ObjectName 
    getPersistenceManagerFactoryResourceObjectName(String instanceName, 
                                                   String jndiName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kServerInstanceKeyName, 
                             kNameKeyName},
                new String[]{kPersistenceManagerFactoryResourceType, 
                             instanceName, jndiName}));
    }

        public static ObjectName 
    getMailResourceObjectName(String instanceName, String jndiName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kServerInstanceKeyName, 
                             kNameKeyName},
                new String[]{kMailResourceType, instanceName, jndiName}));
    }

        public static ObjectName 
    getCustomResourceObjectName(String instanceName, String jndiName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kServerInstanceKeyName, 
                             kNameKeyName},
                new String[]{kCustomResourceType, instanceName, jndiName}));
    }

    public static ObjectName getWebContainerObjectName(String instanceName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kWebContainer, instanceName}));
    }

    public static ObjectName getEjbContainerObjectName(String instanceName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kEjbContainer, instanceName}));
    }

    public static ObjectName getMdbContainerObjectName(String instanceName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kMdbContainer, instanceName}));
    }

    public static ObjectName getJVMComponentObjectName(String instanceName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[] {kTypeKeyName, 
                              kServerInstanceKeyName}, 
                new String[] {kJvmType, instanceName}));
    }

    public static ObjectName getJTSComponentObjectName(String instanceName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kJtsComponent, instanceName}));
    }

    public static ObjectName getLogServiceObjectName(String instanceName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kLogService, instanceName}));
    }

    public static ObjectName getSecurityServiceObjectName(String instanceName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kSecurityServiceType, instanceName}));
    }

    public static ObjectName getHttpListenerObjectName(String instanceName, 
                                                       String id)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kHTTPListenerType, id, instanceName}));
    }

/*        public static ObjectName 
    getHttpConnectionGroupObjectName(String instanceName, 
                                     String listenerId, 
                                     String connectionGroupId)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, 
                             kHTTPListenerIdKeyName, kServerInstanceKeyName}, 
                new String[]{kHTTPConnectionGroupType, connectionGroupId, 
                             listenerId, instanceName}));
    }
*/
    public static ObjectName getHttpServiceObjectName(String instanceName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kServerInstanceKeyName}, 
                new String[]{kHTTPServiceType, instanceName}));
    }

    public static ObjectName getVirtualServerClassObjectName(
                                                        String  instanceName, 
                                                        String  classId)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kNameKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kVirtualServerClassType, classId, instanceName}));
    }

    public static ObjectName getVirtualServerObjectName(
                                            String  instanceName, 
                                            String  virtualServerClassId, 
                                            String  virtualServerId)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, 
                             kVirtualServerClassIdKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kVirtualServerType, 
                             virtualServerId, 
                             virtualServerClassId, 
                             instanceName}));
    }

        public static ObjectName 
    getVirtualServerAuthDBObjectName(String instanceName, 
                                     String virtualServerClassId, 
                                     String virtualServerId, 
                                     String authDBId)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, 
                             kNameKeyName, 
                             kVirtualServerIdKeyName, 
                             kVirtualServerClassIdKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kAuthDbType, 
                             authDBId, 
                             virtualServerId, 
                             virtualServerClassId, 
                             instanceName}));
    }

        public static ObjectName 
    getMimeObjectName(String instanceName, String id)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kMimeType, id, 
                             instanceName}));
    }

        public static ObjectName 
    getAclObjectName(String instanceName, String id)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kAclType, id, 
                             instanceName}));
    }

        public static ObjectName 
    getLifeCycleModuleObjectName(String instanceName, String name)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kLifecycleModule, name, 
                             instanceName}));
    }

    public static ObjectName getEjbObjectName(String instanceName, 
                                              String moduleName, 
                                              String ejbName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, kModuleNameKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kEjbType, ejbName, moduleName, instanceName}));
    }

    public static ObjectName getEjbObjectName(String instanceName, 
                                              String appName, 
                                              String moduleName, 
                                              String ejbName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, kModuleNameKeyName, 
                             kApplicationNameKeyName, kServerInstanceKeyName}, 
                new String[]{kEjbType, ejbName, moduleName, appName, 
                             instanceName}));
    }

    public static ObjectName getServletObjectName(String instanceName, 
                                                  String moduleName, 
                                                  String servletName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, kModuleNameKeyName, 
                             kServerInstanceKeyName}, 
                new String[]{kServletType, servletName, moduleName, 
                             instanceName}));
    }

    public static ObjectName getServletObjectName(String instanceName, 
                                                  String appName, 
                                                  String moduleName, 
                                                  String servletName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kNameKeyName, kModuleNameKeyName, 
                             kApplicationNameKeyName, kServerInstanceKeyName}, 
                new String[]{kServletType, servletName, moduleName, appName, 
                             instanceName}));
    }

    /**
        Get ObjectName for root monitoring mBean.
        @param instanceName name of the server instance
        @return jmx ObjectName for root monitoring MBean
    */
    public static ObjectName getRootMonitorMBeanName(String instanceName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kMonitoringClassName,
                             kServerInstanceKeyName, kNameKeyName},
                new String[]{kMonitoringType, kMonitoringRootClass,
                             instanceName, kMonitoringRootClass}));
    }

    /**
     */
    public static ObjectName getProfilerObjectName(String instanceName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kServerInstanceKeyName},
                new String[]{kProfiler, instanceName}));
    }

    /**
     */
    public static ObjectName getAuthRealmObjectName(String  instanceName, 
                                                    String  realmName)
    {
        return ( getObjectName (kDefaultIASDomainName,
                new String[]{kTypeKeyName, kServerInstanceKeyName, 
                             kNameKeyName},
                new String[]{kAuthRealmType, instanceName, realmName}));
    }

    /**
            A generic method to create the Object Name of an MBean in given
            domain with specified arrays of Property Names-Values.

            @param domainName String representing the name of domain. May not
                    be null. May not contain any DomainInvalidator characters.
            @param keyNames Array of Strings representing the Property Key Names.
            @param keyValues Array of Strings representing the Property Key Values.
            None of the names, values (and arrays themseleves) may be null.
            @return valid ObjectName with given params, null if there is any error.
    */
    public static ObjectName getObjectName(String domainName, 
            String[] keyNames, String[] keyValues)
    {
        ObjectName validObjectName = null;

        try
        {
            if (domainName                      == null	||
                !isDomainNameValid(domainName)          ||
                keyNames                        == null ||
                keyValues                       == null ||
                keyNames.length < 1                     ||
                keyNames.length != keyValues.length)
            {
                throw new IllegalArgumentException();
            }
            StringBuffer nameBuffer = new StringBuffer();
            nameBuffer.append(domainName);
            nameBuffer.append(kDomainNameSeparatorChar);
            for (int i = 0 ; i < keyNames.length ; i++)
            {
                if (keyNames[i] == null || keyValues[i] == null)
                {
                    throw new IllegalArgumentException();
                }
                nameBuffer.append(keyNames[i]);
                nameBuffer.append(kNameValueSeparatorChar);
                nameBuffer.append(keyValues[i]);
                if (i != (keyNames.length-1))
                {
                    nameBuffer.append(kPropertySeparatorChar);
                }
            }
            validObjectName = new ObjectName(nameBuffer.toString());
        }
        catch(Exception e)
        {
            Debug.printStackTrace(e);
        }
        return ( validObjectName );
    }

    public static ObjectName getAllObjectNamesPattern()
    {
        String name = kDefaultIASDomainName + kDomainNameSeparatorChar + kWildcardChar;
        try
        {
           return new ObjectName(name);
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    private static boolean isDomainNameValid(String name)
    {
        boolean isValid = false;

        isValid =   (name.indexOf(kWildcardChar)                == -1) &&
                    (name.indexOf(kDomainNameSeparatorChar)	== -1) &&
                    (name.indexOf(kPropertySeparatorChar)	== -1) &&
                    (name.indexOf(kNameValueSeparatorChar)	== -1) &&
                    (name.indexOf(kSingleMatchChar)		== -1); 

        return ( isValid );
    }

    public static void main(String[] args) throws Exception
    {
        Logger.log(ObjectNames.getServerInstanceObjectName("ias1"));
        Logger.log(ObjectNames.getApplicationObjectName("ias1", "app1"));
        Logger.log(ObjectNames.getControllerObjectName());
        Logger.log(ObjectNames.getModuleObjectName("ias1", "standalone"));
        Logger.log(ObjectNames.getModuleObjectName("ias1", "app1", "inner"));
        Logger.log(ObjectNames.getObjectName("ias111",
                new String[]{"key1", "key2", "key3"},
                new String[]{"v1", "v2", "v3"}));

        //Testers for patterns
        ObjectName pattern =  
            ObjectNames.getServerInstanceObjectNamePattern();
        Logger.log("" + pattern.isPropertyPattern());
        pattern = ObjectNames.getApplicationObjectNamePattern("ias1");
        Logger.log("" + pattern.isPropertyPattern());
        pattern = ObjectNames.getModuleObjectNamePattern("ias1");
        Logger.log("" + pattern.isPropertyPattern());
        pattern = ObjectNames.getModuleObjectNamePattern("ias1", "bank");
        Logger.log("" + pattern.isPropertyPattern());
        
        ObjectName jmsObjectName = 
            ObjectNames.getJMSAdminHandlerObjectName("ias1");
        Logger.log("JMS Objectname = " + jmsObjectName.toString());
        jmsObjectName = ObjectNames.getJMSAdminHandlerObjectName();
        Logger.log("JMS Objectname = " + jmsObjectName.toString());

        ObjectName httpListener = ObjectNames.getHttpListenerObjectName(
                                    "ias1", "listener1");
        Logger.log("Http listener object name = " + httpListener);

        
        ObjectName mime = ObjectNames.getMimeObjectName("ias1", "text/html");
        Logger.log("Mime object name = " + mime);

        ObjectName acl = ObjectNames.getAclObjectName("ias1", "acl1");
        Logger.log("ACL object name = " + acl);

    }
}
