/*
 *
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */

import java.sql.*;
import com.pointbase.jdbc.jdbcInOutIntWrapper;

public class SampleExternalMethods {

   private Connection m_conn;

   public SampleExternalMethods (Connection p_conn) {
     m_conn = p_conn;
   }

   /**
    * Counts rows in the coffee table.
    * This is the procedure body for COUNTCOFFEE procedure.
    */
   public void countCoffee (jdbcInOutIntWrapper p_count) throws Exception{
     Statement stmt=null;
     try {
       stmt = m_conn.createStatement();
       String query = "select count(*) from coffee";
       ResultSet rs = stmt.executeQuery (query);

       rs.next();
       int count = rs.getInt(1);

       p_count.set (count);
       rs.close();
       stmt.close();
     } catch (Exception e) {
       e.printStackTrace();
       throw e;
     }
   }

   /**
    * Inserts a row in the coffee table.
    * This is the procedure body for INSERTCOFFEE procedure.
    */
   public void insertCoffee (String p_name, int p_qty) throws Exception{
     PreparedStatement pstmt=null;
     try {
       String insertStr = "insert into coffee values (?, ?)";
       pstmt = m_conn.prepareStatement(insertStr);
       pstmt.setString (1, p_name);
       pstmt.setInt (2, p_qty);

       int cnt = pstmt.executeUpdate ();
       m_conn.commit();

       pstmt.close();
     } catch (Exception e) {
       e.printStackTrace();
       throw e;
     }
   }
}
