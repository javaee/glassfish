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
