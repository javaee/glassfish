package com.acme;

import javax.ejb.EJB;
import javax.ejb.ConcurrentAccessException;
import javax.ejb.ConcurrentAccessTimeoutException;
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

@EJB(name="helloStateful", beanInterface=HelloStateful.class)
@WebServlet(urlPatterns="/HelloServlet", loadOnStartup=1)
public class HelloServlet extends HttpServlet {

    // Environment entries
    private String foo = null;

    @EJB HelloSingleton singleton;
    @EJB Hello hello;
    @EJB HelloRemote helloRemote;

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
	    HelloStateful sful = (HelloStateful) new InitialContext().lookup("java:comp/env/helloStateful");
	    sful.hello();
	    hello.foo();
	} catch(Exception e) {
	    e.printStackTrace();
	}

	System.out.println("Remote intf bean says " +
			   helloRemote.hello());

	System.out.println("Calling testNoWait. This one should work since it's not a concurrent invocation");
	singleton.testNoWait();

	System.out.println("Call async wait, then sleep a bit to make sure it takes affect");
	singleton.asyncWait(1);
	try {
	    // Sleep a bit to make sure async call processes before we proceed
	    Thread.sleep(100);
	} catch(Exception e) {
	    System.out.println(e);
	}

	try {
	    System.out.println("Calling testNoWait");
	    singleton.testNoWait();
	    throw new RuntimeException("Expected ConcurrentAccessException");
	} catch(ConcurrentAccessTimeoutException cate) {
	    throw new RuntimeException("Expected ConcurrentAccessException");
	} catch(ConcurrentAccessException cae) {
	    System.out.println("Got expected exception for concurrent access on method with 0 wait");
	}

	singleton.wait(10);

	singleton.reentrantReadWrite();

	singleton.callSing2WithTxAndRollback();
	singleton.hello();

	singleton.read();
	singleton.write();
	singleton.reentrantReadRead();
	singleton.reentrantWriteWrite();
	singleton.reentrantWriteRead();

       	out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" ); 
            out.println("</BODY> </HTML> ");

    }


}
