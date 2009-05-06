import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.security.TransportProtected;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/myurl")
public class TestServlet extends HttpServlet {
    @TransportProtected
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("c:Hello:" + req.isSecure());
    }
}
