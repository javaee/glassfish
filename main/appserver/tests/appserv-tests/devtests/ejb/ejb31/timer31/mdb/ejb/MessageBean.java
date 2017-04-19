package com.acme;

import javax.ejb.MessageDriven;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Timer;
import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;
import javax.interceptor.Interceptors;

import javax.annotation.Resource;
import javax.ejb.MessageDrivenContext;
import javax.jms.MessageListener;
import javax.jms.Message;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.*;
import javax.ejb.EJBException;

@Interceptors(InterceptorA.class)
@MessageDriven(mappedName="jms/ejb_ejb31_timer31_mdb_InQueue", description="mymessagedriven bean description")
 @RolesAllowed("foo")
public class MessageBean implements MessageListener {

    String mname;

    @EJB 
    private SingletonBean singleton;

    @Resource
	private MessageDrivenContext mdc;
     
    @PostConstruct
    public void init() {
        System.out.println("In MessageBean::init()");
    }

    public void onMessage(Message message) {
	System.out.println("In MessageBean::onMessage()");
	System.out.println("getCallerPrincipal = " + mdc.getCallerPrincipal());
        verifyMethodName("onMessage");
    }

    @Schedule(second="*/1", minute="*", hour="*")
    private void onTimeout() {
	System.out.println("In MessageBean::onTimeout()");
	System.out.println("getCallerPrincipal = " + mdc.getCallerPrincipal());

        verifyMethodName("onTimeout");
	try {
	    System.out.println("IsCallerInRole('foo')= " + 
			       mdc.isCallerInRole("foo"));
	    throw new EJBException("Expecting IllegalStateEXception for call to isCallerInRole() from timer callback");
	} catch(IllegalStateException ise) {
	    System.out.println("Successfully received exception for invocation of isCallerInRole from timer callback");
	}
	    
        if (singleton.getAroundTimeoutCalled(null)) {
	    singleton.test1Passed();
        }
    }

    private void onDDTimeout(Timer t) {
	System.out.println("In MessageBean::onDDTimeout()");
        if (singleton.getAroundTimeoutCalled((String)t.getInfo())) {
	    singleton.test2Passed();
        }
    }

    
    @PreDestroy
    public void destroy() {
        System.out.println("In MessageBean::destroy()");
    }

    @AroundTimeout
    private Object around_timeout(InvocationContext ctx) throws Exception {
        String info = (String)((Timer)ctx.getTimer()).getInfo();
        System.out.println("In MessageBean::AroundTimeout() for info " + info);
        singleton.setAroundTimeoutCalled(info);
        return ctx.proceed();
    }

    private void verifyMethodName(String name) {
        try {
            if (mname == null || !mname.equals(name))
                throw new EJBException("Expecting method named " + name + " got " + mname);
        } finally {
            mname = null;
        }
    }

}
