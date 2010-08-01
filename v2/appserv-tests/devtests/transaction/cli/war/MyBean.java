/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.acme;

import java.sql.*;
import javax.ejb.*;
import javax.sql.DataSource;
import javax.annotation.Resource;

/**
 *
 * @author marina vatkina
 */

@Stateless
public class MyBean {

    private @Resource(mappedName="jdbc/__default") DataSource ds;

    public int verify() throws Exception {
        String selectStatement = "select * from student";
        Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(selectStatement);
        ResultSet rs = ps.executeQuery();
        int result = 0;
        while (rs.next()) {
            result++;
            System.out.println("Found: " + rs.getString(1) + " : " + rs.getString(2));
        }
        rs.close();
        ps.close();
        c.close();

        return result;
    }

    public boolean test(int id) throws Exception {
        String insertStatement = "insert into student values ( ? , ? )";
        Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(insertStatement);

        for (int i = 0; i < 3; i++) {
            System.err.println("Call # " + (i + 1));
            ps.setString(1, "BAA" + id + i);
            ps.setString(2, "BBB" + id + i);
            ps.executeUpdate();

            try {
                Thread.sleep(7000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ps.close();
        c.close();
        System.err.println("Insert successfully");

        return true;
    }

}
