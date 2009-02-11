package sfulnoi;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.ejb.EJB;

public class SfulServlet extends HttpServlet {
   
    @EJB 
    private static SfulBean simpleEJB;
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        boolean status = false;
        try {
            
            out.println("-------AnnotatedServlet--------");  
            out.println("AnntatedServlet at " + request.getContextPath ());

            String testcase = request.getParameter("tc");
            out.println("testcase = " + testcase);
            if (testcase != null) {

	      if ("SetID".equals(testcase)){

		out.println("Simple EJB:");
		out.println("@EJB Injection="+simpleEJB);
		String simpleEJBID = null;
		
		if (simpleEJB != null) {
		  simpleEJB.setId("Duke");
		  out.println("@EJB.getName()=" + simpleEJBID);
		}

	      } else if ("GetID".equals(testcase)){
        String simpleEJBID = null;
		if (simpleEJB != null) {
            simpleEJBID = simpleEJB.getId();
            out.println("@EJB.getName()=" + simpleEJBID);
		}

		if (simpleEJB != null &&
		    "Duke".equals(simpleEJBID)){
		  status = true;
		}
		
	      } else {
		out.println("No such testcase");
	      }
	  }
        } catch (Exception ex ) {
            ex.printStackTrace();
            System.out.println("servlet test failed");
            throw new ServletException(ex);
        } finally { 
            if (status) 
	      out.println("Test:Pass");
            else
	      out.println("Test:Fail");
            out.close();
        }
    } 

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    public String getServletInfo() {
        return "StatefulServlet";
    }

}



