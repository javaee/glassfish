import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/myurl2")
@PermitAll
public class TestServlet2 extends BaseTestServlet2 {
    protected void doTrace(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("t:Hello");
    }

    @RolesAllowed("javaee")
    protected void doPut(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("put:Hello, " + req.getRemoteUser() + "\n");
    }
}
