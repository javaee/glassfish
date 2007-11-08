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
package com.sun.enterprise.jbi.serviceengine.install;
import java.io.IOException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import com.sun.enterprise.jbi.serviceengine.ServiceEngineException;

/**
 *
 * @author Manisha Umbarje
 */
public class MBeanHelper {
    
    private static final String OBJ_NAME_PREFIX ="com.sun.jbi:JbiName=";
    
    private static final String SERVICE_NAME =",ServiceName=";
    
    private static final String CONTROL_TYPE =",ControlType=";
    
    private static final String COMPONENT_TYPE=",ComponentType=System";
    
    private static final String ADMIN_SERVICE_CONTROL_TYPE =
            "AdministrationService";
    
    public static final String INSTALLATION_SERVICE = "InstallationService";
    
    public static final String ADMIN_SERVICE = "AdminService";
    
    public static final String CONFIGURATION_SERVICE = "Configuration";
    
    // Esb related MBeans
    
    public static final String FRAMEWORK = "Framework";
    
    public static final String ESB_INSTALLATION_SERVICE =
            "com.sun.jbi.esb:ServiceType=Installation";
    
    public static final String ESB_LIFECYCLE_SERVICE =
            "com.sun.jbi.esb:ServiceType=LifeCycle";
    
    private MBeanServerConnection mbeanServer;
    
    /** Creates a new instance of ObjectNames */
    public MBeanHelper(MBeanServerConnection mbeanServer) {
        this.mbeanServer = mbeanServer;
    }
    
    public ObjectName getObjectName(String domainName, String serviceName)
    throws ServiceEngineException{
        if(serviceName != null && domainName != null) {
            String objName = OBJ_NAME_PREFIX + domainName + SERVICE_NAME;
            String controlType = "";
            if(serviceName.equals(INSTALLATION_SERVICE)) {
                controlType = serviceName;
            } else if(serviceName.equals(ADMIN_SERVICE)) {
                controlType = ADMIN_SERVICE_CONTROL_TYPE;
            } else if(serviceName.equals(FRAMEWORK)) {
                controlType = CONFIGURATION_SERVICE;
            }
            objName = objName + serviceName + CONTROL_TYPE +
                    controlType + COMPONENT_TYPE;
            return getObjectName(objName);
        }
        throw new ServiceEngineException("Either JBI Instance name or Service name or both null");
    }
    
    public ObjectName getObjectName(String stringifiedObjName) throws ServiceEngineException {
        
        if(stringifiedObjName != null) {
            try {
                ObjectInstance objInstance =
                        mbeanServer.getObjectInstance(new ObjectName(stringifiedObjName));
                return objInstance.getObjectName();
            } catch(MalformedObjectNameException e) {
                throw new ServiceEngineException(e.getMessage());
            } catch(InstanceNotFoundException infe) {
                throw new ServiceEngineException(infe.getMessage());
            } catch(IOException ioe) {
                throw new ServiceEngineException(ioe.getMessage());
            }
        } else
            throw new ServiceEngineException(" Null object name");
        
    }
    public Object invokeMBeanOperation(ObjectName objName,
            String operationName, Object[] params, String[] signature)
            throws ServiceEngineException {
        Object result = null;
        
        try {
            result = mbeanServer.invoke(objName,operationName,
                    params, signature);
        } catch (InstanceNotFoundException notFoundEx) {
            throw new ServiceEngineException(notFoundEx);
        } catch ( ReflectionException rEx){
            throw new ServiceEngineException(rEx);
        } catch ( MBeanException mbeanEx ) {
            throw ServiceEngineException.filterExceptions(mbeanEx);
        } catch (RuntimeMBeanException rtEx){
            throw ServiceEngineException.filterExceptions(rtEx);
        } catch (RuntimeOperationsException rtOpEx){
            throw ServiceEngineException.filterExceptions(rtOpEx);
        } catch (Exception ex ) {
            throw ServiceEngineException.filterExceptions(ex);
        }
        
        return result;
        
    }
}
