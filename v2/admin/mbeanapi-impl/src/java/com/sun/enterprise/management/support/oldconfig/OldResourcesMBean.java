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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
 
/*
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/support/oldconfig/OldResourcesMBean.java,v 1.2 2005/12/25 03:41:09 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2005/12/25 03:41:09 $
 */
 
/**
	Generated: Wed Apr 14 23:39:44 PDT 2004
	Generated from:
	com.sun.appserv:type=resources,category=config
*/

package com.sun.enterprise.management.support.oldconfig;

import java.util.Properties;
//import com.sun.enterprise.admin.common.JMSStatus;
import javax.management.ObjectName;
import javax.management.AttributeList;

/**
	Implementing class was: com.sun.enterprise.admin.mbeans.ResourcesMBean
*/

public interface OldResourcesMBean {

        //public JMSStatus        JMSPing();
        //public JMSStatus        JMSPing( final String param1, final String param2, final int param3 );
        public ObjectName       createAdminObjectResource( final AttributeList attribute_list );
        public ObjectName       createAdminObjectResource( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createConnectorConnectionPool( final AttributeList attribute_list );
        public ObjectName       createConnectorConnectionPool( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createConnectorResource( final AttributeList attribute_list );
        public ObjectName       createConnectorResource( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createCustomResource( final AttributeList attribute_list );
        public ObjectName       createCustomResource( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createExternalJndiResource( final AttributeList attribute_list );
        public ObjectName       createExternalJndiResource( final AttributeList param1, final Properties param2, final String param3 );
        public void     createJMSDestination( final String param1, final String param2, final Properties param3 );
        public ObjectName       createJdbcConnectionPool( final AttributeList attribute_list );
        public ObjectName       createJdbcConnectionPool( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createJdbcResource( final AttributeList attribute_list );
        public ObjectName       createJdbcResource( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createJmsConnectionFactory( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createJmsDestinationResource( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createJmsResource( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createMailResource( final AttributeList attribute_list );
        public ObjectName       createMailResource( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createPMFResourceWithJDBCResource( final AttributeList param1, final Properties param2, final String param3 );
        public ObjectName       createPersistenceManagerFactoryResource( final AttributeList attribute_list );
        public ObjectName       createPersistenceManagerFactoryResource( final AttributeList param1, final Properties param2, final String param3 );
        public void     createPhysicalDestination( final AttributeList param1, final Properties param2, final String param3 );
        public java.util.ArrayList      createResource( final String param1, final String param2 );
        public ObjectName       createResourceAdapterConfig( final AttributeList attribute_list );
        public ObjectName       createResourceAdapterConfig( final AttributeList param1, final Properties param2, final String param3 );
        public void     createResourceReference( final String param1, final boolean param2, final String param3 );
        public void     deleteAdminObjectResource( final String param1, final String param2 );
        public void     deleteConnectorConnectionPool( final String param1, final String param2 );
        public void     deleteConnectorConnectionPool( final String param1, final Boolean param2, final String param3 );
        public void     deleteConnectorResource( final String param1, final String param2 );
        public void     deleteCustomResource( final String param1, final String param2 );
        public void     deleteExternalJndiResource( final String param1, final String param2 );
        public void     deleteJMSDestination( final String param1, final String param2 );
        public void     deleteJdbcConnectionPool( final String param1, final String param2 );
        public void     deleteJdbcConnectionPool( final String param1, final Boolean param2, final String param3 );
        public void     deleteJdbcResource( final String param1, final String param2 );
        public void     deleteJmsConenctionFactory( final String param1, final String param2 );
        public void     deleteJmsDestinationResource( final String param1, final String param2 );
        public void     deleteJmsResource( final String param1, final String param2 );
        public void     deleteMailResource( final String param1, final String param2 );
        public void     deletePersistenceManagerFactoryResource( final String param1, final String param2 );
        public void     deletePhysicalDestination( final String param1, final String param2, final String param3 );
        public void     deleteResourceAdapterConfig( final String param1, final String param2 );
        public void     deleteResourceReference( final String param1, final String param2 );
        public boolean  destroyConfigElement();
        public Properties       getActivationConfPropTypes( final String param1, final String param2 );
        public Properties       getActivationConfProps( final String param1, final String param2 );
        public Properties       getAdminObjectConfigProps( final AttributeList param1 );
        public String[] getAdminObjectInterfaceNames( final String param1 );
        public javax.management.ObjectName[]    getAdminObjectResource();
        public javax.management.ObjectName[]    getAdminObjectResource( final String param1 );
        public ObjectName       getAdminObjectResourceByJndiName( final String key );
        public ObjectName       getAdminObjectResourceByJndiName( final String param1, final String param2 );
        public String[] getConnectionDefinitionNames( final String param1 );
        public javax.management.ObjectName[]    getConnectorConnectionPool();
        public javax.management.ObjectName[]    getConnectorConnectionPool( final String param1 );
        public ObjectName       getConnectorConnectionPoolByName( final String key );
        public ObjectName       getConnectorConnectionPoolByName( final String param1, final String param2 );
        public javax.management.ObjectName[]    getConnectorResource();
        public javax.management.ObjectName[]    getConnectorResource( final String param1 );
        public ObjectName       getConnectorResourceByJndiName( final String key );
        public ObjectName       getConnectorResourceByJndiName( final String param1, final String param2 );
        public javax.management.ObjectName[]    getCustomResource();
        public javax.management.ObjectName[]    getCustomResource( final String param1 );
        public ObjectName       getCustomResourceByJndiName( final String key );
        public ObjectName       getCustomResourceByJndiName( final String param1, final String param2 );
        public javax.management.ObjectName[]    getExternalJndiResource();
        public javax.management.ObjectName[]    getExternalJndiResource( final String param1 );
        public ObjectName       getExternalJndiResourceByJndiName( final String key );
        public ObjectName       getExternalJndiResourceByJndiName( final String param1, final String param2 );
        public javax.management.ObjectName[]    getJdbcConnectionPool();
        public javax.management.ObjectName[]    getJdbcConnectionPool( final String param1 );
        public ObjectName       getJdbcConnectionPoolByName( final String key );
        public ObjectName       getJdbcConnectionPoolByName( final String param1, final String param2 );
        public javax.management.ObjectName[]    getJdbcResource();
        public javax.management.ObjectName[]    getJdbcResource( final String param1 );
        public ObjectName       getJdbcResourceByJndiName( final String key );
        public ObjectName       getJdbcResourceByJndiName( final String param1, final String param2 );
        public javax.management.ObjectName[]    getJmsConnectionFactory( final String param1 );
        public javax.management.ObjectName[]    getJmsConnectionFactoryRefs( final String param1 );
        public javax.management.ObjectName[]    getJmsDestinationResource( final String param1 );
        public javax.management.ObjectName[]    getJmsDestinationResourceRefs( final String param1 );
        public String   getJmsRaMappedName( final String param1 );
        public javax.management.ObjectName[]    getJmsResource( final String param1 );
        public javax.management.ObjectName[]    getJmsResource( final String param1, final String param2 );
        public Properties       getMCFConfigProps( final AttributeList param1 );
        public javax.management.ObjectName[]    getMailResource();
        public javax.management.ObjectName[]    getMailResource( final String param1 );
        public ObjectName       getMailResourceByJndiName( final String key );
        public ObjectName       getMailResourceByJndiName( final String param1, final String param2 );
        public String[] getMessageListenerTypes( final String param1 );
        public javax.management.ObjectName[]    getPersistenceManagerFactoryResource();
        public javax.management.ObjectName[]    getPersistenceManagerFactoryResource( final String param1 );
        public ObjectName       getPersistenceManagerFactoryResourceByJndiName( final String key );
        public ObjectName       getPersistenceManagerFactoryResourceByJndiName( final String param1, final String param2 );
        public javax.management.ObjectName[]    getResourceAdapterConfig();
        public javax.management.ObjectName[]    getResourceAdapterConfig( final String param1 );
        public String   getResourceAdapterConfig( final String param1, final Boolean param2, final String param3 );
        public ObjectName       getResourceAdapterConfigByResourceAdapterName( final String key );
        public ObjectName       getResourceAdapterConfigByResourceAdapterName( final String param1, final String param2 );
        public Properties       getResourceAdapterConfigProps( final AttributeList param1 );
        public String   getResourceType( final String param1 );
        public String   getReverseJmsRaMappedName( final String param1 );
        public String[] getSystemConnectorsAllowingPoolCreation();
        public javax.management.ObjectName[]    listPhysicalDestinations( final String param1 );
        public javax.management.ObjectName[]    listReferencees( final String param1 );
        public String[] listResourceReferencesAsString( final String param1 );
        public Boolean  pingConnectionPool( final String param1, final String param2 );
        public void     removeAdminObjectResourceByJndiName( final String key );
        public void     removeConnectorConnectionPoolByName( final String key );
        public void     removeConnectorResourceByJndiName( final String key );
        public void     removeCustomResourceByJndiName( final String key );
        public void     removeExternalJndiResourceByJndiName( final String key );
        public void     removeJdbcConnectionPoolByName( final String key );
        public void     removeJdbcResourceByJndiName( final String key );
        public void     removeMailResourceByJndiName( final String key );
        public void     removePersistenceManagerFactoryResourceByJndiName( final String key );
        public void     removeResourceAdapterConfigByResourceAdapterName( final String key );

}