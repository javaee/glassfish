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

import javax.management.ObjectName;
import javax.management.MBeanException;


import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.admin.util.IAdminConstants;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.i18n.StringManagerBase;
import com.sun.enterprise.util.SystemPropertyConstants;

import com.sun.enterprise.ee.admin.servermgmt.AgentException;
import com.sun.enterprise.admin.servermgmt.RuntimeStatus;
import com.sun.enterprise.admin.servermgmt.RuntimeStatusList;

import com.sun.logging.ee.EELogDomains;

import com.sun.enterprise.ee.admin.ExceptionHandler;

import java.util.logging.Logger;
import java.util.logging.Level;             

import javax.management.ObjectName;
import javax.management.MBeanException;

/**
 * <p>MBean class that facilitates the configuration of the NodeAgent for CLI and remote clients
 *
 */

//ISSUE: Do we really want to throws an AgentException here as this will clients 
//using this mbean to have our runtime; however we seem to be throwing our own
//exceptions everywhere else in the mbeans. The problem with MBeanException 
//currently is that it masks the real exception (due to the fact that MBeanHelper
//does some bogus formatting on the exception.

public class NodeAgentsConfigMBean extends EEBaseConfigMBean 
    implements IAdminConstants, com.sun.enterprise.ee.admin.mbeanapi.NodeAgentsConfigMBean 
{     
    
    private static final StringManager _strMgr = 
        StringManager.getManager(NodeAgentsConfigMBean.class);
    
    private static Logger _logger = null;
    
    public NodeAgentsConfigMBean() {
        super();
    }
      
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
    
    public void clearRuntimeStatus(String agentName) throws AgentException
    {         
        getNodeAgentsConfigBean().clearRuntimeStatus(agentName);
    }
    
    public RuntimeStatus getRuntimeStatus(String agentName) throws AgentException
    {        
        return getNodeAgentsConfigBean().getRuntimeStatus(agentName);
    }
         
    public RuntimeStatusList getNodeAgentRuntimeStatus(String target)
        throws AgentException
    {
        return getNodeAgentsConfigBean().getNodeAgentRuntimeStatus(target);
    }
    
    public ObjectName[] listNodeAgents(String targetName) 
        throws AgentException, MBeanException
    {  
        return toNodeAgentONArray(listNodeAgentsAsString(targetName, false));        
    }   
    
    /**
     * lists node agents and their status
     */
    public String[] listNodeAgentsAsString(String targetName, boolean andStatus) throws AgentException
    {
        return getNodeAgentsConfigBean().listNodeAgentsAsString(targetName, andStatus);
    }                 
    
    /**
     * Removes the specified node agent. This operation is triggered by the asadmin 
     * delete-nodeagent-config command.
     */
    public void deleteNodeAgentConfig(String nodeAgentName) throws AgentException
    {
        getNodeAgentsConfigBean().deleteNodeAgentConfig(nodeAgentName);
    }
      
    
    /**
     * Adds the specified Node Agent to domain.xml. This operation is invoked by the asadmin 
     * create-nodeagent-config command that updates domain.xml. 
     */
    public ObjectName createNodeAgentConfig(String nodeAgentName) 
        throws AgentException, MBeanException
    {
        getNodeAgentsConfigBean().createNodeAgentConfig(nodeAgentName);
        return getNodeAgentObjectName(nodeAgentName);               
    }
    
    /**
     * Adds the specified Node Agent to the domain. This operation is invoked from 
     * the Node Agent when it is initiating the rendezvous. 
     */
    public String rendezvousWithDAS(String host, String port, 
        String nodeAgentName, String protocol, String clientHostName) throws AgentException
    {
        return getNodeAgentsConfigBean().rendezvousWithDAS(host, port, nodeAgentName,
            protocol, clientHostName);
    }        
}
