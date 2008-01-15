/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.phobos;

import com.sun.phobos.container.FileResourceService;
import com.sun.phobos.container.MappableFileResourceService;
import com.sun.phobos.container.ResourceService;
import com.sun.phobos.container.grizzly.GrizzlyPhobosAdapter;
import static com.sun.phobos.container.Constants.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import com.sun.phobos.container.Constants;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.Response;

import com.sun.phobos.container.RequestWrapper;
import com.sun.phobos.container.ResponseWrapper;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.container.Adapter;


/**
 * Phobos Adapter to run withing Glassfish v3.
 *
 * @author Jerome Dochez
 */
public class GlassFishPhobosAdapter extends GrizzlyPhobosAdapter implements ApplicationContainer, Adapter {
    
    final String contextRoot;
    
    /** Hacky implementation for now. 
     *  Will need to be reworked 
     */
    public GlassFishPhobosAdapter(String contextRoot, Properties envProp) {
        super();
        this.contextRoot = contextRoot;
        envProp.setProperty(Constants.PHOBOS_CONTEXT_ROOT_PROPERTY_NAME, contextRoot);
        setProperties(envProp);
        this.setScratchDirectoryName(System.getProperty("java.io.tmpdir"));
        getResourceService();
        startup();
    }
    
        
    protected String getPlatform() {
        return "glassfish";
    }    
    
    public ResourceService getResourceService() {
        
        ResourceService resourceService = super.getResourceService();
        
        if (resourceService!=null) {
            return resourceService;
        }
        
        String appDir = getProperty(PHOBOS_APPLICATION_DIR_PROPERTY_NAME);
        String envDir = getProperty(PHOBOS_ENVIRONMENT_DIR_PROPERTY_NAME);
        String frameworkDir = getProperty(PHOBOS_FRAMEWORK_DIR_PROPERTY_NAME);
        String staticDir = getProperty(PHOBOS_STATIC_DIR_PROPERTY_NAME);
        
        if (appDir == null && envDir == null && frameworkDir == null && staticDir == null) {
            resourceService = new FileResourceService(getProperty(PHOBOS_HOME_PROPERTY_NAME));
        }
        else {
            Map<String,String> prefixMap = new HashMap<String, String>();
            if (appDir != null) {
                prefixMap.put("application", appDir);
            }
            if (envDir != null) {
                prefixMap.put("environment", envDir);
            }
            if (frameworkDir != null) {
                prefixMap.put("framework", frameworkDir);
            }
            if (staticDir != null) {
                prefixMap.put("static", staticDir);
            }
            resourceService = new MappableFileResourceService(getProperty(PHOBOS_HOME_PROPERTY_NAME), prefixMap);
        }
        
        super.setResourceService(resourceService);
        return resourceService;        
    }
   
    protected void afterService(RequestWrapper<Request> request, ResponseWrapper<Response> response) {
    //    request.unwrap().action(ActionCode.ACTION_POST_REQUEST , null);
    }

    /**
     * Starts an application container.
     * ContractProvider starting should not throw an exception but rather should
     * use their prefered Logger instance to log any issue they encounter while
     * starting. Returning false from a start mean that the container failed
     * to start.
     * @return true if the container startup was successful.
     */
    public boolean start() {
        return true;
    }

    /**
     * Stop the application container
     * @return true if stopping was successful.
     */
    public boolean stop() {
        shutdown();
        return true;
    }

    /**
     * Returns the class loader associated with this application
     *
     * @return ClassLoader for this app
     */
    public ClassLoader getClassLoader() {
        return null;
    }    
    
    public String getContextRoot() {
        return contextRoot;
    }

    /**
     * Returns the deployment descriptor associated with this application
     *
     * @return deployment descriptor if they exist or null if not
     */
    public Object getDescriptor() {
        return null;
    }
}
