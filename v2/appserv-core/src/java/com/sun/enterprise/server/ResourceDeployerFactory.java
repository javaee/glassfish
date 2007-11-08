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
 * @(#) ResourceDeployerFactory.java
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
package com.sun.enterprise.server;


import com.sun.enterprise.resource.AdminObjectResourceDeployer;
import com.sun.enterprise.resource.ConnectorConnectionPoolDeployer;
import com.sun.enterprise.resource.ConnectorResourceDeployer;
import com.sun.enterprise.resource.ResourceAdapterConfigDeployer;
import com.sun.enterprise.resource.JdbcResourceDeployer;
import com.sun.enterprise.resource.PersistenceManagerFactoryResourceDeployer;
import com.sun.enterprise.resource.CustomResourceDeployer;
import com.sun.enterprise.resource.ExternalJndiResourceDeployer;
import com.sun.enterprise.resource.MailResourceDeployer;
import com.sun.enterprise.resource.JdbcConnectionPoolDeployer;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.admin.event.ResourceDeployEvent;
import com.sun.enterprise.util.i18n.StringManager;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

/**
 * Deployer factory for different resource types.
 */
public class ResourceDeployerFactory {

    /** Admin object resource deployer */
    private ResourceDeployer resourceAdapterConfigDeployer_ = null;

    /** Admin object resource deployer */
    private ResourceDeployer adminObjectResourceDeployer_ = null;

    /** Connector Resource deployer */
    private ResourceDeployer connectorResourceDeployer_ = null;

    /** Connector Connection pool */
    private ResourceDeployer connectorConnectionPoolDeployer_ = null;

    /** jdbc resource deployer */
    private ResourceDeployer jdbcResourceDeployer_  = null;

    /** pmf resource deployer */
    private ResourceDeployer pmfResourceDeployer_  = null;

    /** custom resource deployer */
    private ResourceDeployer customResourceDeployer_        = null;

    /** external jndi resource deployer */
    private ResourceDeployer externalJndiResourceDeployer_  = null;

    /** java mail resource deployer */
    private ResourceDeployer mailResourceDeployer_  = null;
    
    // start 4650787
	// JdbcConnectionPoolDeployer_ was added to support dynamic connection pool - bug # 4650787
    /** jdbc connection pool deployer */
    private ResourceDeployer JdbcConnectionPoolDeployer_  = null;
    // end 4650787    

	/** logger to log core messages */
	static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);
    
	private static StringManager localStrings =
        StringManager.getManager("com.sun.enterprise.server");
    
    /**
     * Initializes the resource deployers.
     */
    public ResourceDeployerFactory() {
        this.resourceAdapterConfigDeployer_ = 
                 new ResourceAdapterConfigDeployer();
        this.adminObjectResourceDeployer_          = new AdminObjectResourceDeployer();
        this.connectorResourceDeployer_          = new ConnectorResourceDeployer();
        this.connectorConnectionPoolDeployer_          = new ConnectorConnectionPoolDeployer();
        this.jdbcResourceDeployer_         = new JdbcResourceDeployer();
        this.pmfResourceDeployer_          = 
                            new PersistenceManagerFactoryResourceDeployer();
        this.customResourceDeployer_       = new CustomResourceDeployer();
        this.externalJndiResourceDeployer_ = new ExternalJndiResourceDeployer();
        this.mailResourceDeployer_         = new MailResourceDeployer();
    // start 4650787
	// new JdbcConnectionPoolDeployer instance was created to support
	// dynamic connection pool - bug # 4650787
    /** jdbc connection pool deployer */
        this.JdbcConnectionPoolDeployer_    = new JdbcConnectionPoolDeployer();
	// end 4650787        
    }

    /**
     * Returns a resource deployer for the given resource type.
     *
     * @param    type    resource type
     * 
     * @throws   Exception    if unknown resource type
     */
	public ResourceDeployer getResourceDeployer(String type) throws Exception {

	ResourceDeployer deployer = null;

	if (type.equals(ResourceDeployEvent.RES_TYPE_JDBC)) {

            deployer = this.jdbcResourceDeployer_;

        } else if (type.equals(ResourceDeployEvent.RES_TYPE_PMF)) {

            deployer = this.pmfResourceDeployer_;

	} else if (type.equals(ResourceDeployEvent.RES_TYPE_CUSTOM)) {

            deployer = this.customResourceDeployer_;

	} else if (type.equals(ResourceDeployEvent.RES_TYPE_EXTERNAL_JNDI)) {

            deployer = this.externalJndiResourceDeployer_;

        } else if (type.equals(ResourceDeployEvent.RES_TYPE_MAIL)) {

            deployer = this.mailResourceDeployer_;

        } else if (type.equals(ResourceDeployEvent.RES_TYPE_AOR)) {

            deployer = this.adminObjectResourceDeployer_;

        } else if (type.equals(ResourceDeployEvent.RES_TYPE_CR)) {

            deployer = this.connectorResourceDeployer_;

        } else if (type.equals(ResourceDeployEvent.RES_TYPE_CCP)) {

            deployer = this.connectorConnectionPoolDeployer_;

        } else if (type.equals(ResourceDeployEvent.RES_TYPE_RAC)) {

            deployer = this.resourceAdapterConfigDeployer_;

	} else if (type.equals(ResourceDeployEvent.RES_TYPE_JCP)) {
    // start 4650787
	// type check was created to support
	// dynamic connection pool - bug # 4650787
            deployer = this.JdbcConnectionPoolDeployer_;
	// end 4650787

        } else {
	    String msg = localStrings.getString(
				"resource.deployment.resource_type_not_implemented" ,type);
	    throw new Exception(msg);
        }

        return deployer;
	}

    /**
     * Convenience method. It delegates the call to the deployer with the 
     * config resource beands. The deployer returns the implementation 
     * specific object that it wants.
     * 
     * @param    type    type of resource
     * @param    name    jndi name of the resource
     * @param    rbeans  config resource beans
     */
	public Object getResource(String type, String name, Resources rbeans) 
            throws Exception {

        return getResourceDeployer(type).getResource(name, rbeans);
	}
}
