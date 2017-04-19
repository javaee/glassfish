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

@WebServlet(urlPatterns="/HelloServlet", loadOnStartup=1)
public class HelloServlet extends HttpServlet {

    @EJB private SimpleSingleton simpleSingleton;

    @EJB(name="java:app/env/slref") private SimpleStateless simpleStateless;

    @EJB private SimpleStateful simpleStateful;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

	System.out.println("In HelloServlet::init");
	simpleSingleton.hello();
	simpleStateless.hello();
	simpleStateful.hello();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

	resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

	System.out.println("In HelloServlet::doGet");

	simpleSingleton.hello();

	try {
	    simpleStateless.helloPackage();
	    throw new RuntimeException("Expected exception when calling package-private method");
	} catch(EJBException e) {
	    System.out.println("Successfully got exception when calling package private method on no-interface view");
	}

	try {
	    simpleStateless.helloProtected();
	    throw new RuntimeException("Expected exception when calling protected method");
	} catch(EJBException e) {
	    System.out.println("Successfully got exception when calling protected method on no-interface view");
	}

	try {
	    InitialContext ic = new InitialContext();
	    for (NamingEnumeration<Binding> e = ic.listBindings("java:comp/env"); e.hasMore(); ) {
                 Binding b = e.next();// java:comp/env/xxx
               final String name = b.getName().substring("java:comp/env/".length());
                 final String cl = b.getClassName();
                 final Object o = b.getObject();
		 System.out.println("binding = " + b + " , name = " + name + " , cl = " + cl + " , object = " + o);
		 if( !b.getName().startsWith("java:comp/env") ) {
		     throw new RuntimeException("invalid returned env entry prefix");
		 }
	    }
	    // assumes 299 enabled new InitialContext().lookup("java:comp/BeanManager");
	} catch(Exception e) {
	    throw new ServletException(e);
	}

	out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" ); 
            out.println("</BODY> </HTML> ");

    }


}
