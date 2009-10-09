package com.sun.s1asdev.jdbc.nonacc;

import javax.naming.*;
import java.sql.*;
import javax.sql.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleJdbcClient {
    
    public static void main( String argv[] ) throws Exception {
        String testSuite = "nonacc ";
        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        stat.addDescription("Test a stand-alone java program that does getConnection");
        Connection con = null;
        Statement stmt = null;

        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(argv[0]);
            con = ds.getConnection();
            System.out.println(" Got connection " + con );

            stmt = con.createStatement();
            stmt.executeQuery("SELECT * FROM NONACC");
            stat.addStatus(testSuite + "test1 ", stat.PASS ); 
        } catch( Exception e) {
            e.printStackTrace();
            stat.addStatus(testSuite + "test1 ",  stat.FAIL );
        } finally {
            if (stmt != null) { try { stmt.close(); }catch( Exception e) {} }
            if (con != null) { try { con.close(); }catch( Exception e) {} }
        }

        stat.printSummary();
    }
}

