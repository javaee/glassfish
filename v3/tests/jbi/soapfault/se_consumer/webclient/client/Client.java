package client;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.ws.*;
import service.web.example.calculator.*;

public class Client extends HttpServlet {

       //@WebServiceRef(name="sun-web.serviceref/calculator") CalculatorService service;
	CalculatorService service = new CalculatorService();	

       public void doGet(HttpServletRequest req, HttpServletResponse resp) 
		throws javax.servlet.ServletException {
           doPost(req, resp);
       }

       public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws javax.servlet.ServletException {
	    PrintWriter out=null;
            try {
                System.out.println(" Service is :" + service);
                resp.setContentType("text/html");
            	out = resp.getWriter();
                Calculator port = service.getCalculatorPort();
                int ret = port.add(1, 2);
		printFailure(out);
            } catch(java.lang.Exception e) {
		e.printStackTrace();
			if(e instanceof service.web.example.calculator.Exception_Exception) {
	    		printSuccess(out);
			}
            } finally {
		if(out != null) {
                    out.flush();
                    out.close();
		}
	    }
       }

       public void printFailure(PrintWriter out) {
		if(out == null) return;
		out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("Test FAILED: SOAPFaultException not thrown");
                out.println("</p>");
                out.println("</body>");
                out.println("</html>");
       }

       public void printSuccess(PrintWriter out) {
		if(out == null) return;
                out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("Exception thrown Successfully");
                out.println("</p>");
                out.println("</body>");
                out.println("</html>");
       }
}

