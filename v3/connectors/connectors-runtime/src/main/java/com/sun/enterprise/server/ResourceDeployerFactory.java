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


import com.sun.appserv.connectors.spi.ConnectorConstants;
import com.sun.enterprise.resource.deployer.ConnectorConnectionPoolDeployer;
import com.sun.enterprise.resource.deployer.ConnectorResourceDeployer;
import com.sun.enterprise.resource.deployer.JdbcConnectionPoolDeployer;
import com.sun.enterprise.resource.deployer.JdbcResourceDeployer;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import java.util.logging.Logger;

/**
 * Deployer factory for different resource types.
 */
public class ResourceDeployerFactory {
    //TODO V3 should this be a Service ?
    /*TODO V3 can't the factory do deploy/undeploy the resource by finding the resource-type and the
    TODO V3   deployer automatically ? */
    /**
     * Connector Resource deployer
     */
    private ResourceDeployer connectorResourceDeployer_ = null;

    /**
     * Connector Connection pool
     */
    private ResourceDeployer connectorConnectionPoolDeployer_ = null;

    /**
     * jdbc resource deployer
     */
    private ResourceDeployer jdbcResourceDeployer_ = null;

    /**
     * jdbc connection pool deployer
     */
    private ResourceDeployer JdbcConnectionPoolDeployer_ = null;

    /**
     * logger to log core messages
     */
    static Logger _logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    private static StringManager localStrings =
            StringManager.getManager(ResourceDeployerFactory.class);

    /**
     * Initializes the resource deployers.
     */
    public ResourceDeployerFactory() {
        this.connectorResourceDeployer_ = new ConnectorResourceDeployer();
        this.connectorConnectionPoolDeployer_ = new ConnectorConnectionPoolDeployer();
        this.jdbcResourceDeployer_ = new JdbcResourceDeployer();
        this.JdbcConnectionPoolDeployer_ = new JdbcConnectionPoolDeployer();
    }

    /**
     * Returns a resource deployer for the given resource type.
     *
     * @param type resource type
     * @throws Exception if unknown resource type
     */
    public ResourceDeployer getResourceDeployer(String type) throws Exception {

        ResourceDeployer deployer = null;

        if (type.equals(ConnectorConstants.RES_TYPE_JDBC)) {
            deployer = this.jdbcResourceDeployer_;
        } else if (type.equals(ConnectorConstants.RES_TYPE_CR)) {
            deployer = this.connectorResourceDeployer_;
        } else if (type.equals(ConnectorConstants.RES_TYPE_CCP)) {
            deployer = this.connectorConnectionPoolDeployer_;
        } else if (type.equals(ConnectorConstants.RES_TYPE_JCP)) {
            deployer = this.JdbcConnectionPoolDeployer_;
        } else {
            String msg = localStrings.getString(
                    "resource.deployment.resource_type_not_implemented", type);
            throw new Exception(msg);
        }
        return deployer;
    }
}
