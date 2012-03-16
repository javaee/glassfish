package com.sun.s1asdev.ejb32.ejblite.timer;

import java.io.Serializable;
import java.rmi.NoSuchObjectException;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.ejb.*;
import javax.annotation.Resource;

@Stateful
@TransactionManagement(TransactionManagementType.BEAN)
public class StatefulWrapperImpl implements StatefulWrapper {

    @Resource private SessionContext context;
    private Foo foo = null;

    public boolean doFooTest(boolean bmt) {
        boolean result = false;
        try {
            if (bmt) {
                foo = (Foo) context.lookup("java:global/ejb-ejb32-ejblite-timer-web/FooBMT");
            } else {
                foo = (Foo) context.lookup("java:global/ejb-ejb32-ejblite-timer-web/FooCMT");
            }
            doTest(foo);
            result = true;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void doTest(TimerStuff timerStuff) throws Exception {

        System.out.println("doTest(): creating the runtimeExTimer ");
        Timer runtimeExTimer =
            timerStuff.createTimer(1, "RuntimeException");

        System.out.println("doTest(): creating the timer");
        Timer timer = timerStuff.createTimer(1, 1);
        
        //
        System.out.println("doTest(): creating the timer2");
        Timer timer2 = timerStuff.createTimer(10000, 10000);
        
        //
        System.out.println("doTest(): creating the timer3");
        Timer timer3 = timerStuff.createTimer(new Date());
        
        //
        System.out.println("doTest(): creating the timer4");
        Timer timer4 = timerStuff.createTimer(new Date(new Date().getTime() + 2000));
        
        //
        System.out.println("doTest(): creating the timer5");
        Timer timer5 = timerStuff.createTimer(new Date(new Date().getTime() + 20000), 10000);

        System.out.println("doTest(): creating the createTimerAndRollback");
        timerStuff.createTimerAndRollback(20000);
        
        //
        System.out.println("doTest(): creating the createTimerAndCancel");
        timerStuff.createTimerAndCancel(20000);
        
        // @@@ reevaluate double cancel logic
        //timerStuff.createTimerAndCancelAndCancel(20000);
        
        //
        System.out.println("doTest(): creating the createTimerAndCancelAndRollback");
        timerStuff.createTimerAndCancelAndRollback(20000);
        
        //
        System.out.println("doTest(): creating the cancelTimer(timer2)");
        timerStuff.cancelTimer(timer2);
        System.out.println("doTest(): assertTimerNotactive(timer2)");
        timerStuff.assertTimerNotActive(timer2);

        //
        timerStuff.cancelTimerAndRollback(timer5);
        // @@@ reevaluate double cancel logic
        //timerStuff.cancelTimerAndCancelAndRollback(timer6);
        
        Timer timer7 = 
            timerStuff.createTimer(1, 1, "cancelTimer");
        Timer timer8 = 
            timerStuff.createTimer(1, 1, "cancelTimerAndRollback");
        Timer timer9 =         
            timerStuff.createTimer(1, "cancelTimerAndRollback");

        Timer timer11 = timerStuff.getTimeRemainingTest1(20);
        timerStuff.getTimeRemainingTest2(20, timer11);
        timerStuff.getTimeRemainingTest2(20, timer);
        
        Timer timer12 = timerStuff.getNextTimeoutTest1(20);
        timerStuff.getNextTimeoutTest2(20, timer12);
        timerStuff.getNextTimeoutTest2(20, timer);

        System.out.println("cancelling timer");
        timerStuff.cancelTimer(timer);

        System.out.println("cancelling timer5");
        timerStuff.cancelTimer(timer5);

        System.out.println("cancelling timer11");
        timerStuff.cancelTimer(timer11);

        System.out.println("cancelling timer12");
        timerStuff.cancelTimer(timer12);

        // It's possible that the following timers haven't expired yet
        try {
            timerStuff.cancelTimerNoError(timer8);
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            timerStuff.cancelTimerNoError(timer3);
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            timerStuff.cancelTimerNoError(timer4);
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            timerStuff.cancelTimerNoError(timer7);
        } catch(Exception e) {
            e.printStackTrace();
        }

        try {
            timerStuff.cancelTimerNoError(runtimeExTimer);
        } catch(Exception e) {
            e.printStackTrace();
        }

        timerStuff.cancelTimerNoError(null);
        try { Thread.sleep(3000); } catch(Exception e) {};
        timerStuff.assertNoTimers();
    }

    public void removeFoo() throws javax.ejb.RemoveException {
        if (foo != null) {
            foo.remove();
        }
    }
}
