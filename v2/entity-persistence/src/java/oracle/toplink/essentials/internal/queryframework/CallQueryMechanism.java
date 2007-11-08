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
package oracle.toplink.essentials.internal.queryframework;

import java.util.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.internal.expressions.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>:
 * Mechanism used for custom SQL and stored procedure queries.
 * <p>
 * <p><b>Responsibilities</b>:
 * Executes the appropriate call.
 *
 * @author James Sutherland
 * @since TOPLink/Java 2.0
 */
public class CallQueryMechanism extends DatasourceCallQueryMechanism {

    /**
     * Initialize the state of the query
     * @param query - owner of mechanism
     */
    public CallQueryMechanism(DatabaseQuery query) {
        super(query);
    }

    /**
     * Initialize the state of the query
     * @param query - owner of mechanism
     * @param call - sql call
     */
    public CallQueryMechanism(DatabaseQuery query, DatabaseCall call) {
        super(query, call);
        call.setIsFieldMatchingRequired(true);
    }

    /**
     * Return the call.
     */
    public DatabaseCall getDatabaseCall() {
        return (DatabaseCall)call;
    }

    /**
     * INTERNAL:
     * This is different from 'prepareForExecution' in that this is called on the original query,
     * and the other is called on the copy of the query.
     * This query is copied for concurrency so this prepare can only setup things that
     * will apply to any future execution of this query.
     */
    public void prepareCall() throws QueryException {
        DatabaseQuery query = getQuery();
        AbstractSession executionSession = getSession().getExecutionSession(query);
        if (hasMultipleCalls()) {
            if(getQuery().shouldCloneCall()){
                //For glassFish bug2689, the call needs to be cloned when query asks to do so. 
                calls = ((Vector)getCalls().clone());
            }
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatabaseCall call = (DatabaseCall)callsEnum.nextElement();
                if (!query.shouldIgnoreBindAllParameters()) {
                    call.setUsesBinding(query.shouldBindAllParameters());
                }
                if (!query.shouldIgnoreCacheStatement()) {
                    call.setShouldCacheStatement(query.shouldCacheStatement());
                }
                if (query.isReadQuery()) {
                    ReadQuery readQuery = (ReadQuery)query;
                    call.setMaxRows(readQuery.getMaxRows());
                    if (readQuery.getFirstResult() != 0) {
                        call.setFirstResult(readQuery.getFirstResult());
                        call.setIsResultSetScrollable(true);
                        call.setResultSetType(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE);
                        call.setResultSetConcurrency(java.sql.ResultSet.CONCUR_READ_ONLY);
                    }
                }
                call.prepare(executionSession);
            }
        } else if (getCall() != null) {
            if(getQuery().shouldCloneCall()){
                //For glassFish bug2689, the call needs to be cloned when query asks to do so. 
                call = (DatabaseCall)getDatabaseCall().clone();
                setCall(call);
            } 
            DatabaseCall call = getDatabaseCall();
            if (!query.shouldIgnoreBindAllParameters()) {
                call.setUsesBinding(query.shouldBindAllParameters());
            }
            if (!query.shouldIgnoreCacheStatement()) {
                call.setShouldCacheStatement(query.shouldCacheStatement());
            }
            if (query.isReadQuery()) {
                ReadQuery readQuery = (ReadQuery)query;
                call.setMaxRows(readQuery.getMaxRows());
                if (readQuery.getFirstResult() != 0) {
                    call.setFirstResult(readQuery.getFirstResult());
                    call.setIsResultSetScrollable(true);
                    call.setResultSetType(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE);
                    call.setResultSetConcurrency(java.sql.ResultSet.CONCUR_READ_ONLY);
                }
            }
            call.prepare(executionSession);
        }
    }

    /**
     * Pre-build configure the SQL call.
     */
    public void prepareDeleteAll() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatabaseCall call = (DatabaseCall)callsEnum.nextElement();
                call.returnNothing();
                if (getQuery().getDescriptor().usesOptimisticLocking()) {
                    call.setHasOptimisticLock(true);
                }
            }
        } else {
            getCall().returnNothing();
            if (getQuery().getDescriptor().usesOptimisticLocking()) {
                getDatabaseCall().setHasOptimisticLock(true);
            }
        }

        prepareCall();
    }

    /**
     * Pre-build configure the SQL call.
     */
    public void prepareDeleteObject() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatabaseCall call = (DatabaseCall)callsEnum.nextElement();
                call.returnNothing();
                if (getQuery().getDescriptor().usesOptimisticLocking()) {
                    call.setHasOptimisticLock(true);
                }
            }
        } else {
            getCall().returnNothing();
            if (getQuery().getDescriptor().usesOptimisticLocking()) {
                getDatabaseCall().setHasOptimisticLock(true);
            }
        }
        prepareCall();
    }

    /**
     * Pre-build configure the SQL call.
     */
    public void prepareDoesExist(DatabaseField field) {
        getCall().returnOneRow();
        Vector fields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
        fields.addElement(field);
        getDatabaseCall().setFields(fields);
        prepareCall();
    }

    /**
     * Pre-build configure the SQL call.
     */
    public void prepareExecuteSelect() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatabaseCall databseCall = (DatabaseCall)callsEnum.nextElement();
                databseCall.returnManyRows();
                databseCall.setIsFieldMatchingRequired(isCallQueryMechanism());
            }
        } else {
            DatabaseCall call = getDatabaseCall();
            call.returnManyRows();
            call.setIsFieldMatchingRequired(isCallQueryMechanism());
        }
        prepareCall();
    }

    /**
     * Pre-build configure the SQL call.
     */
    public void prepareSelectAllRows() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatabaseCall call = (DatabaseCall)callsEnum.nextElement();
                call.returnManyRows();
                if (isCallQueryMechanism()) {
                    call.setIsFieldMatchingRequired(true);
                    // Set the fieldsincluding joined and partial fields and compute joined indexes,
                    // this requires and assume that the custom SQL returns the fields in the correct order.
                    call.setFields(((ObjectLevelReadQuery)getQuery()).getSelectionFields());
                    ((ObjectLevelReadQuery)getQuery()).getJoinedAttributeManager().computeJoiningMappingIndexes(true, getSession(), 0);
                }
            }
        } else {
            getCall().returnManyRows();
            if (isCallQueryMechanism()) {
                DatabaseCall call = getDatabaseCall();
                call.setIsFieldMatchingRequired(true);
                // Set the fieldsincluding joined and partial fields and compute joined indexes,
                // this requires and assume that the custom SQL returns the fields in the correct order.
                call.setFields(((ObjectLevelReadQuery)getQuery()).getSelectionFields());
                ((ObjectLevelReadQuery)getQuery()).getJoinedAttributeManager().computeJoiningMappingIndexes(true, getSession(), 0);
            }
        }
        prepareCall();
    }

    /**
     * Pre-build configure the SQL call.
     */
    public void prepareSelectOneRow() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatabaseCall call = (DatabaseCall)callsEnum.nextElement();
                call.returnOneRow();
                if (isCallQueryMechanism()) {
                    call.setIsFieldMatchingRequired(true);
                    // Set the fieldsincluding joined and partial fields and compute joined indexes,
                    // this requires and assume that the custom SQL returns the fields in the correct order.
                    call.setFields(((ObjectLevelReadQuery)getQuery()).getSelectionFields());
                    ((ObjectLevelReadQuery)getQuery()).getJoinedAttributeManager().computeJoiningMappingIndexes(true, getSession(), 0);
                }
            }
        } else {
            getCall().returnOneRow();
            if (isCallQueryMechanism()) {
                DatabaseCall call = getDatabaseCall();
                call.setIsFieldMatchingRequired(true);
                // Set the fieldsincluding joined and partial fields and compute joined indexes,
                // this requires and assume that the custom SQL returns the fields in the correct order.
                call.setFields(((ObjectLevelReadQuery)getQuery()).getSelectionFields());
                ((ObjectLevelReadQuery)getQuery()).getJoinedAttributeManager().computeJoiningMappingIndexes(true, getSession(), 0);
            }
        }
        prepareCall();
    }

    /**
     * Pre-build configure the SQL call.
     */
    public void prepareUpdateObject() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatabaseCall call = (DatabaseCall)callsEnum.nextElement();
                call.returnNothing();
                if (getQuery().getDescriptor().usesOptimisticLocking()) {
                    call.setHasOptimisticLock(true);
                }
            }
        } else if (getCall() != null) {
            getCall().returnNothing();
            if (getQuery().getDescriptor().usesOptimisticLocking()) {
                getDatabaseCall().setHasOptimisticLock(true);
            }
        }
        prepareCall();
    }

    /**
     * INTERNAL:
     * Configure the call to be a dynamic custom SQL call, so that it ignore the # token.
     */
    public void setCallHasCustomSQLArguments() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatabaseCall databseCall = (DatabaseCall)callsEnum.nextElement();
                if (databseCall.isSQLCall()) {
                    ((SQLCall)databseCall).setHasCustomSQLArguments(true);
                }
            }
        } else if (getCall().isSQLCall()) {
            ((SQLCall)getCall()).setHasCustomSQLArguments(true);
        }
    }

    /**
     * Update the foreign key fields when resolving a bi-directonal reference in a UOW.
     * This must always be dynamic as it is called within an insert or delete query and 
     * is really part of the write operation and does not fire update events or worry about locking.
     */
    protected void updateForeignKeyFieldShallow(WriteObjectQuery writeQuery) {
        for (Enumeration tablesEnum = getDescriptor().getTables().elements();
                 tablesEnum.hasMoreElements();) {
            DatabaseTable table = (DatabaseTable)tablesEnum.nextElement();
            SQLUpdateStatement updateStatement = new SQLUpdateStatement();
            updateStatement.setModifyRow(getModifyRow());
            updateStatement.setTranslationRow(getTranslationRow());
            updateStatement.setTable(table);
            updateStatement.setWhereClause(getDescriptor().getObjectBuilder().buildPrimaryKeyExpression(table));// Must not check version, ok as just inserted it.
            // Bug 2996585
            StatementQueryMechanism updateMechanism = new StatementQueryMechanism(writeQuery, updateStatement);
            writeQuery.setModifyRow(updateStatement.getModifyRow());
            updateMechanism.updateObject();

        }
    }
}
