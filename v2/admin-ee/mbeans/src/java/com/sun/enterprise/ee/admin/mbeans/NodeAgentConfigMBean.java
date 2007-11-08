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

import com.sun.enterprise.admin.servermgmt.RuntimeStatus;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerTags;

import com.sun.enterprise.ee.admin.servermgmt.AgentException;

import com.sun.enterprise.ee.admin.ExceptionHandler;

import com.sun.enterprise.util.i18n.StringManager;

import com.sun.logging.ee.EELogDomains;
import java.util.logging.Logger;
import java.util.logging.Level; 
import java.util.ArrayList;

import javax.management.AttributeList;
import javax.management.Attribute;

import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.AttributeNotFoundException;

import com.sun.enterprise.ee.admin.proxy.NodeAgentProxy;
import com.sun.enterprise.ee.admin.mbeanapi.NodeAgentMBean;

public class NodeAgentConfigMBean extends EEBaseConfigMBean
    implements com.sun.enterprise.ee.admin.mbeanapi.NodeAgentConfigMBean 
{         
    private static final StringManager _strMgr = 
        StringManager.getManager(NodeAgentConfigMBean.class);

    private static Logger _logger = null;                   
    
    //The logger is used to log to server log file
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
    
    public NodeAgentConfigMBean() {
        super();
    }            
    
    private String getNodeAgentName() throws AgentException
    {             
        try {
            return (String)getAttribute(ServerTags.NAME);            
        } catch (Exception ex) {
            throw getExceptionHandler().handleAgentException(ex, 
                "nodeagent.NodeAgentConfigMBean.Exception", "unknown");
        }                    
    }
        
    
    public RuntimeStatus getRuntimeStatus() throws AgentException
    {          
        return getNodeAgentsConfigBean().getRuntimeStatus(getNodeAgentName());        
    }
    
    public void clearRuntimeStatus() throws AgentException
    {
        getNodeAgentsConfigBean().clearRuntimeStatus(getNodeAgentName());        
    }
    
    public void delete() throws AgentException
    {        
        getNodeAgentsConfigBean().deleteNodeAgentConfig(getNodeAgentName());          
    }
       
    //Note that we would like to delegate all of this up into the ServersConfigMBean,
    //but it has not yet been built.
    public String[] listServerInstancesAsString(boolean andStatus) throws AgentException
    {    
        String agentName = getNodeAgentName();
        try {            
            return getServersConfigBean().listServerInstancesAsString(agentName,
                andStatus);
        } catch (Exception ex) {
            throw getExceptionHandler().handleAgentException(
                ex, "nodeagent.listInstances.Exception", agentName);
        }        
    }
    
    public ObjectName[] listServerInstances() throws AgentException, MBeanException
    {
        return toServerONArray(listServerInstancesAsString(false));    
    }

    /**
     * If any of attribute(s) is(are) changed, domain.xml is flushed to disk.
     * Node Agent is informed. Call synchronizeWithDAS on Node Agent MBean. 
     *
     * @param list              List of attributes to be set.
     *
     * @return AttributeList    List of attributes modified.
     */
    public AttributeList setAttributes(AttributeList list) {
       list = super.setAttributes(list);
       informNodeAgent();
       return list;
    }
           
    /**
     * If an attribute is changed, domain.xml is flushed to disk.
     * Node Agent is informed. Call synchronizeWithDAS on Node Agent MBean. 
     *
     * @param attr              Attribute that is changed.
     *
     * @exception AttributeNotFoundException if this attribute is not
     *  supported by this MBean
     * @exception MBeanException if the initializer of an object
     *  throws an exception
     * @exception ReflectionException if a Java reflection exception
     *  occurs when invoking the getter
     */
    public void setAttribute(Attribute attr)
        throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        super.setAttribute(attr);
       informNodeAgent();
    }

    private void informNodeAgent() {

        String agentName = null;
        try {
            agentName = getNodeAgentName();
           // do the context flush
           flushAll();
           // call node agent mbean to synchronize
           NodeAgentMBean agentMBean = NodeAgentProxy.getNodeAgentProxy(
                            agentName);
           agentMBean.synchronizeWithDAS();
        } catch(ConfigException ce) {
            getLogger().log(Level.WARNING,"eeadmin.nodeagentconfig.flush_failed");
        } catch(AgentException ae) {
            getLogger().log(Level.WARNING,"eeadmin.nodeagentconfig.agent_exp",
                        agentName);
        } catch(MBeanException mbe) {
            getLogger().log(Level.WARNING,"eeadmin.nodeagentconfig.mbean_exp"                , mbe);
        }

    }
}
    
