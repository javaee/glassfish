package sfulnoi;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.ejb.EJB;

public class SfulServlet extends HttpServlet {
   
    @EJB
    private SfulBean simpleEJB;
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        boolean status = false;
        try {
            
            out.println("-------SfulServlet--------");  
            out.println("SfulServlet at " + request.getContextPath ());

            String testcase = request.getParameter("tc");
            out.println("testcase = " + testcase);
            if (testcase != null) {

        if ("SetName".equals(testcase)){
		out.println("Simple EJB:");
		out.println("@EJB Injection="+simpleEJB);

		if (simpleEJB != null) {
		  out.println("SetName in a stateful session bean.");
            try {
                simpleEJB.setName("Duke");
                status = true;
            } catch (Exception e) {
                e.printStackTrace();
                status = false;
            }
		}

	      } else if ("GetName".equals(testcase)){

		String simpleEJBName = null;
		
		if (simpleEJB != null) {
		  simpleEJBName = simpleEJB.getName();
		  out.println("@EJB.getName()=" + simpleEJBName);
		}

		if (simpleEJB != null &&
		    "Duke".equals(simpleEJBName)){
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
        return "SfulServlet";
    }

}



