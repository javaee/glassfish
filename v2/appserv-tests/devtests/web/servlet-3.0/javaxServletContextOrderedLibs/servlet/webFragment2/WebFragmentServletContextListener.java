package webFragment2;

import java.util.*;
import javax.servlet.*;

public class WebFragmentServletContextListener
        implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();

        List<String> orderedLibs = (List<String>) sc.getAttribute(
            ServletContext.ORDERED_LIBS);
        if ((orderedLibs == null) || (orderedLibs.size() != 2)) {
            throw new RuntimeException(
                "Missing or wrong-sized " + ServletContext.ORDERED_LIBS +
                " attribute");
        }
        if (!"webFragment2.jar".equals(orderedLibs.get(0)) ||
                !"webFragment1.jar".equals(orderedLibs.get(1))) {
            throw new RuntimeException(
                ServletContext.ORDERED_LIBS +
                " attribute has wrong contents");
        }

        ServletRegistration sreg = sc.addServlet("WebFragment2Servlet",
            "webFragment2.WebFragmentServlet");
        sreg.addMapping("/webFragment2Servlet");
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // Do nothing
    }

}
