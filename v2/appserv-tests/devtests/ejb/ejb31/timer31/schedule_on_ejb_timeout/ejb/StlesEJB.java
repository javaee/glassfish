package com.sun.s1asdev.ejb31.timer.schedule_on_ejb_timeout;


import javax.ejb.*;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

public class StlesEJB implements Stles, SessionBean, TimedObject {

    private SessionContext context;
    private static Set timers = new HashSet();

    public void ejbTimeout(Timer timer) {
        System.out.println(timer.getInfo() + " expired");
        timers.add(timer.getInfo());
    }

    public void createTimer() throws Exception {
        ScheduleExpression expression = new ScheduleExpression().second("*/2").minute("*").hour("*");
        TimerConfig config = new TimerConfig("Timer01", false);
        context.getTimerService().createCalendarTimer(expression, config);
    }

    public void setSessionContext(SessionContext context) throws EJBException, RemoteException {
        this.context = context;
    }

    public void ejbRemove() throws EJBException, RemoteException {}
    public void ejbActivate() throws EJBException, RemoteException { }
    public void ejbPassivate() throws EJBException, RemoteException { }


    public void verifyTimers() throws Exception {
        if (!timers.contains("Timer01"))
            throw new EJBException("Timer01 hadn't fired"); 
        if (!timers.contains("Timer00"))
            throw new EJBException("Timer00 hadn't fired"); 
    }

}
