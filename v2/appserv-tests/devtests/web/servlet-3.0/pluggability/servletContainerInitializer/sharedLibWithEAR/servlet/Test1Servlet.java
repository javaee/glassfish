package webapp1;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/mytest1")
public class Test1Servlet extends HttpServlet {
    ServletContext context;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        context = config.getServletContext();
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

	StringBuffer ret = new StringBuffer();
        String attr = (String) context.getAttribute("SHAREDLIB-1");
	ret.append(attr +";");
        attr = (String) context.getAttribute("SHAREDLIB-2");
	ret.append(attr +";");
	attr = (String) context.getAttribute("SHAREDLIB-3");
	ret.append(attr +";");
        attr = (String) context.getAttribute("SHAREDLIB-4");
	ret.append(attr +";");
        attr = (String) context.getAttribute("APPLIB-1");
	ret.append(attr +";");
	attr = (String) context.getAttribute("APPLIB-2");
	ret.append(attr +";");
        res.getWriter().write(ret.toString());
    }
}
