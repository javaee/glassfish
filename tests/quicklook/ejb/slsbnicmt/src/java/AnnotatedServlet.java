package slsbnicmt;

import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.ejb.EJB;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class AnnotatedServlet extends HttpServlet {
   
    @EJB
    private AnnotatedEJB simpleEJB;
    
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

	      if ("EJBInject".equals(testcase)){

		out.println("Simple EJB:");
		out.println("@EJB Injection="+simpleEJB);
		String simpleEJBName = null;
		
		if (simpleEJB != null) {
		  simpleEJBName = simpleEJB.getName();
		  out.println("@EJB.getName()=" + simpleEJBName);
		}

		if (simpleEJB != null &&
		    "foo".equals(simpleEJBName)){
		  status = true;
		}

	      } else if ("JpaPersist".equals(testcase)){
		
		if (simpleEJB != null) {
		  out.println("Persist Entity");
		  status  = simpleEJB.persistEntity();
		}

	      } else if ("JpaRemove".equals(testcase)){

		if (simpleEJB != null) {
		  out.println("Verify Persisted Entity and Remove Entity");
		  status  = simpleEJB.removeEntity();
		}

	      } else if ("JpaVerify".equals(testcase)){

		if (simpleEJB != null) {
		  out.println("Verify Removed Enitity");
		  status  = simpleEJB.verifyRemove();
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
        return "AnnontatedServlet";
    }

    private Object lookupField(String name) {
        try {
            return new InitialContext().lookup("java:comp/env/" + getClass().getName() + "/" + name);
        } catch (NamingException e) {
            return null;
        }
    }

}



