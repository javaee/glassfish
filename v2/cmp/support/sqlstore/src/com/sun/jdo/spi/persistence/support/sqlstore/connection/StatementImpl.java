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

import java.sql.*;

/**
 * This class implements the <code>java.sql.Statement</code>
 * interface, which is part of the JDBC API. You should use
 * the <code>java.sql.Statement</code> interface as an object
 * type instead of this class.
 */

public class StatementImpl implements Statement {
    ConnectionImpl conn;
    Statement stmt;

    //
    // Create a new StatementImpl object.
    //
    public StatementImpl() {
        super();
        this.conn = null;
        this.stmt = null;
    }

    /**
     * @ForteInternal
     * Create a new StatementImpl object.  Keep references to
     * corresponding JDBC Connection and Statement objects.
     *
     * @param  conn   ConnectionImpl
     * @param  stmt   JDBC Statement
     */
    public StatementImpl(ConnectionImpl conn, Statement stmt) {
        super();
        this.conn = conn;
        this.stmt = stmt;
    }

    //----------------------------------------------------------------------
    // Wrapper methods for JDBC Statement:

    public ResultSet executeQuery(String sql) throws SQLException {
        try {
            this.conn.checkXact();
            return (this.stmt.executeQuery(sql));
        } catch (SQLException se) {
            throw se;
        }
    }

    public int executeUpdate(String sql) throws SQLException {
        try {
            this.conn.checkXact();
            return (this.stmt.executeUpdate(sql));
        } catch (SQLException se) {
            throw se;
        }
    }

    public void close() throws SQLException {
        try {
            this.stmt.close();
        } catch (SQLException se) {
            throw se;
        }
    }

    public int getMaxFieldSize() throws SQLException {
        try {
            return (this.stmt.getMaxFieldSize());
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setMaxFieldSize(int max) throws SQLException {
        try {
            this.stmt.setMaxFieldSize(max);
        } catch (SQLException se) {
            throw se;
        }
    }

    public int getMaxRows() throws SQLException {
        try {
            return (this.stmt.getMaxRows());
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setMaxRows(int max) throws SQLException {
        try {
            this.stmt.setMaxRows(max);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        try {
            this.stmt.setEscapeProcessing(enable);
        } catch (SQLException se) {
            throw se;
        }
    }

    public int getQueryTimeout() throws SQLException {
        try {
            return (this.stmt.getQueryTimeout());
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        try {
            this.stmt.setQueryTimeout(seconds);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void cancel() throws SQLException {
        try {
            this.stmt.cancel();
        } catch (SQLException se) {
            throw se;
        }
    }

    public SQLWarning getWarnings() throws SQLException {
        try {
            return (this.stmt.getWarnings());
        } catch (SQLException se) {
            throw se;
        }
    }

    public void clearWarnings() throws SQLException {
        try {
            this.stmt.clearWarnings();
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setCursorName(String name) throws SQLException {
        try {
            this.stmt.setCursorName(name);
        } catch (SQLException se) {
            throw se;
        }
    }

    public boolean execute(String sql) throws SQLException {
        try {
            this.conn.checkXact();
            return (this.stmt.execute(sql));
        } catch (SQLException se) {
            throw se;
        }
    }

    public ResultSet getResultSet() throws SQLException {
        try {
            return (this.stmt.getResultSet());
        } catch (SQLException se) {
            throw se;
        }
    }

    public int getUpdateCount() throws SQLException {
        try {
            return (this.stmt.getUpdateCount());
        } catch (SQLException se) {
            throw se;
        }
    }

    public boolean getMoreResults() throws SQLException {
        try {
            return (this.stmt.getMoreResults());
        } catch (SQLException se) {
            throw se;
        }
    }

    public int getResultSetConcurrency() throws SQLException {
        try {
            return (this.stmt.getResultSetConcurrency());
        } catch (SQLException se) {
            throw se;
        }
    }

    //--------------------------JDBC 2.0-----------------------------

    public void setFetchDirection(int direction) throws SQLException {
        try {
            this.stmt.setFetchDirection(direction);
        } catch (SQLException se) {
            throw se;
        }
    }

    public int getFetchDirection() throws SQLException {
        try {
            return (this.stmt.getFetchDirection());
        } catch (SQLException se) {
            throw se;
        }
    }

    public void setFetchSize(int rows) throws SQLException {
        try {
            this.stmt.setFetchSize(rows);
        } catch (SQLException se) {
            throw se;
        }
    }

    public int getFetchSize() throws SQLException {
        try {
            return (this.stmt.getFetchSize());
        } catch (SQLException se) {
            throw se;
        }
    }

    public int getResultSetType()
            throws SQLException {
        try {
            return (this.stmt.getResultSetType());
        } catch (SQLException se) {
            throw se;
        }
    }

    public void addBatch(String sql) throws SQLException {
        try {
            this.stmt.addBatch(sql);
        } catch (SQLException se) {
            throw se;
        }
    }

    public void clearBatch() throws SQLException {
        try {
            this.stmt.clearBatch();
        } catch (SQLException se) {
            throw se;
        }
    }

    public int[] executeBatch() throws SQLException {
        try {
            this.conn.checkXact();
            return (this.stmt.executeBatch());
        } catch (SQLException se) {
            throw se;
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            return (this.stmt.getConnection());
        } catch (SQLException se) {
            throw se;
        }
    }


    //-------------Begin New methods added in JDBC 3.0 --------------
    public boolean getMoreResults(int current) throws SQLException {
        try {
            return (this.stmt.getMoreResults(current));
        } catch (SQLException se) {
            throw se;
        }
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        try {
            return (this.stmt.getGeneratedKeys());
        } catch (SQLException se) {
            throw se;
        }
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        try {
            return (this.stmt.executeUpdate(sql,autoGeneratedKeys) );
        } catch (SQLException se) {
            throw se;
        }
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        try {
            return (this.stmt.executeUpdate(sql,columnIndexes));
        } catch (SQLException se) {
            throw se;
        }
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        try {
            return (this.stmt.executeUpdate(sql,columnNames));
        } catch (SQLException se) {
            throw se;
        }
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        try {
            return (this.stmt.execute(sql,autoGeneratedKeys));
        } catch (SQLException se) {
            throw se;
        }
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        try {
            return (this.stmt.execute(sql,columnIndexes));
        } catch (SQLException se) {
            throw se;
        }
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        try {
            return (this.stmt.execute(sql,columnNames));
        } catch (SQLException se) {
            throw se;
        }
    }

    public int getResultSetHoldability() throws SQLException {
        try {
            return (this.stmt.getResultSetHoldability());
        } catch (SQLException se) {
            throw se;
        }
    }

    //-------------End New methods added in JDBC 3.0 --------------

    /*
     * This method unwraps given Statement and return the Statement from
     * JDBC driver.
     */
    public Statement unwrapStatement() {
        return this.stmt;
    }
}
