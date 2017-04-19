package com.sun.s1asdev.ejb.timer.timerserialization.ejb;

import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;

public interface TimerSFSBHome
    extends EJBHome
{
	public TimerSFSB create(String timerName)
        throws CreateException, RemoteException;
}
