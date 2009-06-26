package webFragment2;

import javax.servlet.*;

public class WebFragmentServletContextListener
        implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        ServletRegistration sreg =
            sce.getServletContext().addServlet("MyServlet",
                "webFragment2.WebFragmentServlet");
        sreg.addMapping("/fragmentServlet");
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // Do nothing
    }

}
