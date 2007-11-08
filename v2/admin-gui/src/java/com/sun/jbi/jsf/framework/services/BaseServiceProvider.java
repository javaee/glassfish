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
 * BaseServiceProvider.java
 */
package com.sun.jbi.jsf.framework.services;

import com.sun.jbi.jsf.framework.connectors.ServerConnector;
import com.sun.jbi.jsf.framework.services.administration.providers.glassfish.ServerInformation;
import com.sun.jbi.jsf.framework.services.administration.providers.glassfish.ServerInformationImpl;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 *
 * @author Sun Microsystems
 */
public class BaseServiceProvider implements Service {
    
    protected ServerConnector serverConnector;    
    protected MBeanServerConnection serverConnection = null;
    protected String targetName;
    protected ServerInformation serverInfo;
    
    private Logger logger = Logger.getLogger(BaseServiceProvider.class.getName());
    
    
    /** Creates a new instance of BaseServiceProvider */
    public BaseServiceProvider(ServerConnector connector,String targetName) {
        serverConnector = connector;
        serverConnection = serverConnector.getConnection();
        this.targetName = targetName;
        // do cluster setup
        setup();
    }
    
    protected void setup() {
        try {
            //  for cluster environment - use this class to get instance MBS
            serverInfo = new ServerInformationImpl(serverConnection);
            if ( serverInfo.isDAS() ) {
                 serverConnection = serverInfo.getMBeanServerConnection(targetName,false);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        
    }
    
    /**
     * invoke an mbean operation
     * @param objectName    object name of mbean
     * @param operationName name of mbean operation to invoke 
     */
    public Object invoke(ObjectName objectName, String operationName) {
        Object resultObject = null;
        try {
            if( this.serverConnection != null && serverConnection.isRegistered(objectName) ) {
                    resultObject = (String) this.serverConnection.invoke(objectName,
                            operationName,
                            null,
                            null);
            }
        } catch(Exception e) {
           e.printStackTrace();
        }
        return resultObject;
    }    
    
    /**
     * invoke an mbean operation
     * @param objectName    object name of mbean
     * @param operationName name of mbean operation to invoke 
     * @param parameters    list of  parameters
     */
    public Object invoke(ObjectName objectName, String operationName, Object[] parameters) {
        Object result = null;
        String[] signature = this.getSignatures(parameters);
        
        try {
            if (this.serverConnection != null && serverConnection.isRegistered(objectName) ) {
                result = serverConnection.invoke(objectName,
                        operationName,
                        parameters,
                        signature);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }  

    /**
     * invoke an mbean operation
     * @param objectName    object name of mbean
     * @param operationName name of mbean operation to invoke 
     * @param parameters    list of  parameters
     * @param signatures    list of parameters signatures
     */
    public Object invoke(ObjectName objectName, String operationName, Object[] parameters, String[] signatures) {
        Object result = null;
        
        try {
            if (this.serverConnection != null && serverConnection.isRegistered(objectName) ) {
                result = serverConnection.invoke(objectName,
                        operationName,
                        parameters,
                        signatures);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }  
    
    
    /**
     * invoke an mbean operation and return a long result
     * @param objectName    object name of mbean
     * @param operationName name of mbean operation to invoke 
     * @param parameters    list of  parameters
     */
    public long invokeLong(ObjectName objectName, String operationName, Object[] parameters) {
        Long result = (Long)invoke(objectName,operationName,parameters);
        long value = -1;
        if ( result!=null ) {
            value = result.longValue();
        }
        return value;
    }
    
    
    /** get the attribute given the objectname ane attribute name
     * @param   objectName  the object name
     * @param   attrName    the attribute name
     */
    public Object getAttribute(ObjectName objectName, String attrName) {
        Object attrValue = null;
        try {
            
            if ( serverConnection!=null && serverConnection.isRegistered(objectName) ) {
                attrValue = serverConnection.getAttribute(objectName,attrName);
            }
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        return attrValue;
    }
    
    
    /** get the signatures of the params
     * @param params    list of params
     * @return list of parameter signatures
     */
    protected String[] getSignatures(Object[] params) {
        if (params == null || params.length == 0) {
            return null;
        }
        String[] signatures = new String[params.length];
        for (int index = 0; index < params.length; index++) {
            if(params[index] == null) {
                signatures[index] = "java.lang.Object";
            } else {
                signatures[index] = params[index].getClass().getName();
            }
        }
        return signatures;
    }    
    
    
}
