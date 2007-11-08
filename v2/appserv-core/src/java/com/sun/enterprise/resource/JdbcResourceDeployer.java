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
 * @(#) JdbcResourceDeployer.java
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information").
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with iPlanet/Sun Microsystems.
 */
package com.sun.enterprise.resource;

import com.sun.enterprise.server.ResourceDeployer;
import com.sun.enterprise.connectors.util.ResourcesUtil;

import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectorAdminServiceUtils;
import com.sun.enterprise.connectors.ConnectorRuntime;

import com.sun.enterprise.Switch;
import com.sun.enterprise.repository.IASJ2EEResourceFactoryImpl;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

import com.sun.enterprise.ManagementObjectManager;

import javax.naming.InitialContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;


/**
 * Handles Jdbc resource events in the server instance. When user adds a 
 * jdbc resource, the admin instance emits resource event. The jdbc 
 * resource events are propagated to this object.
 *
 * The methods can potentially be called concurrently, therefore implementation
 * need to be synchronized.
 *
 * @author  Nazrul Islam
 * @since   JDK1.4
 */
public class JdbcResourceDeployer implements ResourceDeployer {
    
    private static final StringManager localStrings =
        StringManager.getManager("com.sun.enterprise.resource");
    /** logger for this deployer */
    private static Logger _logger=LogDomains.getLogger(LogDomains.CORE_LOGGER);
    private static final String PM_JNDI_EXTENSION = "__pm";
    /**
     * Deploy the resource into the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @exception Exception thrown if fail
     */
	public synchronized void deployResource(Object resource) throws Exception {
        
        com.sun.enterprise.config.serverbeans.JdbcResource jdbcRes = 
            (com.sun.enterprise.config.serverbeans.JdbcResource) resource;
        
        if (jdbcRes.isEnabled()) {
            String jndiName = jdbcRes.getJndiName();
	    String poolName = jdbcRes.getPoolName();

        // loads dependent jdbc connection pool if not loaded
        loadPool(jdbcRes);

	    ManagementObjectManager mgr = Switch.getSwitch().getManagementObjectManager();
	    mgr.registerJDBCResource( jndiName );
	    ConnectorRuntime runtime = ConnectorRuntime.getRuntime();
	    runtime.createConnectorResource( jndiName, poolName, null);
	    runtime.createConnectorResource( getPMJndiName( jndiName),
	            poolName, null);
	    _logger.finest("deployed resource " + jndiName );	    
        } else {
            _logger.log(Level.INFO, "core.resource_disabled", 
                new Object[] {jdbcRes.getJndiName(), 
                              IASJ2EEResourceFactoryImpl.JDBC_RES_TYPE});
        }
    }

    /**
     * Undeploy the resource from the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @exception Exception thrown if fail
     */
	public synchronized void undeployResource(Object resource) 
            throws Exception {

        com.sun.enterprise.config.serverbeans.JdbcResource jdbcRes = 
            (com.sun.enterprise.config.serverbeans.JdbcResource) resource;
        
	String jndiName = jdbcRes.getJndiName();
	String pmJndiName = getPMJndiName( jdbcRes.getJndiName() );

	ConnectorRuntime runtime = ConnectorRuntime.getRuntime();
	runtime.deleteConnectorResource( jndiName );
	runtime.deleteConnectorResource( pmJndiName );
	
        ManagementObjectManager mgr =
                Switch.getSwitch().getManagementObjectManager();
        mgr.unregisterJDBCResource( jndiName );

        //Since 8.1 PE/SE/EE - if no more resource-ref to the pool 
        //of this resource exists in this server instance, remove 
        //pool from connector runtime
        checkAndDeletePool(jdbcRes);
        

    }

    /**
     * Redeploy the resource into the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @exception Exception thrown if fail
     */
	public synchronized void redeployResource(Object resource)
            throws Exception {

        undeployResource(resource);
        deployResource(resource);
    }

    /**
     * Enable the resource in the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @exception Exception thrown if fail
     */
	public synchronized void enableResource(Object resource) throws Exception {
        deployResource(resource);
    }

    /**
     * Disable the resource in the server's runtime naming context
     *
     * @param resource a resource object (eg. JmsResource)
     * @exception Exception thrown if fail
     */
	public synchronized void disableResource(Object resource) throws Exception {
        undeployResource(resource);
    }


    /**
     * Utility method to find a resource from Resources beans and converte
     * it to a resource object to be used by the implemented ResourceDeployer
     *
     * @param     name      resource name (normally the jndi-name)
     * @param     rbeans    Resources config-beans  
     * @exception Exception thrown if fail
     */
    public Object getResource(String name, Resources rbeans) throws Exception {

        Object res = rbeans.getJdbcResourceByJndiName(name);

        if (res == null) {
            String msg = localStrings.getString("resource.no_resource",name);
            throw new Exception(msg);
        }

        return res;
    }


    /* Return the system PM name for the JNDI name*/
    private String getPMJndiName( String jndiName )  {
        return jndiName + PM_JNDI_EXTENSION;
    }

    private void loadPool(com.sun.enterprise.config.serverbeans.JdbcResource jr)
            throws Exception {

        String poolName = jr.getPoolName();
        Resources resources = (Resources) jr.parent();
        ConfigBean cb = resources.getJdbcConnectionPoolByName(poolName);
        if (cb != null) {
            try {
                InitialContext ic = new InitialContext();
                ic.lookup(ConnectorAdminServiceUtils.
                                getReservePrefixedJNDINameForPool(poolName));
            } catch (Exception e) {
                // pool is not loaded
                JdbcConnectionPoolDeployer deployer = 
                        new JdbcConnectionPoolDeployer();
                deployer.actualDeployResource(cb);
            }
        }
    }

    /**
     * Checks if no more resource-refs to resources exists for the 
     * JDBC connection pool and then deletes the pool
     * @param cr Jdbc Resource Config bean
     * @throws Exception if unable to access configuration/undeploy resource. 
     * @since 8.1 pe/se/ee
     */
    private void checkAndDeletePool(com.sun.enterprise.config.serverbeans.JdbcResource
                    cr) throws Exception {
        String poolName = cr.getPoolName();
        Resources res = (Resources) cr.parent();
        
        try {
            boolean poolReferred = 
                ResourcesUtil.createInstance().isJdbcPoolReferredInServerInstance(poolName);
            if (!poolReferred) {
                _logger.fine("Deleting JDBC pool " + poolName + "as there is no more " +
                        "resource-refs to the pool in this server instance");
                com.sun.enterprise.config.serverbeans.JdbcConnectionPool jcp 
                                    = res.getJdbcConnectionPoolByName(poolName);
                //Delete/Undeploy Pool
                JdbcConnectionPoolDeployer deployer = 
                        new JdbcConnectionPoolDeployer();
                deployer.actualUndeployResource(jcp);
            }
        } catch (ConfigException ce) {
            _logger.warning(ce.getMessage());
            _logger.fine("Exception while deleting pool : " + ce );
            throw ce;
        }
    }
    
}
