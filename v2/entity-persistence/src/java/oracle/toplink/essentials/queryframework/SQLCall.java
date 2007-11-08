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
package oracle.toplink.essentials.queryframework;

import java.util.Vector;
import java.io.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.internal.expressions.ParameterExpression;

/**
 * <b>Purpose</b>: Used as an abstraction of an SQL call.
 * A call is an SQL string with parameters.
 */
public class SQLCall extends DatabaseCall implements QueryStringCall {
    protected boolean hasCustomSQLArguments;

    /**
     * PUBLIC:
     * Create a new SQL call.
     */
    public SQLCall() {
        super();
        this.hasCustomSQLArguments = false;
    }

    /**
     * PUBLIC:
     * Create a new SQL call.
     */
    public SQLCall(String sqlString) {
        this();
        setSQLString(sqlString);
    }

    /**
     * INTERNAL:
     * Set the data passed through setCustomSQLArgumentType and useCustomSQLCursorOutputAsResultSet methods.
     */
    protected void afterTranslateCustomQuery(Vector updatedParameters, Vector updatedParameterTypes) {
        for (int i = 0; i < getParameters().size(); i++) {
            Integer parameterType = (Integer)getParameterTypes().elementAt(i);
            Object parameter = getParameters().elementAt(i);
            if ((parameterType == MODIFY) || (parameterType == OUT) || (parameterType == OUT_CURSOR) || ((parameterType == IN) && parameter instanceof DatabaseField)) {
                DatabaseField field = (DatabaseField)parameter;
                afterTranslateCustomQueryUpdateParameter(field, i, parameterType, updatedParameters, updatedParameterTypes);
            } else if (parameterType == INOUT) {
                DatabaseField outField = (DatabaseField)((Object[])parameter)[1];
                afterTranslateCustomQueryUpdateParameter(outField, i, parameterType, updatedParameters, updatedParameterTypes);
                if ((((Object[])parameter)[0] instanceof DatabaseField) && (((Object[])parameter)[0] != ((Object[])parameter)[1])) {
                    DatabaseField inField = (DatabaseField)((Object[])parameter)[0];
                    afterTranslateCustomQueryUpdateParameter(inField, i, parameterType, updatedParameters, updatedParameterTypes);
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Set the data passed through setCustomSQLArgumentType and useCustomSQLCursorOutputAsResultSet methods.
     */
    protected void afterTranslateCustomQueryUpdateParameter(DatabaseField field, int index, Integer parameterType, Vector updatedParameters, Vector updatedParameterTypes) {
        for (int j = 0; j < updatedParameters.size(); j++) {
            DatabaseField updateField = (DatabaseField)updatedParameters.elementAt(j);
            if (field.equals(updateField)) {
                Integer updateParameterType = (Integer)updatedParameterTypes.elementAt(j);
                if (updateParameterType == null) {
                    field.setType(updateField.getType());
                } else if (updateParameterType == OUT_CURSOR) {
                    if (parameterType == OUT) {
                        getParameterTypes().setElementAt(OUT_CURSOR, index);
                    } else {
                        throw ValidationException.cannotSetCursorForParameterTypeOtherThanOut(field.getName(), toString());
                    }
                }
                break;
            }
        }
    }

    /**
     * INTERNAL:
     * Used to avoid misiterpreting the # in custom SQL.
     */
    public boolean hasCustomSQLArguments() {
        return hasCustomSQLArguments;
    }

    public boolean isSQLCall() {
        return true;
    }

    public boolean isQueryStringCall() {
        return true;
    }

    /**
     * INTERNAL:
     * Called by prepare method only.
     */
    protected void prepareInternal(AbstractSession session) {
        if (hasCustomSQLArguments()) {
            // hold results of setCustomSQLArgumentType and useCustomSQLCursorOutputAsResultSet methods
            Vector updatedParameters = null;
            Vector updatedParameterTypes = null;
            if (getParameters().size() > 0) {
                updatedParameters = getParameters();
                setParameters(oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance());
                updatedParameterTypes = getParameterTypes();
                setParameterTypes(oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance());
            }

            translateCustomQuery();

            if (updatedParameters != null) {
                afterTranslateCustomQuery(updatedParameters, updatedParameterTypes);
            }
        }

        super.prepareInternal(session);
    }

    /**
     * INTERNAL:
     * Used to avoid misiterpreting the # in custom SQL.
     */
    public void setHasCustomSQLArguments(boolean hasCustomSQLArguments) {
        this.hasCustomSQLArguments = hasCustomSQLArguments;
    }

    /**
     * PUBLIC:
     * This method should only be used with custom SQL:
     * it sets a type to OUT or INOUT parameter (prefixed with ### or #### in custom SQL string).
     */
    public void setCustomSQLArgumentType(String customParameterName, Class type) {
        DatabaseField field = new DatabaseField(customParameterName);
        field.setType(type);
        getParameters().add(field);
        getParameterTypes().add(null);
    }

    /**
     * Set the SQL string.
     */
    public void setSQLString(String sqlString) {
        setSQLStringInternal(sqlString);
    }

    /**
     * INTERNAL:
     * All values are printed as ? to allow for parameter binding or translation during the execute of the call.
     */
    public void appendTranslationParameter(Writer writer, ParameterExpression expression, DatabasePlatform platform) throws IOException {
        try {
            platform.writeParameterMarker(writer, expression);
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
        getParameters().addElement(expression);
        getParameterTypes().addElement(TRANSLATION);
    }

    /**
     * PUBLIC:
     * This method should only be used with custom SQL:
     * Used for Oracle result sets through procedures.
     * It defines OUT parameter (prefixed with ### in custom SQL string)
     * as a cursor output.
     */
    public void useCustomSQLCursorOutputAsResultSet(String customParameterName) {
        DatabaseField field = new DatabaseField(customParameterName);
        getParameters().add(field);
        getParameterTypes().add(OUT_CURSOR);
        setIsCursorOutputProcedure(true);
    }
}
