package com.sun.s1asdev.ejb.timer.sessiontimer;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.EJBContext;
import java.rmi.RemoteException;
import javax.naming.*;

public class TimerSessionEJB implements TimedObject, SessionBean 
{
	private SessionContext context;
    private TimerSingleton singleton;

	public void ejbCreate() throws RemoteException {
	    
	    singleton = (TimerSingleton) 
		context.lookup("java:module/TimerSingleton!com.sun.s1asdev.ejb.timer.sessiontimer.TimerSingleton");

	}

	public void ejbRemove() throws RemoteException {}

	public void setSessionContext(SessionContext sc) {
		context = sc;
	}

	// business method to create a timer
	public TimerHandle createTimer(int ms) {

            try {
                System.out.println("Calling getMessageContext");
                context.getMessageContext();
            } catch(IllegalStateException ise) {
                System.out.println("getMessageContext() successfully threw illegalStateException");
            }

	    TimerService timerService = context.getTimerService();
	    Timer timer = timerService.createTimer(ms, "created timer");
	    return timer.getHandle();
	}

	// timer callback method
	public void ejbTimeout(Timer timer) {
            try {
                System.out.println("Calling getMessageContext");
                context.getMessageContext();
            } catch(IllegalStateException ise) {
                System.out.println("getMessageContext() successfully threw illegalStateException");
            }

	    singleton.setTimeoutReceived();

	}

	public void ejbActivate() {}
	public void ejbPassivate() {}
}
