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
@EJB(name="java:module/m1", beanName="HelloSingleton", beanInterface=Hello.class)
public class HelloServlet extends HttpServlet {

    @EJB(name="java:module/env/m2")
    private Hello m1;

    @EJB(name="java:app/a1")
    private HelloRemote a1;

    @EJB(name="java:app/env/a2")
    private HelloRemote a2;

    private Hello singleton1;
    private Hello singleton2;
    private Hello singleton3;
    private Hello singleton4;
    private Hello singleton5;
    private HelloRemote stateless1;
    private HelloRemote stateless2;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

	System.out.println("In HelloServlet::init");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
	System.out.println("In HelloServlet::doGet");

	resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();


	try {
	    
	    InitialContext ic = new InitialContext();

	    String appName = (String) ic.lookup("java:comp/AppName");
	    String moduleName = (String) ic.lookup("java:comp/ModuleName");

	    // lookup via intermediate context 
	    Context appCtx = (Context) ic.lookup("java:app");
	    Context appCtxEnv = (Context) appCtx.lookup("env");
	    stateless2 = (HelloRemote) appCtxEnv.lookup("AS2");
	    NamingEnumeration<Binding> bindings = appCtxEnv.listBindings("");
	    System.out.println("java:app/env/ bindings ");
	    while(bindings.hasMore()) {
		System.out.println("binding : " + bindings.next().getName());
	    }


	    singleton1 = (Hello) ic.lookup("java:module/m1");

	    // standard java:app name for ejb 
	    singleton2 = (Hello) ic.lookup("java:app/ejb-ejb31-full-ear-ejb/HelloSingleton");

	    singleton3 = (Hello) ic.lookup("java:global/" + appName + "/ejb-ejb31-full-ear-ejb/HelloSingleton");

	    // lookup some java:app defined by ejb-jar
	    singleton4 = (Hello) ic.lookup("java:app/env/AS1");

	    // global dependency
	    singleton5 = (Hello) ic.lookup("java:global/GS1");


	    stateless1 = (HelloRemote) ic.lookup("java:app/env/AS2");

	    System.out.println("My AppName = " + 
			       ic.lookup("java:comp/AppName"));

	    System.out.println("My ModuleName = " + 
			       ic.lookup("java:comp/ModuleName"));



	    try {
		org.omg.CORBA.ORB orb = (org.omg.CORBA.ORB) ic.lookup("java:module/MORB1");
		throw new RuntimeException("Should have gotten naming exception");
	    } catch(NamingException ne) {
		System.out.println("Successfully was *not* able to see ejb-jar module-level dependency");
	    }

	} catch(Exception e) {
	    e.printStackTrace();
	}

	m1.hello();
	a1.hello();
	a2.hello();
	singleton1.hello();
	singleton2.hello();
	singleton3.hello();
	singleton4.hello();
	singleton5.hello();

	stateless1.hello();
	stateless2.hello();


       	out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" ); 
            out.println("</BODY> </HTML> ");

    }


}
