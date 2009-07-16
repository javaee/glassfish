import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/myurl2b")
@RolesAllowed("javaee")
public class BaseTestServlet2 extends HttpServlet {
    @RolesAllowed("noSuchRole")
    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("g:Hello, " + req.getRemoteUser() + "\n");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("p:Hello, " + req.getRemoteUser() + "\n");
    }

    @DenyAll
    protected void doTrace(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("t:Hello, " + req.getRemoteUser() + "\n");
    }
}
