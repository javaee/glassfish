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
import java.io.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.internal.expressions.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * INTERNAL:
 * <b>Purpose<b>: Used as an abstraction of a datasource invocation.
 *
 * @author James Sutherland
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public abstract class DatasourceCall implements Call {
    // Back reference to query, unfortunately required for events.
    protected DatabaseQuery query;

    // The parameters (values) are ordered as they appear in the call.
    transient protected Vector parameters;

    // The parameter types determine if the parameter is a modify, translation or literal type.
    transient protected Vector parameterTypes;
    public static final Integer LITERAL = new Integer(1);
    public static final Integer MODIFY = new Integer(2);
    public static final Integer TRANSLATION = new Integer(3);
    public static final Integer CUSTOM_MODIFY = new Integer(4);
    public static final Integer OUT = new Integer(5);
    public static final Integer INOUT = new Integer(6);
    public static final Integer IN = new Integer(7);
    public static final Integer OUT_CURSOR = new Integer(8);

    // Store if the call has been prepared.
    protected boolean isPrepared;

    // Type of call.
    protected int returnType;
    protected static final int NO_RETURN = 1;
    protected static final int RETURN_ONE_ROW = 2;
    protected static final int RETURN_MANY_ROWS = 3;
    protected static final int RETURN_CURSOR = 4;

    public DatasourceCall() {
        this.isPrepared = false;
        this.returnType = RETURN_MANY_ROWS;
    }

    /**
     * The parameters are the values in order of occurance in the SQL statement.
     * This is lazy initialized to conserv space on calls that have no parameters.
     */
    public Vector getParameters() {
        if (parameters == null) {
            parameters = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        }
        return parameters;
    }

    /**
     * The parameter types determine if the parameter is a modify, translation or litteral type.
     */
    public Vector getParameterTypes() {
        if (parameterTypes == null) {
            parameterTypes = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        }
        return parameterTypes;
    }

    /**
     * The parameters are the values in order of occurance in the SQL statement.
     */
    public void setParameters(Vector parameters) {
        this.parameters = parameters;
    }

    /**
     * The parameter types determine if the parameter is a modify, translation or litteral type.
     */
    public void setParameterTypes(Vector parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    /**
     * The parameters are the values in order of occurance in call.
     * This is lazy initialized to conserv space on calls that have no parameters.
     */
    public boolean hasParameters() {
        return (parameters != null) && (!getParameters().isEmpty());
    }

    /**
     * The return type is one of, NoReturn, ReturnOneRow or ReturnManyRows.
     */
    public boolean areManyRowsReturned() {
        return getReturnType() == RETURN_MANY_ROWS;
    }

    public boolean isOutputParameterType(Integer parameterType) {
        return (parameterType == OUT) || (parameterType == INOUT) || (parameterType == OUT_CURSOR);
    }

    /**
     * Bound calls can have the SQL pre generated.
     */
    protected boolean isPrepared() {
        return isPrepared;
    }

    /**
     * Bound calls can have the SQL pre generated.
     */
    protected void setIsPrepared(boolean isPrepared) {
        this.isPrepared = isPrepared;
    }

    /**
     * Return the appropriate mechanism,
     * with the call added as necessary.
     */
    public DatabaseQueryMechanism buildNewQueryMechanism(DatabaseQuery query) {
        return new DatasourceCallQueryMechanism(query, this);
    }

    /**
     * Return the appropriate mechanism,
     * with the call added as necessary.
     */
    public DatabaseQueryMechanism buildQueryMechanism(DatabaseQuery query, DatabaseQueryMechanism mechanism) {
        if (mechanism.isCallQueryMechanism() && (mechanism instanceof DatasourceCallQueryMechanism)) {
            // Must also add the call singleton...
            DatasourceCallQueryMechanism callMechanism = ((DatasourceCallQueryMechanism)mechanism);
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

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException exception) {
            ;//Do nothing
        }

        return null;
    }

    /**
     * Return the SQL string for logging purposes.
     */
    public abstract String getLogString(Accessor accessor);

    /**
     * Back reference to query, unfortunately required for events.
     */
    public DatabaseQuery getQuery() {
        return query;
    }

    /**
     * The return type is one of, NoReturn, ReturnOneRow or ReturnManyRows.
     */
    public int getReturnType() {
        return returnType;
    }

    /**
     * The return type is one of, NoReturn, ReturnOneRow or ReturnManyRows.
     */
    public boolean isCursorReturned() {
        return getReturnType() == RETURN_CURSOR;
    }

    /**
     * Return whether all the results of the call have been returned.
     */
    public boolean isFinished() {
        return !isCursorReturned();
    }

    /**
     * The return type is one of, NoReturn, ReturnOneRow or ReturnManyRows.
     */
    public boolean isNothingReturned() {
        return getReturnType() == NO_RETURN;
    }

    /**
     * The return type is one of, NoReturn, ReturnOneRow or ReturnManyRows.
     */
    public boolean isOneRowReturned() {
        return getReturnType() == RETURN_ONE_ROW;
    }

    public boolean isSQLCall() {
        return false;
    }

    public boolean isStoredFunctionCall() {
        return false;
    }

    public boolean isStoredProcedureCall() {
        return false;
    }

    public boolean isEJBQLCall() {
        return false;
    }

    public boolean isEISInteraction() {
        return false;
    }

    public boolean isQueryStringCall() {
        return false;
    }

    /**
     * Allow pre-printing of the query/SQL string for fully bound calls, to save from reprinting.
     */
    public void prepare(AbstractSession session) {
        setIsPrepared(true);
    }

    /**
     * Cursor return is used for cursored streams.
     */
    public void returnCursor() {
        setReturnType(RETURN_CURSOR);
    }

    /**
     * Many rows are returned for read-all queries.
     */
    public void returnManyRows() {
        setReturnType(RETURN_MANY_ROWS);
    }

    /**
     * No return is used for modify calls like insert / update / delete.
     */
    public void returnNothing() {
        setReturnType(NO_RETURN);
    }

    /**
     * One row is returned for read-object queries.
     */
    public void returnOneRow() {
        setReturnType(RETURN_ONE_ROW);
    }

    /**
     * Back reference to query, unfortunately required for events.
     */
    public void setQuery(DatabaseQuery query) {
        this.query = query;
    }

    /**
     * The return type is one of, NoReturn, ReturnOneRow or ReturnManyRows.
     */
    public void setReturnType(int returnType) {
        this.returnType = returnType;
    }

    /**
     * Allow the call to translate from the translation for predefined calls.
     */
    public void translate(AbstractRecord translationRow, AbstractRecord modifyRow, AbstractSession session) {
        //do nothing by default.
    }

    /**
     * Return the query string of the call.
     * This must be overwritten by subclasses that support query language translation (SQLCall, XQueryCall).
     */
    public String getQueryString() {
        return "";
    }

    /**
     * Set the query string of the call.
     * This must be overwritten by subclasses that support query language translation (SQLCall, XQueryCall).
     */
    public void setQueryString(String queryString) {
        // Nothing by default.
    }

    /**
     * INTERNAL:
     * Parse the query string for # markers for custom query based on a query language.
     * This is used by SQLCall and XQuery call, but can be reused by other query languages.
     */
    public void translateCustomQuery() {
        if (getQueryString().indexOf("#") == -1) {
            if (this.getQuery().shouldBindAllParameters() && getQueryString().indexOf("?") == -1){
                return;
            }
            translatePureSQLCustomQuery();
            return;
        }

        int lastIndex = 0;
        int litteralIndex = 0;// This index is used to determine the position of litterals
        String queryString = getQueryString();
        Writer writer = new CharArrayWriter(queryString.length() + 50);
        try {
            // ** This method is heavily optimized do not touch anyhthing unless you "know" what your doing.
            while (lastIndex != -1) {
                int poundIndex = queryString.indexOf('#', lastIndex);
                String token;
                if (poundIndex == -1) {
                    token = queryString.substring(lastIndex, queryString.length());
                    lastIndex = -1;
                } else {
                    token = queryString.substring(lastIndex, poundIndex);
                }
                writer.write(token);
                if (poundIndex != -1) {
                    int wordEndIndex = poundIndex + 1;
                    while ((wordEndIndex < queryString.length()) && (whitespace().indexOf(queryString.charAt(wordEndIndex)) == -1)) {
                        wordEndIndex = wordEndIndex + 1;
                    }

                    // Check for ## which means field from modify row.
                    if (queryString.charAt(poundIndex + 1) == '#') {
                        // Check for ### which means OUT parameter type.
                        if (queryString.charAt(poundIndex + 2) == '#') {
                            // Check for #### which means INOUT parameter type.
                            if (queryString.charAt(poundIndex + 3) == '#') {
                                String fieldName = queryString.substring(poundIndex + 4, wordEndIndex);
                                DatabaseField field = createField(fieldName);
                                appendInOut(writer, field);
                            } else {
                                String fieldName = queryString.substring(poundIndex + 3, wordEndIndex);
                                DatabaseField field = createField(fieldName);
                                appendOut(writer, field);
                            }
                        } else {
                            String fieldName = queryString.substring(poundIndex + 2, wordEndIndex);
                            DatabaseField field = createField(fieldName);
                            appendModify(writer, field);
                        }
                    } else {
                        String fieldName = queryString.substring(poundIndex + 1, wordEndIndex);
                        DatabaseField field = createField(fieldName);
                        appendIn(writer, field);
                    }
                    lastIndex = wordEndIndex;
                }
            }
            setQueryString(writer.toString());
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }

    /**
     * INTERNAL:
     * Parse the query string for # markers for custom query based on a query language.
     * This is used by SQLCall and XQuery call, but can be reused by other query languages.
     */
    public void translatePureSQLCustomQuery() {
        int lastIndex = 0;
        String queryString = getQueryString();
        int parameterIndex = 1; // this is the parameter index
        Writer writer = new CharArrayWriter(queryString.length() + 50);
        try {
            // ** This method is heavily optimized do not touch anyhthing unless you "know" what your doing.
            while (lastIndex != -1) {
                int markIndex = queryString.indexOf('?', lastIndex);
                String token;
                if (markIndex == -1) { // did not find question mark then we are done looking
                    token = queryString.substring(lastIndex, queryString.length()); //write rest of sql
                    lastIndex = -1;
                } else {
                    token = queryString.substring(lastIndex, markIndex);
                    lastIndex = markIndex + 1;
                }
                writer.write(token);
                if (markIndex != -1) {  // found the question mark now find the named token
                    int wordEndIndex = markIndex + 1;
                    while ((wordEndIndex < queryString.length()) && (whitespace().indexOf(queryString.charAt(wordEndIndex)) == -1)) {
                        wordEndIndex = wordEndIndex + 1;
                    }
                    if (wordEndIndex > markIndex + 1){ //found a 'name' for this token (may be positional)
                        String fieldName = queryString.substring(markIndex + 1, wordEndIndex);
                        DatabaseField field = createField(fieldName);
                        appendIn(writer, field);
                        lastIndex = wordEndIndex;
                    }else{
                        DatabaseField field = createField(String.valueOf(parameterIndex));
                        parameterIndex++;
                        appendIn(writer, field);
                    }
                }
            }
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
        setQueryString(writer.toString());
    }

    /**
     * INTERNAL:
     * Create a new Database Field
     * This method can be overridden by subclasses to return other field types
     */
    protected DatabaseField createField(String fieldName) {
        return new DatabaseField(fieldName);
    }

    /**
     * INTERNAL:
     * All values are printed as ? to allow for parameter binding or translation during the execute of the call.
     */
    public void appendLiteral(Writer writer, Object literal) {
        try {
            writer.write(argumentMarker());
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
        getParameters().addElement(literal);
        getParameterTypes().addElement(LITERAL);
    }

    /**
     * INTERNAL:
     * All values are printed as ? to allow for parameter binding or translation during the execute of the call.
     */
    public void appendTranslation(Writer writer, DatabaseField modifyField) {
        try {
            writer.write(argumentMarker());
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
        getParameters().addElement(modifyField);
        getParameterTypes().addElement(TRANSLATION);
    }

    /**
     * INTERNAL:
     * All values are printed as ? to allow for parameter binding or translation during the execute of the call.
     */
    public void appendModify(Writer writer, DatabaseField modifyField) {
        try {
            writer.write(argumentMarker());
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
        getParameters().addElement(modifyField);
        getParameterTypes().addElement(MODIFY);
    }

    /**
     * INTERNAL:
     * All values are printed as ? to allow for parameter binding or translation during the execute of the call.
     */
    public void appendIn(Writer writer, DatabaseField field) {
        try {
            writer.write(argumentMarker());
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
        getParameters().addElement(field);
        getParameterTypes().addElement(IN);
    }

    /**
     * INTERNAL:
     * All values are printed as ? to allow for parameter binding or translation during the execute of the call.
     */
    public void appendInOut(Writer writer, DatabaseField inoutField) {
        try {
            writer.write(argumentMarker());
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
        Object[] inOut = { inoutField, inoutField };
        getParameters().addElement(inOut);
        getParameterTypes().addElement(INOUT);
    }

    /**
     * INTERNAL:
     * All values are printed as ? to allow for parameter binding or translation during the execute of the call.
     */
    public void appendOut(Writer writer, DatabaseField outField) {
        try {
            writer.write(argumentMarker());
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
        getParameters().addElement(outField);
        getParameterTypes().addElement(OUT);
    }

    /**
     * Add the parameter.
     * If using binding bind the parameter otherwise let the platform print it.
     * The platform may also decide to bind the value.
     */
    public void appendParameter(Writer writer, Object parameter, AbstractSession session) {
        session.getDatasourcePlatform().appendParameter(this, writer, parameter);
    }

    /**
     * INTERNAL:
     * Return the character to use for the argument marker.
     * ? is used in SQL, however other query languages such as XQuery need to use other markers.
     */
    protected char argumentMarker() {
        return '?';
    }

    /**
     * INTERNAL:
     * Return the characters that represent non-arguments names.
     */
    protected String whitespace() {
        return ",); \n\t:";
    }

    /**
     * INTERNAL:
     * Allow the call to translate from the translation for predefined calls.
     */
    public void translateQueryString(AbstractRecord translationRow, AbstractRecord modifyRow, AbstractSession session) {
        if (getQueryString().indexOf(argumentMarker()) == -1) {
            return;
        }

        //has a '?'
        if (getParameters().isEmpty()) {
            //has no parameters
            return;
        }

        int lastIndex = 0;
        int parameterIndex = 0;
        String queryString = getQueryString();
        Writer writer = new CharArrayWriter(queryString.length() + 50);
        try {
            // ** This method is heavily optimized do not touch anyhthing unless you know "very well" what your doing!!
            // Must translate field parameters and may get new bound parameters for large data.
            Vector parameterFields = getParameters();
            setParameters(null);
            while (lastIndex != -1) {
                int tokenIndex = queryString.indexOf(argumentMarker(), lastIndex);
                String token;
                if (tokenIndex == -1) {
                    token = queryString.substring(lastIndex, queryString.length());
                    lastIndex = -1;
                } else {
                    token = queryString.substring(lastIndex, tokenIndex);
                }
                writer.write(token);
                if (tokenIndex != -1) {
                    // Process next parameter.
                    Integer parameterType = (Integer)getParameterTypes().elementAt(parameterIndex);
                    if (parameterType == MODIFY) {
                        DatabaseField field = (DatabaseField)parameterFields.elementAt(parameterIndex);
                        Object value = modifyRow.get(field);
                        appendParameter(writer, value, session);
                    } else if (parameterType == CUSTOM_MODIFY) {
                        DatabaseField field = (DatabaseField)parameterFields.elementAt(parameterIndex);
                        Object value = modifyRow.get(field);
                        if (value != null) {
                            value = session.getDatasourcePlatform().getCustomModifyValueForCall(this, value, field, false);
                        }
                        appendParameter(writer, value, session);
                    } else if (parameterType == TRANSLATION) {
                        Object parameter = parameterFields.elementAt(parameterIndex);
                        Object value = null;

                        // Parameter expressions are used for nesting and correct mapping conversion of the value.
                        if (parameter instanceof ParameterExpression) {
                            value = ((ParameterExpression)parameter).getValue(translationRow, session);
                        } else {
                            DatabaseField field = (DatabaseField)parameter;
                            value = translationRow.get(field);
                            // Must check for the modify row as well for custom SQL compatibility as only one # is required.
                            if ((value == null) && (modifyRow != null)) {
                                value = modifyRow.get(field);
                            }
                        }
                        appendParameter(writer, value, session);
                    } else if (parameterType == LITERAL) {
                        Object value = parameterFields.elementAt(parameterIndex);
                        if(value instanceof DatabaseField) {
                            value = null;
                        }
                        appendParameter(writer, value, session);
                    } else if (parameterType == IN) {
                        Object parameter = parameterFields.elementAt(parameterIndex);
                        Object value = getValueForInParameter(parameter, translationRow, modifyRow, session, false);
                        appendParameter(writer, value, session);
                    } else if (parameterType == INOUT) {
                        Object parameter = parameterFields.elementAt(parameterIndex);
                        Object value = getValueForInOutParameter(parameter, translationRow, modifyRow, session);
                        appendParameter(writer, value, session);
                    }
                    lastIndex = tokenIndex + 1;
                    parameterIndex++;
                }
            }

            setQueryString(writer.toString());

        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }

    /**
     * INTERNAL:
     * Returns value for IN parameter. Called by translate and translateSQLString methods.
     * In case shouldBind==true tries to return a DatabaseField with type instead of null,
     * returns null only in case no DatabaseField with type was found.
     */
    protected Object getValueForInParameter(Object parameter, AbstractRecord translationRow, AbstractRecord modifyRow, AbstractSession session, boolean shouldBind) {
        Object value = parameter;

        // Parameter expressions are used for nesting and correct mapping conversion of the value.
        if (parameter instanceof ParameterExpression) {
            value = ((ParameterExpression)parameter).getValue(translationRow, session);
        } else if (parameter instanceof DatabaseField) {
            DatabaseField field = (DatabaseField)parameter;
            value = translationRow.get(field);
            // Must check for the modify row as well for custom SQL compatibility as only one # is required.
            if (modifyRow != null) {
                if (value == null) {
                    value = modifyRow.get(field);
                }
                if (value != null) {
                    DatabaseField modifyField = modifyRow.getField(field);
                    if (modifyField != null) {
                        if (session.getDatasourcePlatform().shouldUseCustomModifyForCall(modifyField)) {
                            value = session.getDatasourcePlatform().getCustomModifyValueForCall(this, value, modifyField, shouldBind);
                        }
                    }
                }
            }
            if ((value == null) && shouldBind) {
                if (field.getType() != null) {
                    value = field;
                } else if (modifyRow != null) {
                    DatabaseField modifyField = modifyRow.getField(field);
                    if ((modifyField != null) && (modifyField.getType() != null)) {
                        value = modifyField;
                    }
                }
                if (value == null) {
                    DatabaseField translationField = translationRow.getField(field);
                    if (translationField == null){
                        throw QueryException.namedArgumentNotFoundInQueryParameters(field.getName());
                    }
                    if (translationField.getType() != null) {
                        value = translationField;
                    }
                }
            }
        }
        return value;
    }

    /**
     * INTERNAL:
     * Returns value for INOUT parameter. Called by translate and translateSQLString methods.
     */
    protected Object getValueForInOutParameter(Object parameter, AbstractRecord translationRow, AbstractRecord modifyRow, AbstractSession session) {
        // parameter ts an array of two Objects: inParameter and outParameter
        Object inParameter = ((Object[])parameter)[0];
        Object inValue = getValueForInParameter(inParameter, translationRow, modifyRow, session, true);
        Object outParameter = ((Object[])parameter)[1];
        return createInOutParameter(inValue, outParameter, session);
    }

    /**
     * INTERNAL:
     * Returns INOUT parameter. Called by getValueForInOutParameter method.
     * Descendents may override this method.
     */
    protected Object createInOutParameter(Object inValue, Object outParameter, AbstractSession session) {
        Object[] inOut = { inValue, outParameter };
        return inOut;
    }
}
