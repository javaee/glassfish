import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BaseTestServlet extends HttpServlet {
    @RolesAllowed("javaee")
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
