package com.sun.s1asdev.ejb.timer.timerserialization.ejb;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

public class TimerSLSBEJB
    implements TimedObject, SessionBean 
{
	private SessionContext context;

	public void ejbCreate() {}

	public void setSessionContext(SessionContext sc) {
		context = sc;
	}

	// business method to create a timer
	public Timer createTimer(int ms) {
		TimerService timerService = context.getTimerService();
		Timer timer = timerService.createTimer(ms, "created timer");
		return timer;
	}

	// timer callback method
	public void ejbTimeout(Timer timer) {
		System.out.println("TimerSLSB::ejbTimeout() invoked");
	}
    
	public void ejbRemove() {}

	public void ejbActivate() {
        System.out.println ("In TimerSLSB.activate()");
    }

	public void ejbPassivate() {
        System.out.println ("In TimerSLSB.passivate()");
    }
}
