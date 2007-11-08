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

package com.sun.enterprise.connectors.util;
/**
 * This class overrides the isReferenced method in the ResourcesUtil class.
 * An instance of this class is always returned when ResourcesUtil.getInstance()
 * happens in the DAS
 *
 * @author    Aditya Gore, Kshitiz Saxena
 * @version
 */

import com.sun.enterprise.config.*;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.RelativePathResolver;
import com.sun.enterprise.connectors.DeferredResourceConfig;

import java.util.logging.Level;

public class DASResourcesUtil extends ResourcesUtil {
    
    /**
     * Flags if a getConnection call is coming from the connector runtime.
     * Only for such a call, we should not be checking isReferenced
     * The reason for this is that the getConnection call might be happening
     * in the DAS for a jdbc resource which does not have a resource-ref in
     * the DAS. However, we still need to give a connection from that
     * resource, so we should not check for referencing info.
     */
    boolean noReferenceCheckNeeded = false;
    
    protected DASResourcesUtil() throws ConfigException{
        super();
    }
    
    protected DASResourcesUtil(ConfigContext configContext) throws ConfigException {
        super(configContext);
    }
    
    public static void setAdminConfigContext() throws ConfigException {
        if(!isDAS()){
            String message = localStrings.getString("cannot.set.admin.context");
            throw new ConfigException(message);
        }
        DASResourcesUtil resUtil = new DASResourcesUtil(com.sun.enterprise.admin.server.core.
                AdminService.getAdminService().getAdminContext().getAdminConfigContext());
        resUtil.noReferenceCheckNeeded = true;
        localResourcesUtil.set(resUtil);
    }
    
    public static void resetAdminConfigContext() {
        localResourcesUtil.set(null);
    }
    
    public String getLocation(String moduleName) {
        if(moduleName == null) {
            return null;
        }
        
        //Use admin config context, as in DAS, the server config context would
        //not have the latest config context changes until the end of the deploy
        //event
        ConfigContext adminConfigCtx = com.sun.enterprise.admin.server.core.
                AdminService.getAdminService().getAdminContext().getAdminConfigContext();
        Domain domain = null;
        try {
             domain = ServerBeansFactory.getDomainBean(adminConfigCtx);
        } catch (ConfigException e) {
            String message = localStrings.getString("error.getting.domain");
            _logger.log(Level.WARNING, message + e.getMessage());
            _logger.log(Level.FINE,message + e.getMessage(), e);
             return null;
        }
        ConnectorModule connectorModule = domain.
                getApplications().getConnectorModuleByName(moduleName);
        
        if (connectorModule == null) {
            return null;
        }
        
        String connectorModuleLocation = connectorModule.getLocation();
        String connectorModuleLocationResolved = RelativePathResolver.
                resolvePath(connectorModuleLocation);
        return connectorModuleLocationResolved;
    }
    
    /**
     * Gets the deployment location for a J2EE application.
     * @param rarName
     * @return
     */
    public String getApplicationDeployLocation(String appName) {
        //Use admin config context, as in DAS, the server config context would
        //not have the latest config context changes until the end of the deploy
        //event
        ConfigContext adminConfigCtx = com.sun.enterprise.admin.server.core.
                AdminService.getAdminService().getAdminContext().getAdminConfigContext();
        Domain domain = null;
        try {
             domain = ServerBeansFactory.getDomainBean(adminConfigCtx);
        } catch (ConfigException e) {
            String message = localStrings.getString("error.getting.domain");
            _logger.log(Level.WARNING, message + e.getMessage());
            _logger.log(Level.FINE,message + e.getMessage(), e);
            return null;
        }
        J2eeApplication app = domain.
                getApplications().getJ2eeApplicationByName(appName);
        return RelativePathResolver.resolvePath(app.getLocation());
        
    }
    
    /**
     * Returns true if the given resource is referenced by this server.
     *
     * @param   resourceName   the name of the resource
     *
     * @return  true if the named resource is used/referred by this server
     *
     * @throws  ConfigException  if an error while parsing domain.xml
     */
    protected boolean isReferenced(String resourceName) throws ConfigException {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("isReferenced in DASResourcesUtil:: " + resourceName);
        }
        
        if (noReferenceCheckNeeded) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("getConnectionFromConnectorRuntime :: true");
            }
            return true;
        }
        
        return super.isReferenced( resourceName );
    }
    
}
