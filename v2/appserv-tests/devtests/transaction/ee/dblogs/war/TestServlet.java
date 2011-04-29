package com.acme;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.naming.*;

@WebServlet(urlPatterns="/TestServlet", loadOnStartup=1)
public class TestServlet extends HttpServlet {

    @EJB private MyBean bean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

        PrintWriter out = resp.getWriter();
	resp.setContentType("text/html");

        String type = req.getQueryString();
        out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet TestServlet</title>");
            out.println("</head>");
            out.println("<body>");
        try {
	    out.println("" + ((type == null)? bean.testone(req.getLocalPort()) : bean.testtwo(req.getLocalPort())));
        }catch(Throwable e){
            out.println("got exception");
            out.println(e);
            e.printStackTrace();
        } finally {
            out.println("</body>");
            out.println("</html>");

            out.close();
            out.flush();

        }

    }

}
