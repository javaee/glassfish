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

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.admin.target.TargetType;
import com.sun.enterprise.admin.target.Target;
import com.sun.enterprise.admin.target.TargetBuilder;
import com.sun.enterprise.admin.util.IAdminConstants;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;

import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;
import com.sun.enterprise.ee.admin.ExceptionHandler;

import java.util.logging.Logger;
import java.util.logging.Level;        

public class StatusConfigMBean extends EEBaseConfigMBean 
    implements IAdminConstants, com.sun.enterprise.ee.admin.mbeanapi.StatusConfigMBean
{            
    private static final TargetType[] VALID_TYPES = new TargetType[] {
        TargetType.DOMAIN, TargetType.DAS, TargetType.CLUSTER, 
        TargetType.SERVER, TargetType.NODE_AGENT};

    private static final StringManager _strMgr = 
        StringManager.getManager(StatusConfigMBean.class);

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

    public StatusConfigMBean() {
        super();
    }
   
    public void clearStatus(String targetName) throws InstanceException
    {        
        try {                   
            ConfigContext configContext = getConfigContext();
            Target target = TargetBuilder.INSTANCE.createTarget(
                VALID_TYPES, targetName, configContext);                              
            if (target.getType() == TargetType.NODE_AGENT) {                
                //Clear status of the specified node agent
				getNodeAgentsConfigBean().clearRuntimeStatus(
                    target.getNodeAgents()[0].getName());
            } else if (target.getType() == TargetType.DOMAIN) {
                //Clear status of all node agents and server instances
                final Server[] servers = target.getServers();
                final int numServers = servers.length;
                final NodeAgent[] agents = target.getNodeAgents();
                final int numAgents = agents.length;                
                for (int i = 0; i < numServers; i++) {                
                    getServersConfigBean().clearRuntimeStatus(
                        servers[i].getName());
                }
                for (int i = 0; i < numAgents; i++) {
                    getNodeAgentsConfigBean().clearRuntimeStatus(
                        agents[i].getName());
                }
            } else {                        
                //Clear status of server instance or server instances in a cluster
                final Server[] servers = target.getServers();          
                final int numServers = servers.length;                    
                for (int i = 0; i < numServers; i++) {                
                    getServersConfigBean().clearRuntimeStatus(
                        servers[i].getName());
                }
            }
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.clearRuntimeStatus.Exception", targetName);
        }         
    }
        
    public RuntimeStatusList getStatus(String targetName) throws InstanceException
    {
        try {                   
            ConfigContext configContext = getConfigContext();
            Target target = TargetBuilder.INSTANCE.createTarget(
                DOMAIN_TARGET, VALID_TYPES, targetName, configContext);      
            RuntimeStatusList result = new RuntimeStatusList();  
            if (target.getType() == TargetType.NODE_AGENT) {
                //Get status for the specified node agent                
				result.add(getNodeAgentsConfigBean().getRuntimeStatus(
                    target.getNodeAgents()[0].getName()));
            } else if (target.getType() == TargetType.DOMAIN) {
                //Get status for all node agents and server instances in the domain
                final Server[] servers = target.getServers();
                final int numServers = servers.length;
                final NodeAgent[] agents = target.getNodeAgents();
                final int numAgents = agents.length;                            
                for (int i = 0; i < numServers; i++) {                
                    result.add(getServersConfigBean().getRuntimeStatus(
                        servers[i].getName()));
                }
                for (int i = 0; i < numAgents; i++) {
                    result.add(getNodeAgentsConfigBean().getRuntimeStatus(
                        agents[i].getName()));
                }
            } else {                        
                //Get status for the instance or instances in the specified cluster
                final Server[] servers = target.getServers();          
                final int numServers = servers.length; 
                //Check for a zero length cluster as a special case we would like
                //to report a non-running status.
                if (numServers == 0 && target.getType() == TargetType.CLUSTER) {                    
                    result.add(getClustersConfigBean().getRuntimeStatus(
                        target.getClusters()[0].getName()));
                } else {                                   
                    for (int i = 0; i < numServers; i++) {                
                        result.add(getServersConfigBean().getRuntimeStatus(
                            servers[i].getName()));
                    }
                }
            }
            return result;
        } catch (Exception ex) {
            throw getExceptionHandler().handleInstanceException(
                ex, "eeadmin.getRuntimeStatus.Exception", targetName);
        }
    }       
}
