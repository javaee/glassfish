import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        boolean passed = false;

        HttpSession httpSession = request.getSession();

        // Exception expected
        Object attr1 = new MyNonSerializable();
        try {
            httpSession.setAttribute("attr1", attr1);
        } catch (IllegalArgumentException iae) {
            passed = true;
        }

        // No exception expected
        Object attr2 = new MySerializable();
        httpSession.setAttribute("attr2", attr2);

        response.getWriter().print(passed);
    }

    private static class MyNonSerializable {

        public MyNonSerializable() {
        }
    }

    private static class MySerializable implements Serializable {

        public MySerializable() {
        }
    }

}

