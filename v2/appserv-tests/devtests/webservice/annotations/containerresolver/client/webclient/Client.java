package client;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.ws.*;

import com.example.hello1.*;
import com.example.hello2.*;

public class Client extends HttpServlet {

       @WebServiceRef(wsdlLocation="http://localhost:8080/containerresolver-app1/Hello1Service?wsdl") Hello1Service service1;
       @WebServiceRef(wsdlLocation="http://localhost:8080/containerresolver-app2/Hello2Service?wsdl") Hello2Service service2;

       public void doGet(HttpServletRequest req, HttpServletResponse resp) 
		throws javax.servlet.ServletException {
           doPost(req, resp);
       }

       public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws javax.servlet.ServletException {
            try {
                Hello1 port1 = service1.getHello1Port();
                String ret = port1.sayHello1("Hi");
                PrintWriter out = resp.getWriter();
                resp.setContentType("text/html");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("So the RESULT OF SERVICE IS :");
                out.println("</p>");
                out.println("[" + ret + "]");
                out.println("</p>");
                Hello2 port2 = service2.getHello2Port();
                ret = port2.sayHello2("Hi");
                out.println("[" + ret + "]");
                out.println("</p>");
                out.println("</body>");
                out.println("</html>");
                out.flush();
                out.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
       }
}

