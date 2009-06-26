package webFragment1;

import javax.servlet.*;

public class WebFragmentServletContextListener
        implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        try {
            sce.getServletContext().addServlet("SomeServlet",
                                               "SomeServlet");
        } catch (IllegalStateException ise) {
            sce.getServletContext().setAttribute("exception", ise);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // Do nothing
    }

}
