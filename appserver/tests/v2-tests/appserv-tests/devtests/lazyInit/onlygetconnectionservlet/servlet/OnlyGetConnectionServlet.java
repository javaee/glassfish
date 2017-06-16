package com.sun.s1asdev.jdbc.onlygetconnectionservlet.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.transaction.*;
import javax.sql.*;
import java.sql.*;
import java.io.*;
import javax.naming.InitialContext;

/**
 * Collection of getConnection tests using a servlet
 * 
 * @author aditya.gore@sun.com
 */ 

public class OnlyGetConnectionServlet extends HttpServlet {

    private DataSource ds;
    private PrintWriter out;
    private UserTransaction utx;

    public void doGet( HttpServletRequest req, HttpServletResponse resp ) 
            throws IOException, ServletException
    {
System.out.println(" @@@@ in doGet");    
        out = resp.getWriter();
        writeHeader();

        try {
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:comp/env/jdbc/onlygetconnectionservlet");
            utx = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
        } catch(Exception e) {
            e.printStackTrace( out );
            return;
        }

//        out.println("-----Test1----");
//        test1();
//        out.println("--------------");
//        out.println("-----Test1----");
//        test2();
//        out.println("--------------");
        test2();

        writeFooter();
    }

    private void test1() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = ds.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM ONLYGETCONNECTION");
            out.println("test1 :: PASSED"); 
        } catch(Exception e) {
            e.printStackTrace( out ); 
            return;
        } finally {
            if ( rs != null ) { try { rs.close(); }catch( Exception e) {} }
            if ( stmt != null ) { try { stmt.close(); }catch( Exception e) {} }
            if ( con != null ) { try { con.close(); }catch( Exception e) {} }
        }
    }

    private void test2() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            utx.begin();
            con = ds.getConnection();
            try {
                Thread.sleep( 5000 );    
            } catch(Exception e) {
            }
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM ONLYGETCONNECTION");
            utx.commit();
            out.println("test2 :: PASSED"); 
        } catch(Exception e) {
            e.printStackTrace( out ); 
            return;
        } finally {
            if ( rs != null ) { try { rs.close(); }catch( Exception e) {} }
            if ( stmt != null ) { try { stmt.close(); }catch( Exception e) {} }
            if ( con != null ) { try { con.close(); }catch( Exception e) {} }
        }
    }

    private void writeHeader() {
        out.println( "<html>" );
        out.println( "<head><title>onlygetconnectionservlet results</title></head>");
        out.println( "<body>");
    }

    private void writeFooter() {
        out.println( "</body>");
        out.println( "</html>");
    }
}
