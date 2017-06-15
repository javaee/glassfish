package org.glassfish.jdbc.devtests.v3.test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import org.glassfish.jdbc.devtests.v3.util.HtmlUtil;
import org.glassfish.jdbc.devtests.v3.util.TablesUtil;

/**
 *
 * @author jagadish
 */
public class UserTxTest implements SimpleTest {

    Map<String, Boolean> resultsMap = new HashMap<String, Boolean>();

    public Map<String, Boolean> runTest(DataSource ds1, PrintWriter out) {
        try {
            if (testUserTxWithRollback(ds1, out)) {
                resultsMap.put("user-tx-rollback", true);
            }else{
                resultsMap.put("user-tx-rollback", false);
            }
        } catch (Exception e) {
            resultsMap.put("user-tx-rollback", false);
        }

        try {
            if (testUserTxWithCommit(ds1, out)) {
                resultsMap.put("user-tx-commit", true);
            }else{
                resultsMap.put("user-tx-commit", false);
            }
        } catch (Exception e) {
            resultsMap.put("user-tx-commit", false);
        }

        HtmlUtil.printHR(out);
        return resultsMap;

    }

    private boolean testUserTxWithRollback(DataSource ds1, PrintWriter out) throws SystemException {
        boolean result = false;
        Connection con = null;

        Statement stmt = null;
        ResultSet rs = null;

        String tableName = "user_tx_table_rollback_test";
        String content = "testUserTxWithRollback";
        String columnName = "message";
        TablesUtil.createTables(ds1, out, tableName, columnName);

        out.println("<h4> user-tx-rollback test </h4>");
        javax.transaction.UserTransaction ut = null;
        try {
            InitialContext ic = new InitialContext();
            ut = (javax.transaction.UserTransaction) ic.lookup("java:comp/UserTransaction");
            out.println("<br>Able to lookup UserTransaction");
            ut.begin();
            out.println("<br>");
            out.println("Started UserTransaction<br>");

            out.println("Trying to get connection ...<br>");

            out.println("ds value : " + ds1);
            con = ds1.getConnection();
            out.println("<br>Got connection : " + con);
            stmt = con.createStatement();
            stmt.executeUpdate("insert into " + tableName + " values ('" + content + "')");
            out.println("<br>");

            out.println("Able to lookup datasource");
            out.println("<br>");
            out.println("Rolling back transaction");
            ut.rollback();
            if (!TablesUtil.verifyTableContent(ds1, out, tableName, columnName, content)) {
                result = true;
            }

        } catch (Throwable e) {
            HtmlUtil.printException(e, out);
            result = false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }

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

            TablesUtil.deleteTables(ds1, out, tableName);
            out.println("<br> Test result : " + result);
            return result;
        }
    }

    private boolean testUserTxWithCommit(DataSource ds1, PrintWriter out) throws SystemException,
            IllegalStateException, SecurityException {
        boolean result = false;
        Connection con = null;

        Statement stmt = null;
        ResultSet rs = null;

        String tableName = "user_tx_table_commit_test";
        String content = "testUserTxWithCommit";
        String columnName = "message";
        TablesUtil.createTables(ds1, out, tableName, columnName);

        out.println("<h4> user-tx-commit test </h4>");
        javax.transaction.UserTransaction ut = null;
        try {
            InitialContext ic = new InitialContext();
            ut = (javax.transaction.UserTransaction) ic.lookup("java:comp/UserTransaction");
            out.println("<br>Able to lookup UserTransaction");
            ut.begin();
            out.println("<br>");
            out.println("Started UserTransaction<br>");

            out.println("Trying to get connection ...<br>");

            out.println("ds value : " + ds1);
            con = ds1.getConnection();
            out.println("<br>Got connection : " + con);
            stmt = con.createStatement();
            stmt.executeUpdate("insert into " + tableName + " values ('" + content + "')");
            out.println("<br>");

            out.println("Able to lookup datasource");
            out.println("<br>");
            ut.commit();
            result = TablesUtil.verifyTableContent(ds1, out, tableName, columnName, content);

        } catch (Throwable e) {
            HtmlUtil.printException(e, out);
            out.println("Rolling back transaction");
            ut.rollback();
            result = false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                HtmlUtil.printException(e, out);
            }

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
            TablesUtil.deleteTables(ds1, out, tableName);
            out.println("<br> Test result : " + result);
            return result;
        }
    }
}
