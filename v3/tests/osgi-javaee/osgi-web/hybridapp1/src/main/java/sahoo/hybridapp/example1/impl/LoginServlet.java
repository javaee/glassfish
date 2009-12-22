package sahoo.hybridapp.example1.impl;

import sahoo.hybridapp.example1.UserAuthService;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@WebServlet(urlPatterns = "/login")
public class LoginServlet extends HttpServlet
{
    @EJB
    UserAuthService userAuthService;

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, java.io.IOException
    {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<HTML> <HEAD> <TITLE> Login " +
                "</TITLE> </HEAD> <BODY BGCOLOR=white>");

        String name = req.getParameter("name");
        String password = req.getParameter("password");
        try
        {

            if (userAuthService.login(name, password)) {
                out.println("Welcome " + name);
            } else {
                out.println("Incorrect user name or password. Try again");
            }
        }
        catch (Exception e)
        {
            out.println("Incorrect user name or password.");
        }
        out.println("</BODY> </HTML> ");

    }
}
