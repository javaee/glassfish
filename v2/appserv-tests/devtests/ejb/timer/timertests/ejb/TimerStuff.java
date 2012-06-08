package com.sun.s1asdev.ejb.timer.timertests;

import java.io.Serializable;
import java.util.Date;
import java.rmi.RemoteException;
import javax.ejb.*;

public interface TimerStuff {

    TimerHandle createTimer(long duration, String info) throws RemoteException, Exception;

    TimerHandle createTimer(long duration) throws RemoteException, Exception;
    TimerHandle createTimer(long duration, long interval) throws RemoteException, Exception;
    TimerHandle createTimer(long duration, long interval, String info) throws RemoteException, Exception;
    TimerHandle createTimer(Date expirationTime) throws RemoteException, Exception;
    TimerHandle createTimer(Date expirationTime, long interval) throws RemoteException, Exception;

    void createTimerAndRollback(long duration) throws RemoteException, Exception;

    void createTimerAndCancel(long duration) throws RemoteException, Exception;

    void createTimerAndCancelAndCancel(long duration) throws RemoteException, Exception;

    void createTimerAndCancelAndRollback(long duration) throws RemoteException, Exception;

    void cancelTimerNoError(TimerHandle timerHandle) throws RemoteException, Exception;
    void cancelTimer(TimerHandle timerHandle) throws RemoteException, Exception;

    void cancelTimerAndRollback(TimerHandle timerHandle) throws RemoteException, Exception;

    void cancelTimerAndCancel(TimerHandle timerHandle) throws RemoteException, Exception;

    void cancelTimerAndCancelAndRollback(TimerHandle timerHandle) throws RemoteException, Exception;

    void getTimersTest() throws RemoteException, Exception;

    TimerHandle getTimeRemainingTest1(int numIterations) throws RemoteException, Exception;

    void  getTimeRemainingTest2(int numIterations, TimerHandle th) throws RemoteException, Exception;

    TimerHandle getNextTimeoutTest1(int numIterations) throws RemoteException, Exception;

    void  getNextTimeoutTest2(int numIterations, TimerHandle th) throws RemoteException, Exception;

    void assertNoTimers() throws RemoteException, Exception;

    Serializable getInfo(TimerHandle handle) throws RemoteException, Exception;
    Serializable getInfoNoError(TimerHandle handle) throws RemoteException, Exception;

    void assertTimerNotActive(TimerHandle handle) throws RemoteException;

    void sendMessageAndCreateTimer() throws RemoteException, Exception;
    void recvMessageAndCreateTimer(boolean expectMessage) 
        throws RemoteException, Exception;
    void sendMessageAndCreateTimerAndRollback() 
        throws RemoteException, Exception;
    void recvMessageAndCreateTimerAndRollback(boolean expectMessage) 
        throws RemoteException, Exception;
}
