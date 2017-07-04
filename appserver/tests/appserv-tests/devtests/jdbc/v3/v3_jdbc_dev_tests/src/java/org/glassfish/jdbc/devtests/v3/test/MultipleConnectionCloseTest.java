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
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;
import org.glassfish.jdbc.devtests.v3.util.TablesUtil;

/**
 *
 * @author jagadish
 */
public class MultipleConnectionCloseTest implements SimpleTest {

  Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds1, PrintWriter out) {
        try {
            if (multipleCloseConnection_1(ds1, out)) {
                resultsMap.put("multiple-close-connection-1", true);
            }else{
                resultsMap.put("multiple-close-connection-1", false);
            }
        } catch (Exception e) {
            resultsMap.put("multiple-close-connection-1", false);
        }

       try {
            if (multipleCloseConnection_2(ds1, out)) {
                resultsMap.put("multiple-close-connection-2", true);
            }else{
                resultsMap.put("multiple-close-connection-2", false);
            }
        } catch (Exception e) {
            resultsMap.put("multiple-close-connection-2", false);
        }

        HtmlUtil.printHR(out);
        return resultsMap;

    }

    private boolean multipleCloseConnection_1(DataSource ds1, PrintWriter out) {
        Connection conn1 = null;
        boolean passed = true;
        //clean the database
        out.println("<h4> Multiple close connection - Test1 </h4>");    

        try {
            conn1 = ds1.getConnection();
        } catch (Exception e) {
            HtmlUtil.printException(e, out);
            passed = false;
        } finally {
            try {
                if (conn1 != null) {
                    conn1.close();
                    conn1.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
                passed = false;
            }
        }
        out.println("<br> Test result : " + passed);
        return passed;
    }

    private boolean multipleCloseConnection_2(DataSource ds1, PrintWriter out) {
         Connection conn1 = null;
        Statement stmt = null;
        boolean passed = true;
        String tableName = "multiple_close_connection_table";
        String columnName = "name";
        TablesUtil.createTables(ds1, out, tableName, columnName);

        out.println("<h4> Multiple close connection - Test2 </h4>");    
        try {
            conn1 = ds1.getConnection();
            stmt = conn1.createStatement();
            stmt.executeQuery("SELECT * FROM multiple_close_connection_table");
        } catch (Exception e) {
            e.printStackTrace();
            passed = false;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    passed = false;
                }
            }
            try {
                if (conn1 != null) {
                    conn1.close();
                    conn1.createStatement();
                    // trying to create statement on a closed connection, must throw exception.
                }
            } catch (Exception e) {
                try {
                    conn1.close();
                } catch (Exception e1) {
                    e.printStackTrace();
                    //closing a connection multiple times is a no-op.
                    //If exception is thrown, its a failure.
                    passed = false;
                }
            }
            TablesUtil.deleteTables(ds1, out, tableName);
        }
        out.println("<br> Test result : " + passed);
        return passed;
    }
}
