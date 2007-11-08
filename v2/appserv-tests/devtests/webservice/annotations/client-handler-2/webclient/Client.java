package client;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.ws.*;

public class Client extends HttpServlet {

        @javax.jws.HandlerChain(name="some name", file="myhandler.xml")
        @WebServiceRef SubtractNumbersService service;

       public void doGet(HttpServletRequest req, HttpServletResponse resp) 
		throws javax.servlet.ServletException {
           doPost(req, resp);
       }

       public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws javax.servlet.ServletException {
            try {
                SubtractNumbersPortType port = service.getSubtractNumbersPortType();
                ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,"http://localhost:8080/client-handler-2/webservice/SubtractNumbersService?WSDL");
                int ret = port.subtractNumbers(9999, 8888);
                PrintWriter out = resp.getWriter();
                resp.setContentType("text/html");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("So the RESULT OF SUBTRACT SERVICE IS :");
                out.println("</p>");
                out.println("[" + ret + "]");
                out.println("</body>");
                out.println("</html>");
                out.flush();
                out.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
       }
}

