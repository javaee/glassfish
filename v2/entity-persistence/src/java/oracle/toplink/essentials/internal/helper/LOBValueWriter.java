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
package oracle.toplink.essentials.internal.helper;

import java.sql.*;
import java.util.*;
import oracle.toplink.essentials.internal.expressions.SQLSelectStatement;
import oracle.toplink.essentials.internal.expressions.ForUpdateClause;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.databaseaccess.DatabaseCall;
import oracle.toplink.essentials.internal.databaseaccess.Accessor;

/**
 * INTERNAL:
 * <p><b>Purpose</b>:LOBValueWriter is used to write a large size of object into an Oracle
 * CLOB/BLOB column through Oracle LOB Locator. It's a work-around object for the well-known 4k write
 * limits on an Oracle thin driver.
 *
 * <p><b>Responsibilities</b>:<ul>
 * <li> Build the Oracle empty lob method call string for the insert call.
 * <li> Build the minimial SELECT call to retrieve the locator.
 * <li> Write the lob value through the locator.
 * <li> Resolve the multiple table INSERT/SELECT orders.
 * <li> Resolve the nested unit of work commit issue.
 * </ul>
 *
 * @author: King Wang
 * @since TopLink/Java 5.0. July 2002.
 */
public class LOBValueWriter {
    //DatabaseCalls still to be processed
    private Collection calls = null;
    private Accessor accessor;

    /**
     * This is the default constructor for the class.
     *
     * Bug 2804663 - Each DatabaseAccessor will now hold on to its own instance
     * of this class, hence a singleton pattern is not applicable.
     */
    public LOBValueWriter(Accessor accessor) {
        this.accessor = accessor;
    }

    protected void buildAndExecuteCall(DatabaseCall dbCall, AbstractSession session) {
        DatabaseQuery query = dbCall.getQuery();
        if (!query.isWriteObjectQuery()) {
            //if not writequery, should not go through the locator writing..
            return;
        }
        WriteObjectQuery writeQuery = (WriteObjectQuery)query;
        writeQuery.setAccessor(accessor);
        //build a select statement form the query
        SQLSelectStatement selectStatement = buildSelectStatementForLocator(writeQuery, dbCall, session);

        //then build a call from the statement
        DatabaseCall call = buildCallFromSelectStatementForLocator(selectStatement, writeQuery, dbCall, session);

        accessor.executeCall(call, call.getQuery().getTranslationRow(), session);
    }

    /**
    * Fetch the locator(s) from the result set and write LOB value to the table
    */
    public void fetchLocatorAndWriteValue(DatabaseCall dbCall, Object resultSet) throws SQLException {
        Enumeration enumFields = dbCall.getContexts().getFields().elements();
        Enumeration enumValues = dbCall.getContexts().getValues().elements();
        AbstractSession executionSession = dbCall.getQuery().getSession().getExecutionSession(dbCall.getQuery());
        while (enumFields.hasMoreElements()) {
            DatabaseField field = (DatabaseField)enumFields.nextElement();
            Object value = enumValues.nextElement();

            //write the value through the locator
            executionSession.getPlatform().writeLOB(field, value, (ResultSet)resultSet, executionSession);
        }
    }

    /**
    * Build the select statement for selecting the locator
    */
    private SQLSelectStatement buildSelectStatementForLocator(WriteObjectQuery writeQuery, DatabaseCall call, AbstractSession session) {
        SQLSelectStatement selectStatement = new SQLSelectStatement();
        Vector tables = writeQuery.getDescriptor().getTables();
        selectStatement.setTables(tables);
        //rather than get ALL fields from the descriptor, only use the LOB-related fields to build the minimal SELECT statement.
        selectStatement.setFields(call.getContexts().getFields());
        //the where clause setting here is sufficient if the object does not map to multiple tables.
        selectStatement.setWhereClause(writeQuery.getDescriptor().getObjectBuilder().buildPrimaryKeyExpressionFromObject(writeQuery.getObject(), session));
        //need pessimistic locking for the locator select
        selectStatement.setLockingClause(ForUpdateClause.newInstance(ObjectBuildingQuery.LOCK));

        if (tables.size() > 1) {
            //the primary key expression from the primary table
            Expression expression = selectStatement.getWhereClause();

            //additioanl join from the non-primary tables
            Expression additionalJoin = (Expression)writeQuery.getDescriptor().getQueryManager().getAdditionalJoinExpression();
            if (additionalJoin != null) {
                expression = expression.and(additionalJoin);
            }

            //where clause now contains extra joins across all tables
            selectStatement.setWhereClause(expression);
        }

        //normalize the statement at the end, such as assign alias to all tables, and build sorting statement
        selectStatement.normalize(session, writeQuery.getDescriptor());
        return selectStatement;
    }

    /**
    * Build the sql call from the select statement for selecting the locator
    */
    private DatabaseCall buildCallFromSelectStatementForLocator(SQLSelectStatement selectStatement, WriteObjectQuery writeQuery, DatabaseCall dbCall, AbstractSession session) {
        DatabaseCall call = selectStatement.buildCall(session);

        //the LOB context must be passed into the new call object
        call.setContexts(dbCall.getContexts());
        //need to explictly define one rwo return, otherwise, TL assumes multiple rows return and confuses the accessor
        call.returnOneRow();
        //the query object has to be set in order to access to the platform and login objects
        call.setQuery(writeQuery);
        // prepare it
        call.prepare(session);
        //finally do the translation
        call.translate(writeQuery.getTranslationRow(), writeQuery.getModifyRow(), session);
        return call;
    }

    // Building of SELECT statements is no longer done in DatabaseAccessor.basicExecuteCall
    // for updates because DatabaseCall.isUpdateCall() can't recognize update in case
    // StoredProcedureCall is used. Therefore in all cases: insert(single or multiple tables) 
    // and update the original (insert and update) calls are saved
    // and both building and executing of SELECT statements postponed until
    // buildAndExecuteSelectCalls method is called.

    /**
    * Add original (insert or update) call to the collection
    */
    public void addCall(Call call) {
        if (calls == null) {
            //use lazy initialization
            calls = new ArrayList(2);
        }
        calls.add(call);
    }

    // Bug 3110860: RETURNINGPOLICY-OBTAINED PK CAUSES LOB TO BE INSERTED INCORRECTLY
    // The deferred locator SELECT calls should be generated and executed after ReturningPolicy
    // merges PK obtained from the db into the object held by the query.
    // That's why original (insert or update) calls are saved,
    // and both building and executing of SELECT statements postponed until
    // this method is called.

    /**
    * Build and execute the deferred select calls.
    */
    public void buildAndExecuteSelectCalls(AbstractSession session) {
        if ((calls == null) || calls.isEmpty()) {
            //no deferred select calls (it means no locator is required)
            return;
        }

        //all INSERTs have been executed, time to execute the SELECTs
        try {
            for (Iterator callIt = calls.iterator(); callIt.hasNext();) {
                DatabaseCall dbCall = (DatabaseCall)callIt.next();
                buildAndExecuteCall(dbCall, session);
            }
        } finally {
            //after executing all select calls, need to empty the collection.
            //this is neccessary in the nested unit of work cases.
            calls.clear();
        }
    }
}
