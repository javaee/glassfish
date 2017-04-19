package com.acme;

import javax.ejb.*;
import javax.annotation.*;
import javax.interceptor.*;

import javax.inject.Inject;

import javax.naming.InitialContext;

import javax.enterprise.inject.spi.BeanManager;

import javax.enterprise.event.Event;

import java.lang.reflect.Method;

import org.jboss.weld.examples.translator.*;

@Singleton
@Startup
public class SingletonBean2 {

    private final Event<SomeEvent> someEvent;

    @Inject
    public SingletonBean2(Event<SomeEvent> se) {
	someEvent = se;
	System.out.println("In SingletonBean2 someEvent = " + someEvent);
	
    }

    @Inject Foo foo;

    @EJB
	private StatelessLocal statelessEE;

    @Inject private TranslatorController tc;

    @Inject StatelessLocal2 sl2;

    @Resource(lookup="java:module/FooManagedBean")
    private FooManagedBean fmb;

    @Resource
    private BeanManager beanManagerInject;
    
    @Resource SessionContext sesCtx;

    @PostConstruct
    public void init() {
        System.out.println("In SingletonBean2::init()");
	if( beanManagerInject == null ) {
	    throw new EJBException("BeanManager is null");
	}
	System.out.println("Bean manager inject = " + beanManagerInject);
	testIMCreateDestroyMO();
	testSingletonWithInjectionConstructor();

	System.out.println("Sending some event...");
	someEvent.fire( new SomeEvent(2) );
    }

    public void hello() {
	System.out.println("In SingletonBean2::hello() " + foo);
	statelessEE.hello();

	fmb.hello();
	
	BeanManager beanMgr = (BeanManager)
	    sesCtx.lookup("java:comp/BeanManager");
	
	System.out.println("Successfully retrieved bean manager " +
			   beanMgr + " for JCDI enabled app");
			   

    }

    @Schedule(second="*/10", minute="*", hour="*")
	private void timeout() {
	System.out.println("In SingletonBean::timeout() " + foo);

	System.out.println("tc.getText() = " + tc.getText());

	statelessEE.hello();
    }

    @PreDestroy
    public void destroy() {
        System.out.println("In SingletonBean::destroy()");
    }

    private void testIMCreateDestroyMO() {

	try {

	    // Test InjectionManager managed bean functionality
	    Object injectionMgr = new InitialContext().lookup("com.sun.enterprise.container.common.spi.util.InjectionManager");
	    Method createManagedMethod = injectionMgr.getClass().getMethod("createManagedObject", java.lang.Class.class);
	    System.out.println("create managed object method = " + createManagedMethod);
	    FooManagedBean f2 = (FooManagedBean) createManagedMethod.invoke(injectionMgr, FooManagedBean.class);
	    f2.hello();
	

	    Method destroyManagedMethod = injectionMgr.getClass().getMethod("destroyManagedObject", java.lang.Object.class);
	    System.out.println("destroy managed object method = " + destroyManagedMethod);
	    destroyManagedMethod.invoke(injectionMgr, f2);

	     FooNonManagedBean nonF = (FooNonManagedBean) createManagedMethod.invoke(injectionMgr, FooNonManagedBean.class);
	     System.out.println("FooNonManagedBean = " + nonF);
	     nonF.hello();
	     destroyManagedMethod.invoke(injectionMgr, nonF);


	} catch(Exception e) {
	    throw new EJBException(e);
	}


    }


    private void testSingletonWithInjectionConstructor() {

	try {

	    SingletonBeanA b= (SingletonBeanA) sesCtx.lookup("java:module/SingletonBeanA");
            if (b.getBar() == null) {
                throw new Exception("Bar is null");
            }

	} catch(Exception e) {
	    throw new EJBException(e);
	}


    }

}
