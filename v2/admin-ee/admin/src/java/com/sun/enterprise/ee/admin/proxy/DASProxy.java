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

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.logging.ee.EELogDomains;
import com.sun.enterprise.ee.admin.clientreg.InstanceRegistry;
import com.sun.enterprise.ee.admin.proxy.BaseProxy;
import com.sun.enterprise.admin.servermgmt.InstanceException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 *
 * @author  kebbs
 */
public class DASProxy extends BaseProxy implements InvocationHandler {     
    
    private static Logger _logger = null;             
    
            
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
    public static Object getDASProxy(Class theInterface, ObjectName objName)         
    {        
        return Proxy.newProxyInstance(
            theInterface.getClassLoader(), 
            new Class[] {theInterface}, new DASProxy(objName));
    }
    
    private ObjectName _objectName = null;
    
    private MBeanServerConnection _connection = null;   
    
    /** Creates a new instance of InstanceProxy */
    private DASProxy(ObjectName objName) {
        super();
        _objectName = objName;
    }
        
    private ConfigContext getConfigContext()
    {
        return AdminService.getAdminService().getAdminContext().getAdminConfigContext();
    }
    
    /**
     * Get a connection to the server instances's mbean server using the InstanceRegistry
     */
    private MBeanServerConnection getConnection() throws InstanceException {
        if (_connection == null) {            
            _connection = InstanceRegistry.getDASConnection(getConfigContext());
        }
        return _connection;
    }    
    
    
    /**
     * Returns the object name for the specified node agent. Really the object name 
     * seems to be the same across all node agents.
     */
    private ObjectName getObjectName()
    {
        return _objectName;
    }
    
    /**
     * Invoke a method on the dynamic proxy
     */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable
    {        
        final String methodName = method.getName();
        getLogger().log(Level.FINEST, "DASProxy:invoke " + methodName);
        try {
            return getConnection().invoke(getObjectName(), methodName, 
                args, getParameterTypes(method.getParameterTypes()));                           
        } catch (Exception ex) {           
            //Do not log exceptions indicating that the node agent was unreachable (i.e. down)
            if (!isUnreachable(ex)) {
                getLogger().log(Level.FINE, "das.proxy.exception", ex);         
            } else {
                getLogger().log(Level.FINE, "das.proxy.cannotConnect", getObjectName());
            }
            throw (ex);
        }
    } 
    
}
