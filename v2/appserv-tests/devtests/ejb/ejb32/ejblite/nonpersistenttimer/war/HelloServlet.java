package com.sun.s1asdev.ejb32.ejblite.timer;

import javax.ejb.EJB;
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
import javax.annotation.Resource;

@WebServlet(urlPatterns="/HelloServlet", loadOnStartup=1)
public class HelloServlet extends HttpServlet {

    @EJB    
    private StatefulWrapper wrapper;


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
	System.out.println("In HelloServlet::init");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

	resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

	System.out.println("In HelloServlet::doGet");


	try {

            boolean result = wrapper.doFooTest(true);
            if (!result) 
                throw new RuntimeException("BMT failed");
            else
                System.out.println("In HelloServlet::BMT passed");

            result = wrapper.doFooTest(false);
            if (!result) 
                throw new RuntimeException("CMT failed");
            else
                System.out.println("In HelloServlet::CMT passed");
	    

	} catch(RuntimeException e) {
	    throw e;
	} catch(Exception e) {
	    throw new RuntimeException(e);
	} finally {
            try {
                 wrapper.removeFoo();
            } catch(Exception e) {
                e.printStackTrace();
            }
       }


	out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" ); 
            out.println("</BODY> </HTML> ");

    }

}
