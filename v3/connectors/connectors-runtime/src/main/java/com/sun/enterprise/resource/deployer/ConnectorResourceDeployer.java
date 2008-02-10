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


package com.sun.enterprise.resource;

import com.sun.enterprise.server.ResourceDeployer;
import com.sun.enterprise.connectors.util.ResourcesUtil;

import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.enterprise.connectors.ConnectorAdminServiceUtils;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.deployment.ConnectionDefDescriptor;
import com.sun.logging.LogDomains;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.connectors.ConnectorConstants;
import javax.naming.InitialContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils;

import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  * @author    Srikanth P
  */

public class ConnectorResourceDeployer implements ResourceDeployer {

    static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
    public synchronized void deployResource(Object resource) throws Exception {

        ConnectorResource domainResource = 
            (com.sun.enterprise.config.serverbeans.ConnectorResource)resource;
        String jndiName = domainResource.getJndiName();
        String poolName = domainResource.getPoolName();
        ConnectorRuntime crt = ConnectorRuntime.getRuntime();
        _logger.log(Level.FINE,
                   "Calling backend to add connector resource",jndiName);

        // load connection pool if not loaded from the ctx in event 
        loadPool(domainResource);

        crt.createConnectorResource(jndiName,poolName,null);
        _logger.log(Level.FINE,
                   "Added connector resource in backend",jndiName);
    }

    public synchronized void undeployResource(Object resource) 
                  throws Exception {
        ConnectorResource domainResource = 
           (com.sun.enterprise.config.serverbeans.ConnectorResource)resource;
        String jndiName = domainResource.getJndiName();
        ConnectorRuntime crt = ConnectorRuntime.getRuntime();
        crt.deleteConnectorResource(jndiName);
        
        //Since 8.1 PE/SE/EE - if no more resource-ref to the pool 
        //of this resource in this server instance, remove pool from connector
        //runtime
        checkAndDeletePool(domainResource);
        
    }


    public synchronized void redeployResource(Object resource) 
                  throws Exception {
    }

    public synchronized void disableResource(Object resource) 
                  throws Exception {
        undeployResource(resource);
    }

    public synchronized void enableResource(Object resource) throws Exception {
        deployResource(resource);
    }

    public Object getResource(String name, Resources rbeans) throws Exception {

        Object res = rbeans.getConnectorResourceByJndiName(name);
        if (res == null) {
            Exception ex = new Exception("No such resource");
            _logger.log(Level.SEVERE,"no_resource",name);
            _logger.log(Level.SEVERE,"",ex);
            throw ex;
        }
        return res;

    }

    private void loadPool(ConnectorResource cr) throws Exception {

        String poolName = cr.getPoolName();
        Resources resources = (Resources) cr.parent();
        ConfigBean cb = resources.getConnectorConnectionPoolByName(poolName);
        com.sun.enterprise.config.serverbeans.ConnectorConnectionPool cp = 
        	(com.sun.enterprise.config.serverbeans.ConnectorConnectionPool)cb;
        if (cb != null) {
        	if (ConnectionPoolObjectsUtils.isPoolSystemPool(poolName)){
        	    createPool(cp);
        	} else {
        	    try {
                    InitialContext ic = new InitialContext();
                    ic.lookup(ConnectorAdminServiceUtils.getReservePrefixedJNDINameForPool(poolName));
                } catch (Exception e) {
                    // pool is not loaded
                    createPool(cp);
                }
        	}
        }
    }

    /**
     * Checks if no more resource-refs to resources exists for the 
     * connector connection pool and then deletes the pool
     * @param cr ConnectorResource
     * @throws Exception (ConfigException / undeploy exception)
     * @since 8.1 pe/se/ee
     */
    private void checkAndDeletePool(ConnectorResource cr) throws Exception {
        try {
            String poolName = cr.getPoolName();
            Resources res = (Resources) cr.parent();

            boolean poolReferred = 
                ResourcesUtil.createInstance().isPoolReferredInServerInstance(poolName);
            if (!poolReferred) {
                _logger.fine("Deleting pool " + poolName + "as there is no more " +
                        "resource-refs to the pool in this server instance");
                com.sun.enterprise.config.serverbeans.ConnectorConnectionPool ccp 
                                    = res.getConnectorConnectionPoolByName(poolName);
                //Delete/Undeploy Pool
                ConnectorConnectionPoolDeployer deployer = 
                        new ConnectorConnectionPoolDeployer();
                deployer.undeployResource(ccp);
            }
            
        } catch (ConfigException ce) {
            _logger.warning(ce.getMessage());
            _logger.fine("Exception while deleting pool : " + ce );
            throw ce;
        }
    }
    
    private void createPool(com.sun.enterprise.config.serverbeans.ConnectorConnectionPool cp) 
    throws Exception {
        ConnectorConnectionPoolDeployer deployer = 
            new ConnectorConnectionPoolDeployer();
        
        deployer.deployResource(cp);
    }
}
