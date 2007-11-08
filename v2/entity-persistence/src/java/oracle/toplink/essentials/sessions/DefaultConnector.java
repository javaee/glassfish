/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.sessions;

import java.util.*;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.sql.*;
import java.io.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.localization.*;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedClassForName;

/**
 * <p>
 * <b>Purpose</b>:Use this Connector to build a java.sql.Connection in the
 * "standard" fashion, via the DriverManager.
 *
 * @author Big Country
 * @since TOPLink/Java 2.1
 */
public class DefaultConnector implements Connector {
    protected String driverClassName;
    protected String driverURLHeader;
    protected String databaseURL;

    /**
     * PUBLIC:
     * Construct a Connector with default settings (Sun JDBC-ODBC bridge).
     * The database URL will still need to be set.
     */
    public DefaultConnector() {
        this("sun.jdbc.odbc.JdbcOdbcDriver", "jdbc:odbc:", "");
    }

    /**
     * PUBLIC:
     * Construct a Connector with the specified settings.
     */
    public DefaultConnector(String driverClassName, String driverURLHeader, String databaseURL) {
        this.initialize(driverClassName, driverURLHeader, databaseURL);
    }

    /**
     * INTERNAL:
     * Clone the connector.
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception exception) {
            throw new InternalError("Clone failed");
        }
    }

    /**
     * INTERNAL:
     * Connect with the specified properties and return the Connection.
     * @return java.sql.Connection
     */
    public Connection connect(Properties properties) throws DatabaseException {
        this.loadDriver();// ensure the driver has been loaded and registered
        try {
            return DriverManager.getConnection(this.getConnectionString(), properties);
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception);
        }
    }

    /**
     * PUBLIC:
     * Return the JDBC connection string.
     * This is a combination of the driver-specific URL header and the database URL.
     */
    public String getConnectionString() {
        return this.getDriverURLHeader() + this.getDatabaseURL();
    }

    /**
     * PUBLIC:
     * Provide the details of my connection information. This is primarily for JMX runtime services.
     * @return java.lang.String
     */
    public String getConnectionDetails() {
        return this.getConnectionString();
    }

    /**
     * PUBLIC:
     * The database URL is the JDBC URL for the database server.
     * The driver header is <i>not</i> be included in this URL
     * (e.g. "dbase files"; not "jdbc:odbc:dbase files").
     */
    public String getDatabaseURL() {
        return databaseURL;
    }

    /**
     * PUBLIC:
     * The driver class is the name of the Java class for the JDBC driver being used
     * (e.g. "sun.jdbc.odbc.JdbcOdbcDriver").
     */
    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * PUBLIC:
     * The driver URL header is the string predetermined by the JDBC driver to be
     * part of the URL connection string, (e.g. "jdbc:odbc:").
     * This is required to connect to the database.
     */
    public String getDriverURLHeader() {
        return driverURLHeader;
    }

    /**
     * INTERNAL:
     * Initialize the connector with the specified settings.
     */
    protected void initialize(String driverClassName, String driverURLHeader, String databaseURL) {
        this.setDriverClassName(driverClassName);
        this.setDriverURLHeader(driverURLHeader);
        this.setDatabaseURL(databaseURL);
    }

    /**
     * INTERNAL:
     * Ensure that the driver has been loaded and registered with the
     * DriverManager. Just loading the class should cause the static
     * initialization code to do the necessary registration.
     * Return the loaded driver Class.
     */
    protected Class loadDriver() throws DatabaseException {
        // CR#... The correct class loader must be used to load the class,
        // not that Class.forName must be used to initialize the class a simple loadClass may not.
        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    return (Class)AccessController.doPrivileged(new PrivilegedClassForName(this.getDriverClassName(), true, ConversionManager.getDefaultManager().getLoader()));
                } catch (PrivilegedActionException exception) {
                    throw DatabaseException.configurationErrorClassNotFound(this.getDriverClassName());                }
            } else {
                return oracle.toplink.essentials.internal.security.PrivilegedAccessHelper.getClassForName(this.getDriverClassName(), true, ConversionManager.getDefaultManager().getLoader());
            }
        } catch (ClassNotFoundException exception) {
            throw DatabaseException.configurationErrorClassNotFound(this.getDriverClassName());
        }
    }

    /**
     * PUBLIC:
     * The database URL is the JDBC URL for the database server.
     * The driver header is <i>not</i> be included in this URL
     * (e.g. "dbase files"; not "jdbc:odbc:dbase files").
     */
    public void setDatabaseURL(String databaseURL) {
        this.databaseURL = databaseURL;
    }

    /**
     * PUBLIC:
     * The driver class is the name of the Java class for the JDBC driver being used
     * (e.g. "sun.jdbc.odbc.JdbcOdbcDriver").
     */
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    /**
     * PUBLIC:
     * The driver URL header is the string predetermined by the JDBC driver to be
     * part of the URL connection string, (e.g. "jdbc:odbc:").
     * This is required to connect to the database.
     */
    public void setDriverURLHeader(String driverURLHeader) {
        this.driverURLHeader = driverURLHeader;
    }

    /**
     * PUBLIC:
     * Print connection string.
     */
    public String toString() {
        return oracle.toplink.essentials.internal.helper.Helper.getShortClassName(getClass()) + "(" + getConnectionString() + ")";
    }

    /**
     * INTERNAL:
     * Print something useful on the log.
     */
    public void toString(PrintWriter writer) {
        writer.println(ToStringLocalization.buildMessage("datasource_URL", (Object[])null) + "=> \"" + this.getConnectionString() + "\"");
    }
}
