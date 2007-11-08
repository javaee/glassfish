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
package oracle.toplink.essentials.internal.databaseaccess;

import java.util.Vector;
import oracle.toplink.essentials.exceptions.DatabaseException;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.sessions.Login;
import oracle.toplink.essentials.queryframework.Call;

/**
 * INTERNAL:
 * Accessor defines the interface used primarily by the assorted
 * TopLink Sessions to interact with a data store. In "normal"
 * TopLink this data store is a relational database. But this interface
 * also allows developers using the TopLink SDK to develop Accessors
 * to other, non-relational, data stores.<p>
 *
 * Accessors must implement the following behavior: <ul>
 *    <li>connect to and disconnect from the data store
 *    <li>handle transaction contexts
 * <li>execute calls that read, insert, update, and delete data
 * <li>keep track of concurrently executing calls
 * <li>supply metadata about the data store
 * </ul>
 *
 * @see oracle.toplink.essentials.publicinterface.Session
 * @see Call
 *
 * @author Big Country
 * @since TOPLink/Java 3.0
 */
public interface Accessor extends Cloneable {

    /**
     * To be called after JTS transaction has been completed (committed or rolled back)
     */
    public void afterJTSTransaction();

    /**
     * Begin a transaction on the data store.
     */
    void beginTransaction(AbstractSession session) throws DatabaseException;

    /**
     * Return a clone of the accessor.
     */
    Object clone();

    /**
     * Close the accessor's connection.
     * This is used only for external connection pooling
     * when it is intended for the connection to be reconnected in the future.
     */
    void closeConnection();

    /**
     * Commit a transaction on the data store.
     */
    void commitTransaction(AbstractSession session) throws DatabaseException;

    /**
     * Connect to the data store using the configuration
     * information in the login.
     */
    void connect(Login login, AbstractSession session) throws DatabaseException;

    /**
     * Decrement the number of calls in progress.
     * Used for external pooling.
     */
    void decrementCallCount();

    /**
     * Disconnect from the data store.
     */
    void disconnect(AbstractSession session) throws DatabaseException;

    /**
     * Execute the call.
     * The actual behavior of the execution depends on the type of call.
     * The call may be parameterized where the arguments are in the translation row.
     * The row will be empty if there are no parameters.
     * @return a row, a collection of rows, a row count, or a cursor
     */
    Object executeCall(Call call, AbstractRecord translationRow, AbstractSession session) throws DatabaseException;

    /**
     * Execute any deferred select calls.  This method will generally be called
     * after one or more select calls have been collected in a LOBValueWriter (to be
     * executed after all insert calls are executed).
     * Bug 2804663.
     *
     * @see oracle.toplink.essentials.internal.helper.LOBValueWriter#buildAndExecuteCallForLocator(DatabaseCall,Session,Accessor)
     */
    void flushSelectCalls(AbstractSession session);

    /**
     * Return the number of calls currently in progress.
     * Used for load balancing and external pooling.
     */
    int getCallCount();

    /**
     * Return the column metadata for the specified
     * selection criteria.
     */
    Vector getColumnInfo(String catalog, String schema, String tableName, String columnName, AbstractSession session) throws DatabaseException;

    /**
     * Return the JDBC connection for relational accessors.
     * This will fail for non-relational accessors.
     */
    java.sql.Connection getConnection();

    /**
     * Return the driver level connection,
     * this will need to be cast to the implementation class for the data access type being used.
     */
    Object getDatasourceConnection();

    /**
     * Return the table metadata for the specified
     * selection criteria.
     */
    Vector getTableInfo(String catalog, String schema, String tableName, String[] types, AbstractSession session) throws DatabaseException;

    /**
     * Increment the number of calls in progress.
     * Used for external pooling.
     */
    void incrementCallCount(AbstractSession session);

    /**
     * Return whether the accessor is connected to the data store.
     */
    boolean isConnected();

    /**
     * Reconnect to the database. This can be used if the connection was
     * temporarily disconnected or if it timed out.
     */
    void reestablishConnection(AbstractSession session) throws DatabaseException;

    /**
     * Roll back a transaction on the data store.
     */
    void rollbackTransaction(AbstractSession session) throws DatabaseException;

    /**
     * Return whether the accessor uses an external
     * transaction controller (e.g. JTS).
     */
    boolean usesExternalTransactionController();

    /**
     * This method will be called after a series of writes have been issued to
     * mark where a particular set of writes has completed.  It will be called
     * from commitTransaction and may be called from writeChanges.   Its main
     * purpose is to ensure that the batched statements have been executed
     */
    public void writesCompleted(AbstractSession session);
}
