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

package org.glassfish.jdbc.devtests.v3.test;

import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;
import org.glassfish.jdbc.devtests.v3.util.TablesUtil;

/**
 * Tests if statement-timeout (query-timeout) is being set on all types of
 * statements created in an application : Statement, PreparedStatement, 
 * CallableStatement.
 * 
 * Assumes that statement-timeout attribute is set on the pool to 30.
 * 
 * @author shalini
 */
public class StatementTimeoutTest implements SimpleTest {
    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds, PrintWriter out) {
        
        //Create required tables and insert entry into them
        String tableName = "CUSTOMER";
        String columnName = "name";
        TablesUtil.createTables(ds, out, tableName, columnName);        
        TablesUtil.insertEntry(ds, out, tableName, "abcd");
        TablesUtil.insertEntry(ds, out, tableName, "pqrs");

        try {
            if (statementTest(ds, out)) {
                resultsMap.put("statement-test", true);
            }else{
                resultsMap.put("statement-test", false);
            }
        } catch (Exception e) {
            resultsMap.put("statement-test", false);
        }
        
        try {
            if (preparedStatementTest(ds, out, tableName)) {
                resultsMap.put("prepared-statement-test", true);
            }else{
                resultsMap.put("prepared-statement-test", false);
            }
        } catch (Exception e) {
            resultsMap.put("prepared-statement-test", false);
        }

        try {
            if (callableStatementTest(ds, out, tableName)) {
                resultsMap.put("callable-statement-test", true);
            }else{
                resultsMap.put("callable-statement-test", false);
            }
        } catch (Exception e) {
            resultsMap.put("callable-statement-test", false);
        }

        TablesUtil.deleteTables(ds, out, tableName);
        HtmlUtil.printHR(out);
        return resultsMap;
    }

    /**
     * Tests if statement-timeout is being set on a CallableStatement created 
     * in the application.
     * @param ds
     * @param out
     * @param tableName
     * @return boolean result
     */
    private boolean callableStatementTest(DataSource ds, PrintWriter out, 
            String tableName) {
        boolean result = false;
        Connection conFromDS = null;
        CallableStatement stmt = null;
        out.println("<h4> Callable Statement Test </h4>");
        try {
            out.println("<br> Getting a connection");
            conFromDS = ds.getConnection();
            out.print("<br> Preparing a CallableStatement");
            stmt = conFromDS.prepareCall("select * from " + tableName);
            
            out.println("<br> getQueryTimeout() on the statement");
            if(stmt.getQueryTimeout() == 30) {
                out.println("<br> Timeout = 30");
                result = true;
            }
        } catch (SQLException sqlEx) {
            HtmlUtil.printException(sqlEx, out);
        } finally {
            try {
                if(stmt != null) {
                    stmt.close();
                }
            } catch(SQLException ex) {
                HtmlUtil.printException(ex, out);
            }
            try {
                if(conFromDS != null) {
                    conFromDS.close();
                }
            } catch(SQLException ex) {
                HtmlUtil.printException(ex, out);
            }
            out.println("<br> Test result : " + result);
            return result;
        }                
    }

    /**
     * Tests if statement-timeout is being set on a PreparedStatement
     * created in the application.
     * @param ds
     * @param out
     * @param tableName
     * @return boolean result
     */
    private boolean preparedStatementTest(DataSource ds, PrintWriter out, 
            String tableName) {
        boolean result = false;
        Connection conFromDS = null;
        PreparedStatement stmt = null;
        out.println("<h4> Prepared Statement Test </h4>");
        
        try {
            out.println("<br> Getting a connection...");
            conFromDS = ds.getConnection();
            out.println("<br>Creating a PreparedStatement query");
            stmt = conFromDS.prepareStatement("select * from " + tableName);
            
            out.println("<br>getQueryTimeout() on the statement");
            if(stmt.getQueryTimeout() == 30) {
                out.println("<br>Timeout = 30");
                result = true;
            }
        } catch (SQLException sqlEx) {
            HtmlUtil.printException(sqlEx, out);
        } finally {
            try {
                if(stmt != null) {
                    stmt.close();
                }
            } catch(SQLException ex) {
                HtmlUtil.printException(ex, out);
            }
            try {
                if(conFromDS != null) {
                    conFromDS.close();
                }
            } catch(SQLException ex) {
                HtmlUtil.printException(ex, out);
            }
            out.println("<br> Test result : " + result);
            return result;
        }        
    }

    /**
     * Tests if statement-timeout is being set on a Statement
     * created in the application
     * @param ds
     * @param out
     * @return boolean result
     */
    private boolean statementTest(DataSource ds, PrintWriter out) {
        boolean result = false;
        Connection conFromDS = null;
        Statement stmt = null;
        out.println("<h4> Statement Test </h4>");
        try {
            out.println("<br>Getting a connection...");
            conFromDS = ds.getConnection();
            out.print("<br>Creating a Statement");
            stmt = conFromDS.createStatement();
            
            out.println("<br> Getting the queryTimeout on the statement");
            if(stmt.getQueryTimeout() == 30) {
                out.println("<br>Timeout = 30");
                result = true;
            }
        } catch (SQLException sqlEx) {
            HtmlUtil.printException(sqlEx, out);
        } finally {
            try {
                if(stmt != null) {
                    stmt.close();
                }
            } catch(SQLException ex) {
                HtmlUtil.printException(ex, out);
            }
            try {
                if(conFromDS != null) {
                    conFromDS.close();
                }
            } catch(SQLException ex) {
                HtmlUtil.printException(ex, out);
            }
            out.println("<br> Test result : " + result);
            return result;
        }
    }
}
