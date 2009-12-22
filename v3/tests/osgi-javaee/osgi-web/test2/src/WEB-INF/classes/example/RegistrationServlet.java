package example;

import javax.servlet.http.*;
import javax.servlet.*;

public class RegistrationServlet extends HttpServlet {
  public void service(HttpServletRequest req, HttpServletResponse res) 
    throws ServletException, java.io.IOException {
    System.out.println(this);
  }
}
