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

import com.sun.s1asdev.jdbc.multipleusercredentials.ejb.SimpleSessionHome;
import com.sun.s1asdev.jdbc.multipleusercredentials.ejb.SimpleSession;

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
        System.out.println("JDBC Multiple User Credentials test");

        InitialContext ic = new InitialContext();
        SimpleSessionHome simpleSessionHome = (SimpleSessionHome) ic.lookup("java:comp/env/ejb/SimpleSessionEJB");
        out.println("Running multiple user credentials test ");

        //stat.addDescription("Running serializable connector test ");
        SimpleSession simpleSession = simpleSessionHome.create();

        boolean passed = true;
        for (int i = 0; i < 50; i++) {
            try {
                if (simpleSession.test1() == false) {
                    passed = false;
                    break;
                }
            } catch (Exception e) {
                passed = false;
                break;
            }
        }
        if(passed){
	    System.out.println("Multiple User credentials test : test1 : PASS");
            out.println("TEST:PASS");
	} else {
	    System.out.println("Multiple User credentials test : test1 : FAIL");
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
