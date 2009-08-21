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

	    FooManagedBean f = (FooManagedBean)
		new InitialContext().lookup("java:module/FooManagedBean");
	    f.hello();

	    Object jaxrsEjbGlue = new InitialContext().lookup("java:org.glassfish.ejb.container.interceptor_binding_spi");
	    System.out.println("jaxrsEjbGlue = " + jaxrsEjbGlue);
	    Method m = jaxrsEjbGlue.getClass().getMethod("registerInterceptor", java.lang.Object.class);
	    System.out.println("register interceptor method = " + m);

	    m.invoke(jaxrsEjbGlue, new com.sun.jersey.JerseyInterceptor());


	    // Test InjectionManager managed bean functionality
	    Object injectionMgr = new InitialContext().lookup("com.sun.enterprise.container.common.spi.util.InjectionManager");
	    Method createManagedMethod = injectionMgr.getClass().getMethod("createManagedObject", java.lang.Class.class);
	    System.out.println("create managed object method = " + createManagedMethod);
	    FooManagedBean f2 = (FooManagedBean) createManagedMethod.invoke(injectionMgr, FooManagedBean.class);
	    f2.hello();
	    f2.assertInterceptorBinding();

	    Method destroyManagedMethod = injectionMgr.getClass().getMethod("destroyManagedObject", java.lang.Object.class);
	    System.out.println("destroy managed object method = " + destroyManagedMethod);
	    destroyManagedMethod.invoke(injectionMgr, f2);

	     FooNonManagedBean nonF = (FooNonManagedBean) createManagedMethod.invoke(injectionMgr, FooNonManagedBean.class);
	     System.out.println("FooNonManagedBean = " + nonF);
	     nonF.hello();
	     destroyManagedMethod.invoke(injectionMgr, nonF);
	    
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
