package client;

import javax.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.rpc.Stub;
import helloservice.*;


public class Client extends HttpServlet {


       public void doGet(HttpServletRequest req, HttpServletResponse resp) 
		throws javax.servlet.ServletException {
           doPost(req, resp);
       }

       public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws javax.servlet.ServletException {
            try {
           String targetEndpointAddress = "http://localhost:8080/hello-jaxrpc/hello";//?wsdl
            InitialContext ic = new InitialContext();

            MyHelloService myHelloService =
                (MyHelloService) ic.lookup(
                    "java:comp/env/service/MyHelloService");

           HelloIF helloPort = myHelloService.getHelloIFPort();
           ((Stub)helloPort)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY,
                    targetEndpointAddress);

           String ret= helloPort.sayHello("All");

                PrintWriter out = resp.getWriter();
                resp.setContentType("text/html");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("So the RESULT OF jaxrpc web SERVICE IS :");
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

