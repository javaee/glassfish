package com.sun.s1asdev.ejb.timer.sessiontimersecurity;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface TimerSession extends EJBObject {
	public TimerHandle createTimer(int ms) throws RemoteException;

	public void dummyBusinessMethod() throws RemoteException;
}

