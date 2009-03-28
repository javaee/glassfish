package com.acme;

import javax.servlet.annotation.WebListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.annotation.PostConstruct;
import javax.naming.InitialContext;

import java.lang.reflect.Method;

import javax.ejb.EJB;

@WebListener
public class ContextListener implements ServletContextListener {

    @EJB private SimpleStateless simpleStateless;

    public void contextInitialized(ServletContextEvent sce) {

	System.out.println("In ContextListener::contextInitialized");

	try {
	    Object jaxrsEjbGlue = new InitialContext().lookup("org.glassfish.ejb.container.JaxrsEjbGlue");
	    System.out.println("jaxrsEjbGlue = " + jaxrsEjbGlue);
	    Method m = jaxrsEjbGlue.getClass().getMethod("registerInterceptor", java.lang.Object.class);
	    System.out.println("register interceptor method = " + m);

	    m.invoke(jaxrsEjbGlue, new Object());
	    
	} catch(Exception e) {
	    e.printStackTrace();
	}
	
	simpleStateless.hello();

    }

    @PostConstruct
    public void pc() {
	System.out.println("In ContextListener::postConstruct");
    }

    public void contextDestroyed(ServletContextEvent sce) {

	System.out.println("In ContextListener::contextDestroyed");

    }

}
