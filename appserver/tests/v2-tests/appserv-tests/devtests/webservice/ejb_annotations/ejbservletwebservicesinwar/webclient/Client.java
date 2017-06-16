package webclient;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import ejbclient.*;
import webclient.*;
import javax.xml.ws.*;

public class Client extends HttpServlet {

        @javax.xml.ws.WebServiceRef(ejbclient.HelloEjbService.class)
        ejbclient.Hello hiport1;

    @javax.xml.ws.WebServiceRef(webclient.HelloService.class)
            webclient.Hello hiport2;

       public void doGet(HttpServletRequest req, HttpServletResponse resp) 
		throws javax.servlet.ServletException {
           doPost(req, resp);
       }

       public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws javax.servlet.ServletException {
            try {
                String ret1 = hiport1.sayHello("All");
                String ret2 = hiport2.sayHello("All");

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
                out.println("[" + ret1 + "]");
                out.println("[" + ret2 + "]");
                out.println("</body>");
                out.println("</html>");
                out.flush();
                out.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
       }
}

