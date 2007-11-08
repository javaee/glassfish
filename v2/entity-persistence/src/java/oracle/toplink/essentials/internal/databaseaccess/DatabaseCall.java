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

import java.util.*;
import java.sql.*;
import java.io.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.sessions.DatabaseRecord;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.internal.expressions.ParameterExpression;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * INTERNAL:
 * <b>Purpose<b>: Used as an abstraction of a database invocation.
 * A call is an SQL string or procedure call with parameters.
 */
public abstract class DatabaseCall extends DatasourceCall {
    // The result and statement are cached for cursor selects.
    transient protected Statement statement;
    transient protected ResultSet result;

    // The call may specify that its parameters should be bound.
    protected int usesBinding;

    // Bound calls can use prepared statement caching.
    protected int shouldCacheStatement;

    // The returned fields.
    transient protected Vector fields;

    // Field matching is required for custom SQL when the fields order is not known.
    protected boolean isFieldMatchingRequired;

    // optimistic locking determination is required for batch writing
    protected boolean hasOptimisticLock;
    protected boolean isResultSetScrollable;

    // JDK 1.2 supports various types of results set
    protected int resultSetType;

    // JDK 1.2 supports various types of concurrency on results set
    protected int resultSetConcurrency;

    //query timeout limit in seconds
    protected int queryTimeout;

    //max rows returned in the result set by the call
    protected int maxRows;

    //firstResult set into the result set by the call
    protected int firstResult;

    //contain field - value pairs for LOB fields used to the
    //steaming operation during the writing (to the table)
    private transient AbstractRecord contexts = null;
    protected boolean isCursorOutputProcedure;

    // This parameter is here to determine if TopLink should expect a ResultSet back from the call
    // TopLink needs to know this information in order to call teh correct JDBC API
    protected int returnsResultSet;

    // Whether the call has to build output row
    protected boolean shouldBuildOutputRow;

    // Callable statement is required if there is an output parameter
    protected boolean isCallableStatementRequired;
    protected String sqlString;

    public DatabaseCall() {
        this.usesBinding = FalseUndefinedTrue.Undefined;
        this.shouldCacheStatement = FalseUndefinedTrue.Undefined;
        this.isFieldMatchingRequired = false;
        this.returnType = RETURN_MANY_ROWS;
        this.queryTimeout = 0;
        this.maxRows = 0;
        this.isCursorOutputProcedure = false;
        this.shouldBuildOutputRow = false;
        this.returnsResultSet = FalseUndefinedTrue.Undefined;
    }

    /**
     * INTERNAL:
     */
    public void appendIn(Object inObject) {
        getParameters().add(inObject);
        getParameterTypes().add(IN);
    }

    /**
     * INTERNAL:
     */
    public void appendInOut(DatabaseField inoutField) {
        Object[] inOut = { inoutField, inoutField };
        getParameters().add(inOut);
        getParameterTypes().add(INOUT);
    }

    /**
     * INTERNAL:
     */
    public void appendInOut(Object inValueOrField, DatabaseField outField) {
        Object[] inOut = { inValueOrField, outField };
        getParameters().add(inOut);
        getParameterTypes().add(INOUT);
    }

    /**
     * INTERNAL:
     */
    public void appendOut(DatabaseField outField) {
        getParameters().add(outField);
        getParameterTypes().add(OUT);
    }

    /**
     * INTERNAL:
     */
    public void appendOutCursor(DatabaseField outField) {
        getParameters().add(outField);
        getParameterTypes().add(OUT_CURSOR);
    }

    /**
     * Add the parameter.
     * If using binding bind the parameter otherwise let the platform print it.
     * The platform may also decide to bind the value.
     */
    public void appendParameter(Writer writer, Object parameter, AbstractSession session) {
        if (usesBinding == FalseUndefinedTrue.True) {
            bindParameter(writer, parameter);
        } else {
            session.getPlatform().appendParameter(this, writer, parameter);
        }
    }

    /**
     * Bind the parameter. Binding is determined by the call and second the platform.
     */
    public void bindParameter(Writer writer, Object parameter) {
        if (parameter instanceof Vector) {
            throw QueryException.inCannotBeParameterized(getQuery());
        }

        try {
            writer.write("?");
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
        getParameters().addElement(parameter);
    }

    /**
     * Return the appropriate mechanism,
     * with the call added as necessary.
     */
    public DatabaseQueryMechanism buildNewQueryMechanism(DatabaseQuery query) {
        return new CallQueryMechanism(query, this);
    }

    /**
     * INTERNAL:
     * Return DatabaseRow containing output fields and values.
     * Called only if shouldBuildOutputRow method returns true.
     */
    public AbstractRecord buildOutputRow(CallableStatement statement) throws SQLException {
        AbstractRecord row = new DatabaseRecord();
        for (int index = 0; index < parameters.size(); index++) {
            Object parameter = parameters.elementAt(index);
            if (parameter instanceof OutputParameterForCallableStatement) {
                OutputParameterForCallableStatement outParameter = (OutputParameterForCallableStatement)parameter;
                if (!outParameter.isCursor()) {
                    Object value = statement.getObject(index + 1);
                    DatabaseField field = outParameter.getOutputField();
                    row.put(field, value);
                }
            }
        }

        return row;
    }

    /**
     * Return the appropriate mechanism,
     * with the call added as necessary.
     */
    public DatabaseQueryMechanism buildQueryMechanism(DatabaseQuery query, DatabaseQueryMechanism mechanism) {
        if (mechanism.isCallQueryMechanism() && (mechanism instanceof CallQueryMechanism)) {
            // Must also add the call singleton...
            CallQueryMechanism callMechanism = ((CallQueryMechanism)mechanism);
            if (!callMechanism.hasMultipleCalls()) {
                callMechanism.addCall(callMechanism.getCall());
                callMechanism.setCall(null);
            }
            callMechanism.addCall(this);
            return mechanism;
        } else {
            return buildNewQueryMechanism(query);
        }
    }

    /**
     * INTERNAL:
     * Returns INOUT parameter. The first parameter is value to pass in, the second DatabaseField for out.
     */
    protected Object createInOutParameter(Object inValue, Object outParameter, AbstractSession session) {
        if (outParameter instanceof OutputParameterForCallableStatement) {
            return new InOutputParameterForCallableStatement(inValue, (OutputParameterForCallableStatement)outParameter);
        }
        if (outParameter instanceof DatabaseField) {
            return new InOutputParameterForCallableStatement(inValue, (DatabaseField)outParameter, session.getPlatform());
        }

        //should never happen
        return null;
    }

    /**
     * INTERNAL:
     * Return the SQL string for the call.
     */
    public String getCallString() {
        return getSQLString();
    }

    /**
     * The fields expected by the calls result set.
     * null means that the fields are unknown and should be built from the result set.
     */
    public Vector getFields() {
        return fields;
    }

    /**
     * INTERNAL:
     * Unfortunately can't avoid referencing query and descriptor:
     * the call should be performed after the translateCustomSQL (in SQLCall)
     * in the middle of prepare method (no parameter available earlier).
     *
     */
    protected DatabaseField getFieldWithTypeFromDescriptor(DatabaseField outField) {
        if (getQuery().getDescriptor() != null) {
            return getQuery().getDescriptor().getTypedField(outField);
        } else {
            return null;
        }
    }

    /**
     * INTERNAL:
     * Return 1-based index of out cursor parameter, or -1.
     */
    public int getCursorOutIndex() {
        for (int i = 0; i < getParameters().size(); i++) {
            Object parameter = getParameters().elementAt(i);
            if (parameter instanceof OutputParameterForCallableStatement) {
                if (((OutputParameterForCallableStatement)parameter).isCursor()) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    /**
     * get first result
     */
    public int getFirstResult() {
        return this.firstResult;
    }

    /**
     * Return the SQL string for logging purposes.
     */
    public String getLogString(Accessor accessor) {
        if (hasParameters()) {
            StringWriter writer = new StringWriter();
            writer.write(getSQLString());
            writer.write(Helper.cr());
            if (hasParameters()) {
                AbstractSession session = null;
                if (getQuery() != null) {
                    session = getQuery().getSession();
                }
                appendLogParameters(getParameters(), accessor, writer, session);
            }
            return writer.toString();
        } else {
            return getSQLString();
        }
    }
    
    /**
     * Print the parameters to the write for logging purposes.
     */
    public static void appendLogParameters(Collection parameters, Accessor accessor, StringWriter writer, AbstractSession session) {
        writer.write("\tbind => [");
        for (Iterator paramsEnum = parameters.iterator(); paramsEnum.hasNext();) {
            Object parameter = paramsEnum.next();
            if (parameter instanceof DatabaseField) {
                writer.write("null");
            } else {
                if (session != null) {
                    parameter = session.getPlatform().convertToDatabaseType(parameter);
                }
                writer.write(String.valueOf(parameter));
            }
            if (paramsEnum.hasNext()) {
                writer.write(", ");
            } else {
                writer.write("]");
            }
        }
    }
    
    /**
     * get max rows returned from the call
     */
    public int getMaxRows() {
        return this.maxRows;
    }

    /**
     * INTERNAL
     * Returns the fields to be used in output row.
     */
    public Vector getOutputRowFields() {
        Vector fields = new Vector();
        for (int i = 0; i < getParameters().size(); i++) {
            Integer parameterType = (Integer)getParameterTypes().elementAt(i);
            Object parameter = getParameters().elementAt(i);
            if (parameterType == OUT) {
                fields.add(parameter);
            } else if (parameterType == INOUT) {
                fields.add(((Object[])parameter)[1]);
            }
        }
        return fields;
    }

    /**
     * INTERNAL:
     * Return the query string (SQL) of the call.
     */
    public String getQueryString() {
        return getSQLString();
    }

    /**
     * The result set is stored for the return value of cursor selects.
     */
    public ResultSet getResult() {
        return result;
    }

    /**
     * ADVANCED:
     * This method returns a value that represents if the customer has set whether or not TopLink should expect
     * the stored procedure to returning a JDBC ResultSet.  The result of the method corresponds
     * to false, true.
     */
    public boolean getReturnsResultSet() {
        if (returnsResultSet == FalseUndefinedTrue.Undefined) {
            return !shouldBuildOutputRow();
        } else {
            return returnsResultSet == FalseUndefinedTrue.True;
        }
    }

    public int getResultSetConcurrency() {
        return resultSetConcurrency;
    }

    public int getResultSetType() {
        return resultSetType;
    }

    /**
     * Return the SQL string that will be executed.
     */
    public String getSQLString() {
        return sqlString;
    }

    /**
     * The statement is stored for the return value of cursor selects.
     */
    public Statement getStatement() {
        return statement;
    }

    /**
     * This check is needed only when doing batch writing.
     */
    public boolean hasOptimisticLock() {
        return hasOptimisticLock;
    }

    /**
     * Callable statement is required if there is an output parameter.
     */
    protected boolean isCallableStatementRequired() {
        return isCallableStatementRequired;
    }

    /**
     * Return if the call is dynamic SQL call.
     * This means the call has no parameters, is not using binding,
     * is not a stored procedure (CallableStatement), or cursor.
     * This means that a Statement, not PrepareStatement will be used for the call.
     */
    protected boolean isDynamicCall(AbstractSession session) {
        return DatabaseAccessor.shouldUseDynamicStatements && (!usesBinding(session)) && (!isResultSetScrollable()) && (!hasParameters());
    }

    /**
     * Used for Oracle result sets through procedures.
     */
    public boolean isCursorOutputProcedure() {
        return isCursorOutputProcedure;
    }

    /**
     * The return type is one of, NoReturn, ReturnOneRow or ReturnManyRows.
     */
    public boolean isCursorReturned() {
        return getReturnType() == RETURN_CURSOR;
    }

    /**
     * Return if field matching is required.
     * Field matching is required for custom SQL statements where the result set field order is not known.
     */
    public boolean isFieldMatchingRequired() {
        return isFieldMatchingRequired;
    }

    /**
     * Return whether all the results of the call have been returned.
     */
    public boolean isFinished() {
        return !isCursorReturned();
    }

    /**
     * Return true for procedures with any output (or in/out) parameters and no cursors
     */
    public boolean isNonCursorOutputProcedure() {
        return !isCursorOutputProcedure() && shouldBuildOutputRow();
    }

    public boolean isResultSetScrollable() {
        return isResultSetScrollable;
    }

    /**
     * Allow for the field order to be matched if required.
     * This is required for custom SQL.
     */
    public void matchFieldOrder(ResultSet resultSet, DatabaseAccessor accessor, AbstractSession session) {
        if ((getFields() != null) && (!isFieldMatchingRequired())) {
            return;
        }
        setFields(accessor.buildSortedFields(getFields(), resultSet, session));
    }

    /**
     * INTERNAL:
     * Allow pre-printing of the SQL string for fully bound calls, to save from reprinting.
     * Should be called before translation.
     */
    public void prepare(AbstractSession session) {
        if (isPrepared()) {
            return;
        }

        prepareInternal(session);

        setIsPrepared(true);
    }

    /**
     * INTERNAL:
     * Called by prepare method only. May be overridden.
     */
    protected void prepareInternal(AbstractSession session) {
        if (isCursorOutputProcedure()) {
            // 1. If there are no OUT_CURSOR parameters - change the first OUT to OUT_CURSOR;
            // 2. If there are multiple OUT_CURSOR parameters - throw Validation exception
            int nFirstOutParameterIndex = -1;
            boolean hasFoundOutCursor = false;
            for (int index = 0; index < parameters.size(); index++) {
                Integer parameterType = (Integer)parameterTypes.elementAt(index);
                if (parameterType == DatasourceCall.OUT_CURSOR) {
                    if (hasFoundOutCursor) {
                        // one cursor has been already found
                        throw ValidationException.multipleCursorsNotSupported(toString());
                    } else {
                        hasFoundOutCursor = true;
                    }
                } else if (parameterType == DatasourceCall.OUT) {
                    if (nFirstOutParameterIndex == -1) {
                        nFirstOutParameterIndex = index;
                    }
                } else if (parameterType == null) {
                    // setCustomSQLArgumentType method was called when custom SQL is not used
                    throw ValidationException.wrongUsageOfSetCustomArgumentTypeMethod(toString());
                }
            }
            if (!hasFoundOutCursor && (nFirstOutParameterIndex >= 0)) {
                parameterTypes.setElementAt(DatasourceCall.OUT_CURSOR, nFirstOutParameterIndex);
            }
        }

        for (int i = 0; i < getParameters().size(); i++) {
            Object parameter = getParameters().elementAt(i);
            Integer parameterType = (Integer)getParameterTypes().elementAt(i);
            if (parameterType == MODIFY) {
                // in case the field's type is not set, the parameter type is set to CUSTOM_MODIFY.
                DatabaseField field = (DatabaseField)parameter;
                if ((field.getType() == null) || session.getPlatform().shouldUseCustomModifyForCall(field)) {
                    getParameterTypes().setElementAt(CUSTOM_MODIFY, i);
                }
            } else if (parameterType == INOUT) {
                // In case there is a type in outField, outParameter is created.
                // During translate call, either outParameter or outField is used for
                // creating inOut parameter.
                setShouldBuildOutputRow(true);
                setIsCallableStatementRequired(true);
                DatabaseField outField = (DatabaseField)((Object[])parameter)[1];
                if (outField.getType() == null) {
                    DatabaseField typeOutField = getFieldWithTypeFromDescriptor(outField);
                    if (typeOutField != null) {
                        outField = (DatabaseField)typeOutField.clone();
                    }
                }
                if (outField.getType() != null) {
                    // outParameter contains all the info for registerOutputParameter call.
                    OutputParameterForCallableStatement outParameter = new OutputParameterForCallableStatement(outField, session.getPlatform());
                    ((Object[])parameter)[1] = outParameter;
                }
            } else if ((parameterType == OUT) || (parameterType == OUT_CURSOR)) {
                boolean isCursor = parameterType == OUT_CURSOR;
                if (!isCursor) {
                    setShouldBuildOutputRow(true);
                }
                setIsCallableStatementRequired(true);
                DatabaseField outField = (DatabaseField)parameter;
                if (outField.getType() == null) {
                    DatabaseField typeOutField = getFieldWithTypeFromDescriptor(outField);
                    if (typeOutField != null) {
                        outField = (DatabaseField)typeOutField.clone();
                    }
                }

                // outParameter contains all the info for registerOutputParameter call.
                OutputParameterForCallableStatement outParameter = new OutputParameterForCallableStatement(outField, session.getPlatform(), isCursor);
                getParameters().setElementAt(outParameter, i);
                // nothing to do during translate method
                getParameterTypes().setElementAt(LITERAL, i);
            }
        }
        if (this.returnsResultSet == FalseUndefinedTrue.Undefined) {
            setReturnsResultSet(!isCallableStatementRequired());
        }
    }

    /**
     * INTERNAL:
     * Prepare the JDBC statement, this may be parameterize or a call statement.
     * If caching statements this must check for the pre-prepared statement and re-bind to it.
     */
    public Statement prepareStatement(DatabaseAccessor accessor, AbstractRecord translationRow, AbstractSession session) throws SQLException {
        Statement statement = accessor.prepareStatement(this, session);

        if (getMaxRows() > 0) { 
            statement.setMaxRows(getMaxRows()); 
        }

        if (!hasParameters()) {
            return statement;
        }

        for (int index = 0; index < getParameters().size(); index++) {
            session.getPlatform().setParameterValueInDatabaseCall(this.getParameters(), (PreparedStatement)statement, index, session);
        }

        return statement;
    }

    /**
     * The fields expected by the calls result set.
     */
    public void setFields(Vector fields) {
        this.fields = fields;
    }

    /**
     * The firstResult set on the result set
     */
    public void setFirstResult(int firstResult) {
        this.firstResult = firstResult;
    }

    /**
     * This check is needed only when doing batch writing and we hit on optimistic locking.
     */
    public void setHasOptimisticLock(boolean hasOptimisticLock) {
        this.hasOptimisticLock = hasOptimisticLock;
    }

    /**
     * Callable statement is required if there is an output parameter.
     */
    protected void setIsCallableStatementRequired(boolean isCallableStatementRequired) {
        this.isCallableStatementRequired = isCallableStatementRequired;
    }

    /**
     * Used for Oracle result sets through procedures.
     */
    public void setIsCursorOutputProcedure(boolean isCursorOutputProcedure) {
        this.isCursorOutputProcedure = isCursorOutputProcedure;
    }

    /**
     * Field matching is required for custom SQL statements where the result set field order is not known.
     */
    public void setIsFieldMatchingRequired(boolean isFieldMatchingRequired) {
        this.isFieldMatchingRequired = isFieldMatchingRequired;
    }

    public void setIsResultSetScrollable(boolean isResultSetScrollable) {
        this.isResultSetScrollable = isResultSetScrollable;
    }

    /**
     * set query max returned row size to the JDBC Statement
     */
    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    /**
     * INTERNAL:
     * Set the query string (SQL) of the call.
     */
    public void setQueryString(String queryString) {
        setSQLStringInternal(queryString);
    }

    /**
     * The result set is stored for the return value of cursor selects.
     */
    public void setResult(ResultSet result) {
        this.result = result;
    }

    public void setResultSetConcurrency(int resultSetConcurrency) {
        this.resultSetConcurrency = resultSetConcurrency;
    }

    /**
     * INTERNAL:
     * Set the SQL string.
     */
    protected void setSQLStringInternal(String sqlString) {
        this.sqlString = sqlString;
    }

    public void setResultSetType(int resultSetType) {
        this.resultSetType = resultSetType;
    }

    /**
     * PUBLIC:
     * Use this method to tell TopLink that the stored procedure will be returning a JDBC ResultSet
     */
    public void setReturnsResultSet(boolean returnsResultSet) {
        if (returnsResultSet) {
            this.returnsResultSet = FalseUndefinedTrue.True;
        } else {
            this.returnsResultSet = FalseUndefinedTrue.False;
        }
    }

    /**
     * INTERNAL:
     * Set whether the call has to build output row
     */
    protected void setShouldBuildOutputRow(boolean shouldBuildOutputRow) {
        this.shouldBuildOutputRow = shouldBuildOutputRow;
    }

    /**
     * Bound calls can use prepared statement caching.
     */
    public void setShouldCacheStatement(boolean shouldCacheStatement) {
        if (shouldCacheStatement) {
            this.shouldCacheStatement = FalseUndefinedTrue.True;
        } else {
            this.shouldCacheStatement = FalseUndefinedTrue.False;
        }
    }

    /**
     * The statement is stored for the return value of cursor selects.
     */
    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    /**
     * The call may specify that its parameters should be bound.
     */
    public void setUsesBinding(boolean usesBinding) {
        if (usesBinding) {
            this.usesBinding = FalseUndefinedTrue.True;
        } else {
            this.usesBinding = FalseUndefinedTrue.False;
        }
    }

    /**
     * Set whether the call has to build output row
     */
    public boolean shouldBuildOutputRow() {
        return this.shouldBuildOutputRow;
    }

    /**
     * Bound calls can use prepared statement caching.
     */
    public boolean shouldCacheStatement(AbstractSession session) {
        return shouldCacheStatement(session.getPlatform());
    }

    /**
     * Bound calls can use prepared statement caching.
     */
    public boolean shouldCacheStatement(DatabasePlatform databasePlatform) {
        //CR4272  If result set is scrollable, do not cache statement since scrollable cursor can not be used for cached statement
        if (isResultSetScrollable()) {
            return false;
        }
        if (shouldCacheStatement == FalseUndefinedTrue.Undefined) {
            return databasePlatform.shouldCacheAllStatements();
        } else {
            return shouldCacheStatement == FalseUndefinedTrue.True;
        }
    }

    /**
     * INTERNAL:
     * Print the SQL string.
     */
    public String toString() {
        String str = Helper.getShortClassName(getClass());
        if (getSQLString() == null) {
            return str;
        } else {
            return str + "(" + getSQLString() + ")";
        }
    }

    /**
     * INTERNAL:
     * Allow the call to translate from the translation for predefined calls.
     */
    public void translate(AbstractRecord translationRow, AbstractRecord modifyRow, AbstractSession session) {
        if (!isPrepared()) {
            throw ValidationException.cannotTranslateUnpreparedCall(toString());
        }
        if (usesBinding(session)) {
            Vector parametersValues = new Vector();
            for (int index = 0; index < getParameters().size(); index++) {
                Object parameter = getParameters().elementAt(index);
                Object parameterType = getParameterTypes().elementAt(index);
                if (parameterType == MODIFY) {
                    DatabaseField field = (DatabaseField)parameter;
                    Object value = modifyRow.get(field);
                    if (value == null) {
                        value = modifyRow.getField(field);
                    }
                    parametersValues.addElement(value);
                } else if (parameterType == CUSTOM_MODIFY) {
                    DatabaseField field = (DatabaseField)parameter;
                    Object value = modifyRow.get(field);
                    value = session.getPlatform().getCustomModifyValueForCall(this, value, field, true);
                    if (value == null) {
                        value = modifyRow.getField(field);
                    }
                    parametersValues.addElement(value);
                } else if (parameterType == TRANSLATION) {
                    Object value = null;
                    DatabaseField field = null;
                    if (parameter instanceof ParameterExpression) {
                        value = ((ParameterExpression)parameter).getValue(translationRow, session);
                    } else {
                        field = (DatabaseField)parameter;
                        value = translationRow.get(field);
                        if (value == null) {// Backward compatibility double check.
                            value = modifyRow.get(field);
                        }
                    }
                    if (value instanceof Vector) {
                        throw QueryException.inCannotBeParameterized(getQuery());
                    }
                    if ((value == null) && (field != null)) {
                        value = translationRow.getField(field);
                    }
                    parametersValues.addElement(value);
                } else if (parameterType == LITERAL) {
                    parametersValues.addElement(parameter);
                } else if (parameterType == IN) {
                    Object value = getValueForInParameter(parameter, translationRow, modifyRow, session, true);
                    parametersValues.addElement(value);
                } else if (parameterType == INOUT) {
                    Object value = getValueForInOutParameter(parameter, translationRow, modifyRow, session);
                    parametersValues.addElement(value);
                }
            }
            setParameters(parametersValues);
            return;
        }

        translateQueryString(translationRow, modifyRow, session);
    }

    /**
     * The call may specify that its parameters should be bound.
     */
    public boolean usesBinding(AbstractSession session) {
        return usesBinding(session.getPlatform());
    }

    /**
     * The call may specify that its parameters should be bound.
     */
    public boolean usesBinding(DatabasePlatform databasePlatform) {
        if (usesBinding == FalseUndefinedTrue.Undefined) {
            return databasePlatform.shouldBindAllParameters();
        } else {
            return usesBinding == FalseUndefinedTrue.True;
        }
    }

    /**
     * INTERNAL:
     * Return if the locator is required for the LOB (BLOB and CLOB) writing.
     */
    public boolean isLOBLocatorNeeded() {
        return contexts != null;
    }

    /**
     * INTERNAL:
     * Add a field - value pair for LOB field into the context.
     */
    public void addContext(DatabaseField field, Object value) {
        if (contexts == null) {
            contexts = new DatabaseRecord(2);
        }
        contexts.add(field, value);
    }

    /**
     * INTERNAL:
     * Return the contexts (for LOB)
     */
    public AbstractRecord getContexts() {
        return contexts;
    }

    /**
     * INTERNAL:
     * Set the contexts (for LOB)
     */
    public void setContexts(AbstractRecord contexts) {
        this.contexts = contexts;
    }

    /**
     * PUBLIC:
     * Used for Oracle result sets through procedures.
     * The first OUT parameter is set as a cursor output.
     */
    public void useUnnamedCursorOutputAsResultSet() {
        setIsCursorOutputProcedure(true);
    }
}
