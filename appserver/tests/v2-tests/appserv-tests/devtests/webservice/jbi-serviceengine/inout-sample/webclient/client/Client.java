package client;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.ws.*;
import service.web.example.adder.*;

public class Client extends HttpServlet {

       @WebServiceRef(name="sun-web.serviceref/adder") AdderService service;

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
                Adder port = service.getAdderPort();
                System.out.println("port is : " + port);
                int result = port.add(101, 2);
                System.out.println("result is : " + result);
		//printFailure(out);
		printSuccess(out, result);
            } catch(java.lang.Exception e) {
		e.printStackTrace();
	    	//printSuccess(out);
	    	printFailure(out);
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

       public void printSuccess(PrintWriter out, int result) {
		if(out == null) return;
                out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("Value is : " + result);
                out.println("</p>");
                out.println("</body>");
                out.println("</html>");
       }
}

