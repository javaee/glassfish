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
