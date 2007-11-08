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
 * StatementImpl.java
 *
 * Create on March 3, 2000
 */

package com.sun.jdo.spi.persistence.support.sqlstore.connection;

import java.math.BigDecimal;
import java.sql.*;


/**
 * This class implements the <code>java.sql.PreparedStatement</code>
 * interface, which is part of the JDBC API. You should use
 * the <code>java.sql.PreparedStatement</code> interface as an object
 * type instead of this class.
 */
// CHANGES
//     23-Dec-1997
//         Created (psb)
//

public class PreparedStatementImpl
        extends StatementImpl
        implements PreparedStatement {
    //
    // Create a new PreparedStatementImpl object.
    //
    public PreparedStatementImpl() {
        super();
        this.conn = null;
        this.stmt = null;
    }

    //
    // Create a new PreparedStatementImpl object and keep references to
    // corresponding JDBC Connection and PreparedStatement objects.
    //
    // @param  conn   ConnectionImpl
    // @param  pstmt   JDBC PreparedStatement
    //
    public PreparedStatementImpl(ConnectionImpl conn,
                                 PreparedStatement pstmt) {
        super();
        this.conn = conn;
        this.stmt = (Statement) pstmt;
    }

    //----------------------------------------------------------------------
    // Wrapper methods for JDBC PreparedStatement:

    public ResultSet executeQuery() throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            this.conn.checkXact();
            return (pstmt.executeQuery());
        } catch (SQLException se) {
            throw se;
        }
    }

    public int executeUpdate() throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            this.conn.checkXact();
            return (pstmt.executeUpdate());
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setNull(parameterIndex, sqlType);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setBoolean(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setByte(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setShort(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setInt(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setLong(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setFloat(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setDouble(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x)
            throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setBigDecimal(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setString(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setBytes(int parameterIndex, byte x[]) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setBytes(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setDate(int parameterIndex, java.sql.Date x)
            throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setDate(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setTime(int parameterIndex, java.sql.Time x)
            throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setTime(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setTimestamp(int parameterIndex, java.sql.Timestamp x)
            throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setTimestamp(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setTimestamp(int parameterIndex, java.sql.Timestamp x,
                             java.util.Calendar cal)
            throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setTimestamp(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setTime(int parameterIndex, java.sql.Time x,
                        java.util.Calendar cal)
            throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setTime(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setArray(int i, Array x)
            throws SQLException {
    }

    public void setClob(int i, Clob x)
            throws SQLException {
    }

    public ResultSetMetaData getMetaData()
            throws SQLException {
        return (null);
    }

    public Clob getClob(int i)
            throws SQLException {
        return (null);
    }

    public void setDate(int parameterIndex, Date x,
                        java.util.Calendar cal)
            throws SQLException {
    }

    public Array getArray(int i)
            throws SQLException {
        return (null);
    }

    public Time getTime(int parameterIndex, java.util.Calendar cal)
            throws SQLException {
        return (null);
    }

    public Blob getBlob(int i)
            throws SQLException {
        return (null);
    }

    public void setBlob(int i, Blob x) throws SQLException {
    }

    public Date getDate(int parameterIndex, java.util.Calendar cal)
            throws SQLException {
        return (null);
    }

    public Timestamp getTimestamp(int parameterIndex,
                                  java.util.Calendar cal)
            throws SQLException {
        return (null);
    }

    public void setAsciiStream(int parameterIndex,
                               java.io.InputStream x,
                               int length)
            throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setAsciiStream(parameterIndex, x, length);
        } catch (SQLException se) {
            throw se;
        }
    }

    /**
     * @deprecated
     */
    public void setUnicodeStream(int parameterIndex,
                                 java.io.InputStream x,
                                 int length)
            throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setUnicodeStream(parameterIndex, x, length);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setBinaryStream(int parameterIndex,
                                java.io.InputStream x,
                                int length)
            throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setBinaryStream(parameterIndex, x, length);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void clearParameters() throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.clearParameters();
        } catch (SQLException se) {
            throw se;
        }
    }

    //----------------------------------------------------------------------
    // Advanced features:

    public void setObject(int parameterIndex,
                          Object x, int targetSqlType, int scale)
            throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setObject(parameterIndex, x, targetSqlType, scale);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setObject(int parameterIndex,
                          Object x, int targetSqlType)
            throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setObject(parameterIndex, x, targetSqlType);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setObject(parameterIndex, x);
        } catch (SQLException se) {
            throw se;
        }
    }

    public boolean execute() throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            this.conn.checkXact();
            return (pstmt.execute());
        } catch (SQLException se) {
            throw se;
        }
    }

    //--------------------------JDBC 2.0-----------------------------

    public void addBatch() throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.addBatch();
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setCharacterStream(int parameterIndex,
                                   java.io.Reader reader,
                                   int length) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setCharacterStream(parameterIndex, reader, length);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setRef(int i, Ref x) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        try {
            pstmt.setRef(i, x);
        } catch (SQLException se) {
            throw se;
        }
    }

/* BETA3

	public void setBlobLocator (int i, BlobLocator x) throws SQLException
	{
		PreparedStatement pstmt = (PreparedStatement)this.stmt;

		try
		{
			pstmt.setBlobLocator(i, x);
		}
		catch (SQLException se)
		{
			throw se;
		}
	}

	public void setClobLocator (int i, ClobLocator x) throws SQLException
	{
		PreparedStatement pstmt = (PreparedStatement)this.stmt;

		try
		{
			pstmt.setClobLocator(i, x);
		}
		catch (SQLException se)
		{
			throw se;
		}
	}

	public void setStructLocator (int i, StructLocator x) throws SQLException
	{
		PreparedStatement pstmt = (PreparedStatement)this.stmt;

		try
		{
			pstmt.setStructLocator(i, x);
		}
		catch (SQLException se)
		{
			throw se;
		}
	}

	public void setArrayLocator (int i, ArrayLocator x) throws SQLException
	{
		PreparedStatement pstmt = (PreparedStatement)this.stmt;

		try
		{
			pstmt.setArrayLocator(i, x);
		}
		catch (SQLException se)
		{
			throw se;
		}
	}

BETA3 */

// JDK1.2FCS

    public void setNull(int i, int j, String str) throws SQLException {
        PreparedStatement pstmt = (PreparedStatement) this.stmt;

        /* Comment out for now.
		try
		{
			pstmt.setNull(i, j, str);
		}
		catch (SQLException se)
		{
			throw se;
		}
		*/
    }

    //-------------Begin New methods added in JDBC 3.0 --------------
    public void setURL(int parameterIndex,java.net.URL x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {

        throw new UnsupportedOperationException();
    }

    //-------------End New methods added in JDBC 3.0 --------------
}
