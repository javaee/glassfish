package client;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.ws.*;
import service.web.example.oneway.*;

public class Client extends HttpServlet {

       @WebServiceRef(name="sun-web.serviceref/oneway") OneWayService service;
	   //static OneWay port;

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
                OneWay port = service.getOneWayPort();
				//if (port == null) port = service.getOneWayPort();
                System.out.println("port is : " + port);
                port.subtract(101, 2);
                port.sayHi();
				printSuccess(out);
            } catch(java.lang.Exception e) {
				e.printStackTrace();
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
                out.println("Endpoint invocation failed.");
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
                out.println("Endpoint is invoked successfully.");
                out.println("</p>");
                out.println("</body>");
                out.println("</html>");
       }
}

