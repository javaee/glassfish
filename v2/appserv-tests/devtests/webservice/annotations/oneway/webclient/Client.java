package client;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.ws.*;
import javax.xml.ws.soap.*;

public class Client extends HttpServlet {


       @WebServiceRef(wsdlLocation="http://HTTP_HOST:HTTP_PORT/oneway/HelloService?WSDL")
        HelloService service;

       public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws javax.servlet.ServletException {
           doPost(req, resp);
       }

       public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws javax.servlet.ServletException {
           String ret="Oneway works";
           try {
                
                Hello port = service.getHelloPort();

                port.sayHello("All");


                PrintWriter out = resp.getWriter();
                resp.setContentType("text/html");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("So the RESULT OF HELLO SERVICE IS :");
                out.println("</p>");
                out.println("[" + ret + "]");
                out.println("</body>");
                out.println("</html>");
                out.flush();
                out.close();
            } catch(Exception e) {
                e.printStackTrace();
                ret = "Oneway fails with " + e.toString();
            }
       }
}

