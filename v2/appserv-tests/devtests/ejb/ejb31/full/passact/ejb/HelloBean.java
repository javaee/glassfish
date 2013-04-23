package com.acme;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import javax.annotation.*;
import java.util.concurrent.Future;

@Stateful
@Interceptors(NonSerializableInterceptor.class)
public class HelloBean implements Hello {

    @Resource
    private SessionContext sessionCtx;

    private boolean passivated = false;
    private boolean activated = false;

    @EJB private SingletonNoIntf singletonNoIntf;
   
    @EJB private SingletonMultiIntf singletonMultiIntf;

    @EJB private Local1 local1;

    @EJB private Local1 local2;

    @EJB private Remote1 remote1;

    @EJB private Remote2 remote2;

    @EJB private StatelessNoIntf statelessNoIntf;

    @EJB private StatefulNoIntf statefulNoIntf;


    @PostConstruct
    public void init() {
	System.out.println("In HelloBean::init()");
    }

    public String hello() {
	System.out.println("In HelloBean::hello()");
System.out.println("+++ sessionCtx type: " + sessionCtx.getClass());

	StatefulExpiration se = (StatefulExpiration) sessionCtx.lookup("java:module/StatefulExpiration");
	se.hello();

	singletonNoIntf.hello();
	singletonMultiIntf.hello();
	local1.hello();
	local2.hello();
	remote1.hello();
	remote2.hello();
	statelessNoIntf.hello();
	statefulNoIntf.hello();

	return "hello, world\n";
    }

    public boolean passivatedAndActivated() {
	return passivated && activated;
    }

    @PrePassivate
    public void prePass() {
	System.out.println("In HelloBean::prePass()");
	passivated = true;
    }

    @PostActivate
    public void postAct() {
	System.out.println("In HelloBean::postAct()");
        hello();
	activated = true;
    }
    


    @PreDestroy
    public void destroy() {
	System.out.println("In HelloBean::destroy()");
    }


}
