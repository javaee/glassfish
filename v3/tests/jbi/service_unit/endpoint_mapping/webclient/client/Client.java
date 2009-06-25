package client;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.ws.*;
import service.web.example.endpointmapping.*;

public class Client extends HttpServlet {

       //@WebServiceRef(name="sun-web.serviceref/calculator") EndpointMappingService service;
       EndpointMappingService service = new EndpointMappingService();

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
                EndpointMapping port = service.getEndpointMappingPort();
                int ret = port.add(1, 2);
		printSuccess(out,ret);
            } catch(java.lang.Exception e) {
		//e.printStackTrace();
	    	printFailure(out, e.getMessage());
            } finally {
		if(out != null) {
                    out.flush();
                    out.close();
		}
	    }
       }

       public void printFailure(PrintWriter out, String errMsg) {
		if(out == null) return;
		out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("Test FAILED: Error message - " + errMsg);
                out.println("</p>");
                out.println("</body>");
                out.println("</html>");
       }

       public void printSuccess(PrintWriter out, int result) {
		if(out == null) return;
                out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("Result is : " + result);
                out.println("</p>");
                out.println("</body>");
                out.println("</html>");
       }
}

