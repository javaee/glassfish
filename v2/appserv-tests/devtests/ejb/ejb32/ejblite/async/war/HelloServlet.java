package com.sun.s1asdev.ejb32.ejblite.async;

import javax.ejb.*;
import java.io.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.naming.*;
import javax.annotation.Resource;

import com.acme.*;

@WebServlet(urlPatterns="/HelloServlet", loadOnStartup=1)
public class HelloServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
	System.out.println("In HelloServlet::init");
    }

    @EJB SingletonBean sb;
    @EJB StatefulBean sfTimeout;
    @EJB StatefulBean2 sfNoTimeout;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

	resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

	System.out.println("In HelloServlet::doGet");


	try {

            sb.hello();

            sfTimeout.hello();
            sfNoTimeout.hello();

            System.out.println("Sleeping to wait for sf bean to be removed ...");
            Thread.sleep(7000);
            System.out.println("Waking up , checking sf bean existence");

            try {
                sfTimeout.hello();
                throw new RuntimeException("StatefulTimeout(0) bean should have timed out");
            } catch(EJBException e) {
                System.out.println("Stateful bean successfully timed out");
            }

            sfNoTimeout.hello();
            System.out.println("Stateful bean with longer timeout is still around");

            if( sb.getPassed() ) {
                System.out.println("getPassed() returned true");
            } else {
                throw new EJBException("getPassed() returned false");
            }


	} catch(RuntimeException e) {
	    throw e;
	} catch(Exception e) {
	    throw new RuntimeException(e);
       }


	out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" ); 
            out.println("</BODY> </HTML> ");

    }

}
