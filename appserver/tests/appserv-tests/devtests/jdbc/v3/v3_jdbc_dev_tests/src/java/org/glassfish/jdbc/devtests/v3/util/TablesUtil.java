/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.jdbc.devtests.v3.util;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

/**
 *
 * @author jagadish
 */
public class TablesUtil {

    /**
     * Creates Tables needed to execute JDBC devtests
     * @param ds1
     * @param out
     * @param tableName
     * @param columnName
     */
    public static void createTables(DataSource ds1, PrintWriter out, String tableName, String columnName) {
        Connection con = null;
        Statement stmt = null;
        try {

            con = ds1.getConnection();
            stmt = con.createStatement();
            String query = "create table " + tableName + "(" + columnName + " char(50))";
            stmt.executeUpdate(query);
        } catch (Exception e) {
            HtmlUtil.printException(e, out);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }

            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }
        }
    }
        
    /**
     * Deletes tables used by JDBC devtests.
     * @param ds1
     * @param out
     * @param tableName
     */
    public static void deleteTables(DataSource ds1, PrintWriter out, String tableName) {
        Connection con = null;
        Statement stmt = null;
        try {

            con = ds1.getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate("drop table " + tableName);
        } catch (Exception e) {
            HtmlUtil.printException(e, out);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }

            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }
        }
    }

    /**
     * Insert values into tables needed by JDBC devtests
     * @param ds
     * @param out
     * @param tableName
     * @param content
     */
    public static void insertEntry(DataSource ds, PrintWriter out, String tableName, String content) {
        Connection con = null;
        Statement stmt = null;
        try {
            con = ds.getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate("INSERT INTO " + tableName + " VALUES('" + 
                content + "')");
        } catch (SQLException ex) {
            HtmlUtil.printException(ex, out);
        } finally {
            if(stmt != null) {
                try {
                    stmt.close();
                } catch(SQLException ex) {
                    HtmlUtil.printException(ex, out);
                }
            }
            if(con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    HtmlUtil.printException(ex, out);
                }
            }
        }
    }

    /**
     * Verifies table content by getting the number of rows in it, used by the 
     * JDBC devtests. Returns a true if there are any rows in the table.
     * @param ds1
     * @param out
     * @param tableName
     * @param columnName
     * @param content
     * @return boolean result
     */
    public static boolean verifyTableContent(DataSource ds1, PrintWriter out, String tableName, String columnName, String content) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        boolean result = false;
        try {

            con = ds1.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select count(*) ROW_COUNT from " + tableName + " where " + columnName + " = '" + content + "'");
            if (rs.next()) {
                if (rs.getInt("ROW_COUNT") > 0) {
                    result = true;
                }
            }
        } catch (Exception e) {
            HtmlUtil.printException(e, out);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }

            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }
            return result;
        }
    }
}
