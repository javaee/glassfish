import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/myurl2")
@RolesAllowed("javaee")
public class TestServlet2 extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("g:Hello, " + req.getRemoteUser() + "\n");
    }

    @RolesAllowed("staff")
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("p:Hello, " + req.getRemoteUser() + "\n");
    }

    @PermitAll
    public void doTrace(HttpServletRequest req, HttpServletResponse res) 
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("t:Hello");
    }

    @DenyAll
    protected void doPut(HttpServletRequest req, HttpServletResponse res) 
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("p:Hello, " + req.getRemoteUser() + "\n");
    }
}
