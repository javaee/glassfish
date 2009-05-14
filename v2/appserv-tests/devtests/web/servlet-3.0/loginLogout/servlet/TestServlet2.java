import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/myurl2")
public class TestServlet2 extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        StringBuilder b = new StringBuilder("g:Hello, ");
        b.append((req.getRemoteUser() == null) + ", ");
        b.append(req.isUserInRole("javaee") + ", ");

        if (req.authenticate(res)) {
            b.append(req.getRemoteUser() + ", ");
            b.append(req.isUserInRole("javaee") + ", ");
            req.logout();

            b.append((req.getRemoteUser() == null) + ", ");
            b.append(req.isUserInRole("javaee") + "\n");
            PrintWriter writer = res.getWriter();
            writer.write(b.toString());
        } 
    }
}
