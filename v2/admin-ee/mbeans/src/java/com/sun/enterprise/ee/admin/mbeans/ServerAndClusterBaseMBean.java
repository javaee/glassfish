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

package com.sun.enterprise.ee.admin.mbeans;

import com.sun.enterprise.admin.meta.MBeanRegistryFactory;

import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ResourceHelper;
import com.sun.enterprise.config.serverbeans.ApplicationHelper;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;

import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.admin.common.exception.MBeanConfigException;

import com.sun.logging.ee.EELogDomains;

import com.sun.enterprise.ee.admin.ExceptionHandler;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level; 

import javax.management.ObjectName;
import javax.management.Attribute;

import com.sun.enterprise.admin.util.jmx.AttributeListUtils;

public abstract class ServerAndClusterBaseMBean extends SystemPropertyBaseMBean 
    implements IAdminConstants
{    
    private static final StringManager _strMgr = 
        StringManager.getManager(ServerAndClusterBaseMBean.class);

    private static Logger _logger = null;
    
    public ServerAndClusterBaseMBean() {
        super();
    }       
    
    protected static Logger getLogger() 
    {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
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

    private EEApplicationsConfigMBean getEEApplicationsConfigMBean()
        throws InstanceException, MBeanConfigException
    {
        try {
            return (EEApplicationsConfigMBean)MBeanRegistryFactory.getAdminMBeanRegistry().instantiateMBean(
                "applications", new String[]{this.getDomainName()}, null, 
                getConfigContext());                
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(ex, 
                getLogMessageId(), "");
        }
    }
    
    public void createResourceReference(boolean enabled,
        String referenceName) throws InstanceException
    {       
        String name = getName();
        try {            
            getResourcesConfigBean().createResourceReference(name, enabled, 
                referenceName);
        } catch (Exception ex) {        
            throw getExceptionHandler().handleInstanceException(ex, 
                getLogMessageId(), name);
        }
    }
               
    public void deleteResourceReference(String referenceName)
        throws InstanceException
    {
        String name = getName();
        try {            
            getResourcesConfigBean().deleteResourceReference(name, 
                referenceName);
        } catch (Exception ex) {        
            throw getExceptionHandler().handleInstanceException(ex, 
                getLogMessageId(), name);
        }
    }          
            
    public String[] listResourceReferencesAsString()
         throws InstanceException
    {
        String name = getName();
        try {            
            return getResourcesConfigBean().listResourceReferencesAsString(name);
        } catch (Exception ex) {        
            throw getExceptionHandler().handleInstanceException(ex, 
                getLogMessageId(), name);
        }
    }
    
    public void createApplicationReference(boolean enabled,
        String virtualServers, String referenceName) throws InstanceException 
    {                 
        String name = getName();
        try {            
            getEEApplicationsConfigMBean().createApplicationReference(name, 
                enabled, virtualServers, referenceName);               
        } catch (Exception ex) {        
            throw getExceptionHandler().handleInstanceException(ex, 
                getLogMessageId(), name);
        }
    }           

    public void deleteApplicationReference(String referenceName)
        throws InstanceException
    {
        String name = getName();
        try {            
            getEEApplicationsConfigMBean().deleteApplicationReference(name, 
                referenceName);               
        } catch (Exception ex) {        
            throw getExceptionHandler().handleInstanceException(ex, 
                getLogMessageId(), name);
        }
    }               
        
    public String[] listApplicationReferencesAsString()
         throws InstanceException
    {
        String name = getName();
        try {            
            return getEEApplicationsConfigMBean().listApplicationReferencesAsString(
                name);
        } catch (Exception ex) {        
            throw getExceptionHandler().handleInstanceException(ex, 
                getLogMessageId(), name);
        }
    }
    
    public ObjectName listConfiguration() 
        throws InstanceException
    {
        String name = getName();
        try {            
            getConfigsConfigBean().listConfigurationsAsString(name);
            return getConfigurationObjectName(name);
        } catch (Exception ex) {        
            throw getExceptionHandler().handleInstanceException(ex, 
                getLogMessageId(), name);
        }
    }
        
	/**
	 * Returns resource-ref object names by the given type.
	 * @param type Valid types are custom-resource | external-jndi-resource | 
	 * jdbc-resource | mail-resource | persistence-manager-factory-resource |
	 * admin-object-resource | connector-resource |  resource-adapter-config |
     * jdbc-connection-pool | connector-connection-pool.
	 * @return Returns an ObjectName array. The array will be of 0 length if no
	 * resource-refs exist.
	 * @throws InstanceException
	 */
	public ObjectName[] listResourceRefsByType(String type)
		throws InstanceException
	{
        /*
            Convert hyphenated type to camel case. 
            eg:- jdbc-resource -> JdbcResource
         */
        type = AttributeListUtils.dash2CamelCase(type);
		final ArrayList refsByType = new ArrayList();
		try {
			final ObjectName[] refs = (ObjectName[])invoke(
				"getResourceRef", null, null);
			if (refs != null) {
				for (int i = 0; i < refs.length; i++) {
					final String ref = (String)getMBeanServer().getAttribute(
                            refs[i], "ref");
					assert ref != null;
					final String resType = ResourceHelper.getResourceType(
						getConfigContext(), ref);
					if (resType.equals(type)) {
						if (!ResourceHelper.isSystemResource(getConfigContext(), 
								ref)) {
							refsByType.add(refs[i]);
						}
					}
				}
			}
		} catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(ex, 
                getLogMessageId(), getName());
		}
		return (ObjectName[])refsByType.toArray(new ObjectName[0]);
	}

	/**
	 * Returns application-ref object names by the given type.
	 * @param type Valid types are lifecycle-module | j2ee-application | 
	 * ejb-module | web-module | connector-module | appclient-module.
	 * @return Returns an ObjectName array. The array will be of 0 length if no
	 * application-refs exist.
	 * @throws InstanceException
	 */
	public ObjectName[] listApplicationRefsByType(String type)
		throws InstanceException
	{
        /*
            Convert hyphenated type to camel case. 
            eg:- j2ee-application -> J2eeApplication
         */
        type = AttributeListUtils.dash2CamelCase(type);
		final ArrayList refsByType = new ArrayList();
		try {
			final ObjectName[] refs = (ObjectName[])invoke(
				"getApplicationRef", null, null);
			if (refs != null) {
				for (int i = 0; i < refs.length; i++) {
					final String ref = (String)getMBeanServer().getAttribute(
						refs[i], "ref");
					assert ref != null;
					final String appType = ApplicationHelper.getApplicationType(
						getConfigContext(), ref);
					if (appType.equals(type)) {
						if (!ApplicationHelper.isSystemApp(getConfigContext(), 
								ref)) {
							refsByType.add(refs[i]);
						}
					}
				}
			}
		} catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(ex, 
                getLogMessageId(), getName());
		}
		return (ObjectName[])refsByType.toArray(new ObjectName[0]);
	}


	/**
	 * Returns all non-system application-ref object names
	 * @return Returns an ObjectName array. The array will be of 0 length if no
	 * application-refs exist.
	 * @throws InstanceException
	 */
	public ObjectName[] listApplicationRefs()
		throws InstanceException
	{
		final ArrayList userRefs = new ArrayList();
		try {
			final ObjectName[] refs = (ObjectName[])invoke(
				"getApplicationRef", null, null);
			if (refs != null) {
				for (int i = 0; i < refs.length; i++) {
					final String ref = (String)getMBeanServer().getAttribute(
						refs[i], "ref");
					assert ref != null;
					final String appType = ApplicationHelper.getApplicationType(
						getConfigContext(), ref);
						if (!ApplicationHelper.isSystemApp(getConfigContext(), 
								ref)) {
							userRefs.add(refs[i]);
						}
				}
			}
		} catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(ex, 
                getLogMessageId(), getName());
		}
		return (ObjectName[])userRefs.toArray(new ObjectName[0]);
	}
}
