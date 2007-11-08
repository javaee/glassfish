/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * ConnectionImpl.java
 *
 * Create on March 3, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.connection;

import java.math.BigDecimal;
import java.sql.*;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;
import java.io.InputStream;
import java.io.Reader;



/**
 * This class implements the <code>java.sql.CallableStatement</code>
 * interface, which is part of the JDBC API. You should use
 * the <code>java.sql.CallableStatement</code> interface as an object
 * type instead of this class.
 */
public class CallableStatementImpl extends PreparedStatementImpl
        implements CallableStatement {
    //
    // Keep references to corresponding JDBC Connection and
    // CallableStatement objects.
    //
    // @param  conn   ConnectionImpl
    // @param  cstmt  CallableStatement
    //
    public CallableStatementImpl(ConnectionImpl conn,
                                 CallableStatement cstmt) {
        super();
        this.conn = conn;
        this.stmt = (Statement) cstmt;
    }

    //----------------------------------------------------------------------
    // Wrapper methods for JDBC CallableStatement:

    public void registerOutParameter(int parameterIndex, int sqlType)
            throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            cstmt.registerOutParameter(parameterIndex, sqlType);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void registerOutParameter(int parameterIndex, int sqlType, int scale)
            throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            cstmt.registerOutParameter(parameterIndex, sqlType, scale);
        } catch (SQLException se) {
            throw se;
        }
    }

    public boolean wasNull() throws SQLException {
        CallableStatement cstmt = (CallableStatement) this.stmt;

        try {
            return (cstmt.wasNull());
        } catch (SQLException se) {
            throw se;
        }
    }

    public String getString(int parameterIndex) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getString(parameterIndex));
        } catch (SQLException se) {
            throw se;
        }
    }

    public boolean getBoolean(int parameterIndex) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getBoolean(parameterIndex));
        } catch (SQLException se) {
            throw se;
        }
    }

    public byte getByte(int parameterIndex) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getByte(parameterIndex));
        } catch (SQLException se) {
            throw se;
        }
    }

    public short getShort(int parameterIndex) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getShort(parameterIndex));
        } catch (SQLException se) {
            throw se;
        }
    }

    public int getInt(int parameterIndex) throws SQLException {
        CallableStatement cstmt = (CallableStatement) this.stmt;

        try {
            return (cstmt.getShort(parameterIndex));
        } catch (SQLException se) {
            throw se;
        }
    }

    public long getLong(int parameterIndex) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getLong(parameterIndex));
        } catch (SQLException se) {
            throw se;
        }
    }

    public float getFloat(int parameterIndex) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getFloat(parameterIndex));
        } catch (SQLException se) {
            throw se;
        }
    }

    public double getDouble(int parameterIndex) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getDouble(parameterIndex));
        } catch (SQLException se) {
            throw se;
        }
    }

    public BigDecimal getBigDecimal(int parameterIndex, int scale)
            throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getBigDecimal(parameterIndex, scale));
        } catch (SQLException se) {
            throw se;
        }
    }

    public byte[] getBytes(int parameterIndex) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getBytes(parameterIndex));
        } catch (SQLException se) {
            throw se;
        }
    }

    public java.sql.Date getDate(int parameterIndex) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getDate(parameterIndex));
        } catch (SQLException se) {
            throw se;
        }
    }

    public java.sql.Time getTime(int parameterIndex) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getTime(parameterIndex));
        } catch (SQLException se) {
            throw se;
        }
    }

    public java.sql.Timestamp getTimestamp(int parameterIndex)
            throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getTimestamp(parameterIndex));
        } catch (SQLException se) {
            throw se;
        }
    }

    //----------------------------------------------------------------------
    // Advanced features:


    public Object getObject(int parameterIndex) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getObject(parameterIndex));
        } catch (SQLException se) {
            throw se;
        }
    }

    //--------------------------JDBC 2.0-----------------------------

    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getBigDecimal(parameterIndex));
        } catch (SQLException se) {
            throw se;
        }
    }

    public Object getObject(int i, java.util.Map map) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getObject(i, map));
        } catch (SQLException se) {
            throw se;
        }
    }

    public Ref getRef(int i) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getRef(i));
        } catch (SQLException se) {
            throw se;
        }
    }

    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

// JDK1.2FCS

    public void registerOutParameter(int i, int j, String str) throws SQLException {
        /* Comment out for now.
		try
		{
			// CallableStatement cstmt = (CallableStatement) this.stmt;
			// cstmt.registerOutParameter(i, j, str);
		}
		catch (SQLException se)
		{
			throw se;
		}
		*/
    }


    //-------------Begin New methods added in JDBC 3.0 --------------
    public void registerOutParameter(String parameterName,int sqlType) throws SQLException {

        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            cstmt.registerOutParameter(parameterName,sqlType);
        } catch (SQLException se) {
            throw se;
        }

    }

    public void registerOutParameter(String parameterName,int sqlType,int scale)
                                                                    throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            cstmt.registerOutParameter(parameterName,sqlType,scale);
        } catch (SQLException se) {
            throw se;
        }

    }

    public void registerOutParameter(String parameterName,int sqlType,String typeName)
                                                                     throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            cstmt.registerOutParameter(parameterName,sqlType,typeName);
        } catch (SQLException se) {
            throw se;
        }

    }

    public java.net.URL getURL(int parameterIndex) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setURL(String parameterName,URL val) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setNull(String parameterName,int sqlType) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setBoolean(String parameterName, boolean x) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setByte(String parameterName, byte x) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setShort(String parameterName, short x) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setInt(String parameterName, int x) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setLong(String parameterName, long x) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setFloat(String parameterName, float x) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setDouble(String parameterName, double x) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setString(String parameterName, String x) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setBytes(String parameterName, byte[] x) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setDate(String parameterName, Date x) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setTime(String parameterName, Time x) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setAsciiStream(String parameterName, InputStream x, int length)
                                                                throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setBinaryStream(String parameterName, InputStream x, int length)
                                                                throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setObject(String parameterName, Object x, int targetSqlType, int scale)
                                                                 throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setObject(String parameterName, Object x, int targetSqlType)
                                                                 throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setObject(String parameterName, Object x) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setCharacterStream(String parameterName, Reader reader, int length)
                                                                   throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setTimestamp(String parameterName, Timestamp x, Calendar cal)
                                                                    throws SQLException {

        throw new UnsupportedOperationException();
    }

    public void setNull(String parameterName, int sqlType, String typeName)
                                                                    throws SQLException {
        throw new UnsupportedOperationException();
    }

    public String getString(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getString(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public boolean getBoolean(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getBoolean(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public byte getByte(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getByte(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public short getShort(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getShort(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public int getInt(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getInt(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public long getLong(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getLong(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public float getFloat(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getFloat(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public double getDouble(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getDouble(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public byte[] getBytes(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getBytes(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public Date getDate(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getDate(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public Time getTime(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getTime(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public Timestamp getTimestamp(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getTimestamp(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public Object getObject(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getObject(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getBigDecimal(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public Object getObject(String parameterName, Map map) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getObject(parameterName,map));
        } catch (SQLException se) {
            throw se;
        }
    }

    public Ref getRef(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getRef(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public Blob getBlob(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getBlob(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public Clob getClob(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getClob(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public Array getArray(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getArray(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getDate(parameterName,cal));
        } catch (SQLException se) {
            throw se;
        }
    }

    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getTime(parameterName,cal));
        } catch (SQLException se) {
            throw se;
        }
    }

    public Timestamp getTimestamp(String parameterName, Calendar cal)
                                                             throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getTimestamp(parameterName,cal));
        } catch (SQLException se) {
            throw se;
        }
    }

    public URL getURL(String parameterName) throws SQLException {
        try {
            CallableStatement cstmt = (CallableStatement) this.stmt;
            return (cstmt.getURL(parameterName));
        } catch (SQLException se) {
            throw se;
        }
    }

    //-------------End New methods added in JDBC 3.0 --------------

}
