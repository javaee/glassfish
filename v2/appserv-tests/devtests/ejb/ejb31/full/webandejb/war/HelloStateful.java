package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import java.util.concurrent.TimeUnit;

@Stateful
@LocalBean
@Local(Hello.class)
@StatefulTimeout(value=15, unit=TimeUnit.SECONDS)
public class HelloStateful {

    @Resource
    private SessionContext sesCtx;

    private HelloStateful me;

    @EJB HelloStateful2 sf2;

    // invalid    @EJB java.util.Observable sf22;


    @PostConstruct 
    private void init() {
	System.out.println("HelloStateful::init()");
       	me = sesCtx.getBusinessObject(HelloStateful.class);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public String hello() {
	
	System.out.println("get invoked interface = " + 
		     	sesCtx.getInvokedBusinessInterface() );
	
	sf2.hello();
	sf2.goodbye();

	return "hello, world!\n";
    }

    public void foo() {
	System.out.println("In HelloStateful::foo");
    }

    @Remove
    public void goodbye() {}

    @PreDestroy
    private void destroy() {
	System.out.println("HelloStateful::destroy()");
    }

    @AfterBegin
	private void afterBegin() {
	System.out.println("In HelloStateful::afterBegin()");
    }

    @BeforeCompletion
	protected void beforeCompletion() {
	System.out.println("In HelloStateful::beforeCompletion()");
    }

    @AfterCompletion
    void afterCompletion(boolean committed) {
	System.out.println("In HelloStateful::afterCompletion(). Committed = " +
			   committed);
    }


}
