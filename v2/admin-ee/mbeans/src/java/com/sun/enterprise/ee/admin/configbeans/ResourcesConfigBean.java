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

package com.sun.enterprise.ee.admin.configbeans;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.target.Target;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.ServerTags;

import com.sun.enterprise.config.serverbeans.ResourceHelper;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.ClusterHelper;

import com.sun.enterprise.ee.admin.ExceptionHandler;
import com.sun.enterprise.admin.configbeans.BaseConfigBean;
import com.sun.logging.ee.EELogDomains;

import com.sun.enterprise.admin.configbeans.ResourceReferenceHelper;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;


/**
 *
 * @author  kebbs
 */
public class ResourcesConfigBean extends BaseConfigBean implements IAdminConstants
{   
    
    private static final TargetType[] VALID_LIST_TYPES = new TargetType[] {
        TargetType.CLUSTER, TargetType.SERVER, TargetType.DAS};
        
    private static final StringManager _strMgr = 
        StringManager.getManager(ResourcesConfigBean.class);

    private static Logger _logger = null;
        
    private static Logger getLogger() 
    {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }    
    
    private static ExceptionHandler _handler = null;
    
    
    private ResourceReferenceHelper getResourceReferenceHelper()
    {
        return new ResourceReferenceHelper(getConfigContext());        
    }
    
    //The exception handler is used to parse and log exceptions
    protected static ExceptionHandler getExceptionHandler() 
    {
        if (_handler == null) {
            _handler = new ExceptionHandler(getLogger());
        }
        return _handler;
    }
  
    public ResourcesConfigBean(ConfigContext configContext) 
    {
        super(configContext);
    }  
 
    public void createResourceReference(String targetName, boolean enabled,
        String referenceName) throws ConfigException
    {       
        try {
            getResourceReferenceHelper().createResourceReference(
                targetName, enabled, referenceName); 

        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "eeadmin.createResourceReference.Exception",
                new String[] {referenceName, targetName});
        }            
    }
           
    
    public void deleteResourceReference(String targetName, String referenceName)
        throws ConfigException
    {
        try {
            getResourceReferenceHelper().deleteResourceReference(
                targetName, referenceName);        

        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "eeadmin.deleteResourceReference.Exception",
                new String[] {referenceName, targetName});            
        }                    
    }          
            
    public String[] listResourceReferencesAsString(String targetName)
         throws ConfigException
    {
        try {
            final ConfigContext configContext = getConfigContext();            
            final Target target = TargetBuilder.INSTANCE.createTarget(
                VALID_LIST_TYPES, targetName, configContext);   
            String refName = null;            
            ResourceRef[] refs = target.getResourceRefs(); 
            ArrayList result = new ArrayList();            
            for (int i = 0; i < refs.length; i++) {
                refName = refs[i].getRef();
                //Filter out the system resources.
                if (!ResourceHelper.isSystemResource(configContext, refName)) {
                    result.add(refName);
                }
            }
            return (String[])result.toArray(new String[result.size()]);             
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "eeadmin.listResourceReferences.Exception", targetName); 
        }
    }

    public String getResourceType(String id) throws ConfigException
    {
        final String type = ResourceHelper.getResourceType(
                getConfigContext(), id);
        if (null == type) {
            throw new ConfigException(_strMgr.getString("noSuchResource", id));
        }
        final String externalType = ResourceTypeMapper.extern(type);
        assert externalType != null : ("No external mapping found for " + type);
        return externalType;
    }

    private static final String[] mappedTypes = {
        Resources.CUSTOM_RESOURCE,          ServerTags.CUSTOM_RESOURCE,
        Resources.EXTERNAL_JNDI_RESOURCE,   ServerTags.EXTERNAL_JNDI_RESOURCE,
        Resources.JDBC_RESOURCE,            ServerTags.JDBC_RESOURCE,
        Resources.MAIL_RESOURCE,            ServerTags.MAIL_RESOURCE,
        Resources.ADMIN_OBJECT_RESOURCE,    ServerTags.ADMIN_OBJECT_RESOURCE,
        Resources.CONNECTOR_RESOURCE,       ServerTags.CONNECTOR_RESOURCE,
        Resources.RESOURCE_ADAPTER_CONFIG,  ServerTags.RESOURCE_ADAPTER_CONFIG,
        Resources.JDBC_CONNECTION_POOL,     ServerTags.JDBC_CONNECTION_POOL,
        Resources.CONNECTOR_CONNECTION_POOL,            ServerTags.CONNECTOR_CONNECTION_POOL,
        Resources.PERSISTENCE_MANAGER_FACTORY_RESOURCE, ServerTags.PERSISTENCE_MANAGER_FACTORY_RESOURCE,
    };

    private static final class ResourceTypeMapper {
        private static String extern(String internalType) {
            for (int i = 0; i < mappedTypes.length; ) {
                if (mappedTypes[i].equals(internalType)) {
                    return mappedTypes[i+1];
                }
                i += 2;
            }
            return null;
        }
    }
}
