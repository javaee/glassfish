import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/myurl")
public class TestServlet extends HttpServlet {
    @PermitAll
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("p:Hello");
    }

    @RolesAllowed("javaee")
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("r:Hello, " + req.getRemoteUser() + "\n");
    }

    @DenyAll
    protected void doTrace(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("d:Hello, " + req.getRemoteUser() + "\n");
    }
}
