package com.acme;

import javax.servlet.annotation.WebListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.annotation.PostConstruct;
import javax.naming.InitialContext;

import java.lang.reflect.Method;


@WebListener
public class ContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {

	System.out.println("In ContextListener::contextInitialized");

	try {
	    Object jaxrsEjbGlue = new InitialContext().lookup("java:org.glassfish.ejb.container.interceptor_binding_spi");
	    System.out.println("jaxrsEjbGlue = " + jaxrsEjbGlue);
	    Method m = jaxrsEjbGlue.getClass().getMethod("registerInterceptor", java.lang.Object.class);
	    System.out.println("register interceptor method = " + m);

	    m.invoke(jaxrsEjbGlue, new com.sun.jersey.JerseyInterceptor());
	    
	} catch(Exception e) {
	    e.printStackTrace();
	}

    }

    @PostConstruct
    public void pc() {
	System.out.println("In ContextListener::postConstruct");
    }

    public void contextDestroyed(ServletContextEvent sce) {

	System.out.println("In ContextListener::contextDestroyed");

    }

}
