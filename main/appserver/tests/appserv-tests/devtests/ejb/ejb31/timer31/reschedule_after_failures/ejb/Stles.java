package com.sun.s1asdev.ejb31.timer.reschedule_after_failures;

@javax.ejb.Remote
public interface Stles {

    public void createTimers() throws Exception;

    public void verifyTimers() throws Exception;
}
