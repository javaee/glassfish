/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

/*
 * Created on April 21, 2005, 4:37 PM
 */


package com.sun.persistence.runtime.sqlstore.sql.update;

import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.runtime.LogHelperSQLStore;
import com.sun.persistence.support.JDODataStoreException;
import com.sun.persistence.utility.logging.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * This class represents static information used for insert/update/delete
 * operations bound to SQL statements. Static information does not change
 * btw. multiple executions of the same statement and includes:
 * <ul>
 * <li>The SQL statement text</li>
 * <li>The association btw. fields and statement parameters</li>
 * </ul>
 * The actual parameter values are likely to change for each execution and
 * therefor considered dynamic information.
 * @author Pramod Gopinath
 * @author Markus Fuchs
 */
public class BaseDMLStatement {

    /**
     * Number of managed fields in the entity accociated with this statement.
     */
    private int numOfFields;

    /**
     * Final SQL statement text.
     */
    private String sqlString;

    /**
     * Field parameter binder for the SET clause.
     */
    private FieldParameterBinder setClauseBinder;

    /**
     * Field parameter binder for the WHERE clause.
     */
    private FieldParameterBinder whereClauseBinder;

    /** Runtime logger instance. */
    private static Logger logger = LogHelperSQLStore.getLogger();

    /** Logger for SQL statements. */
    private static Logger sqlLogger = LogHelperSQLStore.getSqlLogger();

    /** Used in logging messages. */
    private static final String className = BaseDMLStatement.class.getName();

    /** I18N support. */
    private static final I18NHelper msg = I18NHelper.getInstance(
            "com.sun.persistence.runtime.Bundle"); //NOI18N

    /**
     * Creates a new instance of BaseDMLStatement.
     * @param numOfFields Number of managed fields in the entity accociated
     * with this statement.
     */
    public BaseDMLStatement(int numOfFields) {
        this.numOfFields = numOfFields;
    }

    /**
     * Set the SQL string for this DML statement.
     * @param sqlString the SQL string for this statement.
     */
    public void setSQLString(String sqlString) {
        this.sqlString = sqlString;
    }

    /**
     * Adds a state field to the statement. This method is called for state
     * fields of Java primitive types.
     * @param fieldNum JDO absolute field number. The field number identifies
     * the field and will be used during the double dispatch process.
     * @param whereClause If <code>true</code> this field is bound to the WHERE
     * clause of the SQL statement. If <code>false</code> this field is bound
     * to the SET clause.
     */
    public void addField(int fieldNum, boolean whereClause) {
        if (!whereClause) {
            getSetClauseBinder().addField(fieldNum);
        } else {
            getWhereClauseBinder().addField(fieldNum);
        }
    }

    /**
     * Adds a transcriber for a state field of Object type. The transcriber will
     * be used to bind the value to the SQL statement. For object typed fields,
     * we need to remember the sql column type in order to bind a possible null
     * value if the field is null.
     * @param fieldNum JDO absolute field number. The field number identifies
     * the field and will be used during the double dispatch process.
     * @param sqlType Mapped column type.
     * @param whereClause If <code>true</code> this field is bound to the WHERE
     * clause of the SQL statement. If <code>false</code> this field is bound
     * to the SET clause.
     */
    public void addField(int fieldNum, int sqlType, boolean whereClause) {
        if (!whereClause) {
            getSetClauseBinder().addField(fieldNum, sqlType);
        } else {
            getWhereClauseBinder().addField(fieldNum, sqlType);
        }
    }

    /**
     * Adds a {@link NullableFieldParameterBinder} handling an embedded or
     * relationship field. The relationship must be mapped to a foreign key and
     * this entity must be defined as the relationship's owning side for
     * changes to be effective. Relationship updates done on the non-owning
     * side will not be stored! Relationships mapped to join tables are not
     * handled here.
     * <p />Note: The handling of embedded fields is not yet implemented!
     * @param fieldNum JDO absolute field number. The field number identifies
     * the field and will be used during the double dispatch process.
     * @param fpb Field parameter binder used for a relationship or embedded
     * field. Relationships must not be mapped to join tables and the field must
     * be defined as owning side in the meta data.
     * @param whereClause If <code>true</code> this field is bound to the WHERE
     * clause of the SQL statement. If <code>false</code> this field is bound
     * to the SET clause.
     */
    public void addFieldParameterBinder(int fieldNum,
            NullableFieldParameterBinder fpb, boolean whereClause) {

        if (!whereClause) {
            getSetClauseBinder().addFieldParameterBinder(fieldNum, fpb);
        } else {
            getWhereClauseBinder().addFieldParameterBinder(fieldNum, fpb);
        }
    }

    /**
     * Binds the values from the instance represented by the state manager to
     * the prepared statement before execution.
     * @param sm the <code>StateManager</code> that would be used to get the
     * field values into the prepared statement.
     * @param ps prepared statement.
     * @throws RuntimeException
     * Exceptions during parameter binding are wrapped into RuntimeExceptions.
     */
    private void bindParameters(StateManagerInternal sm, PreparedStatement ps) {

        final boolean debug = logger.isLoggable(Logger.FINEST);
        int bindingIndex = 1;

        if (debug) {
            logger.entering(className, "bindParameters"); // NOI18N
        }

        if (setClauseBinder != null) {
            bindingIndex = setClauseBinder.bindFieldValues(sm, ps,
                    bindingIndex);
        }
        if (whereClauseBinder != null) {
            // This bindingIndex can be safely ignored
            bindingIndex = whereClauseBinder.bindFieldValues(sm, ps,
                    bindingIndex);
        }

        if (debug) {
            logger.exiting(className, "bindParameters"); // NOI18N
        }
    }

    /**
     * Logs the values from the instance represented by the state manager into
     * a string.
     * @param sm the <code>StateManager</code> that would be used to get the
     * field values into the prepared statement.
     * @throws RuntimeException
     * Exceptions during parameter binding are wrapped into RuntimeExceptions.
     * @return A string containing the values from the instance represented by
     * the state manager.
     */
    private String logParameters(StateManagerInternal sm) {
        StringBuffer rs = new StringBuffer();
        if (setClauseBinder != null) {
            rs.append(setClauseBinder.logFieldValues(sm));
        }
        if (rs.length() > 0) {
            rs.append(FieldParameterBinder.DELIMITER);
        }
        if (whereClauseBinder != null) {
            rs.append(whereClauseBinder.logFieldValues(sm));
        }
        return rs.toString();
    }

    /**
     * Executes the statement in the database. Statement batching could be
     * achived by batching statements here, leaving the execution of the
     * statements to the {@link ConnectionHandler} at flush time.
     * @param sm the StateManager used to bind the values to the statement.
     * @param connection database connection.
     */
    public void execute(StateManagerInternal sm, Connection connection) {
        final boolean debug = logger.isLoggable(Logger.FINEST);
        final boolean sqlDebug = sqlLogger.isLoggable();

        if (debug) {
            logger.entering(className, "execute"); // NOI18N
        }

        Throwable ex = null;
        try {
            ConnectionHandler handler = new ConnectionHandler(connection);
            PreparedStatement ps = handler.getPreparedStatement(sqlString);

            bindParameters(sm, ps);
            ps.execute();
        } catch (RuntimeException re) {
            // SQLExceptions during parameter binding are wrapped into
            // RuntimeExceptions, so the exception might contain the original
            // problem as a nested exception.
            if (re.getCause() != null) {
                ex = re.getCause();
            } else {
                ex = re;
            }
        } catch (Throwable t) {
           ex = t;
        }

        if (ex != null || sqlDebug) {
            // Determine the values that were bound to the statement above.
            String parameterLog = logParameters(sm);
            if (ex != null) {
                throw newJDODataStoreException(ex,
                        formatSQLText(sqlString, parameterLog));
            } else {
                sqlLogger.fine(formatSQLText(sqlString, parameterLog));
            }
        }

        if (debug) {
            logger.exiting(className, "execute"); // NOI18N
        }
    }

    /**
     * Returns the field parameter binder for the SET clause. The SET clause
     * binder is lazily initialized, as e.g. delete statements don't have a SET
     * clause.
     * @return The field parameter binder for the SET clause.
     */
    private FieldParameterBinder getSetClauseBinder() {
        if (setClauseBinder == null) {
            setClauseBinder =
                    new FieldParameterBinder(false, numOfFields);
        }
        return setClauseBinder;
    }

    /**
     * Returns the field parameter binder for the WHERE clause. The WHERE clause
     * binder is lazily initialized, as e.g. insert statements don't have a
     * WHERE clause.
     * @return The field parameter binder for the WHERE clause.
     */
    private FieldParameterBinder getWhereClauseBinder() {
        if (whereClauseBinder == null) {
            whereClauseBinder =
                    new FieldParameterBinder(true, numOfFields);
        }
        return whereClauseBinder;
    }

    /**
     * Returns a string containing the text of the SQL statement about to be
     * executed and the input values for the placeholders.
     * @param sqlText Specifies the text of the SQL statement to be executed.
     * @param parameters Holds the input values used for the SQL statement.
     * @return The SQL text and the input values formatted into a printable
     * string.
     */
    private static String formatSQLText(String sqlText, String parameters) {
        StringBuffer rc = new StringBuffer();
        rc.append(
                msg.msg("update.BaseDMLStatement.bindParameters.sqlStatement")); // NOI18N
        rc.append("<").append(sqlText).append("> "); // NOI18N

        if (parameters != null) {
            rc.append(
                    msg.msg("update.BaseDMLStatement.bindParameters.withInputValues")). // NOI18N
                    append(" ").append(parameters); // NOI18N
        } else {
            rc.append(
                    msg.msg("update.BaseDMLStatement.bindParameters.withNoInputValues")); // NOI18N
        }
        return rc.toString();
    }

    /**
     * Wraps the passed <code>Exception</code> into a
     * <code>JDODataStoreException</code>.
     * @param t root cause exception.
     * @param sqlText executed sql statement.
     * @return <code>Exception</code> wrapped into a
     * <code>JDODataStoreException</code>
     */
    private static JDODataStoreException newJDODataStoreException(
            Throwable t, String sqlText) {
        String exceptionMessage = msg.msg(
                "core.persistencestore.jdbcerror", sqlText); // NOI18N
        return new JDODataStoreException(exceptionMessage, t);
    }

}
