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
 * NodeAgentProxy.java
 *
 * Created on September 15, 2003, 1:37 PM
 */

package com.sun.enterprise.ee.admin.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.ConnectException;

import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.ee.admin.servermgmt.AgentException;
import com.sun.logging.ee.EELogDomains;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;

import com.sun.enterprise.ee.admin.clientreg.NodeAgentRegistry;
import com.sun.enterprise.ee.admin.proxy.BaseProxy;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.NodeAgents;
import com.sun.enterprise.config.serverbeans.NodeAgent;
import com.sun.enterprise.config.serverbeans.ElementProperty;    

import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;

import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContextImpl;

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.admin.util.IAdminConstants;

import com.sun.enterprise.admin.configbeans.BaseConfigBean;

import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.ee.admin.mbeanapi.NodeAgentMBean;

/**
 * @author  kebbs
 *
 * The NodeAgentProxy class is a dynamic proxy used to invoke methods on a Node Agent. In 
 * addition to invoking the remote MBean server in the Node Agent, the proxy 
 * performs two tasks:
 *
 * 1) Attempts to rendezvous with the Node Agent if this has not already happened. This 
 * "rendezvous on demand" functionality is necessary since it is not clear when the node 
 * agent will be running. The common case here is that the node agent is not installed or
 * is not bound to a DAS. Then the node agent is bound (bind-nodeagent) and instances
 * are created (create-instance). Then the node agent is installed still in an unbound
 * state not knowing the DAS properties. The operation targeted at the node agent (e.g. 
 * create-instance) will trigger an rendezvous automatically.
 *
 * 2) Because the Node Agent is down, some of its operations are not required to 
 * succeed. For example start/stop-instance must be able to reach the Node Agent 
 * since there is no persistent state in domain.xml associated with these operations; 
 * however, addition of new server instances persists state in domain.xml. If the 
 * node agent is unreachable during these operations, then this is not considered 
 * a failure since the node agent will pick up the changes the next time it is restarted
 * and resynchronizes.
 */
public class NodeAgentProxy extends BaseProxy implements InvocationHandler {
        
    /**
     * Class InvocationAttributes stores information about each method invoked in the 
     * dynamic proxy.
     */
    private class InvocationAttributes {
        boolean _mustSucceed;
        
        InvocationAttributes(boolean mustSucceed) {
            _mustSucceed = mustSucceed;
        }
 
        boolean mustSucceed() {
            return _mustSucceed;
        }
    }      
   
    
    private static final StringManager _strMgr = 
        StringManager.getManager(NodeAgentProxy.class);
    
    private static Logger _logger = null;             
    
    //The _invocationAttributes maps a methodName to an InvocationAttributes object.
    private static HashMap _invocationAttributes = null;
    
    //Default invocationAttributes
    private static InvocationAttributes _defaultAttributes = null;        
    
    //The default domain name for the Node Agent's mbean server.
    private static String _defaultDomain = null;
    
    //FIXME: Should'nt the object name for the node agent have its name embedded in it
    //rather than making its host and port be the only differentiating factory. If so 
    //this should not be static.
    private ObjectName _objectName = null;
    
    private MBeanServerConnection _connection = null;   
    
    private String _nodeAgentName = null;
    
    /**
     * Returns a dynamic proxy capable of implementing the NodeAgentMBean interface.
     */
    public static NodeAgentMBean getNodeAgentProxy(String nodeAgentName)         
    {
        return (NodeAgentMBean)Proxy.newProxyInstance(
            NodeAgentMBean.class.getClassLoader(), 
            new Class[] {NodeAgentMBean.class}, new NodeAgentProxy(nodeAgentName));
    }
    
    private String getNodeAgentName() {
        return _nodeAgentName;
    }
    
    private static Logger getLogger() 
    {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }
    
    
       
    /** 
     * Creates a new instance of NodeAgentProxy. We initialize our static content
     * only once.
     */
    private NodeAgentProxy(String nodeAgentName) {
        _nodeAgentName = nodeAgentName;    
        synchronized (NodeAgentProxy.class) {
            if (_invocationAttributes == null) {
                _invocationAttributes = new HashMap();
                _invocationAttributes.put("synchronizeWithDAS", 
                    new InvocationAttributes(false));
                //_invocationAttributes.put("rendezvousWithNodeAgent", 
                //    new InvocationAttributes(false));
            }
        }
    }
    
    private InvocationAttributes getInvocationAttributes(String methodName) 
    {
        InvocationAttributes attrs = (InvocationAttributes)_invocationAttributes.get(methodName);
        //If the attributes are not found, revert to the default.
        if (attrs == null) {
            if (_defaultAttributes == null) {
                _defaultAttributes = new InvocationAttributes(true);
            }
            attrs = _defaultAttributes;
        }
        return attrs;
    }
    
    /**
     * Returns the object name for the specified node agent. Really the object name 
     * seems to be the same across all node agents.
     */
    private ObjectName getObjectName() throws IOException, 
        MalformedObjectNameException, AgentException
    {
        if (_objectName == null) {
            if (_defaultDomain == null) {
                _defaultDomain = getConnection().getDefaultDomain();
            }
            _objectName = new ObjectName(_defaultDomain + ":type=NodeAgent,name=" +
                getNodeAgentName() + ",category=config");                         
        }
        return _objectName;
    }
        
    
    /**
     * Get a connection to the node agent's mbean server using the NodeAgentRegistry
     */
    private MBeanServerConnection getConnection() throws AgentException {
        if (_connection == null) {
            _connection = NodeAgentRegistry.getNodeAgentConnection(getNodeAgentName());
        }
        return _connection;
    }    
                   
    /**
     * hasRendezvousOccurred - this method report whether the nodeagent has rendezvoused with the DAS
     * @return -  true means the nodeagent has rendezvoused and the mbean invoke can proceed
     *            false means that the nodeagent has not redezvoused and the mbean invoke can not proceed
     */
    private boolean hasRendezvousOccurred() throws ConfigException, MBeanException
    {       
        final ConfigContext configContext = 
            ApplicationServer.getServerContext().getConfigContext();
        final Domain domain = ServerBeansFactory.getDomainBean(configContext);
        final NodeAgents controllers = domain.getNodeAgents();
        final NodeAgent controller = controllers.getNodeAgentByName(getNodeAgentName());
        
        boolean rendezvousOccurred = true;
        ElementProperty rendezvousProperty = null;
        
        //It is an expected case for the controller to be null. In the case where we 
        //are unbinding from a node agent, the node controller element will already be 
        //removed before invoking synchronizeWithDAS via this proxy.
        //Tested case where nodeagent has never rendezvoused and then it is deleted, works correctly
        //with connection failure exception being thrown
        if (controller != null) {                    
            //See if a rendezvous has occurred        
            rendezvousProperty = controller.getElementPropertyByName(
                IAdminConstants.RENDEZVOUS_PROPERTY_NAME);            
            rendezvousOccurred = new Boolean(rendezvousProperty.getValue()).booleanValue();                 
        }
        
        if (rendezvousOccurred) {
            if (controller == null) {
                getLogger().log(Level.FINEST, "NodeAgentProxy: Defaulted to NodeAgent has rendezvoused since the agent has been removed ???");
            } else {
                getLogger().log(Level.FINEST, "NodeAgentProxy: The Nodeagent has rendezvoused with the DAS");
            }
        } else {
                getLogger().log(Level.FINEST, "NodeAgentProxy: The NodeAgent has not rendezvoused with the DAS");
        }
        
        return rendezvousOccurred;
        
    }    
    
    private Exception mangleInvokeException(Exception ex) {
        Exception result = null;
        //MBeanException and AgentException can pass through directly. All other exceptions must
        //be wrapped as an agent exception. This is to avoid a UndeclaredException from being thrown
        //from the dynamic proxies invoke method. In other words MBeanException and AgentException 
        //are the only exceptions being thrown from the NodeAgentMBean interface.
        
        //The MBeanException is a little tricky, we need to propagate its cause rather than 
        //the exception itself. This is necessary to preserve the original message of the exception
        //so that the CLI can display it.
        if (ex instanceof MBeanException) {
            Throwable t = ex.getCause();
            if (t != null) {
                if (t instanceof Exception) {
                    ex = (Exception)t;
                }
            }
            result = ex;
        } 
        
        if (ex instanceof AgentException) {
            result = ex;
        } else {
            result = new AgentException(ex);
        } 
        //If the server is unreachable then we want to mark the connection as disabled
        if (isUnreachable(ex)) {
            try {
                NodeAgentRegistry.disconnectNodeAgentConnection(getNodeAgentName());
            } catch (Exception ex2) {
                getLogger().log(Level.WARNING, "nodeAgent.proxy.exception", ex2); 
            }
        }
        return result;        
    }
      
    
    /**
     * Invoke a method on the dynamic proxy.  This methos will throw a ConnectException
     * if the NodeAgent has yet to rendezvous with DAS.  This exception will thrown only if
     * the command is flagged a "mustSuceed", if not, it will be logged at the "FINE" log level.
     */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {        
        final String methodName = method.getName();
        getLogger().log(Level.FINEST, "NodeAgentProxy:invoke " + methodName);
        InvocationAttributes attrs = getInvocationAttributes(methodName);     
        Exception exception = null;
        Object result = null;        
        try {                    
            // check to see if nodeagent has rendezvoused with DAS
            // if not, can't continue with any of the nodeagent commands
            if(!hasRendezvousOccurred()) {
                // throw a ConnectException so the isReachable method
                // works correctly
                throw new ConnectException(_strMgr.getString("nodeAgent.not.rendezvoused"));
            }
            
            getLogger().log(Level.FINEST, "Sending command ....");
            result = getConnection().invoke(getObjectName(), methodName, 
                args, getParameterTypes(method.getParameterTypes()));
               
            return result;        
        } catch (Exception ex) {
            getLogger().log(Level.FINEST, "NodeAgentProxy:invoke: Connection processing exception: " + ex.toString());
            
            exception = mangleInvokeException(ex);
            if (attrs.mustSucceed()) {                
                throw exception;
            } else {
                //Do not log exceptions indicating that the node agent was unreachable (i.e. down)
                if (!isUnreachable(exception)) {
                    getLogger().log(Level.FINE, "nodeAgent.proxy.exception", exception);         
                } else {
                    getLogger().log(Level.FINE, "nodeAgent.proxy.cannotConnect", getNodeAgentName());
                }
                return result;
            }
        }
    }
}
