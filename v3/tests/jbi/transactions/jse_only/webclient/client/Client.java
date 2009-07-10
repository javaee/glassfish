package client;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.ws.*;
import service.web.example.calculator.*;

public class Client extends HttpServlet {

       //@WebServiceRef(name="sun-web.serviceref/calculator") CalculatorService service;
	CalculatorService service = new CalculatorService();

       public void doGet(HttpServletRequest req, HttpServletResponse resp) 
		throws javax.servlet.ServletException {
           doPost(req, resp);
       }

       public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws javax.servlet.ServletException {
	    UserTransaction ut = null;
            try {
		ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
		ut.begin();
                System.out.println(" Service is :" + service);
                Calculator port = service.getCalculatorPort();
                int ret = port.add(1, 2);
		ut.commit();
                PrintWriter out = resp.getWriter();
                resp.setContentType("text/html");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("So the RESULT OF Calculator add SERVICE IS :");
                out.println("</p>");
                out.println("[" + ret + "]");
                out.println("</body>");
                out.println("</html>");
                out.flush();
                out.close();
            } catch(Exception e) {
		try {
			if(ut != null)
				ut.rollback();
		} catch (Exception ex) {
                	ex.printStackTrace();
		}
                e.printStackTrace();
            }
       }
}

