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
 * SunManagementService.java
 *  Implements ManagementService interface
 *
 */

package com.sun.jbi.jsf.framework.services.management.providers;

import com.sun.jbi.jsf.framework.common.GenericConstants;
import com.sun.jbi.jsf.framework.common.Util;
import com.sun.jbi.jsf.framework.connectors.ServerConnector;
import com.sun.jbi.jsf.framework.services.BaseServiceProvider;
import com.sun.jbi.jsf.framework.services.management.ManagementService;
import com.sun.jbi.jsf.framework.services.management.StateConstants;
import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Logger;
import javax.management.ObjectName;

/**
 *
 * @author Sun Microsystems
 */
public class SunManagementService extends BaseServiceProvider implements Serializable, ManagementService {
    
    private static final String DOMAIN_NAME_PREFIX = "com.sun.jbi:";                    //$NON-NLS-1$
    private static final String DOMAIN_NAME_SUFFIX = "JbiName=";                        //$NON-NLS-1$
    private static final String CONTROL_TYPE_PREFIX="ControlType=Lifecycle";            //$NON-NLS-1$
    private static final String INSTALLED_TYPE_PREFIX = "InstalledType=";               //$NON-NLS-1$
    private static final String COMPONENT_TYPE_PREFIX = "ComponentType=Installed";      //$NON-NLS-1$
    private static final String COMPONENT_NAME_PREFIX = "ComponentName=";               //$NON-NLS-1$
    private static final String ADMIN_SERVICE_OBJECTNAME = "com.sun.jbi:ServiceName=JbiReferenceAdminUiService,ComponentType=System";
    
    private Logger logger = Logger.getLogger(SunManagementService.class.getName());
    
    /** Creates a new instance of SunManagementService */
    public SunManagementService(ServerConnector connector,String targetName) {
        super(connector,targetName);
    }
    
    
    /**
     * get the state of the component
     */
    public String getState(String componentName, String componentType) {
        String state = StateConstants.SHUTDOWN_STATE;
        
        try {
            String name = getObjectName(componentName,componentType);
            ObjectName objName = new ObjectName(name);
            // get attribute "CurrentState"
            state = (String)getAttribute(objName,"CurrentState");     //$NON-NLS-1$
            
        } catch(Exception e) {
            e.printStackTrace();
        }
        return state;
    }
    
    private String getObjectName(String componentName, String componentType) {
        String name =
                    DOMAIN_NAME_PREFIX +
                    DOMAIN_NAME_SUFFIX  + targetName + GenericConstants.COMMA_SEPARATOR +
                    COMPONENT_NAME_PREFIX + componentName + GenericConstants.COMMA_SEPARATOR + 
                    CONTROL_TYPE_PREFIX + GenericConstants.COMMA_SEPARATOR +
                    COMPONENT_TYPE_PREFIX +  GenericConstants.COMMA_SEPARATOR +
                    INSTALLED_TYPE_PREFIX + Util.mapInstalledType(componentType);
        return name;
    }
    

    /**
     * start (service engine, binding component,service assembly, service unit, endpoint)
     * @param componentName name of the component
     */
    public String start(String componentName, String componentType) {
        String result = "";
        if(componentType.equals(GenericConstants.SA_TYPE)) {
            result = startServiceAssembly(componentName);
        } else {
            try {
                String name = getObjectName(componentName,componentType);
                ObjectName objName = new ObjectName(name);
                invoke(objName,"start");     //$NON-NLS-1$
            } catch(Exception e) {
                e.printStackTrace();
            }       
        }
        return result;
    }

    /**
     * stop (service engine, binding component,service assembly, service unit, endpoint)
     * @param componentName name of the component
     */
    public String stop(String componentName, String componentType) {
        String result = "";
        if(componentType.equals(GenericConstants.SA_TYPE)) {
            result = stopServiceAssembly(componentName);
        } else {
            try {
                String name = getObjectName(componentName,componentType);
                ObjectName objName = new ObjectName(name);
                invoke(objName,"stop");     //$NON-NLS-1$
            } catch(Exception e) {
                e.printStackTrace();
            }       
        }
        return result;
    }

    /**
     * shutdowm (service engine, binding component,service assembly, service unit, endpoint)
     * @param componentName name of the component
     */
    public String shutdown(String componentName, String componentType) {
        String result = "";
        if(componentType.equals(GenericConstants.SA_TYPE)) {
            result = shutdownServiceAssembly(componentName);
        } else {
            try {
                String name = getObjectName(componentName,componentType);
                ObjectName objName = new ObjectName(name);
                invoke(objName,"shutDown");     //$NON-NLS-1$
            } catch(Exception e) {
                e.printStackTrace();
            }       
        }
        return result;
    }
    
    /**
     * shutdowmForce (service engine, binding component,service assembly, service unit, endpoint)
     * @param componentName name of the component
     */
    public String shutdownForce(String componentName, String componentType) {
        String result = "";
        try {
            String name = getObjectName(componentName,componentType);
            ObjectName objName = new ObjectName(name);
            invoke(objName,"shutDownForce");     //$NON-NLS-1$
        } catch(Exception e) {
            e.printStackTrace();
        }        
        return result;
    }    
    
    public String suspend(String componentName, String componentType) {
        String result = "";
        // todo
        return result;
    }

    public String resume(String componentName, String componentType) {
        String result = "";
        // todo
        return result;
    }
    
    private String startServiceAssembly(String componentName) {
        String result = "";
        try {
            ObjectName objName = new ObjectName(ADMIN_SERVICE_OBJECTNAME);
            invoke(objName,"startServiceAssembly",new Object[] {componentName,targetName});     //$NON-NLS-1$
        } catch(Exception e) {
            e.printStackTrace();
        }        
        return result;
    }
    
    private String stopServiceAssembly(String componentName) {
        String result = "";
        try {
            ObjectName objName = new ObjectName(ADMIN_SERVICE_OBJECTNAME);
            invoke(objName,"stopServiceAssembly",new Object[] {componentName,targetName});     //$NON-NLS-1$
        } catch(Exception e) {
            e.printStackTrace();
        }        
        return result;
    }
     
    private String shutdownServiceAssembly(String componentName) {
        String result = "";
        try {
            ObjectName objName = new ObjectName(ADMIN_SERVICE_OBJECTNAME);
            invoke(objName,"shutdownServiceAssembly",new Object[] {componentName,targetName});     //$NON-NLS-1$
        } catch(Exception e) {
            e.printStackTrace();
        }        
        return result;
    }
 
}
