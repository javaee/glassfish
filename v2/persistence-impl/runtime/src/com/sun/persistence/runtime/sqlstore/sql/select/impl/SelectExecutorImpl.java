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


package com.sun.persistence.runtime.sqlstore.sql.select.impl;

import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.persistence.runtime.connection.SQLConnector;
import com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager;
import com.sun.persistence.runtime.sqlstore.sql.impl.InputParameter;
import com.sun.persistence.runtime.sqlstore.sql.select.SelectExecutor;
import com.sun.persistence.runtime.query.QueryInternal;
import com.sun.persistence.runtime.LogHelperSQLStore;
import com.sun.persistence.support.JDODataStoreException;
import com.sun.persistence.utility.logging.Logger;
import com.sun.org.apache.jdo.util.I18NHelper;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

/**
 * SelectExecutorImpl
 * @author Mitesh Meswani
 */
class SelectExecutorImpl implements SelectExecutor {
    /**
     * sql string of the query
     */
    private String statementText;

    /**
     * ResultElement for this query
     */
    private List<ResultElement> resultElements;

    /**
     * Params for this query
     */
    private List<InputParameter> inputParams;

    /**
     * The logger
     */
    private static Logger logger = LogHelperSQLStore.getLogger();

    /**
     * The sql logger
     */
    private static Logger sqlLogger = LogHelperSQLStore.getSqlLogger();

    /** I18N support */
    private final static I18NHelper msg =
        I18NHelper.getInstance("com.sun.persistence.runtime.Bundle"); //NOI18N


    public SelectExecutorImpl(String sqlStatement,
            List<ResultElement> resultElements,
            List<InputParameter> inputParams) {
        this.statementText = sqlStatement;
        this.resultElements = resultElements;
        this.inputParams = inputParams;
    }

    /**
     * @inheritDoc
     */
    public List execute(PersistenceManagerInternal pm, QueryInternal userParams) {
        SQLStoreManager srm = (SQLStoreManager) pm.getStoreManager();
        SQLConnector cr = (SQLConnector) srm.getConnector();
        Connection conn = cr.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        //TODO: Log the sql text as done in tp here
        if (sqlLogger.isLoggable() ) {
            boolean logParamValues = sqlLogger.isLoggable(Logger.FINER);
            sqlLogger.fine(formatSqlText(statementText, userParams,
                    logParamValues));
        }

        List result;
        try {
            ps = conn.prepareStatement(statementText);
            //Bind params
            int i = 1;
            for (InputParameter inputParam : inputParams) {
                bindInputParameter(ps, i++, inputParam.getValue(userParams));
            }
            rs = ps.executeQuery();
            result = getResult(pm, rs);
        } catch (SQLException e) {
            //TODO: Get the message string from bundle
            throw new JDODataStoreException(
                    "Error while executing query " +
                    formatSqlText(statementText, userParams, true) +
                    "Please look at the nested exception for more details",
                    e);
        } catch (JDODataStoreException e) {
            //TODO: Get the message string from bundle
            throw new JDODataStoreException(
                    "Error while executing query " +
                    formatSqlText(statementText, userParams, true) +
                    "Please look at the nested exception for more details",
                    e);
        } finally {
            try {
                if (conn != null)
                {
                    if (ps != null) {
                        if (rs != null) {
                            rs.close();
                        }
                        ps.close();
                    }
                    //let the connector close connection
                    cr.releaseConnection();
                }
            } catch (SQLException e) {
                //Ignore the exception
            }
        }
        return result;
    }

    /**
     * Get result from given result set with results bound to given pm
     * @param pm The pm to which the result objects will be bound
     * @param rs The result set from which result needs to be fetched
     * @return A List containing results fetched from the result set
     * @throws SQLException
     */
    private List getResult(PersistenceManagerInternal pm, ResultSet rs)
            throws SQLException {
        ArrayList result = new ArrayList();

        // When a query has more than one projection, each row of the
        // result will be Object[] such that each element of the array
        // corresponds to a projection
        // We treat queries with single projection specially so that
        // each row of the result contains the single projection
        Object[] resultRow = new Object[resultElements.size()];

        boolean singleProjection = (resultElements.size() == 1);
        while (rs.next()) {
            int i = 0;
            for (ResultElement resultElement : resultElements) {
                resultRow[i++] = resultElement.getResult(pm, rs);
            }
            // Single projections are treated specially.
            result.add(singleProjection ? resultRow[0] : resultRow);
        }

        return result;
    }

    /**
     * Binds given <code>val</code> to given <code>ps</code> at given
     * <code>index</code>
     * @param ps the given <code>PreparedStatement</code>
     * @param index the given <code>index</code>
     * @param val the given <code>value</code>.
     * @throws SQLException
     */
    private void bindInputParameter(PreparedStatement ps, int index,
            Object val) throws SQLException {
        // According to the spec, the behavior in case of null input parameter
        // is undefined. In production, we will bind such parameters as
        // setObject(index, null).
        //TODO : Add a logging message at WARNING level for above case

        if (val instanceof Number) {
            Number number = (Number) val;
            if (number instanceof Integer) {
                ps.setInt(index, number.intValue());
            } else if (number instanceof Long) {
                ps.setLong(index, number.longValue());
            } else if (number instanceof Short) {
                ps.setShort(index, number.shortValue());
            } else if (number instanceof Byte) {
                ps.setByte(index, number.byteValue());
            } else if (number instanceof Double) {
                ps.setDouble(index, number.doubleValue());
            } else if (number instanceof Float) {
                ps.setFloat(index, number.floatValue());
            } else if (number instanceof BigDecimal) {
                ps.setBigDecimal(index, (BigDecimal) number);
            } else if (number instanceof BigInteger) {
                ps.setBigDecimal(index, new BigDecimal((BigInteger) number));
            }
        } else if (val instanceof String) {
            ps.setString(index, (String) val);
        } else if (val instanceof Boolean) {
            ps.setBoolean(index, ((Boolean) val).booleanValue());
        } else if (val instanceof java.util.Date) {
            if (val instanceof java.sql.Date) {
                ps.setDate(index, (java.sql.Date) val);
            } else if (val instanceof Time) {
                ps.setTime(index, (Time) val);
            } else if (val instanceof Timestamp) {
                ps.setTimestamp(index, (Timestamp) val);
            } else {
                Timestamp timestamp = new Timestamp(
                        ((java.util.Date) val).getTime());
                ps.setTimestamp(index, timestamp);
            }
        } else if (val instanceof Character) {
            ps.setString(index, val.toString());
        } else if (val instanceof byte[]) {
            byte[] ba = (byte[]) val;
            ps.setBinaryStream(index, new ByteArrayInputStream(ba), ba.length);
        } else if (val instanceof Blob) {
            ps.setBlob(index, (Blob) val);
        } else if (val instanceof Clob) {
            ps.setClob(index, (Clob) val);
        } else {
            ps.setObject(index, val);
        }
    }
    String getStatementText() {
        return statementText;
    }

    /**
     * Returns text for the sql being executed along with param values
     * @param sqlText The sql text that will be passed to database
     * @param userParams paramater information
     * @param printParamValues A flag indincating whether parameters should
     * be printed or not.
     * @return text for the sql being executed
     */
    private String formatSqlText(String sqlText, QueryInternal userParams,
            boolean printParamValues) {
        StringBuffer str = new StringBuffer();

        str.append(msg.msg("sqlstore.sql.generator.statement.sqlStatement") );   //NOI18N
        str.append("<").append(sqlText).append("> "); // NOI18N

        if (printParamValues) {
            boolean paramProcessed = false;
            for (InputParameter inputParam : inputParams) {
                Object value = inputParam.getValue(userParams);
                if (paramProcessed) {
                    str.append(", "); // NOI18N
                } else {
                    str.append(msg.msg(
                            "sqlstore.sql.generator.statement.withinputvalues")); // NOI18N
                    paramProcessed = true;
                }

                if (value == null) {
                    str.append("<null>"); // NOI18N
                } else {
                    str.append(value.getClass().getName());
                    str.append(":"); // NOI18N
                    str.append(value.toString());
                }
            }
            if(!paramProcessed) {
                str.append(msg.msg(
                        "sqlstore.sql.generator.statement.withnoinputvalues")); // NOI18N
            }
        }

        return str.toString();
    }


}
