package com.acme;

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

@WebServlet(urlPatterns="/HelloServlet", loadOnStartup=1)
@EJB(name="java:module/ES1", beanName="SingletonBean", beanInterface=SingletonBean.class)
public class HelloServlet extends HttpServlet {

    @EJB(name="java:module/env/ES2")
    private SingletonBean simpleSingleton;

    @EJB(name="java:app/EL1")
    private StatelessBean simpleStateless;

    @EJB(name="java:app/env/EL2")
    private StatelessBean simpleStateless2;

    private SingletonBean sb2;
    private SingletonBean sb3;
    private SingletonBean sb4;
    private SingletonBean sb5;
    private StatelessBean slsb;
    private StatelessBean slsb2;
    private StatelessBean slsb3;
    private StatelessBean slsb4;
    private StatelessBean slsb5;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

	System.out.println("In HelloServlet::init");

	try {
	    InitialContext ic = new InitialContext();
	    sb2 = (SingletonBean) ic.lookup("java:module/SingletonBean");
	    sb3 = (SingletonBean) ic.lookup("java:module/SingletonBean!com.acme.SingletonBean");

	    sb4 = (SingletonBean) ic.lookup("java:module/ES1");
	    sb5 = (SingletonBean) ic.lookup("java:module/env/ES2");

	    slsb = (StatelessBean) ic.lookup("java:module/StatelessBean");
	    slsb2 = (StatelessBean) ic.lookup("java:app/StatelessBean");
	    slsb3 = (StatelessBean) ic.lookup("java:app/StatelessBean!com.acme.StatelessBean");

	    slsb4 = (StatelessBean) ic.lookup("java:app/EL1");
	    slsb5 = (StatelessBean) ic.lookup("java:app/env/EL2");

	    System.out.println("My AppName = " + 
			       ic.lookup("java:comp/AppName"));

	    System.out.println("My ModuleName = " + 
			       ic.lookup("java:comp/ModuleName"));

	} catch(Exception e) {
	    e.printStackTrace();
	    throw new ServletException(e);
	}
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

	resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

	System.out.println("In HelloServlet::doGet");

	simpleSingleton.hello();

	simpleStateless.hello();
	simpleStateless2.hello();

	sb2.hello();

	sb3.hello();

	sb4.hello();

	sb5.hello();

	slsb.hello();

	slsb2.hello();

	slsb3.hello();

	slsb4.hello();

	slsb5.hello();

	out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" ); 
            out.println("</BODY> </HTML> ");

    }


}
