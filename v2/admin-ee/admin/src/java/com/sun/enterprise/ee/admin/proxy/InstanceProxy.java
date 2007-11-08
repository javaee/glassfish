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
 * InstanceProxy.java
 *
 * Created on October 3, 2003, 2:58 PM
 */

package com.sun.enterprise.ee.admin.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.ee.admin.clientreg.InstanceRegistry;
import com.sun.enterprise.ee.admin.proxy.BaseProxy;
import com.sun.enterprise.admin.servermgmt.InstanceException;
import com.sun.enterprise.ee.admin.mbeanapi.ServerRuntimeMBean;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanException;

/**
 *
 * @author  kebbs
 */
public class InstanceProxy extends BaseProxy implements InvocationHandler {
        
    private static Logger _logger = null;             
    
    
    //The default domain name for the Node Agent's mbean server.
    private static String _defaultDomain = null;
    
    private ObjectName _objectName = null;
    
    private MBeanServerConnection _connection = null;   
    
    private String _instanceName;
    
    /** Creates a new instance of InstanceProxy */
    private InstanceProxy(String instanceName) {
        _instanceName = instanceName;
    }
 
    private String getInstanceName() {
        return _instanceName;
    }
    
    private static Logger getLogger() 
    {
        if (_logger == null) {
            _logger = Logger.getLogger(EELogDomains.EE_ADMIN_LOGGER);
        }
        return _logger;
    }
    
    /**
     * Returns a dynamic proxy capable of implementing the ServerRuntimeMBean interface.
     */
    public static ServerRuntimeMBean getInstanceProxy(String instanceName)         
    {
        return (ServerRuntimeMBean)Proxy.newProxyInstance(
            ServerRuntimeMBean.class.getClassLoader(), 
            new Class[] {ServerRuntimeMBean.class}, new InstanceProxy(instanceName));
    }
    
    /**
     * Get a connection to the server instances's mbean server using the InstanceRegistry
     */
    private MBeanServerConnection getConnection() throws InstanceException {
        if (_connection == null) {
            _connection = InstanceRegistry.getInstanceConnection(getInstanceName());
        }
        return _connection;
    }    
    
    
    /**
     * Returns the object name for the specified node agent. Really the object name 
     * seems to be the same across all node agents.
     */
    private ObjectName getObjectName() throws IOException, 
        MalformedObjectNameException, InstanceException
    {
        if (_objectName == null) {            
            if (_defaultDomain == null) {
                //The default domain returned by the Mbean Server is not correct here. This
                //is not an issue for config mbeans, only for runtime mbeans
                //_defaultDomain = getConnection().getDefaultDomain();
                _defaultDomain = getDefaultDomain();
            }
            _objectName = new ObjectName(_defaultDomain + ":j2eeType=J2EEServer,name=" + 
                getInstanceName() + ",category=runtime");
        }
        return _objectName;
    }
    
    private Exception mangleInvokeException(Exception ex) {
        Exception result = null;
        //MBeanException and InstanceException can pass through directly. All other exceptions must
        //be wrapped as an instance exception. This is to avoid a UndeclaredException from being thrown
        //from the dynamic proxies invoke method. In other words MBeanException and InstanceException 
        //are the only exceptions being thrown from the ServerRuntimeMBean interface.
        
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
        
        if (ex instanceof InstanceException) {
            result = ex;
        } else {
            result = new InstanceException(ex);
        } 
        
        //If the server is unreachable then we want to mark the connection as disabled
        if (isUnreachable(ex)) {
            try {
                InstanceRegistry.disconnectInstanceConnection(getInstanceName());
            } catch (Exception ex2) {
                getLogger().log(Level.WARNING, "server.proxy.exception", ex2); 
            }
        }
        return result;        
    }
    
    /**
     * Invoke a method on the dynamic proxy
     */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {        
        final String methodName = method.getName();
            getLogger().log(Level.FINEST, "InstanceProxy:invoke " + methodName);
        try {
            return getConnection().invoke(getObjectName(), methodName, 
                args, getParameterTypes(method.getParameterTypes()));                           
        } catch (Exception ex) {           
            ex = mangleInvokeException(ex);
            //Do not log exceptions indicating that the node agent was unreachable (i.e. down)
            if (!isUnreachable(ex)) {                
                getLogger().log(Level.FINE, "server.proxy.exception", ex);         
            } else {
                getLogger().log(Level.FINE, "server.proxy.cannotConnect", getInstanceName());
            }
            throw (ex);
        }
    } 
    
}
