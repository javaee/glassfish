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
package oracle.toplink.essentials.jndi;

import java.util.*;
import java.sql.*;
import javax.naming.*;
import javax.sql.*;
import oracle.toplink.essentials.sessions.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.localization.*;

/**
 * Specifies the J2EE DataSource lookup options.
 * This connector is normally used with a login in a J2EE environment
 * to connect to a server's connection pool defined by the DataSource name.
 * The JNDI name that the DataSource is registered under must be specified,
 * this must include any required prefix such as "java:comp/env/", (unless a DataSource object is given).
 * A Context is only required if not running on the server, otheriwse default to a new InitialContext().
 * @author Big Country
 * @since TOPLink/Java 2.1
 */
public class JNDIConnector implements Connector {
    protected DataSource dataSource;
    protected Context context;
    protected String name;
    public static final int STRING_LOOKUP = 1;
    public static final int COMPOSITE_NAME_LOOKUP = 2;
    public static final int COMPOUND_NAME_LOOKUP = 3;
		//default setting is composite name to be consistent with previous TopLink versions
    protected int lookupType = COMPOSITE_NAME_LOOKUP;

    /**
     * PUBLIC:
     * Construct a Connector with no settings.
     * The datasource name will still need to be set.
     */
    public JNDIConnector() {
        super();
    }

    /**
     * PUBLIC:
     * Construct a Connector with the datasource name.
     */
    public JNDIConnector(Context context, String name) throws ValidationException {
        this(name);
        this.context = context;
    }

    /**
     * PUBLIC:
     * Construct a Connector with the datasource name.
     */
    public JNDIConnector(String name) {
        this.name = name;
    }

    /**
     * PUBLIC:
     * Construct a Connector with the datasource object.
     */
    public JNDIConnector(DataSource dataSource) {
        this.dataSource = dataSource;
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
     */
    public Connection connect(Properties properties) throws DatabaseException, ValidationException {
        String user = properties.getProperty("user");
        String password = properties.getProperty("password");

        DataSource dataSource = getDataSource();
        if (dataSource == null) {
            try {
                //bug#2761428 and 4405389 JBoss needs to look up datasources based on a string not a composite or compound name
                if (lookupType == STRING_LOOKUP) {
                    dataSource = (DataSource)getContext().lookup(getName());
                } else if (lookupType == COMPOSITE_NAME_LOOKUP) {
                    dataSource = (DataSource)getContext().lookup(new CompositeName(name));
                } else {
                    dataSource = (DataSource)getContext().lookup(new CompoundName(name, new Properties()));
                }
                this.setDataSource(dataSource);
            } catch (NamingException exception) {
                throw ValidationException.cannotAcquireDataSource(getName(), exception);
            }
        }

        try {
            // WebLogic connection pools do not require a user name and password.
            // JDBCLogin usually initializes these values with an empty string.
            // WebLogic data source does not support the getConnection() call with arguments
            // it only supports the zero argument call. DM 26/07/2000
            if ((user == null) || (user.equalsIgnoreCase(""))) {
                return dataSource.getConnection();
            } else {
                return dataSource.getConnection(user, password);
            }
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception);
        }
    }

    /**
     * PUBLIC:
     * Return the JNDI Context that can supplied the named DataSource.
     */
    public Context getContext() {
        if (context == null) {
            try {
                context = new InitialContext();
            } catch (NamingException exception) {
            }
        }
        return context;
    }

    /**
     * PUBLIC:
     * Return the javax.sql.DataSource.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * PUBLIC:
     * Return the name of the DataSource within the
     * JNDI Context.
     */
    public String getName() {
        return name;
    }

    /**
     * PUBLIC:
     * Provide the details of my connection information. This is primarily for JMX runtime services.
     * @return java.lang.String
     */
    public String getConnectionDetails() {
        return getName();
    }

    /**
     * PUBLIC:
     * Set the JNDI Context that can supply the named DataSource.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * PUBLIC:
     * Set the javax.sql.DataSource.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * PUBLIC:
     * Set the name of the DataSource within the
     * JNDI Context.
     */
    public void setName(String name) throws ValidationException {
        this.name = name;
    }

    public void setLookupType(int lookupType) {
        this.lookupType = lookupType;
    }

    public int getLookupType() {
        return lookupType;
    }

    /**
     * PUBLIC:
     * Print data source info.
     */
    public String toString() {
        return Helper.getShortClassName(getClass()) + ToStringLocalization.buildMessage("datasource_name", (Object[])null) + "=>" + getName();
    }

    /**
     * INTERNAL:
     * Print something useful on the log.
     */
    public void toString(java.io.PrintWriter writer) {
        writer.print(ToStringLocalization.buildMessage("connector", (Object[])null) + "=>" + Helper.getShortClassName(getClass()));
        writer.print(" ");
        writer.println(ToStringLocalization.buildMessage("datasource_name", (Object[])null) + "=>" + getName());
    }
}
