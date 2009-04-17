import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

    private static final String CATALOG_OFFERS = "/catalog/offers/";
    private static final String CATALOG_MORE_OFFERS = "/catalog/moreOffers/";

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        Set<String> resPaths = getServletContext().getResourcePaths("/catalog");
        if (resPaths == null) {
            throw new ServletException("No resource paths");
        }
   
        if (!resPaths.contains(CATALOG_OFFERS)) {
            throw new ServletException(CATALOG_OFFERS +
                " missing from resource paths");
        }

        if (!resPaths.contains(CATALOG_MORE_OFFERS)) {
            throw new ServletException(CATALOG_MORE_OFFERS +
                " missing from resource paths");
        }
    }
}
