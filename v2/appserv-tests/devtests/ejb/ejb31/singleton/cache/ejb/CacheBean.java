package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;


@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@LocalBean
@Remote(CacheRemote.class)
public class CacheBean {

    @EJB CacheBean me;
    @Resource SessionContext sesCtx;
    private AtomicInteger refreshCount = new AtomicInteger();

    private volatile boolean failure = false;
    private volatile boolean finishedInit = false;
    private String failureMsg;
    private volatile boolean shutdown = false;

       @PostConstruct
       private void init() {
              System.out.println("In Cache:init()");
	      me = sesCtx.getBusinessObject(CacheBean.class);
	      me.runAsync();
	      try {
		System.out.println("Blocking 2 secs in init");
		Thread.sleep(1500);
		System.out.println("Waking up from sleep in init...");
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	      finishedInit = true;
       }

    @Asynchronous
    public void fooAsync() { return; }

    public int checkCache() {

	if( failure ) {
	    throw new EJBException(failureMsg);
	}

	int refreshes = refreshCount.intValue();
	if( refreshes == 0 ) {
	    throw new EJBException("no refreshes");
	}

	System.out.println("Successful cache check total refreshes = " + refreshes);
	return refreshes;
    }

       @Schedule(second="*/1", minute="*", hour="*", persistent=false)
       private void refresh() {
	   int count = refreshCount.incrementAndGet();
	   System.out.println("In Cache:refresh() num refreshes = " + count);
       }

       @PreDestroy
       private void destroy() {
	   shutdown = true;
              System.out.println("In Cache:destroy()");
       }

    @Asynchronous
    public void runAsync() {

	if( !finishedInit ) {
	    failure = true;
	    failureMsg = "Async called before init finished";
	}

	System.out.println("In Singleton::run()");

	while(!shutdown) {

	    try {
		System.out.println("Going to sleep...");
		Thread.sleep(5000);
		System.out.println("Waking up from sleep...");
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	}

	System.out.println("Exiting Cache::run() due to shutdown");
	return;
    }
       

}
