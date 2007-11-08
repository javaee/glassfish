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

import com.sun.enterprise.admin.mbeans.ResourcesMBean;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.ServerTarget;
import com.sun.enterprise.admin.target.ClusterTarget;

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

import com.sun.enterprise.ee.admin.configbeans.ResourcesConfigBean;

import com.sun.logging.ee.EELogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.JMException;

import com.sun.enterprise.admin.util.jmx.AttributeListUtils;

/**
 *
 * @author  kebbs
 */
public class ResourcesConfigMBean extends ResourcesMBean 
    implements com.sun.enterprise.ee.admin.mbeanapi.ResourcesConfigMBean, IAdminConstants
{   
    
    private static final TargetType[] VALID_LIST_TYPES = new TargetType[] {
        TargetType.CLUSTER, TargetType.SERVER, TargetType.DAS};
        
    private static final StringManager _strMgr = 
        StringManager.getManager(ResourcesConfigMBean.class);

    private static Logger _logger = null;
        
    private static Logger getLogger() 
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

    /** Creates a new instance of ConfigsConfigMBean */
    public ResourcesConfigMBean() 
    {
        super();
    }  
 
    public void createResourceReference(String targetName, boolean enabled,
        String referenceName) throws ConfigException
    {                       
        getResourcesConfigBean().createResourceReference(targetName, enabled, referenceName);    
    }
           
    
    public void deleteResourceReference(String targetName, String referenceName)
        throws ConfigException
    {   
        getResourcesConfigBean().deleteResourceReference(targetName, referenceName);               
    }          
            
    public String[] listResourceReferencesAsString(String targetName)
         throws ConfigException
    {
        return getResourcesConfigBean().listResourceReferencesAsString(
            targetName);
    }

    /**
     */
    public ObjectName[] listReferencees(String resourceRef) 
        throws ConfigException
    {
        ObjectName[]        referencees = new ObjectName[0];
        try {
            final ConfigContext ctx         = getConfigContext();            
            final ArrayList objectNames     = new ArrayList();

            final Server[] servers = 
                ServerHelper.getServersReferencingResource(ctx, resourceRef);
            if (servers != null) {
                for (int i = 0; i < servers.length; i++) {
                    final String name = servers[i].getName();
                    if (!ServerHelper.isServerClustered(ctx, name))
                    {
                        objectNames.add(getServerObjectName(name));
                    }
                }
            }
            final Cluster[] clusters = 
                ClusterHelper.getClustersReferencingResource(ctx, resourceRef);
            if (clusters != null) {
                for (int i = 0; i < clusters.length; i++) {
                    final String name = clusters[i].getName();
                    objectNames.add(getClusterObjectName(name));
                }
            }
            referencees = (ObjectName[])objectNames.toArray(new ObjectName[0]);
        } catch (Exception ex) {
            throw getExceptionHandler().handleConfigException(
                ex, "eeadmin.listResourceReferences.Exception", resourceRef); 
        }

        return referencees;
    }

    private ResourcesConfigBean getResourcesConfigBean()
    {
        return new ResourcesConfigBean(getConfigContext());
    }
        
    public String getResourceType(String id) throws ConfigException
    {
        return getResourcesConfigBean().getResourceType(id);
    }

    /**
     */
    public ObjectName[] getJmsConnectionFactoryRefs(String targetName)
        throws ConfigException, JMException, Exception
    {
        ObjectName[] jmsConnectionFactoryRefs = new ObjectName[0];
        final ObjectName targetON = getTargetObjectName(
            getListRefsTarget(targetName));
        assert targetON != null;
        final ObjectName[] refs = listResourceRefs(targetON, 
            ServerTags.CONNECTOR_RESOURCE);
        if (refs.length > 0) {
            final ObjectName[] oaCF = super.getJmsConnectionFactory(targetName);
            assert oaCF != null && oaCF.length > 0;
            jmsConnectionFactoryRefs = ResourceAndRefMatcher.instance.
                matchingRefs(oaCF, refs);
        }
        return jmsConnectionFactoryRefs;
    }

    /**
     */
    public ObjectName[] getJmsDestinationResourceRefs(String targetName)
        throws ConfigException, JMException, Exception
    {
        ObjectName[] jmsDestinationRefs = new ObjectName[0];
        final ObjectName targetON = getTargetObjectName(
            getListRefsTarget(targetName));
        assert targetON != null;
        final ObjectName[] refs = listResourceRefs(targetON, 
            ServerTags.ADMIN_OBJECT_RESOURCE);
        if (refs.length > 0) {
            final ObjectName[] oaCF = super.getJmsDestinationResource(targetName);
            assert oaCF != null && oaCF.length > 0;
            jmsDestinationRefs = ResourceAndRefMatcher.instance.
                matchingRefs(oaCF, refs);
        }
        return jmsDestinationRefs;
    }

    private static final TargetType[] VALID_LIST_REFS_TARGETS = new TargetType[] {
        TargetType.CLUSTER, TargetType.SERVER, TargetType.DAS};

    Target getListRefsTarget(String targetName) throws ConfigException
    {
        final Target target = TargetBuilder.INSTANCE.createTarget(
                VALID_LIST_REFS_TARGETS, targetName, getConfigContext());
        return target;
    }

    ObjectName getTargetObjectName(Target target) throws MBeanException
    {
        ObjectName targetON = null;
        if (target instanceof ServerTarget) {
            targetON = getServerObjectName(target.getName());
        } else if (target instanceof ClusterTarget) {
            targetON = getClusterObjectName(target.getName());
        }
        return targetON;
    }

    ObjectName[] listResourceRefs(ObjectName target, String type) 
        throws JMException
    {
        return (ObjectName[])getMBeanServer().invoke(target, 
            "listResourceRefsByType", new Object[]{type}, 
            new String[]{"java.lang.String"});
    }
}
