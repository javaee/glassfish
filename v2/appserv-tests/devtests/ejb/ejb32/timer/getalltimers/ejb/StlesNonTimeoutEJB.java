package com.acme.ejb32.timer.getalltimers;


import javax.ejb.*;
import javax.annotation.Resource;

import java.util.Set;
import java.util.Collection;
import java.util.HashSet;

@Stateless
public class StlesNonTimeoutEJB implements StlesNonTimeout {

    @Resource
    private TimerService timerSvc;

    @EJB(lookup = "java:module/SingletonTimeoutEJB")
    SingletonTimeoutEJB singletonTimeoutEJB;

    static Set<String> expected_infos = new HashSet<String>();
    private static Set<String> errors = new HashSet<String>();

    /*
     * this includes "Sglt.timeout" and "Sglt.timeout.nonpersist" because
     * programmatic timers of Singleton are tested here.
     * It doesn't include "Stles.timeout.cancel" or "Stles.timeout.nonpersist"
     */
    static {
        expected_infos.add("Stles.schedule.anno");
        expected_infos.add("Stles.schedule.anno.nonpersist");
        expected_infos.add("Sglt.schedule.anno");
        expected_infos.add("Sglt.schedule.anno.nonpersist");
        expected_infos.add("Sglt.timeout");
        expected_infos.add("Sglt.timeout.nonpersist");
    }

    public void verifyAllTimers() {
        singletonTimeoutEJB.createTimerForTimeout();

        try {
            // waiting for creation
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new EJBException(e);
        }
        Collection<Timer> ts = timerSvc.getAllTimers();
        for(Timer t : ts) {
            String info = "" + t.getInfo();
            if (!expected_infos.contains(info)) {
                errors.add(info);
            }
        }
        
        if (ts.size() != expected_infos.size()) {
            printTimerInfos(ts);
            throw new EJBException("timerSvc.getAllTimers().size() = "
                    + ts.size() + " but we expect " + expected_infos.size());
        }

        if (!errors.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (String e : errors) {
                sb.append("" + e).append(", ");
            }
            throw new EJBException("Timers SHOULD NOT found for infos: " + sb.toString() );
        }
    }

    static void printTimerInfos(Collection<Timer> ts){
        StringBuffer sb = new StringBuffer("<");
        for(Timer t:ts) {
            sb.append(t.getInfo().toString()+", ");
        }
        sb.append(">");
        System.out.println(sb);
    }
}
