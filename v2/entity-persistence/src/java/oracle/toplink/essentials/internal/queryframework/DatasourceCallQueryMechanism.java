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
import oracle.toplink.essentials.internal.databaseaccess.DatasourceCall;
import oracle.toplink.essentials.internal.databaseaccess.DatabaseCall;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>:
 * Mechanism used for call queries.
 * <p>
 * <p><b>Responsibilities</b>:
 * Executes the appropriate call.
 *
 * @author James Sutherland
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public class DatasourceCallQueryMechanism extends DatabaseQueryMechanism {
    protected DatasourceCall call;

    /** Normally only a single call is used, however multiple table may require multiple calls on write. */
    protected Vector calls;

    /**
     * Initialize the state of the query
     * @param query - owner of mechanism
     */
    public DatasourceCallQueryMechanism(DatabaseQuery query) {
        super(query);
    }

    /**
     * Initialize the state of the query
     * @param query - owner of mechanism
     */
    public DatasourceCallQueryMechanism(DatabaseQuery query, DatasourceCall call) {
        super(query);
        this.call = call;
        call.setQuery(query);
    }

    /**
     * Add the call.
     */
    public void addCall(DatasourceCall call) {
        getCalls().addElement(call);
        call.setQuery(getQuery());
    }

    /**
     * Read all rows from the database using a cursored stream.
     * @exception  DatabaseException - an error has occurred on the database
     */
    public DatabaseCall cursorSelectAllRows() throws DatabaseException {
        try {
            return (DatabaseCall)executeCall();
        } catch (java.lang.ClassCastException e) {
            throw QueryException.mustUseCursorStreamPolicy();
        }
    }

    /**
     * INTERNAL:
     * Delete a collection of objects. Assume call is correct.
     * @exception  DatabaseException - an error has occurred on the database
     */
    public Integer deleteAll() throws DatabaseException {
        if(((DeleteAllQuery)getQuery()).isPreparedUsingTempStorage()) {
            return deleteAllUsingTempTables();
        } else {
            if (hasMultipleCalls()) {
                Integer returnedRowCount = null;
                
                // Deletion must occur in reverse order.
                for (int index = getCalls().size() - 1; index >= 0; index--) {
                    DatasourceCall databseCall = (DatasourceCall)getCalls().elementAt(index);
                    returnedRowCount = (Integer)executeCall(databseCall);
                }
                // returns the number of rows removed from the first table in insert order
                return returnedRowCount;
            } else {
                return (Integer)executeCall();
            }
        }
    }

    /**
     * Execute deleteAll using temp tables
     * @exception  DatabaseException - an error has occurred on the database.
     * @return the row count.
     */
    public Integer deleteAllUsingTempTables() throws DatabaseException {
        DatabaseException ex = null;
        Integer returnedRowCount = null;
        
        // Deletion must occur in reverse order.

        // first call - crete temp table.
        // may fail in case global temp table already exists.
        try {
            DatasourceCall databseCall = (DatasourceCall)getCalls().elementAt(getCalls().size() - 1);
            executeCall(databseCall);
        } catch (DatabaseException databaseEx) {
            // ignore
        }                

        // second call - populate temp table.
        // if that fails save the exception and untill cleanup
        if(ex == null) {
            try {
                DatasourceCall databseCall = (DatasourceCall)getCalls().elementAt(getCalls().size() - 2);
                executeCall(databseCall);
            } catch (DatabaseException databaseEx) {
                ex = databaseEx;
            }
        }
        
        // third (a call per table) - delete from original tables calls.
        // if that fails save the exception untill cleanup
        for (int index = getCalls().size() - 3; index >= 1 && ex == null; index--) {
            DatasourceCall databseCall = (DatasourceCall)getCalls().elementAt(index);
            try {
                // returns the number of rows removed from the first table in insert order
                returnedRowCount = (Integer)executeCall(databseCall);
            } catch (DatabaseException databaseEx) {
                ex = databaseEx;
            }
        }

        // last call - cleanup temp table.
        // ignore exceptions here.
        try {
            DatasourceCall databseCall = (DatasourceCall)getCalls().elementAt(0);
            executeCall(databseCall);
        } catch (DatabaseException databaseEx) {
            // ignore
        }

        if(ex != null) {
            throw ex;
        }
        
        return returnedRowCount;
    }

    /**
     * INTERNAL:
     * Delete an object.  Assume call is correct
     * @exception  DatabaseException - an error has occurred on the database
     */
    public Integer deleteObject() throws DatabaseException {
        if (hasMultipleCalls()) {
            Integer returnedRowCount = null;

            // Deletion must occur in reverse order.
            for (int index = getCalls().size() - 1; index >= 0; index--) {
                DatasourceCall databseCall = (DatasourceCall)getCalls().elementAt(index);
                Integer rowCount = (Integer)executeCall(databseCall);
                if ((index == (getCalls().size() - 1)) || (rowCount.intValue() <= 0)) {// Row count returned must be from first table or zero if any are zero.
                    returnedRowCount = rowCount;
                }
            }
            return returnedRowCount;
        } else {
            return (Integer)executeCall();
        }
    }

    /**
     * Execute the call.  It is assumed the call has been fully prepared.
     * @exception  DatabaseException - an error has occurred on the database.
     */
    protected Object executeCall() throws DatabaseException {
        return executeCall(getCall());
    }

    /**
     * Execute the call.  It is assumed the call has been fully prepared.
     * @exception  DatabaseException - an error has occurred on the database.
     */
    protected Object executeCall(DatasourceCall databaseCall) throws DatabaseException {
        // For CR 2923 must move to session we will execute call on now
        // so correct DatasourcePlatform used by translate. 
        AbstractSession sessionToUse = getSession().getExecutionSession(getQuery());
        DatasourceCall clonedCall = (DatasourceCall)databaseCall.clone();
        clonedCall.setQuery(getQuery());
        clonedCall.translate(getTranslationRow(), getModifyRow(), sessionToUse);
        return sessionToUse.executeCall(clonedCall, getTranslationRow(), getQuery());
    }

    /**
     * Execute a non selecting call.
     * @exception  DatabaseException - an error has occurred on the database.
     * @return the row count.
     */
    public Integer executeNoSelect() throws DatabaseException {
        return executeNoSelectCall();
    }

    /**
     * Execute a non selecting call.
     * @exception  DatabaseException - an error has occurred on the database.
     * @return the row count.
     */
    public Integer executeNoSelectCall() throws DatabaseException {
        if (hasMultipleCalls()) {
            Integer returnedRowCount = null;
            for (int index = 0; index < getCalls().size(); index++) {
                DatasourceCall databseCall = (DatasourceCall)getCalls().elementAt(index);
                Integer rowCount = (Integer)executeCall(databseCall);
                if ((index == 0) || (rowCount.intValue() <= 0)) {// Row count returned must be from first table or zero if any are zero.
                    returnedRowCount = rowCount;
                }
            }
            return returnedRowCount;
        } else {
            return (Integer)executeCall();
        }
    }

    /**
     * INTERNAL:
     * Execute a selecting call.
     * @exception  DatabaseException - an error has occurred on the database
     */
    public Vector executeSelect() throws DatabaseException {
        return executeSelectCall();
    }

    /**
     * INTERNAL:
     * Execute a selecting call.
     * @exception  DatabaseException - an error has occurred on the database
     */
    public Vector executeSelectCall() throws DatabaseException {
        if (hasMultipleCalls()) {
            Vector results = new Vector();
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatasourceCall databseCall = (DatasourceCall)callsEnum.nextElement();
                Helper.addAllToVector(results, (Vector)executeCall(databseCall));
            }

            return results;
        } else {
            return (Vector)executeCall();
        }
    }

    /**
     * Return the call.
     */
    public DatasourceCall getCall() {
        return call;
    }

    /**
     * Normally only a single call is used, however multiple table may require multiple calls on write.
     * This is lazy initialied to conserv space.
     */
    public Vector getCalls() {
        if (calls == null) {
            calls = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(3);
        }
        return calls;
    }

    /**
     * Normally only a single call is used, however multiple table may require multiple calls on write.
     * This is lazy initialied to conserv space.
     */
    public boolean hasMultipleCalls() {
        return (calls != null) && (!calls.isEmpty());
    }

    /**
     * Insert the object.  Assume the call is correct
     * @exception  DatabaseException - an error has occurred on the database
     */
    public void insertObject() throws DatabaseException {
        Class cls = ((DatabaseQuery)getQuery()).getReferenceClass();
        boolean usesSequencing = getDescriptor().usesSequenceNumbers();
        boolean shouldAcquireValueAfterInsert = false;
        if (usesSequencing) {
            shouldAcquireValueAfterInsert = getSession().getSequencing().shouldAcquireValueAfterInsert(cls);
        }
        Collection returnFields = null;

        // Check to see if sequence number should be retrieved after insert
        if (usesSequencing && !shouldAcquireValueAfterInsert) {
            // This is the normal case.  Update object with sequence number before insert.
            updateObjectAndRowWithSequenceNumber();
        }

        if (hasMultipleCalls()) {
            for (int index = 0; index < getCalls().size(); index++) {
                DatasourceCall databseCall = (DatasourceCall)getCalls().elementAt(index);
                executeCall(databseCall);
                if (returnFields != null) {
                    updateObjectAndRowWithReturnRow(returnFields, index == 0);
                }
                if ((index == 0) && usesSequencing && shouldAcquireValueAfterInsert) {
                    updateObjectAndRowWithSequenceNumber();
                }
            }
        } else {
            executeCall();
            if (returnFields != null) {
                updateObjectAndRowWithReturnRow(returnFields, true);
            }
            if (usesSequencing && shouldAcquireValueAfterInsert) {
                updateObjectAndRowWithSequenceNumber();
            }
        }

        // Bug 3110860: RETURNINGPOLICY-OBTAINED PK CAUSES LOB TO BE INSERTED INCORRECTLY
        // The deferred locator SELECT calls should be generated and executed after ReturningPolicy
        // merges PK obtained from the db into the object held by the query.
        //
        //Oracle thin driver handles LOB differently. During the insert, empty lob would be
        //insert first, and then the LOb locator is retrieved and LOB data are written through
        //the locator.
        // 
        // Bug 2804663 - LOBValueWriter is no longer a singleton, so we execute any deferred
        // select calls through the DatabaseAccessor which holds the writer instance
        AbstractSession executionSession = getSession().getExecutionSession(getQuery());
        executionSession.getAccessor().flushSelectCalls(executionSession);
    }

    /**
     * Return true if this is a call query mechanism
     */
    public boolean isCallQueryMechanism() {
        return true;
    }

    /**
     * INTERNAL:
     * This is different from 'prepareForExecution' in that this is called on the original query,
     * and the other is called on the copy of the query.
     * This query is copied for concurrency so this prepare can only setup things that
     * will apply to any future execution of this query.
     */
    public void prepare() {
        if ((!hasMultipleCalls()) && (getCall() == null)) {
            throw QueryException.sqlStatementNotSetProperly(getQuery());
        }
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
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatasourceCall call = (DatasourceCall)callsEnum.nextElement();
                call.prepare(executionSession);
            }
        } else if (getCall() != null) {
            getCall().prepare(executionSession);
        }
    }

    /**
     * Pre-build configure the call.
     */
    public void prepareCursorSelectAllRows() throws QueryException {
        getCall().returnCursor();
        prepareCall();
    }

    /**
     * Pre-build configure the call.
     */
    public void prepareDeleteAll() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatasourceCall call = (DatasourceCall)callsEnum.nextElement();
                call.returnNothing();
            }
        } else {
            getCall().returnNothing();
        }
        prepareCall();
    }

    /**
     * Pre-build configure the call.
     */
    public void prepareDeleteObject() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatasourceCall call = (DatasourceCall)callsEnum.nextElement();
                call.returnNothing();
            }
        } else {
            getCall().returnNothing();
        }
        prepareCall();
    }

    /**
     * Pre-build configure the call.
     */
    public void prepareDoesExist(DatabaseField field) {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                ((DatasourceCall)callsEnum.nextElement()).returnOneRow();
            }
        } else {
            getCall().returnOneRow();
        }
        prepareCall();
    }

    /**
     * Pre-build configure the call.
     */
    public void prepareExecuteNoSelect() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                ((DatasourceCall)callsEnum.nextElement()).returnNothing();
            }
        } else {
            getCall().returnNothing();
        }
        prepareCall();
    }

    /**
     * Pre-build configure the call.
     */
    public void prepareExecuteSelect() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatasourceCall databseCall = (DatasourceCall)callsEnum.nextElement();
                databseCall.returnManyRows();
            }
        } else {
            getCall().returnManyRows();
        }
        prepareCall();
    }

    /**
     * Pre-build configure the call.
     */
    public void prepareInsertObject() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                ((DatasourceCall)callsEnum.nextElement()).returnNothing();
            }
        } else {
            getCall().returnNothing();
        }
        prepareCall();
    }

    /**
     * Prepare the report items.  Indexes of results needs to be calculates
     */
    protected void prepareReportQueryItems(){
        //calculate indexes after normalize to insure expressions are set up correctly
        int itemOffset = 0;
        for (Iterator items = ((ReportQuery)getQuery()).getItems().iterator(); items.hasNext();){
            ReportItem item = (ReportItem) items.next();
            item.setResultIndex(itemOffset);
            if (item.getAttributeExpression() != null){
                JoinedAttributeManager joinManager = item.getJoinedAttributeManager();
                if (joinManager.hasJoinedExpressions()){
                    itemOffset = joinManager.computeJoiningMappingIndexes(true, getSession(),itemOffset);
                }else{
                    if (item.getDescriptor() != null){
                        itemOffset += item.getDescriptor().getAllFields().size();
                    }else {
                        ++itemOffset; //only a single attribute can be selected
                    }
                }
            }
        }
        
    }
    /**
     * Pre-build configure the call.
     */
    public void prepareReportQuerySelectAllRows() {
        prepareReportQueryItems();
        prepareExecuteSelect();
    }

    /**
     * Prepare for a sub select using a call.
     */
    public void prepareReportQuerySubSelect() {
        prepareReportQueryItems();
        prepareCall();
    }

    /**
     * Pre-build configure the call.
     */
    public void prepareSelectAllRows() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatasourceCall databseCall = (DatasourceCall)callsEnum.nextElement();
                databseCall.returnManyRows();
            }
        } else {
            getCall().returnManyRows();
        }
        prepareCall();
    }

    /**
     * Pre-build configure the call.
     */
    public void prepareSelectOneRow() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatasourceCall databseCall = (DatasourceCall)callsEnum.nextElement();
                databseCall.returnOneRow();
            }
        } else {
            getCall().returnOneRow();
        }
        prepareCall();
    }

    /**
     * Pre-build configure the call.
     */
    public void prepareUpdateObject() {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatasourceCall call = (DatasourceCall)callsEnum.nextElement();
                call.returnNothing();
            }
        } else if (getCall() != null) {
            getCall().returnNothing();
        }
        prepareCall();
    }

    /**
       * Pre-build configure the call.
       */
    public void prepareUpdateAll() {
        if (getCall() != null) {
            getCall().returnNothing();
        }

        prepareCall();
    }

    /**
     * Read all rows from the database. Assume call is correct returns the required fields.
     * @return Vector containing the database rows
     * @exception  DatabaseException - an error has occurred on the database
     */
    public Vector selectAllReportQueryRows() throws DatabaseException {
        return executeSelect();
    }

    /**
     * Read all rows from the database. Assume call is correct returns the required fields.
     * @return Vector containing the database rows
     * @exception  DatabaseException - an error has occurred on the database
     */
    public Vector selectAllRows() throws DatabaseException {
        return executeSelectCall();
    }

    /**
     * Read a single row from the database. Assume call is correct.
     * @return row containing data
     * @exception  DatabaseException - an error has occurred on the database
     */
    public AbstractRecord selectOneRow() throws DatabaseException {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatasourceCall databaseCall = (DatasourceCall)callsEnum.nextElement();
                AbstractRecord result = (AbstractRecord)executeCall(databaseCall);
                if (result != null) {
                    return result;
                }
            }

            return null;
        } else {
            return (AbstractRecord)executeCall();
        }
    }

    /**
     * Perform a does exist check
     * @param field - the field used for does exist check
     * @return  the associated row from the database
     * @exception  DatabaseException - an error has occurred on the database
     */
    public AbstractRecord selectRowForDoesExist(DatabaseField field) throws DatabaseException {
        if (hasMultipleCalls()) {
            for (Enumeration callsEnum = getCalls().elements(); callsEnum.hasMoreElements();) {
                DatasourceCall databaseCall = (DatasourceCall)callsEnum.nextElement();
                AbstractRecord result = (AbstractRecord)executeCall(databaseCall);
                if (result != null) {
                    return result;
                }
            }

            return null;
        } else {
            return (AbstractRecord)executeCall();
        }
    }

    /**
     * Set the call.
     */
    public void setCall(DatasourceCall call) {
        this.call = call;
        if (call != null) {
            call.setQuery(getQuery());
        }
    }

    /**
     * Normally only a single call is used, however multiple table may require multiple calls on write.
     * This is lazy initialied to conserv space.
     */
    protected void setCalls(Vector calls) {
        this.calls = calls;
    }

    /**
     * Update the object.  Assume the call is correct.
     * @exception  DatabaseException - an error has occurred on the database.
     * @return the row count.
     */
    public Integer updateObject() throws DatabaseException {
        Collection returnFields = null;
        Integer returnedRowCount = null;
        if (hasMultipleCalls()) {
            for (int index = 0; index < getCalls().size(); index++) {
                DatasourceCall databseCall = (DatasourceCall)getCalls().elementAt(index);
                Integer rowCount = (Integer)executeCall(databseCall);
                if ((index == 0) || (rowCount.intValue() <= 0)) {// Row count returned must be from first table or zero if any are zero.
                    returnedRowCount = rowCount;
                }
                if (returnFields != null) {
                    updateObjectAndRowWithReturnRow(returnFields, false);
                }
            }
        } else {
            returnedRowCount = (Integer)executeCall();
            if (returnFields != null) {
                updateObjectAndRowWithReturnRow(returnFields, false);
            }
        }

        //Oracle thin driver handles LOB differently. During the insert, empty lob would be
        //insert first, and then the LOb locator is retrieved and LOB data are written through
        //the locator.
        // 
        // Bug 2804663 - LOBValueWriter is no longer a singleton, so we execute any deferred
        // select calls through the DatabaseAccessor which holds the writer instance
        //
        // Building of SELECT statements is no longer done in DatabaseAccessor.basicExecuteCall
        // because DatabaseCall.isUpdateCall() can't recognize update in case StoredProcedureCall
        // is used.
        AbstractSession executionSession = getSession().getExecutionSession(getQuery());
        executionSession.getAccessor().flushSelectCalls(executionSession);
        return returnedRowCount;
    }

    /**
       * Update the rows on the database.  Assume the call is correct.
       * @exception  DatabaseException - an error has occurred on the database.
       */
    public Integer updateAll() throws DatabaseException {
        if(((UpdateAllQuery)getQuery()).isPreparedUsingTempStorage() && getSession().getPlatform().supportsTempTables()) {
            return updateAllUsingTempTables();
        } else {
            Integer rowCount = executeNoSelectCall();
            if(((UpdateAllQuery)getQuery()).isPreparedUsingTempStorage()) {
                // the query was prepared using Oracle anonymous block 
                AbstractRecord outputRow = (AbstractRecord)getQuery().getProperty("output");
                rowCount = (Integer)outputRow.get("ROW_COUNT");
            }
            return rowCount;
        }
    }

    /**
     * Execute updateAll using temp tables
     * @exception  DatabaseException - an error has occurred on the database.
     * @return the row count.
     */
    public Integer updateAllUsingTempTables() throws DatabaseException {
        int nTables = getCalls().size() / 4;
        DatabaseException ex = null;
        Integer returnedRowCount = null;
        
        // first quarter - crete temp tables calls.
        // may fail in case global temp table already exists.
        for (int index = 0; index < nTables; index++) {
            try {
                DatasourceCall databseCall = (DatasourceCall)getCalls().elementAt(index);
                executeCall(databseCall);
            } catch (DatabaseException databaseEx) {
                // ignore
            }
        }

        // second quarter - populate temp tables calls.
        // if that fails save the exception and untill cleanup
        for (int index = nTables; index < nTables*2 && ex == null; index++) {
            try {
                DatasourceCall databseCall = (DatasourceCall)getCalls().elementAt(index);
                executeCall(databseCall);
            } catch (DatabaseException databaseEx) {
                ex = databaseEx;
            }
        }
        
        // third quarter - update original tables calls.
        // if that fails save the exception and untill cleanup
        for (int index = nTables*2; index < nTables*3 && ex == null; index++) {
            try {
                DatasourceCall databseCall = (DatasourceCall)getCalls().elementAt(index);
                Integer rowCount = (Integer)executeCall(databseCall);
                if ((index == nTables*2) || (rowCount.intValue() <= 0)) {// Row count returned must be from first table or zero if any are zero.
                    returnedRowCount = rowCount;
                }
            } catch (DatabaseException databaseEx) {
                ex = databaseEx;
            }
        }
        
        // last quarter - cleanup temp tables calls.
        // ignore exceptions here.
        for (int index = nTables*3; index < nTables*4; index++) {
            try {
                DatasourceCall databseCall = (DatasourceCall)getCalls().elementAt(index);
                executeCall(databseCall);
                } catch (DatabaseException databaseEx) {
                    // ignore
                }
        }

        if(ex != null) {
            throw ex;
        }
        
        return returnedRowCount;
    }

    /**
     * Update the foreign key fields when resolving a bi-directonal reference in a UOW.
     * This is rare to occur for non-relational, however if it does each of the calls must be re-executed.
     */
    protected void updateForeignKeyFieldShallow(WriteObjectQuery writeQuery) {
        // For CR 2923 must move to session we will execute call on now
        // so correct DatasourcePlatform used by translate. 
        AbstractSession sessionToUse = getSession().getExecutionSession(getQuery());

        // yes - this is a bit ugly...
        Vector calls = ((DatasourceCallQueryMechanism)this.getDescriptor().getQueryManager().getUpdateQuery().getQueryMechanism()).getCalls();
        for (Enumeration stream = calls.elements(); stream.hasMoreElements();) {
            DatasourceCall call = (DatasourceCall)((DatasourceCall)stream.nextElement()).clone();
            call.setQuery(writeQuery);
            sessionToUse.executeCall(call, this.getTranslationRow(), writeQuery);
        }
    }
}
