package com.sun.s1asdev.ejb.timer.sessiontimer;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface TimerSession extends EJBObject {
	public TimerHandle createTimer(int ms) throws RemoteException;
}

