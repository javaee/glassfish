package client;

import javax.servlet.http.*;
import java.io.PrintWriter;
import java.security.Principal;

import javax.xml.ws.*;

import endpoint.ejb.*;

public class Client extends HttpServlet {

//    @WebServiceRef(name="sun-web.serviceref/HelloEJBService")
    HelloEJBService service = new HelloEJBService();

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws javax.servlet.ServletException {
        doPost(req, resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws javax.servlet.ServletException {
        try {
            Principal p = req.getUserPrincipal();
	    String principal = (p==null)? "NULL": p.toString();
            System.out.println("****Servlet: principal = " + principal);

            Hello port = service.getHelloEJBPort();
	    String ret = port.sayHello("PrincipalSent="+principal);
            System.out.println("Return value from webservice:"+ret);
            
            PrintWriter out = resp.getWriter();
            resp.setContentType("text/html");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>TestServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<p>");
            out.println("So the RESULT OF EJB webservice IS :");
            out.println("</p>");
            out.println("[" + ret + "]");
            out.println("</body>");
            out.println("</html>");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
