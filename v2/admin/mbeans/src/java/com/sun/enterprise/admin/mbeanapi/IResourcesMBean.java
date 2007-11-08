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
 * $Id: IResourcesMBean.java,v 1.4 2005/12/25 03:42:11 tcfujii Exp $
 * author hamid@sun.com
 */

package com.sun.enterprise.admin.mbeanapi;

import java.util.ArrayList;
import java.util.Properties;
import javax.management.AttributeList;
import javax.management.ObjectName;
import javax.management.MBeanException;

import com.sun.enterprise.admin.common.exception.JMSAdminException;
import com.sun.enterprise.admin.common.JMSStatus;
import com.sun.enterprise.admin.common.JMSDestinationInfo;

public interface IResourcesMBean {
    
    public ObjectName createCustomResource( AttributeList   attrList,
    Properties      props,
    String          tgtName )
    throws Exception;
    
    public ObjectName createExternalJndiResource( AttributeList   attrList,
    Properties      props,
    String          tgtName)
    throws Exception;
    
    public ObjectName createPersistenceManagerFactoryResource( AttributeList   attrList,
    Properties      props,
    String          tgtName)
    throws Exception;
    
    public ObjectName createPMFResourceWithJDBCResource( AttributeList   attrList,
    Properties      props,
    String          tgtName)
    throws Exception;
    
    public ObjectName createJmsConnectionFactory( AttributeList   attrList,
    Properties      props,
    String          tgtName)
    throws Exception;
    
    public ObjectName createJmsDestinationResource( AttributeList   attrList,
    Properties      props,
    String          tgtName)
    throws Exception;
    
    public ObjectName createJmsResource( AttributeList   attrList,
    Properties      props,
    String          tgtName)
    throws Exception;
    
    public ObjectName createJdbcConnectionPool( AttributeList   attrList,
    Properties      props,
    String          tgtName)
    throws Exception;
    
    public ObjectName createConnectorConnectionPool( AttributeList   attrList,
    Properties      props,
    String          tgtName)
    throws Exception;
    
    public ObjectName createConnectorResource( AttributeList   attrList,
    Properties      props,
    String          tgtName)
    throws Exception;
    
    public ObjectName createAdminObjectResource( AttributeList   attrList,
    Properties      props,
    String          tgtName)
    throws Exception;
    
    public ObjectName createJdbcResource( AttributeList   attrList,
    Properties      props,
    String          tgtName)
    throws Exception;
    
    public ObjectName createMailResource( AttributeList   attrList,
    Properties      props,
    String          tgtName)
    throws Exception;
    
    public ObjectName createResourceAdapterConfig( AttributeList   attrList,
    Properties      props,
    String          tgtName)
    throws Exception;
    
    /**
     * GETTER Methods for resources
     */
    public ObjectName getCustomResourceByJndiName( String key, String tgtName )
    throws Exception;
    
    public ObjectName getJdbcResourceByJndiName( String key, String tgtName )
    throws Exception;
    
    public ObjectName getJdbcConnectionPoolByName( String key, String tgtName )
    throws Exception;
    
    public ObjectName getExternalJndiResourceByJndiName( String key, String tgtName )
    throws Exception;
    
    public ObjectName getMailResourceByJndiName( String key, String tgtName )
    throws Exception;
    
    public ObjectName getConnectorResourceByJndiName( String key, String tgtName )
    throws Exception;
    
    public ObjectName getResourceAdapterConfigByResourceAdapterName( String key, String tgtName )
    throws Exception;
    
    public ObjectName getAdminObjectResourceByJndiName( String key, String tgtName )
    throws Exception;
    
    public ObjectName getPersistenceManagerFactoryResourceByJndiName( String key, String tgtName )
    throws Exception;
    
    public ObjectName getConnectorConnectionPoolByName( String key, String tgtName )
    throws Exception;
    
    public ObjectName[] getCustomResource( String tgtName )
    throws Exception;
    
    public ObjectName[] getPersistenceManagerFactoryResource( String tgtName )
    throws Exception;
    
    public ObjectName[] getJmsResource( String tgtName )
    throws Exception;
    
    public ObjectName[] getJmsConnectionFactory( String tgtName )
    throws Exception;
    
    public ObjectName[] getJmsDestinationResource( String tgtName )
    throws Exception;
    
    public ObjectName[] getJmsResource( String resType, String tgtName )
    throws Exception;     
    
    public ObjectName[] getJdbcConnectionPool( String tgtName )
    throws Exception;
    
    public ObjectName[] getConnectorResource( String tgtName )
    throws Exception;
    
    public ObjectName[] getAdminObjectResource( String tgtName )
    throws Exception;
    
    public ObjectName[] getConnectorConnectionPool( String tgtName )
    throws Exception;
    
    public ObjectName[] getJdbcResource( String tgtName )
    throws Exception;
    
    public ObjectName[] getResourceAdapterConfig( String tgtName )
    throws Exception;
    
    public String getResourceAdapterConfig( String resAdapterConfig, Boolean verbose, String tgtName )
    throws Exception;
    
    
    public ObjectName[] getMailResource( String tgtName )
    throws Exception;
    
    public ObjectName[] getExternalJndiResource( String tgtName )
    throws Exception;
    
    public void deleteCustomResource( String name, String tgtName )
    throws Exception;
    
    public void deleteJdbcConnectionPool( String name, String tgtName )
    throws Exception;
    
    public void deleteJdbcConnectionPool( String name, Boolean cascade, String tgtName )
    throws Exception;
    
    public void deletePersistenceManagerFactoryResource( String name, String tgtName )
    throws Exception;
    
    public void deleteJmsConenctionFactory( String name, String tgtName )
    throws Exception;
    
    public void deleteJmsDestinationResource( String name, String tgtName )
    throws Exception;
    
    public void deleteJmsResource( String name, String tgtName )
    throws Exception;
    
    public void deleteConnectorResource( String name, String tgtName )
    throws Exception;
    
    public void deleteAdminObjectResource( String name, String tgtName )
    throws Exception;
    
    public void deleteConnectorConnectionPool( String name, String tgtName )
    throws Exception;
    
    public void deleteConnectorConnectionPool( String name, Boolean cascade, String tgtName )
    throws Exception;
    
    public void deleteJdbcResource( String name, String tgtName )
    throws Exception;
    
    public void deleteResourceAdapterConfig( String name, String tgtName )
    throws Exception;
    
    public void deleteMailResource( String name, String tgtName )
    throws Exception;
    
    public void deleteExternalJndiResource( String name, String tgtName )
    throws Exception;
    
    public ArrayList createResource(String resourceXMLFile, String tgtName)
    throws Exception;
    
    public String[] getConnectionDefinitionNames(String rarName)
    throws Exception;
    
    public Properties getResourceAdapterConfigProps(AttributeList attrList)
    throws Exception;
    
    public Properties getMCFConfigProps(AttributeList attrList)
    throws Exception;
    
    public Properties getAdminObjectConfigProps(AttributeList attrList)
    throws Exception;
    
    public String[] getAdminObjectInterfaceNames(String rarName)
    throws Exception;
    
    public Boolean pingConnectionPool(String poolName, String tgtName) throws Exception;
    
    public JMSStatus JMSPing() throws JMSAdminException;
    
    public JMSStatus JMSPing(String username, String password, int port)
    throws JMSAdminException;
    
    public void createJMSDestination(String destName, String destType, Properties destProps, String tgtName)
    throws JMSAdminException;
    
    
    public void deleteJMSDestination(String destName, String destType, String tgtName)
    throws JMSAdminException;

	public void flushJMSDestination(String destName, String destType, String tgtName)
	throws JMSAdminException;

    public JMSDestinationInfo[] listJMSDestinations(String destType, String tgtName)
    throws JMSAdminException;
    
    public void createPhysicalDestination(AttributeList attrList ,Properties props,
    String target)throws JMSAdminException,MBeanException;
    
    public void deletePhysicalDestination(String destName, String destType,
    String target)throws JMSAdminException,MBeanException;
    
    public ObjectName[] listPhysicalDestinations(String target)
    throws JMSAdminException,MBeanException;
    
    public String[] getMessageListenerTypes(String raName)
    throws Exception;
    
    public Properties getActivationConfProps(String raName,
    String msgLsnrType)
    throws Exception;
    
    
    public Properties getActivationConfPropTypes(String raName,
    String msgLsnrType)
    throws Exception;
}

