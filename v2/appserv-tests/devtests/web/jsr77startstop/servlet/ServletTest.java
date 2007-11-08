package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.*;
import javax.sql.*;
import java.sql.*;

public class ServletTest extends HttpServlet{
    private String dbName = "java:comp/env/jdbc/ConnPoolTest";
    private Connection con;
   
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[Servlet.init]");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet.doGet]");
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        System.out.println("[Servlet.doPost]");
        PrintWriter out = response.getWriter();
        
        try{
            makeConnection();
            out.println("jsr77StartStop::PASS");
        } catch (Throwable t) {
            out.println("jsr77StartStop::FAIL");
        }
    }

    private void makeConnection() throws NamingException, SQLException {
        System.out.println("######################## Test JNDI ############");
        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource) ic.lookup(dbName);
        con =  ds.getConnection();

        String selectStatement =
            "select dummy " +
            "from dummy enrollment where dummy = ? ";
        PreparedStatement prepStmt = 
            con.prepareStatement(selectStatement);

        prepStmt.setString(1, "dummy");
        ResultSet rs = prepStmt.executeQuery();
        ArrayList a = new ArrayList();

        while (rs.next()) {
            String id = rs.getString(1);
            a.add(id);
        }

        prepStmt.close();
    }
}
