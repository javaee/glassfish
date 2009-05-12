package com.acme;

import javax.ejb.MessageDriven;
import javax.ejb.EJB;
import javax.ejb.Schedule;

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
	singleton.testPassed();
    }

    
    @PreDestroy
    public void destroy() {
        System.out.println("In MessageBean::destroy()");
    }



}
