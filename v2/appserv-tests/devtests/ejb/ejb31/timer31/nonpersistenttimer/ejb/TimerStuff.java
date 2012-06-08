package com.sun.s1asdev.ejb31.timer.nonpersistenttimer;

import java.io.Serializable;
import java.util.Date;
import javax.ejb.*;

public interface TimerStuff {

    Timer createTimer(long duration, String info) throws Exception;

    Timer createTimer(long duration) throws  Exception;
    Timer createTimer(long duration, long interval) throws  Exception;
    Timer createTimer(long duration, long interval, String info) throws  Exception;
    Timer createTimer(Date expirationTime) throws  Exception;
    Timer createTimer(Date expirationTime, long interval) throws  Exception;

    void createTimerAndRollback(long duration) throws  Exception;

    void createTimerAndCancel(long duration) throws  Exception;

    void createTimerAndCancelAndCancel(long duration) throws  Exception;

    void createTimerAndCancelAndRollback(long duration) throws  Exception;

    void cancelTimerNoError(Timer timer) throws  Exception;
    void cancelTimer(Timer timer) throws  Exception;

    void cancelTimerAndRollback(Timer timer) throws  Exception;

    void cancelTimerAndCancel(Timer timer) throws  Exception;

    void cancelTimerAndCancelAndRollback(Timer timer) throws  Exception;

    void getTimersTest() throws  Exception;

    Timer getTimeRemainingTest1(int numIterations) throws  Exception;

    void  getTimeRemainingTest2(int numIterations, Timer th) throws  Exception;

    Timer getNextTimeoutTest1(int numIterations) throws  Exception;

    void  getNextTimeoutTest2(int numIterations, Timer th) throws  Exception;

    void assertNoTimers() throws  Exception;

    Serializable getInfo(Timer timer) throws  Exception;
    Serializable getInfoNoError(Timer timer) throws  Exception;

    void assertTimerNotActive(Timer timer) throws Exception;

    void sendMessageAndCreateTimer() throws  Exception;
    void recvMessageAndCreateTimer(boolean expectMessage) 
        throws  Exception;
    void sendMessageAndCreateTimerAndRollback() 
        throws  Exception;
    void recvMessageAndCreateTimerAndRollback(boolean expectMessage) 
        throws  Exception;


}
