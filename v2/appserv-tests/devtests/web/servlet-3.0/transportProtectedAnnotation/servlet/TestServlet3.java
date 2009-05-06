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

@WebServlet("/myurl3")
@RolesAllowed("javaee")
public class TestServlet3 extends HttpServlet {
    @TransportProtected
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("g:Hello:" + req.getRemoteUser() + ":" + req.isSecure());
    }

    @TransportProtected(false)
    protected void doTrace(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("t:Hello:" + req.getRemoteUser() + ":" + req.isSecure());
    }
}
