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
 * InstanceRegistry.java
 *
 * Created on September 12, 2003, 3:46 PM
 */
package com.sun.enterprise.ee.admin.clientreg;

import javax.management.MBeanServerConnection;

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ServerHelper;

import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.admin.util.JMXConnectorConfig;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;
import com.sun.enterprise.admin.server.core.AdminService;

import java.io.IOException;

/** 
 * @author  kebbs
 *
 * Class NodeAgentRegistry maintains a list of MBeanServer connections to one 
 * or more Server instances 
 */ 
public class InstanceRegistry extends JMXConnectorRegistry implements IAdminConstants {         
    
    private static final boolean DEBUG = true;     
    
    private static final StringManager _strMgr = 
        StringManager.getManager(InstanceRegistry.class);
    
    // There is only one InstanceRegistry 
    private static InstanceRegistry _registry = null;
    
    private InstanceRegistry () {
        super();
        _registry = this;
    }
    
    /**
     * Initialize and return the one and only one static Server Instance Registry.
     */
    public synchronized static InstanceRegistry getInstanceRegistry() {
        if (_registry == null) {
            _registry = new InstanceRegistry();
        }
        return _registry;
    }               
    
    /**
     * Implementaiton of abstract method to return the system JmxConnector element
     * associated with the given server instance. 
     */
    protected MBeanServerConnectionInfo findConnectionInfo(String instanceName) 
        throws AgentException
    {        
        return findConnectionInfo(getConfigContext(), instanceName);
    }
    
    protected MBeanServerConnectionInfo findConnectionInfo(ConfigContext configContext, String instanceName) 
        throws AgentException
    {
        try {
            JMXConnectorConfig config = ServerHelper.getJMXConnectorInfo(configContext, 
                instanceName);            
            return new MBeanServerConnectionInfo(config);
        } catch (Exception ex) {
            throw new AgentException(ex);
        }
    }
    
    private ConfigContext getConfigContext()
    {
        return AdminService.getAdminService().getAdminContext().getAdminConfigContext();
    }
        
    /** 
     * Return an MBeanServerConnection to the specifiec node agent.
     */
    public static synchronized MBeanServerConnection getDASConnection(ConfigContext configContext)
        throws InstanceException
    {
        try {
            String dasName = ServerHelper.getDAS(configContext).getName();
            return getInstanceConnection(dasName);
        } catch (ConfigException ex) {
            throw new InstanceException(ex);
        }
    }  

    /**
     * Remove connection 
     */
    public static synchronized void removeInstanceConnection(String instanceName)
        throws InstanceException
    {
        try {
            getInstanceRegistry().removeConnectorFromCache(instanceName);
        } catch (IOException ex) {
            throw new InstanceException(ex);
        }
    }
    
    public static synchronized void disconnectInstanceConnection(String instanceName)
        throws InstanceException
    {
        try {
            getInstanceRegistry().disconnectCachedConnector(instanceName);
        } catch (IOException ex) {
            throw new InstanceException(ex);
        }
    }
    
    /**
     * Return an MBeanServerConnection to the specifiec node agent.
     */
    public static synchronized MBeanServerConnection getInstanceConnection(String instanceName)
        throws InstanceException
    {
        try {
            return getInstanceRegistry().getConnection(instanceName);       
        } catch (AgentException ex) {
            throw new InstanceException(ex);
        }
    }    
     
    public static synchronized MBeanServerConnectionInfo getDASConnectionInfo(ConfigContext configContext) 
        throws InstanceException 
    {
        try {
            String dasName = ServerHelper.getDAS(configContext).getName();
            return getInstanceConnectionInfo(configContext, dasName);
        } catch (ConfigException ex) {
            throw new InstanceException(ex);
        }
    }

    public static synchronized MBeanServerConnectionInfo getInstanceConnectionInfo(String instanceName) 
        throws InstanceException 
    {
        try {
            return getInstanceRegistry().findConnectionInfo(instanceName);
        } catch (AgentException ex) {
            throw new InstanceException(ex);
        }
    }

    public static synchronized MBeanServerConnectionInfo getInstanceConnectionInfo(ConfigContext configContext,
        String instanceName) throws InstanceException 
    {
        try {
            return getInstanceRegistry().findConnectionInfo(configContext, instanceName);
        } catch (AgentException ex) {
            throw new InstanceException(ex);
        }
    }

}
