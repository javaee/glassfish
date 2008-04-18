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
