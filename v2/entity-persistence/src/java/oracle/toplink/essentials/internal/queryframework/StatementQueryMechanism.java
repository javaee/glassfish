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
import oracle.toplink.essentials.internal.expressions.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.sessions.SessionProfiler;

/**
 * <p><b>Purpose</b>:
 * Mechanism used for all  statement objects.
 * <p>
 * <p><b>Responsibilities</b>:
 * Executes the appropriate statement.
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public class StatementQueryMechanism extends CallQueryMechanism {
    protected SQLStatement sqlStatement;

    /** Normally only a single statement is used, however multiple table may require multiple statements on write. */
    protected Vector sqlStatements;

    /**
     * INTERNAL:
     * Return a new mechanism for the query
     * @param query - owner of mechanism
     */
    public StatementQueryMechanism(DatabaseQuery query) {
        super(query);
    }

    /**
     * Return a new mechanism for the query
     * @param query - owner of mechanism
     * @param statement - sql statement
     */
    public StatementQueryMechanism(DatabaseQuery query, SQLStatement statement) {
        super(query);
        this.sqlStatement = statement;
    }

    /**
     * The statement is no longer require after prepare so can be released.
     */
    public void clearStatement() {
        // Only clear the statement if it is an expression query, otherwise the statement may still be needed.
        if (isExpressionQueryMechanism()) {
            setSQLStatement(null);
            setSQLStatements(null);
        }
    }

    /**
     * Clone the mechanism for the specified query clone.
     */
    public DatabaseQueryMechanism clone(DatabaseQuery queryClone) {
        StatementQueryMechanism clone = (StatementQueryMechanism)super.clone(queryClone);
        if ((!hasMultipleStatements()) && (getSQLStatement() != null)) {
            clone.setSQLStatement((SQLStatement)sqlStatement.clone());
        } else {
            Vector currentStatements = getSQLStatements();
            if (currentStatements != null) {
                Vector statementClone = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(currentStatements.size());
                Enumeration enumtr = currentStatements.elements();
                while (enumtr.hasMoreElements()) {
                    statementClone.addElement(((SQLStatement)enumtr.nextElement()).clone());
                }
                clone.setSQLStatements(statementClone);
            }
        }
        return clone;
    }

    /**
     * INTERNAL:
     * delete the object
     * @exception  DatabaseException - an error has occurred on the database.
     * @return the row count.
     */
    public Integer deleteObject() throws DatabaseException {
        // Prepare the calls if not already set (prepare may not have had the modify row).
        if ((!hasMultipleCalls()) && (getCall() == null)) {
            prepareDeleteObject();
            if ((!hasMultipleCalls()) && (getCall() == null)) {
                return new Integer(1);// Must be 1 otherwise locking error will occur.
            }
        }

        return super.deleteObject();
    }

    /**
     * Update the object
     * @exception  DatabaseException - an error has occurred on the database.
     * @return the row count.
     */
    public Integer executeNoSelect() throws DatabaseException {
        // Prepare the calls if not already set (prepare may not have had the modify row).
        if ((!hasMultipleCalls()) && (getCall() == null)) {
            prepareExecuteNoSelect();
        }

        return super.executeNoSelect();
    }

    /**
     * Return the selection criteria for the statement.
     */
    public Expression getSelectionCriteria() {
        return getSQLStatement().getWhereClause();
    }

    /**
     * INTERNAL:
     * Return the sqlStatement
     */
    public SQLStatement getSQLStatement() {
        return sqlStatement;
    }

    /**
     * Normally only a single statement is used, however multiple table may require multiple statements on write.
     * This is lazy initialied to conserv space.
     */
    public Vector getSQLStatements() {
        if (sqlStatements == null) {
            sqlStatements = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(3);
        }
        return sqlStatements;
    }

    /**
     * Normally only a single statement is used, however multiple table may require multiple statements on write.
     * This is lazy initialied to conserv space.
     */
    public boolean hasMultipleStatements() {
        return (sqlStatements != null) && (!sqlStatements.isEmpty());
    }

    /**
     * Insert the object
     * @exception  DatabaseException - an error has occurred on the database.
     */
    public void insertObject() throws DatabaseException {
        // Prepare the calls if not already set (prepare may not have had the modify row).
        if ((!hasMultipleCalls()) && (getCall() == null)) {
            prepareInsertObject();
        }

        super.insertObject();
    }

    /**
     * Insert the object if the reprepare flag is set, first reprepare the query.
     * Added for CR#3237
     * @param boolean reprepare - whether to reprepare the query.
     */
    public void insertObject(boolean reprepare) {
        if (reprepare) {
            // Clear old calls, and reprepare. 
            setCalls(null);
            prepareInsertObject();
        }
        insertObject();
    }

    /**
     * Return true if this is a call query mechanism
     */
    public boolean isCallQueryMechanism() {
        return false;
    }

    /**
     * Return true if this is a statement query mechanism
     */
    public boolean isStatementQueryMechanism() {
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
        if ((!hasMultipleStatements()) && (getSQLStatement() == null)) {
            throw QueryException.sqlStatementNotSetProperly(getQuery());
        }

        // Cannot call super yet as the call is not built.
    }

    /**
     * Pre-build the SQL call from the statement.
     */
    public void prepareCursorSelectAllRows() {
        setCallFromStatement();
        // The statement is no longer require so can be released.
        clearStatement();

        super.prepareCursorSelectAllRows();
    }

    /**
     * Pre-build the SQL call from the statement.
     */
    public void prepareDeleteAll() {
        setCallFromStatement();
        // The statement is no longer require so can be released.
        clearStatement();

        super.prepareDeleteAll();
    }

    /**
     * Pre-build the SQL call from the statement.
     */
    public void prepareDeleteObject() {
        setCallFromStatement();
        // The statement is no longer require so can be released.
        clearStatement();

        super.prepareDeleteObject();
    }

    /**
     * Pre-build the SQL call from the statement.
     */
    public void prepareDoesExist(DatabaseField field) {
        setCallFromStatement();
        // The statement is no longer require so can be released.
        clearStatement();

        getCall().returnOneRow();
        prepareCall();
    }

    /**
     * Pre-build the SQL call from the statement.
     */
    public void prepareExecuteNoSelect() {
        setCallFromStatement();
        // The statement is no longer require so can be released.
        clearStatement();

        super.prepareExecuteNoSelect();
    }

    /**
     * Pre-build the SQL call from the statement.
     */
    public void prepareExecuteSelect() {
        setCallFromStatement();
        // The statement is no longer require so can be released.
        clearStatement();

        super.prepareExecuteSelect();
    }

    /**
     * Pre-build the SQL call from the statement.
     */
    public void prepareInsertObject() {
        // Require modify row to prepare.
        if (getModifyRow() == null) {
            return;
        }

        if (hasMultipleStatements()) {
            for (Enumeration statementEnum = getSQLStatements().elements();
                     statementEnum.hasMoreElements();) {
                ((SQLModifyStatement)statementEnum.nextElement()).setModifyRow(getModifyRow());
            }
        } else if (getSQLStatement() != null) {
            ((SQLModifyStatement)getSQLStatement()).setModifyRow(getModifyRow());
        }
        setCallFromStatement();
        // The statement is no longer require so can be released.
        clearStatement();

        super.prepareInsertObject();
    }

    /**
     * Pre-build the SQL call from the statement.
     */
    public void prepareSelectAllRows() {
        setCallFromStatement();
        // The statement is no longer require so can be released.
        clearStatement();

        super.prepareSelectAllRows();
    }

    /**
     * Pre-build the SQL call from the statement.
     */
    public void prepareSelectOneRow() {
        setCallFromStatement();
        // The statement is no longer require so can be released.
        clearStatement();

        super.prepareSelectOneRow();
    }

    /**
     * Pre-build the SQL call from the statement.
     */
    public void prepareUpdateObject() {
        // Require modify row to prepare.
        if (getModifyRow() == null) {
            return;
        }

        if (hasMultipleStatements()) {
            for (Enumeration statementEnum = getSQLStatements().elements();
                     statementEnum.hasMoreElements();) {
                ((SQLModifyStatement)statementEnum.nextElement()).setModifyRow(getModifyRow());
            }
        } else if (getSQLStatement() != null) {
            ((SQLModifyStatement)getSQLStatement()).setModifyRow(getModifyRow());
        }
        setCallFromStatement();
        // The statement is no longer require so can be released.
        clearStatement();

        super.prepareUpdateObject();
    }

    /**
     * Pre-build the SQL call from the statement.
     */
    public void prepareUpdateAll() {
        setCallFromStatement();// Will build an SQLUpdateAllStatement
        clearStatement();// The statement is no longer require so can be released.
        super.prepareUpdateAll();
    }

    /**
     * Pre-build the SQL call from the statement.
     */
    protected void setCallFromStatement() {
        // Profile SQL generation.
        getSession().startOperationProfile(SessionProfiler.SQL_GENERATION);
        if (hasMultipleStatements()) {
            for (Enumeration statementEnum = getSQLStatements().elements();
                     statementEnum.hasMoreElements();) {
                //			DatabaseCall call = ((SQLStatement) statementEnum.nextElement()).buildCall(getSession());
                DatabaseCall call = null;
                if (getDescriptor() != null) {
                    call = getDescriptor().buildCallFromStatement((SQLStatement)statementEnum.nextElement(), getSession());
                } else {
                    call = ((SQLStatement)statementEnum.nextElement()).buildCall(getSession());
                }

                // In case of update call may be null if no update required.
                if (call != null) {
                    addCall(call);
                }
            }
        } else {
            DatabaseCall call = null;
            if (getDescriptor() != null) {
                call = getDescriptor().buildCallFromStatement(getSQLStatement(), getSession());
            } else {
                call = getSQLStatement().buildCall(getSession());
            }

            // In case of update call may be null if no update required.
            if (call != null) {
                setCall(call);
            }
        }

        // Profile SQL generation.
        getSession().endOperationProfile(SessionProfiler.SQL_GENERATION);
    }

    /**
     * Set the sqlStatement
     */
    public void setSQLStatement(SQLStatement statement) {
        this.sqlStatement = statement;
    }

    /**
     * Normally only a single statement is used, however multiple table may require multiple statements on write.
     * This is lazy initialied to conserv space.
     */
    protected void setSQLStatements(Vector sqlStatements) {
        this.sqlStatements = sqlStatements;
    }

    /**
     * Update the object
     * @exception  DatabaseException - an error has occurred on the database.
     * @return the row count.
     */
    public Integer updateObject() throws DatabaseException {
        // Prepare the calls if not already set (prepare may not have had the modify row).
        if ((!hasMultipleCalls()) && (getCall() == null)) {
            prepareUpdateObject();
            if ((!hasMultipleCalls()) && (getCall() == null)) {
                return new Integer(1);// Must be 1 otherwise locking error will occur.
            }
        }

        return super.updateObject();
    }
}
