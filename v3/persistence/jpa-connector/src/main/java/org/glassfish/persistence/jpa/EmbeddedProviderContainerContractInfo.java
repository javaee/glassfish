/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
  Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.persistence.jpa;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.deployment.DeploymentContext;
import org.jvnet.hk2.component.Habitat;

import javax.naming.NamingException;

/**
 * Implementation of ProviderContainerContractInfo while running inside embedded server
 *
 * @author Mitesh Meswani
 */
public class EmbeddedProviderContainerContractInfo extends ServerProviderContainerContractInfo {

    private static final String DEFAULT_EMBEDDED_DS_NAME = "jdbc/__embedded_default";  // NOI18N

    private Habitat habitat;

    public EmbeddedProviderContainerContractInfo(DeploymentContext deploymentContext, ConnectorRuntime connectorRuntime, Habitat habitat, boolean isDas) {
        super(deploymentContext, connectorRuntime, isDas);
        this.habitat = habitat;
    }

    /**
     * get name of default data source. If running in embedded mode, create default data source if one does not exist
     *
     * @return
     */
    @Override
    public String getDefaultDataSourceName() {
        //User is running without defining a data source. Create one if required and return its name
        checkAndCreateDefaultDSResource();
        return DEFAULT_EMBEDDED_DS_NAME;
    }

    @Override
    public boolean isWeavingEnabled() {
        return false; //Weaving is not enabled while running in embedded environment
    }

    /**
     * Check if DEFAULT_EMBEDDED_DS_NAME exists if not create it
     */
    private void checkAndCreateDefaultDSResource() {
        boolean dataSourceExist = true;
        try {
            lookupDataSource(DEFAULT_EMBEDDED_DS_NAME);
        } catch (NamingException e) {
            dataSourceExist = false;
        }

        if (!dataSourceExist) {
            createDefaultDSResource();
        }
    }


    private static final String CONNECTION_POOL_ID = "__embedded_default_pool";
    private static final String CONNECTION_POOL_DB_NAME = "${com.sun.aas.instanceRoot}/lib/databases/embedded_default";
    private static final String EMBEDDED_DERBY_DS_NAME = "org.apache.derby.jdbc.EmbeddedDataSource";

    /**
     * Create JDBC resource and corresponding DataSource resource  
     */
    private void createDefaultDSResource() {
        // Create JDBC  connection pool
        ParameterMap params = new ParameterMap();
        params.add("datasourceclassname", EMBEDDED_DERBY_DS_NAME);
        params.add("property", "databaseName=" + CONNECTION_POOL_DB_NAME + ":connectionAttributes=\\;create\\=true");
        params.add("jdbc_connection_pool_id", CONNECTION_POOL_ID);
        runCommand("create-jdbc-connection-pool", params);

        // Create JDBC resource
        params = new ParameterMap();
        params.add("connectionpoolid", CONNECTION_POOL_ID);
        params.add("jndi_name", DEFAULT_EMBEDDED_DS_NAME);
        runCommand("create-jdbc-resource", params);

        // The actual publishing of the resource into JNDI tree happens in response to asynchronous event
        // To make sure that the event actually got dispatched and the resource did get published, try to look up
        // the resource before returning
        final int NO_OF_RETRIES = 5;
        final int MILIS_TO_SLEEP = 200;
        boolean lookupSucceeded = false;
        for (int i = 0 ; i < NO_OF_RETRIES && !lookupSucceeded; i++) {
            try {
                lookupDataSource(DEFAULT_EMBEDDED_DS_NAME);
                lookupSucceeded = true;
            } catch (NamingException e) {
                try {
                    //Sleep to give the asynchronous notification a chance to go through
                    Thread.sleep(MILIS_TO_SLEEP);
                } catch (InterruptedException e1) {
                    //ignore
                }
            } 
        }
    }

    /**
     * Executes the specified __asadmin command.
     * @param commandName the command to execute
     * @param parameters  the command parameters
     * @return ActionReport object with command execute status details.
     */
    private ActionReport runCommand(String commandName, ParameterMap parameters) {
        CommandRunner cr = habitat.getComponent(CommandRunner.class);
        ActionReport ar = habitat.getComponent(ActionReport.class);
        cr.getCommandInvocation(commandName, ar).parameters(parameters).execute();
        return ar;
    }

}
