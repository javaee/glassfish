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

import java.util.Properties;

import oracle.toplink.essentials.exceptions.DatabaseException;
import oracle.toplink.essentials.internal.databaseaccess.Accessor;
import oracle.toplink.essentials.internal.databaseaccess.DatabasePlatform;
import oracle.toplink.essentials.internal.databaseaccess.Platform;

/**
 * <p>
 * <b>Purpose</b>: Define the information required to connect to a TopLink session.
 * <p>
 * <b>Description</b>: This interface represents a generic concept of a login to be used
 * when connecting to a data-store.  It is independant of JDBC so that the TopLink
 * session interface can be used for JCA, XML, non-relational or three-tiered frameworks.
 * <p>
 * @see DatabaseLogin
 */
public interface Login {

    /**
     * PUBLIC:
     * All logins must take a user name and password.
     */
    String getPassword();

    /**
     * PUBLIC:
     * All logins must take a user name and password.
     */
    String getUserName();

    /**
     * PUBLIC:
     * All logins must take a user name and password.
     */
    void setPassword(String password);

    /**
     * PUBLIC:
     * All logins must take a user name and password.
     */
    void setUserName(String userName);

    /**
     * PUBLIC:
     * Return whether TopLink uses some externally managed connection pooling.
     */
    boolean shouldUseExternalConnectionPooling();

    /**
     * PUBLIC:
     * Return whether TopLink uses some externally managed transaction service such as JTS.
     */
    boolean shouldUseExternalTransactionController();

    /**
     * INTERNAL:
     * Return the database platform specific information.
     * This allows TopLink to configure certain advanced features for the database desired.
     * The platform also allows configuration of sequence information.
     * NOTE: this must only be used for relational specific usage and will not work for
     * non-relational datasources.
     */
    DatabasePlatform getPlatform();

    /**
     * PUBLIC:
     * Return the datasource platform specific information.
     * This allows TopLink to configure certain advanced features for the datasource desired.
     * The platform also allows configuration of sequence information.
     */
    Platform getDatasourcePlatform();

    /**
     * INTERNAL:
     * Set the database platform specific information.
     * This allows TopLink to configure certain advanced features for the database desired.
     * The platform also allows configuration of sequence information.
     */
    void setPlatform(Platform platform);

    /**
     * PUBLIC:
     * Set the database platform specific information.
     * This allows TopLink to configure certain advanced features for the database desired.
     * The platform also allows configuration of sequence information.
     */
    void setDatasourcePlatform(Platform platform);

    /**
     * INTERNAL:
     * Connect to the datasource, and return the driver level connection object.
     */
    Object connectToDatasource(Accessor accessor) throws DatabaseException;

    /**
     * INTERNAL:
     * Build the correct datasource Accessor for this login instance.
     */
    Accessor buildAccessor();

    /**
     * INTERNAL:
     * Clone the login.
     */
    Object clone();

    /**
     * PUBLIC:
     * Return the qualifier for the all of the tables.
     */
    public String getTableQualifier();

    /**
     * INTERNAL:
     * Used for cache isolation.
     */
    public boolean shouldAllowConcurrentReadWrite();

    /**
     * INTERNAL:
     * Used for Cache Isolation.  Causes TopLink to lock at the class level on
     * cache updates.
     */
    public boolean shouldSynchronizeWrites();
    
    /**
     * INTERNAL:
     * Used for Cache Isolation.  Causes TopLink to lock at the object level on
     * cache updates and cache access.
     */
    public boolean shouldSynchronizeObjectLevelReadWrite();
    
    /**
     * INTERNAL:
     * Used for Cache Isolation.  Causes TopLink to lock at the object level on
     * cache updates and cache access, based on database transaction.
     */
    public boolean shouldSynchronizeObjectLevelReadWriteDatabase();
    
    /**
     * INTERNAL:
     * Used for cache isolation.
     */
    public boolean shouldSynchronizedReadOnWrite();
    
    /**
     * PUBLIC:
     * The properties are additional, driver-specific, connection information
     * to be passed to the driver.<p>
     * NOTE: Do not set the password directly by getting the properties and
     * setting the "password" property directly. Use the method DatabaseLogin.setPassword(String).
     */
    public Object getProperty(String name);
    
    /**
     * PUBLIC:
     * The properties are additional, driver-specific, connection information
     * to be passed to the JDBC driver.
     */
    public void setProperties(Properties properties);

    /**
     * PUBLIC:
     * Some JDBC drivers require additional, driver-specific, properties.
     * Add the specified property to those to be passed to the JDBC driver.
     */
    public void setProperty(String propertyName, Object propertyValue);
}
