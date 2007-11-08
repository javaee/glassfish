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

package com.sun.enterprise.ee.nodeagent;

import com.sun.enterprise.admin.util.JMXConnectorConfig;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.ElementProperty;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.NodeAgentHelper;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;
import com.sun.enterprise.ee.admin.mbeanapi.NodeAgentMBean;
import com.sun.enterprise.ee.admin.proxy.NodeAgentProxy;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.ee.EELogDomains;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanException;
import com.sun.enterprise.admin.util.IAdminConstants;

/**
 * NodeAgentNotification on DAS relocation implementation
 *
 * Provides methods for notifying already running Node agents
 * when DAS is moved from one location to other.    
 *
 * @author Nandini Ektare
 */

public class NodeAgentNotifier implements Runnable {

    // Logger and StringManager
    private static final Logger _logger =
	Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
    
    private static final StringManager _strMgr =
	StringManager.getManager(NodeAgentNotifier.class);
    
    private final ServerContext dasCtx;
    
    public NodeAgentNotifier(ServerContext sc) {
	_logger.fine(_strMgr.getString("notifier.thread_initialized"));
        dasCtx = sc;
    }

    public void run() {
	_logger.fine(_strMgr.getString("notifier.thread_running"));
	// vars
	ConfigContext configContext = dasCtx.getConfigContext();
	// determine the instance type for appropriate processing
        notifyAllNodeAgentsOfNewLocation(configContext);
    }

    private void notifyAllNodeAgentsOfNewLocation(ConfigContext cc) {
        try {
            AdminService as = ServerHelper.getAdminServiceForServer(
                                cc, "server");
            JmxConnector config = as.getJmxConnectorByName(
                                      as.getSystemJmxConnectorName());            
            ElementProperty clientHostname = config.getElementPropertyByName(
                                            IAdminConstants.HOST_PROPERTY_NAME);
            if (clientHostname != null) {
                String newHost = (String) clientHostname.getValue();
                NodeAgent[] nas = NodeAgentHelper.getNodeAgentsInDomain(cc);
                for (NodeAgent na : nas) 
                    dasHasMoved(newHost, config.getPort(), na);
            }            
        } catch (ConfigException ce) {
            _logger.log(Level.WARNING, _strMgr.getString(
                            "notifier.fetch_error", ce.getMessage()));
        }
    }

    private void dasHasMoved(
        String newHost, String newPort, NodeAgent na) {
        
        try {
            NodeAgentMBean agentMBean = 
                NodeAgentProxy.getNodeAgentProxy(na.getName());
            agentMBean.dasHasMoved(newHost, newPort);
        } catch (AgentException ex) {
            _logger.log(Level.WARNING, _strMgr.getString(
                "notifier.nodeagent_notify_error", na.getName()));
        } catch (MBeanException me) {
            _logger.log(Level.WARNING, _strMgr.getString(
                "notifier.nodeagent_contact_error", na.getName()));
        }
    }
}