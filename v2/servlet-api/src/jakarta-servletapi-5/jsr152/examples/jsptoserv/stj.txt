import javax.servlet.*;
import javax.servlet.http.*;

public class servletToJsp extends HttpServlet {

    public void doGet (HttpServletRequest request,
		       HttpServletResponse response) {

	try {
	    // Set the attribute and Forward to hello.jsp
	    request.setAttribute ("servletName", "servletToJsp");
	    getServletConfig().getServletContext().getRequestDispatcher("/jsptoserv/hello.jsp").forward(request, response);
	} catch (Exception ex) {
	    ex.printStackTrace ();
	}
    }
}
