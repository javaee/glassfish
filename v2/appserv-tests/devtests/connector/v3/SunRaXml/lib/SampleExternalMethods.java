/*
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */

import java.sql.*;

public class SampleExternalMethods {

    /**
     * Counts rows in the coffee table.
     * This is the procedure body for COUNTCOFFEE procedure.
     */
    public static void countCoffee(int[] count) throws Exception {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = DriverManager.getConnection("jdbc:default:connection");

            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select count(*) from coffee");

            if (rs.next()) {
                count[0] = rs.getInt(1);
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (Exception e) {
                }
            if (conn != null)
                try {
                    stmt.close();
                } catch (Exception e) {
                }
        }
    }

    /**
     * Inserts a row in the coffee table.
     * This is the procedure body for INSERTCOFFEE procedure.
     */
    public static void insertCoffee(String name, int qty) throws Exception {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection("jdbc:default:connection");
            stmt = conn.prepareStatement("insert into coffee values (?, ?)");
            stmt.setString(1, name);
            stmt.setInt(2, qty);

            stmt.executeUpdate();
            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (Exception e) {
                }
            if (conn != null)
                try {
                    stmt.close();
                } catch (Exception e) {
                }
        }
    }
}
