package ejb31.timer.methodintf;


import javax.ejb.*;
import javax.annotation.Resource;

@Stateless
public class StlesEJB implements Stles {

    @Resource TimerService ts;

    private static boolean ct = false;
    private static boolean t1 = false;
    private static boolean t2 = false;
    private static boolean t3 = false;

    @Timeout
    private void timeout1(Timer t) {
        t1 = validate(false);
        t.cancel();
    }

    @Schedule(second="*", minute="*", hour="*", info="timer-every-sec")
    private void timeout2(Timer t) {
        t2 = validate(true);
        t.cancel();
    }

    @Schedule(second="4/2", minute="*", hour="*", dayOfWeek="0-7", info="another-timer")
    private void timeout3(Timer t) {
        t3 = validate(false);
        t.cancel();
    }

    public void createTimer() {
        ct = validate(true);
        ScheduleExpression se = new ScheduleExpression().second("*/5").minute("*").hour("*");
        TimerConfig tc = new TimerConfig("timer-5-sec", true);
        ts.createCalendarTimer(se, tc);
    }

    private void log(Timer t) {
        System.out.println("in StlesEJB:timeout "  + t.getInfo() + " - persistent: " + t.isPersistent());
    }

    private boolean validate(boolean op) {
        boolean valid = true;
        try {
            javax.transaction.TransactionSynchronizationRegistry r = (javax.transaction.TransactionSynchronizationRegistry)
                   new javax.naming.InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
            System.out.println("========> TX Status for " + op + " : " + r.getTransactionStatus());
            if (op && r.getTransactionStatus() != javax.transaction.Status.STATUS_ACTIVE) {
                System.out.println("ERROR: NON-Active transaction");
                valid = false;
            } else if (!op && r.getTransactionStatus() == javax.transaction.Status.STATUS_ACTIVE) {
                System.out.println("ERROR: Active transaction");
                valid = false;
            }
        } catch(Exception e) {
            System.out.println("handleEjbTimeout threw exception");
            e.printStackTrace();
            valid = false;
        }

        return valid;
    }

    public boolean verifyTimers() {
        return ct && t1 && t2 && t3;
    }

}
