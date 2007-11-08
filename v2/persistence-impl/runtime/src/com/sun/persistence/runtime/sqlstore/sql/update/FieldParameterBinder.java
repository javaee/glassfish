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
 * Created on April 21, 2005, 11:53 AM
 */

package com.sun.persistence.runtime.sqlstore.sql.update;

import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.state.FieldManager;
import com.sun.org.apache.jdo.state.StateManagerInternal;
import com.sun.org.apache.jdo.util.I18NHelper;

import com.sun.persistence.runtime.LogHelperSQLStore;
import com.sun.persistence.support.JDOFatalInternalException;
import com.sun.persistence.support.spi.PersistenceCapable;
import com.sun.persistence.utility.logging.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;


/**
 * Binds the fields of an entity to a sql statement. This class basically stores
 * the association btw. fields of a persistence capable instance and columns of
 * the statement being bound. It has two parts, dealing with either static or
 * dynamic information. The static information is represented by
 * FieldParameterBinder and the dynamic part by the inner classes implementing
 * the {@link FieldManager} interface. Foreign key relationships and embedded
 * fields of the entity are handled by {@link NullableFieldParameterBinder}.
 * This class can not handle relationships mapped to join tables.
 * @author Pramod Gopinath
 * @author Markus Fuchs
 */
public class FieldParameterBinder {

    /**
     * Is set to true in case we are processing where clause and false for
     * set clause.
     */
    private boolean whereClause;

    /**
     * Index of the next parameter in the sql statement.
     */
    protected int index;

    /**
     * Array of the field numbers that will be bound to the sql statement.
     * This array contains the JDO absolute field number for each field that
     * will be bound. Each field is uniquely identified by field number.
     */
    protected int[] fields;

    /**
     * Array of sql column types associated to object typed fields. This array
     * is "sparse", i.e. contains valid entries only for fields of Object type.
     * At index i the array contains the sql type of the column at binding index i+1.
     * For foreign key fields, the array is not sparse and the array length
     * matches exactly the number of parameters to be bound for this field.
     */
    protected int[] sqlTypes;

    /**
     * Sparse array of {@link NullableFieldParameterBinder}s. The array only
     * contains entries for the entity's foreign key relationships or embedded
     * fields.
     */
    protected NullableFieldParameterBinder[] fieldParameterBinders;

    /**
     * Delimiter used to append fields into the sql buffer associated with
     * printing the sql statement.
     */
    static String DELIMITER = ", ";  // NOI18N

    /** Runtime logger instance. */
    private static Logger logger = LogHelperSQLStore.getLogger();

    /** Used in logging messages. */
    private static final String className = FieldParameterBinder.class.getName();

    /** I18N helper. */
    private static final I18NHelper msg = I18NHelper.getInstance(
            "com.sun.persistence.runtime.Bundle", // NOI18N
            FieldParameterBinder.class.getClassLoader());

   /**
    * Creates a new instance of FieldParameterBinder. Instantiates the fields,
    * sqlType, and NullableFieldParameterBinder array using the number
    * of fields value.
    * @param whereClause true in case we are processing the where clause and
    * false for the set clause.
    * @param numOfFields (maximum) number of fields to be bound.
    */
    public FieldParameterBinder(boolean whereClause, int numOfFields) {
       this.whereClause      = whereClause;
       fields                = new int[numOfFields];
       sqlTypes              = new int[numOfFields];
       fieldParameterBinders = new NullableFieldParameterBinder[numOfFields];
    }

    /**
     * Adds the field number into the array of fields. This array is used by
     * the double dispatch process and we'll get a callback for each field
     * identified by the field number in the array. During the callback the
     * value of the field is fetched from the StateManager and bound to the
     * prepared statement at the current binding index. Foreign key
     * relationships and embedded field are handled recursively in the same way
     * by {@link NullableFieldParameterBinder}.
     * @param fieldNum the JDO absolute field number.
     */
    public void addField(int fieldNum) {
        fields[index++] = fieldNum;
    }

    /**
     * Adds the field number into the array of fields and remembers the sql type
     * of the mapped column. This method is used for fields of Object type,
     * e.g. wrappers, String, Date, foreign key, or embedded fields. During the
     * double dispatch process, the get the appropriate SQLTranscriber for the
     * field type. If the field is null, the transcriber will use the column
     * type to bind the null value into the prepared statement. State fields can
     * only be mapped to one single column. The implemetation does not allow a
     * state fields to be mapped to multiple columns.
     * @param fieldNum the JDO absolute fields number of the object typed field.
     * @param sqlType type of the column to be bound. As fields of Object type
     * can be null, the <code>sqlType</code> would be used to bind null values
     * to the statement.
     */
    public void addField(int fieldNum, int sqlType) {
        // Note: The order of the following statements is significant, as
        // addField increases the index member variable.
        sqlTypes[index] = sqlType;
        addField(fieldNum);
    }

    /**
     * Adds the field number into the array of fields and adds the
     * <code>fieldParameterBinder</code> into the sparse array of {@link
     * NullableFieldParameterBinder}s. Each entry corresponds to either a
     * foreign key relationship field or an embedded field. Relationships mapped
     * to join tables are <em>not</em> handled here. The
     * NullableFieldParameterBinder is used as part of double dispatch process
     * to bind the values that are associated to the field to the prepared
     * statement.
     * @param fieldNum the JDO absolute field number of the foreign key
     * relationship or embedded field for which a NullableFieldParameterBinder
     * is added.
     * @param fieldParameterBinder an instance of NullableFieldParameterBinder
     * for the embedded or foreign key field.
     */
    public void addFieldParameterBinder(int fieldNum,
            NullableFieldParameterBinder fieldParameterBinder) {
        fieldParameterBinders[fieldNum] = fieldParameterBinder;
        addField(fieldNum);
    }

    /**
     * Binds the parameters accociated to the <code>StateManager sm</code> to
     * the prepared statement. The binding is done  during the double dispatch
     * process, using a {@link BindingFieldManager}.
     * @param sm <code>StateManager</code> representing the instance being bound
     * to the statement.
     * @param ps the prepared statement.
     * @param bindingIndex the starting parameter binding index.
     * @return The next parameter index to be bound.
     */
    public int bindFieldValues(StateManagerInternal sm, PreparedStatement ps,
            int bindingIndex) {

        boolean debug = logger.isLoggable(Logger.FINEST);

        if (debug) {
            logger.entering(className, "bindFieldValues"); // NOI18N
        }

        boolean identifying = getIdentifyingProperty(sm);
        BindingFieldManager fm = new BindingFieldManager(
                sm.getPersistenceManager(), identifying, ps, bindingIndex);
        sm.provideFields(getFieldNumbersForDoubleDispatch(fields, index),
                fm, identifying);

        if (debug) {
            logger.exiting(className, "bindFieldValues"); // NOI18N
        }

        return fm.bindingIndex;
    }

    /**
     * Returns the comma separated list of parameters bound for the
     * <code>StateManager sm</code>. The parameter log is created by
     * double dispatch using a <code>LoggingFieldManager</code>.
     * @param sm <code>StateManager</code> representing the instance being bound.
     * @return The comma separated list of parameters bound for this entity as a
     * String.
     */
    public String logFieldValues(StateManagerInternal sm) {

        boolean identifying = getIdentifyingProperty(sm);
        LoggingFieldManager fm =
                new LoggingFieldManager(sm.getPersistenceManager(), identifying);
        sm.provideFields(getFieldNumbersForDoubleDispatch(fields, index),
                fm, identifying);

        return fm.getParameterLog();
    }

    //
    // Helper methods
    //

    /**
     * We must be getting the values from the before image for fields bound to
     * the WHERE clause if required a before image is available from this state
     * manager.
     * @param sm the <code>StateManager</code> to be bound.
     * @return <code>true</code>, if the double dispatch process should get
     *         values from the before image, false otherwise.
     */
    private boolean getIdentifyingProperty(StateManagerInternal sm) {
        return (whereClause ? sm.isBeforeImageRequired() : false);
    }

    /**
     * Creates a copy of the <code>fields</code> array, containing only field
     * numbers actually used. As FieldParameterBinders correspond to entities,
     * the created <code>fields</code> array might be longer than the number of
     * fields actually bound, as not all fields might be loaded/modified. The
     * entity's relationship or embedded fields are represented by
     * NullableFieldParameterBinders. In this case the length of the
     * <code>fields</code> array matches the size of the relationship key.
     * @param fields int array containing field numbers.
     * @param length maximum index actually set.
     * @return new array of field numbers.
     */
    private static int[] getFieldNumbersForDoubleDispatch(int[] fields,
            int length) {
        assert length <= fields.length : "length must be <= fields.length"; // NOI18N
        int[] rc = fields;
        if (length < fields.length) {
            rc = new int[length];
            System.arraycopy(fields, 0, rc, 0, length);
        }
        return rc;
    }

    /**
     * Appends each value being bound to a string buffer. When printing the sql
     * statement, we can also log this buffer of the values that were bound.
     * @param sb logging string buffer.
     * @param value the value to be appended to the string buffer.
     */
    protected static void appendToLoggingString(StringBuffer sb, Object value) {
        assert sb != null : "Logging buffer must not be null!"; // NOI18N
        if (sb.length() > 0) {
            sb.append(DELIMITER);
        }
        sb.append(value);
    }

    /**
     * This class represents the {@link FieldManager} binding values to a
     * <code>PreparedStatement</code>. This is the dynamic part of the binding
     * process, using double dispatch. Each field value returned by double
     * dispatch from the {@link PersistenceCapable} is bound to the next
     * parameter index of the prepared statement. The statement is constructed
     * to hold the parameters in the same order as the values are returned by
     * the double dispatch process. The correspondence btw. fields and input
     * parameters is one of the reasons, why fields can not be mapped to
     * multiple columns.
     * <p />All fetch methods defined in the {@link FieldManager} interface
     * throw an <code>UnsupportedOperationException</code>.
     */
    class BindingFieldManager extends BasicFieldManager {

        /**
         * The <code>PreparedStatement<//code> into which the values would be
         * bound as part of double dispatch process.
         */
        PreparedStatement ps;

        /**
         * The statement parameter index for which the last value was bound.
         */
        int bindingIndex;


        /**
         * Creates a new <code>BindingFieldManager</code> instance. The
         * identifying property will be <code>false</code> for the SET- clause
         * of a <code>PreparedStatement</code>. The identifying property of the
         * FieldManager used for the WHERE- clause is determined from the
         * <code>StateManager</code> being bound.
         * @param pm the <code>PersistenceManager</code> representing the
         * current transaction context.
         * @param identifying boolean value indicating if values will be
         * obtained from the current or before image during the double dispatch
         * process.
         * @param ps the <code>PreparedStatement</code>.
         * @param bindingIndex the starting position from where field values
         * have to be bound into the prepared statement.
         */
        BindingFieldManager(PersistenceManagerInternal pm, boolean identifying,
                PreparedStatement ps, int bindingIndex) {

            super(pm, identifying);
            this.ps           = ps;
            this.bindingIndex = bindingIndex;
        }

        /** @inheritDoc */
        public void storeBooleanField(int fieldNum, boolean value) {
            try {
                ps.setBoolean(bindingIndex++, value);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        /** @inheritDoc */
        public void storeCharField(int fieldNum, char value) {
            try {
                ps.setString(bindingIndex++, String.valueOf(value));
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        /** @inheritDoc */
        public void storeByteField(int fieldNum, byte value) {
            try {
                ps.setByte(bindingIndex++, value);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        /** @inheritDoc */
        public void storeShortField(int fieldNum, short value) {
            try {
                ps.setShort(bindingIndex++, value);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        /** @inheritDoc */
        public void storeIntField(int fieldNum, int value) {
            try {
                ps.setInt(bindingIndex++, value);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        /** @inheritDoc */
        public void storeLongField(int fieldNum, long value){
            try {
                ps.setLong(bindingIndex++, value);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        /** @inheritDoc */
        public void storeFloatField(int fieldNum, float value) {
            try {
                ps.setFloat(bindingIndex++, value);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        /** @inheritDoc */
        public void storeDoubleField(int fieldNum, double value) {
            try {
                ps.setDouble(bindingIndex++, value);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        /** @inheritDoc */
        public void storeStringField(int fieldNum, String value) {
            SQLTranscriber ts =
                    SQLTranscriberFactory.getTranscriber("java.lang.String"); // NOI18N
            try {
                // Prepared statements are bound starting with binding index 1,
                // the appropriate column type is stored at binding index - 1.
                bindingIndex = ts.store(ps, bindingIndex, sqlTypes[bindingIndex - 1], value);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        /** @inheritDoc */
        public void storeObjectField(int fieldNum, Object value) {
            NullableFieldParameterBinder fpb = fieldParameterBinders[fieldNum];

            if (fpb == null) {
                transcribeObjectField(value);
            } else {
                if (value == null) {
                    bindingIndex = fpb.bindNull(ps, bindingIndex);
                } else if (value instanceof PersistenceCapable) {
                    storeReferenceField(value, fpb);
                } else if (value instanceof Collection) {
                    // TODO: Collection handling
                    throw new UnsupportedOperationException("Not yet implemented");
                } else {
                    throw new JDOFatalInternalException(msg.msg(
                            "update.fieldParameterBinder.notBound", //NOI18N
                            value, fieldNum));
                }
            }
        }

        //
        // Helper methods
        //

        /**
         * Binds a state field of Object type to the <code>PreparedStatement</code>
         * by calling the appropriate {@link SQLTranscriber}.
         * @param value the value of an object typed state field.
         */
        private void transcribeObjectField(Object value) {
            SQLTranscriber ts;
            if (value == null) {
                // Null values can be bound w/ any transcriber,
                // we only need the sql column type.
                ts = SQLTranscriberFactory.getTranscriber("java.lang.Object"); // NOI18N
            } else {
                // Get the transcriber appropriate for the field type.
                ts = SQLTranscriberFactory.getTranscriber(value.getClass().getName());
            }

            try {
                // Prepared statements are bound starting with binding index 1,
                // so the appropriate sql type is stored at binding index - 1.
                bindingIndex = ts.store(ps, bindingIndex, sqlTypes[bindingIndex - 1], value);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * Binds a relationship field to the <code>PreparedStatement</code> by
         * storing foreign key values. The values for the foreign key columns
         * are obtained by double dispatch from the foreign
         * <code>StateManager</code>. Relationships mapped to join tables can
         * not be handled this way.
         * @param value the value of a reference relationship field.
         * @param fpb the <code>NullableFieldParameterBinder</code> binding the
         * foreign key values.
         */
        private void storeReferenceField(Object value, FieldParameterBinder fpb) {
            // Get the SM from the value which in this case is a PC instance.
            StateManagerInternal foreignSM =
                    pm.findStateManager((PersistenceCapable) value);
            assert foreignSM != null : "Related state manager must not be null!"; // NOI18N
            bindingIndex = fpb.bindFieldValues(foreignSM, ps, bindingIndex);
        }
    }

    /**
     * This class represents a <code>FieldManager</code> used to log the
     * parameter values that are (were previously) bound to the
     * <code>PreparedStatement</code> into a <code>StringBuffer</code>.
     * All fetch methods defined in the {@link FieldManager} interface
     * throw an <code>UnsupportedOperationException</code>.
     */
    class LoggingFieldManager extends BasicFieldManager implements FieldManager {

        /**
         * <code>StringBuffer</code> for the values of the fields being
         * bound. Will be used to display the actual parameters that are bound
         * to the prepared statement.
         */
        StringBuffer parameterLog;

        /**
         * Creates a <code>FieldManager</code> used to log the parameter values
         * that were (previously) bound to the <code>PreparedStatement</code>
         * into a <code>StringBuffer</code>. It is used in dynamic part related
         * to a <code>FieldParameterBinder</code> during the double dispatch
         * process. Creates a non-identifying FieldManager for the SET- clause
         * of the prepared statement. The identifying property of the
         * <code>FieldManager</code> used for the WHERE- clause is determined
         * from the current <code>StateManager</code>.
         * @param pm the current <code>PersistenceManager</code>.
         * @param identifying the value indicating if values will be obtained
         * from the current or before image during the double dispatch  process.
         */
        LoggingFieldManager(PersistenceManagerInternal pm, boolean identifying) {
            super(pm, identifying);
            this.parameterLog = new StringBuffer();
        }

        /**
         * Returns the logging string containing the parameter values.
         * @return the logging string containing the parameter values.
         */
        public String getParameterLog() {
            return parameterLog.toString();
        }

        /** @inheritDoc */
        public void storeBooleanField(int fieldNum, boolean value) {
            appendToLoggingString(parameterLog, Boolean.valueOf(value));
        }

        /** @inheritDoc */
        public void storeCharField(int fieldNum, char value) {
            appendToLoggingString(parameterLog, Character.valueOf(value));
        }

        /** @inheritDoc */
        public void storeByteField(int fieldNum, byte value) {
            appendToLoggingString(parameterLog, Byte.valueOf(value));
        }

        /** @inheritDoc */
        public void storeShortField(int fieldNum, short value) {
            appendToLoggingString(parameterLog, Short.valueOf(value));
        }

        /** @inheritDoc */
        public void storeIntField(int fieldNum, int value) {
            appendToLoggingString(parameterLog, Integer.valueOf(value));
        }

        /** @inheritDoc */
        public void storeLongField(int fieldNum, long value){
            appendToLoggingString(parameterLog, Long.valueOf(value));
        }

        /** @inheritDoc */
        public void storeFloatField(int fieldNum, float value) {
            appendToLoggingString(parameterLog, Float.valueOf(value));
        }

        /** @inheritDoc */
        public void storeDoubleField(int fieldNum, double value) {
            appendToLoggingString(parameterLog, Double.valueOf(value));
        }

        /** @inheritDoc */
        public void storeStringField(int fieldNum, String value) {
            appendToLoggingString(parameterLog, value);
        }

        /** @inheritDoc */
        public void storeObjectField(int fieldNum, Object value) {
            NullableFieldParameterBinder fpb = fieldParameterBinders[fieldNum];

            if (fpb == null) {
                appendToLoggingString(parameterLog, value);
            } else {
                if (value == null) {
                    fpb.logNull(parameterLog);
                } else if (value instanceof PersistenceCapable) {
                    logReferenceField(value, fpb);
                } else if (value instanceof Collection) {
                    // TODO: Collection handling
                    throw new UnsupportedOperationException("Not yet implemented");
                } else {
                    throw new JDOFatalInternalException(msg.msg(
                            "update.fieldParameterBinder.notBound", // NOI18N
                            value, fieldNum));
                }
            }
        }

        //
        // Helper method
        //

        /**
         * Logs the values bound to the <code>PreparedStatement</code> for a
         * reference relationship field. The reference is stored by storing the
         * foreign key values, obtained by double dispatch from the foreign
         * <code>StateManager</code>.
         * @param value the value of a reference relationship field.
         * @param fpb <code>NullableFieldParameterBinder</code> binding the
         * foreign key values.
         */
        private void logReferenceField(Object value,
                NullableFieldParameterBinder fpb) {
            // Get the SM from the value which in this case is a PC class.
            StateManagerInternal foreignSM =
                    pm.findStateManager((PersistenceCapable) value);
            assert foreignSM != null : "Related state manager must not be null!"; // NOI18N
            if (parameterLog.length() > 0) {
                parameterLog.append(DELIMITER);
            }
            parameterLog.append(fpb.logFieldValues(foreignSM));
        }
    }

    /**
     * Basic implemention of the {@link FieldManager} interface. All methods
     * throw an <code>UnsupportedOperationException</code>. Stores the current
     * <code>PersistenceManager</code> and the identifying property of this
     * <code>FieldManager</code>, which will be used by the concrete
     * implementation classes {@link BindingFieldManager} and
     * {@link LoggingFieldManager}.
     * @see com.sun.org.apache.jdo.state.FieldManager
     */
    abstract class BasicFieldManager implements FieldManager {

        /**
         * Current <code>PersistenceManager</code>.
         */
        PersistenceManagerInternal pm;

        /**
         * Indicates if values will be obtained from the current or before image
         * during the double dispatch process.
         */
        boolean identifying;

        /**
         * Creates a new <code>BasicFieldManager</code> instance.
         * @param pm the current <code>PersistenceManager</code>.
         * @param identifying boolean value indicating if values will be obtained
         * from the current or before image during the double dispatch process.
         */
        BasicFieldManager(PersistenceManagerInternal pm, boolean identifying) {
            this.pm          = pm;
            this.identifying = identifying;
        }

        /** @inheritDoc */
        public void storeBooleanField(int fieldNum, boolean value) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public boolean fetchBooleanField(int fieldNum) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public void storeCharField(int fieldNum, char value) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public char fetchCharField(int fieldNum) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public void storeByteField(int fieldNum, byte value) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public byte fetchByteField(int fieldNum) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public void storeShortField(int fieldNum, short value) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public short fetchShortField(int fieldNum) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public void storeIntField(int fieldNum, int value) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public int fetchIntField(int fieldNum) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public void storeLongField(int fieldNum, long value){
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public long fetchLongField(int fieldNum) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public void storeFloatField(int fieldNum, float value) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public float fetchFloatField(int fieldNum) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public void storeDoubleField(int fieldNum, double value) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public double fetchDoubleField(int fieldNum) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public void storeStringField(int fieldNum, String value) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public String fetchStringField(int fieldNum) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public void storeObjectField(int fieldNum, Object value) {
            throw new UnsupportedOperationException();
        }

        /** @inheritDoc */
        public Object fetchObjectField(int fieldNum) {
            throw new UnsupportedOperationException();
        }
    }

}
