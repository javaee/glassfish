package test;

import java.util.Set;
import javax.servlet.*;

public class MyServletContainerInitializer
        implements ServletContainerInitializer {

    public void onStartup(Set<Class<?>> c, ServletContext ctx) {
        ctx.addListener("test.MyServletContextListener");
    }
}
