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
 * Created on June 7, 2005, 5:21 PM
 */

package com.sun.persistence.runtime.sqlstore.sql.update;

import com.sun.persistence.support.JDOFatalInternalException;
import com.sun.org.apache.jdo.util.I18NHelper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.HashMap;

/**
 * Factory for SQLTranscribers. Returns the appropriate transcriber for
 * a given java field type.
 * @author Markus Fuchs
 */
public class SQLTranscriberFactory {

    /**
     * I18N support.
     */
    private static final I18NHelper msg = I18NHelper.getInstance(
            "com.sun.persistence.runtime.Bundle"); //NOI18N

    // TODO: Add transcribers for remaining Object types
    // TODO: Change type comparision not to use Strings
    static Map<String, SQLTranscriber> transcribers = new HashMap<String, SQLTranscriber>();

    static {
        transcribers.put("java.lang.Object",      new ObjectTranscriber());     //NOI18N
        transcribers.put("java.lang.Boolean",     new BooleanTranscriber());    //NOI18N
        transcribers.put("java.lang.Character",   new CharacterTranscriber());  //NOI18N
        transcribers.put("java.lang.Byte",        new ByteTranscriber());       //NOI18N
        transcribers.put("java.lang.Short",       new ShortTranscriber());      //NOI18N
        transcribers.put("java.lang.Integer",     new IntegerTranscriber());    //NOI18N
        transcribers.put("java.lang.Long",        new LongTranscriber());       //NOI18N
        transcribers.put("java.lang.Float",       new FloatTranscriber());      //NOI18N
        transcribers.put("java.lang.Double",      new DoubleTranscriber());     //NOI18N
        transcribers.put("java.lang.String",      new StringTranscriber());     //NOI18N
        transcribers.put("java.util.Date",        new UtilDateTranscriber());   //NOI18N
        transcribers.put("java.sql.Date",         new DateTranscriber());       //NOI18N
        transcribers.put("java.sql.Time",         new TimeTranscriber());       //NOI18N
        transcribers.put("java.sql.Timestamp",    new TimestampTranscriber());  //NOI18N
        transcribers.put("java.math.BigDecimal",  new BigDecimalTranscriber()); //NOI18N
    }

    /**
     * Returns the appropriate transcriber for a given Java type.
     * @param typeName fully qualified type name.
     * @return the appropriate transcriber for the given Java type.
     */
    public static SQLTranscriber getTranscriber(String typeName) {
       SQLTranscriber rc = null;
        rc = transcribers.get(typeName);
        if (rc == null) {
            throw new JDOFatalInternalException(msg.msg(
                    "EXC_UnknownTranscriberType", typeName));  // NOI18N
        }
        return rc;
    }

    private SQLTranscriberFactory() {};

    private static class ObjectTranscriber implements SQLTranscriber {

        /**
         * Binds an <code>java.lang.Object</code> value to the prepared statement
         * <code>ps</code> at index <code>index</code>.
         * @param ps Prepared statement
         * @param index Parameter index
         * @param sqlType Mapped column type
         * @param value <code>java.lang.Object</code> value
         * @throws SQLException if a database access error occurs
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            if (value != null) {
                ps.setObject(index, value);
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }
    }

    private static class BooleanTranscriber implements SQLTranscriber {

        /**
         * Binds a <code>java.lang.Boolean</code> value to the prepared statement
         * at index <code>index</code>.
         * @param ps Prepared statement
         * @param index Parameter index
         * @param sqlType Mapped column type
         * @param value <code>java.lang.Boolean</code> value
         * @throws SQLException if a database access error occurs
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            if (value != null) {
                ps.setBoolean(index, ((Boolean) value).booleanValue());
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }
    }

    private static class CharacterTranscriber implements SQLTranscriber {

        /**
         * Binds a <code>java.lang.Character</code> value to the prepared statement
         * at index <code>index</code>.
         * @param ps Prepared statement
         * @param index Parameter index
         * @param sqlType Mapped column type
         * @param value <code>java.lang.Character</code> value
         * @throws SQLException if a database access error occurs
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            if (value != null) {
                ps.setString(index, ((Character) value).toString());
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }
    }

    private static class ByteTranscriber implements SQLTranscriber {

        /**
         * Binds a <code>java.lang.Byte</code> value to the prepared statement
         * at index <code>index</code>.
         * @param ps Prepared statement
         * @param index Parameter index
         * @param sqlType Mapped column type
         * @param value <code>java.lang.Byte</code> value
         * @throws SQLException if a database access error occurs
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            if (value != null) {
                ps.setByte(index, ((Byte) value).byteValue());
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }
    }

    private static class ShortTranscriber implements SQLTranscriber {

        /**
         * Binds a <code>java.lang.Short</code> value to the prepared statement
         * at index <code>index</code>.
         * @param ps Prepared statement
         * @param index Parameter index
         * @param sqlType Mapped column type
         * @param value <code>java.lang.Short</code> value
         * @throws SQLException if a database access error occurs
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            if (value != null) {
                ps.setShort(index, ((Short) value).shortValue());
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }
    }

    private static class IntegerTranscriber implements SQLTranscriber {

        /**
         * Binds a <code>java.lang.Integer</code> value to the prepared statement
         * at index <code>index</code>.
         * @param ps Prepared statement
         * @param index Parameter index
         * @param sqlType Mapped column type
         * @param value <code>java.lang.Integer</code> value
         * @throws SQLException if a database access error occurs
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            if (value != null) {
                ps.setInt(index, ((Integer) value).intValue());
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }
    }

    private static class LongTranscriber implements SQLTranscriber {

        /**
         * Binds a <code>java.lang.Long</code> value to the prepared statement
         * at index <code>index</code>.
         * @param ps Prepared statement
         * @param index Parameter index
         * @param sqlType Mapped column type
         * @param value <code>java.lang.Long</code> value
         * @throws SQLException if a database access error occurs
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            if (value != null) {
                ps.setLong(index, ((Long) value).longValue());
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }
    }

    private static class FloatTranscriber implements SQLTranscriber {

        /**
         * Binds a <code>java.lang.Float</code> value to the prepared statement
         * at index <code>index</code>.
         * @param ps Prepared statement
         * @param index Parameter index
         * @param sqlType Mapped column type
         * @param value <code>java.lang.Float</code> value
         * @throws SQLException if a database access error occurs
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            if (value != null) {
                ps.setFloat(index, ((Float) value).floatValue());
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }
    }

    private static class DoubleTranscriber implements SQLTranscriber {

        /**
         * Binds a <code>java.lang.Double</code> value to the prepared statement
         * at index <code>index</code>.
         * @param ps Prepared statement
         * @param index Parameter index
         * @param sqlType Mapped column type
         * @param value <code>java.lang.Double</code> value
         * @throws SQLException if a database access error occurs
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            if (value != null) {
                ps.setDouble(index, ((Double) value).doubleValue());
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }
    }

    private static class StringTranscriber implements SQLTranscriber {

        /**
         * Binds a <code>java.lang.String</code> value to the prepared statement
         * at index <code>index</code>.
         * @param ps Prepared statement
         * @param index Parameter index
         * @param sqlType Mapped column type
         * @param value <code>java.lang.String</code> value
         * @throws SQLException if a database access error occurs
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            // TODO: What about Strings mapped to CHAR(x) columns?
            if (value != null) {
                ps.setString(index, (String) value);
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }
    }

    private static class UtilDateTranscriber implements SQLTranscriber {

        /**
         * Binds a <code>java.util.Date</code> value to the prepared statement
         * at index <code>index</code>.
         * @param ps Prepared statement.
         * @param index Parameter index.
         * @param sqlType Mapped column type
         * @param value <code>java.util.Date</code> value.
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            if (value != null) {
                Timestamp timestamp =
                        new Timestamp(((java.util.Date) value).getTime());
                ps.setTimestamp(index, timestamp);
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }
    }

    private static class DateTranscriber implements SQLTranscriber {

        /**
         * Binds a <code>java.sql.Date</code> value to the prepared statement
         * at index <code>index</code>.
         * @param ps Prepared statement
         * @param index Parameter index
         * @param sqlType Mapped column type
         * @param value <code>java.sql.Date</code> value
         * @throws SQLException if a database access error occurs
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            if (value != null) {
                ps.setDate(index, (java.sql.Date) value);
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }

        // TODO: DO WE NEED TO TAKE CARE OF setDate() with the CALENDAR OBJECT
    }

    private static class TimeTranscriber implements SQLTranscriber {

        /**
         * Binds a <code>java.sql.Time</code> value to the prepared statement
         * at index <code>index</code>.
         * @param ps Prepared statement
         * @param index Parameter index
         * @param sqlType Mapped column type
         * @param value <code>java.sql.Time</code> value
         * @throws SQLException if a database access error occurs
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            if (value != null) {
                ps.setTime(index, (java.sql.Time) value);
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }
    }

    private static class TimestampTranscriber implements SQLTranscriber {

        /**
         * Binds a <code>java.sql.Timestamp</code> value to the prepared statement
         * at index <code>index</code>.
         * @param ps Prepared statement
         * @param index Parameter index
         * @param sqlType Mapped column type
         * @param value <code>java.sql.Timestamp</code> value
         * @throws SQLException if a database access error occurs
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            if (value != null) {
                ps.setTimestamp(index, (java.sql.Timestamp) value);
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }
    }

    private static class BigDecimalTranscriber implements SQLTranscriber {

        /**
         * Binds a <code>java.math.BigDecimal</code> value to the prepared statement
         * at index <code>index</code>.
         * @param ps Prepared statement
         * @param index Parameter index
         * @param sqlType Mapped column type
         * @param value <code>java.math.BigDecimal</code> value
         * @throws SQLException if a database access error occurs
         */
        public int store(PreparedStatement ps, int index, int sqlType,
                Object value) throws SQLException {

            if (value != null) {
                ps.setBigDecimal(index, (java.math.BigDecimal) value);
            } else {
                ps.setNull(index, sqlType);
            }
            return index + 1;
        }
    }
}
