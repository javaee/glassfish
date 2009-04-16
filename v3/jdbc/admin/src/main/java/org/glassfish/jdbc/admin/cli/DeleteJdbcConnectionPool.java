/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.jdbc.admin.cli;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.resource.common.ResourceStatus;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * Delete JDBC Connection Pool Command
 * 
 */
@Service(name="delete-jdbc-connection-pool")
@Scoped(PerLookup.class)
@I18n("delete.jdbc.connection.pool")
public class DeleteJdbcConnectionPool implements AdminCommand {
    
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteJdbcConnectionPool.class);    

    @Param(optional=true, defaultValue="false")
    Boolean cascade;
    
    @Param(name="jdbc_connection_pool_id", primary=true)
    String jdbc_connection_pool_id;

    @Param(optional=true)
    String target; /*depracated - remove after QA,doc,CCC approval*/
    
    @Inject
    Resources resources;
    
    @Inject
    Server[] servers;
    
    @Inject
    JdbcConnectionPool[] connPools;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the paramter names and the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        try {
            JDBCConnectionPoolManager jdbcConnMgr = new JDBCConnectionPoolManager();
            ResourceStatus rs = jdbcConnMgr.delete(servers, resources, connPools, 
                    cascade.toString(), jdbc_connection_pool_id);
            if (rs.getStatus() == ResourceStatus.SUCCESS) {
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);       
            } else {
                if (rs.getMessage() != null) report.setMessage(rs.getMessage());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                if (rs.getException()!= null) {
                    report.setFailureCause(rs.getException());
                    Logger.getLogger(DeleteJdbcConnectionPool.class.getName()).log(Level.SEVERE, 
                            "Something went wrong in delete-jdbc-connection-pool", rs.getException());
                }
            }
        } catch(Exception e) {
            Logger.getLogger(DeleteJdbcConnectionPool.class.getName()).log(Level.SEVERE, 
                    "Something went wrong in delete-jdbc-connection-pool", e);
            String msg = e.getMessage() != null ? e.getMessage() : 
                localStrings.getLocalString("delete.jdbc.connection.pool.fail", 
                    "{0} delete failed ", jdbc_connection_pool_id);
            report.setMessage(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }        
    }
}
