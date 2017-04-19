package com.sun.s1asdev.ejb.timer.timerserialization.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface TimerSFSB
    extends EJBObject
{

	public void createTimer(int ms)
        throws RemoteException;

	public String getName()
        throws RemoteException;

	public long getTimeRemaining()
        throws RemoteException;

	public TimerHandle getTimerHandle()
        throws RemoteException;

	public void cancelTimer()
        throws RemoteException;

}

