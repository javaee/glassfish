package com.acme;

import javax.ejb.MessageDriven;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Timer;
import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;

import javax.jms.MessageListener;
import javax.jms.Message;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


@MessageDriven(mappedName="jms/ejb_ejb31_timer31_mdb_InQueue", description="mymessagedriven bean description")
public class MessageBean implements MessageListener {

    @EJB 
    private SingletonBean singleton;
     
    @PostConstruct
    public void init() {
        System.out.println("In MessageBean::init()");
    }

    public void onMessage(Message message) {
    }

    @Schedule(second="*/1", minute="*", hour="*")
    private void onTimeout() {
	System.out.println("In MessageBean::onTimeout()");
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
    private void around_timeout(InvocationContext ctx) throws Exception {
        String info = (String)((Timer)ctx.getTimer()).getInfo();
        System.out.println("In MessageBean::AroundTimeout() for info " + info);
        singleton.setAroundTimeoutCalled(info);
        ctx.proceed();
    }

}
