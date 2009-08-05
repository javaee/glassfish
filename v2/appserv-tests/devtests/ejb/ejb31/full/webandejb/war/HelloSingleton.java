package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import java.util.concurrent.TimeUnit;

@Singleton
@EJB(name="helloStateful", beanInterface=HelloStateful.class)
@Lock(LockType.WRITE)
@AccessTimeout(value=10, unit=TimeUnit.SECONDS)
public class HelloSingleton extends Super1  {

    @EJB 
    Hello hello;

    @EJB
    HelloStateless slsb;

    @EJB
    private HelloSingleton2 sing2;

    @Resource 
    private SessionContext sesCtx;
    
    private HelloSingleton me;

    @PostConstruct    
    //@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    private void init() {
	System.out.println("HelloSingleton::init()");
	me = sesCtx.getBusinessObject(HelloSingleton.class);

	TimerService ts = sesCtx.getTimerService();
	ts.createTimer(2000, "");
	System.out.println("Created timer in HelloSingleton");

	slsb.hello();

	slsb.helloAsync();

	//throw new EJBException("force PostConstruct failure");
    }

    @Timeout
    @Lock(LockType.WRITE)
    public void timeout(Timer t) {
	System.out.println("In HelloSingleton::timeout");
    }

    public void callSing2WithTxAndRollback() {
	// Call Singleton 2 with a transaction
	// If it's initialized lazily as a side-effect of this
	// invocation, the fact that we rollback our transaction
	// should not impact any transactional work performed in
	// its CMT/TX_REQUIRED constructor.
	try {
	    // Executes in its own transaction but throws a runtime exception.
	    sing2.hello1();
	} catch(EJBException e) {
	    // Catch the runtime exception so it doesn't affect the client
	    // tx (yet)
	}

	// Make sure we can still call the singleton after a
	// runtime exception
	sing2.hello2();

	sesCtx.setRollbackOnly();

	System.out.println("Exiting HelloSingleton::callSing2WithTxAndRollback");
    }

    public void wait(int seconds) {
	try {
	    System.out.println("In HelloSingleton::wait. Sleeping with lock...");
	    Thread.sleep(seconds * 1000);
	    System.out.println("Awake in HelloSingleton::wait. Releasing lock...");
	} catch(Exception e) {
	    System.out.println(e);
	}
    }

    @Asynchronous
    public void asyncWait(int seconds) {
	me.wait(seconds);
    }

    @AccessTimeout(value=5, unit=TimeUnit.MILLISECONDS)
    public String hello() {
	
	System.out.println("get invoked interface = " + 
			   sesCtx.getInvokedBusinessInterface());

	HelloStateful sful = (HelloStateful) sesCtx.lookup("helloStateful");
	sful.hello();
	hello.foo();
	sful.goodbye();

	return "hello, world!\n";
    }

    @Lock(LockType.READ)
    public void read() {
	System.out.println("In HelloSingleton::read()");
    }

    public void write() {
	System.out.println("In HelloSingleton::write()");
    }

    @Lock(LockType.READ)
    public void reentrantReadRead() {
	System.out.println("In HelloSingleton::ReentrantReadRead()");
	me.read();
    }

    @Lock(LockType.READ)
    public void reentrantReadWrite() {
	System.out.println("In HelloSingleton::ReentrantReadWrite()");
	try {
	    me.write();
	    throw new EJBException("Expected illegal loopback exception");
	} catch(IllegalLoopbackException ile) {
	    System.out.println("Successfully caused illegal loopback");
	}
    }

    public void reentrantWriteRead() {
	System.out.println("In HelloSingleton::ReentrantWriteRead()");
	me.read();
    }

    public void reentrantWriteWrite() {
	System.out.println("In HelloSingleton::ReentrantWriteWrite()");
	me.write();
    }

    @AccessTimeout(0)
    public void testNoWait() {
	System.out.println("In HelloSingleton::testNoWait");
    }

    @PreDestroy
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    private void destroy() {
	System.out.println("HelloSingleton::destroy()");
    }

}



 
/*
    @Lock(LockType.WRITE)
  class Super2 {

	@Lock(LockType.READ)
	public void super2() {}

	public void super22() {}

    }
*/
