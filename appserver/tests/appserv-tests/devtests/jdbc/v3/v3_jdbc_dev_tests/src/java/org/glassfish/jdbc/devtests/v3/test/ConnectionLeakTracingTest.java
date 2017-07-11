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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;
import org.glassfish.jdbc.devtests.v3.util.TablesUtil;

/**
 * Tests Connection Leak tracing/ Leak reclaim.
 * 
 * Assumes that steady-pool-size=1, max-pool-size=1, 
 * connection-leak-timeout-in-seconds = 10, connection-leak-reclaim = true
 * attributes are set in the pool configuration.
 * @author shalini
 */
public class ConnectionLeakTracingTest implements SimpleTest {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();    
    
    public Map<String, Boolean> runTest(DataSource ds, PrintWriter out) {
        //create CUSTOMER table needed for this test
        String tableName = "CUSTOMER";
        createTables(ds, out, tableName);
        
        out.println("<h4> Connection Leak Tracing Test </h4>");
        
        for(int i=0; i<3; i++) {
            try {
                out.println("<br> Trial " + i);
                if (!connLeakTracingTest1(ds, out, tableName)) {
                    resultsMap.put("conn-leak-tracing-test1", false);
                    out.println("<br>connLeakTracingTest has failed");
                    break;
                }
                Thread.sleep(20000);
            } catch (InterruptedException ex) {
                HtmlUtil.printException(ex, out);
                resultsMap.put("conn-leak-tracing-test1", false);
            } catch (Exception ex) {
                resultsMap.put("conn-leak-tracing-test1", false);
            }
        }
        out.println("<br> Test result : true");
        resultsMap.put("conn-leak-tracing-test1", true);
        
        //Delete the CUSTOMER table created.
        TablesUtil.deleteTables(ds, out, tableName);
        
        HtmlUtil.printHR(out);
        return resultsMap;
    }

    private boolean connLeakTracingTest1(DataSource ds, PrintWriter out, 
            String tableName) {
        Connection conn = null;
        boolean passed = true;
        try {
            out.println("<br>Getting a connection...");
            conn = ds.getConnection();
            out.println("<br> Inserting an entry into the table");
            insertEntry(conn, tableName);
            out.println("<br> Emptying table...");
            emptyTable(conn, tableName);
        } catch (Exception ex) {
            HtmlUtil.printException(ex, out);
            passed = false;
        }
        return passed;
    }

    private void createTables(DataSource ds, PrintWriter out, String tableName) {
        Connection con = null;
        Statement stmt = null;
        try {
            con = ds.getConnection();
            stmt = con.createStatement();
            String query = "create table " + tableName + "(id " +
                    "integer not null, phone char(16))";
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

    private void emptyTable(Connection conn, String tableName) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DELETE FROM " + tableName);
        stmt.close();
    }

    private void insertEntry(Connection conn, String tableName) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("INSERT into " + tableName +
                "  values (1, 'abcd')");
        stmt.close();        
    }

}
