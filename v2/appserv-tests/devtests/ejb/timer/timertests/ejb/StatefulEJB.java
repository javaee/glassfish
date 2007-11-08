package com.sun.s1asdev.ejb.timer.timertests;

import java.rmi.RemoteException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.*;


public class StatefulEJB extends TimerStuffImpl implements SessionBean, SessionSynchronization {
    private SessionContext sc;
    private TimerHandle timerHandle;
    public StatefulEJB(){
    }

    public void setSessionContext(SessionContext sc) {
	this.sc = sc;
        setContext(sc);

        checkCallerSecurityAccess("setSessionContext", false);
        getTimerService("setSessionContext", false);
    }

    public void ejbCreate(TimerHandle th) throws RemoteException {
	System.out.println("In ejbtimer.Stateful::ejbCreate !!");

        timerHandle = th;

        checkGetSetRollbackOnly("ejbCreate", false);
        checkCallerSecurityAccess("ejbCreate", false);

        getTimerService("ejbCreate", false);

        try {
            Timer t = th.getTimer();
            throw new EJBException("shouldn't allow stateful ejbCreate to " +
                                   "access timer methods");
        } catch(IllegalStateException ise) {
            System.out.println("Successfully caught exception when trying " +
                               "access timer methods in stateful ejbCreate");
        }
    }

    public void ejbRemove() throws RemoteException {
        checkCallerSecurityAccess("ejbRemove", false);
        checkGetSetRollbackOnly("ejbRemove", false);
        getTimerService("ejbRemove", false);
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void afterBegin() {  
        System.out.println("in afterBegin"); 
        try {
            Timer t = timerHandle.getTimer();
            t.getInfo();
            TimerHandle aHandle = t.getHandle();
            java.util.Date date = t.getNextTimeout();
            System.out.println("Successfully got timer in afterBegin");
        } catch(Exception e) {
            System.out.println("Error : got exception in afterBegin");
        }
    }

    public void beforeCompletion() {
        System.out.println("in beforeCompletion"); 
        try {
            Timer t = timerHandle.getTimer();
        } catch(NoSuchObjectLocalException nsole) {
            System.out.println("Successfull got NoSuchObjectLocalException " +
                               " in beforeCompletion");
        } catch(Exception e) {
            System.out.println("Error : got exception in beforeCompletion");
            e.printStackTrace();
        }
    }

    public void afterCompletion(boolean committed) {
        System.out.println("in afterCompletion. committed = " + committed); 
        try {
            Timer t = timerHandle.getTimer();
            System.out.println("Error : should have gotten exception in " +
                               "afterCompletion");
            Thread.currentThread().dumpStack();
        } catch(IllegalStateException ise) {
            System.out.println("got expected illegal state exception in " +
                               "afterCompletion");
        } catch(Exception e) {
            System.out.println("Error : got unexpected exception in " +
                               "beforeCompletion");
            e.printStackTrace();
        }

    }

}
