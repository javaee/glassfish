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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




package org.apache.catalina.realm;


import java.io.File;
import java.security.MessageDigest;
import java.security.Principal;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Realm;
import org.apache.catalina.util.HexUtils;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.util.Base64;


/**
*
* Implmentation of <b>Realm</b> that works with any JDBC supported database.
* See the JDBCRealm.howto for more details on how to set up the database and
* for configuration options.
*
* <p><strong>TODO</strong> - Support connection pooling (including message
* format objects) so that <code>authenticate()</code> does not have to be
* synchronized.</p>
*
* @author Craig R. McClanahan
* @author Carson McDonald
* @author Ignacio Ortega
* @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:53 $
*/

public class JDBCRealm
    extends RealmBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The connection username to use when trying to connect to the database.
     */
    protected String connectionName = null;


    /**
     * The connection URL to use when trying to connect to the database.
     */
    protected String connectionPassword = null;


    /**
     * The connection URL to use when trying to connect to the database.
     */
    protected String connectionURL = null;


    /**
     * The connection to the database.
     */
    protected Connection dbConnection = null;


    /**
     * Instance of the JDBC Driver class we use as a connection factory.
     */
    protected Driver driver = null;


    /**
     * The JDBC driver to use.
     */
    protected String driverName = null;


    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String info =
        "org.apache.catalina.realm.JDBCRealm/1.0";


    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String name = "JDBCRealm";


    /**
     * The PreparedStatement to use for authenticating users.
     */
    protected PreparedStatement preparedCredentials = null;


    /**
     * The PreparedStatement to use for identifying the roles for
     * a specified user.
     */
    protected PreparedStatement preparedRoles = null;


    /**
     * The column in the user role table that names a role
     */
    protected String roleNameCol = null;


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * The column in the user table that holds the user's credintials
     */
    protected String userCredCol = null;


    /**
     * The column in the user table that holds the user's name
     */
    protected String userNameCol = null;


    /**
     * The table that holds the relation between user's and roles
     */
    protected String userRoleTable = null;


    /**
     * The table that holds user data.
     */
    protected String userTable = null;


    // ------------------------------------------------------------- Properties

    /**
     * Return the username to use to connect to the database.
     *
     */
    public String getConnectionName() {
        return connectionName;
    }

    /**
     * Set the username to use to connect to the database.
     *
     * @param connectionName Username
     */
    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    /**
     * Return the password to use to connect to the database.
     *
     */
    public String getConnectionPassword() {
        return connectionPassword;
    }

    /**
     * Set the password to use to connect to the database.
     *
     * @param connectionPassword User password
     */
    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }

    /**
     * Return the URL to use to connect to the database.
     *
     */
    public String getConnectionURL() {
        return connectionURL;
    }

    /**
     * Set the URL to use to connect to the database.
     *
     * @param connectionURL The new connection URL
     */
    public void setConnectionURL( String connectionURL ) {
      this.connectionURL = connectionURL;
    }

    /**
     * Return the JDBC driver that will be used.
     *
     */
    public String getDriverName() {
        return driverName;
    }

    /**
     * Set the JDBC driver that will be used.
     *
     * @param driverName The driver name
     */
    public void setDriverName( String driverName ) {
      this.driverName = driverName;
    }

    /**
     * Return the column in the user role table that names a role.
     *
     */
    public String getRoleNameCol() {
        return roleNameCol;
    }

    /**
     * Set the column in the user role table that names a role.
     *
     * @param roleNameCol The column name
     */
    public void setRoleNameCol( String roleNameCol ) {
        this.roleNameCol = roleNameCol;
    }

    /**
     * Return the column in the user table that holds the user's credentials.
     *
     */
    public String getUserCredCol() {
        return userCredCol;
    }

    /**
     * Set the column in the user table that holds the user's credentials.
     *
     * @param userCredCol The column name
     */
    public void setUserCredCol( String userCredCol ) {
       this.userCredCol = userCredCol;
    }

    /**
     * Return the column in the user table that holds the user's name.
     *
     */
    public String getUserNameCol() {
        return userNameCol;
    }

    /**
     * Set the column in the user table that holds the user's name.
     *
     * @param userNameCol The column name
     */
    public void setUserNameCol( String userNameCol ) {
       this.userNameCol = userNameCol;
    }

    /**
     * Return the table that holds the relation between user's and roles.
     *
     */
    public String getUserRoleTable() {
        return userRoleTable;
    }

    /**
     * Set the table that holds the relation between user's and roles.
     *
     * @param userRoleTable The table name
     */
    public void setUserRoleTable( String userRoleTable ) {
        this.userRoleTable = userRoleTable;
    }

    /**
     * Return the table that holds user data..
     *
     */
    public String getUserTable() {
        return userTable;
    }

    /**
     * Set the table that holds user data.
     *
     * @param userTable The table name
     */
    public void setUserTable( String userTable ) {
      this.userTable = userTable;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * If there are any errors with the JDBC connection, executing
     * the query or anything we return null (don't authenticate). This
     * event is also logged, and the connection will be closed so that
     * a subsequent request will automatically re-open it.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     */
    public Principal authenticate(String username, String credentials) {

        Connection dbConnection = null;

        try {

            // Ensure that we have an open database connection
            dbConnection = open();

            // Acquire a Principal object for this user
            Principal principal = authenticate(dbConnection,
                                               username, credentials);

            // Release the database connection we just used
            release(dbConnection);

            // Return the Principal (if any)
            return (principal);

        } catch (SQLException e) {

            // Log the problem for posterity
            log(sm.getString("jdbcRealm.exception"), e);

            // Close the connection so that it gets reopened next time
            if (dbConnection != null)
                close(dbConnection);

            // Return "not authenticated" for this request
            return (null);

        }

    }


    // -------------------------------------------------------- Package Methods


    // ------------------------------------------------------ Protected Methods


    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return <code>null</code>.
     *
     * @param dbConnection The database connection to be used
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *  authenticating this username
     *
     * @exception SQLException if a database error occurs
     */
    public synchronized Principal authenticate(Connection dbConnection,
                                               String username,
                                               String credentials)
        throws SQLException {

        // Look up the user's credentials
        String dbCredentials = null;
        PreparedStatement stmt = credentials(dbConnection, username);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            dbCredentials = rs.getString(1).trim();
        }
        rs.close();
        if (dbCredentials == null) {
            return (null);
        }

        // Validate the user's credentials
        boolean validated = false;
        if (hasMessageDigest()) {
            // Hex hashes should be compared case-insensitive
            validated = (digest(credentials).equalsIgnoreCase(dbCredentials));
        } else
            validated = (digest(credentials).equals(dbCredentials));

        if (validated) {
            if (debug >= 2)
                log(sm.getString("jdbcRealm.authenticateSuccess",
                                 username));
        } else {
            if (debug >= 2)
                log(sm.getString("jdbcRealm.authenticateFailure",
                                 username));
            return (null);
        }

        // Accumulate the user's roles
        ArrayList list = new ArrayList();
        stmt = roles(dbConnection, username);
        rs = stmt.executeQuery();
        while (rs.next()) {
            list.add(rs.getString(1).trim());
        }
        rs.close();
        dbConnection.commit();

        // Create and return a suitable Principal for this user
        return (new GenericPrincipal(this, username, credentials, list));

    }


    /**
     * Close the specified database connection.
     *
     * @param dbConnection The connection to be closed
     */
    protected void close(Connection dbConnection) {

        // Do nothing if the database connection is already closed
        if (dbConnection == null)
            return;

        // Close our prepared statements (if any)
        try {
            preparedCredentials.close();
        } catch (Throwable f) {
            ;
        }
        try {
            preparedRoles.close();
        } catch (Throwable f) {
            ;
        }

        // Close this database connection, and log any errors
        try {
            dbConnection.close();
        } catch (SQLException e) {
            log(sm.getString("jdbcRealm.close"), e); // Just log it here
        }

        // Release resources associated with the closed connection
        this.dbConnection = null;
        this.preparedCredentials = null;
        this.preparedRoles = null;

    }


    /**
     * Return a PreparedStatement configured to perform the SELECT required
     * to retrieve user credentials for the specified username.
     *
     * @param dbConnection The database connection to be used
     * @param username Username for which credentials should be retrieved
     *
     * @exception SQLException if a database error occurs
     */
    protected PreparedStatement credentials(Connection dbConnection,
                                            String username)
        throws SQLException {

        if (preparedCredentials == null) {
            StringBuffer sb = new StringBuffer("SELECT ");
            sb.append(userCredCol);
            sb.append(" FROM ");
            sb.append(userTable);
            sb.append(" WHERE ");
            sb.append(userNameCol);
            sb.append(" = ?");
            preparedCredentials =
                dbConnection.prepareStatement(sb.toString());
        }

        preparedCredentials.setString(1, username);
        return (preparedCredentials);

    }


    /**
     * Return a short name for this Realm implementation.
     */
    protected String getName() {

        return (this.name);

    }


    /**
     * Return the password associated with the given principal's user name.
     */
    protected String getPassword(String username) {

        return (null);

    }


    /**
     * Return the Principal associated with the given user name.
     */
    protected Principal getPrincipal(String username) {

        return (null);

    }


    /**
     * Open (if necessary) and return a database connection for use by
     * this Realm.
     *
     * @exception SQLException if a database error occurs
     */
    protected Connection open() throws SQLException {

        // Do nothing if there is a database connection already open
        if (dbConnection != null)
            return (dbConnection);

        // Instantiate our database driver if necessary
        if (driver == null) {
            try {
                Class clazz = Class.forName(driverName);
                driver = (Driver) clazz.newInstance();
            } catch (Throwable e) {
                throw new SQLException(e.getMessage());
            }
        }

        // Open a new connection
        Properties props = new Properties();
        if (connectionName != null)
            props.put("user", connectionName);
        if (connectionPassword != null)
            props.put("password", connectionPassword);
        dbConnection = driver.connect(connectionURL, props);
        dbConnection.setAutoCommit(false);
        return (dbConnection);

    }


    /**
     * Release our use of this connection so that it can be recycled.
     *
     * @param dbConnnection The connection to be released
     */
    protected void release(Connection dbConnection) {

        ; // NO-OP since we are not pooling anything

    }


    /**
     * Return a PreparedStatement configured to perform the SELECT required
     * to retrieve user roles for the specified username.
     *
     * @param dbConnection The database connection to be used
     * @param username Username for which roles should be retrieved
     *
     * @exception SQLException if a database error occurs
     */
    protected PreparedStatement roles(Connection dbConnection, String username)
        throws SQLException {

        if (preparedRoles == null) {
            StringBuffer sb = new StringBuffer("SELECT ");
            sb.append(roleNameCol);
            sb.append(" FROM ");
            sb.append(userRoleTable);
            sb.append(" WHERE ");
            sb.append(userNameCol);
            sb.append(" = ?");
            preparedRoles =
                dbConnection.prepareStatement(sb.toString());
        }

        preparedRoles.setString(1, username);
        return (preparedRoles);

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     *
     * Prepare for active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents it from being started
     */
    public void start() throws LifecycleException {

        // Validate that we can open our connection
        try {
            open();
        } catch (SQLException e) {
            throw new LifecycleException(sm.getString("jdbcRealm.open"), e);
        }

        // Perform normal superclass initialization
        super.start();

    }


    /**
     * Gracefully shut down active use of the public methods of this Component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

        // Perform normal superclass finalization
        super.stop();

        // Close any open DB connection
        close(this.dbConnection);

    }


}
