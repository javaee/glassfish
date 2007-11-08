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
package oracle.toplink.essentials.internal.ejb.cmp3.jdbc.base;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import javax.sql.DataSource;
import oracle.toplink.essentials.internal.ejb.cmp3.transaction.base.TransactionManagerImpl;

/**
 * A stubbed out impl of DataSource that can be used for testing.
 *
 * Does not support multiple threads or multiple usernames/passwords.
 */
public class DataSourceImpl implements DataSource {
    String dsName;
    String url;
    String userName;
    String password;

    // When a transaction is active we need to get the right connection.
    // This should not be set (will be null) when the data source is non-JTA (non-tx).
    TransactionManagerImpl tm;

    /************************/
    /***** Internal API *****/
    /************************/
    private void debug(String s) {
        System.out.println(s);
    }

    /*
     * Use this constructor to create a new datasource
     */
    public DataSourceImpl(String dsName, String url, String userName, String password) {
        this.dsName = dsName;
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

    /*
     * Return the unique name of this data source
     */
    public String getName() {
        return this.dsName;
    }

    /*
     * This must be called right after initialization if data source is transactional.
     * Must not get set if data source is a non-transactional data source.
     */
    public void setTransactionManager(TransactionManagerImpl tm) {
        this.tm = tm;
    }

    /*
     * Get all connections from the DriverManager.
     */
    public Connection internalGetConnection(String userName, String password) throws SQLException {
        return DriverManager.getConnection(this.url, userName, password);
    }

    /*
     * Get all connections from the DriverManager.
     */
    public Connection internalGetConnection() throws SQLException {
        return internalGetConnection(this.userName, this.password);
    }

    /*
     * Return true if this data source is transactional, false if not
     */
    public boolean isTransactional() {
        return tm != null;
    }

    /************************************************************/
    /***** Supported DataSource  API *****/
    /************************************************************/

    /*
     * Forward to the other method.
     */
    public Connection getConnection() throws SQLException {
        return getConnection(this.userName, this.password);
    }

    /*
     * Go to the Transaction Manager to get a connection
     */
    public Connection getConnection(String userName, String password) throws SQLException {
        if (isTransactional() && tm.isTransactionActive()) {
            // This will actually eventually call back into this class, but allows
            // the connection to be cached in the transaction first
            return tm.getConnection(this, userName, password);
        } else {//{
            debug("Ds - Allocating new non-tx connection");
        }
        return internalGetConnection(userName, password);
    }

    /*
     * Forward to the DriverManager.
     */
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    /*
     * Forward to the DriverManager.
     */
    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
    }

    /*
     * Forward to the DriverManager.
     */
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    /*
     * Forward to the DriverManager.
     */
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

    /*
     * JDBC 4.0
     */
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException();
    }

    /*
     * JDBC 4.0
     */
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
