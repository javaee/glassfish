package com.sun.s1asdev.ejb.timer.timerserialization.ejb;

import javax.ejb.TimedObject;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import java.rmi.RemoteException;

public class TimerSFSBEJB
    implements SessionBean 
{
	private SessionContext context;
    private String timerName;
    private Context initialCtx;
    private Timer timer;

	public void ejbCreate(String timerName) {
        this.timerName = timerName;
    }

    public String getName() {
        return this.timerName;
    }

	public void setSessionContext(SessionContext sc) {
		this.context = sc;
        try {
            this.initialCtx = new InitialContext();
        } catch (Throwable th) {
            th.printStackTrace();
        }
	}

	// business method to create a timer
	public void createTimer(int ms)
        throws RemoteException
    {
        try {
            InitialContext initialCtx = new InitialContext();
            TimerSLSBHome home = (TimerSLSBHome) initialCtx.lookup("java:comp/env/ejb/TimerSLSB");
		    TimerSLSB slsb = (TimerSLSB) home.create();
		    timer = slsb.createTimer(ms);
            System.out.println ("PG-> after createTimer()");
        } catch (Exception ex) {
            throw new RemoteException("Exception during TimerSFSBEJB::createTimer", ex);
        }
	}

	public long getTimeRemaining() {
            long timeRemaining = -1;
            try {
                timeRemaining = timer.getTimeRemaining();
            } catch(NoSuchObjectLocalException nsole) {
                System.out.println("Timer was cancelled, but that's ... OK!");
            }
            return timeRemaining;
        }

	public TimerHandle getTimerHandle() {
            TimerHandle handle = null;
            try {
                handle = timer.getHandle();
            } catch(NoSuchObjectLocalException nsole) {
                System.out.println("Timer was cancelled, but that's ... OK!");
            }
            return handle;
        }

	public void cancelTimer() {
            try {
                timer.cancel();
            } catch(NoSuchObjectLocalException nsole) {
                System.out.println("Timer was cancelled, but that's ... OK!");
            }
    }


	public void ejbRemove() {}

	public void ejbActivate() {
        System.out.println ("In TimerSFSB.activate()");
    }

	public void ejbPassivate() {
        System.out.println ("In TimerSFSB.passivate()");
    }
}
