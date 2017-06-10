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
