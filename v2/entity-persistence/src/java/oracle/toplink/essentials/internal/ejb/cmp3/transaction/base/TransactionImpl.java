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
package oracle.toplink.essentials.internal.ejb.cmp3.transaction.base;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.util.Vector;
import java.sql.*;
import javax.transaction.xa.XAResource;
import javax.transaction.*;
import oracle.toplink.essentials.exceptions.TransactionException;
import oracle.toplink.essentials.internal.ejb.cmp3.base.ExceptionFactory;
import oracle.toplink.essentials.internal.ejb.cmp3.jdbc.base.ConnectionProxyHandler;
import oracle.toplink.essentials.internal.ejb.cmp3.jdbc.base.DataSourceImpl;

/**
 * Implementation of JTA Transaction class. The guts of the tx logic
 * is contained in this class.
 *
 * Currently support is limited to enlisting only a single tx data source
 */
public class TransactionImpl implements Transaction {
    // Set by client-induced rollback marking
    boolean markedForRollback;

    // Used to maintain the tx status 
    int status;

    // Collection of Synchronization listeners
    Vector listeners;

    // The transactional connection we use
    Connection connection;
    static Class proxyClass = Proxy.getProxyClass(Connection.class.getClassLoader(), new Class[] { Connection.class });

    // The enlisted data source
    DataSourceImpl dataSource;

    /***** Static constants *****/

    // Cribbed from java.transaction.Status
    public static final int STATUS_ACTIVE = 0;
    public static final int STATUS_MARKED_ROLLBACK = 1;
    public static final int STATUS_PREPARED = 2;
    public static final int STATUS_COMMITTED = 3;
    public static final int STATUS_ROLLEDBACK = 4;
    public static final int STATUS_UNKNOWN = 5;
    public static final int STATUS_NO_TRANSACTION = 6;
    public static final int STATUS_PREPARING = 7;
    public static final int STATUS_COMMITTING = 8;
    public static final int STATUS_ROLLING_BACK = 9;

    // Set this to true for debugging of afterCompletion exceptions
    public static boolean DUMP_AFTER_COMPLETION_ERRORS = true;

    /************************/
    /***** Internal API *****/
    /************************/
    private void debug(String s) {
        System.out.println(s);
    }

    /*
     * Constructor invoked and new instance created on tx begin
     */
    public TransactionImpl() {
        markedForRollback = false;
        status = STATUS_ACTIVE;
        listeners = new Vector();
    }

    /*
     * Lazily allocate the connection. This will be used
     * by the data source if in a transaction.
     */
    public Connection getConnection(DataSourceImpl ds, String user, String password) throws SQLException {
        // We don't have a datasource connection yet, so allocate one
        if (connection == null) {
            debug("TxImpl - allocating new connection");
            dataSource = ds;
            connection = ds.internalGetConnection(user, password);
            connection.setAutoCommit(false);
        } else {
            // We already have a connection. Make sure the data sources are the same.
            if (ds.getName() != dataSource.getName()) {
                throw TransactionException.multipleResourceException();
            }
        }

        //  return connection;
        // Allocate and return a proxy for the connection
        debug("TxImpl - creating connection proxy");
        Connection proxyConnection = null;
        try {
            InvocationHandler handler = new ConnectionProxyHandler(connection);
            proxyConnection = (Connection)proxyClass.getConstructor(new Class[] { InvocationHandler.class }).newInstance(new Object[] { handler });
        } catch (Exception ex) {
            throw TransactionException.internalProxyException(ex);
        }
        return proxyConnection;
    }

    /*
     * Invoke afterCompletion callbacks.
     * If DUMP_AFTER_COMPLETION_ERRORS flag is set then dump
     * the exceptions to System.out, otherwise swallow them.
     *
     * NOTE: In either case it will not affect the outcome
     * of the transaction.
     */
    public void invokeAfterCompletion() {
        // Call all of the afterCompletion callbacks
        debug("TxImpl - invoking afterCompletion");
        int i;
        int j;
        for (i = 0, j = listeners.size(); i < j; i++) {
            try {
                ((Synchronization)listeners.elementAt(i)).afterCompletion(status);
            } catch (Throwable t) {
                if (DUMP_AFTER_COMPLETION_ERRORS) {
                    t.printStackTrace(System.out);
                }
            }
        }
    }

    /*
     * Rollback the transaction on the connection.
     */
    public void rollbackConnection() throws SQLException {
        if (connection != null) {
            debug("TxImpl - rolling back connection");
            status = STATUS_ROLLING_BACK;
            connection.rollback();
            status = STATUS_ROLLEDBACK;
        }
    }

    /*
     * Commit the transaction on the connection.
     */
    public void commitConnection() throws SQLException {
        if (connection != null) {
            debug("TxImpl - committing connection");
            status = STATUS_COMMITTING;
            connection.commit();
            status = STATUS_COMMITTED;
        }
    }

    /*
     * Clean up after everything is over
     */
    public void cleanup() {
        debug("TxImpl - cleanup");
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ex) {
            }

            // Ignore
            connection = null;
        }
        status = STATUS_NO_TRANSACTION;
    }

    /*************************************/
    /***** Supported Transaction API *****/
    /*************************************/
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        Exception error = null;

        debug("TxImpl - commit");
        // Make sure we are allowed to proceed
        switch (status) {
        case STATUS_ACTIVE:// This is the normal case - do nothing
            break;
        case STATUS_MARKED_ROLLBACK: {
            // Tx was marked for rollback by the user, error
            error = new ExceptionFactory().txMarkedForRollbackException();
            break;
        }
        default:// Tx in some other state, error
            throw new ExceptionFactory().invalidStateException(status);
        }

        // Call beforeCompletion callback. 
        if (error == null) {
            try {
                debug("TxImpl - invoking beforeCompletion");
                int i;
                int j;
                for (i = 0, j = listeners.size(); i < j; i++) {
                    ((Synchronization)listeners.elementAt(i)).beforeCompletion();
                }
            } catch (Exception ex) {
                error = ex;
                status = STATUS_ROLLING_BACK;
                debug("TxImpl - error in beforeCompletion: " + ex);
            }
        }

        // Now if we didn't get any errors then commit the connection
        if ((error == null) && (status == STATUS_ACTIVE)) {
            try {
                commitConnection();
            } catch (Exception ex) {
                error = ex;
            }
        } else {
            try {
                rollbackConnection();
            } catch (Exception ex) {
                error = ex;
            }
        }

        // Whether we were successful or not, call afterCompletion and clean up
        invokeAfterCompletion();
        cleanup();

        // Throw any error that may have occurred at any point in the commit
        if (error != null) {
            throw new ExceptionFactory().newSystemException(error);
        }
    }

    public int getStatus() throws SystemException {
        return status;
    }

    public void registerSynchronization(Synchronization synchronization) throws RollbackException, IllegalStateException, SystemException {
        debug("TxImpl - registering sync listener: " + synchronization);
        listeners.add(synchronization);
    }

    public void rollback() throws IllegalStateException, SystemException {
        Exception error = null;

        debug("TxImpl - rollback");
        try {
            rollbackConnection();
        } catch (Exception ex) {
            error = ex;
        }

        // Call afterCompletion callback and clean up
        invokeAfterCompletion();
        cleanup();

        // Throw any error that may have occurred while rolling back
        if (error != null) {
            throw new ExceptionFactory().newSystemException(error);
        }
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        debug("TxImpl - setRollbackOnly");
        status = STATUS_MARKED_ROLLBACK;
    }

    /*****************************************/
    /***** NOT supported Transaction API *****/
    /*****************************************/
    public boolean enlistResource(XAResource xaresource) throws RollbackException, IllegalStateException, SystemException {
        return false;
    }

    public boolean delistResource(XAResource xaresource, int i) throws IllegalStateException, SystemException {
        return false;
    }
}
