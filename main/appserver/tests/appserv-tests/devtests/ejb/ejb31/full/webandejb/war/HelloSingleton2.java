package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.transaction.UserTransaction;

@Singleton
//@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class HelloSingleton2 {

    //@Resource
    //    private UserTransaction ut;

    @Resource 
    private SessionContext sesCtx;

    @PostConstruct
    public void init() {
	System.out.println("In HelloSingleton2::init()");
	UserTransaction ut = sesCtx.getUserTransaction();
        try {
	   ut.begin();
	   TimerService ts = sesCtx.getTimerService();
	   ts.createTimer(2000, "");
	   ut.commit();
        } catch(Exception e) {
	    try {
		ut.rollback();
	    } catch(Exception e1) { e1.printStackTrace(); }
	    e.printStackTrace();
        } 
	//throw new EJBException("HelloSingleton2 :: force init failure");
    }

    @PreDestroy 
    public void destroy() {
	System.out.println("In HelloSingleton2::destroy");
    }

    @Timeout 
    public void timeout(Timer t) {
	System.out.println("In HelloSingleton2::timeout");
    }

    //@Schedule(second="15", minute="*", persistent=false)
    public void refresh() {
	System.out.println("In HelloSingleton2:refresh()");

    }

    //@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void hello1() {
	throw new EJBException("HelloSingleton2::hello1 -- force business method runtime exception");
	//System.out.println("In HelloSingleton2:hello1()");
    }

    public void hello2() {
	System.out.println("In HelloSingleton2:hello2()");
    }

}