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

import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.support.JDODataStoreException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class represents a result element that is accessed from a resultset
 * using index.
 * @author Mitesh Meswani
 */
public class IndexedResultColumnElement implements ResultColumnElement {
    protected int columnIndex;

    private static final I18NHelper i18nHelper = I18NHelper.getInstance(
            "com.sun.persistence.runtime.Bundle", // NOI18N
            IndexedResultColumnElement.class.getClassLoader());

    IndexedResultColumnElement(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    /**
     * @inheritDoc
     */
    public boolean getBooleanResult(ResultSet rs) {
        try {
            return rs.getBoolean(columnIndex);
        } catch (SQLException e) {
            throw newJdbcErrorException(e);
        }
    }

    /**
     * @inheritDoc
     */
    public char getCharResult(ResultSet rs) {
        try {
            String str = null;
            str = rs.getString(columnIndex);
            if (str != null) {
                char retVal = '\0';
                if (str.length() >= 1) {
                    retVal = str.charAt(0);
                }
                return retVal;
            }
            //TODO: Properly format and I18N this message
            throw new JDODataStoreException(
                    "Can not obtain char result from a null string");
        } catch (SQLException e) {
            throw newJdbcErrorException(e);
        }
    }

    /**
     * @inheritDoc
     */
    public byte getByteResult(ResultSet rs) {
        try {
            return rs.getByte(columnIndex);
        } catch (SQLException e) {
            throw newJdbcErrorException(e);
        }
    }

    /**
     * @inheritDoc
     */
    public short getShortResult(ResultSet rs) {
        try {
            return rs.getShort(columnIndex);
        } catch (SQLException e) {
            throw newJdbcErrorException(e);
        }
    }

    /**
     * @inheritDoc
     */
    public int getIntResult(ResultSet rs) {
        try {
            return rs.getInt(columnIndex);
        } catch (SQLException e) {
            throw newJdbcErrorException(e);
        }
    }

    /**
     * @inheritDoc
     */
    public long getLongResult(ResultSet rs) {
        try {
            return rs.getLong(columnIndex);
        } catch (SQLException e) {
            throw newJdbcErrorException(e);
        }
    }

    /**
     * @inheritDoc
     */
    public float getFloatResult(ResultSet rs) {
        try {
            return rs.getFloat(columnIndex);
        } catch (SQLException e) {
            throw newJdbcErrorException(e);
        }
    }

    /**
     * @inheritDoc
     */
    public double getDoubleResult(ResultSet rs) {
        try {
            return rs.getDouble(columnIndex);
        } catch (SQLException e) {
            throw newJdbcErrorException(e);
        }
    }

    /**
     * @inheritDoc
     */
    public String getStringResult(ResultSet rs) {
        try {
            return rs.getString(columnIndex);
        } catch (SQLException e) {
            throw newJdbcErrorException(e);
        }
    }

    /**
     * @inheritDoc
     */
    public Object getObjectResult(ResultSet rs) {
        try {
            return rs.getObject(columnIndex);
        } catch (SQLException e) {
            throw newJdbcErrorException(e);
        }
    }

    private JDODataStoreException newJdbcErrorException(SQLException e) {
        //TODO: Need to fix the message string to reflect correct error
        //We will not have the sqlText for the statement available here as we
        //had in tp. Need to come up with correct message for the exception.
        //The message needs to use columnIndex to give user a hint of where
        //things went wrong
        String exceptionMessage = i18nHelper.msg(
                "core.persistencestore.jdbcerror"); // NOI18N
        return new JDODataStoreException(exceptionMessage, e);
    }
}
