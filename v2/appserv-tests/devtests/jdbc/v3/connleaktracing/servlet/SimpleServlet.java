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

import com.sun.s1asdev.jdbc.connectionleaktracing.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.connectionleaktracing.ejb.SimpleBMP;

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
        System.out.println("JDBC connection leak tracing test");

        InitialContext ic = new InitialContext();
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome) ic.lookup("java:comp/env/ejb/SimpleBMPEJB");
        out.println("Running connection leak tracing test ");

        //stat.addDescription("Running serializable connector test ");
        SimpleBMP bean = simpleBMPHome.create();

	    for(int i=0; i<3; i++){
                if(!bean.test1()){
		    //stat.addStatus("jdbc-connectionleakttracing : test ", stat.FAIL);
                    out.println("TEST:FAIL");
	            break;
	        }
	        Thread.sleep(20000);
	    }
	    out.println("TEST:PASS");
	} catch(NamingException ne) {
	    ne.printStackTrace();
	} catch(CreateException e) {
	    e.printStackTrace();
	} catch(java.lang.InterruptedException ie) {
		ie.printStackTrace();
        } finally {
	    out.println("END_OF_TEST");
	    out.flush();
	}
    }
}
