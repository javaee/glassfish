package com.sun.s1asdev.ejb.timer.restore;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface TimerSession extends EJBObject {

    public TimerHandle createTimer(long initialDelay, long interval, String msg) throws RemoteException;

    public void deleteTimers() throws RemoteException;

    public void migrateTimersFrom(String owner) throws RemoteException;

    public void createTimerInOtherServer(String owner, String timerId, 
                                         long initialExpiration,
                                         long intervalDuration, String info)
        throws RemoteException;
}

