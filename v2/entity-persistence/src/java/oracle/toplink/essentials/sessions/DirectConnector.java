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
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedNewInstanceFromClass;

/**
 * <p>
 * <b>Purpose</b>:Use this Connector to build a java.sql.Connection by
 * directly instantiating the Driver, as opposed to using the DriverManager.
 *
 * @author Big Country
 * @since TOPLink/Java 2.1
 */
public class DirectConnector extends DefaultConnector {

    /** cache up the instantiated Driver to speed up reconnects */
    protected Driver cachedInstance;

    /**
     * PUBLIC:
     * Construct a Connector with default settings (Sun JDBC-ODBC bridge).
     * Although this does not really make sense for a "direct" Connector -
     * the Sun JdbcOdbcDriver works fine with the DriverManager.
     */
    public DirectConnector() {
        super();
    }

    /**
     * PUBLIC:
     * Construct a Connector with the specified settings.
     */
    public DirectConnector(String driverClassName, String driverURLHeader, String databaseURL) {
        super(driverClassName, driverURLHeader, databaseURL);
    }

    /**
     * INTERNAL:
     * Connect with the specified properties and return the Connection.
     * @return java.sql.Connection
     */
    public Connection connect(Properties properties) throws DatabaseException {
        try {
            return this.instantiateDriver(this.loadDriver()).connect(this.getConnectionString(), properties);
        } catch (SQLException exception) {
            throw DatabaseException.sqlException(exception);
        }
    }

    /**
     * INTERNAL:
     * Instantiate the Driver if necessary.
     * @return java.sql.Driver
     */
    protected Driver instantiateDriver(Class driverClass) throws DatabaseException {
        if (cachedInstance != null) {
            return cachedInstance;
        }

        try {
            if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                try {
                    cachedInstance = (Driver)AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(driverClass));
                } catch (PrivilegedActionException exception) {
                    Exception throwableException = exception.getException();
                    if (throwableException instanceof InstantiationException) {
                        throw DatabaseException.configurationErrorNewInstanceInstantiationException((InstantiationException)throwableException, driverClass);
                    } else {
                        throw DatabaseException.configurationErrorNewInstanceIllegalAccessException((IllegalAccessException)throwableException, driverClass);
                    }
                }
            } else {
                cachedInstance = (Driver)PrivilegedAccessHelper.newInstanceFromClass(driverClass);
            }
            return cachedInstance;
        } catch (InstantiationException ie) {
            throw DatabaseException.configurationErrorNewInstanceInstantiationException(ie, driverClass);
        } catch (IllegalAccessException iae) {
            throw DatabaseException.configurationErrorNewInstanceIllegalAccessException(iae, driverClass);
        }
    }
}
