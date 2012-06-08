package com.sun.s1asdev.ejb.timer.timertests;

import java.lang.reflect.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.io.Serializable;
import java.util.Date;
import java.util.Collection;
import java.util.Iterator;
import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueConnection;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.QueueSender;
import javax.jms.QueueReceiver;
import javax.jms.TextMessage;

public class TimerStuffImpl implements TimerStuff {
    
    private static final int TX_CMT = 0;
    private static final int TX_BMT = 1;
    private static final int TX_UNSPECIFIED = 2;
    
    private EJBContext context_;
    private int txMode = TX_CMT;
    
    private Queue queue;
    private QueueSession session;
    private QueueConnection connection;
    private QueueConnectionFactory qcFactory;

    public TimerStuffImpl() {}

    protected void setContext(EJBContext context) {
        context_ = context;
        try {
            InitialContext ic = new InitialContext();
            String txModeStr = (String) ic.lookup("java:comp/env/txmode");
            if( txModeStr.equals("CMT") ) {
                txMode = TX_CMT;
            } else if( txModeStr.equals("BMT") ) {
                txMode = TX_BMT;
            } else {
                txMode = TX_UNSPECIFIED;
            }
	    /** TODO
            queue = (Queue) ic.lookup("java:comp/env/jms/MyQueue");

            qcFactory = (QueueConnectionFactory) 
                ic.lookup("java:comp/env/jms/MyQueueConnectionFactory");
	    **/

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean isCMT() { return txMode == TX_CMT; }
    protected boolean isBMT() { return txMode == TX_BMT; }
    protected boolean isTxUnspecified() { return txMode == TX_UNSPECIFIED; }
    
    public TimerHandle createTimer(long duration, String info) throws RemoteException, Exception {
        TimerService ts = context_.getTimerService();
        Timer t = ts.createTimer(duration, info);
        return t.getHandle();
    }

    public TimerHandle createTimer(long duration) throws RemoteException, Exception {
        return createTimer(duration, "createTimer<long>" + duration);
    }

    public TimerHandle createTimer(long initialDuration, long interval) 
        throws RemoteException, Exception {
        return createTimer(initialDuration, interval, 
                           "createTimer<long, long>" + initialDuration +
                           "," + interval);
    }

    public TimerHandle createTimer(long initialDuration, long interval, 
                                   String info) 
        throws RemoteException, Exception {
        if( isBMT() ) {
            context_.getUserTransaction().begin();
        }
        TimerService ts = context_.getTimerService();
        Timer t = ts.createTimer(initialDuration, interval, 
                                 new AppInfo(info));
                                 
        if( isBMT() ) {
            context_.getUserTransaction().commit();
        }
        return t.getHandle();
    }

    public TimerHandle createTimer(Date expirationTime) 
        throws RemoteException, Exception {
        TimerService ts = context_.getTimerService();
        if( isBMT() ) {
            context_.getUserTransaction().begin();
        }
        // NOTE : in tx_unspecified case, technically there is a race
        // condition if the timer expires before getHandle is called.
        // This should happen very rarely since container puts the brakes
        // on a bit by adding a few seconds if, upon creation, the expiration
        // time has already passed.
        Timer t = ts.createTimer(expirationTime,
                                 "createTimer<Date>" + expirationTime);
        if( isBMT() ) {
            context_.getUserTransaction().commit();
        }
        return t.getHandle();
    }

    public TimerHandle createTimer(Date expirationTime, long interval) 
        throws RemoteException, Exception {

        if( isBMT() ) {
            context_.getUserTransaction().begin();
        }
        TimerService ts = context_.getTimerService();
        Timer t = ts.createTimer(expirationTime, interval,
                                 "createTimer<Date, long>" + expirationTime +
                                 "," + interval);
        if( isBMT() ) {
            context_.getUserTransaction().commit();
        }
        return t.getHandle();
    }

    public void createTimerAndRollback(long duration) throws RemoteException, Exception {
        if( isTxUnspecified() ) {
            return;
        } else if( isBMT() ) {
            context_.getUserTransaction().begin();
        }
        TimerService ts = context_.getTimerService();
        Timer t = ts.createTimer(duration, "createTimerAndRollback" + duration);
        if( isBMT() ) {
            context_.getUserTransaction().rollback();
        } else {
            context_.setRollbackOnly();
        }
    }

    public void createTimerAndCancel(long duration) throws RemoteException, Exception {
        if( isBMT() ) {
            context_.getUserTransaction().begin();
        }
        TimerService ts = context_.getTimerService();
        Timer t = ts.createTimer(duration, "createTimerAndCancel" + duration);
        t.cancel();
        if( isBMT() ) {
            context_.getUserTransaction().commit();
        }
    }

    public void createTimerAndCancelAndCancel(long duration) throws RemoteException, Exception {
        if( isTxUnspecified() ) {
            return; 
        } else if( isBMT() ) {
            context_.getUserTransaction().begin();
        }
        TimerService ts = context_.getTimerService();
        Timer t = ts.createTimer(duration, "createTimerAndCancelAndCancel" 
                                 + duration);
        t.cancel();
        t.cancel();
        if( isBMT() ) {
            context_.getUserTransaction().commit();
        }
    }

    public void createTimerAndCancelAndRollback(long duration) 
        throws RemoteException, Exception {
        if( isTxUnspecified() ) {
            return;
        } else if( isBMT() ) {
            context_.getUserTransaction().begin();
        }
        TimerService ts = context_.getTimerService();
        Timer t = ts.createTimer(duration, "createTimerAndCancelAndRollback" 
                                 + duration);
        t.cancel();
        if( isBMT() ) {
            context_.getUserTransaction().rollback();
        } else {
            context_.setRollbackOnly();
        }
    }

    public void cancelTimerNoError(TimerHandle timerHandle) throws RemoteException, Exception {
        cancelTimer(timerHandle, false);
    }

    public void cancelTimer(TimerHandle timerHandle) throws RemoteException, Exception {
        cancelTimer(timerHandle, true);
    }

    private void cancelTimer(TimerHandle timerHandle, boolean throwError) throws RemoteException, Exception {
        if( isBMT() ) {
            context_.getUserTransaction().begin();
        }
        try {
            Timer t = timerHandle.getTimer();
            t.cancel();
        } catch(Exception e) {
            if( throwError ) {
                throw new RemoteException("", e);
            }
        } finally {
            if( isBMT() ) {
                context_.getUserTransaction().commit();
            }
        }
    }

    public void cancelTimerAndCancel(TimerHandle timerHandle) throws RemoteException, Exception {
        if( isBMT() ) {
            context_.getUserTransaction().begin();
        }
        Timer t = timerHandle.getTimer();
        t.cancel();
        t.cancel();
        if( isBMT() ) {
            context_.getUserTransaction().commit();
        }
    }
    
    public void cancelTimerAndRollback(TimerHandle timerHandle) 
        throws RemoteException, Exception {
        if( isTxUnspecified() ) {
            return;
        } else if( isBMT() ) {
            context_.getUserTransaction().begin();
        }
        Timer t = timerHandle.getTimer();
        t.cancel();
        if( isBMT() ) {
            context_.getUserTransaction().rollback();
        } else {
            context_.setRollbackOnly();
        }
    }

    public void cancelTimerAndCancelAndRollback(TimerHandle timerHandle) 
        throws RemoteException, Exception {
        if( isTxUnspecified() ) {
            return;
        } else if( isBMT() ) {
            context_.getUserTransaction().begin();
        }
        Timer t = timerHandle.getTimer();
        t.cancel();
        t.cancel();
        if( isBMT() ) {
            context_.getUserTransaction().rollback();
        } else {
            context_.setRollbackOnly();
        }
    }

    private void printTimers(Collection timers) {
        System.out.println("printTimers:" + timers.size());
        int i = 0;
        for(Iterator iter = timers.iterator(); iter.hasNext();) {
            Timer t = (Timer) iter.next();
            System.out.println("timer element " + i);
            System.out.println("info = " + t.getInfo());
            System.out.println("next timeout = " + t.getNextTimeout());
            System.out.println("time remaining = " + t.getTimeRemaining());
            i++;
        }
    }

    public void getTimersTest() throws RemoteException, Exception {
        if( isBMT() ) {
            context_.getUserTransaction().begin();
        }
        TimerService ts = context_.getTimerService();
        Collection timers1= ts.getTimers();
        printTimers(timers1);
        Timer t = ts.createTimer(1000000, "getTimersTest");
        Collection timers2 = ts.getTimers();
        printTimers(timers2);
        t.cancel();
        Collection timers3 = ts.getTimers();
        printTimers(timers3);
        if( isBMT() ) {
            context_.getUserTransaction().commit();
        }
        if( (timers1.size() == timers3.size()) &&
            (timers2.size() == (timers1.size() + 1)) ) {
            // success
        } else {
            throw new RemoteException("getTimers failure");
        }                                   
    }

    public TimerHandle getTimeRemainingTest1(int numIterations) throws RemoteException, Exception {
        if( isBMT() ) {
            context_.getUserTransaction().begin();
        }
        TimerService ts = context_.getTimerService();
        Timer t = ts.createTimer(1, 1, "getTimeRemainingTest1");
        System.out.println("Remaining times for " + t.getInfo());
        for(int i = 0; i < numIterations; i++) {
            long timeRemaining = t.getTimeRemaining();
            System.out.println("Time remaining = " + timeRemaining);
            try { Thread.sleep(100); } catch(Exception e) {};
        }
        if( isBMT() ) {
            context_.getUserTransaction().commit();
        }
        return t.getHandle();
    }

    public void getTimeRemainingTest2(int numIterations, TimerHandle th) throws RemoteException, Exception {

        Timer t = th.getTimer();
        System.out.println("Remaining times for " + t.getInfo());
        for(int i = 0; i < numIterations; i++) {
            long timeRemaining = t.getTimeRemaining();
            System.out.println("Time remaining = " + timeRemaining);
            try { Thread.sleep(100); } catch(Exception e) {};
        }
    }

    public TimerHandle getNextTimeoutTest1(int numIterations) throws RemoteException, Exception {
        if( isBMT() ) {
            context_.getUserTransaction().begin();
        }
        TimerService ts = context_.getTimerService();
        Timer t = ts.createTimer(1, 1, "getNextTimeoutTest1");
        System.out.println("Remaining times for " + t.getInfo());
        for(int i = 0; i < numIterations; i++) {
            Date nextTimeout = t.getNextTimeout();
            System.out.println("Next timeout = " + nextTimeout);
            try { Thread.sleep(100); } catch(Exception e) {};
        }
        if( isBMT() ) {
            context_.getUserTransaction().commit();
        }
        return t.getHandle();
    }

    public void getNextTimeoutTest2(int numIterations, TimerHandle th) throws RemoteException, Exception {

        Timer t = th.getTimer();
        System.out.println("Remaining times for " + t.getInfo());
        for(int i = 0; i < numIterations; i++) {
            Date nextTimeout = t.getNextTimeout();
            System.out.println("Next timeout = " + nextTimeout);
            try { Thread.sleep(100); } catch(Exception e) {};
        }
    }

    // Make sure there are no active timers.  
    public void assertNoTimers() throws RemoteException, Exception {
        checkCallerSecurityAccess("assertNoTimers", true);
        TimerService ts = context_.getTimerService();
        Collection timers= ts.getTimers();
        if(!timers.isEmpty()) {
            throw new RemoteException(timers.size() + " timers still exist");
        }
    }

    public void assertTimerNotActive(TimerHandle timerHandle) 
        throws RemoteException {
        try {
            Timer t = timerHandle.getTimer();
            throw 
                new Exception("assertTimerNotActive called with active timer");
        } catch(NoSuchObjectLocalException nsole) {
            // caught expected exception
        } catch(Throwable t) {
            throw new RemoteException("caught wrong exception", t);
        }
    }

    public Serializable getInfoNoError(TimerHandle timerHandle) 
        throws Exception {
        return getInfo(timerHandle, false);
        
    }

    public Serializable getInfo(TimerHandle timerHandle) throws Exception {
        return getInfo(timerHandle, true);
    }

    public void sendMessageAndCreateTimer() throws RemoteException, Exception {
        if( isTxUnspecified() ) {
            return;
        }
        try {
            if( isBMT() ) {
                context_.getUserTransaction().begin();
            }
            sendMessageAndCreateTimer("sendMessageAndCreateTimer");
        } catch(Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage(), e);
        } finally {
            if( isBMT() ) {
                context_.getUserTransaction().commit();
            }
        }
        return;
    }

    public void sendMessageAndCreateTimerAndRollback() throws RemoteException, Exception {
        if( isTxUnspecified() ) {
            return;
        }
        try {

            if( isBMT() ) {
                context_.getUserTransaction().begin();
            }

            sendMessageAndCreateTimer("sendMessageAndCreateTimerAndRollback");
        } catch(Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage(), e);
        } finally {
            if( isBMT() ) {
                context_.getUserTransaction().rollback();
            } else {
                context_.setRollbackOnly();
            }
        }
        return;
    }

    public void recvMessageAndCreateTimer(boolean expectMessage) 
        throws RemoteException, Exception {
        if( isTxUnspecified() ) {
            return;
        }
        try {
            
            if( isBMT() ) {
                context_.getUserTransaction().begin();
            }

            recvMessageAndCreateTimer("recvMessageAndCreateTimer", 
                                      expectMessage);

        } catch(Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage(), e);
        } finally {
            if( isBMT() ) {
                context_.getUserTransaction().commit();
            }
        }
        return;
    }

    public void recvMessageAndCreateTimerAndRollback(boolean expectMessage) 
        throws RemoteException, Exception {

        if( isTxUnspecified() ) {
            return;
        }

        try {
            if( isBMT() ) {
                context_.getUserTransaction().begin();
            }

            recvMessageAndCreateTimer("recvMessageAndCreateTimerAndRollback",
                                      expectMessage);

        } catch(Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage(), e);
        } finally {
            if( isBMT() ) {
                context_.getUserTransaction().rollback();
            } else {
                context_.setRollbackOnly();
            }
        }
        return;
    }

    private void sendMessageAndCreateTimer(String timerName)
        throws Exception {
        try {
            QueueSender sender = session.createSender(queue);
            // Send a message.
            TextMessage message = session.createTextMessage();
            message.setText(timerName);
            sender.send(message);
            TimerService ts = context_.getTimerService();
            Timer t = ts.createTimer(1, timerName);
            TimerHandle th = t.getHandle();
        } catch(Exception e) {
            throw e;
        } 
        return;
    }

    private void recvMessageAndCreateTimer(String timerName,
                                           boolean expectMessage)
        throws Exception {
        try {
            QueueReceiver receiver = session.createReceiver(queue);
            // Send a message.
            TextMessage message = (TextMessage) receiver.receiveNoWait();
            
            if( message == null ) {
                if( expectMessage ) {
                    throw new RemoteException("Expecting message in " +
                                              "recvMessageAndCreateTimerAndRollback");           
                }
            } else {
                if( !expectMessage ) {
                    throw new RemoteException("shouldn't have gotten msg " +
                                              message.getText());
                }
            }
            
            TimerService ts = context_.getTimerService();
            Timer t = ts.createTimer(1, timerName);
            TimerHandle th = t.getHandle();
        } catch(Exception e) {
            throw e;
        } 

        return;
    }

    private void timerAfterCancelTest(Timer t) {

        try {
            t.cancel();
            throw new EJBException("timer " + t + " should have thrown " +
                " an exception since it was accessed after having been " +
                                   "cancelled");
        } catch(NoSuchObjectLocalException fe) {
            System.out.println("Successfully got exception after accessing " +
                               "cancelled timer " + t);
        }

        try {
            t.getInfo();
            throw new EJBException("timer " + t + " should have thrown " +
                " an exception since it was accessed after having been " +
                                   "cancelled");
        } catch(NoSuchObjectLocalException fe) {
            System.out.println("Successfully got exception after accessing " +
                               "cancelled timer " + t);
        }

        try {
            t.getHandle();
            throw new EJBException("timer " + t + " should have thrown " +
                " an exception since it was accessed after having been " +
                                   "cancelled");
        } catch(NoSuchObjectLocalException fe) {
            System.out.println("Successfully got exception after accessing " +
                               "cancelled timer " + t);
        }

        try {
            t.getNextTimeout();
            throw new EJBException("timer " + t + " should have thrown " +
                " an exception since it was accessed after having been " +
                                   "cancelled");
        } catch(NoSuchObjectLocalException fe) {
            System.out.println("Successfully got exception after accessing " +
                               "cancelled timer " + t);
        }

        try {
            t.getTimeRemaining();
            throw new EJBException("timer " + t + " should have thrown " +
                " an exception since it was accessed after having been " +
                                   "cancelled");
        } catch(NoSuchObjectLocalException fe) {
            System.out.println("Successfully got exception after accessing " +
                               "cancelled timer " + t);
        }

    }

    private Serializable getInfo(TimerHandle timerHandle, boolean throwError) throws Exception {
        try {
            Timer t = timerHandle.getTimer();
            return t.getInfo();
        } catch(Exception e) {
            if( throwError ) {
                throw e;
            }
        }
        return null;
    }

    protected void getTimerService(String method, boolean allowed) {
        try {
            TimerService ts = context_.getTimerService();
            if( !allowed ) {
                throw new EJBException("Error : getTimerService should have " +
                                       "failed in " + method);
            }
        } catch(IllegalStateException e) {
            if( allowed ) {
                throw new EJBException("Error : getTimerService should have " +
                                       "succeeded in " + method);
            }
        }
    }

    protected void doTimerStuff(String method, boolean allowed) {
        boolean txStarted = false;
        try {
            if( isTxUnspecified() ) {
                return;
            }
            TimerService ts = context_.getTimerService();
            if( isBMT() ) {
                context_.getUserTransaction().begin();
                txStarted = true;
            }
            Timer t = ts.createTimer(1, 1, "doTimerStuff_" + method);
            TimerHandle th = t.getHandle();
            t = th.getTimer();
            t.cancel();
            if( !allowed ) {
                throw new EJBException("Error : doTimerStuff should have " +
                                       "failed in " + method);
            }
        } catch(IllegalStateException ise) {
            if( allowed ) {
                throw new EJBException("Error : doTimerStuff should have " +
                                       "succeeded in " + method);
            }
        } catch(EJBException ejbe) {
            throw ejbe;
        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException("Caught unexpected exception in " +
                                   " doTimerStuff " + method);
        } finally {
            if( txStarted ) {
                try {
                    context_.getUserTransaction().commit();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void handleEjbTimeout(Timer t) throws RemoteException, Exception {
        Serializable info = t.getInfo();
        String infoString = info.toString();
        TimerHandle th = t.getHandle();
        if( infoString.startsWith("cancelTimer") ) {
            try {
                Method m = this.getClass().getMethod
                    (infoString, new Class[] {TimerHandle.class});
                System.out.println("Invoking " + infoString + " in handleEjbTimeout");
                m.invoke(this, new Object[] { th });
            } catch(InvocationTargetException ite) {
                throw new Exception(ite.getCause());
            } catch(Exception e) {
                throw e;
            }
        } else if( infoString.startsWith("RuntimeException") ) {
            System.out.println("Causing runtime exception from ejbTimeout");
            throw new RuntimeException("force ejbtimeout delivery");
        } else if( infoString.startsWith("setRollbackOnly") ) {
            if( isCMT() ) {
                System.out.println("setRollbackOnly from ejbTimeout");
                context_.setRollbackOnly();
            }
        } else {
            getTimerService("ejbTimeout", true);
            doTimerStuff("ejbTimeout", true);
        } 
    }

    protected void checkCallerSecurityAccess(String method, boolean allowed) {
        try {
            context_.getCallerPrincipal();
            if( !allowed ) {
                throw new IllegalStateException(method + " should NOT have " +
                                                "access to getCallerPrincipal");
            }
        } catch(IllegalStateException ise) {
            if( allowed ) {
                ise.printStackTrace();
                throw new IllegalStateException(method + " should have access" +
                                                "to getCallerPrincipal");
            }
        }

    }

    protected void checkGetSetRollbackOnly(String method, boolean allowed) {

        allowed = allowed && isBMT();

        try {
            context_.getRollbackOnly();
            if( !allowed ) {
                throw new IllegalStateException(method + " should NOT have " +
                                                "access to getRollbackOnly");
            }
        } catch(IllegalStateException ise) {
            if( allowed ) {
                ise.printStackTrace();
                throw new IllegalStateException(method + " should have access" +
                                                "to getRollbackOnly");
            }
        }

    }


    protected void setupJmsConnection() {
	/** TODO
	
        try {
            InitialContext ic = new InitialContext();
            connection = qcFactory.createQueueConnection();
            session = connection.createQueueSession(true, 0);
            connection.start();
        } catch(Exception e) { e.printStackTrace(); }
	*/
    }

    protected void cleanup() {
	/** TODO
        if( connection != null ) {
            try {
                connection.close();
                connection = null;
            } catch(JMSException jmse) {
                jmse.printStackTrace();
            }
        }
	**/
    }


}
