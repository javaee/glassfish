package com.acme;

import javax.ejb.*;
import javax.annotation.*;

import javax.naming.InitialContext;

@Singleton
@Startup
public class SimpleSingleton {

    @Resource 
    private SessionContext sessionCtx;

    @PostConstruct
    private void init() {
	 System.out.println("In SimpleSingleton:init()");

	 try {
	     SimpleStateless ss = (SimpleStateless)
		 sessionCtx.lookup("java:app/env/slref");
	     ss.hello();
	     System.out.println("Successfully looked up web-component defined environment dependency from @Startup Singleton @PostConstruct");
	 } catch(Exception e) {
	     throw new EJBException(e);
	 }

    }

    public void hello() {
	 System.out.println("In SimpleSingleton:hello()");
	 try {
	     SimpleStateless ss = (SimpleStateless)
		 sessionCtx.lookup("java:app/env/slref");
	     ss.hello();

	     // assumes 299 enabled new InitialContext().lookup("java:comp/BeanManager");
	 } catch(Exception e) {
	     throw new EJBException(e);
	 }
    }

     @PreDestroy
     private void destroy() {
	 System.out.println("In SimpleSingleton:destroy()");
     }

    

}
