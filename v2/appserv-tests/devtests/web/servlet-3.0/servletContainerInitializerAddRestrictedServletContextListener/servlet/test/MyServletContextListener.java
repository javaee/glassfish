package test;

import javax.servlet.*;

public class MyServletContextListener
        implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        ctx.setAttribute("testname", "testvalue");

        // Make sure that this ServletContextListener is restricted, because
        // it was registered programmatically by MyServletContainerInitializer
        try {
            ctx.addServlet("abc", "def");
            throw new RuntimeException(
                "Required IllegalStateException not thrown");
        } catch (UnsupportedOperationException e) {
            // Do nothing
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // Do nothing
    }

}
