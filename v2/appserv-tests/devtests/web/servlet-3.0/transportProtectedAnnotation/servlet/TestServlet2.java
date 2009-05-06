import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.TransportProtected;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/myurl2")
@TransportProtected(true)
public class TestServlet2 extends HttpServlet {
    @PermitAll
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("m:Hello:" + req.isSecure());
    }

    @TransportProtected(false)
    @RolesAllowed("javaee")
    protected void doTrace(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("mfr:Hello:" + req.getRemoteUser() + ":" + req.isSecure());
    }
}
