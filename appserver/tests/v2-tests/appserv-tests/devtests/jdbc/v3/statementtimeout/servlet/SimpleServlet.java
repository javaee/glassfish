package servlet;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ejb.CreateException;
import java.io.IOException;
import java.io.PrintWriter;

import com.sun.s1asdev.jdbc.statementtimeout.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.statementtimeout.ejb.SimpleBMP;

public class SimpleServlet extends HttpServlet {


    public void doGet (HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
      doPost(request, response);
    }

    /** handles the HTTP POST operation **/
    public void doPost (HttpServletRequest request,HttpServletResponse response)
          throws ServletException, IOException {
        doTest(request, response);
    }

    public void doTest(HttpServletRequest request, HttpServletResponse response) throws IOException{
        PrintWriter out = response.getWriter();

        try{
        System.out.println("Statement Timeout test");

        InitialContext ic = new InitialContext();
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome) ic.lookup("java:comp/env/ejb/SimpleBMPEJB");
        out.println("Running Statement Timeout test ");

        SimpleBMP simpleBMP = simpleBMPHome.create();

        if (simpleBMP.statementTest()) {
	    System.out.println("Statement Timeout test : statementTest : PASS");
	    out.println("TEST:PASS");
        } else {
	    System.out.println("Statement Timeout test : statementTest : FAIL");
            out.println("TEST:FAIL");
        }

        if (simpleBMP.preparedStatementTest()) {
	    System.out.println("Statement Timeout test : preparedStatementTest : PASS");
	    out.println("TEST:PASS");
        } else {
	    System.out.println("Statement Timeout test : preparedStatementTest : FAIL");
            out.println("TEST:FAIL");
        }

        if (simpleBMP.callableStatementTest()) {
	    System.out.println("Statement Timeout test : callableStatementTest : PASS");
	    out.println("TEST:PASS");
        } else {
	    System.out.println("Statement Timeout test : callableStatementTest : FAIL");
            out.println("TEST:FAIL");
        }

	} catch(NamingException ne) {
	    ne.printStackTrace();
	} catch(CreateException e) {
	    e.printStackTrace();
        } finally {
	    out.println("END_OF_TEST");
	    out.flush();
	}
    }
}
