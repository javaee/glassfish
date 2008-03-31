package myapp.util;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;

/**
 *
 * @author jagadish
 */
public class TablesUtil {

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
