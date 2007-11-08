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
	PROPRIETARY/CONFIDENTIAL. Use of this product is subject
	to license terms. Copyright (c) 2002 Sun Microsystems, Inc.
        All rights reserved.
	
	$Id: MBeansDescriptions.java,v 1.4 2005/12/25 04:14:34 tcfujii Exp $
 */

package com.sun.enterprise.admin.server.core.mbean.config.naming;

import com.sun.enterprise.admin.common.ObjectNames;
import com.sun.enterprise.config.serverbeans.ServerXPathHelper;
/**
    Provides naming MBeans descriptions for ConfigMbeans
*/
public class  MBeansDescriptions
{
    final static String CONFIG_MBEANS_BASE_CLASS_PREFIX    = "com.sun.enterprise.admin.server.core.mbean.config.";
    final static String DOMAIN_PROLOG = "ias:";
    final static char   PATTERNS_SEPARATOR                 = '|';

    final static int  MODE_CONFIG       = 0x0001;
    final static int  MODE_MONITOR      = 0x0002;

    //CONFIG MBEANS NAMING DATA
    final static Object [][] mbean_descriptions = 
    {
        //+++++++++++++ SERVER-INSTANCE
        {   
            ObjectNames.kServerInstance,
            MODE(MODE_CONFIG),
            "{0}",                   //dotted name pattern(s)
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kServerInstance,
                            ObjectNames.kNameKeyName, "{0}"),
            XPATH_TO_MASK(ServerXPathHelper.getServerIdXpath("{0}")),               //XPATH pattern
            "ManagedServerInstance",                 //MBean ClassName
        },
        //+++++++++++++ HTTP-SERVICE
        {   
            ObjectNames.kHTTPServiceType,
            MODE(MODE_CONFIG | MODE_MONITOR),
            "{0}.http-service",                   //dotted name pattern(s)
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kHTTPServiceType,
                            ObjectNames.kServerInstanceKeyName, "{0}"),
            XPATH_TO_MASK(ServerXPathHelper.getHTTPServiceXpath()),               //XPATH pattern
            "ManagedHTTPService",                 //MBean ClassName
        },
        //+++++++++++++ HTTP-LISTENER
        {   
            ObjectNames.kHTTPListenerType,
            MODE(MODE_CONFIG),
            "{0}.http-listener.{1} | {0}.http-service.http-listener.{1}",   //dotted name pattern(s)
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kHTTPListenerType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}"), 
            XPATH_TO_MASK(ServerXPathHelper.getHTTPListenerIdXpath("{1}")),    //XPATH pattern
            "ManagedHTTPListener",                              //MBean ClassName
        },
        //+++++++++++++ IIOP-SERVICE
        {   
            ObjectNames.kOrbType,
            MODE(MODE_CONFIG),
            "{0}.orb | {0}.iiop-service" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kOrbType,
                            ObjectNames.kServerInstanceKeyName, "{0}"),
            XPATH_TO_MASK(ServerXPathHelper.getIIOPServiceXpath()),               //XPATH pattern
            "ManagedORBComponent",              //MBean ClassName
        },
        //+++++++++++++ IIOP-LISTENER
        {   
            ObjectNames.kOrbListenerType,
            MODE(MODE_CONFIG),
            "{0}.orblistener.{1} | {0}.iiop-listener.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kOrbListenerType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}"),
            XPATH_TO_MASK(ServerXPathHelper.getIIOPListenerIdXpath("{1}")),    //XPATH pattern
            "ManagedORBListener",              //MBean ClassName
        },
        {   
            ObjectNames.kJdbcResourceType,
            MODE(MODE_CONFIG),
            "{0}.jdbc-resource.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kJdbcResourceType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getJDBCResourceIdXpath("{1}")),    //XPATH pattern
            "ManagedJDBCResource",   //MBean ClassName
        },
        {   
            ObjectNames.kJdbcConnectionPoolType,
            MODE(MODE_CONFIG),
            "{0}.jdbc-connection-pool.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kJdbcConnectionPoolType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getJDBCConnectionPoolIdXpath("{1}")),   //XPATH pattern
            "ManagedJDBCConnectionPool",   //MBean ClassName
        },
        {   
            ObjectNames.kJndiResourceType,
            MODE(MODE_CONFIG),
            "{0}.external-jndi-resource.{1} | {0}.jndi-resource.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kJndiResourceType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getJNDIResourceIdXpath("{1}")),   //XPATH pattern
            "ManagedJNDIResource",   //MBean ClassName
        },
        {   
            ObjectNames.kCustomResourceType,
            MODE(MODE_CONFIG),
            "{0}.custom-resource.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kCustomResourceType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getCustomResourceIdXpath("{1}")),   //XPATH pattern
            "ManagedCustomResource",   //MBean ClassName
        },
        {   
            ObjectNames.kJtsComponent,
            MODE(MODE_CONFIG | MODE_MONITOR),
            "{0}.transaction-service",
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kJtsComponent,
                             ObjectNames.kServerInstanceKeyName, "{0}"),
            XPATH_TO_MASK(ServerXPathHelper.getTransactionServiceXpath()),   //XPATH pattern
            "ManagedTransactionService",   //MBean ClassName

        },
        {   
            ObjectNames.kMdbContainer,
            MODE(MODE_CONFIG),
            "{0}.mdb-container",
             ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kMdbContainer,
                             ObjectNames.kServerInstanceKeyName, "{0}"),
            XPATH_TO_MASK(ServerXPathHelper.getMDBContainerXpath()),   //XPATH pattern
            "ManagedMdbContainer",   //MBean ClassName

        },
        {   
            ObjectNames.kEjbContainer,
            MODE(MODE_CONFIG),
            "{0}.ejb-container",
             ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kEjbContainer,
                             ObjectNames.kServerInstanceKeyName, "{0}"),
            XPATH_TO_MASK(ServerXPathHelper.getEJBContainerXpath()),   //XPATH pattern
            "ManagedEjbContainer",   //MBean ClassName

        },
        {   
            ObjectNames.kWebContainer,
            MODE(MODE_CONFIG),
            "{0}.web-container",
             ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kWebContainer,
                             ObjectNames.kServerInstanceKeyName, "{0}"),
            XPATH_TO_MASK(ServerXPathHelper.getWEBContainerXpath()),   //XPATH pattern
            "ManagedWebContainer",   //MBean ClassName

        },
        {   
            ObjectNames.kJMSAdminHandler,
            MODE(MODE_CONFIG),
            "{0}.jms-service",
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kJMSAdminHandler,
                             ObjectNames.kServerInstanceKeyName, "{0}"),
            XPATH_TO_MASK(ServerXPathHelper.getJmsServiceXpath()),   //XPATH pattern
            "JMSAdminHandler",   //MBean ClassName

        },
        {   
            ObjectNames.kJvmType,
            MODE(MODE_CONFIG),
            "{0}.java-config",
             ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kJvmType,
                             ObjectNames.kServerInstanceKeyName, "{0}"),
            XPATH_TO_MASK(ServerXPathHelper.getJavaConfigXpath()),   //XPATH pattern
            "ManagedJVM",   //MBean ClassName
        },
        {   
            ObjectNames.kLogService,
            MODE(MODE_CONFIG),
            "{0}.log-service",
             ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kLogService,
                             ObjectNames.kServerInstanceKeyName, "{0}"),
            XPATH_TO_MASK(ServerXPathHelper.getLogServiceXpath()),   //XPATH pattern
            "ManagedLogService",   //MBean ClassName

        },
        {   
            ObjectNames.kSecurityServiceType,
            MODE(MODE_CONFIG),
            "{0}.security-service" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kSecurityServiceType,
                            ObjectNames.kServerInstanceKeyName, "{0}"),
            XPATH_TO_MASK(ServerXPathHelper.getSecurityServiceXpath()),   //XPATH pattern
            "ManagedSecurityService",   //MBean ClassName
        },
        {   
            ObjectNames.kApplication,
            MODE(MODE_CONFIG),
            "{0}.j2ee-application.{1} | {0}.application.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kApplication,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getAppIdXpathExpression("{1}")),   //XPATH pattern
            "ManagedJ2EEApplication",   //MBean ClassName
        },
        {   
            ObjectNames.kStandaloneEjbModule,
            MODE(MODE_CONFIG),
            "{0}.ejb-module.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kModule,
                            ObjectNames.kModuleTypeKeyName, ObjectNames.kEjbModule,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getEjbModuleIdXpathExpression("{1}")),   //XPATH pattern
            "ManagedStandaloneJ2EEEjbJarModule",   //MBean ClassName
        },
        {   
            ObjectNames.kEjbModule,
            MODE(MODE_CONFIG),
            "{0}.j2ee-application.{1}.ejb-module.{2} | {0}.application.{1}.ejb-module.{2}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kModule,
                            ObjectNames.kModuleTypeKeyName, ObjectNames.kEjbModule,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{2}",
                            ObjectNames.kApplicationNameKeyName, "{1}"),
            "",   //XPATH pattern
            "ManagedJ2EEEjbJarModule",   //MBean ClassName 
        },
        {   
            ObjectNames.kStandaloneWebModule,
            MODE(MODE_CONFIG),
            "{0}.web-module.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kModule,
                            ObjectNames.kModuleTypeKeyName, ObjectNames.kWebModule,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getWebModuleIdXpathExpression("{1}")),   //XPATH pattern
            "ManagedStandaloneJ2EEWebModule",   //MBean ClassName
        },
        {   
            ObjectNames.kWebModule,
            MODE(MODE_CONFIG),
            "{0}.j2ee-application.{1}.web-module.{2} | {0}.application.{1}.web-module.{2}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kModule,
                            ObjectNames.kModuleTypeKeyName, ObjectNames.kWebModule,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kApplicationNameKeyName, "{1}",
                            ObjectNames.kNameKeyName, "{2}" ),
            "",   //XPATH pattern
            "ManagedJ2EEWebModule",   //MBean ClassName
        },
        {   
            ObjectNames.kConnectorModule,
            MODE(MODE_CONFIG),
            "{0}.connector-module.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kModule,
                            ObjectNames.kModuleTypeKeyName, ObjectNames.kConnectorModule,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getConnectorModuleIdXpathExpression("{1}")),   //XPATH pattern
            "ManagedStandaloneConnectorModule",   //MBean ClassName
        },
    /*    {   
            ObjectNames.kHTTPConnectionGroupType,
            MODE(MODE_CONFIG),
            "{0}.connectiongroup.*.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kHTTPConnectionGroupType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kHTTPListenerIdKeyName, "{1}",
                            ObjectNames.kNameKeyName, "{3}" 
        },
    */
        {   
            ObjectNames.kLifecycleModule,
            MODE(MODE_CONFIG),
            "{0}.lifecycle-module.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kLifecycleModule,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getLifecycleModuleIdXpath("{1}")),   //XPATH pattern
            "ManagedLifecycleModule",   //MBean ClassName
        },
        {   
            ObjectNames.kVirtualServerClassType,
            MODE(MODE_CONFIG),
            "{0}.virtual-server-class" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kVirtualServerClassType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, ObjectNames.kVirtualServerClassDefaultName ),
            XPATH_TO_MASK(ServerXPathHelper.getHTTPServiceXpath()),   //XPATH pattern
            "ManagedVirtualServerClass",   //MBean ClassName
        },
        {   
            ObjectNames.kVirtualServerType,
            MODE(MODE_CONFIG),
            "{0}.virtual-server.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kVirtualServerType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kVirtualServerClassIdKeyName, ObjectNames.kVirtualServerClassDefaultName, 
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getVirtualServerIdXpath(ObjectNames.kVirtualServerClassDefaultName, "{1}")),   //XPATH pattern
            "ManagedVirtualServer",   //MBean ClassName
        },
/*
        {   
            ObjectNames.kMimeType,
            MODE(MODE_CONFIG),
            "{0}.mime.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kMimeType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getHTTPMimeIdXpath("{1}")),   //XPATH pattern
            "ManagedHTTPMime",   //MBean ClassName
        },
        {   
            ObjectNames.kAclType,
            MODE(MODE_CONFIG),
            "{0}.acl.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kAclType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getHTTPAclIdXpath("{1}")),   //XPATH pattern
            "ManagedHTTPAcl",   //MBean ClassName
        },
        {   
            ObjectNames.kAuthDbType,
            MODE(MODE_CONFIG),
            "{0}.virtual-server.{1}.auth-db.{2}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kAuthDbType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kVirtualServerClassIdKeyName, ObjectNames.kVirtualServerClassDefaultName, 
                            ObjectNames.kVirtualServerIdKeyName, "{1}", 
                            ObjectNames.kNameKeyName, "{2}" ),
            XPATH_TO_MASK(ServerXPathHelper.getAuthDbIdXpath(ObjectNames.kVirtualServerClassDefaultName, "{1}", "{2}")),   //XPATH pattern
            "ManagedAuthDb",   //MBean ClassName
        },
*/
        {   
            ObjectNames.kAuthRealmType,
            MODE(MODE_CONFIG),
            "{0}.authrealm.{1} | {0}.security-service.authrealm.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kAuthRealmType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getSecurityAuthRealmIdXpath("{1}")),   //XPATH pattern
            "ManagedAuthRealm",   //MBean ClassName
        },
        {   
            ObjectNames.kPersistenceManagerFactoryResourceType,
            MODE(MODE_CONFIG),
            "{0}.persistence-manager-factory-resource.{1} | {0}.resources.persistence-manager-factory-resource.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kPersistenceManagerFactoryResourceType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getPMFactoryResourceIdXpath("{1}")),   //XPATH pattern
            "ManagedPMFactoryResource",   //MBean ClassName
        },
/*
        {   
            ObjectNames.kAclType,
            MODE(MODE_CONFIG),
            "{0}.http-service.acl.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kAclType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getHTTPAclIdXpath("{1}")),   //XPATH pattern
            "ManagedHTTPAcl",   //MBean ClassName
        },
*/
        {   
            ObjectNames.kMailResourceType,
            MODE(MODE_CONFIG),
            "{0}.mail-resource.{1}" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kMailResourceType,
                            ObjectNames.kServerInstanceKeyName, "{0}",
                            ObjectNames.kNameKeyName, "{1}" ),
            XPATH_TO_MASK(ServerXPathHelper.getMailResourceIdXpath("{1}")),   //XPATH pattern
            "ManagedJavaMailResource",   //MBean ClassName
        },
        {   
            ObjectNames.kProfiler,
            MODE(MODE_CONFIG),
            "{0}.profiler" ,  
            ObjectNamePairs(ObjectNames.kTypeKeyName, ObjectNames.kProfiler,
                            ObjectNames.kServerInstanceKeyName, "{0}" ),
            XPATH_TO_MASK(ServerXPathHelper.getProfilerXpath()),   //XPATH pattern
            "ManagedProfiler",   //MBean ClassName
        },
       } ;

    

       
    //*************************************************************************************
    static String ObjectNamePairs(String name1, String value1, String name2, String value2)
    {
        return ObjectNamePairs(new String[]{name1, value1, name2, value2});
    }
    static String ObjectNamePairs(String name1, String value1, String name2, String value2, String name3, String value3)
    {
        return ObjectNamePairs(new String[]{name1, value1, name2, value2, name3, value3});
    }
    static String ObjectNamePairs(String name1, String value1, String name2, String value2, String name3, String value3, String name4, String value4)
    {
        return ObjectNamePairs(new String[]{name1, value1, name2, value2, name3, value3, name4, value4});
    }
    static String ObjectNamePairs(String name1, String value1, String name2, String value2, String name3, String value3, String name4, String value4, String name5, String value5)
    {
        return ObjectNamePairs(new String[]{name1, value1, name2, value2, name3, value3, name4, value4, name5, value5});
    }
    static String ObjectNamePairs(String[] pairs)
    {
        String str = null;
        for(int i=0; i<pairs.length-1; i = i+2)
            if(i==0)
               str = DOMAIN_PROLOG + pairs[i] + "=" + pairs[i+1];
            else
               str = str + "," + pairs[i] + "=" + pairs[i+1];
//System.out.println("+++++++++++++++++++++ objname="+str);            
        return str;
    }
    
    private static Integer MODE(int type)
    {
       return new Integer(type);    
    }
    
    private static String XPATH_TO_MASK(String xpath)
    {
        char[] chrs =  xpath.toCharArray();
        char[] newchrs = new char[chrs.length*2];
        int j = 0;
        for(int i=0; i<chrs.length; i++)
        {
            newchrs[j++] = chrs[i];
            if(chrs[i]=='\'')
                newchrs[j++] = '\'';
        }
        return String.valueOf(newchrs, 0, j);
    }
}    
