package client;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.ws.*;
import javax.xml.ws.soap.*;

public class Client extends HttpServlet {

        @javax.xml.ws.WebServiceRef(SubtractNumbersService.class)
       //@Addressing (required = true,enabled=true ,responses=AddressingFeature.Responses.NON_ANONYMOUS)
        //@Addressing 
        SubtractNumbersImpl port;

       public void doGet(HttpServletRequest req, HttpServletResponse resp) 
		throws javax.servlet.ServletException {
           doPost(req, resp);
       }

       public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws javax.servlet.ServletException {
            try {
                  com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump=true;

                ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,"http://HTTP_HOST:HTTP_PORT/webservices13-respectbinding/webservice/SubtractNumbersService?WSDL");
                int ret = port.subtractNumbers(9999, 4444);
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

