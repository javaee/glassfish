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

import java.lang.IllegalArgumentException;

//JMX imports
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.AttributeNotFoundException;
import javax.management.modelmbean.ModelMBeanInfo;

import com.sun.enterprise.admin.meta.MBeanRegistryFactory;

//event handling
import com.sun.enterprise.admin.event.EventContext;
import com.sun.enterprise.admin.event.DynamicReconfigEvent;

//config imports
import com.sun.enterprise.admin.MBeanHelper;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.admin.AdminContext;
import com.sun.enterprise.ee.admin.ExceptionHandler;

// Logging
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.ee.EELogDomains;

import com.sun.enterprise.ee.admin.proxy.NodeAgentProxy;
import com.sun.enterprise.ee.admin.mbeanapi.NodeAgentMBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;

/**
 *  Represents the common functionality for node-agent/log-service and 
 *   node-agent/log-service/module-log-levels element in the domain.xml
 *
 * @author Satish Viswanatham
 */
public class BaseNodeAgentLogMBean extends EEBaseConfigMBean
{
    
    private static Logger _logger = null;            
    
    private static ExceptionHandler _handler = null;

    //The logger is used to log to server log file
    protected static Logger getLogger() 
    {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }

    /**
     * If any of attribute(s) is(are) changed, domain.xml is flushed to disk.
     * Node Agent is informed. Call synchronizeWithDAS on Node Agent MBean. 
     * 
     * @param attrList  A list of attributes: The identification of 
     *                  the attributes to be set and the values they are to 
     *                  be set to.
     * @return The list of attributes that were set, with their new values.
     */
     public AttributeList setAttributes(AttributeList list) {
       list = super.setAttributes(list);
       informNodeAgent();
       return list;
    }


    /**
     * Set the value of a specific attribute of this MBean.
     * domain.xml is flushed to disk.
     * Node Agent is informed. Call synchronizeWithDAS on Node Agent MBean.
     *
     * @param attr The identification of the attribute to be set
     *  and the new value
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

    //The exception handler is used to parse and log exceptions
    protected static ExceptionHandler getExceptionHandler() 
    {
        if (_handler == null) {
            _handler = new ExceptionHandler(getLogger());
        }
        return _handler;
    }

    private String getNodeAgentName() throws AgentException
    {     
        try {
            String[] locs = MBeanHelper.getLocation(
                        (ModelMBeanInfo)getMBeanInfo());

            return locs[1];
        } catch (Exception ex) {
            throw getExceptionHandler().handleAgentException(ex, 
                "nodeagent.NodeAgentConfigMBean.Exception", "unknown");
        }            
    }

}
