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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;
import org.glassfish.jdbc.devtests.v3.util.TablesUtil;

/**
 * Devtest to test if application server removes a connection from pool 
 * after using it for "maxconnectionusage" times.
 * 
 * Assumes that steady-pool-size = 1, max-pool-size = 1, 
 * max-connection-usage-count = 10 attributes are set on the pool.
 * 
 * @author shalini
 */
public class MaxConnectionUsageTest implements SimpleTest {
    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();
    
    /**
     * Tests max-connection-usage assuming pool size = 1 and 
     * DataSource is of type : javax.sql.DataSource
     * @param ds
     * @param out
     * @return resultsMap
     */
    public Map<String, Boolean> runTest(DataSource ds, PrintWriter out) {
        String tableName = "max_connection_usage";
        createTables(ds, out, tableName);
        try {
            if (maxConnUsageTest1(ds, out)) {
                resultsMap.put("max-conn-usage-test1", true);
            }else{
                resultsMap.put("max-conn-usage-test1", false);
            }
        } catch (Exception e) {
            resultsMap.put("max-conn-usage-test1", false);
        }

        try {
            if (connectionSharingTest(ds, out, 21112, tableName)) {
                resultsMap.put("max-conn-usage-connection-sharing-test", true);
            }else{
                resultsMap.put("max-conn-usage-connection-sharing-test", false);
            }
        } catch (Exception e) {
            resultsMap.put("max-conn-usage-connection-sharing-test", false);
        }
        
        TablesUtil.deleteTables(ds, out, tableName);
        HtmlUtil.printHR(out);
        return resultsMap;                        
    }

    /**
     * Assuming pool has only one connection, calling maxConnUsageTest2(...) and
     * maxConnUsageTest3(...) 5 times each will ensure that "maxconnectionsusage=10"
     * is met.  
     * Further call to maxConnUsageTest2(...) will return a different connection
     * 
     * If they are different, connection was dropped and created and test passes.
     * 
     * @param ds
     * @param out
     * @param value
     * @param tableName
     * @return boolean status
     */
    private boolean connectionSharingTest(DataSource ds, PrintWriter out, 
            int value, String tableName) {
        String[] results = new String[10];
        out.println("<h4> Max Connection Usage - Connection Sharing Test </h4>");
        out.println("<br> Invoking maxConnUsageTest2 and maxConnUsageTest3 methods");
        for(int i=0; i<10; i++) {
            if(i % 2 == 0) {
                results[i] = maxConnUsageTest2(ds, out, value, tableName);
            } else {
                results[i] = maxConnUsageTest3(ds, out, (i/2)+1, value, 
                        tableName);
            }
        }
        boolean status = true;
        String tmpResult1 = results[0];
        for(int i =0; i<results.length; i++) {
            if(!results[i].equalsIgnoreCase(tmpResult1)) {
                out.println("<br>Result 0 : " + tmpResult1);
                out.println("<br>Result [" + i + "] : " + results[i]);
                status = false;
                break;
            }
        }
        
        out.println("<br> Further call to maxConnUsageTest2");
        String tmpResult2 = maxConnUsageTest2(ds, out, value, tableName);
        
        if(!tmpResult2.equalsIgnoreCase(tmpResult1) && status) {
            status = true;
        } else {
            out.println("<br>Marking status as false during verification");
            out.println("<br>Value : " + value);
            out.println("<br>Result 1 : " + tmpResult1);
            out.println("<br>Result 2 : " + tmpResult2);
            status = false;
        }
        out.println("<br> Test result : " + status);
        return status;
    }

    /**
     * Creates table needed for the Max Connection Usage devtest.
     * 
     * @param ds
     * @param out
     * @param tableName
     */
    private void createTables(DataSource ds, PrintWriter out, String tableName) {
        Connection con = null;
        Statement stmt = null;
        try {
            con = ds.getConnection();
            stmt = con.createStatement();
            String query = "create table " + tableName + "(id " +
                    "integer not null, value char(16))";
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
     * Tests if the first and last physical connections are different.
     * Assuming maxconnectionusage property is 10 and connectionSharing ON, the
     * physical connections con-1 and con-11 must be different.     
     * @param ds
     * @param out
     * @return boolean result
     */
    private boolean maxConnUsageTest1(DataSource ds, PrintWriter out) {
        Connection firstConnection = null;
        Connection lastConnection = null;
        boolean result = false;
        com.sun.appserv.jdbc.DataSource ds1 = (com.sun.appserv.jdbc.DataSource) ds;
        out.println("<h4> Max Connection Usage - Test1 </h4>");

        out.println("<br> Getting connections and physical connections...");
        for(int i=0; i<11; i++) {
            Connection con = null;
            try {
                con = ds1.getConnection();
                //Getting physical connection
                if(i==0) {
                    firstConnection = ds1.getConnection(con);
                } else if(i==10) {
                    lastConnection = ds1.getConnection(con);
                    //Necessary for last connection to make subsequent tests pass
                    ds1.markConnectionAsBad(con);
                }
                result = (firstConnection != lastConnection);
            } catch (Exception ex) {
                HtmlUtil.printException(ex, out);
                result = false;
            } finally {
                if(con != null) {
                    try {
                        con.close();
                    } catch (Exception ex) {
                        HtmlUtil.printException(ex, out);
                    }
                }
            }
        }
        out.println("<br> Test result : " + result);
        return result;
    }

    /**
     * Returns physical connection's ID  after inserting entries into a table
     * @param ds
     * @param out
     * @param value
     * @param tableName
     * @return physicalConnectionString
     */
    private String maxConnUsageTest2(DataSource ds, PrintWriter out, int value, 
            String tableName) {
        Connection physicalConnection = null;
        Connection conn = null;
        String physicalConnectionString = null;
        com.sun.appserv.jdbc.DataSource ds1 = (com.sun.appserv.jdbc.DataSource) ds;
        Statement stmt = null;
        try {
            conn = ds1.getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("insert into " + tableName + " values (" + 
                    value + ",'" + value + "')");
            physicalConnection = ds1.getConnection(conn);
            physicalConnectionString = physicalConnection.toString();
        } catch (Exception ex) {
            physicalConnection = null;
            return null;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }
          
            try {
                if(conn != null) {
                    conn.close();
                }
            } catch(Exception ex) {
                HtmlUtil.printException(ex, out);
            }
        }
        return physicalConnectionString;
    }

    /**
     * Returns the physical connection's ID after testing if the entries inserted 
     * using maxConnUsageTest2(....) method are persisted.
     * @param ds
     * @param out
     * @param count
     * @param value
     * @param tableName
     * @return physicalConnectionString
     */
    private String maxConnUsageTest3(DataSource ds, PrintWriter out, int count, 
            int value, String tableName) {
        Connection physicalConnection = null;
        Connection conn = null;
        com.sun.appserv.jdbc.DataSource ds1 = (com.sun.appserv.jdbc.DataSource) ds;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ds1.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select count(*) as COUNT from " + tableName + " where id=" + value);
            while(rs.next()) {
                int resultCount = rs.getInt("COUNT");
                if(count == resultCount) {
                    physicalConnection = ds1.getConnection(conn);
                } else {
                    out.println("Expected count [" + count + 
                            "] does not match [" + resultCount + "]");
                    break;
                }
            }
        } catch (Exception ex) {
            physicalConnection = null;
            return null;
        } finally {
            try {
                if(rs != null) {
                    rs.close();
                }
            } catch(Exception ex) {
                HtmlUtil.printException(ex, out);
            }
            
            try {
                if(stmt != null) {
                    stmt.close();
                } 
            } catch (Exception ex) {
                    HtmlUtil.printException(ex, out);
            }
            
            try {
                if(conn != null) {
                    conn.close();
                }
            } catch(Exception ex) {
                HtmlUtil.printException(ex, out);
            }
        }
        return physicalConnection.toString();
    }
    
}
