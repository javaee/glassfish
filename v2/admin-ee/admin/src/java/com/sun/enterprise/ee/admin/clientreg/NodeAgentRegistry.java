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
 * NodeAgentRegistry.java
 *
 * Created on September 12, 2003, 3:46 PM
 */
package com.sun.enterprise.ee.admin.clientreg;

import javax.management.MBeanServerConnection;

import com.sun.enterprise.config.serverbeans.JmxConnector;   
import com.sun.enterprise.config.serverbeans.ElementProperty;   
import com.sun.enterprise.config.serverbeans.NodeAgentHelper;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.admin.util.JMXConnectorConfig;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;

import com.sun.enterprise.util.i18n.StringManager;
import java.io.IOException;
/** 
 * @author  kebbs
 *
 * Class NodeAgentRegistry maintains a list of MBeanServer connections to one 
 * or more Node Agents. 
 */ 
public class NodeAgentRegistry extends JMXConnectorRegistry implements IAdminConstants {            
    private static final StringManager _strMgr = 
        StringManager.getManager(NodeAgentRegistry.class);
    
    // There is only one NodeAgentRegistry 
    private static NodeAgentRegistry _registry = null;
    
    private NodeAgentRegistry () {
        super();
        _registry = this;
    }
    
    /**
     * Initialize and return the one and only one static Node Agent Registry.
     */
    public synchronized static NodeAgentRegistry getNodeAgentRegistry() {
        if (_registry == null) {
            _registry = new NodeAgentRegistry();
        }
        return _registry;
    }
               
     protected MBeanServerConnectionInfo findConnectionInfo(
        String nodeAgentName) throws AgentException
     {
         return findConnectionInfo(getConfigContext(), nodeAgentName);
     }
     
    /**
     * Implementaiton of abstract method to return the system JmxConnector element
     * associated with the given Node Agent. 
     */
    protected MBeanServerConnectionInfo findConnectionInfo(ConfigContext configContext, 
        String nodeAgentName) throws AgentException
    {
        try {            
            JMXConnectorConfig config = NodeAgentHelper.getJMXConnectorInfo(configContext, 
                nodeAgentName);            
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
    public static synchronized MBeanServerConnection getNodeAgentConnection(String nodeAgentName)
        throws AgentException
    {
        return getNodeAgentRegistry().getConnection(nodeAgentName);       
    }    
    
     /**
     * remove an MBeanServerConnection to the specifiec node agent from the cache
     */
    public static synchronized void removeNodeAgentConnection(String nodeAgentName)
    {
        try {
            getNodeAgentRegistry().removeConnectorFromCache(nodeAgentName);
        } catch (IOException e) {
            // don't care about the exception
        }
    }    

    public static synchronized void disconnectNodeAgentConnection(String nodeAgentName)
        throws AgentException
    {
        try {
            getNodeAgentRegistry().disconnectCachedConnector(nodeAgentName);
        } catch (IOException ex) {
            throw new AgentException(ex);
        }
    }
     
    public static synchronized MBeanServerConnectionInfo getNodeAgentConnectionInfo(ConfigContext configContext,
        String nodeAgentName) throws AgentException 
    {        
        return getNodeAgentRegistry().findConnectionInfo(configContext, nodeAgentName);        
    }
}
