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

package com.sun.enterprise.connectors.service;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.enterprise.connectors.ConnectorConnectionPool;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.ConnectionPoolObjectsUtils;
import java.sql.DatabaseMetaData;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

/**
 * Jdbc admin service performs Jdbc related operations for administration.
 *
 * @author shalini
 */
public class JdbcAdminServiceImpl extends ConnectorService {

    private ConnectorConnectionPoolAdminServiceImpl ccPoolAdmService;
    
    /**
     * Default constructor
     */
    public JdbcAdminServiceImpl() {
        super();
        ccPoolAdmService = (ConnectorConnectionPoolAdminServiceImpl)
                ConnectorAdminServicesFactory.getService(ConnectorConstants.CCP);


    }

    /**
     * Get Validation class names list for the database vendor that the jdbc 
     * connection pool refers to. This is used for custom connection validation.
     * @param dbVendor
     * @return all validation class names.
     */        
    public Set<String> getValidationClassNames(String dbVendor) {
        SortedSet classNames = new TreeSet();
        if(dbVendor.equalsIgnoreCase("DERBY")) {
            classNames.add("org.glassfish.api.jdbc.validation.DerbyConnectionValidation");
        } else if(dbVendor.equalsIgnoreCase("MYSQL")) {
            classNames.add("org.glassfish.api.jdbc.validation.MySQLConnectionValidation");
        } else if(dbVendor.equalsIgnoreCase("ORACLE")) {
            classNames.add("org.glassfish.api.jdbc.validation.OracleConnectionValidation");
        } else if(dbVendor.equalsIgnoreCase("POSTGRES")) {
            classNames.add("org.glassfish.api.jdbc.validation.PostgresConnectionValidation");
        }
        return classNames;
    }

    /**
     * Get Validation table names list for the database that the jdbc 
     * connection pool refers to. This is used for connection validation.
     * @param poolName
     * @return all validation table names.
     * @throws javax.resource.ResourceException
     * @throws javax.naming.NamingException
     */
    public Set<String> getValidationTableNames(String poolName)
            throws ResourceException {
        ManagedConnectionFactory mcf = ccPoolAdmService.getManagedConnectionFactory(poolName);
        final Subject defaultSubject = ccPoolAdmService.getDefaultSubject(poolName, mcf, null);
        ManagedConnection mc = null;
        java.sql.Connection con = null;
        try {
            mc = ccPoolAdmService.getManagedConnection(mcf, defaultSubject, null);

            if (mc != null) {
                con = (java.sql.Connection) mc.getConnection(defaultSubject, null);
            }
            return getValidationTableNames(con,
                    getDefaultDatabaseName(poolName, mcf));
        } catch(Exception re) {
            _logger.log(Level.WARNING, "pool.get_validation_table_names_failure", re.getMessage());
            throw new ResourceException(re);
        } finally {
            try {
                if(mc != null) {
                    mc.destroy();
                }
            } catch(Exception ex) {
                _logger.log(Level.FINEST, "pool.get_validation_table_names_mc_destroy", poolName);
            }
        }
    }

    /**
     * Returns a databaseName that is populated in pool's default DATABASENAME
     * @param poolName
     * @param mcf
     * @return
     * @throws javax.naming.NamingException if poolName lookup fails
     */
    private String getDefaultDatabaseName(String poolName, ManagedConnectionFactory mcf) 
            throws NamingException {
        // All this to get the default user name and principal
        String databaseName = null;
        ConnectorConnectionPool connectorConnectionPool = null;
        try {
            String jndiNameForPool = ConnectorAdminServiceUtils.getReservePrefixedJNDINameForPool(poolName);
            Context ic = _runtime.getNamingManager().getInitialContext();
            connectorConnectionPool = (ConnectorConnectionPool) ic.lookup(jndiNameForPool);
        } catch (NamingException ne) {
            throw ne;
        }

        databaseName = ccPoolAdmService.getPropertyValue("DATABASENAME", connectorConnectionPool);

        // To avoid using "" as the default databasename, try to get
        // the databasename from MCF. 
        if (databaseName == null || databaseName.trim().equals("")) {
            databaseName = ConnectionPoolObjectsUtils.getValueFromMCF("DatabaseName", poolName, mcf);
        }
        return databaseName;
    }    

    /**
     * Get Validation table names list for the catalog that the jdbc 
     * connection pool refers to. This is used for connection validation.
     * @param con
     * @param catalog database name used.
     * @return 
     * @throws javax.resource.ResourceException
     */
    public static Set<String> getValidationTableNames(java.sql.Connection con, String catalog) 
            throws ResourceException {

        
        SortedSet<String> tableNames = new TreeSet();
        if(catalog.trim().equals("")) {
            catalog = null;
        }
        
        if (con != null) {
            java.sql.ResultSet rs = null;
            try {
                DatabaseMetaData dmd = con.getMetaData();
                rs = dmd.getTables(catalog, null, null, null);
                while(rs.next()) {
                    String tableName = rs.getString(3);
                    tableNames.add(tableName);
                }
            } catch (Exception sqle) {
                _logger.log(Level.INFO, "pool.get_validation_table_names");
                throw new ResourceException(sqle);
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                } catch (Exception e1) {}
            }
        } else {
            throw new ResourceException("The connection is not valid as "
                    + "the connection is null");            
        }
        return tableNames;
    }
    
    /**
     * Utility method to check if the retrieved table is accessible, since this
     * will be used for connection validation method "table".
     * @param tableName
     * @param con
     * @return accessibility status of the table.
     */
    private static boolean isPingable(String tableName, java.sql.Connection con) {
        java.sql.Statement stmt = null;
        java.sql.ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
        } catch (Exception sqle) {
            _logger.log(Level.INFO, "pool.exc_is_pingable", tableName);
            return false;
            
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e1) {
            }

            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e2) {
            }
        }
        return true;
    }
}
