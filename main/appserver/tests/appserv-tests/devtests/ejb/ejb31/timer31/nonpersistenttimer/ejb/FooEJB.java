package com.sun.s1asdev.ejb31.timer.nonpersistenttimer;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.*;


public class FooEJB extends TimerStuffImpl implements SessionBean, TimedObject {
    private SessionContext sc;

    public FooEJB(){
    }

    public void ejbTimeout(Timer t) {
        checkCallerSecurityAccess("ejbTimeout", false);

        try {
            System.out.println("In FooEJB::ejbTimeout --> " + t.getInfo());
            if (t.isPersistent())
                throw new RuntimeException("FooEJB::ejbTimeout -> " 
                       + t.getInfo() + " is PERSISTENT!!!");
        } catch(RuntimeException e) {
            System.out.println("got exception while calling getInfo");
            throw e;
        }

        try {
            handleEjbTimeout(t);
        } catch(RuntimeException re) {
            throw re;
        } catch(Exception e) {
            System.out.println("handleEjbTimeout threw exception");
            e.printStackTrace();
        }

    }

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
        setContext(sc);

        System.out.println("In FooEJB::setSessionContext");

        checkCallerSecurityAccess("setSessionContext", false);

        getTimerService("setSessionContext", false);
        doTimerStuff("setSessionContext", false);
    }

    public void ejbCreate() throws EJBException {
	System.out.println("In ejbnonpersistenttimer.Foo::ejbCreate !!");
        setupJmsConnection();
        checkGetSetRollbackOnly("ejbCreate", false);
        checkCallerSecurityAccess("ejbCreate", false);

        getTimerService("ejbCreate", true);
        doTimerStuff("ejbCreate", false);
    }

    public void ejbRemove() throws EJBException {
        System.out.println("In FooEJB::ejbRemove");
        checkCallerSecurityAccess("ejbRemove", false);
        checkGetSetRollbackOnly("ejbRemove", false);
        getTimerService("ejbRemove", true);
        doTimerStuff("ejbRemove", false);
        cleanup();
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    

}
