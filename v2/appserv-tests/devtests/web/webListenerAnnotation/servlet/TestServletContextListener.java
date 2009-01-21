import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

@javax.servlet.annotation.WebListener
public class TestServletContextListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println(">>> contextInitialized");
        ServletContext context = sce.getServletContext();
        context.setAttribute("myattr", "myservletcontextlistener");
    }   

    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println(">>> contextDestroyed");
    }
}
